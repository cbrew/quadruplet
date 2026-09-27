package notation

import (
	"strconv"

	"github.com/cbrew/quadruplet/go/grammar"
	"github.com/cbrew/quadruplet/go/term"
)

// ParseFS parses a feature term in the FeatureNotation style:
// Cat or Cat[f=v, ...], where a value is a name, a ?variable, a list
// [v, ...] or semantics <...> in the logic language. Categories are a
// capital followed by lower-case letters and digits. Feature maps cannot be
// nested.
func ParseFS(s string) (*term.Map, error) {
	toks, err := lex(s, modeFeatures)
	if err != nil {
		return nil, err
	}
	p := &parser{toks: toks}
	m, err := p.featureExpr()
	if err != nil {
		return nil, err
	}
	if !p.at(tEOF) {
		return nil, p.errorf("unexpected %s after feature term", describe(p.peek()))
	}
	return m, nil
}

// ParseFeatureGrammar parses a grammar in the FeatureNotation style:
//
//	"word": Cat[...] | Cat[...]          lexical entries
//	Cat[...] -> Cat[...] Cat[...] | ...  rules, one per alternative
//	Cat[...] => Cat[...] ... : <(0,1)(1,0)>, ...   multiple CFG rules
func ParseFeatureGrammar(s string) (*grammar.Grammar, error) {
	toks, err := lex(s, modeFeatures)
	if err != nil {
		return nil, err
	}
	p := &parser{toks: toks}
	g := grammar.New()
	var cfg, mcfg []*grammar.Rule
	for !p.at(tEOF) {
		if p.at(tWord) {
			word := unquote(p.next().text)
			if _, err := p.expect(tColon); err != nil {
				return nil, err
			}
			for {
				m, err := p.featureExpr()
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
		lhs, err := p.featureExpr()
		if err != nil {
			return nil, err
		}
		arrow := p.next()
		if arrow.kind != tArrow && arrow.kind != tArrow2 {
			p.i--
			return nil, p.errorf("expected '->' or '=>', found %s", describe(arrow))
		}
		for {
			rhs, err := p.featureRHS()
			if err != nil {
				return nil, err
			}
			r := &grammar.Rule{LHS: lhs, RHS: rhs}
			if arrow.kind == tArrow2 {
				if _, err := p.expect(tColon); err != nil {
					return nil, err
				}
				if r.Linseq, err = p.semLinseqs(); err != nil {
					return nil, err
				}
				mcfg = append(mcfg, r)
			} else {
				cfg = append(cfg, r)
			}
			if !p.at(tPipe) {
				break
			}
			p.next()
		}
	}
	for _, r := range append(cfg, mcfg...) {
		g.AddRule(r)
	}
	return g, nil
}

// featureRHS parses one or more categories, stopping before one that turns
// out to be the left-hand side of the next rule.
func (p *parser) featureRHS() ([]*term.Map, error) {
	var rhs []*term.Map
	for p.at(tCategory) {
		save := p.i
		m, err := p.featureExpr()
		if err != nil {
			return nil, err
		}
		if len(rhs) > 0 && p.at(tArrow, tArrow2) {
			p.i = save
			break
		}
		rhs = append(rhs, m)
	}
	if len(rhs) == 0 {
		return nil, p.errorf("expected a category, found %s", describe(p.peek()))
	}
	return rhs, nil
}

// featureExpr parses Cat or Cat[mapping]. The cat feature comes last, after
// the others, as in the Kotlin version.
func (p *parser) featureExpr() (*term.Map, error) {
	cat, err := p.expect(tCategory)
	if err != nil {
		return nil, err
	}
	b := term.NewMapBuilder(4)
	if p.at(tLsq) {
		p.next()
		item := func() error {
			name, err := p.expect(tFname)
			if err != nil {
				return err
			}
			if _, err := p.expect(tEquals); err != nil {
				return err
			}
			v, err := p.featureValue()
			if err != nil {
				return err
			}
			b.Put(name.text, v)
			return nil
		}
		if p.at(tFname) {
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
	}
	b.Put("cat", term.NewAtom(cat.text))
	return b.Build(), nil
}

func (p *parser) featureValue() (term.Term, error) {
	t := p.peek()
	switch t.kind {
	case tFname:
		p.next()
		return term.NewAtom(t.text), nil
	case tSynVar:
		p.next()
		return term.NewSynVar(t.text), nil
	case tSem:
		p.next()
		l, err := ParseLogic(t.text[1 : len(t.text)-1])
		if err != nil {
			return nil, offset(err, t.pos+1)
		}
		return term.NewSem(l), nil
	case tLsq:
		p.next()
		var elems []term.Term
		for {
			v, err := p.featureValue()
			if err != nil {
				return nil, err
			}
			elems = append(elems, v)
			if !p.at(tComma) {
				break
			}
			p.next()
		}
		if _, err := p.expect(tRsq); err != nil {
			return nil, err
		}
		return term.NewList(elems...), nil
	}
	return nil, p.errorf("expected a feature value, found %s", describe(t))
}

// semLinseqs parses <(0,1)(1,0)>, <...>, ...: a list with one element per
// <...>, each a list of number lists.
func (p *parser) semLinseqs() (*term.List, error) {
	var seqs []term.Term
	for {
		t, err := p.expect(tSem)
		if err != nil {
			return nil, err
		}
		seq, err := parseNumseqs(t.text[1:len(t.text)-1], t.pos+1)
		if err != nil {
			return nil, err
		}
		seqs = append(seqs, seq)
		if !p.at(tComma) {
			break
		}
		p.next()
	}
	return term.NewList(seqs...), nil
}

// parseNumseqs parses (0,1)(1,0) (Linearization.g4) into [[0, 1], [1, 0]].
func parseNumseqs(s string, base int) (*term.List, error) {
	toks, err := lex(s, modeLogic)
	if err != nil {
		return nil, offset(err, base)
	}
	p := &parser{toks: toks}
	var seqs []term.Term
	for {
		if _, err := p.expect(tSemLparen); err != nil {
			return nil, offset(err, base)
		}
		var nums []term.Term
		for {
			n, err := p.number()
			if err != nil {
				return nil, offset(err, base)
			}
			nums = append(nums, n)
			if !p.at(tSemComma) {
				break
			}
			p.next()
		}
		if _, err := p.expect(tSemRparen); err != nil {
			return nil, offset(err, base)
		}
		seqs = append(seqs, term.NewList(nums...))
		if !p.at(tSemLparen) {
			break
		}
	}
	if !p.at(tEOF) {
		return nil, offset(p.errorf("unexpected %s", describe(p.peek())), base)
	}
	return term.NewList(seqs...), nil
}

// number accepts a run of digits, which the logic lexer calls a constant
// and the island lexer a number.
func (p *parser) number() (*term.Int, error) {
	t := p.peek()
	if t.kind == tConstant || t.kind == tNumber {
		if n, err := strconv.Atoi(t.text); err == nil {
			p.next()
			return term.NewInt(n), nil
		}
	}
	return nil, p.errorf("expected a number, found %s", describe(t))
}

func unquote(word string) string { return word[1 : len(word)-1] }

// offset shifts the position of a syntax error found in a substring.
func offset(err error, base int) error {
	if se, ok := err.(*SyntaxError); ok {
		return &SyntaxError{se.Pos + base, se.Msg}
	}
	return err
}
