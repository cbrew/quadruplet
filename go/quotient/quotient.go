// Package quotient maps a parse tree to its class under the equivalence
// that forgets the order in which dependents attach within a head's
// projection: [VP [VP saw her] [PP on Tuesday]] and [VP saw her [PP on
// Tuesday]] are one class, as are two orders of adjunction and a modifier
// attached to any layer of one head's chain. The words and their order are
// fixed, so what a class keeps of a projection is its top category, its
// span, and its dependents in surface order, each with its own class.
//
// A projection is a base phrase with the layers above it. For verbs, the
// base is a lexical verb phrase (frames.LexicalVP), and a layer is a verb
// phrase with exactly one verb phrase daughter and no conjunction:
// adjunction (VP -> VP PP, VP -> ADVP VP), and, if Aux is set, auxiliaries
// (VP -> MD VP). For nouns the base is a noun phrase with no noun phrase
// daughter, and a layer is a noun phrase with exactly one.
//
// Scope-taking modifiers are exempt: the order of attachment is their
// meaning (knocked twice intentionally, knocked intentionally twice). A
// layer or base with a scope-taking daughter (an adverb or adverb phrase
// with a word in Scope) closes a segment: it and what lies below it stay a
// unit, flattened among themselves, and are one dependent of the segment
// above.
package quotient

import (
	"slices"
	"strings"

	"github.com/cbrew/quadruplet/go/cfg"
	fr "github.com/cbrew/quadruplet/go/frames"
)

// Scope is the words whose adverbs take scope: negation, frequency and
// quantificational adverbs, repetitive again, focus particles, and a few
// subject- and speaker-oriented adverbs.
var Scope = map[string]bool{
	"not": true, "n't": true, "never": true, "again": true, "twice": true, "thrice": true,
	"always": true, "often": true, "sometimes": true, "usually": true, "rarely": true, "seldom": true,
	"frequently": true, "occasionally": true, "only": true, "even": true, "just": true, "also": true,
	"still": true, "already": true, "almost": true, "nearly": true, "hardly": true, "barely": true,
	"once": true, "ever": true, "intentionally": true, "deliberately": true, "accidentally": true,
	"probably": true, "possibly": true, "certainly": true, "perhaps": true, "maybe": true,
}

// Options says which projections to flatten.
type Options struct {
	Verbs, Nouns bool
	Aux          bool // verb projections go up through auxiliaries' layers
	NoScope      bool // no exemption for scope-taking modifiers
	// Unlabelled forgets phrases' labels, keeping words' tags. It is not
	// meant to be combined with Verbs or Nouns: which nodes are on a
	// projection depends on their labels, so the combination is not a
	// coarsening of either.
	Unlabelled bool
}

// Key is the tree's class, as a string: equal for trees in one class. The
// tree's words may be spelled word|tag, as the parser has them.
func Key(t *cfg.Tree, o Options) string {
	q := &quot{o: o}
	var b strings.Builder
	q.write(&b, q.index(t, nil, 0))
	return b.String()
}

type node struct {
	t      *cfg.Tree
	kids   []*node
	parent *node
	l, r   int
	head   int  // the daughter that continues the projection down, or -1
	member bool // on a projection: a base or a layer over a member
	scope  bool // has a scope-taking daughter
}

type quot struct{ o Options }

func word(t *cfg.Tree) string {
	w, _, _ := strings.Cut(t.Words[0], "|")
	return strings.ToLower(w)
}

func (q *quot) index(t *cfg.Tree, parent *node, l int) *node {
	n := &node{t: t, parent: parent, l: l, r: l, head: -1}
	if t.Words != nil {
		n.r = l + len(t.Words)
		return n
	}
	for _, c := range t.Children {
		k := q.index(c, n, n.r)
		n.kids = append(n.kids, k)
		n.r = k.r
	}
	rhs := make([]string, len(t.Children))
	for i, c := range t.Children {
		rhs[i] = c.Label
	}
	for _, k := range n.kids {
		if q.scopes(k) {
			n.scope = true
		}
	}
	switch {
	case q.o.Verbs && fr.IsVP(t.Label):
		if fr.LexicalVP(t.Label, rhs) {
			n.member = true
		} else if h := q.layer(n, rhs, "VP"); h >= 0 && n.kids[h].member {
			n.member, n.head = true, h
		}
	case q.o.Nouns && bottom(t.Label) == "NP":
		if h := q.layer(n, rhs, "NP"); h >= 0 {
			if n.kids[h].member {
				n.member, n.head = true, h
			}
		} else if !slices.ContainsFunc(rhs, func(d string) bool { return fr.Chain(d)[0] == "NP" }) {
			n.member = true
		}
	}
	return n
}

func bottom(sym string) string {
	c := fr.Chain(sym)
	return c[len(c)-1]
}

// layer is the index of the one daughter of category cat that n is a layer
// over, or -1 if n is not a layer: it has exactly one such daughter, no
// conjunction, and, for verbs unless Aux, no verb, modal or to.
func (q *quot) layer(n *node, rhs []string, cat string) int {
	h := -1
	for i, d := range rhs {
		switch t := fr.Tag(d); {
		case fr.Chain(d)[0] == cat:
			if h >= 0 {
				return -1
			}
			h = i
		case t == "CC" || t == "CONJP":
			return -1
		case cat == "VP" && !q.o.Aux && (fr.IsVerbTag(d) || t == "MD" || t == "TO"):
			return -1
		}
	}
	return h
}

// scopes says whether a daughter is a scope-taking modifier.
func (q *quot) scopes(n *node) bool {
	if q.o.NoScope {
		return false
	}
	top := fr.Chain(n.t.Label)[0]
	if top != "RB" && top != "ADVP" && top != "RBR" && top != "RBS" {
		return false
	}
	var any bool
	var walk func(n *node)
	walk = func(n *node) {
		if n.t.Words != nil {
			any = any || Scope[word(n.t)]
			return
		}
		for _, k := range n.kids {
			walk(k)
		}
	}
	walk(n)
	return any
}

// top says whether n is the top of a projection: a member whose parent does
// not continue it.
func top(n *node) bool {
	if !n.member {
		return false
	}
	p := n.parent
	return p == nil || !p.member || p.head < 0 || p.kids[p.head] != n
}

func (q *quot) write(b *strings.Builder, n *node) {
	if n.t.Words != nil {
		b.WriteString("(" + n.t.Label + " " + strings.Join(n.t.Words, " ") + ")")
		return
	}
	if top(n) {
		q.segment(b, n)
		return
	}
	b.WriteString("(" + q.label(n))
	for _, k := range n.kids {
		b.WriteByte(' ')
		q.write(b, k)
	}
	b.WriteByte(')')
}

// label is a phrase's label as the class keeps it.
func (q *quot) label(n *node) string {
	if q.o.Unlabelled {
		return "X"
	}
	return n.t.Label
}

// segment writes the projection segment topped by n: its label, its span,
// and the dependents gathered down its head chain, in surface order; a
// head daughter with a scope-taking daughter of its own begins a lower
// segment, written as one dependent.
func (q *quot) segment(b *strings.Builder, n *node) {
	var deps []*node
	var lower *node
	for m := n; ; {
		for i, k := range m.kids {
			if i != m.head {
				deps = append(deps, k)
			}
		}
		if m.head < 0 {
			break
		}
		h := m.kids[m.head]
		if h.scope {
			lower = h
			break
		}
		m = h
	}
	if lower != nil {
		deps = append(deps, lower)
	}
	slices.SortFunc(deps, func(a, c *node) int { return a.l - c.l })
	b.WriteString("[" + q.label(n))
	for _, d := range deps {
		b.WriteByte(' ')
		if d == lower {
			q.segment(b, d)
		} else {
			q.write(b, d)
		}
	}
	b.WriteByte(']')
}
