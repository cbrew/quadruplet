package term

import "testing"

func TestBindingsArePersistent(t *testing.T) {
	var empty *Bindings
	b1 := empty.Bind("?x", NewAtom("a"))
	b2 := b1.Bind("?y", NewAtom("b"))
	if b1.Len() != 1 || b2.Len() != 2 || empty.Len() != 0 {
		t.Fatalf("lengths %d %d %d", empty.Len(), b1.Len(), b2.Len())
	}
	if _, ok := b1.Lookup("?y"); ok {
		t.Error("extending must leave the original untouched")
	}
	if v, ok := b2.Lookup("?x"); !ok || v.String() != "a" {
		t.Error("earlier bindings are visible through later ones")
	}
	if got := b2.String(); got != "{?x=a, ?y=b}" {
		t.Errorf("String() = %s", got)
	}
}

func TestMapEqualityIgnoresOrder(t *testing.T) {
	a := NewMap([]string{"a", "b"}, []Term{NewAtom("p"), NewAtom("q")})
	b := NewMap([]string{"b", "a"}, []Term{NewAtom("q"), NewAtom("p")})
	if !Equal(a, b) || a.Hash() != b.Hash() {
		t.Error("maps with the same features must be equal and hash equally")
	}
	if a.String() == b.String() {
		t.Error("but they print in insertion order")
	}
	c := NewMap([]string{"a", "b", "a"}, []Term{NewAtom("p"), NewAtom("q"), NewAtom("r")})
	if c.String() != "[a=r, b=q]" {
		t.Errorf("a repeated key keeps its position and takes its last value: %s", c)
	}
}

func TestSetsDeduplicate(t *testing.T) {
	p, q := NewConst("p"), NewConst("q")
	a := NewAnd(p, q, NewConst("p"))
	if len(a.Elems()) != 2 || a.String() != "(p ∧ q)" {
		t.Errorf("NewAnd(p, q, p) = %v", a)
	}
	if !Equal(NewAnd(p, q), NewAnd(q, p)) || NewAnd(p, q).Hash() != NewAnd(q, p).Hash() {
		t.Error("conjunctions compare as sets")
	}
	if CreateAnd(p, NewAnd(q, p)).String() != "(p ∧ q)" {
		t.Error("CreateAnd flattens and deduplicates")
	}
	if CreateAnd(p, p) != Lambda(p) {
		t.Error("a single distinct conjunct stands alone")
	}
	big := make([]Lambda, 40)
	for i := range big {
		big[i] = NewConst(string(rune('a' + i%20)))
	}
	if n := len(NewOr(big...).Elems()); n != 20 {
		t.Errorf("large sets deduplicate too: %d", n)
	}
}

func TestConstructorsHashDistinctly(t *testing.T) {
	b := NewApp(NewConst("f"), NewQVar(1))
	seen := map[uint64]bool{}
	for _, l := range []Lambda{b, NewExists(b), NewForall(b), NewLam(b), NewNot(b), NewAnd(b, NewConst("c")), NewOr(b, NewConst("c"))} {
		if seen[l.Hash()] {
			t.Errorf("hash collision for %v", l)
		}
		seen[l.Hash()] = true
	}
}

func TestNormalizedIsIdempotent(t *testing.T) {
	raw := NewSem(NewNot(NewNot(NewApp(NewLam(NewApp(NewConst("f"), NewVar(1))), NewConst("c")))))
	n := Normalized(raw)
	if n.String() != "<f(c)>" {
		t.Errorf("Normalized = %v", n)
	}
	if !Equal(n, Normalized(n)) {
		t.Error("not idempotent")
	}
}

func TestNormalOrderReduce(t *testing.T) {
	redex := NewApp(NewLam(NewApp(NewConst("f"), NewVar(1))), NewConst("c"))
	a := NormalOrderReduce(NewAnd(NewConst("a"), redex))
	if a.String() != "(a ∧ f(c))" {
		t.Errorf("got %v", a)
	}
	n := NormalOrderReduce(NewNot(redex))
	if n.String() != "Not(body=f(c))" {
		t.Errorf("got %v", n)
	}
}

func TestSubstAndRenaming(t *testing.T) {
	var b *Bindings
	b = b.Bind("?x", NewList(NewAtom("p"), NewSynVar("?y"))).Bind("?y", NewAtom("sg"))
	m := NewMap([]string{"a", "b"}, []Term{NewSynVar("?x"), NewList(NewAtom("q"))})
	got := b.Subst(m).(*Map)
	if got.String() != "[a=[p, sg], b=[q]]" {
		t.Errorf("Subst = %v", got)
	}
	if got.Values()[1] != m.Values()[1] {
		t.Error("unchanged values are shared")
	}
	swapped := NewMap([]string{"a", "b"}, []Term{NewSynVar("?x1"), NewSynVar("?x0")})
	if c := Canonicalize(swapped); c.String() != "[a=?x0, b=?x1]" {
		t.Errorf("Canonicalize = %v", c)
	}
	x := NewMap([]string{"a", "b", "c"}, []Term{NewSynVar("?b"), NewSynVar("?a"), NewSynVar("?a1")})
	y := NewMap([]string{"d", "e"}, []Term{NewSynVar("?a"), NewSynVar("?a1")})
	if r := RenamedApart(x, y); r.String() != "[a=?b, b=?a2, c=?a11]" {
		t.Errorf("RenamedApart = %v", r)
	}
	mixed := NewMap([]string{"a", "sem"}, []Term{NewSynVar("?s"), NewSem(NewSemVar("?s"))})
	r := RenamedApart(mixed, NewSynVar("?s")).(*Map)
	if _, ok := r.Values()[0].(*SynVar); !ok || r.String() != "[a=?s1, sem=<?s1>]" {
		t.Errorf("each variable keeps its kind: %v", r)
	}
}
