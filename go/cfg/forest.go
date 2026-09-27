package cfg

import (
	"iter"
	"math/big"
	"slices"
	"strings"
)

// Order is every item after every item its hyperedges join: by width, and
// within a span by unary rank.
func (f *Forest) Order() []int32 {
	out := make([]int32, len(f.Items))
	for i := range out {
		out[i] = int32(i)
	}
	rank := f.G.rank
	slices.SortFunc(out, func(a, b int32) int {
		x, y := f.Items[a], f.Items[b]
		if wx, wy := x.R-x.L, y.R-y.L; wx != wy {
			return int(wx - wy)
		}
		return int(rank[x.Sym] - rank[y.Sym])
	})
	return out
}

// Count is the number of trees of the whole input: derivations of the
// grammar's own rules, since binarization changes none.
func (f *Forest) Count() *big.Int {
	total := new(big.Int)
	if len(f.Goals) == 0 {
		return total
	}
	ways := make([]*big.Int, len(f.Items))
	tmp := new(big.Int)
	for _, item := range f.Order() {
		w := new(big.Int)
		for e := f.Head[item]; e >= 0; e = f.Edges[e].Next {
			h := &f.Edges[e]
			tmp.SetInt64(1)
			if h.Left >= 0 {
				tmp.Mul(tmp, ways[h.Left])
			}
			if h.Right >= 0 {
				tmp.Mul(tmp, ways[h.Right])
			}
			w.Add(w, tmp)
		}
		ways[item] = w
	}
	for _, g := range f.Goals {
		total.Add(total, ways[g])
	}
	return total
}

// Stats counts the forest's items and hyperedges, and how many items are of
// the grammar's own symbols rather than binarization prefixes.
func (f *Forest) Stats() (items, own, edges int) {
	for _, it := range f.Items {
		if !f.G.Prefix[it.Sym] {
			own++
		}
	}
	return len(f.Items), own, len(f.Edges)
}

// Tree is a parse tree in the grammar's own rules: a symbol over its
// children, or, for a lexical entry, over its words.
type Tree struct {
	Label    string
	Children []*Tree
	Words    []string
}

// String writes the tree in Penn Treebank brackets: (S (NP (DT the) ...)).
func (t *Tree) String() string {
	var b strings.Builder
	t.write(&b)
	return b.String()
}

func (t *Tree) write(b *strings.Builder) {
	b.WriteByte('(')
	b.WriteString(t.Label)
	for _, w := range t.Words {
		b.WriteByte(' ')
		b.WriteString(w)
	}
	for _, c := range t.Children {
		b.WriteByte(' ')
		c.write(b)
	}
	b.WriteByte(')')
}

// Trees yields the trees of the whole input one at a time, lazily: the
// first few of an astronomically ambiguous forest cost little.
func (f *Forest) Trees() iter.Seq[*Tree] {
	return func(yield func(*Tree) bool) {
		for _, g := range f.Goals {
			for seq := range f.pieces(g) {
				if !yield(seq[0]) {
					return
				}
			}
		}
	}
}

// pieces yields what an item contributes to its parent's children: the item
// as one tree, or, for a binarization prefix, the several trees it stands for.
func (f *Forest) pieces(item int32) iter.Seq[[]*Tree] {
	return func(yield func([]*Tree) bool) {
		it := f.Items[item]
		label := f.G.Names[it.Sym]
		wrap := func(children []*Tree) []*Tree {
			if f.G.Prefix[it.Sym] {
				return children
			}
			return []*Tree{{Label: label, Children: children}}
		}
		for e := f.Head[item]; e >= 0; e = f.Edges[e].Next {
			h := f.Edges[e]
			if h.Step < 0 {
				if !yield([]*Tree{{Label: label, Words: f.Tokens[it.L:it.R]}}) {
					return
				}
				continue
			}
			for left := range f.pieces(h.Left) {
				if h.Right < 0 {
					if !yield(wrap(left)) {
						return
					}
					continue
				}
				for right := range f.pieces(h.Right) {
					if !yield(wrap(slices.Concat(left, right))) {
						return
					}
				}
			}
		}
	}
}

// Contains says whether a tree of the grammar's own rules is in the forest.
// It checks each node against the hyperedges, through the binarization
// prefixes, without enumerating anything.
func (f *Forest) Contains(t *Tree) bool {
	for _, g := range f.Goals {
		if f.G.Names[f.Items[g].Sym] == t.Label {
			if _, ok := f.node(g, t, 0); ok {
				return true
			}
		}
	}
	return false
}

// node checks that the item, starting at word l, is the tree t, and returns
// where the tree ends.
func (f *Forest) node(item int32, t *Tree, l int32) (int32, bool) {
	it := f.Items[item]
	if it.L != l || f.G.Names[it.Sym] != t.Label {
		return 0, false
	}
	if t.Words != nil {
		if int(it.R-it.L) != len(t.Words) || !slices.Equal(f.Tokens[it.L:it.R], t.Words) {
			return 0, false
		}
		for e := f.Head[item]; e >= 0; e = f.Edges[e].Next {
			if f.Edges[e].Step < 0 {
				return it.R, true
			}
		}
		return 0, false
	}
	// the children's spans, found left to right
	ends := make([]int32, len(t.Children))
	at := l
	for i, c := range t.Children {
		end, ok := f.span(c, at)
		if !ok {
			return 0, false
		}
		ends[i] = end
		at = end
	}
	if at != it.R {
		return 0, false
	}
	children := make([]int32, len(t.Children))
	for i, c := range t.Children {
		start := l
		if i > 0 {
			start = ends[i-1]
		}
		sym, ok := f.G.Symbol(c.Label)
		if !ok {
			return 0, false
		}
		child, ok := f.Find(sym, start, ends[i])
		if !ok {
			return 0, false
		}
		if _, ok := f.node(child, c, start); !ok {
			return 0, false
		}
		children[i] = child
	}
	// the hyperedges that build it: prefix by prefix, then the item itself
	has := func(item, left, right int32) bool {
		for e := f.Head[item]; e >= 0; e = f.Edges[e].Next {
			if h := f.Edges[e]; h.Step >= 0 && h.Left == left && h.Right == right {
				return true
			}
		}
		return false
	}
	if len(children) == 1 {
		return it.R, has(item, children[0], -1)
	}
	left := children[0]
	for i := 1; i < len(children)-1; i++ {
		labels := make([]string, i+1)
		for j := range labels {
			labels[j] = t.Children[j].Label
		}
		p, ok := f.G.PrefixSymbol(labels)
		if !ok {
			return 0, false
		}
		pi, ok := f.Find(p, l, ends[i])
		if !ok || !has(pi, left, children[i]) {
			return 0, false
		}
		left = pi
	}
	return it.R, has(item, left, children[len(children)-1])
}

// span is the number of words a tree covers, added to where it starts.
func (f *Forest) span(t *Tree, l int32) (int32, bool) {
	if t.Words != nil {
		return l + int32(len(t.Words)), true
	}
	for _, c := range t.Children {
		var ok bool
		if l, ok = f.span(c, l); !ok {
			return 0, false
		}
	}
	return l, len(t.Children) > 0
}
