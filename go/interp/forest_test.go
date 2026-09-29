package interp

import (
	"math/rand/v2"
	"slices"
	"testing"

	"github.com/cbrew/quadruplet/go/cfg"
)

// randomGrammar is small and ambiguous: rules of one to four daughters, and
// unary rules only down to lower symbols, so that there are no cycles.
func randomGrammar(rng *rand.Rand) ([]cfg.Rule, map[string][]string) {
	syms := []string{"A", "B", "C", "D", "E"}
	var rules []cfg.Rule
	for range 8 + rng.IntN(12) {
		lhs := rng.IntN(len(syms))
		if rng.IntN(3) == 0 {
			lhs = 0
		}
		k := 1 + rng.IntN(4)
		if k == 1 {
			if lhs > 0 {
				rules = append(rules, cfg.Rule{LHS: syms[lhs], RHS: []string{syms[rng.IntN(lhs)]}})
			}
			continue
		}
		rhs := make([]string, k)
		for i := range rhs {
			rhs[i] = syms[rng.IntN(len(syms))]
		}
		rules = append(rules, cfg.Rule{LHS: syms[lhs], RHS: rhs})
	}
	lexicon := map[string][]string{}
	for _, w := range []string{"x", "y", "z"} {
		for range 1 + rng.IntN(3) {
			lexicon[w] = append(lexicon[w], syms[rng.IntN(len(syms))])
		}
	}
	return rules, lexicon
}

// The passes over a forest agree with measuring each of its trees.
func TestForestMeasures(t *testing.T) {
	rng := rand.New(rand.NewPCG(5, 6))
	checked := 0
	for range 400 {
		rules, lexicon := randomGrammar(rng)
		g, err := cfg.New(rules, lexicon, []string{"A"})
		if err != nil {
			t.Fatal(err)
		}
		for range 4 {
			words := make([]string, 1+rng.IntN(7))
			for i := range words {
				words[i] = []string{"x", "y", "z"}[rng.IntN(3)]
			}
			f := g.Parse(words)
			if n := f.Count(); n.Sign() == 0 || n.Int64() > 400 || !n.IsInt64() {
				continue
			}
			checked++
			var phrases []float64
			depths := make([]float64, 5)
			shortest, ways := -1, 0.0
			for tree := range f.Trees() {
				n := FromTree(tree)
				p := Phrases(n)
				for len(phrases) <= p {
					phrases = append(phrases, 0)
				}
				phrases[p]++
				for d := CentreDepth(n); d < len(depths); d++ {
					depths[d]++
				}
				switch l := DependencyLength(n); {
				case shortest < 0 || l < shortest:
					shortest, ways = l, 1
				case l == shortest:
					ways++
				}
			}
			if got := PhraseCounts(f); !slices.Equal(got, phrases) {
				t.Fatalf("%v on %v: phrase counts %v, want %v", rules, words, got, phrases)
			}
			if got := DepthCounts(f, 4); !slices.Equal(got, depths) {
				t.Fatalf("%v on %v: depth counts %v, want %v", rules, words, got, depths)
			}
			if l, w := ShortestDependencies(f); l != shortest || w != ways {
				t.Fatalf("%v on %v: shortest dependencies %d in %v trees, want %d in %v", rules, words, l, w, shortest, ways)
			}
		}
	}
	t.Logf("%d forests checked tree by tree", checked)
	if checked < 200 {
		t.Fatalf("only %d forests checked", checked)
	}
}
