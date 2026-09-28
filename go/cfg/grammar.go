// Package cfg is a fast parser for context-free grammars: grammars whose
// categories are plain symbols, with no features to unify. It builds the
// same packed forest as package chart would, holding only the items that lie
// on a derivation of the whole input, in flat memory over integer symbols.
//
// The method is that of Schmid's BitPar (2004), and of the LCFRS parser in
// cbrew/odd_one_out, specialised to context-free rules. Rules are binarized
// by pairing up the daughters that occur together most often, as BitPar
// does, so the parser only ever joins two items. A first pass recognises,
// for every span, the symbols derivable over it (CKY), testing all the split
// points of a step with one AND of bit vectors, as BitPar does. A second
// pass goes top down
// from the goal and keeps an item only if it is derivable and on a
// derivation of the whole input, recording, as it goes, every way of
// building it. No item that could not be part of a parse is ever stored.
package cfg

import (
	"errors"
	"fmt"
	"slices"
	"strconv"
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
// step completes, or -1 for the step that builds an auxiliary symbol.
type Step struct {
	Parent, Left, Right int32
	Rule                int32
}

// Grammar is a context-free grammar compiled for parsing.
type Grammar struct {
	Names  []string // symbol -> name; auxiliary symbols are named {A B}, {{A B} C}, ...
	Aux    []bool   // symbol -> it is an auxiliary symbol, made by binarization
	Starts []int32  // the start symbols
	Rules  []Rule   // the grammar's rules, without duplicates
	Steps  []Step

	ruleIndex map[string]int32 // rule, as a string -> its index
	ruleStep  []int32          // rule -> the step that completes it
	auxStep   []int32          // auxiliary symbol -> the one step that builds it; -1 for others
	width     []int32          // symbol -> how many of a rule's daughters it covers

	number     map[string]int32
	lexicon    map[string][]int32 // word or phrase -> symbols
	maxPhrase  int                // the most words in a lexical phrase
	unaryUp    [][]int32          // child -> unary steps over it
	unaryDown  [][]int32          // parent -> unary steps building it
	binUp      [][]up             // left -> (right, step), sorted by right
	binDown    [][]down           // parent -> lefts, sorted, each with its (right, step)s
	binParents []int32            // the symbols binary steps build, by unary rank
	rank       []int32            // symbol -> above all its unary descendants
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
	g := &Grammar{number: map[string]int32{}, lexicon: map[string][]int32{}, ruleIndex: map[string]int32{}}
	for _, r := range rules {
		if len(r.RHS) == 0 {
			return nil, fmt.Errorf("cfg: %s: rules with no daughters are not supported", r)
		}
		key := r.String()
		if _, ok := g.ruleIndex[key]; ok {
			continue
		}
		g.ruleIndex[key] = int32(len(g.Rules))
		g.Rules = append(g.Rules, r)
	}
	g.binarize()
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
	g.Aux = append(g.Aux, false)
	g.auxStep = append(g.auxStep, -1)
	g.width = append(g.width, 1)
	return s
}

func (g *Grammar) step(st Step) int32 {
	g.Steps = append(g.Steps, st)
	return int32(len(g.Steps) - 1)
}

// binarize makes the steps: for each rule one step that completes it, over
// its daughter or over two symbols, and one step for each auxiliary symbol,
// which stands for a pair of symbols. As in BitPar (Schmid 2004, following
// Andreas Eisele), each rule in turn replaces its most frequent pair of
// neighbouring daughters with an auxiliary symbol, the counts being over all
// the rules, until two symbols are left; if their pair is in other rules
// too, it is replaced as well, leaving a unary step. So an auxiliary symbol
// is shared by every rule that has its pair, wherever it is in the rule,
// and over a span it is built once for them all. Each rule still has one
// binary tree, and each auxiliary symbol one step, so derivations of the
// binarized grammar are one to one with those of the grammar.
func (g *Grammar) binarize() {
	type pair [2]int32
	type work struct {
		lhs, rule int32
		ds        []int32
	}
	g.ruleStep = make([]int32, len(g.Rules))
	count := map[pair]int{}
	var todo []work
	for ri, r := range g.Rules {
		w := work{lhs: g.symbol(r.LHS), rule: int32(ri)}
		for _, s := range r.RHS {
			w.ds = append(w.ds, g.symbol(s))
		}
		if len(w.ds) == 1 {
			g.ruleStep[ri] = g.step(Step{w.lhs, w.ds[0], -1, w.rule})
			continue
		}
		for i := 1; i < len(w.ds); i++ {
			count[pair{w.ds[i-1], w.ds[i]}]++
		}
		todo = append(todo, w)
	}
	for len(todo) > 0 {
		var next []work
		for _, w := range todo {
			ds := w.ds
			if len(ds) == 1 {
				g.ruleStep[w.rule] = g.step(Step{w.lhs, ds[0], -1, w.rule})
				continue
			}
			at := 0
			for i := 1; i+1 < len(ds); i++ {
				if count[pair{ds[i], ds[i+1]}] > count[pair{ds[at], ds[at+1]}] {
					at = i
				}
			}
			p := pair{ds[at], ds[at+1]}
			if len(ds) == 2 && count[p] == 1 {
				g.ruleStep[w.rule] = g.step(Step{w.lhs, ds[0], ds[1], w.rule})
				continue
			}
			x := g.aux(p[0], p[1])
			nd := slices.Concat(ds[:at], []int32{x}, ds[at+2:])
			// the pairs across the new symbol's edges replace those across the old
			if at > 0 {
				count[pair{nd[at-1], x}]++
				count[pair{nd[at-1], p[0]}]--
			}
			if at+1 < len(nd) {
				count[pair{x, nd[at+1]}]++
				count[pair{p[1], nd[at+1]}]--
			}
			next = append(next, work{w.lhs, w.rule, nd})
		}
		todo = next
	}
}

// aux is the auxiliary symbol for the pair a b. Its key starts with a
// character no symbol name can hold, so it never meets a grammar symbol.
func (g *Grammar) aux(a, b int32) int32 {
	key := "\x00" + strconv.Itoa(int(a)) + " " + strconv.Itoa(int(b))
	if s, ok := g.number[key]; ok {
		return s
	}
	s := g.symbol(key)
	g.Names[s] = "{" + g.Names[a] + " " + g.Names[b] + "}"
	g.Aux[s] = true
	g.width[s] = g.width[a] + g.width[b]
	g.auxStep[s] = g.step(Step{s, a, b, -1})
	return s
}

// Symbol is a symbol's number, if the grammar has it.
func (g *Grammar) Symbol(name string) (int32, bool) {
	s, ok := g.number[name]
	return s, ok && !g.Aux[s]
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
	// lowest rank first, so that a symbol found brings its unary ancestors
	// with it before they would be tested
	for s := range n {
		if len(g.binDown[s]) > 0 {
			g.binParents = append(g.binParents, int32(s))
		}
	}
	slices.SortStableFunc(g.binParents, func(a, b int32) int { return int(g.rank[a] - g.rank[b]) })
	return nil
}

// Size is the number of symbols, grammar rules and binarized steps.
func (g *Grammar) Size() (symbols, rules, steps int) {
	return len(g.Names), len(g.Rules), len(g.Steps)
}
