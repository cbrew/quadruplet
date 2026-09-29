package counts

import (
	"encoding/json"
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/interp"
)

// tree reads a bracketed tree, (S (NP-SBJ (PRP I)) (VP ...)), with function
// tags after a hyphen, into interp's form, under Top.
func tree(t *testing.T, s string) *interp.Node {
	toks := strings.Fields(strings.NewReplacer("(", " ( ", ")", " ) ").Replace(s))
	at := 0
	var read func() map[string]any
	read = func() map[string]any {
		at++ // (
		label := toks[at]
		at++
		cat, fn, _ := strings.Cut(label, "-")
		node := map[string]any{"c": cat + "[]"}
		if fn != "" {
			node["f"] = strings.Split(fn, "-")
		}
		var kids []any
		for toks[at] != ")" {
			if toks[at] == "(" {
				kids = append(kids, read())
			} else {
				node["w"] = toks[at]
				at++
			}
		}
		at++
		if kids != nil {
			node["k"] = kids
		}
		return node
	}
	raw, _ := json.Marshal(map[string]any{"c": "Top[]", "k": []any{read()}})
	n, err := interp.FromJSON(raw)
	if err != nil {
		t.Fatal(err)
	}
	return n
}

func TestNormalVerbs(t *testing.T) {
	nf := Options{NormalVerbs: true}
	for _, c := range []struct {
		a, b string
		same bool
	}{
		// adjunction nested or flat
		{"(S (NP-SBJ (PRP I)) (VP (VP (VBD saw) (NP (PRP her))) (PP-TMP (IN on) (NP (NNP Tuesday)))))",
			"(S (NP-SBJ (PRP I)) (VP (VBD saw) (NP (PRP her)) (PP-TMP (IN on) (NP (NNP Tuesday)))))", true},
		// a modal is a modifier: an adverb before or after it, at either layer
		{"(S (NP-SBJ (PRP I)) (VP (MD will) (VP (ADVP (RB soon)) (VP (VB go)))))",
			"(S (NP-SBJ (PRP I)) (VP (MD will) (ADVP (RB soon)) (VP (VB go))))", true},
		// scope-bearing modifiers are blurred too
		{"(S (NP-SBJ (PRP I)) (VP (VP (VBD knocked) (ADVP (RB twice))) (ADVP (RB intentionally))))",
			"(S (NP-SBJ (PRP I)) (VP (VBD knocked) (ADVP (RB twice)) (ADVP (RB intentionally))))", true},
		// a complement is not a modifier
		{"(S (NP-SBJ (PRP I)) (VP (VBD talked) (PP-CLR (IN about) (NP (NN it)))))",
			"(S (NP-SBJ (PRP I)) (VP (VBD talked) (PP-TMP (IN about) (NP (NN it)))))", false},
	} {
		ta, err := ConvertWith(tree(t, c.a), nf)
		if err != nil {
			t.Fatal(err)
		}
		tb, err := ConvertWith(tree(t, c.b), nf)
		if err != nil {
			t.Fatal(err)
		}
		if (ta.String() == tb.String()) != c.same {
			t.Errorf("same: %v, want %v\n %s\n %s", ta.String() == tb.String(), c.same, ta, tb)
		}
		// without the normal form the first three differ
		pa, _ := Convert(tree(t, c.a))
		pb, _ := Convert(tree(t, c.b))
		if c.same && pa.String() == pb.String() {
			t.Errorf("the plain conversion already identifies\n %s\n %s", pa, pb)
		}
	}
}

func TestClauseTypes(t *testing.T) {
	for _, c := range []struct{ s, verb, want string }{
		// a subjectless infinitive is the collapsed chain SxVP, I - NP
		{"(S (NP-SBJ (PRP I)) (VP (VBP want) (SxVP (TO to) (VP (VB go)))))", "want", "VBP_S1.I-1.W0.N0.P0.A0"},
		{"(S (NP-SBJ (PRP I)) (VP (VBP want) (SxVP (TO to) (VP (VB go)))))", "go", "VB_S0.I1.W0.N-1.P0.A0"},
		{"(S (NP-SBJ (PRP I)) (VP (VBP wonder) (SBAR (IN if) (S (NP-SBJ (PRP he)) (VP (VBD left))))))", "wonder", "VBP_S1.I0.W-1.N-1.P0.A0"},
		{"(S (NP-SBJ (PRP I)) (VP (VBP know) (SBAR (IN that) (S (NP-SBJ (PRP he)) (VP (VBD left))))))", "know", "VBP_S0.I0.W0.N-1.P0.A0"},
	} {
		out, err := Convert(tree(t, c.s))
		if err != nil {
			t.Fatal(err)
		}
		var got string
		var walk func(x *Tree)
		walk = func(x *Tree) {
			if x.Words != nil && x.Words[0] == c.verb {
				got = x.Label
			}
			for _, k := range x.Children {
				walk(k)
			}
		}
		walk(out)
		if got != c.want {
			t.Errorf("%s: %s is %s, want %s", c.s, c.verb, got, c.want)
		}
	}
}
