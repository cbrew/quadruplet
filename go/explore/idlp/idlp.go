// Package idlp builds immediate dominance (ID) grammars for verbs'
// projections: a projection licensed by the multiset of its daughters'
// labels, whatever their order. The projections come from the counts
// treebank with flat verb projections (package counts, FlatVerbs): each is
// one node, labelled V:counts, whose daughters are the verb and its
// dependents. An ID rule is that label and the multiset of the daughters'
// labels, in one of two forms:
//
//   - Attested: the whole multiset, modifiers included, as seen.
//   - Free modifiers: only the frame part (the verb and the daughters
//     with nonzero counts, its complements) as seen; any number of
//     modifiers (daughters counting zero, of labels seen as modifiers of
//     verbs) may be added anywhere.
//
// Complements are slots, not a multiset, as in the frames of
// odd_one_out's dep2tiger: a second complement of the same type is a
// second thing and gets a slot of its own, numbered (NP, NP#2, as a, a2
// there), in the order the tree gives them. Modifiers stay a multiset.
// Which complement fills which slot is part of an analysis, so without
// linear precedence a projection with two complements of one type has a
// derivation for each assignment of them to the slots.
//
// For the parser, the ID rules are compiled into ordinary binary rules
// over states that are the sub-multisets of a rule taken so far, the
// daughters being taken in the order the sentence gives them: ID<S + x> ->
// ID<S> x, and V:c -> ID<D> for a rule's whole multiset D. So a projection
// over given daughters has exactly one derivation, and every order of the
// daughters is licensed: the forest of the ID grammar, with no linear
// precedence (LP) constraints at all.
package idlp

import (
	"slices"
	"strconv"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	"github.com/cbrew/quadruplet/go/explore/counts"
)

// Rule is an ID rule: a projection's label and its daughters' labels,
// sorted.
type Rule struct {
	LHS       string
	Daughters []string
}

func (r Rule) key() string { return r.LHS + " -> {" + strings.Join(r.Daughters, " ") + "}" }

// IsProjection says whether a label is a flat verb projection's.
func IsProjection(label string) bool { return strings.HasPrefix(label, "V:") }

// Zero says whether a label counts nothing.
func Zero(label string) bool {
	v, ok := counts.Parse(label)
	return ok && v == counts.Vec{}
}

// verbish says whether a daughter's label is a verb's (the core of its
// projection).
func verbish(label string) bool {
	tag, _, ok := strings.Cut(label, "_")
	return ok && strings.HasPrefix(tag, "VB")
}

// inFrame says which of a projection's daughters are its frame: the verb,
// and the daughters with nonzero counts; or, where there are none (a
// zero-count coordination heading the projection), all of them.
func inFrame(daughters []string) []bool {
	out := make([]bool, len(daughters))
	any := false
	for i, d := range daughters {
		out[i] = verbish(d) || !Zero(d)
		any = any || out[i]
	}
	if !any {
		for i := range out {
			out[i] = true
		}
	}
	return out
}

// Frame is the frame part of a projection's daughters, and the rest, its
// modifiers.
func Frame(daughters []string) (frame, modifiers []string) {
	for i, in := range inFrame(daughters) {
		if in {
			frame = append(frame, daughters[i])
		} else {
			modifiers = append(modifiers, daughters[i])
		}
	}
	return frame, modifiers
}

// Grammar is an ID grammar compiled for the parser.
type Grammar struct {
	Rules     []cfg.Rule      // the compiled rules, and the other rules as they were
	IDRules   map[string]int  // ID rule -> how often it was seen
	Modifiers map[string]bool // with free modifiers: the labels that may be added
	free      bool
	states    map[string]struct{} // the states made
}

// slotted numbers a projection's repeated frame daughters as slots, in
// order: the second NP is NP#2. Modifiers are left as they are.
func slotted(daughters []string) []string {
	frame := inFrame(daughters)
	out := make([]string, len(daughters))
	seen := map[string]int{}
	for i, d := range daughters {
		out[i] = d
		if frame[i] {
			seen[d]++
			if seen[d] > 1 {
				out[i] = d + "#" + strconv.Itoa(seen[d])
			}
		}
	}
	return out
}

// base is the label a slot is filled by: NP#2 -> NP.
func base(slot string) string {
	if i := strings.LastIndex(slot, "#"); i >= 0 {
		return slot[:i]
	}
	return slot
}

// State is the label of a state: a sub-multiset, sorted.
func State(ms []string) string { return "ID<" + strings.Join(ms, " ") + ">" }

// modified is a state's label once a modifier has been taken: with free
// modifiers, a frame of one daughter (the verb alone) completes a
// projection only after a modifier, or it would be the verb over again.
func modified(state string) string { return state + "+m" }

// Compile reads the ID rules off trees with flat verb projections, and
// compiles them, with every other rule of the trees as it is.
func Compile(trees []*cfg.Tree, free bool) *Grammar {
	g := &Grammar{IDRules: map[string]int{}, Modifiers: map[string]bool{}, free: free, states: map[string]struct{}{}}
	other := map[string]cfg.Rule{}
	multisets := map[string][]string{} // key -> the multiset whose sub-multisets are states
	finals := map[string]cfg.Rule{}
	var walk func(t *cfg.Tree)
	walk = func(t *cfg.Tree) {
		if t.Words != nil {
			return
		}
		var ds []string
		for _, k := range t.Children {
			ds = append(ds, k.Label)
			walk(k)
		}
		if !IsProjection(t.Label) {
			r := cfg.Rule{LHS: t.Label, RHS: ds}
			other[r.String()] = r
			return
		}
		d := slotted(ds)
		if free {
			var frame []string
			for i, in := range inFrame(ds) {
				if in {
					frame = append(frame, d[i])
				} else {
					g.Modifiers[ds[i]] = true
				}
			}
			d = frame
		}
		d = slices.Clone(d)
		slices.Sort(d)
		r := Rule{t.Label, d}
		g.IDRules[r.key()]++
		multisets[strings.Join(d, " ")] = d
		if !free || len(d) > 1 {
			f := cfg.Rule{LHS: t.Label, RHS: []string{State(d)}}
			finals[f.String()] = f
		}
		if free {
			f := cfg.Rule{LHS: t.Label, RHS: []string{modified(State(d))}}
			finals[f.String()] = f
		}
	}
	for _, t := range trees {
		walk(t)
	}
	seen := map[string]bool{}
	add := func(r cfg.Rule) {
		if k := r.String(); !seen[k] {
			seen[k] = true
			g.Rules = append(g.Rules, r)
		}
	}
	for _, r := range other {
		add(r)
	}
	for _, r := range finals {
		add(r)
	}
	// every sub-multiset of every rule's multiset is a state; from each, a
	// rule for each daughter still to come
	for _, d := range multisets {
		for _, sub := range subMultisets(d) {
			s := State(sub)
			g.states[s] = struct{}{}
			rest := minus(d, sub)
			for i, x := range rest {
				if i > 0 && rest[i-1] == x {
					continue
				}
				next := slices.Clone(sub)
				next = append(next, x)
				slices.Sort(next)
				if len(sub) == 0 {
					add(cfg.Rule{LHS: State(next), RHS: []string{base(x)}})
					if free {
						add(cfg.Rule{LHS: modified(State(next)), RHS: []string{modified(s), base(x)}})
					}
				} else {
					add(cfg.Rule{LHS: State(next), RHS: []string{s, base(x)}})
					if free {
						add(cfg.Rule{LHS: modified(State(next)), RHS: []string{modified(s), base(x)}})
					}
				}
			}
		}
	}
	if free {
		// modifiers may come anywhere: first, or after any state; a state
		// with a modifier taken is marked, and the empty state is only
		// ever reached by a modifier
		empty := modified(State(nil))
		for m := range g.Modifiers {
			add(cfg.Rule{LHS: empty, RHS: []string{m}})
			for s := range g.states {
				if s == State(nil) {
					add(cfg.Rule{LHS: empty, RHS: []string{empty, m}})
					continue
				}
				add(cfg.Rule{LHS: modified(s), RHS: []string{s, m}})
				add(cfg.Rule{LHS: modified(s), RHS: []string{modified(s), m}})
			}
		}
	}
	return g
}

// States is how many states the compiled grammar has.
func (g *Grammar) States() int { return len(g.states) }

// Derivation is a tree with flat verb projections as the compiled grammar
// derives it: each projection's daughters taken in order through the
// states.
func (g *Grammar) Derivation(t *cfg.Tree) *cfg.Tree {
	if t.Words != nil {
		return t
	}
	kids := make([]*cfg.Tree, len(t.Children))
	for i, k := range t.Children {
		kids[i] = g.Derivation(k)
	}
	if !IsProjection(t.Label) {
		return &cfg.Tree{Label: t.Label, Children: kids}
	}
	labels := make([]string, len(kids))
	for i, k := range kids {
		labels[i] = k.Label
	}
	frame := inFrame(labels)
	slots := slotted(labels)
	var sub []string
	var x *cfg.Tree
	flag := false
	for i, k := range kids {
		mod := g.free && !frame[i]
		if mod {
			flag = true
		} else {
			sub = append(sub, slots[i])
			slices.Sort(sub)
		}
		s := State(sub)
		if flag {
			s = modified(s)
		}
		if x == nil {
			x = &cfg.Tree{Label: s, Children: []*cfg.Tree{k}}
		} else {
			x = &cfg.Tree{Label: s, Children: []*cfg.Tree{x, k}}
		}
	}
	return &cfg.Tree{Label: t.Label, Children: []*cfg.Tree{x}}
}

// subMultisets are all the sub-multisets of a sorted multiset, each sorted.
func subMultisets(d []string) [][]string {
	out := [][]string{nil}
	for i := 0; i < len(d); {
		j := i
		for j < len(d) && d[j] == d[i] {
			j++
		}
		var next [][]string
		for _, s := range out {
			for k := 0; k <= j-i; k++ {
				t := slices.Clone(s)
				for range k {
					t = append(t, d[i])
				}
				next = append(next, t)
			}
		}
		out = next
		i = j
	}
	return out
}

// minus is d less the elements of sub, both sorted.
func minus(d, sub []string) []string {
	var out []string
	j := 0
	for _, x := range d {
		if j < len(sub) && sub[j] == x {
			j++
			continue
		}
		out = append(out, x)
	}
	return out
}
