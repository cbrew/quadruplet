package cfg

import (
	"bufio"
	"fmt"
	"math/big"
	"math/rand/v2"
	"os"
	"path/filepath"
	"slices"
	"strconv"
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/chart"
	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/term"
)

// TreeGrammar's rules, as chart.TreeGrammar has them, give the tree counts
// the Kotlin implementation recorded in treeas.golden.
func TestTreeGrammarCounts(t *testing.T) {
	rules := []Rule{
		{"odd", []string{"odd", "odd", "odd"}}, {"odd", []string{"odd", "even", "even"}},
		{"even", []string{"odd", "even", "odd"}}, {"even", []string{"odd", "odd", "even"}},
		{"odd", []string{"odd", "even"}}, {"even", []string{"odd", "odd"}},
		{"even", []string{"even", "even"}}, {"odd", []string{"even", "odd"}},
		{"odd", []string{"even", "odd", "even"}}, {"odd", []string{"even", "even", "odd"}},
		{"even", []string{"even", "even", "even"}}, {"even", []string{"even", "odd", "odd"}},
	}
	g, err := New(rules, map[string][]string{"a": {"odd"}}, []string{"odd", "even"})
	if err != nil {
		t.Fatal(err)
	}
	f, err := os.Open(filepath.Join("..", "testdata", "golden", "treeas.golden"))
	if err != nil {
		t.Fatal(err)
	}
	defer f.Close()
	sc := bufio.NewScanner(f)
	for sc.Scan() {
		rec := strings.Split(sc.Text(), "\t")
		n, _ := strconv.Atoi(rec[0])
		got := g.Parse(slices.Repeat([]string{"a"}, n)).Count()
		if got.String() != rec[3] {
			t.Errorf("%d words: %s trees, want %s", n, got, rec[3])
		}
	}
}

func cat(name string) *term.Map {
	return term.NewMap([]string{"cat"}, []term.Term{term.NewAtom(name)})
}

// randomGrammar is a small, ambiguous grammar: rules of one to four
// daughters, unary rules only from a symbol to a lower one (so there are no
// cycles), and words that are several symbols, some of them phrases.
func randomGrammar(rng *rand.Rand) ([]Rule, map[string][]string) {
	syms := []string{"A", "B", "C", "D", "E"}
	var rules []Rule
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
			rules = append(rules, Rule{syms[lhs], []string{syms[rng.IntN(lhs)]}})
			continue
		}
		rhs := make([]string, k)
		for i := range rhs {
			rhs[i] = syms[rng.IntN(len(syms))]
		}
		rules = append(rules, Rule{syms[lhs], rhs})
	}
	lexicon := map[string][]string{}
	for _, w := range []string{"x", "y", "z", "x y"} {
		for range 1 + rng.IntN(3) {
			lexicon[w] = append(lexicon[w], syms[rng.IntN(len(syms))])
		}
	}
	return rules, lexicon
}

// featureGrammar is the same grammar for package chart.
func featureGrammar(rules []Rule, lexicon map[string][]string) *grammar.Grammar {
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

// live is package chart's complete edges on a parse of the whole input from
// a start category, as "Cat[] l r", and the number of trees.
func live(c *chart.Chart, start string) (map[string]bool, *big.Int) {
	out := map[string]bool{}
	trees := new(big.Int)
	seen := map[*chart.Edge]bool{}
	var walk func(e *chart.Edge)
	walk = func(e *chart.Edge) {
		if seen[e] {
			return
		}
		seen[e] = true
		if e.Complete() {
			out[fmt.Sprintf("%s %d %d", e.Cat, e.Start, e.End)] = true
		}
		for _, p := range c.Predecessors(e) {
			walk(p.Partial)
			walk(p.Complete)
		}
	}
	for _, e := range c.Solutions() {
		if term.Key(e.Cat) == start {
			walk(e)
			trees.Add(trees, c.CountTreesUnder(e))
		}
	}
	return out, trees
}

// On random grammars and inputs the forest's own items are exactly package
// chart's live complete edges, the tree counts agree, and every tree
// enumerated is in the forest, and distinct.
func TestAgreesWithChart(t *testing.T) {
	rng := rand.New(rand.NewPCG(1, 2))
	words := []string{"x", "y", "z"}
	sentences, parsed := 0, 0
	for range 300 {
		rules, lexicon := randomGrammar(rng)
		fg := featureGrammar(rules, lexicon)
		g, err := FromGrammar(fg, "A")
		if err != nil {
			t.Fatal(err)
		}
		cfgFeature := chart.NewFeatureGrammar(fg)
		for range 4 {
			tokens := make([]string, 1+rng.IntN(6))
			for i := range tokens {
				tokens[i] = words[rng.IntN(len(words))]
			}
			sentences++
			c := chart.New(tokens)
			c.Parse(cfgFeature)
			want, wantTrees := live(c, "A")
			f := g.Parse(tokens)
			got := map[string]bool{}
			for _, it := range f.Items {
				if !g.Prefix[it.Sym] {
					got[fmt.Sprintf("%s %d %d", g.Names[it.Sym], it.L, it.R)] = true
				}
			}
			if len(want) > 0 {
				parsed++
			}
			if !mapsEqual(got, want) {
				t.Fatalf("%v on %v:\n got  %v\n want %v", rules, tokens, got, want)
			}
			count := f.Count()
			if count.Cmp(wantTrees) != 0 {
				var trees []string
				for tree := range f.Trees() {
					trees = append(trees, tree.String())
				}
				t.Fatalf("%q, lexicon %v, on %v: %s trees, want %s\n%s", rules, lexicon, tokens, count, wantTrees,
					strings.Join(trees, "\n"))
			}
			if count.Cmp(big.NewInt(200)) > 0 {
				continue
			}
			distinct := map[string]bool{}
			for tree := range f.Trees() {
				distinct[tree.String()] = true
				if !f.Contains(tree) {
					t.Fatalf("%v on %v: %s not found in its own forest", rules, tokens, tree)
				}
			}
			if int64(len(distinct)) != count.Int64() {
				t.Fatalf("%v on %v: %d distinct trees, count %s", rules, tokens, len(distinct), count)
			}
		}
	}
	t.Logf("%d of %d random sentences parsed", parsed, sentences)
	if parsed < sentences/4 {
		t.Fatalf("only %d of %d random sentences parsed: the test is too weak", parsed, sentences)
	}
}

func mapsEqual(a, b map[string]bool) bool {
	if len(a) != len(b) {
		return false
	}
	for k := range a {
		if !b[k] {
			return false
		}
	}
	return true
}

func TestContainsRejects(t *testing.T) {
	g, err := New([]Rule{{"S", []string{"A", "B", "C"}}, {"A", []string{"X"}}},
		map[string][]string{"a": {"X"}, "b": {"B"}, "c": {"C"}}, []string{"S"})
	if err != nil {
		t.Fatal(err)
	}
	f := g.Parse([]string{"a", "b", "c"})
	var trees []*Tree
	for tree := range f.Trees() {
		trees = append(trees, tree)
	}
	if len(trees) != 1 || trees[0].String() != "(S (A (X a)) (B b) (C c))" {
		t.Fatalf("trees %v", trees)
	}
	wrong := []string{"(S (A (X a)) (C b) (C c))", "(S (X a) (B b) (C c))", "(S (A (X a)) (B b c))"}
	for _, w := range wrong {
		if f.Contains(parseTree(t, w)) {
			t.Errorf("%s: found, but is not in the forest", w)
		}
	}
	if !f.Contains(parseTree(t, trees[0].String())) {
		t.Error("the forest's own tree, reparsed, is not found")
	}
}

func TestRefuses(t *testing.T) {
	if _, err := New([]Rule{{"A", []string{"B"}}, {"B", []string{"C"}}, {"C", []string{"A"}}},
		map[string][]string{"x": {"A"}}, []string{"A"}); err == nil || !strings.Contains(err.Error(), "cycle") {
		t.Errorf("unary cycle: %v", err)
	}
	if _, err := New([]Rule{{"A", nil}}, nil, nil); err == nil {
		t.Error("a rule with no daughters was accepted")
	}
	fg := grammar.New()
	fg.AddRule(&grammar.Rule{LHS: cat("S"), RHS: []*term.Map{
		term.NewMap([]string{"cat", "n"}, []term.Term{term.NewAtom("NP"), term.NewSynVar("?n")})}})
	if _, err := FromGrammar(fg, "S"); err == nil {
		t.Error("a category with a variable was accepted")
	}
}

// parseTree reads a tree in brackets; a node whose one child is not
// bracketed is a lexical entry.
func parseTree(t *testing.T, s string) *Tree {
	toks := strings.Fields(strings.NewReplacer("(", " ( ", ")", " ) ").Replace(s))
	i := 0
	var node func() *Tree
	node = func() *Tree {
		i++ // (
		tr := &Tree{Label: toks[i]}
		i++
		for toks[i] != ")" {
			if toks[i] == "(" {
				tr.Children = append(tr.Children, node())
			} else {
				tr.Words = append(tr.Words, toks[i])
				i++
			}
		}
		i++
		return tr
	}
	tr := node()
	if i != len(toks) {
		t.Fatalf("trailing input in %s", s)
	}
	return tr
}
