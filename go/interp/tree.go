// Package interp reads parse trees: it finds each phrase's head, and folds a
// tree into a reading of it, such as its dependencies, by one function for a
// word and one for a phrase. A reading is a homomorphism on derivations, in the
// sense of interpreted regular tree grammars (Koller and Kuhlmann 2011): the
// tree is parsed once, by package cfg or taken from a treebank, and each
// reading is a fold over it, sharing the walk.
package interp

import (
	"encoding/json"
	"fmt"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
)

// Node is a word or a phrase of a tree, with the categories a treebank uses.
type Node struct {
	Label string   // the grammar symbol, as the tree gives it: NPph[], SxVP[]
	Cat   string   // the category: NP, VP, NN, ","; for a collapsed chain, its top
	Chain []string // a chain of phrases collapsed into one node, top first: [S VP]; else [Cat]
	Fn    []string // function tags, SBJ, TMP, where the treebank gives them
	Kids  []*Node  // a phrase's daughters
	Word  string   // a word's form
	Pos   int      // a word's position, from 0
	Head  int      // the index in Kids of the daughter that heads the phrase; -1 for a word
	// ByDefault says the head rules had nothing to say about the phrase's
	// daughters, and its head is the first daughter from the rule's side.
	ByDefault bool
}

// IsWord says whether the node is a word.
func (n *Node) IsWord() bool { return n.Kids == nil }

// Bottom is the lowest category of a chain: the one whose daughters the node has.
func (n *Node) Bottom() string { return n.Chain[len(n.Chain)-1] }

// HeadWord is the word that heads the node.
func (n *Node) HeadWord() *Node {
	for !n.IsWord() {
		n = n.Kids[n.Head]
	}
	return n
}

// names undoes the renaming tools/masc/treebank.py does to make categories
// into grammar symbols.
var names = map[string]string{
	"Comma": ",", "Period": ".", "Colon": ":", "LQuote": "``", "RQuote": "''", "Quote": `"`, "Apos": "'",
	"Hash": "#", "Dollar": "$", "LRB": "-LRB-", "RRB": "-RRB-", "LSB": "-LSB-", "RSB": "-RSB-",
	"PRPS": "PRP$", "WPS": "WP$", "PROS": "PRO$", "Top": "TOP",
}

// categories reads a grammar label, NPph[] or SxVP[], as the chain of
// treebank categories it stands for: a phrase label that is also a tag
// somewhere has ph added, and a collapsed chain is joined by x.
func categories(label string) []string {
	label = strings.TrimSuffix(label, "[]")
	var out []string
	for _, c := range strings.Split(label, "x") {
		c = strings.TrimSuffix(c, "ph")
		if n, ok := names[c]; ok {
			c = n
		}
		out = append(out, c)
	}
	return out
}

// FromTree makes a tree of package cfg's, whose labels are grammar symbols as
// tools/masc/treebank.py writes them, into a Node tree, with its heads found.
func FromTree(t *cfg.Tree) *Node {
	pos := 0
	var node func(t *cfg.Tree) *Node
	node = func(t *cfg.Tree) *Node {
		chain := categories(t.Label)
		n := &Node{Label: t.Label, Cat: chain[0], Chain: chain, Head: -1}
		if t.Words != nil {
			// a lexical phrase, of several words, is one word here
			n.Word, n.Pos = strings.Join(t.Words, " "), pos
			pos++
			return n
		}
		for _, c := range t.Children {
			n.Kids = append(n.Kids, node(c))
		}
		return n
	}
	n := node(t)
	FindHeads(n)
	return n
}

// jnode is a node of tools/masc/treebank.py's annotated.jsonl.
type jnode struct {
	C string   `json:"c"`
	F []string `json:"f"`
	K []jnode  `json:"k"`
	W *string  `json:"w"`
}

// FromJSON reads a tree as tools/masc/treebank.py writes it to
// annotated.jsonl, with its function tags, and finds its heads.
func FromJSON(raw []byte) (*Node, error) {
	var j jnode
	if err := json.Unmarshal(raw, &j); err != nil {
		return nil, err
	}
	pos := 0
	var node func(j *jnode) (*Node, error)
	node = func(j *jnode) (*Node, error) {
		chain := categories(j.C)
		n := &Node{Label: j.C, Cat: chain[0], Chain: chain, Fn: j.F, Head: -1}
		if j.W != nil {
			n.Word, n.Pos = *j.W, pos
			pos++
			return n, nil
		}
		if len(j.K) == 0 {
			return nil, fmt.Errorf("%s has neither a word nor daughters", j.C)
		}
		for i := range j.K {
			k, err := node(&j.K[i])
			if err != nil {
				return nil, err
			}
			n.Kids = append(n.Kids, k)
		}
		return n, nil
	}
	n, err := node(&j)
	if err != nil {
		return nil, err
	}
	FindHeads(n)
	return n, nil
}

// String writes the tree in brackets, the head daughter of each phrase marked ^.
func (n *Node) String() string {
	var b strings.Builder
	var write func(n *Node, head bool)
	write = func(n *Node, head bool) {
		b.WriteByte('(')
		if head {
			b.WriteByte('^')
		}
		b.WriteString(strings.Join(n.Chain, "/"))
		for _, f := range n.Fn {
			b.WriteString("-" + f)
		}
		if n.IsWord() {
			b.WriteString(" " + n.Word)
		}
		for i, k := range n.Kids {
			b.WriteByte(' ')
			write(k, i == n.Head)
		}
		b.WriteByte(')')
	}
	write(n, false)
	return b.String()
}
