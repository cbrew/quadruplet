package notation

import (
	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/term"
)

// ParseIntegratedGrammar parses a grammar in the IntegratedParser style
// (patio.fcfg, sem2.fcfg):
//
//	"word": Cat[...] | Cat[...]            lexical entries
//	Cat[...] -> Cat[...] "word" ... | ...  rules; quoted words on the
//	                                       right go into the lexicon
//	Cat[...] => Cat[...] ... : <(0 1)(1 0)>, ...   multiple CFG rules
//
// Categories may be any capitalised name and always take brackets. Feature
// values are names, ?variables, lists [...], tuples (...), concatenations
// [a + b], semantics <...> in the logic language, or (unsupported, see the
// README) nested feature maps; +f and -f abbreviate f=true and f=false.
//
// Each alternative of a rule becomes a separate rule. (The Kotlin
// IntegratedVisitor merges the alternatives of a rule into one right-hand
// side; FeatureNotationVisitor separates them, as here.) Lexical entries
// written "word": Cat come first in the lexicon, then words from rules.
func ParseIntegratedGrammar(s string) (*grammar.Grammar, error) {
	toks, err := lex(s, modeIntegrated)
	if err != nil {
		return nil, err
	}
	p := &parser{toks: toks}
	g := grammar.New()
	type lexEntry struct {
		word string
		cat  *term.Map
	}
	var fromRules []lexEntry
	for !p.at(tEOF) {
		if p.at(tWord) {
			word := unquote(p.next().text)
			if _, err := p.expect(tColon); err != nil {
				return nil, err
			}
			for {
				m, err := p.integratedMap()
				if err != nil {
					return nil, err
				}
				g.Lexicon.Add(word, m)
				if !p.at(tPipe) {
					break
				}
				p.next()
			}
			continue
		}
		lhs, err := p.integratedMap()
		if err != nil {
			return nil, err
		}
		arrow := p.next()
		if arrow.kind != tArrow && arrow.kind != tArrow2 {
			p.i--
			return nil, p.errorf("expected '->' or '=>', found %s", describe(arrow))
		}
		for {
			r := &grammar.Rule{LHS: lhs}
			if err := p.integratedRHS(r, arrow.kind == tArrow2); err != nil {
				return nil, err
			}
			if arrow.kind == tArrow2 {
				if _, err := p.expect(tColon); err != nil {
					return nil, err
				}
				var seqs []term.Term
				for {
					seq, err := p.islandNumseqs()
					if err != nil {
						return nil, err
					}
					seqs = append(seqs, seq)
					if !p.at(tComma) {
						break
					}
					p.next()
				}
				r.Linseq = term.NewList(seqs...)
			}
			for _, w := range r.Words {
				fromRules = append(fromRules, lexEntry{w, lhs})
			}
			if len(r.RHS) > 0 {
				g.AddRule(r)
			}
			if !p.at(tPipe) {
				break
			}
			p.next()
		}
	}
	for _, e := range fromRules {
		g.Lexicon.Add(e.word, e.cat)
	}
	return g, nil
}

// integratedRHS parses the categories and words of one alternative,
// stopping before the start of the next statement: a word followed by ':'
// or a category followed by an arrow. A multiple CFG rule has no words.
func (p *parser) integratedRHS(r *grammar.Rule, mcfg bool) error {
	for {
		switch {
		case p.at(tWord) && !mcfg:
			if p.peekAt(1) == tColon {
				return p.checkRHS(r)
			}
			r.Words = append(r.Words, unquote(p.next().text))
		case p.at(tCategory):
			save := p.i
			m, err := p.integratedMap()
			if err != nil {
				return err
			}
			if p.at(tArrow, tArrow2) && (len(r.RHS) > 0 || len(r.Words) > 0) {
				p.i = save
				return nil
			}
			r.RHS = append(r.RHS, m)
		default:
			return p.checkRHS(r)
		}
	}
}

func (p *parser) checkRHS(r *grammar.Rule) error {
	if len(r.RHS) == 0 && len(r.Words) == 0 {
		return p.errorf("expected a category or word, found %s", describe(p.peek()))
	}
	return nil
}

// integratedMap parses Cat[mapping]. Features come in the order written,
// then the +/- abbreviations, then cat, as in the Kotlin version.
func (p *parser) integratedMap() (*term.Map, error) {
	cat, err := p.expect(tCategory)
	if err != nil {
		return nil, err
	}
	if _, err := p.expect(tLsq); err != nil {
		return nil, err
	}
	pairs := term.NewMapBuilder(4)
	abbrevs := term.NewMapBuilder(2)
	var keys []string
	var vals []term.Term
	item := func() error {
		if p.at(tPlus, tMinus) {
			plus := p.next().kind == tPlus
			name, err := p.expect(tFname)
			if err != nil {
				return err
			}
			value := "false"
			if plus {
				value = "true"
			}
			abbrevs.Put(name.text, term.NewAtom(value))
			return nil
		}
		name, err := p.expect(tFname)
		if err != nil {
			return err
		}
		if _, err := p.expect(tEquals); err != nil {
			return err
		}
		v, err := p.integratedValue()
		if err != nil {
			return err
		}
		pairs.Put(name.text, v)
		return nil
	}
	if p.at(tFname, tPlus, tMinus) {
		if err := item(); err != nil {
			return nil, err
		}
	}
	for p.at(tComma) {
		p.next()
		if err := item(); err != nil {
			return nil, err
		}
	}
	if _, err := p.expect(tRsq); err != nil {
		return nil, err
	}
	for _, part := range []*term.Map{pairs.Build(), abbrevs.Build()} {
		keys = append(keys, part.Keys()...)
		vals = append(vals, part.Values()...)
	}
	keys = append(keys, "cat")
	vals = append(vals, term.NewAtom(cat.text))
	return term.NewMap(keys, vals), nil
}

func (p *parser) integratedValue() (term.Term, error) {
	t := p.peek()
	switch t.kind {
	case tFname:
		p.next()
		return term.NewAtom(t.text), nil
	case tSynVar:
		p.next()
		return term.NewSynVar(t.text), nil
	case tCategory:
		return p.integratedMap()
	case tOpen:
		p.next()
		n, err := p.logicExpr(0)
		if err != nil {
			return nil, err
		}
		if _, err := p.expect(tClose); err != nil {
			return nil, err
		}
		l, err := binarize(n)
		if err != nil {
			return nil, err
		}
		return term.NewSem(l), nil
	case tLsq, tLparen:
		p.next()
		closer := tRsq
		if t.kind == tLparen {
			closer = tRparen
		}
		elems, isExpr, err := p.integratedValues(closer)
		if err != nil {
			return nil, err
		}
		if _, err := p.expect(closer); err != nil {
			return nil, err
		}
		switch {
		case t.kind == tLsq && isExpr:
			return term.NewListExpr(elems...), nil
		case t.kind == tLsq:
			return term.NewList(elems...), nil
		case isExpr:
			return term.NewTupleExpr(elems...), nil
		}
		return term.NewTuple(elems...), nil
	}
	return nil, p.errorf("expected a feature value, found %s", describe(t))
}

// integratedValues parses the inside of a list or tuple: values separated by
// commas (possibly none), or an expression a + b + ... .
func (p *parser) integratedValues(closer kind) ([]term.Term, bool, error) {
	if p.at(closer) {
		return nil, false, nil
	}
	var elems []term.Term
	if !p.at(tComma) {
		v, err := p.integratedValue()
		if err != nil {
			return nil, false, err
		}
		elems = append(elems, v)
		if p.at(tPlus) {
			for p.at(tPlus) {
				p.next()
				v, err := p.integratedValue()
				if err != nil {
					return nil, false, err
				}
				elems = append(elems, v)
			}
			return elems, true, nil
		}
	}
	for p.at(tComma) {
		p.next()
		v, err := p.integratedValue()
		if err != nil {
			return nil, false, err
		}
		elems = append(elems, v)
	}
	return elems, false, nil
}

// islandNumseqs parses a linearization <(0 1)(1 0)> into [[0, 1], [1, 0]].
func (p *parser) islandNumseqs() (*term.List, error) {
	if _, err := p.expect(tOpen); err != nil {
		return nil, err
	}
	var seqs []term.Term
	for {
		if _, err := p.expect(tSemLparen); err != nil {
			return nil, err
		}
		var nums []term.Term
		for p.at(tNumber) {
			n, err := p.number()
			if err != nil {
				return nil, err
			}
			nums = append(nums, n)
		}
		if len(nums) == 0 {
			return nil, p.errorf("expected a number, found %s", describe(p.peek()))
		}
		if _, err := p.expect(tSemRparen); err != nil {
			return nil, err
		}
		seqs = append(seqs, term.NewList(nums...))
		if !p.at(tSemLparen) {
			break
		}
	}
	if _, err := p.expect(tClose); err != nil {
		return nil, err
	}
	return term.NewList(seqs...), nil
}
