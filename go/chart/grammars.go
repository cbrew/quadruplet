package chart

import (
	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/term"
)

// FeatureGrammar parses with a feature grammar. Its rules and lexicon are
// normalized once, when it is made, because substitution and beta
// reduction share unchanged subterms and so rely on simplified input.
type FeatureGrammar struct {
	Grammar    *grammar.Grammar
	leftCorner map[string][]*grammar.Rule
	lexicon    map[string][]*term.Map
}

// NewFeatureGrammar indexes g's rules by the key (the cat) of their first
// right-hand category.
func NewFeatureGrammar(g *grammar.Grammar) *FeatureGrammar {
	fg := &FeatureGrammar{Grammar: g, leftCorner: map[string][]*grammar.Rule{}, lexicon: map[string][]*term.Map{}}
	for _, r := range g.Rules {
		r = r.Normalized()
		k := term.Key(r.RHS[0])
		fg.leftCorner[k] = append(fg.leftCorner[k], r)
	}
	for _, w := range g.Lexicon.Words() {
		for _, m := range g.Lexicon.Lookup(w) {
			fg.lexicon[w] = append(fg.lexicon[w], term.Normalized(m).(*term.Map))
		}
	}
	return fg
}

// Spawn uses the same test as Fundamental, so a spawned edge always
// combines with the edge that spawned it.
func (g *FeatureGrammar) Spawn(lc *Edge) []*Edge {
	var out []*Edge
	for _, r := range g.leftCorner[term.Key(lc.Cat)] {
		cat := lc.Cat
		if !cat.Ground() {
			others := []term.Term{r.LHS}
			for _, m := range r.RHS {
				others = append(others, m)
			}
			cat = term.RenamedApart(cat, others...)
		}
		if _, _, ok := term.UnifyBindings(r.RHS[0], cat, nil); ok {
			needed := make([]term.Term, len(r.RHS))
			for i, m := range r.RHS {
				needed[i] = m
			}
			out = append(out, NewEdge(r.LHS, lc.Start, lc.Start, needed))
		}
	}
	return out
}

func (g *FeatureGrammar) Lookup(phrase string, start, end int) []*Edge {
	var out []*Edge
	for _, m := range g.lexicon[phrase] {
		out = append(out, NewEdge(m, start, end, nil))
	}
	return out
}

// TreeGrammar is the TreeAsFeatureGrammar of the Kotlin tests: sentences of
// "a"s with categories S[f=odd] and S[f=even] and twelve rules, giving very
// many trees.
type TreeGrammar struct{}

var (
	odd  = term.NewMap([]string{"f", "cat"}, []term.Term{term.NewAtom("odd"), term.NewAtom("S")})
	even = term.NewMap([]string{"f", "cat"}, []term.Term{term.NewAtom("even"), term.NewAtom("S")})

	treeRules = map[*term.Map][][]term.Term{
		odd: {
			{odd, odd, odd, odd}, {odd, odd, even, even}, {even, odd, even, odd},
			{even, odd, odd, even}, {odd, odd, even}, {even, odd, odd},
		},
		even: {
			{even, even, even}, {odd, even, odd}, {odd, even, odd, even},
			{odd, even, even, odd}, {even, even, even, even}, {even, even, odd, odd},
		},
	}
)

// Spawn returns edges for the rules whose first needed category is lc's;
// each rule is written lhs, rhs....
func (TreeGrammar) Spawn(lc *Edge) []*Edge {
	var out []*Edge
	for first, rules := range treeRules {
		if !term.Equal(lc.Cat, first) {
			continue
		}
		for _, r := range rules {
			out = append(out, NewEdge(r[0], lc.Start, lc.Start, r[1:]))
		}
	}
	return out
}

func (TreeGrammar) Lookup(phrase string, start, end int) []*Edge {
	if phrase == "a" {
		return []*Edge{NewEdge(odd, start, end, nil)}
	}
	return nil
}
