package interp

import (
	"math"

	"github.com/cbrew/quadruplet/go/cfg"
)

// This file measures every tree of a forest at once: how many trees have
// each number of phrases, how many keep their deepest centre-embedding
// within a bound, and which have the shortest dependencies. Each is a pass
// over the forest's hyperedges, as counting its trees is, carrying more than
// a count. A binarization's auxiliary item stands for a block of some rules'
// daughters, and a block can sit anywhere in a rule, so what an auxiliary
// item carries is what each daughter of it would contribute in each place.

// Phrases is the number of phrases in a tree: its nodes above the words.
func Phrases(n *Node) int {
	return Fold(n, func(*Node) int { return 0 }, func(_ *Node, kids []int) int {
		s := 1
		for _, k := range kids {
			s += k
		}
		return s
	})
}

// CentreDepth is the most centre-embedded any word of the tree is, as a
// left-corner parser's stack grows (Resnik 1992): along the path from the
// root to a word, reading each phrase as branching to the right, the number
// of times the path turns left after going right. A daughter other than the
// first of a phrase of k is reached by going right, and one other than the
// last then goes left.
func CentreDepth(n *Node) int {
	var deepest func(n *Node, right bool, d int) int
	deepest = func(n *Node, right bool, d int) int {
		if n.IsWord() {
			return d
		}
		best, k := d, len(n.Kids)
		for i, c := range n.Kids {
			add, r := move(i, k, right)
			best = max(best, deepest(c, r, d+add))
		}
		return best
	}
	return deepest(n, false, 0)
}

// move is how going to daughter i of a phrase of k, having last gone right
// or not, adds to the depth, and whether the path has then last gone right.
func move(i, k int, right bool) (int, bool) {
	switch {
	case k == 1:
		return 0, right
	case i == 0:
		if right {
			return 1, false
		}
		return 0, false
	case i < k-1:
		return 1, false
	default:
		return 0, true
	}
}

// DependencyLength is the total distance, in words, between each word and its
// head, over the tree's dependencies (Dependencies).
func DependencyLength(n *Node) int {
	total := 0
	for _, d := range Dependencies(n) {
		if d.Head >= 0 {
			total += max(d.Dependent-d.Head, d.Head-d.Dependent)
		}
	}
	return total
}

// forestInfo is what the passes need to know of a forest's grammar.
type forestInfo struct {
	f     *cfg.Forest
	width map[int32]int // auxiliary symbol -> how many daughters it stands for
	heads []int         // rule -> its head daughter
}

func info(f *cfg.Forest) *forestInfo {
	g := f.G
	in := &forestInfo{f: f, width: map[int32]int{}}
	var width func(sym int32) int
	aux := map[int32]cfg.Step{}
	for _, st := range g.Steps {
		if st.Rule < 0 {
			aux[st.Parent] = st
		}
	}
	width = func(sym int32) int {
		st, ok := aux[sym]
		if !ok {
			return 1
		}
		if w, ok := in.width[sym]; ok {
			return w
		}
		w := width(st.Left) + width(st.Right)
		in.width[sym] = w
		return w
	}
	for sym := range aux {
		width(sym)
	}
	parents := map[string]bool{}
	for _, r := range g.Rules {
		parents[r.LHS] = true
	}
	for _, r := range g.Rules {
		in.heads = append(in.heads, RuleHead(r, func(s string) bool { return !parents[s] }))
	}
	return in
}

// RuleHead is the daughter that heads a phrase made by a rule, by the head
// rules FindHeads uses; isWord says which of the rule's symbols are words'
// tags rather than phrases.
func RuleHead(r cfg.Rule, isWord func(string) bool) int {
	chain := categories(r.LHS)
	n := &Node{Cat: chain[0], Chain: chain, Head: -1}
	for _, d := range r.RHS {
		c := categories(d)
		k := &Node{Cat: c[0], Chain: c, Head: -1}
		if !isWord(d) {
			k.Kids = []*Node{}
		}
		n.Kids = append(n.Kids, k)
	}
	h, _ := head(n)
	return h
}

// PhraseCounts is how many trees of the forest have each number of phrases:
// out[p] trees have p. Counts are floating point, for they are astronomical.
func PhraseCounts(f *cfg.Forest) []float64 {
	g := f.G
	dist := make([][]float64, len(f.Items))
	for _, item := range f.Order() {
		var d []float64
		for e, end := f.EdgeRange(item); e < end; e++ {
			h := f.Edge(e)
			c := []float64{1}
			if h.Step >= 0 {
				for _, k := range []int32{h.Left, h.Right} {
					if k >= 0 {
						c = convolve(c, dist[k])
					}
				}
				if !g.Aux[f.Items[item].Sym] {
					c = append([]float64{0}, c...)
				}
			}
			d = addInto(d, c)
		}
		dist[item] = d
	}
	var out []float64
	for _, goal := range f.Goals {
		out = addInto(out, dist[goal])
	}
	return out
}

func convolve(a, b []float64) []float64 {
	out := make([]float64, len(a)+len(b)-1)
	for i, x := range a {
		if x == 0 {
			continue
		}
		for j, y := range b {
			out[i+j] += x * y
		}
	}
	return out
}

func addInto(a, b []float64) []float64 {
	for len(a) < len(b) {
		a = append(a, 0)
	}
	for i, x := range b {
		a[i] += x
	}
	return a
}

// DepthCounts is how many trees of the forest have their most
// centre-embedded word (CentreDepth) at depth at most d, for d from 0 to
// most.
func DepthCounts(f *cfg.Forest, most int) []float64 {
	g := f.G
	B := most + 1
	// within[item][right][b]: trees under a non-auxiliary item whose words are
	// all within b more turns, the path having last gone right or not
	within := make([][2][]float64, len(f.Items))
	// block[item][role][right][b]: the same for an auxiliary item's daughters,
	// the block being at the start of its rule (role&1), at its end (role&2),
	// both, or neither; each daughter's place in the rule follows from that
	block := make([][4][2][]float64, len(f.Items))
	daughter := func(k int32, first, last, right bool, b int) float64 {
		// a non-auxiliary item as one daughter, first or last or between, of a
		// rule with more than one
		i, n := 1, 3
		switch {
		case first:
			i = 0
		case last:
			i = 2
		}
		add, r := move(i, n, right)
		if b-add < 0 {
			return 0
		}
		return within[k][idx(r)][b-add]
	}
	var part func(k int32, start, end, right bool, b int) float64
	part = func(k int32, start, end, right bool, b int) float64 {
		if g.Aux[f.Items[k].Sym] {
			return block[k][role(start, end)][idx(right)][b]
		}
		return daughter(k, start, end, right, b)
	}
	for _, item := range f.Order() {
		aux := g.Aux[f.Items[item].Sym]
		if aux {
			for ro := range 4 {
				for r := range 2 {
					block[item][ro][r] = make([]float64, B)
				}
			}
		} else {
			for r := range 2 {
				within[item][r] = make([]float64, B)
			}
		}
		for e, end := f.EdgeRange(item); e < end; e++ {
			h := f.Edge(e)
			for r := range 2 {
				right := r == 1
				for b := range B {
					switch {
					case h.Step < 0:
						within[item][r][b]++
					case aux:
						for ro := range 4 {
							start, end := ro&1 != 0, ro&2 != 0
							block[item][ro][r][b] += part(h.Left, start, false, right, b) * part(h.Right, false, end, right, b)
						}
					case h.Right < 0 && !g.Aux[f.Items[h.Left].Sym]:
						// a unary rule: no move
						within[item][r][b] += within[h.Left][r][b]
					case h.Right < 0:
						// a rule whose daughters are all one auxiliary block
						within[item][r][b] += block[h.Left][role(true, true)][r][b]
					default:
						within[item][r][b] += part(h.Left, true, false, right, b) * part(h.Right, false, true, right, b)
					}
				}
			}
		}
	}
	out := make([]float64, B)
	for _, goal := range f.Goals {
		for b := range B {
			out[b] += within[goal][0][b]
		}
	}
	return out
}

func idx(right bool) int {
	if right {
		return 1
	}
	return 0
}

func role(start, end bool) int {
	r := 0
	if start {
		r |= 1
	}
	if end {
		r |= 2
	}
	return r
}

// best is a least cost and how many ways reach it.
type best struct {
	cost  int
	count float64
}

var none = best{math.MaxInt, 0}

func (a best) plus(b best) best {
	if a.count == 0 || b.count == 0 {
		return none
	}
	return best{a.cost + b.cost, a.count * b.count}
}

func (a best) shifted(c int) best {
	if a.count == 0 {
		return none
	}
	return best{a.cost + c, a.count}
}

func (a *best) or(b best) {
	switch {
	case b.count == 0:
	case b.cost < a.cost:
		*a = b
	case b.cost == a.cost:
		a.count += b.count
	}
}

// ShortestDependencies is the least total dependency length
// (DependencyLength) of any tree of the forest, and how many trees have it.
//
// In a phrase whose head daughter j heads it from word h, a daughter i left of
// it adds h - h_i, and one right of it h_i - h: the phrase adds
// (left - right)·h plus the sum of -h_i over its left daughters and +h_i over
// its right ones. So a block of daughters is summed once for each side of the
// head it might be on, and once for each daughter that might head the rule,
// with that daughter's head word.
func ShortestDependencies(f *cfg.Forest) (int, float64) {
	in := info(f)
	g := f.G
	// whole[item][h - L]: a non-auxiliary item's trees headed by word h
	whole := make([][]best, len(f.Items))
	// left, right: an item's daughters all left of the rule's head, or all right
	left := make([]best, len(f.Items))
	right := make([]best, len(f.Items))
	// headed[item][m][h - L]: an auxiliary item's daughters with the m'th heading the rule from word h
	headed := make([][][]best, len(f.Items))
	sides := func(k int32) {
		// a non-auxiliary item's summaries as one daughter
		l, r := none, none
		L := int(f.Items[k].L)
		for i, b := range whole[k] {
			l.or(b.shifted(-(L + i)))
			r.or(b.shifted(L + i))
		}
		left[k], right[k] = l, r
	}
	// headedAt is the item's daughters with its m'th heading from word h
	headedAt := func(k int32, m, h int) best {
		it := f.Items[k]
		if h < int(it.L) || h >= int(it.R) {
			return none
		}
		if g.Aux[it.Sym] {
			return headed[k][m][h-int(it.L)]
		}
		if m != 0 {
			return none
		}
		return whole[k][h-int(it.L)]
	}
	widthOf := func(k int32) int {
		if w, ok := in.width[f.Items[k].Sym]; ok {
			return w
		}
		return 1
	}
	// block is a pair of items as one block: its daughters with the m'th heading from h
	block := func(a, b int32, m, h int) best {
		if wa := widthOf(a); m < wa {
			return headedAt(a, m, h).plus(right[b])
		} else {
			return left[a].plus(headedAt(b, m-wa, h))
		}
	}
	for _, item := range f.Order() {
		it := f.Items[item]
		L, n := int(it.L), int(it.R-it.L)
		aux := g.Aux[it.Sym]
		if aux {
			w := in.width[it.Sym]
			headed[item] = make([][]best, w)
			for m := range headed[item] {
				headed[item][m] = make([]best, n)
				for i := range n {
					headed[item][m][i] = none
				}
			}
			left[item], right[item] = none, none
		} else {
			whole[item] = make([]best, n)
			for i := range n {
				whole[item][i] = none
			}
		}
		for e, end := f.EdgeRange(item); e < end; e++ {
			h := f.Edge(e)
			switch {
			case h.Step < 0:
				whole[item][0].or(best{0, 1})
			case aux:
				left[item].or(left[h.Left].plus(left[h.Right]))
				right[item].or(right[h.Left].plus(right[h.Right]))
				for m := range headed[item] {
					for i := range n {
						headed[item][m][i].or(block(h.Left, h.Right, m, L+i))
					}
				}
			default:
				st := g.Steps[h.Step]
				rule := g.Rules[st.Rule]
				k, j := len(rule.RHS), in.heads[st.Rule]
				shift := j - (k - 1 - j) // (left - right) daughters of the head
				for i := range n {
					var b best
					switch {
					case h.Right >= 0:
						b = block(h.Left, h.Right, j, L+i)
					default:
						b = headedAt(h.Left, j, L+i)
					}
					whole[item][i].or(b.shifted(shift * (L + i)))
				}
			}
		}
		if !aux {
			sides(item)
		}
	}
	result := none
	for _, goal := range f.Goals {
		for _, b := range whole[goal] {
			result.or(b)
		}
	}
	return result.cost, result.count
}
