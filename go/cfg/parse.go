package cfg

import (
	"math/bits"
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

	index itemTable
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
	return f.index.get(key(sym, l, r))
}

// cells holds the symbols derivable over each span, as BitPar's chart does
// (Schmid 2004): for each start and symbol, a bit vector of the ends it is
// derivable up to, and for each end and symbol, a bit vector of the starts
// it is derivable from. Whether a step joins B over l..m and C over m..r for
// some m is then an AND of B's ends from l with C's starts to r, a word or
// two however many split points there are. Each span also has its symbols as
// a list, in the order they were found.
type cells struct {
	n, nsym int
	pos     int      // words in a vector over positions 0..n
	ends    []uint64 // (l*nsym + sym)*pos: the r with sym derivable over l..r
	starts  []uint64 // (r*nsym + sym)*pos: the l with sym derivable over l..r
	words   int      // words in a set of symbols
	lexical []uint64 // cell*words: the symbols a span is as a lexical entry
	syms    [][]int32
}

// cell numbers the spans l..r, 0 <= l < r <= n, one after another.
func (c *cells) cell(l, r int) int { return l*(2*c.n-l+1)/2 + (r - l - 1) }

func (c *cells) isLexical(sym int32, l, r int) bool {
	return c.lexical[c.cell(l, r)*c.words+int(sym>>6)]&(1<<(uint(sym)&63)) != 0
}

func (c *cells) has(sym int32, l, r int) bool {
	return c.ends[(l*c.nsym+int(sym))*c.pos+r>>6]&(1<<(uint(r)&63)) != 0
}

func (c *cells) add(sym int32, l, r int) bool {
	if c.has(sym, l, r) {
		return false
	}
	c.ends[(l*c.nsym+int(sym))*c.pos+r>>6] |= 1 << (uint(r) & 63)
	c.starts[(r*c.nsym+int(sym))*c.pos+l>>6] |= 1 << (uint(l) & 63)
	cell := c.cell(l, r)
	c.syms[cell] = append(c.syms[cell], sym)
	return true
}

// endsBefore says whether sym is derivable over l..m for some m < r.
func (c *cells) endsBefore(sym int32, l, r int) bool {
	v := c.ends[(l*c.nsym+int(sym))*c.pos:]
	last := (r - 1) >> 6
	for w := (l + 1) >> 6; w < last; w++ {
		if v[w] != 0 {
			return true
		}
	}
	return v[last]&(^uint64(0)>>(63-uint(r-1)&63)) != 0
}

// splits says whether left over l..m and right over m..r for some m.
func (c *cells) splits(left, right int32, l, r int) bool {
	a := c.ends[(l*c.nsym+int(left))*c.pos:]
	b := c.starts[(r*c.nsym+int(right))*c.pos:]
	for w := (l + 1) >> 6; w <= (r-1)>>6; w++ {
		if a[w]&b[w] != 0 {
			return true
		}
	}
	return false
}

// Parse builds the forest of the tokens.
func (g *Grammar) Parse(tokens []string) *Forest {
	f := &Forest{G: g, Tokens: tokens, index: newItemTable()}
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

// recognise is the bottom-up pass, CKY as BitPar does it: for each span,
// each symbol not yet found is tested against its binary steps until one
// joins two derivable symbols, all split points at once; a symbol found
// brings with it everything built from it by unary steps.
func (g *Grammar) recognise(tokens []string) *cells {
	n := len(tokens)
	c := &cells{n: n, nsym: len(g.Names), pos: (n + 64) / 64, words: (len(g.Names) + 63) / 64}
	c.ends = make([]uint64, (n+1)*c.nsym*c.pos)
	c.starts = make([]uint64, (n+1)*c.nsym*c.pos)
	c.lexical = make([]uint64, n*(n+1)/2*c.words)
	c.syms = make([][]int32, n*(n+1)/2)
	closure := func(sym int32, l, r int) {
		if c.add(sym, l, r) {
			for _, a := range g.unaryAbove[sym] {
				c.add(a, l, r)
			}
		}
	}
	for l := range n {
		for r := l + 1; r <= min(n, l+g.maxPhrase); r++ {
			for _, sym := range g.lexicon[strings.Join(tokens[l:r], " ")] {
				i := c.cell(l, r)*c.words + int(sym>>6)
				c.lexical[i] |= 1 << (uint(sym) & 63)
			}
		}
	}
	for width := 1; width <= n; width++ {
		for l := 0; l+width <= n; l++ {
			r := l + width
			if width <= g.maxPhrase {
				for _, sym := range g.lexicon[strings.Join(tokens[l:r], " ")] {
					closure(sym, l, r)
				}
			}
			if width == 1 {
				continue
			}
		parents:
			for _, a := range g.binParents {
				if c.has(a, l, r) {
					continue
				}
				for i := range g.binDown[a] {
					d := &g.binDown[a][i]
					if !c.endsBefore(d.left, l, r) {
						continue
					}
					for _, u := range d.steps {
						if c.splits(d.left, u.right, l, r) {
							closure(a, l, r)
							continue parents
						}
					}
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
		i, added := f.index.add(key(sym, l, r), int32(len(f.Items)))
		if !added {
			return i
		}
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
		if c.has(s, 0, int(n)) {
			f.Goals = append(f.Goals, intern(s, 0, n))
		}
	}
	for len(stack) > 0 {
		item := stack[len(stack)-1]
		stack = stack[:len(stack)-1]
		it := f.Items[item]
		f.first[item] = f.edges
		if c.isLexical(it.Sym, int(it.L), int(it.R)) {
			edge(item, -1, -1, -1)
		}
		for _, si := range g.unaryDown[it.Sym] {
			if child := g.Steps[si].Left; c.has(child, int(it.L), int(it.R)) {
				edge(item, si, intern(child, it.L, it.R), -1)
			}
		}
		// each binary step's split points, all at once
		l, r := int(it.L), int(it.R)
		for i := range g.binDown[it.Sym] {
			d := &g.binDown[it.Sym][i]
			if !c.endsBefore(d.left, l, r) {
				continue
			}
			a := c.ends[(l*c.nsym+int(d.left))*c.pos:]
			for _, u := range d.steps {
				b := c.starts[(r*c.nsym+int(u.right))*c.pos:]
				for w := (l + 1) >> 6; w <= (r-1)>>6; w++ {
					for x := a[w] & b[w]; x != 0; x &= x - 1 {
						m := int32(w<<6 + bits.TrailingZeros64(x))
						edge(item, u.step, intern(d.left, it.L, m), intern(u.right, m, it.R))
					}
				}
			}
		}
	}
}
