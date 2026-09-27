package chart

import (
	"testing"

	"github.com/cbrew/quadruplet/go/notation"
)

// An edge can come from the lexicon and be built by rules as well: "x" is an
// A and a B, and B -> A, so a B over "x" is a word or an A, two ways each.
func TestLexicalEdgesBuiltByRulesToo(t *testing.T) {
	g, err := notation.ParseIntegratedGrammar("S[] -> B[] B[]\nB[] -> A[]\n\"x\": A[] | B[]\n")
	if err != nil {
		t.Fatal(err)
	}
	for name, parse := range parsers {
		c := New([]string{"x", "x"})
		parse(c, NewFeatureGrammar(g))
		if got := c.CountTrees().Int64(); got != 4 {
			t.Errorf("%s: %d trees, want 4", name, got)
		}
		n := 0
		for _, e := range c.Solutions() {
			for range c.Trees(e) {
				n++
			}
		}
		if n != 4 {
			t.Errorf("%s: %d trees enumerated, want 4", name, n)
		}
	}
}
