package cfg

import (
	"iter"
	"math/big"
	"math/rand"
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
	ways := f.ways()
	for _, g := range f.Goals {
		total.Add(total, ways[g])
	}
	return total
}

// Ways is the number of trees under each item, by item number.
func (f *Forest) Ways() []*big.Int { return f.ways() }

// ways is the number of trees under each item.
func (f *Forest) ways() []*big.Int {
	ways := make([]*big.Int, len(f.Items))
	tmp := new(big.Int)
	for _, item := range f.Order() {
		w := new(big.Int)
		for e, end := f.EdgeRange(item); e < end; e++ {
			w.Add(w, f.edgeWays(ways, e, tmp))
		}
		ways[item] = w
	}
	return ways
}

// edgeWays is the number of trees under hyperedge e, in tmp.
func (f *Forest) edgeWays(ways []*big.Int, e int, tmp *big.Int) *big.Int {
	h := f.Edge(e)
	tmp.SetInt64(1)
	if h.Left >= 0 {
		tmp.Mul(tmp, ways[h.Left])
	}
	if h.Right >= 0 {
		tmp.Mul(tmp, ways[h.Right])
	}
	return tmp
}

// Sampler draws trees of a forest at random, each tree of the whole input
// as likely as any other.
type Sampler struct {
	f    *Forest
	ways []*big.Int
	rng  *rand.Rand
}

// Sampler counts the trees under each item, once, for drawing trees with
// rng. The forest must have a tree.
func (f *Forest) Sampler(rng *rand.Rand) *Sampler { return &Sampler{f, f.ways(), rng} }

// Tree draws a tree: a goal item, then at each item a hyperedge, each in
// proportion to the number of trees under it.
func (s *Sampler) Tree() *Tree {
	total := new(big.Int)
	for _, g := range s.f.Goals {
		total.Add(total, s.ways[g])
	}
	r := new(big.Int).Rand(s.rng, total)
	for _, g := range s.f.Goals {
		if r.Cmp(s.ways[g]) < 0 {
			return s.pieces(g)[0]
		}
		r.Sub(r, s.ways[g])
	}
	panic("cfg: a sample beyond the count")
}

// pieces is what a drawn tree of item contributes to its parent's
// children, as Forest.pieces is for every tree.
func (s *Sampler) pieces(item int32) []*Tree {
	f := s.f
	it := f.Items[item]
	r := new(big.Int).Rand(s.rng, s.ways[item])
	tmp := new(big.Int)
	for e, end := f.EdgeRange(item); e < end; e++ {
		w := f.edgeWays(s.ways, e, tmp)
		if r.Cmp(w) >= 0 {
			r.Sub(r, w)
			continue
		}
		h := f.Edge(e)
		if h.Step < 0 {
			return []*Tree{{Label: f.G.Names[it.Sym], Words: f.Tokens[it.L:it.R]}}
		}
		children := s.pieces(h.Left)
		if h.Right >= 0 {
			children = slices.Concat(children, s.pieces(h.Right))
		}
		if f.G.Aux[it.Sym] {
			return children
		}
		return []*Tree{{Label: f.G.Names[it.Sym], Children: children}}
	}
	panic("cfg: a sample beyond the count")
}

// Stats counts the forest's items and hyperedges, and how many items are of
// the grammar's own symbols rather than auxiliary symbols of binarization.
func (f *Forest) Stats() (items, own, edges int) {
	for _, it := range f.Items {
		if !f.G.Aux[it.Sym] {
			own++
		}
	}
	return len(f.Items), own, f.edges
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
// as one tree, or, for an auxiliary symbol, the several trees it stands for.
func (f *Forest) pieces(item int32) iter.Seq[[]*Tree] {
	return func(yield func([]*Tree) bool) {
		it := f.Items[item]
		label := f.G.Names[it.Sym]
		wrap := func(children []*Tree) []*Tree {
			if f.G.Aux[it.Sym] {
				return children
			}
			return []*Tree{{Label: label, Children: children}}
		}
		for e, end := f.EdgeRange(item); e < end; e++ {
			h := *f.Edge(e)
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
// It checks each node against the hyperedges, through the binarization of
// its rule, without enumerating anything.
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
		for e, end := f.EdgeRange(item); e < end; e++ {
			if f.Edge(e).Step < 0 {
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
	// the hyperedges that build it, following its rule's binary tree
	g := f.G
	labels := make([]string, len(t.Children))
	for i, c := range t.Children {
		labels[i] = c.Label
	}
	ri, ok := g.ruleIndex[Rule{t.Label, labels}.String()]
	if !ok {
		return 0, false
	}
	has := func(item, step, left, right int32) bool {
		for e, end := f.EdgeRange(item); e < end; e++ {
			if h := f.Edge(e); h.Step == step && h.Left == left && h.Right == right {
				return true
			}
		}
		return false
	}
	start := func(i int) int32 {
		if i == 0 {
			return l
		}
		return ends[i-1]
	}
	// part is the item for a symbol covering the daughters from the i'th
	var part func(sym int32, i int) (int32, bool)
	part = func(sym int32, i int) (int32, bool) {
		if !g.Aux[sym] {
			return children[i], true
		}
		st := g.Steps[g.auxStep[sym]]
		left, ok := part(st.Left, i)
		if !ok {
			return 0, false
		}
		right, ok := part(st.Right, i+int(g.width[st.Left]))
		if !ok {
			return 0, false
		}
		aux, ok := f.Find(sym, start(i), ends[i+int(g.width[sym])-1])
		return aux, ok && has(aux, g.auxStep[sym], left, right)
	}
	top := g.Steps[g.ruleStep[ri]]
	left, ok := part(top.Left, 0)
	right := int32(-1)
	if ok && top.Right >= 0 {
		right, ok = part(top.Right, int(g.width[top.Left]))
	}
	return it.R, ok && has(item, g.ruleStep[ri], left, right)
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
