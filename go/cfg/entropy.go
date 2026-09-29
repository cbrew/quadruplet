package cfg

import (
	"math"
	"math/big"
)

// Context says, for Entropy, how likely the forest's trees are, what their
// choices are and whose they are. A tree makes one choice at each item it uses: which of the item's
// hyperedges builds it. Where an item stands in a tree is summed up by a
// state, from a small automaton run top down: Start at the goal items, and
// Next for each item a hyperedge joins, from the state of the item it builds.
// Groups splits a hyperedge's choice into coarser choices made first: keys,
// coarse to fine, as many for every hyperedge of an item; the choice of the
// hyperedge itself comes last. Class names the kind of choice at an item in
// a state: level i < len(Groups) is the choice of the i-th key given the
// ones before, level len(Groups) that of the hyperedge given them all.
type Context struct {
	// Weight, if not nil, weighs the trees: a tree is as likely as the
	// product of its hyperedges' weights, each given with the item it builds
	// (a probabilistic grammar puts the rule's probability on the step that
	// completes it, 1 on the others, and a word's probability given its tag
	// on the lexical hyperedge). If nil, every tree is as likely as any
	// other, and the counts are exact.
	Weight func(item Item, e Hyperedge) float64

	Start  int
	Next   func(state int, item Item, e Hyperedge, child Item) int
	Groups func(item Item, e Hyperedge) []string
	Class  func(state int, item Item, level int) string
}

// Entropy splits the entropy of the forest's trees, every tree as likely as
// any other or as the context weighs it, among the kinds of choice the
// context names. The entropy of the trees (log10 of their number, if they
// are equally likely) is the expected sum over a tree's choices of each
// choice's own entropy, given the item and the choices above it (Li and
// Eisner 2009 do the same for weighted hypergraphs):
//
//	log10 T = sum over items x and states s of mu(x, s) H(x)
//
// where mu(x, s) is the expected number of times a tree has x in state s,
// outside(x, s) inside(x) / T, and H(x) is the entropy of the choice among
// x's hyperedges, each as likely as the trees under it together. With the
// chain rule H(x) splits further by the context's groups. Entropy returns
// the part of the entropy each class has, in decimal digits; with the part
// StartChoice, where the input has several start symbols' goals, the parts
// sum to the entropy. It returns nil for a forest with no trees.
func (f *Forest) Entropy(c Context) map[string]float64 {
	if len(f.Goals) == 0 {
		return nil
	}
	m := f.measure(c)
	parts := map[string]float64{}
	if len(f.Goals) > 1 { // which start symbol is a choice too
		parts[StartChoice] = entropyOf(m.goals)
	}
	for _, x := range m.order {
		first, end := f.EdgeRange(x)
		if end-first < 2 || len(m.mu[x]) == 0 {
			continue // no choice
		}
		item := f.Items[x]
		probs := m.probs(x)
		// the groups each hyperedge falls in
		keys := make([][]string, end-first)
		if c.Groups != nil {
			for e := first; e < end; e++ {
				keys[e-first] = c.Groups(item, *f.Edge(e))
			}
		}
		// the entropy of each level of choice, by the chain rule
		levels := len(keys[0])
		prev := 0.0
		hs := make([]float64, levels+1)
		for l := 0; l <= levels; l++ {
			var hl float64
			if l == levels {
				hl = entropyOf(probs)
			} else {
				byPrefix := map[string]float64{}
				for i, p := range probs {
					prefix := ""
					for _, k := range keys[i][:l+1] {
						prefix += k + "\x00"
					}
					byPrefix[prefix] += p
				}
				var ps []float64
				for _, p := range byPrefix {
					ps = append(ps, p)
				}
				hl = entropyOf(ps)
			}
			hs[l] = hl - prev
			prev = hl
		}
		// weighted by how often the item is in each state
		for s, mu := range m.mu[x] {
			for l, h := range hs {
				if h != 0 {
					parts[c.Class(s, item, l)] += mu * h
				}
			}
		}
	}
	return parts
}

// measure is what Entropy and Occupancy need of a forest under a context:
// the items in bottom-up order; for each item, the expected number of times
// a tree has it in each state; the probability of each hyperedge given its
// item; and of each goal.
type measure struct {
	order []int32
	mu    []map[int]float64
	probs func(item int32) []float64
	goals []float64
}

// measure computes inside and outside values, from counts (exactly, with
// big integers) or, where the context weighs hyperedges, from weights.
func (f *Forest) measure(c Context) measure {
	if c.Weight != nil {
		return f.weighted(c)
	}
	ways := f.ways()
	total := new(big.Int)
	for _, g := range f.Goals {
		total.Add(total, ways[g])
	}
	m := measure{order: f.Order(), mu: make([]map[int]float64, len(f.Items))}
	totalF := new(big.Float).SetInt(total)
	ratio := func(a *big.Int, b *big.Float) float64 {
		v, _ := new(big.Float).Quo(new(big.Float).SetInt(a), b).Float64()
		return v
	}
	for _, g := range f.Goals {
		m.goals = append(m.goals, ratio(ways[g], totalF))
	}
	// outside counts, by state, from the goals down
	outside := make([]map[int]*big.Int, len(f.Items))
	for _, g := range f.Goals {
		outside[g] = map[int]*big.Int{c.Start: big.NewInt(1)}
	}
	other := new(big.Int)
	for i := len(m.order) - 1; i >= 0; i-- {
		x := m.order[i]
		for s, o := range outside[x] {
			for e, end := f.EdgeRange(x); e < end; e++ {
				h := *f.Edge(e)
				if h.Left < 0 {
					continue
				}
				kids := []int32{h.Left}
				if h.Right >= 0 {
					kids = append(kids, h.Right)
				}
				for k, kid := range kids {
					other.Set(o)
					if len(kids) == 2 {
						other.Mul(other, ways[kids[1-k]])
					}
					ns := c.Next(s, f.Items[x], h, f.Items[kid])
					if outside[kid] == nil {
						outside[kid] = map[int]*big.Int{}
					}
					if outside[kid][ns] == nil {
						outside[kid][ns] = new(big.Int)
					}
					outside[kid][ns].Add(outside[kid][ns], other)
				}
			}
		}
	}
	w := new(big.Int)
	for x, os := range outside {
		if len(os) == 0 {
			continue
		}
		m.mu[x] = map[int]float64{}
		for s, o := range os {
			m.mu[x][s] = ratio(w.Mul(o, ways[x]), totalF)
		}
	}
	m.probs = func(x int32) []float64 {
		first, end := f.EdgeRange(x)
		inside := new(big.Float).SetInt(ways[x])
		var out []float64
		for e := first; e < end; e++ {
			h := f.Edge(e)
			w.SetInt64(1)
			if h.Left >= 0 {
				w.Mul(w, ways[h.Left])
			}
			if h.Right >= 0 {
				w.Mul(w, ways[h.Right])
			}
			out = append(out, ratio(w, inside))
		}
		return out
	}
	return m
}

// weighted is measure with the context's weights, in floating point: fine
// for the inside probabilities of sentences of tens of words, which stay far
// above the smallest float64.
func (f *Forest) weighted(c Context) measure {
	m := measure{order: f.Order(), mu: make([]map[int]float64, len(f.Items))}
	inside := make([]float64, len(f.Items))
	edge := func(x int32, h Hyperedge) float64 {
		v := c.Weight(f.Items[x], h)
		if h.Left >= 0 {
			v *= inside[h.Left]
		}
		if h.Right >= 0 {
			v *= inside[h.Right]
		}
		return v
	}
	for _, x := range m.order {
		for e, end := f.EdgeRange(x); e < end; e++ {
			inside[x] += edge(x, *f.Edge(e))
		}
	}
	total := 0.0
	for _, g := range f.Goals {
		total += inside[g]
	}
	for _, g := range f.Goals {
		m.goals = append(m.goals, inside[g]/total)
	}
	outside := make([]map[int]float64, len(f.Items))
	for _, g := range f.Goals {
		outside[g] = map[int]float64{c.Start: 1}
	}
	for i := len(m.order) - 1; i >= 0; i-- {
		x := m.order[i]
		for s, o := range outside[x] {
			for e, end := f.EdgeRange(x); e < end; e++ {
				h := *f.Edge(e)
				if h.Left < 0 {
					continue
				}
				kids := []int32{h.Left}
				if h.Right >= 0 {
					kids = append(kids, h.Right)
				}
				for k, kid := range kids {
					v := o * c.Weight(f.Items[x], h)
					if len(kids) == 2 {
						v *= inside[kids[1-k]]
					}
					ns := c.Next(s, f.Items[x], h, f.Items[kid])
					if outside[kid] == nil {
						outside[kid] = map[int]float64{}
					}
					outside[kid][ns] += v
				}
			}
		}
	}
	for x, os := range outside {
		if len(os) == 0 {
			continue
		}
		m.mu[x] = map[int]float64{}
		for s, o := range os {
			m.mu[x][s] = o * inside[x] / total
		}
	}
	m.probs = func(x int32) []float64 {
		first, end := f.EdgeRange(x)
		var out []float64
		for e := first; e < end; e++ {
			out = append(out, edge(x, *f.Edge(e))/inside[x])
		}
		return out
	}
	return m
}

// Occupancy is the expected number of times a tree has an item of each
// class: the sum over items x and states s of the expected number of times a
// tree has x in state s, by class(s, x). It returns nil for a forest with no
// trees.
func (f *Forest) Occupancy(c Context, class func(state int, item Item) string) map[string]float64 {
	if len(f.Goals) == 0 {
		return nil
	}
	m := f.measure(c)
	out := map[string]float64{}
	for x, ss := range m.mu {
		for s, mu := range ss {
			out[class(s, f.Items[x])] += mu
		}
	}
	return out
}

// StartChoice is the class of Entropy's part for the choice of start
// symbol, where the input has more than one.
const StartChoice = "(start symbol)"

// entropyOf is the entropy of a distribution, in decimal digits.
func entropyOf(ps []float64) float64 {
	h := 0.0
	for _, p := range ps {
		if p > 0 {
			h -= p * math.Log10(p)
		}
	}
	return h
}
