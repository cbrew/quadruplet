package term

import (
	"fmt"
	"strconv"
)

// Subst applies the bindings to t exhaustively: a bound variable is replaced
// by its value, itself substituted, so chains such as ?x -> [p, ?y],
// ?y -> sg resolve completely. This terminates because unification's occurs
// check keeps bindings acyclic. Unchanged subterms are shared.
func (b *Bindings) Subst(t Term) Term {
	return mapVariables(t, b.resolve, true)
}

// SubstFS and SubstLambda are Subst for callers that know the kind.
func (b *Bindings) SubstFS(t FS) FS             { return mapFS(t, b.resolve, true) }
func (b *Bindings) SubstLambda(t Lambda) Lambda { return mapLambda(t, b.resolve, true) }

// the fully substituted value of a variable; an unbound variable is itself
func (b *Bindings) resolve(v Term) Term {
	r := b.Deref(v)
	if isVar(r) {
		return r
	}
	return b.Subst(r)
}

// Normalized rebuilds every node of t with the simplifying factories, so
// lambda terms built directly with constructors (as the notation parsers
// build them) take the form the factories produce. Idempotent. Substitution
// and beta reduction share unchanged subterms instead of rebuilding them, so
// they rely on their input being normalized; grammars normalize their rules
// and lexicon when loaded.
func Normalized(t Term) Term {
	return mapVariables(t, func(v Term) Term { return v }, false)
}

// mapVariables rebuilds t, replacing each unification variable v by onVar(v).
// With share set, a ground subterm, or one in which nothing was replaced, is
// returned as the same object. Rebuilt lambda nodes go through the
// factories, so the result stays simplified.
func mapVariables(t Term, onVar func(Term) Term, share bool) Term {
	switch x := t.(type) {
	case FS:
		return mapFS(x, onVar, share)
	case Lambda:
		return mapLambda(x, onVar, share)
	}
	panic(fmt.Sprintf("unexpected term %T", t))
}

func mapFS(fs FS, onVar func(Term) Term, share bool) FS {
	if share && fs.Ground() {
		return fs
	}
	m := func(t Term) Term { return mapVariables(t, onVar, share) }
	switch x := fs.(type) {
	case *SynVar:
		r, ok := onVar(x).(FS)
		if !ok {
			panic("substituting a semantic term into a syntactic variable")
		}
		return r
	case *Sem:
		v := mapLambda(x.value, onVar, share)
		if share && v == x.value {
			return x
		}
		return NewSem(v)
	case *List:
		if elems, changed := mapTerms(x.elems, m); changed || !share {
			return NewList(elems...)
		}
	case *Tuple:
		if elems, changed := mapTerms(x.elems, m); changed || !share {
			return NewTuple(elems...)
		}
	case *ListExpr: // always rebuilt, so that it is simplified
		elems, _ := mapTerms(x.elems, m)
		return NewListExpr(elems...).Simplify()
	case *TupleExpr:
		elems, _ := mapTerms(x.elems, m)
		return NewTupleExpr(elems...).Simplify()
	case *Map:
		vals, changed := mapTerms(x.vals, m)
		if changed || !share {
			return newMap(x.keys, vals)
		}
	}
	return fs // Atom, Int
}

func mapTerms(xs []Term, f func(Term) Term) ([]Term, bool) {
	out := make([]Term, len(xs))
	changed := false
	for i, x := range xs {
		out[i] = f(x)
		changed = changed || out[i] != x
	}
	return out, changed
}

func mapLambda(l Lambda, onVar func(Term) Term, share bool) Lambda {
	if share && l.Ground() {
		return l
	}
	m := func(x Lambda) Lambda { return mapLambda(x, onVar, share) }
	same := func(pairs ...Lambda) bool {
		if !share {
			return false
		}
		for i := 0; i < len(pairs); i += 2 {
			if pairs[i] != pairs[i+1] {
				return false
			}
		}
		return true
	}
	switch x := l.(type) {
	case *SemVar:
		r, ok := onVar(x).(Lambda)
		if !ok {
			panic("substituting a syntactic term into a semantic variable")
		}
		return r
	case *Lam:
		if b := m(x.body); !same(b, x.body) {
			return CreateLam(b)
		}
	case *Exists:
		if b := m(x.body); !same(b, x.body) {
			return CreateExistential(b)
		}
	case *Forall:
		if b := m(x.body); !same(b, x.body) {
			return CreateUniversal(b)
		}
	case *Not:
		if b := m(x.body); !same(b, x.body) {
			return CreateNegation(b)
		}
	case *App:
		if a, b := m(x.fn), m(x.arg); !same(a, x.fn, b, x.arg) {
			return CreateApp(a, b)
		}
	case *Implies:
		if a, b := m(x.e1), m(x.e2); !same(a, x.e1, b, x.e2) {
			return CreateImplication(a, b)
		}
	case *Equiv:
		if a, b := m(x.e1), m(x.e2); !same(a, x.e1, b, x.e2) {
			return CreateEquiv(a, b)
		}
	case *And:
		if elems, changed := mapShared(x.elems, m); changed || !share {
			return CreateAnd(elems...)
		}
	case *Or:
		if elems, changed := mapShared(x.elems, m); changed || !share {
			return CreateOr(elems...)
		}
	}
	return l // Const, Var, QVar, Box, Empty
}

// subterms returns the immediate subterms that can contain variables.
func subterms(t Term) []Term {
	switch x := t.(type) {
	case *Sem:
		return []Term{x.value}
	case *List:
		return x.elems
	case *ListExpr:
		return x.elems
	case *Tuple:
		return x.elems
	case *TupleExpr:
		return x.elems
	case *Map:
		return x.vals
	case *Lam:
		return []Term{x.body}
	case *Exists:
		return []Term{x.body}
	case *Forall:
		return []Term{x.body}
	case *Not:
		return []Term{x.body}
	case *App:
		return []Term{x.fn, x.arg}
	case *Implies:
		return []Term{x.e1, x.e2}
	case *Equiv:
		return []Term{x.e1, x.e2}
	case *And:
		return lambdasAsTerms(x.elems)
	case *Or:
		return lambdasAsTerms(x.elems)
	}
	return nil
}

func lambdasAsTerms(xs []Lambda) []Term {
	out := make([]Term, len(xs))
	for i, x := range xs {
		out[i] = x
	}
	return out
}

// Variables returns the distinct unification variables in t, in order of
// first occurrence.
func Variables(t Term) []Term {
	var acc []Term
	findVariables(t, &acc)
	return acc
}

func findVariables(t Term, acc *[]Term) {
	if t.Ground() {
		return
	}
	if isVar(t) {
		for _, v := range *acc {
			if Equal(v, t) {
				return
			}
		}
		*acc = append(*acc, t)
		return
	}
	for _, s := range subterms(t) {
		findVariables(s, acc)
	}
}

// withName is a variable of the same kind as v, called name.
func withName(v Term, name string) Term {
	if _, ok := v.(*SemVar); ok {
		return NewSemVar(name)
	}
	return NewSynVar(name)
}

// renameVariables renames variables simultaneously: each is looked up once
// in renaming, so a renaming that swaps two names does not chain.
func renameVariables(t Term, renaming map[string]string) Term {
	return mapVariables(t, func(v Term) Term {
		name, _ := varName(v)
		if fresh, ok := renaming[name]; ok {
			return withName(v, fresh)
		}
		return v
	}, true)
}

// Canonicalize renames the variables of t to ?x0, ?x1, ... in order of first
// occurrence.
func Canonicalize(t Term) Term {
	renaming := map[string]string{}
	for _, v := range Variables(t) {
		name, _ := varName(v)
		if _, done := renaming[name]; !done {
			renaming[name] = "?x" + strconv.Itoa(len(renaming))
		}
	}
	if len(renaming) == 0 {
		return t
	}
	return renameVariables(t, renaming)
}

// RenamedApart renames the variables of t that also occur in others, so t
// can be unified with them without identifying unrelated variables that
// happen to share a name ("standardizing apart"). A clashing ?a becomes the
// first of ?a1, ?a2, ... unused on either side; other variables keep their
// names.
func RenamedApart(t Term, others ...Term) Term {
	if t.Ground() {
		return t
	}
	taken := map[string]bool{}
	for _, o := range others {
		for _, v := range Variables(o) {
			name, _ := varName(v)
			taken[name] = true
		}
	}
	if len(taken) == 0 {
		return t
	}
	var mine []string
	used := map[string]bool{}
	for n := range taken {
		used[n] = true
	}
	for _, v := range Variables(t) {
		name, _ := varName(v)
		if !contains(mine, name) {
			mine = append(mine, name)
		}
		used[name] = true
	}
	renaming := map[string]string{}
	for _, old := range mine {
		if !taken[old] {
			continue
		}
		i := 1
		for used[old+strconv.Itoa(i)] {
			i++
		}
		fresh := old + strconv.Itoa(i)
		renaming[old] = fresh
		used[fresh] = true
	}
	if len(renaming) == 0 {
		return t
	}
	return renameVariables(t, renaming)
}

func contains(xs []string, s string) bool {
	for _, x := range xs {
		if x == s {
			return true
		}
	}
	return false
}
