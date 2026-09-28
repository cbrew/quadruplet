package chart

import (
	"runtime"
	"strings"
	"sync"
	"sync/atomic"
)

// ParseParallel fills the chart like Parse, using up to workers goroutines
// at a time for the fundamental rule within a cell (GOMAXPROCS if
// workers <= 0).
//
// The chart is a CYK-style table of cells: cell (i,j) holds the edges
// spanning words i..j. Everything in it is made from shorter cells: lexical
// entries for the words, partial edges over i..k extended by complete edges
// over k..j, and then, within the cell, the edges that complete edges
// spawn. A spawned rule (a zero-length partial edge at i) can only be
// extended, within i..j, by complete edges whose categories would spawn it
// too, so it is combined with those alone. That holds when Spawn returns
// every rule whose first category unifies with the edge, as FeatureGrammar's
// and TreeGrammar's do.
//
// Each cell gets a goroutine, which waits for its left and lower neighbours,
// (i,j-1) and (i+1,j): by induction, once they are finished so are all the
// cells (i,k) and (k,j) it reads. Each finished cell closes its done
// channel, waking the two cells that wait on it. Cells read only finished
// cells, so they need no locks: edges in different cells are never equal,
// so each cell interns its own, and cells with the same start, which share
// that start's spawned edges, are ordered by their left-neighbour
// dependencies. Most of the work falls in the few widest cells, so each
// cell also spreads its fundamental-rule applications over up to workers
// goroutines. Results are added to a cell in the order a sequential build
// would add them, so the chart, including the order of its edges, does not
// depend on how the goroutines are scheduled.
func (c *Chart) ParseParallel(g Grammar, workers int) {
	if workers <= 0 {
		workers = runtime.GOMAXPROCS(0)
	}
	n := len(c.words)
	w := &wave{chart: c, g: g, cells: make([][]*cell, n+1), zeros: make([]*table, n+1)}
	for i := range w.cells {
		w.cells[i] = make([]*cell, n+1)
		w.zeros[i] = newTable()
	}
	if workers == 1 {
		for span := 1; span <= n; span++ {
			for i := 0; i+span <= n; i++ {
				w.build(i, i+span, 1)
			}
		}
		w.assemble()
		return
	}
	done := make([][]chan struct{}, n+1)
	for i := range done {
		done[i] = make([]chan struct{}, n+1)
		for j := i + 1; j <= n; j++ {
			done[i][j] = make(chan struct{})
		}
	}
	var wg sync.WaitGroup
	for i := 0; i < n; i++ {
		for j := i + 1; j <= n; j++ {
			wg.Add(1)
			go func() {
				defer wg.Done()
				if j-i > 1 {
					<-done[i][j-1]
					<-done[i+1][j]
				}
				w.build(i, j, workers)
				close(done[i][j])
			}()
		}
	}
	wg.Wait()
	w.assemble()
}

type wave struct {
	chart *Chart
	g     Grammar
	cells [][]*cell // cells[i][j] spans words i..j
	zeros []*table  // spawned zero-length partial edges, by start
}

type cell struct {
	lexical             bool // the words i..j are in the lexicon
	lexEdges            []*Edge
	completes, partials []*Edge
	edges               *table
	preds               map[*Edge][]Pair
}

// table interns edges: one pointer per distinct edge.
type table struct {
	byHash map[uint64][]*Edge
	list   []*Edge
}

func newTable() *table { return &table{byHash: map[uint64][]*Edge{}} }

func (t *table) intern(e *Edge) (*Edge, bool) {
	for _, o := range t.byHash[e.hash] {
		if o.equal(e) {
			return o, false
		}
	}
	t.byHash[e.hash] = append(t.byHash[e.hash], e)
	t.list = append(t.list, e)
	return e, true
}

// build fills the cell for words i..j, applying the fundamental rule on up to
// helpers goroutines.
func (w *wave) build(i, j, helpers int) {
	cl := &cell{edges: newTable(), preds: map[*Edge][]Pair{}}
	var agenda []*Edge
	add := func(e *Edge, from Pair, hasPred bool) *Edge {
		e, isNew := cl.edges.intern(e)
		if isNew {
			agenda = append(agenda, e)
		}
		if hasPred {
			cl.preds[e] = append(cl.preds[e], from)
		}
		return e
	}
	for _, e := range w.g.Lookup(strings.Join(w.chart.words[i:j], " "), i, j) {
		cl.lexical = true
		cl.lexEdges = append(cl.lexEdges, add(e, Pair{}, false))
	}
	var pairs []Pair
	for k := i + 1; k < j; k++ {
		for _, p := range w.cells[i][k].partials {
			for _, cm := range w.cells[k][j].completes {
				pairs = append(pairs, Pair{p, cm})
			}
		}
	}
	for idx, e := range fundamentals(pairs, helpers) {
		if e != nil {
			add(e, pairs[idx], true)
		}
	}
	for len(agenda) > 0 {
		e := agenda[0]
		agenda = agenda[1:]
		if !e.Complete() {
			cl.partials = append(cl.partials, e)
			continue
		}
		cl.completes = append(cl.completes, e)
		for _, s := range w.g.Spawn(e) {
			z, _ := w.zeros[i].intern(s)
			if e2 := Fundamental(z, e); e2 != nil {
				add(e2, Pair{z, e}, true)
			}
		}
	}
	w.cells[i][j] = cl
}

// assemble gathers the cells into the chart, so that Solutions, CountTrees,
// Trees and the rest work as after Parse.
func (w *wave) assemble() {
	c, n := w.chart, len(w.chart.words)
	for i := 0; i <= n; i++ {
		c.partials[i] = append(c.partials[i], w.zeros[i].list...)
		for j := i + 1; j <= n; j++ {
			cl := w.cells[i][j]
			c.completes[i] = append(c.completes[i], cl.completes...)
			c.partials[j] = append(c.partials[j], cl.partials...)
			for e, pairs := range cl.preds {
				c.preds[e] = pairs
			}
			for _, e := range cl.lexEdges {
				c.lexical[e] = true
			}
		}
	}
	// lexical spans in the order Start finds them
	for j := 0; j < n; j++ {
		if w.cells[j][j+1].lexical {
			c.spans = append(c.spans, Span{c.words[j], j, j + 1})
		}
		for i := 0; i < j; i++ {
			if w.cells[i][j+1].lexical {
				c.spans = append(c.spans, Span{strings.Join(c.words[i:j+1], " "), i, j + 1})
			}
		}
	}
}

// fundamentals applies the fundamental rule to each pair, on up to helpers
// goroutines taking chunks of pairs in turn. result[i] is the edge from
// pairs[i], or nil.
func fundamentals(pairs []Pair, helpers int) []*Edge {
	out := make([]*Edge, len(pairs))
	const chunk = 32
	if helpers <= 1 || len(pairs) < 2*chunk {
		for i, pr := range pairs {
			out[i] = Fundamental(pr.Partial, pr.Complete)
		}
		return out
	}
	var next atomic.Int64
	var wg sync.WaitGroup
	for h := 0; h < helpers; h++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for {
				start := int(next.Add(chunk)) - chunk
				if start >= len(pairs) {
					return
				}
				for i := start; i < min(start+chunk, len(pairs)); i++ {
					out[i] = Fundamental(pairs[i].Partial, pairs[i].Complete)
				}
			}
		}()
	}
	wg.Wait()
	return out
}
