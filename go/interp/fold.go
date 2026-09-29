package interp

import "strings"

// Fold computes a reading of a tree bottom up: word gives a word's value,
// and phrase a phrase's, from the phrase and its daughters' values. A
// reading is a homomorphism from the tree to its values; the tree is walked
// the same way for every one.
func Fold[T any](n *Node, word func(n *Node) T, phrase func(n *Node, kids []T) T) T {
	if n.IsWord() {
		return word(n)
	}
	kids := make([]T, len(n.Kids))
	for i, k := range n.Kids {
		kids[i] = Fold(k, word, phrase)
	}
	return phrase(n, kids)
}

// Dep is an arc from a head word to a word depending on it, positions from
// 0; the root word has Head -1.
type Dep struct {
	Dependent, Head int
	Label           string
}

// Dependencies is the tree's dependency reading: in each phrase, the head
// word of every daughter but the head depends on the head daughter's head
// word. The arc is labelled by the daughter's function tags where the tree has
// them, SBJ or TMP, and by its category otherwise, NP or DT. The result is in
// the order of the dependents.
func Dependencies(n *Node) []Dep {
	words := Fold(n, func(n *Node) int { return 1 }, func(n *Node, kids []int) int {
		s := 0
		for _, k := range kids {
			s += k
		}
		return s
	})
	deps := make([]Dep, words)
	root := Fold(n, func(n *Node) int { return n.Pos }, func(n *Node, kids []int) int {
		h := kids[n.Head]
		for i, k := range n.Kids {
			if i != n.Head {
				deps[kids[i]] = Dep{kids[i], h, Label(k)}
			}
		}
		return h
	})
	deps[root] = Dep{root, -1, "ROOT"}
	return deps
}

// Label is what an arc to a daughter is called: its function tags, or its
// category if it has none.
func Label(n *Node) string {
	if len(n.Fn) > 0 {
		return strings.Join(n.Fn, "-")
	}
	return n.Cat
}
