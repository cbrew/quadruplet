package idlp

import (
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/cfg"
)

func word(label, w string) *cfg.Tree { return &cfg.Tree{Label: label, Words: []string{w}} }

// A projection seen in one order is licensed in every order, once per
// order, and the derivation of a tree is among the parses.
func TestAnyOrder(t *testing.T) {
	const (
		v   = "VBD_S1.I0.W0.N-2.P0.A0"
		np  = "NN_S0.I0.W0.N1.P0.A0"
		adv = "RB_S0.I0.W0.N0.P0.A0"
		vp  = "V:S1.I0.W0.N-1.P0.A0"
	)
	seen := &cfg.Tree{Label: "Top", Children: []*cfg.Tree{{Label: vp, Children: []*cfg.Tree{
		word(v, "saw"), word(np, "dogs"), word(adv, "today")}}}}
	for _, free := range []bool{false, true} {
		g := Compile([]*cfg.Tree{seen}, free)
		lex := map[string][]string{"saw": {v}, "dogs": {np}, "today": {adv}}
		parser, err := cfg.New(g.Rules, lex, []string{"Top"})
		if err != nil {
			t.Fatal(err)
		}
		for _, s := range []string{"saw dogs today", "today saw dogs", "dogs today saw", "saw today dogs"} {
			f := parser.Parse(strings.Fields(s))
			if c := f.Count().Int64(); c != 1 {
				t.Errorf("free %v: %q has %d trees, want 1", free, s, c)
			}
		}
		if f := parser.Parse(strings.Fields("saw dogs today")); !f.Contains(g.Derivation(seen)) {
			t.Errorf("free %v: the derivation of the tree seen is not among its parses", free)
		}
		// with free modifiers, more modifiers; attested, not
		f := parser.Parse(strings.Fields("today saw dogs today"))
		if want := map[bool]int64{false: 0, true: 1}[free]; f.Count().Int64() != want {
			t.Errorf("free %v: two modifiers give %d trees, want %d", free, f.Count().Int64(), want)
		}
	}
}
