package main

import (
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/interp"
)

func TestMeaningKey(t *testing.T) {
	phrases := map[string]bool{"S": true, "NP": true, "VP": true, "PP": true, "SBAR": true, "SBARxS": true, "ADJP": true, "SxVP": true}
	isWord := func(s string) bool { return !phrases[s] }
	key := func(rule string) string {
		parts := strings.Fields(rule)
		r := cfg.Rule{LHS: parts[0], RHS: parts[2:]}
		return meaningKey(r, interp.RuleHead(r, isWord), isWord)
	}
	for _, c := range []struct{ rule, want string }{
		{"VP -> VBD NP", "H:VBD obj"},
		{"VP -> VBD S", "H:VBD comp"},
		{"VP -> VBD PP", "H:VBD M"},
		{"VP -> VBD SBAR", "H:VBD M"},
		{"VP -> VBD SBARxS", "H:VBD comp"},
		{"VP -> MD VP", "aux:MD comp"},
		{"VP -> VBD VP", "aux:VBD comp"},
		{"VP -> VP PP", "H:VP M"},
		{"NP -> DT JJ NN", "w:DT mod H:NN"},
		{"NP -> DT NN NN", "w:DT nn H:NN"},
		{"NP -> NP CC NP", "H:NP cc conj"},
		{"NP -> PRPS NN", "poss H:NN"},
		{"NP -> CD NNS", "num H:NNS"},
		{"S -> NP VP", "mod H:VP"},
		{"SxVP -> VBD NP", "H:VBD obj"},
		{"PP -> IN NP", "H:IN mod"},
		{"NP -> NP , PP ,", "H:NP . M ."},
	} {
		if got := key(c.rule); got != c.want {
			t.Errorf("%s: key %q, want %q", c.rule, got, c.want)
		}
	}
	// what the meaning cannot see: the same relations by different rules
	if key("VP -> VBD PP") != key("VP -> VBD SBAR") {
		t.Error("a PP and an SBAR complement over the same words are both named by their marker")
	}
	if key("VP -> VBD NP") == key("VP -> VBD S") {
		t.Error("an object and a clausal complement differ")
	}
}
