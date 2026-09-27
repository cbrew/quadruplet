package notation

import (
	"fmt"

	"github.com/cbrew/quadruplet/go/term"
)

// ParseLogic parses a lambda term in the logic language, for example
// \P Q . exists x . (P(x) & Q(x)). Names bound by λ become Vars and names
// bound by quantifiers QVars (de Bruijn indices); ?x names become SemVars
// and other free names Consts. Applications are beta-reduced as they are
// built; other constructs are kept as written (see term.Normalized).
//
// Precedence, from tightest: application f(x, y); & (∧); | (∨); then the
// relations -> (→), <-> (↔), = (==), != (<>, ≠), whose right-hand side
// extends as far as possible. Negation (-, ~), λ (\) and the quantifiers
// (exists, forall or all) also extend as far as possible.
func ParseLogic(s string) (term.Lambda, error) {
	toks, err := lex(s, modeLogic)
	if err != nil {
		return nil, err
	}
	p := &parser{toks: toks}
	n, err := p.logicExpr(0)
	if err != nil {
		return nil, err
	}
	if !p.at(tEOF) {
		return nil, p.errorf("unexpected %s after expression", describe(p.peek()))
	}
	return binarize(n)
}

// lnode is the parse tree of a logic expression, as in the Kotlin version:
// leaves carry a name and a type (I individual, P predicate, C constant,
// V variable, Box) and nodes a construct.
type lnode struct {
	label    string
	typ      string
	children []*lnode
}

func leaf(label, typ string) *lnode { return &lnode{label: label, typ: typ} }

const (
	precAnd = 3
	precOr  = 2
	precRel = 1
)

var relations = map[kind]string{tImplies: "implies", tIff: "iff", tSemEquals: "equals", tNotEquals: "not_equals"}

func (p *parser) logicExpr(minPrec int) (*lnode, error) {
	left, err := p.logicPrimary()
	if err != nil {
		return nil, err
	}
	for {
		switch k := p.peek().kind; {
		case k == tSemLparen: // application binds tightest
			args, err := p.applicationTail()
			if err != nil {
				return nil, err
			}
			left = &lnode{label: "application", children: append([]*lnode{left}, args...)}
		case k == tAnd && minPrec <= precAnd:
			p.next()
			right, err := p.logicExpr(precAnd + 1)
			if err != nil {
				return nil, err
			}
			left = &lnode{label: "and", children: []*lnode{left, right}}
		case k == tOr && minPrec <= precOr:
			p.next()
			right, err := p.logicExpr(precOr + 1)
			if err != nil {
				return nil, err
			}
			left = &lnode{label: "or", children: []*lnode{left, right}}
		case relations[k] != "" && minPrec <= precRel:
			p.next()
			right, err := p.logicExpr(0)
			if err != nil {
				return nil, err
			}
			left = &lnode{label: relations[k], children: []*lnode{left, right}}
		default:
			return left, nil
		}
	}
}

func (p *parser) applicationTail() ([]*lnode, error) {
	p.next() // '('
	var args []*lnode
	for {
		a, err := p.logicExpr(0)
		if err != nil {
			return nil, err
		}
		args = append(args, a)
		if !p.at(tSemComma) {
			break
		}
		p.next()
	}
	if _, err := p.expect(tSemRparen); err != nil {
		return nil, err
	}
	return args, nil
}

func (p *parser) logicPrimary() (*lnode, error) {
	t := p.peek()
	switch t.kind {
	case tConstant:
		p.next()
		return leaf(t.text, "C"), nil
	case tSemVar:
		p.next()
		return leaf(t.text, "V"), nil
	case tIndividual:
		p.next()
		return leaf(t.text, "I"), nil
	case tPredicate:
		p.next()
		return leaf(t.text, "P"), nil
	case tBox:
		p.next()
		return leaf("", "Box"), nil
	case tSemLparen:
		p.next()
		e, err := p.logicExpr(0)
		if err != nil {
			return nil, err
		}
		if _, err := p.expect(tSemRparen); err != nil {
			return nil, err
		}
		return e, nil
	case tNot:
		p.next()
		e, err := p.logicExpr(0)
		if err != nil {
			return nil, err
		}
		return &lnode{label: "negated", children: []*lnode{e}}, nil
	case tExists, tForall, tLambda:
		p.next()
		label := map[kind]string{tExists: "exists", tForall: "forall", tLambda: "lambda"}[t.kind]
		var args []*lnode
		for p.at(tIndividual, tPredicate) || (t.kind != tLambda && p.at(tSemVar)) {
			args = append(args, leaf(p.next().text, "A"))
		}
		if len(args) == 0 {
			return nil, p.errorf("expected a variable after %s", describe(t))
		}
		if _, err := p.expect(tDot); err != nil {
			return nil, err
		}
		body, err := p.logicExpr(0)
		if err != nil {
			return nil, err
		}
		return &lnode{label: label, children: append(args, body)}, nil
	}
	return nil, p.errorf("unexpected %s", describe(t))
}

// binding is an entry on the stack of enclosing binders: a name and whether
// a quantifier (rather than a λ) binds it.
type binding struct {
	name       string
	quantifier bool
}

// binarize turns the parse tree into a lambda term, resolving names against
// the enclosing binders.
func binarize(n *lnode) (term.Lambda, error) {
	var stack []binding // innermost last
	var bin func(n *lnode) (term.Lambda, error)
	bin = func(n *lnode) (term.Lambda, error) {
		if n.children == nil {
			q, l := 1, 1
			for i := len(stack) - 1; i >= 0; i-- {
				b := stack[i]
				if b.name == n.label {
					if b.quantifier {
						return term.NewQVar(q), nil
					}
					return term.NewVar(l), nil
				}
				if b.quantifier {
					q++
				} else {
					l++
				}
			}
			switch n.typ {
			case "Box":
				return term.Box, nil
			case "V":
				return term.NewSemVar(n.label), nil
			}
			return term.NewConst(n.label), nil
		}
		children := func(ns []*lnode) ([]term.Lambda, error) {
			out := make([]term.Lambda, len(ns))
			for i, c := range ns {
				l, err := bin(c)
				if err != nil {
					return nil, err
				}
				out[i] = l
			}
			return out, nil
		}
		switch n.label {
		case "and", "or":
			cs, err := children(n.children)
			if err != nil {
				return nil, err
			}
			if n.label == "and" {
				return term.NewAnd(flattenJunction(cs, true)...), nil
			}
			return term.NewOr(flattenJunction(cs, false)...), nil
		case "exists", "forall", "lambda":
			args := n.children[:len(n.children)-1]
			for _, a := range args {
				stack = append(stack, binding{a.label, n.label != "lambda"})
			}
			body, err := bin(n.children[len(n.children)-1])
			stack = stack[:len(stack)-len(args)]
			if err != nil {
				return nil, err
			}
			for range args {
				switch n.label {
				case "exists":
					body = term.NewExists(body)
				case "forall":
					body = term.NewForall(body)
				default:
					body = term.NewLam(body)
				}
			}
			return body, nil
		case "application":
			cs, err := children(n.children)
			if err != nil {
				return nil, err
			}
			r := cs[0]
			for _, a := range cs[1:] {
				r = term.CreateApp(r, a)
			}
			return r, nil
		}
		cs, err := children(n.children)
		if err != nil {
			return nil, err
		}
		switch n.label {
		case "iff", "equals":
			return term.NewEquiv(cs[0], cs[1]), nil
		case "implies":
			return term.NewImplies(cs[0], cs[1]), nil
		case "negated":
			return term.NewNot(cs[0]), nil
		case "not_equals":
			return term.NewNot(term.NewEquiv(cs[0], cs[1])), nil
		}
		return nil, fmt.Errorf("unexpected construct %q", n.label)
	}
	return bin(n)
}

// flattenJunction merges nested conjunctions (or disjunctions) among xs into
// one list, as the Kotlin parser does; duplicates are dropped by the
// constructor.
func flattenJunction(xs []term.Lambda, and bool) []term.Lambda {
	var out []term.Lambda
	var add func(term.Lambda)
	add = func(x term.Lambda) {
		if a, ok := x.(*term.And); ok && and {
			for _, e := range a.Elems() {
				add(e)
			}
			return
		}
		if o, ok := x.(*term.Or); ok && !and {
			for _, e := range o.Elems() {
				add(e)
			}
			return
		}
		out = append(out, x)
	}
	for _, x := range xs {
		add(x)
	}
	return out
}
