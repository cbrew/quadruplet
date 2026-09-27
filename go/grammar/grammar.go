// Package grammar holds feature grammars: rules over feature maps and a
// lexicon from words (or multi-word phrases) to categories.
package grammar

import (
	"strings"

	"github.com/cbrew/quadruplet/go/term"
)

// Rule is a context-free rule LHS -> RHS, or, when Linseq is non-nil, a
// multiple context-free rule LHS => RHS : Linseq. Words are the quoted words
// on the right-hand side in the IntegratedParser notation; they also go
// into the lexicon.
type Rule struct {
	LHS    *term.Map
	RHS    []*term.Map
	Words  []string
	Linseq *term.List
}

// String prints lhs -> rhs... or lhs => rhs...: <linseq...>, as the Kotlin
// version does.
func (r *Rule) String() string {
	var b strings.Builder
	b.WriteString(r.LHS.String())
	if r.Linseq == nil {
		b.WriteString(" -> ")
	} else {
		b.WriteString(" => ")
	}
	for i, m := range r.RHS {
		if i > 0 {
			b.WriteByte(' ')
		}
		b.WriteString(m.String())
	}
	if r.Linseq != nil {
		b.WriteString(": <")
		for i, e := range r.Linseq.Elems() {
			if i > 0 {
				b.WriteString(", ")
			}
			b.WriteString(e.String())
		}
		b.WriteByte('>')
	}
	return b.String()
}

// Normalized rebuilds the rule's categories with term.Normalized.
func (r *Rule) Normalized() *Rule {
	rhs := make([]*term.Map, len(r.RHS))
	for i, m := range r.RHS {
		rhs[i] = term.Normalized(m).(*term.Map)
	}
	return &Rule{LHS: term.Normalized(r.LHS).(*term.Map), RHS: rhs, Words: r.Words, Linseq: r.Linseq}
}

// Equal reports whether two rules are the same.
func (r *Rule) Equal(o *Rule) bool {
	if !term.Equal(r.LHS, o.LHS) || len(r.RHS) != len(o.RHS) || len(r.Words) != len(o.Words) {
		return false
	}
	for i := range r.RHS {
		if !term.Equal(r.RHS[i], o.RHS[i]) {
			return false
		}
	}
	for i := range r.Words {
		if r.Words[i] != o.Words[i] {
			return false
		}
	}
	if (r.Linseq == nil) != (o.Linseq == nil) {
		return false
	}
	return r.Linseq == nil || term.Equal(r.Linseq, o.Linseq)
}

// Grammar is a set of rules, in the order first given, and a lexicon.
type Grammar struct {
	Rules   []*Rule
	Lexicon *Lexicon
}

func New() *Grammar { return &Grammar{Lexicon: NewLexicon()} }

// AddRule adds r unless an equal rule is already present.
func (g *Grammar) AddRule(r *Rule) {
	for _, o := range g.Rules {
		if o.Equal(r) {
			return
		}
	}
	g.Rules = append(g.Rules, r)
}

// Lexicon maps words, and phrases of several space-separated words, to their
// categories, keeping words and categories in the order first added.
type Lexicon struct {
	words   []string
	entries map[string][]*term.Map
}

func NewLexicon() *Lexicon { return &Lexicon{entries: map[string][]*term.Map{}} }

// Add files cat under word, unless an equal category is already there.
func (l *Lexicon) Add(word string, cat *term.Map) {
	cats, seen := l.entries[word]
	if !seen {
		l.words = append(l.words, word)
	}
	for _, c := range cats {
		if term.Equal(c, cat) {
			return
		}
	}
	l.entries[word] = append(cats, cat)
}

// Words returns the words in order of first entry.
func (l *Lexicon) Words() []string { return l.words }

// Lookup returns the categories of a word or phrase.
func (l *Lexicon) Lookup(word string) []*term.Map { return l.entries[word] }
