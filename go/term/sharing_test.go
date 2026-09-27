package term

import (
	"fmt"
	"math/rand/v2"
	"testing"
)

// Shift, QShift, PlaceBoxes, SubstBoxes, quantifierBinds and instantiate
// skip subterms they cannot change, using the facts cached on each term.
// These tests compare them with full-traversal reference versions (as in
// the Kotlin LambdaSharingTest) on many random terms.

func refPlaceBoxes(e Lambda, bvi int) Lambda {
	p := func(l Lambda) Lambda { return refPlaceBoxes(l, bvi) }
	switch x := e.(type) {
	case *And:
		return NewAnd(mapLambdas(x.elems, p)...)
	case *Or:
		return NewOr(mapLambdas(x.elems, p)...)
	case *Var:
		if x.index == bvi {
			return Box
		}
		return x
	case *Forall:
		return NewForall(p(x.body))
	case *Exists:
		return NewExists(p(x.body))
	case *Not:
		return NewNot(p(x.body))
	case *Lam:
		return NewLam(refPlaceBoxes(x.body, bvi+1))
	case *App:
		return NewApp(p(x.fn), p(x.arg))
	case *Equiv:
		return NewEquiv(p(x.e1), p(x.e2))
	case *Implies:
		return NewImplies(p(x.e1), p(x.e2))
	}
	return e
}

func refSubstBoxes(e, x Lambda, bvi, qvi int) Lambda {
	s := func(l Lambda) Lambda { return refSubstBoxes(l, x, bvi, qvi) }
	switch t := e.(type) {
	case *And:
		return CreateAnd(mapLambdas(t.elems, s)...)
	case *Or:
		return CreateOr(mapLambdas(t.elems, s)...)
	case *boxTerm:
		return refQShift(refShift(x, bvi, 0), qvi, 0)
	case *Forall:
		return CreateUniversal(refSubstBoxes(t.body, x, bvi, qvi+1))
	case *Exists:
		return CreateExistential(refSubstBoxes(t.body, x, bvi, qvi+1))
	case *Not:
		return CreateNegation(s(t.body))
	case *Lam:
		return CreateLam(refSubstBoxes(t.body, x, bvi+1, qvi))
	case *App:
		return CreateApp(s(t.fn), s(t.arg))
	case *Equiv:
		return CreateEquiv(s(t.e1), s(t.e2))
	case *Implies:
		return CreateImplication(s(t.e1), s(t.e2))
	}
	return e
}

func refQShift(e Lambda, amount, qvi int) Lambda {
	s := func(l Lambda) Lambda { return refQShift(l, amount, qvi) }
	switch x := e.(type) {
	case *And:
		return CreateAnd(mapLambdas(x.elems, s)...)
	case *Or:
		return CreateOr(mapLambdas(x.elems, s)...)
	case *QVar:
		if x.index > qvi {
			return NewQVar(x.index + amount)
		}
		return x
	case *Forall:
		return CreateUniversal(refQShift(x.body, amount, qvi+1))
	case *Exists:
		return CreateExistential(refQShift(x.body, amount, qvi+1))
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

func refShift(e Lambda, amount, bvi int) Lambda {
	s := func(l Lambda) Lambda { return refShift(l, amount, bvi) }
	switch x := e.(type) {
	case *And:
		return NewAnd(mapLambdas(x.elems, s)...)
	case *Or:
		return NewOr(mapLambdas(x.elems, s)...)
	case *Var:
		if x.index > bvi {
			return NewVar(x.index + amount)
		}
		return x
	case *Forall:
		return NewForall(s(x.body))
	case *Exists:
		return NewExists(s(x.body))
	case *Not:
		return NewNot(s(x.body))
	case *Lam:
		return NewLam(refShift(x.body, amount, bvi+1))
	case *App:
		return NewApp(s(x.fn), s(x.arg))
	case *Equiv:
		return NewEquiv(s(x.e1), s(x.e2))
	case *Implies:
		return NewImplies(s(x.e1), s(x.e2))
	}
	return e
}

func refQuantifierBinds(e Lambda, bvi int) bool {
	switch x := e.(type) {
	case *QVar:
		return x.index == bvi
	case *Exists:
		return refQuantifierBinds(x.body, bvi+1)
	case *Forall:
		return refQuantifierBinds(x.body, bvi+1)
	case *Not:
		return refQuantifierBinds(x.body, bvi)
	case *Lam:
		return refQuantifierBinds(x.body, bvi)
	case *App:
		return refQuantifierBinds(x.fn, bvi) || refQuantifierBinds(x.arg, bvi)
	case *Equiv:
		return refQuantifierBinds(x.e1, bvi) || refQuantifierBinds(x.e2, bvi)
	case *Implies:
		return refQuantifierBinds(x.e1, bvi) || refQuantifierBinds(x.e2, bvi)
	case *And:
		for _, e := range x.elems {
			if refQuantifierBinds(e, bvi) {
				return true
			}
		}
	case *Or:
		for _, e := range x.elems {
			if refQuantifierBinds(e, bvi) {
				return true
			}
		}
	}
	return false
}

// randomTerm builds a random lambda term, normalized as grammar terms are.
func randomTerm(r *rand.Rand, depth int, boxes bool) Lambda {
	return Normalized(rawTerm(r, depth, boxes)).(Lambda)
}

func rawTerm(r *rand.Rand, depth int, boxes bool) Lambda {
	if depth == 0 || r.IntN(4) == 0 {
		n := 4
		if boxes {
			n = 5
		}
		switch r.IntN(n) {
		case 0:
			return NewConst([]string{"a", "b", "c"}[r.IntN(3)])
		case 1:
			return NewVar(1 + r.IntN(3))
		case 2:
			return NewQVar(1 + r.IntN(3))
		case 3:
			return NewSemVar("?f")
		default:
			return Box
		}
	}
	sub := func() Lambda { return rawTerm(r, depth-1, boxes) }
	switch r.IntN(9) {
	case 0:
		return NewLam(sub())
	case 1:
		return NewExists(sub())
	case 2:
		return NewForall(sub())
	case 3:
		return NewNot(sub())
	case 4:
		return NewAnd(sub(), sub())
	case 5:
		return NewOr(sub(), sub())
	case 6:
		return NewImplies(sub(), sub())
	case 7:
		return NewEquiv(sub(), sub())
	}
	return NewApp(sub(), sub())
}

// outcome runs f, reporting a panic as a value so outcomes can be compared.
func outcome(f func() any) (v any) {
	defer func() {
		if p := recover(); p != nil {
			v = fmt.Sprintf("panic: %v", p)
		}
	}()
	return f()
}

func same(t *testing.T, label string, input Lambda, want, got func() any) {
	t.Helper()
	w, g := outcome(want), outcome(got)
	wl, wok := w.(Lambda)
	gl, gok := g.(Lambda)
	switch {
	case wok && gok:
		// compare printed forms too, so element order must also agree
		if !Equal(wl, gl) || wl.String() != gl.String() {
			t.Fatalf("%s on %v:\n got  %v\n want %v", label, input, gl, wl)
		}
	case w != g:
		t.Fatalf("%s on %v:\n got  %v\n want %v", label, input, g, w)
	}
}

func TestSharingMatchesReference(t *testing.T) {
	r := rand.New(rand.NewPCG(2026, 927))
	for i := 0; i < 3000; i++ {
		x := randomTerm(r, 5, false)
		for _, amount := range []int{-1, 0, 1, 2} {
			same(t, fmt.Sprintf("Shift %d", amount), x,
				func() any { return refShift(x, amount, 0) }, func() any { return Shift(x, amount) })
			same(t, fmt.Sprintf("QShift %d", amount), x,
				func() any { return refQShift(x, amount, 0) }, func() any { return QShift(x, amount) })
		}
		same(t, "PlaceBoxes", x, func() any { return refPlaceBoxes(x, 1) }, func() any { return PlaceBoxes(x) })
		for bvi := 1; bvi <= 3; bvi++ {
			same(t, "quantifierBinds", x,
				func() any { return refQuantifierBinds(x, bvi) }, func() any { return quantifierBinds(x, bvi) })
		}
		withBoxes := randomTerm(r, 5, true)
		arg := randomTerm(r, 5, false)
		same(t, "SubstBoxes", withBoxes,
			func() any { return refSubstBoxes(withBoxes, arg, 0, 0) }, func() any { return SubstBoxes(withBoxes, arg) })
		for _, body := range []Lambda{x, withBoxes} {
			same(t, "beta", body,
				func() any { return refSubstBoxes(refShift(refPlaceBoxes(body, 1), -1, 0), arg, 0, 0) },
				func() any { return CreateApp(NewLam(body), arg) })
		}
	}
}

func TestUnchangedTermsAreShared(t *testing.T) {
	closed := NewLam(NewApp(NewConst("f"), NewVar(1)))
	if Shift(closed, 1) != Lambda(closed) || QShift(closed, 1) != Lambda(closed) {
		t.Error("a closed term has nothing to shift")
	}
	q := NewExists(NewApp(NewConst("p"), NewQVar(1)))
	if QShift(q, 2) != Lambda(q) {
		t.Error("a bound QVar is not shifted")
	}
	noBox := NewApp(NewConst("f"), NewConst("c"))
	if SubstBoxes(noBox, NewConst("x")) != Lambda(noBox) {
		t.Error("a Box-free term is unchanged")
	}
	kept := NewApp(NewConst("p"), NewConst("c"))
	boxed := PlaceBoxes(NewAnd(kept, NewApp(NewConst("q"), NewVar(1)))).(*And)
	if boxed.elems[0] != Lambda(kept) {
		t.Error("only the path to the replaced variable is rebuilt")
	}
}
