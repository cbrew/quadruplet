package interp

import (
	"slices"
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/term"
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

// Flat meanings of small trees, with and without function tags.
func TestFlat(t *testing.T) {
	for in, want := range map[string]string{
		"(Top (S (NP (DT The) (NN dog)) (VP (VBD saw) (NP (DT a) (NN cat))) (Period .)))":   "a(x5) cat(x5) dog(x2) mod(x3, x2) obj(x3, x5) saw(x3) the(x2)",
		"(Top (S (NP (PRP He)) (VP (MD will) (VP (VB go) (PP (TO to) (NP (NNP Paris)))))))": "go(x3) he(x1) mod(x3, x1) paris(x5) to(x3, x5) will(x3)",
		"(Top (NP (NP (NNP John) (POS 's)) (NN dog)))":                                      "dog(x3) john(x1) poss(x3, x1)",
		"(Top (NP (NP (NNS cats)) (CC and) (NP (NNS dogs))))":                               "and(x1, x3) cats(x1) dogs(x3)",
		"(Top (NP (NP (DT the) (NN dog)) (SBAR (WHNP (WDT which)) (S (VP (VBD barked))))))": "barked(x4) dog(x2) the(x2) which(x2, x4)",
		"(Top (S (NP (PRP I)) (VP (VBP like) (NP (DT this)))))":                             "i(x1) like(x2) mod(x2, x1) obj(x2, x3) this(x3)",
		"(Top (S (NP (PRP It)) (VP (VBZ is) (VP (VBN done) (ADVP (RB well))))))":            "done(x3) is(x3) it(x1) mod(x3, x1) mod(x3, x4) well(x4)",
	} {
		atoms := Flat(tree(t, in))
		got := make([]string, len(atoms))
		for i, a := range atoms {
			got[i] = a.String()
		}
		if strings.Join(got, " ") != want {
			t.Errorf("%s:\n got  %s\n want %s", in, strings.Join(got, " "), want)
		}
	}
	// with function tags, the subject is named for them
	raw := `{"c": "Top[]", "k": [{"c": "Sph[]", "k": [
		{"c": "NPph[]", "f": ["SBJ"], "k": [{"c": "PRP[]", "w": "I"}]},
		{"c": "VPph[]", "k": [{"c": "VBD[]", "w": "left"}, {"c": "PPph[]", "f": ["TMP"], "k": [{"c": "IN[]", "w": "after"}, {"c": "NPph[]", "k": [{"c": "NN[]", "w": "lunch"}]}]}]}]}]}`
	n, err := FromJSON([]byte(raw))
	if err != nil {
		t.Fatal(err)
	}
	f := Formula(Flat(n))
	if got, want := term.Pretty(f), "∃x1.∃x2.∃x3.(i(x1) ∧ left(x2) ∧ lunch(x3) ∧ sbj(x2, x1) ∧ tmp_after(x2, x3))"; got != want {
		t.Errorf("got  %s\nwant %s", got, want)
	}
	if !term.Closed(f) {
		t.Errorf("%s is not closed", term.Pretty(f))
	}
}
