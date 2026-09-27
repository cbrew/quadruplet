// Package chart is a bottom-up, left-corner chart parser for feature
// grammars, ported from the Kotlin Chart.
//
// Edges are either complete (a category spanning some words) or partial
// (a category still needing further categories to its right). New edges go
// on an agenda; each edge taken from it is added to the chart and combined
// with the edges already there by the fundamental rule, and complete edges
// also spawn partial edges for the rules they can start. Every distinct
// edge is kept once, with the (partial, complete) pairs that produced it, so
// the chart is a packed forest from which trees are counted and enumerated.
package chart

import (
	"math/big"
	"strconv"
	"strings"

	"github.com/cbrew/quadruplet/go/term"
)

// Edge is a chart edge: Cat spanning words Start..End (exclusive). A partial
// edge still needs the categories in Needed; a complete edge needs none.
type Edge struct {
	Cat        term.Term
	Start, End int
	Needed     []term.Term

	hash  uint64
	added bool // in the chart (not just on the agenda)
}

// NewEdge builds an edge; it is complete when needed is empty.
func NewEdge(cat term.Term, start, end int, needed []term.Term) *Edge {
	h := cat.Hash()*31 + uint64(start)
	h = h*31 + uint64(end)
	for _, n := range needed {
		h = h*31 + n.Hash()
	}
	return &Edge{Cat: cat, Start: start, End: end, Needed: needed, hash: h}
}

// Complete reports whether the edge needs nothing more.
func (e *Edge) Complete() bool { return len(e.Needed) == 0 }

func (e *Edge) equal(o *Edge) bool {
	if e.hash != o.hash || e.Start != o.Start || e.End != o.End || len(e.Needed) != len(o.Needed) ||
		!term.Equal(e.Cat, o.Cat) {
		return false
	}
	for i := range e.Needed {
		if !term.Equal(e.Needed[i], o.Needed[i]) {
			return false
		}
	}
	return true
}

// String prints the edge as the Kotlin data classes do.
func (e *Edge) String() string {
	var b strings.Builder
	if e.Complete() {
		b.WriteString("Complete(category=")
	} else {
		b.WriteString("Partial(category=")
	}
	b.WriteString(e.Cat.String())
	b.WriteString(", start=" + itoa(e.Start) + ", end=" + itoa(e.End))
	if !e.Complete() {
		b.WriteString(", needed=[")
		for i, n := range e.Needed {
			if i > 0 {
				b.WriteString(", ")
			}
			b.WriteString(n.String())
		}
		b.WriteByte(']')
	}
	b.WriteByte(')')
	return b.String()
}

// Grammar supplies the edges a chart is built from.
type Grammar interface {
	// Lookup returns complete edges for a word, or for a phrase of several
	// space-separated words, spanning start..end.
	Lookup(phrase string, start, end int) []*Edge
	// Spawn returns zero-length partial edges, at lc.Start, for the rules
	// whose first needed category unifies with the complete edge lc.
	Spawn(lc *Edge) []*Edge
}

// Pair is a way an edge was made: a partial edge extended by a complete one.
type Pair struct {
	Partial, Complete *Edge
}

// Span is a word or lexical phrase found in the sentence.
type Span struct {
	Label      string
	Start, End int
}

// Chart holds the edges for one sentence.
type Chart struct {
	words     []string
	completes [][]*Edge // by start
	partials  [][]*Edge // by end
	edges     map[uint64][]*Edge
	preds     map[*Edge][]Pair
	agenda    agenda
	seq       int
	spans     []Span
}

// New makes an empty chart for the words of a sentence.
func New(words []string) *Chart {
	return &Chart{
		words:     words,
		completes: make([][]*Edge, len(words)+1),
		partials:  make([][]*Edge, len(words)+1),
		edges:     map[uint64][]*Edge{},
		preds:     map[*Edge][]Pair{},
	}
}

// intern returns the chart's edge equal to e, adding e if there is none,
// and whether it is new.
func (c *Chart) intern(e *Edge) (*Edge, bool) {
	for _, o := range c.edges[e.hash] {
		if o.equal(e) {
			return o, false
		}
	}
	c.edges[e.hash] = append(c.edges[e.hash], e)
	return e, true
}

// push interns e and puts it on the agenda if it is new.
func (c *Chart) push(e *Edge) *Edge {
	e, isNew := c.intern(e)
	if isNew {
		c.seq++
		c.agenda.push(agendaItem{e, c.seq})
	}
	return e
}

// Parse fills the chart using g.
func (c *Chart) Parse(g Grammar) {
	c.Start(g)
	for !c.Done() {
		c.Step(g)
	}
}

// Start puts the lexical edges for the sentence on the agenda: each word,
// and each phrase of several words that the grammar knows.
func (c *Chart) Start(g Grammar) {
	for j, w := range c.words {
		if es := g.Lookup(w, j, j+1); len(es) > 0 {
			c.spans = append(c.spans, Span{w, j, j + 1})
			for _, e := range es {
				c.push(e)
			}
		}
		for i := 0; i < j; i++ {
			phrase := strings.Join(c.words[i:j+1], " ")
			if es := g.Lookup(phrase, i, j+1); len(es) > 0 {
				c.spans = append(c.spans, Span{phrase, i, j + 1})
				for _, e := range es {
					c.push(e)
				}
			}
		}
	}
}

// Done reports whether the agenda is empty.
func (c *Chart) Done() bool { return len(c.agenda) == 0 }

// Step takes the next edge from the agenda, adds it to the chart and
// combines it with the edges there. It returns the edge.
func (c *Chart) Step(g Grammar) *Edge {
	e := c.agenda.pop().edge
	e.added = true
	if e.Complete() {
		c.completes[e.Start] = append(c.completes[e.Start], e)
		for _, s := range g.Spawn(e) {
			c.push(s)
		}
		for _, p := range c.partials[e.Start] {
			c.combine(p, e)
		}
	} else {
		c.partials[e.End] = append(c.partials[e.End], e)
		for _, cm := range c.completes[e.End] {
			c.combine(e, cm)
		}
	}
	return e
}

func (c *Chart) combine(p, cm *Edge) {
	if e := Fundamental(p, cm); e != nil {
		e = c.push(e)
		c.preds[e] = append(c.preds[e], Pair{p, cm})
	}
}

// Fundamental is the fundamental rule of chart parsing: the partial edge p
// extended by the complete edge c, or nil if c's category does not unify
// with the first category p needs. c's variables are renamed apart from
// p's first, since a shared name does not mean a shared variable.
func Fundamental(p, c *Edge) *Edge {
	cat := c.Cat
	if !cat.Ground() {
		cat = term.RenamedApart(cat, append([]term.Term{p.Cat}, p.Needed...)...)
	}
	_, b, ok := term.UnifyBindings(p.Needed[0], cat, nil)
	if !ok {
		return nil
	}
	rest := make([]term.Term, len(p.Needed)-1)
	for i, n := range p.Needed[1:] {
		rest[i] = b.Subst(n)
	}
	return NewEdge(b.Subst(p.Cat), p.Start, c.End, rest)
}

// Words returns the sentence.
func (c *Chart) Words() []string { return c.words }

// Spans returns the words and lexical phrases found, in order found.
func (c *Chart) Spans() []Span { return c.spans }

// Completes returns the complete edges, in order added.
func (c *Chart) Completes() []*Edge { return flatten(c.completes) }

// Partials returns the partial edges, in order added.
func (c *Chart) Partials() []*Edge { return flatten(c.partials) }

func flatten(buckets [][]*Edge) []*Edge {
	var out []*Edge
	for _, b := range buckets {
		out = append(out, b...)
	}
	return out
}

// Predecessors returns the ways an edge was made; lexical and spawned edges
// have none.
func (c *Chart) Predecessors(e *Edge) []Pair { return c.preds[e] }

// Solutions returns the complete edges spanning the whole sentence.
func (c *Chart) Solutions() []*Edge {
	var out []*Edge
	for _, e := range c.completes[0] {
		if e.End == len(c.words) {
			out = append(out, e)
		}
	}
	return out
}

// SolutionsMatching returns the solutions whose category unifies with
// target.
func (c *Chart) SolutionsMatching(target term.Term) []*Edge {
	var out []*Edge
	for _, e := range c.Solutions() {
		t := target
		if !t.Ground() {
			t = term.RenamedApart(t, e.Cat)
		}
		if _, _, ok := term.UnifyBindings(e.Cat, t, nil); ok {
			out = append(out, e)
		}
	}
	return out
}

// CountTrees returns the number of distinct trees under the solutions.
// Sub-forests are shared between many parents, so counts are memoised.
func (c *Chart) CountTrees() *big.Int {
	memo := map[*Edge]*big.Int{}
	total := new(big.Int)
	for _, e := range c.Solutions() {
		total.Add(total, c.countTrees(e, memo))
	}
	return total
}

// CountTreesUnder returns the number of distinct trees under e.
func (c *Chart) CountTreesUnder(e *Edge) *big.Int { return c.countTrees(e, map[*Edge]*big.Int{}) }

func (c *Chart) countTrees(e *Edge, memo map[*Edge]*big.Int) *big.Int {
	if n, ok := memo[e]; ok {
		return n
	}
	n := new(big.Int)
	pairs := c.preds[e]
	if len(pairs) == 0 {
		n.SetInt64(1)
	}
	for _, pr := range pairs {
		n.Add(n, new(big.Int).Mul(c.countTrees(pr.Partial, memo), c.countTrees(pr.Complete, memo)))
	}
	memo[e] = n
	return n
}

// Stats summarises a parsed chart.
type Stats struct {
	Length, Solutions, Completes, Partials int
	Trees                                  *big.Int
}

func (c *Chart) Stats() Stats {
	return Stats{
		Length:    len(c.words),
		Solutions: len(c.Solutions()),
		Completes: len(c.Completes()),
		Partials:  len(c.Partials()),
		Trees:     c.CountTrees(),
	}
}

// agenda is a heap of edges ordered by start, then end, then arrival. The
// finished chart does not depend on the order.
type agendaItem struct {
	edge *Edge
	seq  int
}

type agenda []agendaItem

func (a agenda) less(i, j int) bool {
	x, y := a[i], a[j]
	if x.edge.Start != y.edge.Start {
		return x.edge.Start < y.edge.Start
	}
	if x.edge.End != y.edge.End {
		return x.edge.End < y.edge.End
	}
	return x.seq < y.seq
}

// push and pop maintain a binary min-heap (container/heap would box every
// item in an interface).
func (a *agenda) push(x agendaItem) {
	*a = append(*a, x)
	h := *a
	for i := len(h) - 1; i > 0; {
		parent := (i - 1) / 2
		if !h.less(i, parent) {
			break
		}
		h[i], h[parent] = h[parent], h[i]
		i = parent
	}
}

func (a *agenda) pop() agendaItem {
	h := *a
	top := h[0]
	last := len(h) - 1
	h[0] = h[last]
	h[last] = agendaItem{}
	h = h[:last]
	for i := 0; ; {
		l, r, smallest := 2*i+1, 2*i+2, i
		if l < len(h) && h.less(l, smallest) {
			smallest = l
		}
		if r < len(h) && h.less(r, smallest) {
			smallest = r
		}
		if smallest == i {
			break
		}
		h[i], h[smallest] = h[smallest], h[i]
		i = smallest
	}
	*a = h
	return top
}

func itoa(n int) string { return strconv.Itoa(n) }
