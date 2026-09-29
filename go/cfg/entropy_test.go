package cfg

import (
	"fmt"
	"math"
	"math/rand/v2"
	"strings"
	"testing"
)

// ownerContext attributes each choice to the symbol whose rule makes it: a
// choice at an item to the item's symbol, split into the choice of rule
// (level 0) and of the daughters' spans given the rule (level 1); a choice
// at an auxiliary item of binarization, which is one of spans, to the symbol
// of the rule it belongs to, carried down as the state.
func ownerContext(g *Grammar) Context {
	return Context{
		Start: -1,
		Next: func(s int, item Item, _ Hyperedge, child Item) int {
			if !g.Aux[child.Sym] {
				return -1
			}
			if g.Aux[item.Sym] {
				return s
			}
			return int(item.Sym)
		},
		Groups: func(item Item, e Hyperedge) []string {
			if g.Aux[item.Sym] {
				return nil
			}
			if e.Step < 0 {
				return []string{"words"}
			}
			return []string{g.Rules[g.Steps[e.Step].Rule].String()}
		},
		Class: func(s int, item Item, level int) string {
			if g.Aux[item.Sym] {
				return g.Names[s] + "/1"
			}
			return fmt.Sprintf("%s/%d", g.Names[item.Sym], level)
		},
	}
}

// bruteEntropy is what Entropy with ownerContext should give, found by
// enumerating the trees: each node's choice of rule (or of being words) and
// of daughters' spans,
// with the probability that a subtree of the node's symbol and span, drawn
// uniformly, makes it; the surprisals summed by class and averaged over the
// trees.
func bruteEntropy(f *Forest) map[string]float64 {
	type node struct {
		all  map[string]bool            // distinct subtrees
		sigs map[string]map[string]bool // rule and spans -> subtrees
	}
	nodes := map[string]*node{}
	var walk func(t *Tree, l int) (int, string)
	type seen struct {
		key, rule, sig string
	}
	var visits [][]seen
	var cur []seen
	walk = func(t *Tree, l int) (int, string) {
		r := l
		var labels, spans []string
		if t.Words != nil {
			r += len(t.Words)
			labels = []string{"(words)"}
		}
		for _, c := range t.Children {
			start := r
			r, _ = walk(c, r)
			labels = append(labels, c.Label)
			spans = append(spans, fmt.Sprintf("%d-%d", start, r))
		}
		key := fmt.Sprintf("%s %d %d", t.Label, l, r)
		rule := t.Label + " -> " + strings.Join(labels, " ")
		sig := rule + " @ " + strings.Join(spans, " ")
		n := nodes[key]
		if n == nil {
			n = &node{map[string]bool{}, map[string]map[string]bool{}}
			nodes[key] = n
		}
		s := t.String()
		n.all[s] = true
		if n.sigs[sig] == nil {
			n.sigs[sig] = map[string]bool{}
		}
		n.sigs[sig][s] = true
		cur = append(cur, seen{key, rule, sig})
		return r, s
	}
	for tree := range f.Trees() {
		cur = nil
		walk(tree, 0)
		visits = append(visits, cur)
	}
	out := map[string]float64{}
	for _, v := range visits {
		for _, x := range v {
			n := nodes[x.key]
			label := strings.Fields(x.key)[0]
			pSig := float64(len(n.sigs[x.sig])) / float64(len(n.all))
			pRule := 0.0
			for sig, trees := range n.sigs {
				if strings.HasPrefix(sig, x.rule+" @ ") {
					pRule += float64(len(trees))
				}
			}
			pRule /= float64(len(n.all))
			out[label+"/0"] -= math.Log10(pRule) / float64(len(visits))
			out[label+"/1"] -= math.Log10(pSig/pRule) / float64(len(visits))
		}
	}
	return out
}

func TestEntropyAgainstEnumeration(t *testing.T) {
	rng := rand.New(rand.NewPCG(5, 6))
	words := []string{"x", "y", "z"}
	checked := 0
	for range 300 {
		rules, lexicon := randomGrammar(rng)
		g, err := FromGrammar(featureGrammar(rules, lexicon), "A")
		if err != nil {
			t.Fatal(err)
		}
		for range 4 {
			tokens := make([]string, 1+rng.IntN(6))
			for i := range tokens {
				tokens[i] = words[rng.IntN(len(words))]
			}
			f := g.Parse(tokens)
			count := f.Count()
			if count.Sign() == 0 || !count.IsInt64() || count.Int64() > 2000 {
				continue
			}
			got := f.Entropy(ownerContext(g))
			want := bruteEntropy(f)
			sum := 0.0
			for _, v := range got {
				sum += v
			}
			if d := sum - math.Log10(float64(count.Int64())); math.Abs(d) > 1e-9 {
				t.Fatalf("%v on %v: parts sum to %g, log10 of %s trees is %g", rules, tokens, sum, count, sum-d)
			}
			for k := range mergeKeys(got, want) {
				if math.Abs(got[k]-want[k]) > 1e-9 {
					t.Fatalf("%v on %v: %s is %g, by enumeration %g\n got  %v\n want %v",
						rules, tokens, k, got[k], want[k], got, want)
				}
			}
			if count.Int64() > 1 {
				checked++
			}
		}
	}
	t.Logf("%d ambiguous forests checked", checked)
	if checked < 50 {
		t.Fatalf("only %d ambiguous forests: the test is too weak", checked)
	}
}

func mergeKeys(a, b map[string]float64) map[string]bool {
	out := map[string]bool{}
	for k := range a {
		out[k] = true
	}
	for k := range b {
		out[k] = true
	}
	return out
}
