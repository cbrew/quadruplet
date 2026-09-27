// Prototype Go port of quadruplet's chart parser core, specialised to
// ground (variable-free) categories so we can benchmark scheduling
// strategies: the original agenda loop vs. a span-length wavefront, and
// the wavefront run sequentially vs. with goroutines.
package main

import (
	"math/big"
	"runtime"
	"sync"
)

// ---------------------------------------------------------------- grammar

type Cat int32 // interned category (hash-consed feature structure)

type Rule struct {
	LHS Cat
	RHS []Cat
}

// Needed lists are hash-consed cons cells, so a partial is identified by
// (LHS, start, end, remaining-needed) exactly as in the Kotlin data class,
// and dotted rules sharing a suffix collapse into one edge.
type Need int32 // -1 = empty list

type cons struct {
	First Cat
	Rest  Need
}

type Grammar struct {
	Rules      []Rule
	RuleNeed   []Need
	LeftCorner map[Cat][]int32 // rules indexed by first RHS category
	Lexicon    map[string][]Cat
	Names      []string
	conses     []cons
	consIndex  map[cons]Need
}

func (g *Grammar) list(cats []Cat) Need {
	if len(cats) == 0 {
		return -1
	}
	c := cons{cats[0], g.list(cats[1:])}
	if id, ok := g.consIndex[c]; ok {
		return id
	}
	id := Need(len(g.conses))
	g.conses = append(g.conses, c)
	g.consIndex[c] = id
	return id
}

// work simulates the cost of a real feature-structure unification
// (the Kotlin unify builds maps, substitutes and canonicalises).
var work int

var sink uint64

func burn() {
	x := uint64(1)
	for i := 0; i < work; i++ {
		x = x*6364136223846793005 + 1442695040888963407
	}
	if x == 42 {
		sink++
	}
}

// unify for ground categories is identity; burn() stands in for the rest.
func unify(a, b Cat) bool {
	burn()
	return a == b
}

// TreeAsFeatureGrammar from quadruplet: S[f=odd]/S[f=even] over "a"s.
func treeGrammar() *Grammar {
	const odd, even Cat = 0, 1
	rules := []Rule{
		{odd, []Cat{odd, odd, odd}}, {odd, []Cat{odd, even, even}},
		{even, []Cat{odd, even, odd}}, {even, []Cat{odd, odd, even}},
		{odd, []Cat{odd, even}}, {even, []Cat{odd, odd}},
		{even, []Cat{even, even}}, {odd, []Cat{even, odd}},
		{odd, []Cat{even, odd, even}}, {odd, []Cat{even, even, odd}},
		{even, []Cat{even, even, even}}, {even, []Cat{even, odd, odd}},
	}
	g := &Grammar{Rules: rules, LeftCorner: map[Cat][]int32{},
		Lexicon: map[string][]Cat{"a": {odd}},
		Names:   []string{"S[f=odd]", "S[f=even]"}, consIndex: map[cons]Need{}}
	for i, r := range rules {
		g.LeftCorner[r.RHS[0]] = append(g.LeftCorner[r.RHS[0]], int32(i))
		g.RuleNeed = append(g.RuleNeed, g.list(r.RHS))
	}
	return g
}

// ---------------------------------------------------------------- edges

// Edge is a comparable value type, so it can key a Go map directly.
// Need < 0 means complete.
type Edge struct {
	Cat        Cat
	Need       Need
	Start, End int32
}

func (e Edge) Complete() bool { return e.Need < 0 }

type BP struct{ P, C Edge } // backpointer: partial + complete

func fundamental(g *Grammar, p, c Edge) (Edge, bool) {
	n := g.conses[p.Need]
	if !unify(n.First, c.Cat) {
		return Edge{}, false
	}
	return Edge{Cat: p.Cat, Need: n.Rest, Start: p.Start, End: c.End}, true
}

func spawn(g *Grammar, c Edge) []Edge {
	var out []Edge
	for _, ri := range g.LeftCorner[c.Cat] {
		burn() // Kotlin's spawn also unifies against firstNeeded
		out = append(out, Edge{Cat: g.Rules[ri].LHS, Need: g.RuleNeed[ri], Start: c.Start, End: c.Start})
	}
	return out
}

// ------------------------------------------------- 1. agenda (faithful)

type AgendaChart struct {
	completes [][]Edge // by start
	partials  [][]Edge // by end
	seen      map[Edge]struct{}
	preds     map[Edge][]BP
}

func ParseAgenda(g *Grammar, words []string, parPair bool) *AgendaChart {
	n := len(words)
	ch := &AgendaChart{completes: make([][]Edge, n+1), partials: make([][]Edge, n+1),
		seen: map[Edge]struct{}{}, preds: map[Edge][]BP{}}
	var agenda []Edge
	for j, w := range words {
		for _, c := range g.Lexicon[w] {
			agenda = append(agenda, Edge{Cat: c, Need: -1, Start: int32(j), End: int32(j + 1)})
		}
	}
	for len(agenda) > 0 {
		e := agenda[len(agenda)-1]
		agenda = agenda[:len(agenda)-1]
		if _, dup := ch.seen[e]; dup {
			continue
		}
		ch.seen[e] = struct{}{}
		var pairs []BP
		if e.Complete() {
			ch.completes[e.Start] = append(ch.completes[e.Start], e)
			agenda = append(agenda, spawn(g, e)...)
			for _, p := range ch.partials[e.Start] {
				pairs = append(pairs, BP{p, e})
			}
		} else {
			ch.partials[e.End] = append(ch.partials[e.End], e)
			for _, c := range ch.completes[e.End] {
				pairs = append(pairs, BP{e, c})
			}
		}
		for _, r := range pairAll(g, pairs, parPair) {
			ch.preds[r.e] = append(ch.preds[r.e], r.bp)
			agenda = append(agenda, r.e)
		}
	}
	return ch
}

type result struct {
	e  Edge
	bp BP
}

// pairAll applies the fundamental rule to each pair, optionally fanning
// out across goroutines (the "fine-grained" parallelisation).
func pairAll(g *Grammar, pairs []BP, par bool) []result {
	if !par || len(pairs) < 2 {
		var out []result
		for _, bp := range pairs {
			if e, ok := fundamental(g, bp.P, bp.C); ok {
				out = append(out, result{e, bp})
			}
		}
		return out
	}
	w := min(runtime.GOMAXPROCS(0), len(pairs))
	chunks := make([][]result, w)
	var wg sync.WaitGroup
	for k := 0; k < w; k++ {
		wg.Add(1)
		go func(k int) {
			defer wg.Done()
			for i := k; i < len(pairs); i += w {
				if e, ok := fundamental(g, pairs[i].P, pairs[i].C); ok {
					chunks[k] = append(chunks[k], result{e, pairs[i]})
				}
			}
		}(k)
	}
	wg.Wait()
	var out []result
	for _, c := range chunks {
		out = append(out, c...)
	}
	return out
}

// ------------------------------------------ 2. span-length wavefront

// Cell holds every edge spanning (i,j), i<j, plus its backpointers.
// Once a level is finished its cells are immutable, so the next level
// reads them without locks.
type Cell struct {
	completes, partials []Edge
	seen                map[Edge]struct{}
	preds               map[Edge][]BP
}

type WaveChart struct {
	n     int
	cells [][]*Cell           // cells[i][j]
	zero  []map[Edge]struct{} // zero-length (freshly spawned) partials at i
}

func (w *WaveChart) cell(i, j int32) *Cell { return w.cells[i][j] }

func buildCell(g *Grammar, w *WaveChart, words []string, i, j int) {
	c := &Cell{seen: map[Edge]struct{}{}, preds: map[Edge][]BP{}}
	w.cells[i][j] = c
	var agenda []Edge
	if j == i+1 {
		for _, cat := range g.Lexicon[words[i]] {
			agenda = append(agenda, Edge{Cat: cat, Need: -1, Start: int32(i), End: int32(j)})
		}
	}
	// (multi-word lexical lookup for words[i:j] would go here)
	emit := func(e Edge, bp BP) {
		c.preds[e] = append(c.preds[e], bp)
		agenda = append(agenda, e)
	}
	for k := i + 1; k < j; k++ {
		for _, p := range w.cells[i][k].partials {
			for _, cc := range w.cells[k][j].completes {
				if e, ok := fundamental(g, p, cc); ok {
					emit(e, BP{p, cc})
				}
			}
		}
	}
	// Closure within the cell: unary/left-corner chains.  A partial
	// spawned at i by a complete (i,j) can only combine, within this
	// span, with completes whose category would spawn it too, so this
	// is cell-local.  zero[i] is touched by exactly one goroutine per
	// level (cells on a level have distinct i).
	for len(agenda) > 0 {
		e := agenda[len(agenda)-1]
		agenda = agenda[:len(agenda)-1]
		if _, dup := c.seen[e]; dup {
			continue
		}
		c.seen[e] = struct{}{}
		if !e.Complete() {
			c.partials = append(c.partials, e)
			continue
		}
		c.completes = append(c.completes, e)
		for _, z := range spawn(g, e) {
			w.zero[i][z] = struct{}{}
			if ne, ok := fundamental(g, z, e); ok {
				emit(ne, BP{z, e})
			}
		}
	}
}

func ParseWave(g *Grammar, words []string, parallel bool) *WaveChart {
	n := len(words)
	w := &WaveChart{n: n, cells: make([][]*Cell, n+1), zero: make([]map[Edge]struct{}, n+1)}
	for i := range w.cells {
		w.cells[i] = make([]*Cell, n+1)
		w.zero[i] = map[Edge]struct{}{}
	}
	for span := 1; span <= n; span++ {
		if !parallel {
			for i := 0; i+span <= n; i++ {
				buildCell(g, w, words, i, i+span)
			}
			continue
		}
		var wg sync.WaitGroup
		for i := 0; i+span <= n; i++ {
			wg.Add(1)
			go func(i int) { defer wg.Done(); buildCell(g, w, words, i, i+span) }(i)
		}
		wg.Wait() // barrier: level `span` is now immutable
	}
	return w
}

// ------------------------------------------------ stats & tree counting

type preds func(Edge) []BP

// countTrees is memoised; the Kotlin version recomputes shared
// sub-forests, which is why counting explodes long before parsing does.
func countTrees(roots []Edge, pr preds) *big.Int {
	memo := map[Edge]*big.Int{}
	var count func(Edge) *big.Int
	count = func(e Edge) *big.Int {
		if v, ok := memo[e]; ok {
			return v
		}
		bps := pr(e)
		v := new(big.Int)
		if len(bps) == 0 {
			v.SetInt64(1)
		}
		for _, bp := range bps {
			v.Add(v, new(big.Int).Mul(count(bp.P), count(bp.C)))
		}
		memo[e] = v
		return v
	}
	total := new(big.Int)
	for _, r := range roots {
		total.Add(total, count(r))
	}
	return total
}

type Stats struct {
	completes, partials int
	trees               *big.Int
}

func (a *AgendaChart) Stats(n int) Stats {
	s := Stats{}
	var roots []Edge
	for _, c := range a.completes {
		s.completes += len(c)
	}
	for _, p := range a.partials {
		s.partials += len(p)
	}
	for _, c := range a.completes[0] {
		if int(c.End) == n {
			roots = append(roots, c)
		}
	}
	s.trees = countTrees(roots, func(e Edge) []BP { return a.preds[e] })
	return s
}

func (w *WaveChart) Stats() Stats {
	s := Stats{}
	for i := 0; i <= w.n; i++ {
		s.partials += len(w.zero[i])
		for j := i + 1; j <= w.n; j++ {
			s.completes += len(w.cells[i][j].completes)
			s.partials += len(w.cells[i][j].partials)
		}
	}
	roots := w.cells[0][w.n].completes
	s.trees = countTrees(roots, func(e Edge) []BP {
		if e.Start == e.End {
			return nil
		}
		return w.cells[e.Start][e.End].preds[e]
	})
	return s
}
