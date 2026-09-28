package cfg

import (
	"slices"
	"strings"
	"time"
)

// Item is a symbol over the words L..R (half open).
type Item struct {
	Sym  int32
	L, R int32
}

// Hyperedge is one way of building an item: a step of the binarized grammar
// and the items it joins (Right is -1 for a unary step), or, when Step is
// -1, the item's words as a lexical entry.
type Hyperedge struct {
	Step, Left, Right int32
}

// edgeBlock is how many hyperedges a block holds. The hyperedges are kept in
// blocks of this size so that adding one never copies the others: a forest
// can hold a hundred million of them.
const edgeBlock = 1 << 14

// Forest is the packed forest of an input: every item on a derivation of the
// whole input from a start symbol, and every way of building each.
type Forest struct {
	G      *Grammar
	Tokens []string
	Items  []Item
	Goals  []int32 // the start symbols' items over the whole input

	// An item's hyperedges are all found while it is visited, so they are
	// numbered consecutively: first[item] up to first[item]+count[item].
	first  []int
	count  []int32
	blocks [][]Hyperedge
	edges  int

	index map[uint64]int32
	// Derivable is how many (symbol, span) pairs the bottom-up pass found,
	// most of them on no derivation of the whole input.
	Derivable int
	Recognise time.Duration // the bottom-up pass
	Build     time.Duration // the top-down pass
}

func key(sym, l, r int32) uint64 { return uint64(sym)<<32 | uint64(l)<<16 | uint64(r) }

// EdgeRange is the numbers of the item's hyperedges, first <= e < end, in
// the order they were found.
func (f *Forest) EdgeRange(item int32) (first, end int) {
	return f.first[item], f.first[item] + int(f.count[item])
}

// Edge is hyperedge number e.
func (f *Forest) Edge(e int) *Hyperedge { return &f.blocks[e/edgeBlock][e%edgeBlock] }

func (f *Forest) addEdge(h Hyperedge) {
	if f.edges%edgeBlock == 0 {
		f.blocks = append(f.blocks, make([]Hyperedge, edgeBlock))
	}
	f.blocks[f.edges/edgeBlock][f.edges%edgeBlock] = h
	f.edges++
}

// Find is the forest's item for a symbol over l..r, if there is one.
func (f *Forest) Find(sym, l, r int32) (int32, bool) {
	i, ok := f.index[key(sym, l, r)]
	return i, ok
}

// cells holds, for each span, the set of symbols derivable over it: as a
// bitset, and as a list in the order they were found.
type cells struct {
	n, words int
	has      []uint64
	lexical  []uint64 // the symbols a span is as a lexical entry
	syms     [][]int32
}

// cell numbers the spans l..r, 0 <= l < r <= n, one after another.
func (c *cells) cell(l, r int) int { return l*(2*c.n-l+1)/2 + (r - l - 1) }

func (c *cells) test(bits []uint64, sym int32, cell int) bool {
	return bits[cell*c.words+int(sym>>6)]&(1<<(uint(sym)&63)) != 0
}

func (c *cells) set(bits []uint64, sym int32, cell int) bool {
	i, m := cell*c.words+int(sym>>6), uint64(1)<<(uint(sym)&63)
	if bits[i]&m != 0 {
		return false
	}
	bits[i] |= m
	return true
}

func (c *cells) add(sym int32, cell int) {
	if c.set(c.has, sym, cell) {
		c.syms[cell] = append(c.syms[cell], sym)
	}
}

// Parse builds the forest of the tokens.
func (g *Grammar) Parse(tokens []string) *Forest {
	f := &Forest{G: g, Tokens: tokens, index: map[uint64]int32{}}
	n := len(tokens)
	if n == 0 || n >= 1<<16 {
		return f
	}
	start := time.Now()
	c := g.recognise(tokens)
	f.Recognise = time.Since(start)
	for _, s := range c.syms {
		f.Derivable += len(s)
	}
	start = time.Now()
	g.build(f, c)
	f.Build = time.Since(start)
	return f
}

// recognise is the bottom-up pass: CKY over bitsets, closing each span under
// the unary steps.
func (g *Grammar) recognise(tokens []string) *cells {
	n := len(tokens)
	c := &cells{n: n, words: (len(g.Names) + 63) / 64}
	spans := n * (n + 1) / 2
	c.has = make([]uint64, spans*c.words)
	c.lexical = make([]uint64, spans*c.words)
	c.syms = make([][]int32, spans)
	for l := range n {
		for r := l + 1; r <= min(n, l+g.maxPhrase); r++ {
			cell := c.cell(l, r)
			for _, sym := range g.lexicon[strings.Join(tokens[l:r], " ")] {
				c.set(c.lexical, sym, cell)
				c.add(sym, cell)
			}
		}
	}
	for width := 1; width <= n; width++ {
		for l := 0; l+width <= n; l++ {
			r := l + width
			cell := c.cell(l, r)
			for m := l + 1; m < r; m++ {
				rightCell := c.cell(m, r)
				rights := c.syms[rightCell]
				if len(rights) == 0 {
					continue
				}
				for _, a := range c.syms[c.cell(l, m)] {
					ups := g.binUp[a]
					if len(ups) == 0 {
						continue
					}
					if len(ups) <= len(rights) {
						for _, u := range ups {
							if c.test(c.has, u.right, rightCell) {
								c.add(g.Steps[u.step].Parent, cell)
							}
						}
						continue
					}
					for _, b := range rights {
						j, _ := slices.BinarySearchFunc(ups, b, func(u up, b int32) int { return int(u.right - b) })
						for ; j < len(ups) && ups[j].right == b; j++ {
							c.add(g.Steps[ups[j].step].Parent, cell)
						}
					}
				}
			}
			for k := 0; k < len(c.syms[cell]); k++ {
				for _, si := range g.unaryUp[c.syms[cell][k]] {
					c.add(g.Steps[si].Parent, cell)
				}
			}
		}
	}
	return c
}

// build is the top-down pass. From each start symbol over the whole input it
// follows every step whose daughters were derived, so the items it reaches
// are exactly those on a derivation of the whole input, and it records each
// step as a hyperedge.
func (g *Grammar) build(f *Forest, c *cells) {
	n := int32(c.n)
	var stack []int32
	intern := func(sym, l, r int32) int32 {
		k := key(sym, l, r)
		if i, ok := f.index[k]; ok {
			return i
		}
		i := int32(len(f.Items))
		f.index[k] = i
		f.Items = append(f.Items, Item{sym, l, r})
		f.first = append(f.first, 0)
		f.count = append(f.count, 0)
		stack = append(stack, i)
		return i
	}
	edge := func(item, step, left, right int32) {
		f.addEdge(Hyperedge{step, left, right})
		f.count[item]++
	}
	for _, s := range g.Starts {
		if c.test(c.has, s, c.cell(0, int(n))) {
			f.Goals = append(f.Goals, intern(s, 0, n))
		}
	}
	for len(stack) > 0 {
		item := stack[len(stack)-1]
		stack = stack[:len(stack)-1]
		it := f.Items[item]
		f.first[item] = f.edges
		cell := c.cell(int(it.L), int(it.R))
		if c.test(c.lexical, it.Sym, cell) {
			edge(item, -1, -1, -1)
		}
		for _, si := range g.unaryDown[it.Sym] {
			if child := g.Steps[si].Left; c.test(c.has, child, cell) {
				edge(item, si, intern(child, it.L, it.R), -1)
			}
		}
		downs := g.binDown[it.Sym]
		if len(downs) == 0 {
			continue
		}
		for m := it.L + 1; m < it.R; m++ {
			leftCell, rightCell := c.cell(int(it.L), int(m)), c.cell(int(m), int(it.R))
			lefts := c.syms[leftCell]
			if len(lefts) == 0 || len(c.syms[rightCell]) == 0 {
				continue
			}
			join := func(d *down) {
				for _, u := range d.steps {
					if c.test(c.has, u.right, rightCell) {
						edge(item, u.step, intern(d.left, it.L, m), intern(u.right, m, it.R))
					}
				}
			}
			if len(downs) <= len(lefts) {
				for i := range downs {
					if c.test(c.has, downs[i].left, leftCell) {
						join(&downs[i])
					}
				}
				continue
			}
			for _, a := range lefts {
				if j, ok := slices.BinarySearchFunc(downs, a, func(d down, a int32) int { return int(d.left - a) }); ok {
					join(&downs[j])
				}
			}
		}
	}
}
