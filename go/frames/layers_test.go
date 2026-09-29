package frames

import (
	"fmt"
	"math"
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/cfg"
)

// layerGrammar is a small Penn-style grammar whose forests have several
// arrangements of verb nodes: clauses coordinated at the top or inside a
// complement, verbs one above another or side by side, and prepositional
// phrases attached anywhere.
func layerGrammar(t *testing.T) *cfg.Grammar {
	var rules []cfg.Rule
	for _, s := range []string{
		"S -> NP VP", "S -> S CC S", "S -> NP", "SBAR -> IN S",
		"VP -> VBD SBAR", "VP -> VBD", "VP -> VBD NP", "VP -> VBD NP PP", "VP -> VBD PP",
		"VP -> VP PP", "VP -> VP CC VP", "VP -> VBD S", "VP -> MD VP",
		"NP -> DT NN", "NP -> NP PP", "NP -> NP CC NP", "NP -> NN", "PP -> IN NP",
	} {
		parts := strings.Fields(s)
		rules = append(rules, cfg.Rule{LHS: parts[0], RHS: parts[2:]})
	}
	lexicon := map[string][]string{
		"the": {"DT"}, "man": {"NN"}, "said": {"VBD"}, "that": {"IN"}, "cat": {"NN"},
		"slept": {"VBD"}, "and": {"CC"}, "dog": {"NN"}, "saw": {"VBD", "NN"}, "bird": {"NN"},
		"with": {"IN"}, "telescope": {"NN"}, "in": {"IN"}, "park": {"NN"}, "would": {"MD"},
	}
	g, err := cfg.New(rules, lexicon, []string{"S"})
	if err != nil {
		t.Fatal(err)
	}
	return g
}

// spanned is a tree with the span of each node.
type spanned struct {
	label string
	l, r  int
	kids  []*spanned
	verb  bool
}

func span(t *cfg.Tree, start int) *spanned {
	n := &spanned{label: t.Label, l: start}
	if t.Children == nil {
		n.r = start + len(t.Words)
		return n
	}
	end := start
	rhs := make([]string, len(t.Children))
	for i, k := range t.Children {
		s := span(k, end)
		n.kids = append(n.kids, s)
		end = s.r
		rhs[i] = k.Label
	}
	n.r = end
	n.verb = LexicalVP(t.Label, rhs)
	return n
}

func (n *spanned) item() string { return fmt.Sprintf("%s:%d-%d", n.label, n.l, n.r) }

func (n *spanned) hasVerbBelow() bool {
	for _, k := range n.kids {
		if k.verb || k.hasVerbBelow() {
			return true
		}
	}
	return false
}

func (n *spanned) expansion() string {
	var b strings.Builder
	b.WriteString(n.item() + "->")
	for _, k := range n.kids {
		b.WriteString(" " + k.item())
	}
	return b.String()
}

// stages are the five things settled in turn about a tree, cumulatively:
// its skeleton outside the top verb nodes; their expansions; everything
// down to the bottom verb nodes; their expansions; the whole tree.
func stages(n *spanned) [5]string {
	var skeleton, top, mid, bottom, whole strings.Builder
	// hidden says a builder is under a node it does not look into
	var walk func(n *spanned, above, hideSkeleton, hideMid bool)
	walk = func(n *spanned, above, hideSkeleton, hideMid bool) {
		isTop := n.verb && !above
		isBottom := n.verb && !n.hasVerbBelow()
		whole.WriteString("(" + n.item())
		if !hideSkeleton {
			if isTop {
				skeleton.WriteString("[" + n.item() + "]")
				top.WriteString(n.expansion() + ";")
			} else {
				skeleton.WriteString("(" + n.item())
			}
		}
		if !hideMid {
			if isBottom {
				mid.WriteString("[" + n.item() + "]")
				bottom.WriteString(n.expansion() + ";")
			} else {
				mid.WriteString("(" + n.item())
			}
		}
		for _, k := range n.kids {
			walk(k, above || n.verb, hideSkeleton || isTop, hideMid || isBottom)
		}
		whole.WriteString(")")
		if !hideSkeleton && !isTop {
			skeleton.WriteString(")")
		}
		if !hideMid && !isBottom {
			mid.WriteString(")")
		}
	}
	walk(n, false, false, false)
	s1 := skeleton.String()
	s2 := s1 + "|" + top.String()
	s3 := s2 + "|" + mid.String()
	s4 := s3 + "|" + bottom.String()
	return [5]string{s1, s2, s3, s4, whole.String()}
}

func entropy(counts map[string]float64, total float64) float64 {
	h := 0.0
	for _, c := range counts {
		p := c / total
		h -= p * math.Log10(p)
	}
	return h
}

// bruteForce computes Layers by enumerating the forest's trees and taking
// the entropy of each stage's distribution.
func bruteForce(f *cfg.Forest, weight func(r cfg.Rule) float64) (Layers, int) {
	counts := [5]map[string]float64{}
	for i := range counts {
		counts[i] = map[string]float64{}
	}
	var trees int
	var k, klogk, verbs, tops, bottoms, verbFree float64
	for t := range f.Trees() {
		trees++
		w := treeWeight(t, weight)
		k += w
		klogk += w * math.Log10(w)
		for i, s := range stages(span(t, 0)) {
			counts[i][s] += w
		}
		v, top, bottom := VerbNodes(t)
		verbs += w * float64(v)
		tops += w * float64(top)
		bottoms += w * float64(bottom)
		if v == 0 {
			verbFree += w
		}
	}
	var h [5]float64
	for i := range h {
		h[i] = entropy(counts[i], k)
	}
	return Layers{
		Count: math.Log10(float64(trees)), Total: math.Log10(k) - klogk/k,
		Outside: h[0], Top: h[1] - h[0], Between: h[2] - h[1], Bottom: h[3] - h[2], Inside: h[4] - h[3],
		Verbs: verbs / k, TopVerbs: tops / k, BottomVerbs: bottoms / k, VerbFree: verbFree / k,
	}, trees
}

func treeWeight(t *cfg.Tree, weight func(r cfg.Rule) float64) float64 {
	if t.Children == nil {
		return 1
	}
	r := cfg.Rule{LHS: t.Label}
	w := 1.0
	for _, k := range t.Children {
		r.RHS = append(r.RHS, k.Label)
		w *= treeWeight(k, weight)
	}
	return w * weight(r)
}

func TestVerbLayers(t *testing.T) {
	g := layerGrammar(t)
	verbRule := LexicalRules(g)
	lexical := map[string]bool{}
	for i, r := range g.Rules {
		lexical[r.String()] = verbRule[i]
	}
	for _, r := range []string{"VP -> VBD NP PP", "VP -> VBD", "VP -> VBD SBAR"} {
		if !lexical[r] {
			t.Errorf("%s should be a lexical verb phrase rule", r)
		}
	}
	for _, r := range []string{"S -> NP VP", "VP -> VP PP", "VP -> VP CC VP", "VP -> MD VP", "NP -> DT NN"} {
		if lexical[r] {
			t.Errorf("%s should not be a lexical verb phrase rule", r)
		}
	}
	// arbitrary rule weights, and none
	weights := make([]float64, len(g.Rules))
	byRule := map[string]float64{}
	for i, r := range g.Rules {
		weights[i] = 1 + float64(i*7%5)/3
		byRule[r.String()] = weights[i]
	}
	uniform := func(cfg.Rule) float64 { return 1 }
	weighted := func(r cfg.Rule) float64 { return byRule[r.String()] }
	for _, s := range []string{
		"the man said that the cat slept and the dog saw the bird with the telescope in the park",
		"the man saw the dog with the telescope",
		"the man would saw the dog with the telescope in the park",
		"the man with the telescope",
		"the cat slept and the dog slept",
		"the man said that the cat saw the dog and slept in the park",
		"the man said that the cat would saw the dog and slept in the park",
		"the saw slept",
	} {
		f := g.Parse(strings.Fields(s))
		if len(f.Goals) == 0 {
			t.Fatalf("%q: no parse", s)
		}
		for _, w := range []struct {
			name   string
			weight []float64
			of     func(cfg.Rule) float64
		}{{"uniform", nil, uniform}, {"weighted", weights, weighted}} {
			got := VerbLayers(f, verbRule, w.weight)
			want, trees := bruteForce(f, w.of)
			check(t, s+", "+w.name, trees, got, want)
		}
	}
}

func check(t *testing.T, s string, trees int, got, want Layers) {
	{
		for _, x := range []struct {
			name      string
			got, want float64
		}{
			{"count", got.Count, want.Count}, {"total", got.Total, want.Total}, {"outside", got.Outside, want.Outside},
			{"top", got.Top, want.Top}, {"between", got.Between, want.Between},
			{"bottom", got.Bottom, want.Bottom}, {"inside", got.Inside, want.Inside},
			{"verbs", got.Verbs, want.Verbs}, {"top verbs", got.TopVerbs, want.TopVerbs},
			{"bottom verbs", got.BottomVerbs, want.BottomVerbs}, {"verb-free", got.VerbFree, want.VerbFree},
		} {
			if math.Abs(x.got-x.want) > 1e-9 {
				t.Errorf("%q: %s = %.6f, brute force %.6f", s, x.name, x.got, x.want)
			}
		}
		sum := got.Outside + got.Top + got.Between + got.Bottom + got.Inside
		if math.Abs(sum-got.Total) > 1e-9 {
			t.Errorf("%q: terms sum to %g, total %g", s, sum, got.Total)
		}
		t.Logf("%q: %d trees, %+v", s, trees, got)
	}
}
