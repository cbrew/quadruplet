package entropy

// randomGrammar and featureGrammar are copied from package cfg's tests.

import (
	"math/rand/v2"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/term"
)

func randomGrammar(rng *rand.Rand) ([]cfg.Rule, map[string][]string) {
	syms := []string{"A", "B", "C", "D", "E"}
	var rules []cfg.Rule
	for range 8 + rng.IntN(12) {
		lhs := rng.IntN(len(syms))
		if rng.IntN(3) == 0 {
			lhs = 0 // more ways to the start symbol, so more sentences parse
		}
		k := 1 + rng.IntN(4)
		if k == 1 {
			if lhs == 0 {
				continue
			}
			rules = append(rules, cfg.Rule{LHS: syms[lhs], RHS: []string{syms[rng.IntN(lhs)]}})
			continue
		}
		rhs := make([]string, k)
		for i := range rhs {
			rhs[i] = syms[rng.IntN(len(syms))]
		}
		rules = append(rules, cfg.Rule{LHS: syms[lhs], RHS: rhs})
	}
	lexicon := map[string][]string{}
	for _, w := range []string{"x", "y", "z", "x y"} {
		for range 1 + rng.IntN(3) {
			lexicon[w] = append(lexicon[w], syms[rng.IntN(len(syms))])
		}
	}
	return rules, lexicon
}

func featureGrammar(rules []cfg.Rule, lexicon map[string][]string) *grammar.Grammar {
	g := grammar.New()
	for _, r := range rules {
		rule := &grammar.Rule{LHS: cat(r.LHS)}
		for _, s := range r.RHS {
			rule.RHS = append(rule.RHS, cat(s))
		}
		g.AddRule(rule)
	}
	for w, cats := range lexicon {
		for _, c := range cats {
			g.Lexicon.Add(w, cat(c))
		}
	}
	return g
}

func cat(name string) *term.Map {
	return term.NewMap([]string{"cat"}, []term.Term{term.NewAtom(name)})
}
