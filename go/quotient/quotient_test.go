package quotient

import (
	"strings"
	"testing"

	"github.com/cbrew/quadruplet/go/cfg"
)

// parse reads a tree in brackets, (S (NP she) (VP (VBD saw) ...)); a
// bracket with a word and no brackets inside is a word's.
func parse(s string) *cfg.Tree {
	toks := strings.Fields(strings.NewReplacer("(", " ( ", ")", " ) ").Replace(s))
	var at int
	var read func() *cfg.Tree
	read = func() *cfg.Tree {
		at++ // (
		t := &cfg.Tree{Label: toks[at]}
		at++
		for toks[at] != ")" {
			if toks[at] == "(" {
				t.Children = append(t.Children, read())
			} else {
				t.Words = append(t.Words, toks[at])
				at++
			}
		}
		at++
		return t
	}
	return read()
}

func TestKey(t *testing.T) {
	verbs := Options{Verbs: true}
	for _, c := range []struct {
		a, b string
		o    Options
		same bool
	}{
		// nested against flat adjunction
		{"(S (NP (PRP I)) (VP (VP (VBD saw) (NP (PRP her))) (PP (IN on) (NP (NNP Tuesday)))))",
			"(S (NP (PRP I)) (VP (VBD saw) (NP (PRP her)) (PP (IN on) (NP (NNP Tuesday)))))", verbs, true},
		// two orders of adjunction
		{"(VP (VP (VP (VBD left)) (ADVP (RB quickly))) (NP (NN today)))",
			"(VP (VP (VBD left)) (ADVP (RB quickly)) (NP (NN today)))", verbs, true},
		// but not with a scope-taking adverb
		{"(VP (VP (VBD knocked) (ADVP (RB twice))) (ADVP (RB intentionally)))",
			"(VP (VBD knocked) (ADVP (RB twice)) (ADVP (RB intentionally)))", verbs, false},
		// unless the exemption is off
		{"(VP (VP (VBD knocked) (ADVP (RB twice))) (ADVP (RB intentionally)))",
			"(VP (VBD knocked) (ADVP (RB twice)) (ADVP (RB intentionally)))", Options{Verbs: true, NoScope: true}, true},
		// a different span is a different class
		{"(VP (VP (VBD saw) (NP (DT the) (NN man))) (PP (IN with) (NP (DT a) (NN telescope))))",
			"(VP (VBD saw) (NP (NP (DT the) (NN man)) (PP (IN with) (NP (DT a) (NN telescope)))))", verbs, false},
		// the top category is kept
		{"(S (VP (VBD left)))", "(VP (VBD left))", verbs, false},
		// auxiliaries only with Aux
		{"(VP (MD will) (VP (VP (VB go)) (ADVP (RB home))))",
			"(VP (VP (MD will) (VP (VB go))) (ADVP (RB home)))", verbs, false},
		{"(VP (MD will) (VP (VP (VB go)) (ADVP (RB home))))",
			"(VP (VP (MD will) (VP (VB go))) (ADVP (RB home)))", Options{Verbs: true, Aux: true}, true},
		// coordination is not a layer
		{"(VP (VP (VBD ate)) (CC and) (VP (VBD drank)))",
			"(VP (VBD ate) (CC and) (VBD drank))", verbs, false},
		// nouns, only when asked
		{"(NP (NP (DT the) (NN man)) (PP (IN in) (NP (NN town))))",
			"(NP (DT the) (NN man) (PP (IN in) (NP (NN town))))", verbs, false},
		{"(NP (NP (DT the) (NN man)) (PP (IN in) (NP (NN town))))",
			"(NP (DT the) (NN man) (PP (IN in) (NP (NN town))))", Options{Nouns: true}, true},
	} {
		ka, kb := Key(parse(c.a), c.o), Key(parse(c.b), c.o)
		if (ka == kb) != c.same {
			t.Errorf("%s\n%s\nsame class: %v, want %v\n %s\n %s", c.a, c.b, ka == kb, c.same, ka, kb)
		}
	}
}
