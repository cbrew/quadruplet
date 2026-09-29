package interp

import (
	"encoding/json"
	"fmt"
	"io"
	"strings"
)

// FunctionTable predicts the function tags of a phrase, SBJ or TMP or none,
// from where it stands in its parent, having counted them in a treebank. A
// phrase's configuration is its parent's category, its own, which side of
// the head daughter it is on and how far away, and the head daughter's
// category; finer than that, the word heading it, and coarser, less of the
// rest. Where a configuration was seen too rarely, the table backs off to a
// coarser one, down to the phrase's category alone.
type FunctionTable struct {
	// Counts maps a configuration, at each level of backing off, to the
	// tags seen there, joined by "-", "" for none, and how often.
	Counts map[string]map[string]int `json:"counts"`
	// MinCount is how often a configuration must have been seen to be used.
	MinCount int `json:"min_count"`
}

// NewFunctionTable is an empty table using configurations seen at least
// minCount times.
func NewFunctionTable(minCount int) *FunctionTable {
	return &FunctionTable{Counts: map[string]map[string]int{}, MinCount: minCount}
}

// configurations are the keys of daughter i of phrase n, finest first.
func configurations(n *Node, i int) []string {
	k := n.Kids[i]
	side, dist := "h", 0
	switch {
	case i < n.Head:
		side, dist = "l", n.Head-i
	case i > n.Head:
		side, dist = "r", i-n.Head
	}
	d := fmt.Sprint(min(dist, 3))
	parent, cat, head := n.Bottom(), k.Cat, n.Kids[n.Head].Cat
	word := strings.ToLower(k.HeadWord().Word)
	return []string{
		parent + " " + cat + " " + side + d + " " + head + " " + word,
		parent + " " + cat + " " + side + " " + word,
		parent + " " + cat + " " + side + d + " " + head,
		parent + " " + cat + " " + side + d,
		parent + " " + cat + " " + side,
		cat + " " + side,
		cat,
	}
}

// Learn counts the function tags of every phrase of a tree.
func (t *FunctionTable) Learn(n *Node) {
	Fold(n, func(*Node) struct{} { return struct{}{} }, func(n *Node, _ []struct{}) struct{} {
		for i, k := range n.Kids {
			if k.IsWord() {
				continue
			}
			tags := strings.Join(k.Fn, "-")
			for _, c := range configurations(n, i) {
				if t.Counts[c] == nil {
					t.Counts[c] = map[string]int{}
				}
				t.Counts[c][tags]++
			}
		}
		return struct{}{}
	})
}

// Predict is the function tags most often seen on a phrase in daughter i's
// configuration, at the finest level seen often enough; none if none was.
func (t *FunctionTable) Predict(n *Node, i int) []string {
	for _, c := range configurations(n, i) {
		counts := t.Counts[c]
		total, best, bestCount := 0, "", -1
		for tags, k := range counts {
			total += k
			if k > bestCount || k == bestCount && tags < best {
				best, bestCount = tags, k
			}
		}
		if total >= t.MinCount {
			if best == "" {
				return nil
			}
			return strings.Split(best, "-")
		}
	}
	return nil
}

// Assign gives every phrase of a tree the function tags the table predicts,
// in place of any it had.
func (t *FunctionTable) Assign(n *Node) {
	if n.IsWord() {
		return
	}
	// predict from the configurations first, then descend, so that no
	// prediction sees another's result
	for i, k := range n.Kids {
		if !k.IsWord() {
			k.Fn = t.Predict(n, i)
		}
	}
	for _, k := range n.Kids {
		t.Assign(k)
	}
}

// Write writes the table as JSON.
func (t *FunctionTable) Write(w io.Writer) error { return json.NewEncoder(w).Encode(t) }

// ReadFunctionTable reads a table Write wrote.
func ReadFunctionTable(r io.Reader) (*FunctionTable, error) {
	var t FunctionTable
	if err := json.NewDecoder(r).Decode(&t); err != nil {
		return nil, err
	}
	return &t, nil
}
