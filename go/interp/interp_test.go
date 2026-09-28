package interp

import (
	"slices"
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/cfg"
)

// tree reads brackets with grammar labels, (Sph (NP (DT the) ...)), as package
// cfg gives trees; a node whose one child is not bracketed is a word.
func tree(t *testing.T, s string) *Node {
	toks := strings.Fields(strings.NewReplacer("(", " ( ", ")", " ) ").Replace(s))
	i := 0
	var node func() *cfg.Tree
	node = func() *cfg.Tree {
		i++ // (
		n := &cfg.Tree{Label: toks[i] + "[]"}
		i++
		for toks[i] != ")" {
			if toks[i] == "(" {
				n.Children = append(n.Children, node())
			} else {
				n.Words = append(n.Words, toks[i])
				i++
			}
		}
		i++
		return n
	}
	n := node()
	if i != len(toks) {
		t.Fatalf("trailing input in %s", s)
	}
	return FromTree(n)
}

func TestCategories(t *testing.T) {
	for label, want := range map[string][]string{
		"NPph[]": {"NP"}, "SxVP[]": {"S", "VP"}, "Comma[]": {","}, "PRPS[]": {"PRP$"},
		"SBARxSxVP[]": {"SBAR", "S", "VP"}, "Top[]": {"TOP"}, "NN[]": {"NN"},
	} {
		if got := categories(label); !slices.Equal(got, want) {
			t.Errorf("%s: %q, want %q", label, got, want)
		}
	}
}

// The heads Collins's rules choose, marked ^.
func TestHeads(t *testing.T) {
	for in, want := range map[string]string{
		"(S (NP (DT The) (NN dog)) (VP (VBD saw) (NP (DT a) (NN cat))) (Period .))": "(S (NP (DT The) (^NN dog)) (^VP (^VBD saw) (NP (DT a) (^NN cat))) (. .))",
		"(NP (NP (NNP John) (POS 's)) (NN dog))":                                    "(NP (NP (NNP John) (^POS 's)) (^NN dog))",
		"(PP (IN of) (NP (NNS dogs)))":                                              "(PP (^IN of) (NP (^NNS dogs)))",
		"(NP (NP (NNS cats)) (CC and) (NP (NNS dogs)))":                             "(NP (^NP (^NNS cats)) (CC and) (NP (^NNS dogs)))",
		"(SxVP (VB Go) (ADVP (RB away)))":                                           "(S/VP (^VB Go) (ADVP (^RB away)))",
		"(NP (DT the) (NN dog) (Comma ,))":                                          "(NP (DT the) (^NN dog) (, ,))",
		"(SBAR (IN that) (S (NP (PRP it)) (VP (VBZ rains))))":                       "(SBAR (^IN that) (S (NP (^PRP it)) (^VP (^VBZ rains))))",
	} {
		if got := tree(t, in).String(); got != want {
			t.Errorf("%s:\n got  %s\n want %s", in, got, want)
		}
	}
}

func TestDependencies(t *testing.T) {
	n := tree(t, "(Top (S (NP (DT The) (NN dog)) (VP (VBD saw) (NP (DT a) (NN cat))) (Period .)))")
	want := []Dep{{0, 1, "DT"}, {1, 2, "NP"}, {2, -1, "ROOT"}, {3, 4, "DT"}, {4, 2, "NP"}, {5, 2, "."}}
	if got := Dependencies(n); !slices.Equal(got, want) {
		t.Errorf("got %v\nwant %v", got, want)
	}
}

// The annotated trees of tools/masc/treebank.py carry function tags, which
// label the arcs.
func TestFromJSON(t *testing.T) {
	raw := `{"c": "Top[]", "k": [{"c": "Sph[]", "k": [
		{"c": "NPph[]", "f": ["SBJ"], "k": [{"c": "PRP[]", "w": "I"}]},
		{"c": "VPph[]", "k": [{"c": "VBD[]", "w": "left"}, {"c": "NPph[]", "f": ["TMP"], "k": [{"c": "NN[]", "w": "yesterday"}]}]}]}]}`
	n, err := FromJSON([]byte(raw))
	if err != nil {
		t.Fatal(err)
	}
	if got, want := n.String(), "(TOP (^S (NP-SBJ (^PRP I)) (^VP (^VBD left) (NP-TMP (^NN yesterday)))))"; got != want {
		t.Errorf("got  %s\nwant %s", got, want)
	}
	want := []Dep{{0, 1, "SBJ"}, {1, -1, "ROOT"}, {2, 1, "TMP"}}
	if got := Dependencies(n); !slices.Equal(got, want) {
		t.Errorf("got %v\nwant %v", got, want)
	}
}
