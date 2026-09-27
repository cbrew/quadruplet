package term

import "fmt"

// The Create... functions build lambda terms in simplified form: nested
// conjunctions and disjunctions are flattened, vacuous quantifiers dropped,
// double negations removed and applications of a λ beta-reduced. Terms built
// only through them are in the normal form the rest of the package relies
// on (see Normalized).

// CreateLam builds a λ; there is nothing to simplify.
func CreateLam(body Lambda) Lambda { return NewLam(body) }

// CreateEquiv builds an equivalence; there is nothing to simplify.
func CreateEquiv(e1, e2 Lambda) Lambda { return NewEquiv(e1, e2) }

// CreateAnd builds a conjunction, flattening nested conjunctions and
// dropping duplicates. A single distinct conjunct is returned by itself.
func CreateAnd(conjuncts ...Lambda) Lambda {
	flat := flatten(conjuncts, func(l Lambda) []Lambda {
		if a, ok := l.(*And); ok {
			return a.elems
		}
		return nil
	})
	flat = distinct(flat)
	if len(flat) == 1 {
		return flat[0]
	}
	return &And{termFacts: setFacts(tagAnd, flat), elems: flat}
}

// CreateOr is CreateAnd for disjunctions.
func CreateOr(disjuncts ...Lambda) Lambda {
	flat := flatten(disjuncts, func(l Lambda) []Lambda {
		if o, ok := l.(*Or); ok {
			return o.elems
		}
		return nil
	})
	flat = distinct(flat)
	if len(flat) == 1 {
		return flat[0]
	}
	return &Or{termFacts: setFacts(tagOr, flat), elems: flat}
}

func flatten(xs []Lambda, parts func(Lambda) []Lambda) []Lambda {
	nested := false
	for _, x := range xs {
		if parts(x) != nil {
			nested = true
			break
		}
	}
	if !nested {
		return xs
	}
	var out []Lambda
	var add func(Lambda)
	add = func(x Lambda) {
		if p := parts(x); p != nil {
			for _, y := range p {
				add(y)
			}
		} else {
			out = append(out, x)
		}
	}
	for _, x := range xs {
		add(x)
	}
	return out
}

// CreateNegation removes double negation and applies De Morgan's laws when
// every conjunct (or disjunct) is itself negated.
func CreateNegation(body Lambda) Lambda {
	switch b := body.(type) {
	case *Not:
		return b.body
	case *And:
		if inner, ok := allNegated(b.elems); ok {
			return CreateNegation(CreateOr(inner...))
		}
	case *Or:
		if inner, ok := allNegated(b.elems); ok {
			return CreateNegation(CreateAnd(inner...))
		}
	}
	return NewNot(body)
}

func allNegated(xs []Lambda) ([]Lambda, bool) {
	inner := make([]Lambda, len(xs))
	for i, x := range xs {
		n, ok := x.(*Not)
		if !ok {
			return nil, false
		}
		inner[i] = n.body
	}
	return inner, true
}

// CreateImplication rewrites (∃xP(x) → ∀xQ(x)) as ∀x(P(x) → Q(x)) and
// (∀xP(x) → ∃xQ(x)) as ∃x(P(x) → Q(x)).
func CreateImplication(premise, conclusion Lambda) Lambda {
	if p, ok := premise.(*Forall); ok {
		if c, ok := conclusion.(*Exists); ok {
			return CreateExistential(CreateImplication(p.body, c.body))
		}
	}
	if p, ok := premise.(*Exists); ok {
		if c, ok := conclusion.(*Forall); ok {
			return CreateUniversal(CreateImplication(p.body, c.body))
		}
	}
	return NewImplies(premise, conclusion)
}

// CreateUniversal wraps body in ∀, unless the quantifier would bind
// nothing, in which case body is returned with its free QVars shifted down.
// ∀ distributes over a conjunction.
func CreateUniversal(body Lambda) Lambda { return createQuantified(true, body) }

// CreateExistential is CreateUniversal for ∃, which distributes over a
// disjunction.
func CreateExistential(body Lambda) Lambda { return createQuantified(false, body) }

func createQuantified(universal bool, body Lambda) Lambda {
	if !quantifierBinds(body, 1) {
		return shiftQuantifiers(body, 1)
	}
	if a, ok := body.(*And); ok && universal {
		return NewAnd(mapLambdas(a.elems, CreateUniversal)...)
	}
	if o, ok := body.(*Or); ok && !universal {
		return NewOr(mapLambdas(o.elems, CreateExistential)...)
	}
	if universal {
		return NewForall(body)
	}
	return NewExists(body)
}

func mapLambdas(xs []Lambda, f func(Lambda) Lambda) []Lambda {
	out := make([]Lambda, len(xs))
	for i, x := range xs {
		out[i] = f(x)
	}
	return out
}

// quantifierBinds reports whether a quantifier wrapped around input would
// bind QVar(bvi): some QVar(i) under q quantifiers inside input has
// i - q == bvi. The cached QVar depth settles most cases without a walk.
func quantifierBinds(input Lambda, bvi int) bool {
	fq := input.FreeQVarDepth()
	if fq < bvi {
		return false
	}
	if fq == bvi {
		return true
	}
	switch x := input.(type) {
	case *QVar:
		return x.index == bvi
	case *Exists:
		return quantifierBinds(x.body, bvi+1)
	case *Forall:
		return quantifierBinds(x.body, bvi+1)
	case *Not:
		return quantifierBinds(x.body, bvi)
	case *Lam:
		return quantifierBinds(x.body, bvi)
	case *App:
		return quantifierBinds(x.fn, bvi) || quantifierBinds(x.arg, bvi)
	case *Equiv:
		return quantifierBinds(x.e1, bvi) || quantifierBinds(x.e2, bvi)
	case *Implies:
		return quantifierBinds(x.e1, bvi) || quantifierBinds(x.e2, bvi)
	case *And:
		for _, e := range x.elems {
			if quantifierBinds(e, bvi) {
				return true
			}
		}
	case *Or:
		for _, e := range x.elems {
			if quantifierBinds(e, bvi) {
				return true
			}
		}
	}
	return false
}

// shiftQuantifiers lowers by one the QVars free in input above bvi, for use
// when a vacuous quantifier around input is dropped.
func shiftQuantifiers(input Lambda, bvi int) Lambda {
	switch x := input.(type) {
	case *QVar:
		switch {
		case x.index < bvi:
			return x
		case x.index > bvi:
			return NewQVar(x.index - 1)
		default:
			panic(fmt.Sprintf("variable should not be bound: %v", x))
		}
	case *Exists:
		return CreateExistential(shiftQuantifiers(x.body, bvi+1))
	case *Forall:
		return CreateUniversal(shiftQuantifiers(x.body, bvi+1))
	case *And:
		return CreateAnd(mapLambdas(x.elems, func(e Lambda) Lambda { return shiftQuantifiers(e, bvi) })...)
	case *Or:
		return CreateOr(mapLambdas(x.elems, func(e Lambda) Lambda { return shiftQuantifiers(e, bvi) })...)
	case *Lam:
		return CreateLam(shiftQuantifiers(x.body, bvi))
	case *Not:
		return CreateNegation(shiftQuantifiers(x.body, bvi))
	case *Implies:
		return CreateImplication(shiftQuantifiers(x.e1, bvi), shiftQuantifiers(x.e2, bvi))
	case *Equiv:
		return NewEquiv(shiftQuantifiers(x.e1, bvi), shiftQuantifiers(x.e2, bvi))
	case *App:
		return CreateApp(shiftQuantifiers(x.fn, bvi), shiftQuantifiers(x.arg, bvi))
	}
	return input // Var, SemVar, Const, Box, Empty
}

// CreateApp applies predicate to argument, beta-reducing when the predicate
// is a λ.
func CreateApp(predicate, argument Lambda) Lambda {
	if l, ok := predicate.(*Lam); ok {
		return instantiate(l.body, argument)
	}
	return NewApp(predicate, argument)
}

// BetaReducible reports whether the term contains a beta redex.
func BetaReducible(l Lambda) bool {
	switch x := l.(type) {
	case *And:
		for _, e := range x.elems {
			if BetaReducible(e) {
				return true
			}
		}
	case *Or:
		for _, e := range x.elems {
			if BetaReducible(e) {
				return true
			}
		}
	case *App:
		_, isLam := x.fn.(*Lam)
		return isLam || BetaReducible(x.fn) || BetaReducible(x.arg)
	case *Exists:
		return BetaReducible(x.body)
	case *Forall:
		return BetaReducible(x.body)
	case *Not:
		return BetaReducible(x.body)
	case *Lam:
		return BetaReducible(x.body)
	case *Equiv:
		return BetaReducible(x.e1) || BetaReducible(x.e2)
	case *Implies:
		return BetaReducible(x.e1) || BetaReducible(x.e2)
	}
	return false
}

// NormalOrderReduce beta-reduces until no redex remains.
func NormalOrderReduce(l Lambda) Lambda {
	for BetaReducible(l) {
		l = betaReduce(l)
	}
	return l
}

// betaReduce performs one reduction step, on the leftmost-outermost redex.
func betaReduce(ex Lambda) Lambda {
	switch x := ex.(type) {
	case *App:
		if l, ok := x.fn.(*Lam); ok {
			return instantiate(l.body, x.arg)
		}
		if BetaReducible(x.fn) {
			return NewApp(betaReduce(x.fn), x.arg)
		}
		if BetaReducible(x.arg) {
			return NewApp(x.fn, betaReduce(x.arg))
		}
	case *And:
		return NewAnd(reduceFirst(x.elems)...)
	case *Or:
		return NewOr(reduceFirst(x.elems)...)
	case *Forall:
		return NewForall(betaReduce(x.body))
	case *Exists:
		return NewExists(betaReduce(x.body))
	case *Not:
		return NewNot(betaReduce(x.body))
	case *Lam:
		return NewLam(betaReduce(x.body))
	case *Implies:
		if BetaReducible(x.e1) {
			return NewImplies(betaReduce(x.e1), x.e2)
		}
		return NewImplies(x.e1, betaReduce(x.e2))
	case *Equiv:
		if BetaReducible(x.e1) {
			return NewEquiv(betaReduce(x.e1), x.e2)
		}
		return NewEquiv(x.e1, betaReduce(x.e2))
	}
	panic(fmt.Sprintf("unexpected failure in beta reduction: %v", ex))
}

// reduceFirst reduces the first reducible element and keeps the others.
func reduceFirst(xs []Lambda) []Lambda {
	out := make([]Lambda, len(xs))
	done := false
	for i, x := range xs {
		if !done && BetaReducible(x) {
			done = true
			out[i] = betaReduce(x)
		} else {
			out[i] = x
		}
	}
	return out
}

// instantiate reduces (λ.body) arg in one pass over body. The variable bound
// by the removed λ, Var(d + 1) under d Lams inside body, is replaced by arg
// shifted over those d Lams and the q quantifiers above it; free Vars beyond
// it move down by one; any Box already in body is replaced as SubstBoxes
// would. It equals SubstBoxes(Shift(PlaceBoxes(body), -1), arg), building
// each changed node once. Nodes containing a replacement are rebuilt with
// the simplifying factories (substitution can create new redexes), nodes
// whose Vars were only renumbered are rebuilt directly, and unchanged
// subterms are shared.
func instantiate(body, arg Lambda) Lambda {
	in := instantiation{arg: arg}
	return in.run(body, 0, 0)
}

type instantiation struct {
	arg Lambda
	// whether the subterm just processed contained a replaced variable
	hit bool
}

func (in *instantiation) replacement(d, q int) Lambda {
	in.hit = true
	return QShift(Shift(in.arg, d), q)
}

func (in *instantiation) run(e Lambda, d, q int) Lambda {
	if e.FreeVarDepth() <= d && !e.HasBox() {
		in.hit = false
		return e
	}
	switch x := e.(type) {
	case *Var:
		if x.index == d+1 {
			return in.replacement(d, q)
		}
		in.hit = false
		if x.index > d+1 {
			return NewVar(x.index - 1)
		}
		return x
	case *boxTerm:
		return in.replacement(d, q)
	case *Lam:
		b := in.run(x.body, d+1, q)
		if in.hit {
			return CreateLam(b)
		}
		return NewLam(b)
	case *Forall:
		b := in.run(x.body, d, q+1)
		if in.hit {
			return CreateUniversal(b)
		}
		return NewForall(b)
	case *Exists:
		b := in.run(x.body, d, q+1)
		if in.hit {
			return CreateExistential(b)
		}
		return NewExists(b)
	case *Not:
		b := in.run(x.body, d, q)
		if in.hit {
			return CreateNegation(b)
		}
		return NewNot(b)
	case *App:
		a, b, hit := in.run2(x.fn, x.arg, d, q)
		if hit {
			return CreateApp(a, b)
		}
		return NewApp(a, b)
	case *Implies:
		a, b, hit := in.run2(x.e1, x.e2, d, q)
		if hit {
			return CreateImplication(a, b)
		}
		return NewImplies(a, b)
	case *Equiv:
		a, b, hit := in.run2(x.e1, x.e2, d, q)
		if hit {
			return CreateEquiv(a, b)
		}
		return NewEquiv(a, b)
	case *And:
		items, hit := in.runAll(x.elems, d, q)
		if hit {
			return CreateAnd(items...)
		}
		return NewAnd(items...)
	case *Or:
		items, hit := in.runAll(x.elems, d, q)
		if hit {
			return CreateOr(items...)
		}
		return NewOr(items...)
	}
	in.hit = false // Const, QVar, SemVar, Empty
	return e
}

func (in *instantiation) run2(e1, e2 Lambda, d, q int) (Lambda, Lambda, bool) {
	a := in.run(e1, d, q)
	h := in.hit
	b := in.run(e2, d, q)
	in.hit = in.hit || h
	return a, b, in.hit
}

func (in *instantiation) runAll(xs []Lambda, d, q int) ([]Lambda, bool) {
	out := make([]Lambda, len(xs))
	any := false
	for i, x := range xs {
		out[i] = in.run(x, d, q)
		any = any || in.hit
	}
	in.hit = any
	return out, any
}

// PlaceBoxes replaces with Box the Vars bound by a λ just removed from around
// em: Var(d + 1) under d Lams.
func PlaceBoxes(em Lambda) Lambda { return placeBoxes(em, 1) }

// A subterm with no free Var at or above bvi is returned as is; so is one
// whose children all come back unchanged.
func placeBoxes(e Lambda, bvi int) Lambda {
	if e.FreeVarDepth() < bvi {
		return e
	}
	p := func(l Lambda) Lambda { return placeBoxes(l, bvi) }
	switch x := e.(type) {
	case *Var:
		if x.index == bvi {
			return Box
		}
		return x
	case *And:
		if items, changed := mapShared(x.elems, p); changed {
			return NewAnd(items...)
		}
	case *Or:
		if items, changed := mapShared(x.elems, p); changed {
			return NewOr(items...)
		}
	case *Forall:
		if b := p(x.body); b != x.body {
			return NewForall(b)
		}
	case *Exists:
		if b := p(x.body); b != x.body {
			return NewExists(b)
		}
	case *Not:
		if b := p(x.body); b != x.body {
			return NewNot(b)
		}
	case *Lam:
		if b := placeBoxes(x.body, bvi+1); b != x.body {
			return NewLam(b)
		}
	case *App:
		if a, b := p(x.fn), p(x.arg); a != x.fn || b != x.arg {
			return NewApp(a, b)
		}
	case *Equiv:
		if a, b := p(x.e1), p(x.e2); a != x.e1 || b != x.e2 {
			return NewEquiv(a, b)
		}
	case *Implies:
		if a, b := p(x.e1), p(x.e2); a != x.e1 || b != x.e2 {
			return NewImplies(a, b)
		}
	}
	return e
}

// mapShared applies f to each element and reports whether any changed.
func mapShared(xs []Lambda, f func(Lambda) Lambda) ([]Lambda, bool) {
	out := make([]Lambda, len(xs))
	changed := false
	for i, x := range xs {
		out[i] = f(x)
		changed = changed || out[i] != x
	}
	return out, changed
}

// SubstBoxes replaces every Box in e with x, shifted over the Lams and
// quantifiers above the Box, simplifying with the factories as it goes.
func SubstBoxes(e, x Lambda) Lambda { return substBoxes(e, x, 0, 0) }

// Box-free subterms are unchanged; everything else is rebuilt with the
// factories. Like instantiate, this relies on terms already being in normal
// form, which grammars ensure when they are loaded.
func substBoxes(e, x Lambda, bvi, qvi int) Lambda {
	if !e.HasBox() {
		return e
	}
	s := func(l Lambda) Lambda { return substBoxes(l, x, bvi, qvi) }
	switch t := e.(type) {
	case *boxTerm:
		return QShift(Shift(x, bvi), qvi)
	case *And:
		return CreateAnd(mapLambdas(t.elems, s)...)
	case *Or:
		return CreateOr(mapLambdas(t.elems, s)...)
	case *Forall:
		return CreateUniversal(substBoxes(t.body, x, bvi, qvi+1))
	case *Exists:
		return CreateExistential(substBoxes(t.body, x, bvi, qvi+1))
	case *Not:
		return CreateNegation(s(t.body))
	case *Lam:
		return CreateLam(substBoxes(t.body, x, bvi+1, qvi))
	case *App:
		return CreateApp(s(t.fn), s(t.arg))
	case *Equiv:
		return CreateEquiv(s(t.e1), s(t.e2))
	case *Implies:
		return CreateImplication(s(t.e1), s(t.e2))
	}
	return e
}

// QShift adds amount to the QVars free in en.
func QShift(en Lambda, amount int) Lambda { return qshift(en, amount, 0) }

// unchanged unless some QVar(i) under q quantifiers has i - q > qvi
func qshift(e Lambda, amount, qvi int) Lambda {
	if amount == 0 || e.FreeQVarDepth() <= qvi {
		return e
	}
	s := func(l Lambda) Lambda { return qshift(l, amount, qvi) }
	switch x := e.(type) {
	case *QVar:
		if x.index > qvi {
			return NewQVar(x.index + amount)
		}
		return x
	case *And:
		return CreateAnd(mapLambdas(x.elems, s)...)
	case *Or:
		return CreateOr(mapLambdas(x.elems, s)...)
	case *Forall:
		return CreateUniversal(qshift(x.body, amount, qvi+1))
	case *Exists:
		return CreateExistential(qshift(x.body, amount, qvi+1))
	case *Not:
		return NewNot(s(x.body))
	case *Lam:
		return CreateLam(s(x.body))
	case *App:
		return NewApp(s(x.fn), s(x.arg))
	case *Equiv:
		return CreateEquiv(s(x.e1), s(x.e2))
	case *Implies:
		return CreateImplication(s(x.e1), s(x.e2))
	}
	return e
}

// Shift adds n to the Vars free in em.
func Shift(em Lambda, n int) Lambda { return shift(em, n, 0) }

// unchanged unless some Var(i) under d Lams has i - d > bvi
func shift(e Lambda, amount, bvi int) Lambda {
	if amount == 0 || e.FreeVarDepth() <= bvi {
		return e
	}
	s := func(l Lambda) Lambda { return shift(l, amount, bvi) }
	switch x := e.(type) {
	case *Var:
		if x.index > bvi {
			return NewVar(x.index + amount)
		}
		return x
	case *And:
		return NewAnd(mapLambdas(x.elems, s)...)
	case *Or:
		return NewOr(mapLambdas(x.elems, s)...)
	case *Forall:
		return NewForall(s(x.body))
	case *Exists:
		return NewExists(s(x.body))
	case *Not:
		return NewNot(s(x.body))
	case *Lam:
		return NewLam(shift(x.body, amount, bvi+1))
	case *App:
		return NewApp(s(x.fn), s(x.arg))
	case *Equiv:
		return NewEquiv(s(x.e1), s(x.e2))
	case *Implies:
		return NewImplies(s(x.e1), s(x.e2))
	}
	return e
}

// Closed reports whether term has no free Var or QVar.
func Closed(term Lambda) bool { return term.FreeVarDepth() == 0 && term.FreeQVarDepth() == 0 }
