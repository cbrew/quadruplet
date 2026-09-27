package term

import "testing"

func TestTrueIsTheUnitOfConjunction(t *testing.T) {
	a, b := NewConst("a"), NewConst("b")
	if got := CreateAnd(a, True); !Equal(got, a) {
		t.Errorf("a ∧ true = %s", got)
	}
	if got := CreateAnd(True, True); !Equal(got, True) {
		t.Errorf("true ∧ true = %s", got)
	}
	if got, want := CreateAnd(a, CreateAnd(True, b)), CreateAnd(a, b); !Equal(got, want) {
		t.Errorf("a ∧ (true ∧ b) = %s, want %s", got, want)
	}
	// closing an event quantifier with \e.true
	walks := NewLam(NewExists(CreateAnd(NewApp(NewConst("walk"), NewQVar(1)), NewApp(NewVar(1), NewQVar(1)))))
	if got, want := CreateApp(walks, NewLam(True)), NewExists(NewApp(NewConst("walk"), NewQVar(1))); !Equal(got, want) {
		t.Errorf("closing: got %s, want %s", got, want)
	}
}

func TestPretty(t *testing.T) {
	walk := NewApp(NewConst("walk"), NewQVar(1))
	agent := NewApp(NewApp(NewConst("Agent"), NewQVar(1)), NewVar(1))
	a := NewLam(NewExists(CreateAnd(walk, agent)))
	b := NewLam(NewExists(CreateAnd(agent, walk)))
	if got, want := Pretty(a), "λv1.∃x1.(Agent(x1, v1) ∧ walk(x1))"; got != want {
		t.Errorf("got %s, want %s", got, want)
	}
	if Pretty(a) != Pretty(b) {
		t.Errorf("conjunct order shows: %s, %s", Pretty(a), Pretty(b))
	}
	dog := NewForall(NewImplies(NewApp(NewConst("dog"), NewQVar(1)), NewNot(NewExists(NewEquiv(NewQVar(2), NewQVar(1))))))
	if got, want := Pretty(dog), "∀x1.(dog(x1) → ¬∃x2.(x1 = x2))"; got != want {
		t.Errorf("got %s, want %s", got, want)
	}
}
