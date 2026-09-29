// Package counts relabels a treebank tree categorially, without slashes:
// each phrase by the counts of the basic types S, NP, PP and AP in its
// category, and each word by its tag and the counts of its lexical type.
// This is the count invariant of categorial grammar (van Benthem): function
// application adds counts, (X - Y) + Y = X, so a phrase's counts are the
// sum of its daughters', and a modifier X/X counts nothing.
//
// The counts are assigned top down. A complement gets its category's
// counts (a clause S, a verb phrase S - NP, a noun phrase NP, a
// prepositional phrase PP, a predicative adjective phrase AP); a modifier
// gets none; the head gets its mother's counts less its sisters'. So the
// sum holds at every node by construction, and a word's type is what is
// left for it: a transitive verb S - 2NP, a preposition heading a modifier
// -NP, a determiner nothing. Which daughter is the head and which are
// complements comes from the treebank's function tags and head rules
// (package interp), with these choices:
//
//   - Auxiliaries and modals modify the event: in a verb phrase with a
//     verb phrase daughter (VP -> MD VP, VP -> VBZ VP), the verb phrase
//     daughter is the head and the auxiliary counts nothing. A copula with
//     no verb phrase daughter is a main verb, and its predicate a
//     complement: is happy is S - NP - AP.
//   - In a subordinate clause (SBAR) the clause is the head, and a
//     complementizer or relative pronoun counts nothing beside it.
//   - Coordination: the conjuncts (two or more daughters of one category)
//     each get what the head would, and the first conjunction the balance,
//     -(k-1) times it for k conjuncts.
//
// A phrase with one daughter has its daughter's counts, so chains of
// phrases over one phrase are one node.
package counts

import (
	"fmt"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/interp"
)

// Vec is counts of S, NP, PP and AP.
type Vec [4]int

func (v Vec) add(w Vec) Vec { return Vec{v[0] + w[0], v[1] + w[1], v[2] + w[2], v[3] + w[3]} }
func (v Vec) sub(w Vec) Vec { return Vec{v[0] - w[0], v[1] - w[1], v[2] - w[2], v[3] - w[3]} }
func (v Vec) scale(k int) Vec {
	return Vec{k * v[0], k * v[1], k * v[2], k * v[3]}
}

// String is the counts as a grammar symbol: S1.N-1.P0.A0.
func (v Vec) String() string { return fmt.Sprintf("S%d.N%d.P%d.A%d", v[0], v[1], v[2], v[3]) }

var (
	vS  = Vec{1, 0, 0, 0}
	vNP = Vec{0, 1, 0, 0}
	vPP = Vec{0, 0, 1, 0}
	vAP = Vec{0, 0, 0, 1}
)

var clauses = map[string]bool{"S": true, "SQ": true, "SINV": true, "SBARQ": true, "SBAR": true, "FRAG": true, "RRC": true}
var nouns = map[string]bool{"NP": true, "NML": true, "NX": true, "WHNP": true, "NAC": true}

var complementTags = map[string]bool{"SBJ": true, "PRD": true, "CLR": true, "DTV": true, "PUT": true}
var modifierTags = map[string]bool{"ADV": true, "TMP": true, "LOC": true, "MNR": true, "PRP": true,
	"DIR": true, "EXT": true, "BNF": true, "VOC": true}

// category is a complement's counts, by its category.
func category(n *interp.Node) Vec {
	if n.IsWord() {
		switch t := n.Cat; {
		case strings.HasPrefix(t, "NN") || t == "PRP" || t == "CD":
			return vNP
		case strings.HasPrefix(t, "JJ"):
			return vAP
		}
		return Vec{}
	}
	top, bottom := n.Chain[0], n.Bottom()
	switch {
	case bottom == "VP":
		return vS.sub(vNP)
	case clauses[top]:
		return vS
	case nouns[top]:
		return vNP
	case top == "PP" || top == "WHPP":
		return vPP
	case top == "ADJP" || top == "WHADJP":
		return vAP
	}
	return Vec{}
}

func has(fn []string, set map[string]bool) bool {
	for _, f := range fn {
		if set[f] {
			return true
		}
	}
	return false
}

func verbish(n *interp.Node) bool {
	return n.IsWord() && (strings.HasPrefix(n.Cat, "VB") || n.Cat == "MD" || n.Cat == "TO")
}

// complement says whether daughter d of phrase n is a complement.
func complement(n, d *interp.Node) bool {
	if interp.IsPunctuation(d) {
		return false
	}
	if has(d.Fn, complementTags) {
		return true
	}
	if has(d.Fn, modifierTags) {
		return false
	}
	switch parent := n.Bottom(); {
	case parent == "VP":
		if d.IsWord() {
			return d.Cat == "RP"
		}
		switch d.Chain[0] {
		case "NP", "S", "SBAR", "SQ", "SBARQ", "SINV", "UCP", "PRT", "FRAG":
			return true
		}
	case parent == "PP" || parent == "WHPP":
		if d.IsWord() {
			return false
		}
		switch d.Chain[0] {
		case "NP", "S", "SBAR", "SQ", "SINV", "WHNP", "NML":
			return true
		}
	case parent == "ADJP":
		return !d.IsWord() && (d.Chain[0] == "S" || d.Chain[0] == "SBAR")
	}
	return false
}

// head is the daughter that heads n, as the package comment says.
func head(n *interp.Node) int {
	if n.Bottom() == "VP" {
		h := -1
		for i, k := range n.Kids {
			if !k.IsWord() && k.Chain[0] == "VP" {
				if h >= 0 {
					h = -2 // two verb phrases: not a layer
				} else if h == -1 {
					h = i
				}
			}
		}
		if h >= 0 {
			return h
		}
	}
	if n.Bottom() == "SBAR" {
		for i, k := range n.Kids {
			if !k.IsWord() && clauses[k.Chain[0]] {
				return i
			}
		}
	}
	return n.Head
}

// conjuncts are the daughters of n that are coordinated, if it is a
// coordination: it has a conjunction, and two or more daughters of one
// category (or, failing that, every daughter but conjunctions,
// punctuation and words like both, either, not is a conjunct).
func conjuncts(n *interp.Node) []int {
	conj := false
	for _, k := range n.Kids {
		if k.IsWord() && (k.Cat == "CC") || !k.IsWord() && k.Chain[0] == "CONJP" {
			conj = true
		}
	}
	if !conj {
		return nil
	}
	kind := func(k *interp.Node) string {
		if k.IsWord() {
			switch {
			case k.Cat == "CC" || interp.IsPunctuation(k) || k.Cat == "DT" || k.Cat == "RB":
				return ""
			case strings.HasPrefix(k.Cat, "VB"):
				return "VB"
			case strings.HasPrefix(k.Cat, "NN"):
				return "NN"
			case strings.HasPrefix(k.Cat, "JJ"):
				return "JJ"
			}
			return k.Cat
		}
		if k.Chain[0] == "CONJP" {
			return ""
		}
		return k.Chain[0]
	}
	groups := map[string][]int{}
	var all []int
	for i, k := range n.Kids {
		if c := kind(k); c != "" {
			groups[c] = append(groups[c], i)
			all = append(all, i)
		}
	}
	var best []int
	for _, g := range groups {
		if len(g) > len(best) {
			best = g
		}
	}
	if len(best) >= 2 {
		return best
	}
	if len(all) >= 2 {
		return all
	}
	return nil
}

// Tree is a tree relabelled by counts: a phrase's label is its counts, a
// word's its tag and its type's counts, TAG_S1.N-2.P0.A0. Words are
// spelled as in n.
type Tree = cfg.Tree

// Convert relabels a tree as annotated.jsonl gives it, rooted at Top.
func Convert(root *interp.Node) (*Tree, error) {
	if len(root.Kids) != 1 {
		return nil, fmt.Errorf("root %s has %d daughters", root.Label, len(root.Kids))
	}
	out := &Tree{Label: "Top", Children: []*Tree{convert(root.Kids[0], category(root.Kids[0]))}}
	if err := Check(out); err != nil {
		return nil, err
	}
	return out, nil
}

func convert(n *interp.Node, c Vec) *Tree {
	if n.IsWord() {
		return &Tree{Label: n.Cat + "_" + c.String(), Words: []string{n.Word}}
	}
	// a phrase over one daughter is that daughter, with the same counts
	if len(n.Kids) == 1 {
		return convert(n.Kids[0], c)
	}
	counts := make([]Vec, len(n.Kids))
	if cj := conjuncts(n); cj != nil {
		in := map[int]bool{}
		for _, i := range cj {
			in[i] = true
		}
		rest := Vec{}
		for i, k := range n.Kids {
			if !in[i] && complement(n, k) {
				counts[i] = category(k)
				rest = rest.add(counts[i])
			}
		}
		each := c.sub(rest)
		first := -1
		for i, k := range n.Kids {
			switch {
			case in[i]:
				counts[i] = each
			case first < 0 && (k.IsWord() && k.Cat == "CC" || !k.IsWord() && k.Chain[0] == "CONJP"):
				first = i
				counts[i] = each.scale(-(len(cj) - 1))
			}
		}
	} else {
		h := head(n)
		rest := Vec{}
		for i, k := range n.Kids {
			if i != h && complement(n, k) {
				counts[i] = category(k)
				rest = rest.add(counts[i])
			}
		}
		counts[h] = c.sub(rest)
	}
	out := &Tree{Label: c.String()}
	for i, k := range n.Kids {
		out.Children = append(out.Children, convert(k, counts[i]))
	}
	return out
}

// Parse reads a counts label: a phrase's, or the part after a word's tag.
func Parse(label string) (Vec, bool) {
	if i := strings.LastIndex(label, "_"); i >= 0 {
		label = label[i+1:]
	}
	var v Vec
	_, err := fmt.Sscanf(label, "S%d.N%d.P%d.A%d", &v[0], &v[1], &v[2], &v[3])
	return v, err == nil
}

// Check says whether every phrase's counts are the sum of its daughters'.
func Check(t *Tree) error {
	if t.Words != nil {
		return nil
	}
	if t.Label != "Top" {
		want, ok := Parse(t.Label)
		if !ok {
			return fmt.Errorf("bad label %q", t.Label)
		}
		var sum Vec
		for _, k := range t.Children {
			v, ok := Parse(k.Label)
			if !ok {
				return fmt.Errorf("bad label %q", k.Label)
			}
			sum = sum.add(v)
		}
		if sum != want {
			return fmt.Errorf("%s has daughters summing to %s", t.Label, sum)
		}
	}
	for _, k := range t.Children {
		if err := Check(k); err != nil {
			return err
		}
	}
	return nil
}
