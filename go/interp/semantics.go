package interp

import (
	"fmt"
	"slices"
	"strings"
	"unicode"

	"github.com/cbrew/quadruplet/go/term"
)

// Atom is a predication in a flat meaning: a predicate over referents,
// each named by the position of the word that introduces it.
type Atom struct {
	Pred string
	Args []int
}

func (a Atom) String() string {
	args := make([]string, len(a.Args))
	for i, x := range a.Args {
		args[i] = fmt.Sprintf("x%d", x+1)
	}
	return a.Pred + "(" + strings.Join(args, ", ") + ")"
}

// contentTags are the tags of words that introduce a referent of their own.
// Other words are determiners and particles, which say something of their
// head's referent; prepositions, complementizers and the possessive, which
// name a relation; or punctuation, which says nothing.
var contentTags = map[string]bool{
	"NN": true, "NNS": true, "NNP": true, "NNPS": true, "PRP": true, "PRP$": true, "WP": true, "WP$": true,
	"CD": true, "FW": true, "UH": true, "ADD": true, "GW": true, "AFX": true,
	"VB": true, "VBD": true, "VBG": true, "VBN": true, "VBP": true, "VBZ": true, "MD": true,
	"JJ": true, "JJR": true, "JJS": true, "RB": true, "RBR": true, "RBS": true, "WRB": true,
}

// auxiliaries are the verbs that, followed by a verb phrase, say something of
// its event rather than introduce one: tense, aspect, voice, modality.
var auxiliaries = map[string]bool{
	"be": true, "is": true, "are": true, "was": true, "were": true, "been": true, "being": true, "am": true,
	"'s": true, "'re": true, "'m": true, "have": true, "has": true, "had": true, "having": true, "'ve": true,
	"'d": true, "do": true, "does": true, "did": true,
}

// meaning is what a phrase contributes to its parent: the referent it is
// about, -1 if it has none, and the word, if any, that names its relation.
type meaning struct {
	ref    int
	marker string
}

// Flat is a tree's meaning as flat predications, neo-Davidsonian in the
// manner of Hobbs's ontological promiscuity: every content word introduces a
// referent, an event for a verb, and says of it the word, dog(x3); a
// determiner or particle says itself of its head's referent, the(x3); an
// auxiliary or modal of the event of the verb phrase it governs, will(x2);
// and every daughter of a phrase with a referent of its own stands in a
// relation to the phrase's, named by its preposition, complementizer or
// conjunction if it has one, by its function tags if it has those, and
// otherwise by its configuration: obj, nn, num, poss, comp, mod. Scope is not
// represented: a determiner is a condition like any other.
func Flat(n *Node) []Atom {
	var atoms []Atom
	folded := map[int]bool{} // content words that say something of another's referent
	word := func(w *Node) meaning {
		if contentTags[w.Cat] && wordlike(w.Word) {
			return meaning{w.Pos, ""}
		}
		return meaning{-1, ""}
	}
	phrase := func(n *Node, kids []meaning) meaning {
		h, sh := n.Head, n.Head
		marker := kids[h].marker
		hw := n.Kids[h]
		switch {
		case (n.Bottom() == "SBAR" || n.Bottom() == "SBARQ") && !strings.HasPrefix(n.Kids[h].Bottom(), "S"):
			// headed by a complementizer or a wh-phrase: the clause is about its
			// verb, and the head names the relation; without traces, the
			// wh-phrase's own role in the clause is not known
			for i := h + 1; i < len(kids); i++ {
				if strings.HasPrefix(n.Kids[i].Bottom(), "S") && kids[i].ref >= 0 {
					sh = i
					break
				}
			}
			if sh != h {
				marker = strings.ToLower(hw.HeadWord().Word)
			}
		case kids[h].ref < 0:
			// headed by a function word: the phrase is about what it governs
			if hw.IsWord() {
				marker = strings.ToLower(hw.Word)
				if hw.Cat == "POS" {
					marker = "poss"
				}
			}
			sh = -1
			for i := h + 1; i < len(kids) && sh < 0; i++ {
				if kids[i].ref >= 0 {
					sh = i
				}
			}
			for i := h - 1; i >= 0 && sh < 0; i-- {
				if kids[i].ref >= 0 {
					sh = i
				}
			}
			if sh < 0 && hw.IsWord() && hw.Cat == "DT" {
				// a determiner standing alone, this or all, is about something itself
				atoms = append(atoms, Atom{strings.ToLower(hw.Word), []int{hw.Pos}})
				return meaning{hw.Pos, ""}
			}
			if sh < 0 {
				return meaning{-1, marker}
			}
		case hw.IsWord() && (hw.Cat == "MD" || auxiliaries[strings.ToLower(hw.Word)]):
			// an auxiliary followed by a verb phrase: the phrase is about its event
			for i := h + 1; i < len(kids); i++ {
				if n.Kids[i].Bottom() == "VP" && kids[i].ref >= 0 {
					sh = i
					break
				}
			}
			if sh != h {
				folded[hw.Pos] = true
				atoms = append(atoms, Atom{strings.ToLower(hw.Word), []int{kids[sh].ref}})
				marker = kids[sh].marker
			}
		}
		ref := kids[sh].ref
		conj := ""
		for i, k := range n.Kids {
			if i == sh || i == h && sh != h {
				continue
			}
			kv := kids[i]
			switch {
			case k.IsWord() && kv.ref < 0:
				// a function word beside the head: a determiner, a particle, a conjunction
				if IsPunctuation(k) || !wordlike(k.Word) {
					continue
				}
				if k.Cat == "CC" {
					conj = strings.ToLower(k.Word)
					continue
				}
				atoms = append(atoms, Atom{strings.ToLower(k.Word), []int{ref}})
			case kv.ref >= 0:
				atoms = append(atoms, Atom{relation(n, i, kv, conj), []int{ref, kv.ref}})
			}
		}
		return meaning{ref, marker}
	}
	Fold(n, word, phrase)
	// each content word not folded into another's referent says itself of its own
	Fold(n, func(w *Node) struct{} {
		if contentTags[w.Cat] && !folded[w.Pos] && wordlike(w.Word) {
			atoms = append(atoms, Atom{strings.ToLower(w.Word), []int{w.Pos}})
		}
		return struct{}{}
	}, func(*Node, []struct{}) struct{} { return struct{}{} })
	slices.SortFunc(atoms, func(a, b Atom) int {
		return strings.Compare(a.String(), b.String())
	})
	return atoms
}

// wordlike says whether a token has a letter or a digit in it: /, & and $$
// are not predicates.
func wordlike(w string) bool {
	return strings.IndexFunc(w, func(r rune) bool { return unicode.IsLetter(r) || unicode.IsDigit(r) }) >= 0
}

// relation names the relation of daughter i of n, whose meaning is kv, to n's
// referent; conj is the conjunction, if any, seen among n's daughters before it.
func relation(n *Node, i int, kv meaning, conj string) string {
	k := n.Kids[i]
	fn := strings.ToLower(strings.Join(k.Fn, "-"))
	switch {
	case kv.marker != "" && fn != "":
		return fn + "_" + kv.marker
	case kv.marker != "":
		return kv.marker
	case fn != "":
		return fn
	case conj != "":
		return conj
	}
	switch parent, cat := n.Bottom(), k.Cat; {
	case cat == "PRP$" || cat == "WP$":
		return "poss"
	case cat == "CD" || cat == "QP":
		return "num"
	case parent == "VP" && cat == "NP":
		return "obj"
	case parent == "NP" || parent == "NML" || parent == "NX":
		if strings.HasPrefix(cat, "NN") || cat == "NML" || cat == "NX" {
			return "nn"
		}
	case cat == "S" || cat == "SBAR" || cat == "VP" || cat == "SQ" || cat == "SINV" || cat == "SBARQ":
		return "comp"
	}
	return "mod"
}

// Formula is a flat meaning as a closed formula: each referent existentially
// quantified, in the order of the words that introduce them, over the
// conjunction of the atoms.
func Formula(atoms []Atom) term.Lambda {
	var refs []int
	for _, a := range atoms {
		for _, x := range a.Args {
			if !slices.Contains(refs, x) {
				refs = append(refs, x)
			}
		}
	}
	slices.Sort(refs)
	if len(atoms) == 0 {
		return term.True
	}
	rank := map[int]int{}
	for r, x := range refs {
		rank[x] = r + 1
	}
	k := len(refs)
	conj := make([]term.Lambda, len(atoms))
	for i, a := range atoms {
		var t term.Lambda = term.NewConst(a.Pred)
		for _, x := range a.Args {
			t = term.NewApp(t, term.NewQVar(k-rank[x]+1))
		}
		conj[i] = t
	}
	f := term.CreateAnd(conj...)
	for range refs {
		f = term.NewExists(f)
	}
	return f
}
