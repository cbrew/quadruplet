package frames

import (
	"math"

	"github.com/cbrew/quadruplet/go/cfg"
)

// Layers is the entropy of the distribution over a forest's trees, split by
// where each decision sits with respect to the verbs. A verb node is a
// lexical verb phrase (LexicalVP). In a tree, a top verb node has no verb
// node above it and a bottom one none below it; a lone verb node is both.
//
// The split is a chain rule, the stages settled in turn about a tree:
//
//  1. Outside: the skeleton, everything outside the top verb nodes. It fixes
//     which items are the top verb nodes, so the fact that an item expands
//     by a lexical verb phrase rule at all belongs here.
//  2. Top: each top verb node's expansion, which rule and which daughters,
//     given the skeleton.
//  3. Between: everything from there down to the bottom verb nodes: which
//     items are bottom, the verb nodes in between, and the material beside
//     them, given the above. For a lone verb node, the fact that nothing
//     below it has a verb is charged here.
//  4. Bottom: each bottom verb node's expansion, given the above and that
//     nothing below it has a verb. For a lone verb node the expansion was
//     stage 2, and there is nothing here.
//  5. Inside: the verb-free trees of the bottom verb nodes' daughters.
//
// Each term is the conditional entropy of its stage given the earlier ones,
// so each is at least zero and they sum to Total. Because the forest is
// context-free, given an item the tree under it is independent of the tree
// around it, and every term is a sum over items and hyperedges that four
// linear passes compute (two inside, two outside; the bottom layer needs the
// inside sums split by whether a verb lies below, the top layer the outside
// sums split by whether one lies above). Nothing is enumerated or sampled.
// Units are decimal digits, as cfg.Forest.Entropy uses.
//
// The trees are weighted by the product of their rules' weights: with none,
// every tree is as likely as any other and Total is Count, log10 of the
// number of trees; with a PCFG's rule probabilities, Total is the entropy
// of the PCFG's distribution over the forest's trees, less than Count.
type Layers struct {
	Count   float64 // log10 of the number of trees
	Total   float64 // the entropy of the distribution over them
	Outside float64
	Top     float64
	Between float64
	Bottom  float64
	Inside  float64

	// The expected number of verb nodes, top verb nodes and bottom verb
	// nodes in a tree, and the probability that a tree has no verb node.
	Verbs, TopVerbs, BottomVerbs, VerbFree float64
}

// LexicalRules says which of a grammar's rules are lexical verb phrases'.
func LexicalRules(g *cfg.Grammar) []bool {
	out := make([]bool, len(g.Rules))
	for i, r := range g.Rules {
		out[i] = LexicalVP(r.LHS, r.RHS)
	}
	return out
}

// VerbLayers computes Layers for a forest. verbRule is LexicalRules of its
// grammar; weight, if not nil, is a weight for each rule of the grammar,
// such as its probability given its left-hand side. Sums are floating
// point, and Total is +Inf when they overflow.
func VerbLayers(f *cfg.Forest, verbRule []bool, weight []float64) Layers {
	g := f.G
	n := len(f.Items)
	order := f.Order()
	aux := func(x int32) bool { return g.Aux[f.Items[x].Sym] }
	isVerb := func(h *cfg.Hyperedge) bool {
		return h.Step >= 0 && g.Steps[h.Step].Rule >= 0 && verbRule[g.Steps[h.Step].Rule]
	}
	wt := func(h *cfg.Hyperedge) float64 {
		if weight == nil || h.Step < 0 || g.Steps[h.Step].Rule < 0 {
			return 1
		}
		return weight[g.Steps[h.Step].Rule]
	}
	log := func(x float64) float64 {
		if x <= 0 {
			return 0
		}
		return math.Log10(x)
	}
	xlogx := func(x float64) float64 { return x * log(x) }

	// Bottom up, over the trees under each item: in, their weight; L, their
	// weight times log weight (so that L/in is the expected log weight, and
	// log in - L/in the entropy of the trees under the item); inF and LF,
	// the same over the trees with no verb node. Then, for an auxiliary
	// item, through the chain of its binarization to the daughters of the
	// grammar's own symbols it stands for, over the verb-free trees:
	// bottomCharge, the sum of inF log inF of those daughters, and
	// insideCharge, the sum of their entropies, inF log inF - LF.
	count := make([]float64, n) // the number of trees under each item, unweighted
	in := make([]float64, n)
	L := make([]float64, n)
	inF := make([]float64, n)
	LF := make([]float64, n)
	bottomCharge := make([]float64, n)
	insideCharge := make([]float64, n)
	// tails, for a hyperedge: the product over its tails of a, and the sum
	// over its tails of b times the other tails' a
	prod := func(h *cfg.Hyperedge, a []float64) float64 {
		p := a[h.Left]
		if h.Right >= 0 {
			p *= a[h.Right]
		}
		return p
	}
	cross := func(h *cfg.Hyperedge, a, b []float64) float64 {
		if h.Right < 0 {
			return b[h.Left]
		}
		return b[h.Left]*a[h.Right] + a[h.Left]*b[h.Right]
	}
	for _, x := range order {
		for e, end := f.EdgeRange(x); e < end; e++ {
			h := f.Edge(e)
			if h.Step < 0 {
				count[x]++
				in[x]++
				inF[x]++
				continue
			}
			count[x] += prod(h, count)
			w := wt(h)
			in[x] += w * prod(h, in)
			L[x] += w*prod(h, in)*log(w) + w*cross(h, in, L)
			if !isVerb(h) {
				inF[x] += w * prod(h, inF)
				LF[x] += w*prod(h, inF)*log(w) + w*cross(h, inF, LF)
				if aux(x) {
					bottomCharge[x] += w * cross(h, inF, bottomCharge)
					insideCharge[x] += w * cross(h, inF, insideCharge)
				}
			}
		}
		if !aux(x) {
			bottomCharge[x] = xlogx(inF[x])
			insideCharge[x] = xlogx(inF[x]) - LF[x]
		}
	}
	var trees, total, totalL, totalF float64
	for _, goal := range f.Goals {
		trees += count[goal]
		total += in[goal]
		totalL += L[goal]
		totalF += inF[goal]
	}
	out := Layers{Count: log(trees)}
	if total == 0 || math.IsInf(trees, 0) || math.IsInf(total, 0) || math.IsNaN(totalL) {
		out.Count, out.Total = math.Inf(1), math.Inf(1)
		return out
	}
	out.Total = log(total) - totalL/total
	out.VerbFree = totalF / total

	// Top down: outside, the weight of the ways of building the whole
	// input around an item; outside0, those with no verb node above the
	// item; outside1, for an auxiliary item only, those where the nearest
	// rule above it is a verb node's with no verb node above that: the item
	// is within a top verb node's expansion.
	outside := make([]float64, n)
	outside0 := make([]float64, n)
	outside1 := make([]float64, n)
	for _, goal := range f.Goals {
		outside[goal], outside0[goal] = 1, 1
	}
	for i := n - 1; i >= 0; i-- {
		x := order[i]
		for e, end := f.EdgeRange(x); e < end; e++ {
			h := f.Edge(e)
			if h.Step < 0 {
				continue
			}
			w := wt(h)
			tails := [2]int32{h.Left, h.Right}
			for k, t := range tails {
				if t < 0 {
					continue
				}
				other := w
				if o := tails[1-k]; o >= 0 {
					other *= in[o]
				}
				outside[t] += outside[x] * other
				switch {
				case aux(x):
					outside0[t] += outside0[x] * other
					if aux(t) {
						outside1[t] += outside1[x] * other
					}
				case isVerb(h):
					if aux(t) {
						outside1[t] += outside0[x] * other
					}
				default:
					outside0[t] += outside0[x] * other
				}
			}
		}
	}

	// The terms. A hyperedge's surprisal, log of the weight under its item
	// less log of its own weight under its tails, is the cost of choosing
	// it there; the expected sum over a stage's hyperedges is the stage's
	// conditional entropy.
	for _, x := range order {
		var V float64       // over the verb hyperedges at x: the weight under them
		var Z, W, I float64 // and the verb-free weight under their tails, with the charges
		var top float64     // the verb hyperedges' surprisal, weighted, before the choice of a verb rule is split off
		for e, end := f.EdgeRange(x); e < end; e++ {
			h := f.Edge(e)
			ways := 1.0
			if h.Step >= 0 {
				ways = wt(h) * prod(h, in)
			}
			s := log(in[x]) - log(ways)
			p0 := outside0[x] * ways / total
			switch {
			case aux(x):
				out.Outside += p0 * s
				out.Top += outside1[x] * ways / total * s
			case isVerb(h):
				top += p0 * s
				V += ways
				out.Verbs += outside[x] * ways / total
				w := wt(h)
				waysF := w * prod(h, inF)
				Z += waysF
				W += waysF*log(w) + w*cross(h, inF, bottomCharge)
				I += w * cross(h, inF, insideCharge)
			default:
				out.Outside += p0 * s
			}
		}
		if V > 0 {
			// that x is a top verb node is the skeleton's; which verb rule
			// and daughters is the node's own
			pTop := outside0[x] * V / total
			out.TopVerbs += pTop
			split := log(in[x]) - log(V)
			out.Outside += pTop * split
			out.Top += top - pTop*split
		}
		if Z > 0 {
			out.BottomVerbs += outside[x] * Z / total
			out.Inside += outside[x] * I / total
			// the expansion of a bottom verb node with a verb node above it:
			// each verb hyperedge and chain in proportion to its weight and
			// the verb-free weight under its daughters
			out.Bottom += max(outside[x]-outside0[x], 0) * Z / total * (log(Z) - W/Z)
		}
	}
	out.Between = max(out.Total-out.Outside-out.Top-out.Bottom-out.Inside, 0)
	return out
}

// VerbNodes counts a tree's verb nodes (lexical verb phrases), and how many
// of them are top and bottom ones.
func VerbNodes(t *cfg.Tree) (verbs, top, bottom int) {
	var walk func(t *cfg.Tree, above bool) bool
	walk = func(t *cfg.Tree, above bool) (below bool) {
		if t.Children == nil {
			return false
		}
		rhs := make([]string, len(t.Children))
		for i, k := range t.Children {
			rhs[i] = k.Label
		}
		verb := LexicalVP(t.Label, rhs)
		for _, k := range t.Children {
			if walk(k, above || verb) {
				below = true
			}
		}
		if verb {
			verbs++
			if !above {
				top++
			}
			if !below {
				bottom++
			}
		}
		return below || verb
	}
	walk(t, false)
	return
}
