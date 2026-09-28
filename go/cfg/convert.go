package cfg

import (
	"fmt"

	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/term"
)

// FromGrammar compiles a feature grammar whose categories are all ground,
// with no two different categories that unify: for such a grammar
// unification is equality, so it is context-free, each category a symbol.
// The start symbols are the categories whose label (term.Key) is start.
// Rules are used as chart.FeatureGrammar uses them, as context-free rules.
func FromGrammar(g *grammar.Grammar, start string) (*Grammar, error) {
	var cats []*term.Map
	byHash := map[uint64][]*term.Map{}
	name := func(m *term.Map) (string, error) {
		m = term.Normalized(m).(*term.Map)
		if !m.Ground() {
			return "", fmt.Errorf("cfg: category %s has variables", m)
		}
		for _, c := range byHash[m.Hash()] {
			if term.Equal(c, m) {
				return c.String(), nil
			}
		}
		byHash[m.Hash()] = append(byHash[m.Hash()], m)
		cats = append(cats, m)
		return m.String(), nil
	}
	var rules []Rule
	for _, r := range g.Rules {
		lhs, err := name(r.LHS)
		if err != nil {
			return nil, err
		}
		rule := Rule{LHS: lhs}
		for _, m := range r.RHS {
			s, err := name(m)
			if err != nil {
				return nil, err
			}
			rule.RHS = append(rule.RHS, s)
		}
		rules = append(rules, rule)
	}
	lexicon := map[string][]string{}
	for _, w := range g.Lexicon.Words() {
		for _, m := range g.Lexicon.Lookup(w) {
			s, err := name(m)
			if err != nil {
				return nil, err
			}
			lexicon[w] = append(lexicon[w], s)
		}
	}
	var starts []string
	byKey := map[string][]*term.Map{}
	for _, a := range cats {
		k := term.Key(a)
		for _, b := range byKey[k] {
			if _, _, ok := term.UnifyBindings(a, b, nil); ok {
				return nil, fmt.Errorf("cfg: categories %s and %s differ but unify", a, b)
			}
		}
		byKey[k] = append(byKey[k], a)
		if k == start {
			starts = append(starts, a.String())
		}
	}
	return New(rules, lexicon, starts)
}
