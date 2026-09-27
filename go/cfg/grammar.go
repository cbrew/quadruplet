// Package cfg is a fast parser for context-free grammars: grammars whose
// categories are plain symbols, with no features to unify. It builds the
// same packed forest as package chart would, holding only the items that lie
// on a derivation of the whole input, in flat memory over integer symbols.
//
// The method follows the LCFRS parser in cbrew/odd_one_out, specialised to
// context-free rules. Rules of more than two daughters are binarized left
// to right, with prefixes shared between rules, so the parser only ever
// joins two items. A first pass recognises, for every span, the set of
// symbols derivable over it, as bitsets (CKY). A second pass goes top down
// from the goal and keeps an item only if it is derivable and on a
// derivation of the whole input, recording, as it goes, every way of
// building it. No item that could not be part of a parse is ever stored.
package cfg

import (
	"errors"
	"fmt"
	"slices"
	"strings"
)

// Rule is a context-free rule, LHS -> RHS, with at least one daughter.
type Rule struct {
	LHS string
	RHS []string
}

func (r Rule) String() string { return r.LHS + " -> " + strings.Join(r.RHS, " ") }

// Step is a rule of the binarized grammar: Parent -> Left Right, or
// Parent -> Left when Right is -1. Rule is the index of the grammar rule the
// step completes, or -1 for a step that builds a prefix of rules.
type Step struct {
	Parent, Left, Right int32
	Rule                int32
}

// Grammar is a context-free grammar compiled for parsing.
type Grammar struct {
	Names  []string // symbol -> name; binarization prefixes are named |A B ...
	Prefix []bool   // symbol -> it is a prefix made by binarization
	Starts []int32  // the start symbols
	Rules  []Rule   // the grammar's rules, without duplicates
	Steps  []Step

	number    map[string]int32
	lexicon   map[string][]int32 // word or phrase -> symbols
	maxPhrase int                // the most words in a lexical phrase
	unaryUp   [][]int32          // child -> unary steps over it
	unaryDown [][]int32          // parent -> unary steps building it
	binUp     [][]up             // left -> (right, step), sorted by right
	binDown   [][]down           // parent -> lefts, sorted, each with its (right, step)s
	rank      []int32            // symbol -> above all its unary descendants
}

type up struct{ right, step int32 }

type down struct {
	left  int32
	steps []up // (right, step)
}

// New compiles a grammar. lexicon maps a word, or a phrase of words
// separated by single spaces, to the symbols it can be. starts are the
// symbols a whole input may be.
func New(rules []Rule, lexicon map[string][]string, starts []string) (*Grammar, error) {
	g := &Grammar{number: map[string]int32{}, lexicon: map[string][]int32{}}
	seen := map[string]bool{}
	for _, r := range rules {
		if len(r.RHS) == 0 {
			return nil, fmt.Errorf("cfg: %s: rules with no daughters are not supported", r)
		}
		key := r.String()
		if seen[key] {
			continue
		}
		seen[key] = true
		g.Rules = append(g.Rules, r)
	}
	steps := map[[3]int32]int32{} // prefix steps, shared
	for ri, r := range g.Rules {
		lhs := g.symbol(r.LHS)
		rhs := make([]int32, len(r.RHS))
		for i, s := range r.RHS {
			rhs[i] = g.symbol(s)
		}
		switch len(rhs) {
		case 1:
			g.Steps = append(g.Steps, Step{lhs, rhs[0], -1, int32(ri)})
		case 2:
			g.Steps = append(g.Steps, Step{lhs, rhs[0], rhs[1], int32(ri)})
		default:
			left := rhs[0]
			for i := 1; i < len(rhs)-1; i++ {
				p := g.prefix(r.RHS[:i+1])
				k := [3]int32{p, left, rhs[i]}
				if _, ok := steps[k]; !ok {
					steps[k] = int32(len(g.Steps))
					g.Steps = append(g.Steps, Step{p, left, rhs[i], -1})
				}
				left = p
			}
			g.Steps = append(g.Steps, Step{lhs, left, rhs[len(rhs)-1], int32(ri)})
		}
	}
	for word, cats := range lexicon {
		if word == "" || strings.Contains(word, "  ") || strings.TrimSpace(word) != word {
			return nil, fmt.Errorf("cfg: bad lexical entry %q", word)
		}
		for _, c := range cats {
			s := g.symbol(c)
			if !slices.Contains(g.lexicon[word], s) {
				g.lexicon[word] = append(g.lexicon[word], s)
			}
		}
		g.maxPhrase = max(g.maxPhrase, strings.Count(word, " ")+1)
	}
	for _, s := range starts {
		sym, ok := g.number[s]
		if !ok {
			return nil, fmt.Errorf("cfg: start symbol %q is not in the grammar", s)
		}
		g.Starts = append(g.Starts, sym)
	}
	if err := g.index(); err != nil {
		return nil, err
	}
	return g, nil
}

func (g *Grammar) symbol(name string) int32 {
	if s, ok := g.number[name]; ok {
		return s
	}
	s := int32(len(g.Names))
	g.number[name] = s
	g.Names = append(g.Names, name)
	g.Prefix = append(g.Prefix, false)
	return s
}

// prefix is the symbol for the first daughters of some rules. Its key starts
// with a character no symbol name can hold, so it never meets a grammar symbol.
func (g *Grammar) prefix(daughters []string) int32 {
	key := "\x00" + strings.Join(daughters, "\x00")
	if s, ok := g.number[key]; ok {
		return s
	}
	s := g.symbol(key)
	g.Names[s] = "|" + strings.Join(daughters, " ")
	g.Prefix[s] = true
	return s
}

// Symbol is a symbol's number, if the grammar has it.
func (g *Grammar) Symbol(name string) (int32, bool) {
	s, ok := g.number[name]
	return s, ok && !g.Prefix[s]
}

// PrefixSymbol is the symbol binarization made for the first daughters of
// some rule, if there is one.
func (g *Grammar) PrefixSymbol(daughters []string) (int32, bool) {
	s, ok := g.number["\x00"+strings.Join(daughters, "\x00")]
	return s, ok
}

// index builds the tables the parser looks things up in, and ranks the
// symbols so that each comes after everything it can be built from by unary
// steps, refusing a grammar whose unary steps form a cycle.
func (g *Grammar) index() error {
	n := len(g.Names)
	g.unaryUp = make([][]int32, n)
	g.unaryDown = make([][]int32, n)
	g.binUp = make([][]up, n)
	g.binDown = make([][]down, n)
	for i, st := range g.Steps {
		si := int32(i)
		if st.Right < 0 {
			g.unaryUp[st.Left] = append(g.unaryUp[st.Left], si)
			g.unaryDown[st.Parent] = append(g.unaryDown[st.Parent], si)
			continue
		}
		g.binUp[st.Left] = append(g.binUp[st.Left], up{st.Right, si})
		ds := g.binDown[st.Parent]
		j, found := slices.BinarySearchFunc(ds, st.Left, func(d down, l int32) int { return int(d.left - l) })
		if !found {
			ds = slices.Insert(ds, j, down{left: st.Left})
		}
		ds[j].steps = append(ds[j].steps, up{st.Right, si})
		g.binDown[st.Parent] = ds
	}
	for _, us := range g.binUp {
		slices.SortFunc(us, func(a, b up) int { return int(a.right - b.right) })
	}
	// rank by depth-first search over unary steps, children first
	g.rank = make([]int32, n)
	state := make([]byte, n) // 0 new, 1 on the path, 2 done
	var visit func(s int32) error
	visit = func(s int32) error {
		switch state[s] {
		case 1:
			return errors.New("cfg: the unary rules form a cycle through " + g.Names[s])
		case 2:
			return nil
		}
		state[s] = 1
		r := int32(0)
		for _, si := range g.unaryDown[s] {
			c := g.Steps[si].Left
			if err := visit(c); err != nil {
				return err
			}
			r = max(r, g.rank[c]+1)
		}
		g.rank[s] = r
		state[s] = 2
		return nil
	}
	for s := range n {
		if err := visit(int32(s)); err != nil {
			return err
		}
	}
	return nil
}

// Size is the number of symbols, grammar rules and binarized steps.
func (g *Grammar) Size() (symbols, rules, steps int) {
	return len(g.Names), len(g.Rules), len(g.Steps)
}
