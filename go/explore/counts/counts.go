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
	"slices"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/interp"
)

// Vec is counts of the basic types: S (a finite clause), I (a nonfinite
// one), W (a wh- or if/whether clause, or a question), NP, PP and AP.
type Vec [6]int

func (v Vec) add(w Vec) Vec {
	for i := range v {
		v[i] += w[i]
	}
	return v
}

func (v Vec) sub(w Vec) Vec {
	for i := range v {
		v[i] -= w[i]
	}
	return v
}

func (v Vec) scale(k int) Vec {
	for i := range v {
		v[i] *= k
	}
	return v
}

// String is the counts as a grammar symbol: S1.I0.W0.N-1.P0.A0.
func (v Vec) String() string {
	return fmt.Sprintf("S%d.I%d.W%d.N%d.P%d.A%d", v[0], v[1], v[2], v[3], v[4], v[5])
}

var (
	vS  = Vec{1, 0, 0, 0, 0, 0}
	vI  = Vec{0, 1, 0, 0, 0, 0}
	vW  = Vec{0, 0, 1, 0, 0, 0}
	vNP = Vec{0, 0, 0, 1, 0, 0}
	vPP = Vec{0, 0, 0, 0, 1, 0}
	vAP = Vec{0, 0, 0, 0, 0, 1}
)

// clauseType is the type of the clause n is or ends in: W for a question or
// a clause introduced by a wh-phrase, if or whether; else S if its verb
// chain (the verb phrases down its heads) has a finite verb or a modal,
// and I if not.
func clauseType(n *interp.Node) Vec {
	top := n.Chain[0]
	if top == "SQ" || top == "SBARQ" {
		return vW
	}
	if top == "SBAR" {
		if wh(n) >= 0 {
			return vW
		}
		if c := clauseDaughter(n); c >= 0 {
			return clauseType(n.Kids[c])
		}
		return vS
	}
	// the verb chain: n itself if it ends in VP, else its VP daughter
	v := n
	if n.Bottom() != "VP" {
		v = nil
		for _, k := range n.Kids {
			if !k.IsWord() && k.Chain[0] == "VP" {
				v = k
				break
			}
		}
		if v == nil {
			if top == "FRAG" || top == "RRC" {
				return vS
			}
			return vI // a verbless small clause
		}
	}
	for v != nil && !v.IsWord() {
		var next *interp.Node
		for _, k := range v.Kids {
			switch {
			case k.IsWord():
				switch k.Cat {
				case "VBD", "VBZ", "VBP", "MD":
					return vS
				}
			case k.Chain[0] == "VP" && next == nil:
				next = k
			}
		}
		v = next
	}
	return vI
}

// wh is the daughter of an SBAR that makes it a W clause: a wh-phrase, or
// if or whether; -1 if none.
func wh(n *interp.Node) int {
	for i, k := range n.Kids {
		if k.IsWord() {
			if w := strings.ToLower(k.Word); k.Cat == "IN" && (w == "if" || w == "whether") {
				return i
			}
			continue
		}
		if strings.HasPrefix(k.Chain[0], "WH") {
			return i
		}
	}
	return -1
}

// clauseDaughter is an SBAR's clause, or -1.
func clauseDaughter(n *interp.Node) int {
	for i, k := range n.Kids {
		if !k.IsWord() && clauses[k.Chain[0]] {
			return i
		}
	}
	return -1
}

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
		return clauseType(n).sub(vNP)
	case clauses[top]:
		return clauseType(n)
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

// Options are choices in the conversion.
type Options struct {
	// NormalVerbs puts each verb's projection in a normal form: the
	// verb, and its dependents (complements, and modifiers counting zero,
	// auxiliaries among them) attached one at a time, head outward: those
	// on its right first, nearest first, then those on its left, nearest
	// first. The phrases in between are labelled R:counts and L:counts, and
	// the projection's top by its counts. So a projection's tree is fixed
	// by its verb and its dependents, whatever the order of attachment was.
	NormalVerbs bool
	// FlatVerbs makes each verb's projection one node, labelled V:counts,
	// whose daughters are the verb (or the coordination heading the
	// lowest verb phrase) and all the projection's dependents, in the
	// order of the words: complements and modifiers, auxiliaries and
	// modals among them. It is the verb's normal form as an unordered
	// (ID) rule, the order being the sentence's.
	FlatVerbs bool
}

// Convert relabels a tree as annotated.jsonl gives it, rooted at Top.
func Convert(root *interp.Node) (*Tree, error) { return ConvertWith(root, Options{}) }

// ConvertWith is Convert with options.
func ConvertWith(root *interp.Node, o Options) (*Tree, error) {
	if len(root.Kids) != 1 {
		return nil, fmt.Errorf("root %s has %d daughters", root.Label, len(root.Kids))
	}
	cv := converter{o}
	out := &Tree{Label: "Top", Children: []*Tree{cv.convert(root.Kids[0], category(root.Kids[0]))}}
	if err := Check(out); err != nil {
		return nil, err
	}
	return out, nil
}

type converter struct{ o Options }

func (cv converter) convert(n *interp.Node, c Vec) *Tree {
	if n.IsWord() {
		return &Tree{Label: n.Cat + "_" + c.String(), Words: []string{n.Word}}
	}
	// a phrase over one daughter is that daughter, with the same counts
	if len(n.Kids) == 1 {
		return cv.convert(n.Kids[0], c)
	}
	if (cv.o.NormalVerbs || cv.o.FlatVerbs) && n.Bottom() == "VP" && conjuncts(n) == nil {
		return cv.normal(n, c)
	}
	counts := cv.share(n, c)
	out := &Tree{Label: c.String()}
	for i, k := range n.Kids {
		out.Children = append(out.Children, cv.convert(k, counts[i]))
	}
	return out
}

// share gives each daughter of n its counts, n having c.
func (cv converter) share(n *interp.Node, c Vec) []Vec {
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
	} else if n.Bottom() == "SBAR" && clauseDaughter(n) >= 0 {
		// the clause keeps its own type; a wh-phrase, if or whether (or
		// else the first daughter) carries the difference
		cl := clauseDaughter(n)
		counts[cl] = clauseType(n.Kids[cl])
		carrier := wh(n)
		if carrier < 0 {
			carrier = cl
			counts[cl] = c
		} else {
			counts[carrier] = c.sub(counts[cl])
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
	return counts
}

// dependent is a daughter on a verb's projection that is not on its chain
// of heads, with its counts and the position of its first word.
type dependent struct {
	n     *interp.Node
	c     Vec
	first int
}

func firstWord(n *interp.Node) int {
	for !n.IsWord() {
		n = n.Kids[0]
	}
	return n.Pos
}

// normal is the verb projection topped by n in normal form: down its
// chain of heads (layers of adjunction and auxiliaries, whose head is a
// verb phrase) to the lexical verb phrase and its verb, gathering the
// dependents; then the verb (the core: the head of the lowest phrase, which
// may also be a coordination) with the dependents attached head outward.
func (cv converter) normal(n *interp.Node, c Vec) *Tree {
	var deps []dependent
	m, cm := n, c
	var core *Tree
	coreFirst := 0
	for {
		for len(m.Kids) == 1 && !m.IsWord() {
			m = m.Kids[0]
		}
		if m.IsWord() || conjuncts(m) != nil {
			core, coreFirst = cv.convert(m, cm), firstWord(m)
			break
		}
		counts := cv.share(m, cm)
		h := head(m)
		for i, k := range m.Kids {
			if i != h {
				deps = append(deps, dependent{k, counts[i], firstWord(k)})
			}
		}
		hk := m.Kids[h]
		if hk.IsWord() || hk.Bottom() != "VP" {
			core, coreFirst = cv.convert(hk, counts[h]), firstWord(hk)
			break
		}
		m, cm = hk, counts[h]
	}
	if cv.o.FlatVerbs {
		all := append([]dependent{{nil, Vec{}, coreFirst}}, deps...)
		slices.SortFunc(all, func(a, b dependent) int { return a.first - b.first })
		out := &Tree{Label: "V:" + c.String()}
		for _, d := range all {
			if d.n == nil {
				out.Children = append(out.Children, core)
			} else {
				out.Children = append(out.Children, cv.convert(d.n, d.c))
			}
		}
		if len(out.Children) == 1 {
			return core
		}
		return out
	}
	var left, right []dependent
	for _, d := range deps {
		if d.first < coreFirst {
			left = append(left, d)
		} else {
			right = append(right, d)
		}
	}
	slices.SortFunc(right, func(a, b dependent) int { return a.first - b.first })
	slices.SortFunc(left, func(a, b dependent) int { return b.first - a.first })
	x := core
	cur, _ := Parse(core.Label)
	for _, d := range right {
		cur = cur.add(d.c)
		x = &Tree{Label: "R:" + cur.String(), Children: []*Tree{x, cv.convert(d.n, d.c)}}
	}
	for _, d := range left {
		cur = cur.add(d.c)
		x = &Tree{Label: "L:" + cur.String(), Children: []*Tree{cv.convert(d.n, d.c), x}}
	}
	if x != core {
		x.Label = c.String()
	}
	return x
}

// Parse reads a counts label: a phrase's, or the part after a word's tag.
func Parse(label string) (Vec, bool) {
	if i := strings.LastIndex(label, "_"); i >= 0 {
		label = label[i+1:]
	}
	for _, p := range []string{"R:", "L:", "V:"} {
		label = strings.TrimPrefix(label, p)
	}
	var v Vec
	_, err := fmt.Sscanf(label, "S%d.I%d.W%d.N%d.P%d.A%d", &v[0], &v[1], &v[2], &v[3], &v[4], &v[5])
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
