package cfg

import (
	"math"
	"math/big"
)

// Context says, for Entropy, what the forest's choices are and whose they
// are. A tree makes one choice at each item it uses: which of the item's
// hyperedges builds it. Where an item stands in a tree is summed up by a
// state, from a small automaton run top down: Start at the goal items, and
// Next for each item a hyperedge joins, from the state of the item it builds.
// Groups splits a hyperedge's choice into coarser choices made first: keys,
// coarse to fine, as many for every hyperedge of an item; the choice of the
// hyperedge itself comes last. Class names the kind of choice at an item in
// a state: level i < len(Groups) is the choice of the i-th key given the
// ones before, level len(Groups) that of the hyperedge given them all.
type Context struct {
	Start  int
	Next   func(state int, item Item, e Hyperedge, child Item) int
	Groups func(item Item, e Hyperedge) []string
	Class  func(state int, item Item, level int) string
}

// Entropy splits the entropy of the forest's trees, every tree as likely as
// any other, among the kinds of choice the context names. The entropy of the
// trees, log10 of their number, is the expected sum over a tree's choices of
// each choice's own entropy, given the item and the choices above it (Li and
// Eisner 2009 do the same for weighted hypergraphs):
//
//	log10 T = sum over items x and states s of mu(x, s) H(x)
//
// where mu(x, s) is the expected number of times a tree has x in state s,
// outside(x, s) inside(x) / T, and H(x) is the entropy of the choice among
// x's hyperedges, each as likely as the number of trees under it. With the
// chain rule H(x) splits further by the context's groups. Entropy returns
// the part of log10 T each class has, in decimal digits; with the part
// StartChoice, where the input has several start symbols' goals, the parts
// sum to log10 T. It returns nil for a forest with no trees.
func (f *Forest) Entropy(c Context) map[string]float64 {
	if len(f.Goals) == 0 {
		return nil
	}
	ways := f.ways()
	total := new(big.Int)
	for _, g := range f.Goals {
		total.Add(total, ways[g])
	}
	order := f.Order()

	// outside counts, by state, from the goals down
	outside := make([]map[int]*big.Int, len(f.Items))
	for _, g := range f.Goals {
		outside[g] = map[int]*big.Int{c.Start: big.NewInt(1)}
	}
	other := new(big.Int)
	for i := len(order) - 1; i >= 0; i-- {
		x := order[i]
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

	parts := map[string]float64{}
	totalF := new(big.Float).SetInt(total)
	if len(f.Goals) > 1 { // which start symbol is a choice too
		var ps []float64
		for _, g := range f.Goals {
			v, _ := new(big.Float).Quo(new(big.Float).SetInt(ways[g]), totalF).Float64()
			ps = append(ps, v)
		}
		parts[StartChoice] = entropyOf(ps)
	}
	w := new(big.Int)
	q := new(big.Float)
	ratio := func(a *big.Int, b *big.Float) float64 {
		v, _ := q.Quo(new(big.Float).SetInt(a), b).Float64()
		return v
	}
	for _, x := range order {
		first, end := f.EdgeRange(x)
		if end-first < 2 || len(outside[x]) == 0 {
			continue // no choice
		}
		item := f.Items[x]
		inside := new(big.Float).SetInt(ways[x])
		// each hyperedge's probability, and the groups it falls in
		var probs []float64
		var keys [][]string
		for e := first; e < end; e++ {
			h := *f.Edge(e)
			w.SetInt64(1)
			if h.Left >= 0 {
				w.Mul(w, ways[h.Left])
			}
			if h.Right >= 0 {
				w.Mul(w, ways[h.Right])
			}
			probs = append(probs, ratio(w, inside))
			if c.Groups != nil {
				keys = append(keys, c.Groups(item, h))
			} else {
				keys = append(keys, nil)
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
		wx := new(big.Int)
		for s, o := range outside[x] {
			mu := ratio(wx.Mul(o, ways[x]), totalF)
			for l, h := range hs {
				if h != 0 {
					parts[c.Class(s, item, l)] += mu * h
				}
			}
		}
	}
	return parts
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
