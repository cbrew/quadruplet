// Package notation parses quadruplet's notations: feature terms and
// grammars in the FeatureNotation style (demo.fcfg), grammars in the
// IntegratedParser style (patio.fcfg, sem2.fcfg), and the logic language
// used for semantics inside <...>.
//
// The parsers are hand-written recursive descent, following the ANTLR
// grammars of the Kotlin version (FeatureTerms.g4, FeatLexer.g4,
// FeatParser.g4, LogicTerms.g4, Linearization.g4) token for token. Unlike
// ANTLR they do not recover from errors: any syntax error, or input left
// over after a complete term, is reported.
package notation

import (
	"fmt"
	"strings"
	"unicode/utf8"
)

type kind int

const (
	tEOF kind = iota
	// logic language
	tOr
	tAnd
	tImplies
	tIff
	tNot
	tNotEquals
	tSemEquals
	tSemComma
	tSemLparen
	tSemRparen
	tLambda
	tForall
	tExists
	tDot
	tBox
	tIndividual
	tPredicate
	tSemVar
	tConstant
	tNumber
	tClose // '>' ending semantics in the integrated notation
	// feature notations
	tEquals
	tSynVar
	tCategory
	tFname
	tLparen
	tRparen
	tLsq
	tRsq
	tComma
	tPlus
	tMinus
	tOpen // '<' starting semantics in the integrated notation
	tSem  // a whole <...> in the FeatureNotation style
	tArrow
	tArrow2
	tColon
	tPipe
	tWord
)

var kindNames = map[kind]string{
	tEOF: "end of input", tOr: "'|'", tAnd: "'&'", tImplies: "'->'", tIff: "'<->'", tNot: "'-'",
	tNotEquals: "'!='", tSemEquals: "'='", tSemComma: "','", tSemLparen: "'('", tSemRparen: "')'",
	tLambda: "'\\'", tForall: "'forall'", tExists: "'exists'", tDot: "'.'", tBox: "'☐'",
	tIndividual: "individual", tPredicate: "predicate", tSemVar: "variable", tConstant: "constant",
	tNumber: "number", tClose: "'>'", tEquals: "'='", tSynVar: "variable", tCategory: "category",
	tFname: "feature name", tLparen: "'('", tRparen: "')'", tLsq: "'['", tRsq: "']'", tComma: "','",
	tPlus: "'+'", tMinus: "'-'", tOpen: "'<'", tSem: "semantics", tArrow: "'->'", tArrow2: "'=>'",
	tColon: "':'", tPipe: "'|'", tWord: "quoted word",
}

func (k kind) String() string { return kindNames[k] }

type token struct {
	kind kind
	text string
	pos  int // byte offset
}

type mode int

const (
	modeLogic      mode = iota // LogicTerms.g4
	modeIsland                 // FeatLexer.g4 inside <...>
	modeIntegrated             // FeatLexer.g4 default mode
	modeFeatures               // FeatureTerms.g4
)

// A rule matches a token at the start of s, returning its length in bytes
// (0 for no match).
type rule struct {
	kind  kind
	skip  bool
	match func(s string) int
}

func literal(k kind, alts ...string) rule {
	return rule{kind: k, match: func(s string) int {
		best := 0
		for _, a := range alts {
			if strings.HasPrefix(s, a) && len(a) > best {
				best = len(a)
			}
		}
		return best
	}}
}

// pattern matches first then zero or more of rest (or one or more, if
// atLeastOne).
func pattern(k kind, first, rest func(rune) bool, atLeastOne bool) rule {
	return rule{kind: k, match: func(s string) int {
		r, n := utf8.DecodeRuneInString(s)
		if n == 0 || !first(r) {
			return 0
		}
		i, count := n, 0
		for i < len(s) {
			r, w := utf8.DecodeRuneInString(s[i:])
			if !rest(r) {
				break
			}
			i += w
			count++
		}
		if atLeastOne && count == 0 {
			return 0
		}
		return i
	}}
}

func lower(r rune) bool { return r >= 'a' && r <= 'z' }
func upper(r rune) bool { return r >= 'A' && r <= 'Z' }
func digit(r rune) bool { return r >= '0' && r <= '9' }
func anyOf(fs ...func(rune) bool) func(rune) bool {
	return func(r rune) bool {
		for _, f := range fs {
			if f(r) {
				return true
			}
		}
		return false
	}
}
func oneOf(chars string) func(rune) bool {
	return func(r rune) bool { return strings.ContainsRune(chars, r) }
}

var (
	comment = rule{skip: true, match: func(s string) int {
		if !strings.HasPrefix(s, "#") {
			return 0
		}
		if i := strings.IndexAny(s, "\r\n"); i >= 0 {
			return i
		}
		return len(s)
	}}
	space = rule{skip: true, match: func(s string) int {
		i := 0
		for i < len(s) && strings.IndexByte(" \t\r\n", s[i]) >= 0 {
			i++
		}
		return i
	}}
	// constants are any mix of [ ] ' letters digits / _ % ; + : @ ` * # and
	// characters from U+0080 to U+FFFE
	constantChar = anyOf(lower, upper, digit, oneOf("[]'/_%;+:@`*#"),
		func(r rune) bool { return r >= 0x80 && r <= 0xfffe })
)

// the logic tokens, in the priority order of LogicTerms.g4
func logicRules(numbers bool) []rule {
	rs := []rule{
		literal(tOr, "|", "∨"),
		literal(tAnd, "&", "∧"),
		literal(tImplies, "->", "→"),
		literal(tIff, "<->", "↔"),
		literal(tNot, "-", "~"),
		literal(tNotEquals, "!=", "<>", "≠"),
		literal(tSemEquals, "==", "="),
		literal(tSemComma, ","),
		literal(tSemLparen, "("),
		literal(tSemRparen, ")"),
		literal(tLambda, "\\", "λ"),
		literal(tForall, "forall", "all", "∀"),
		literal(tExists, "exists", "∃"),
		literal(tDot, "."),
		literal(tBox, "☐"),
		pattern(tIndividual, lower, digit, false),
		pattern(tPredicate, upper, digit, false),
		pattern(tSemVar, oneOf("?@"), anyOf(lower, digit), true),
		pattern(tConstant, constantChar, constantChar, false),
	}
	if !numbers {
		return append([]rule{comment, space}, rs...)
	}
	// FeatLexer's ISLAND mode: '>' closes, numbers come before constants,
	// and there are no comments
	return append([]rule{literal(tClose, ">"), pattern(tNumber, digit, digit, false), space}, rs...)
}

var (
	logicTokens  = logicRules(false)
	islandTokens = logicRules(true)

	integratedTokens = []rule{
		comment, space,
		literal(tEquals, "="),
		pattern(tSynVar, oneOf("?@"), anyOf(lower, digit), true),
		pattern(tCategory, upper, anyOf(upper, lower, digit), false),
		pattern(tFname, anyOf(lower, digit, oneOf("_")), anyOf(lower, digit, oneOf("_")), false),
		literal(tLparen, "("), literal(tRparen, ")"), literal(tLsq, "["), literal(tRsq, "]"),
		literal(tComma, ","), literal(tPlus, "+"), literal(tMinus, "-"),
		literal(tOpen, "<"),
		literal(tArrow, "->"), literal(tArrow2, "=>"), literal(tColon, ":"), literal(tPipe, "|"),
		{kind: tWord, match: matchWord},
	}

	featureTokens = []rule{
		comment, space,
		literal(tEquals, "="),
		pattern(tSynVar, oneOf("?@"), anyOf(lower, digit), true),
		pattern(tCategory, upper, anyOf(lower, digit), false),
		pattern(tFname, anyOf(lower, digit), anyOf(lower, digit), false),
		literal(tLparen, "("), literal(tRparen, ")"), literal(tLsq, "["), literal(tRsq, "]"),
		literal(tComma, ","),
		{kind: tSem, match: matchSem},
		literal(tArrow, "->"), literal(tArrow2, "=>"), literal(tColon, ":"), literal(tPipe, "|"),
		{kind: tWord, match: matchWord},
	}
)

// matchWord matches a double-quoted word of at least one character.
func matchWord(s string) int {
	if !strings.HasPrefix(s, `"`) {
		return 0
	}
	i := strings.IndexByte(s[1:], '"')
	if i <= 0 {
		return 0
	}
	return i + 2
}

// matchSem matches <...> up to the first '>' that is not part of '->'.
func matchSem(s string) int {
	if !strings.HasPrefix(s, "<") {
		return 0
	}
	for i := 1; i < len(s); {
		switch {
		case strings.HasPrefix(s[i:], "->"):
			i += 2
		case s[i] == '>':
			return i + 1
		default:
			i++
		}
	}
	return 0
}

// SyntaxError reports where parsing failed.
type SyntaxError struct {
	Pos int
	Msg string
}

func (e *SyntaxError) Error() string {
	return fmt.Sprintf("syntax error at offset %d: %s", e.Pos, e.Msg)
}

// lex splits s into tokens. In the integrated mode '<' switches to the
// island mode until the matching '>'.
func lex(s string, m mode) ([]token, error) {
	var toks []token
	for i := 0; i < len(s); {
		var rules []rule
		switch m {
		case modeLogic:
			rules = logicTokens
		case modeIsland:
			rules = islandTokens
		case modeIntegrated:
			rules = integratedTokens
		case modeFeatures:
			rules = featureTokens
		}
		best, bestLen := -1, 0
		for j, r := range rules {
			if n := r.match(s[i:]); n > bestLen {
				best, bestLen = j, n
			}
		}
		if best < 0 {
			r, _ := utf8.DecodeRuneInString(s[i:])
			return nil, &SyntaxError{i, fmt.Sprintf("unexpected character %q", r)}
		}
		r := rules[best]
		if !r.skip {
			toks = append(toks, token{r.kind, s[i : i+bestLen], i})
			switch {
			case m == modeIntegrated && r.kind == tOpen:
				m = modeIsland
			case m == modeIsland && r.kind == tClose:
				m = modeIntegrated
			}
		}
		i += bestLen
	}
	return append(toks, token{tEOF, "", len(s)}), nil
}

// parser holds the tokens and a cursor, shared by the parsers of each
// notation.
type parser struct {
	toks []token
	i    int
}

func (p *parser) peek() token { return p.toks[p.i] }
func (p *parser) peekAt(k int) kind {
	if p.i+k < len(p.toks) {
		return p.toks[p.i+k].kind
	}
	return tEOF
}
func (p *parser) next() token {
	t := p.toks[p.i]
	if t.kind != tEOF {
		p.i++
	}
	return t
}

func (p *parser) at(ks ...kind) bool {
	k := p.peek().kind
	for _, x := range ks {
		if k == x {
			return true
		}
	}
	return false
}

func (p *parser) expect(k kind) (token, error) {
	t := p.peek()
	if t.kind != k {
		return t, p.errorf("expected %v, found %s", k, describe(t))
	}
	return p.next(), nil
}

func (p *parser) errorf(format string, args ...any) error {
	return &SyntaxError{p.peek().pos, fmt.Sprintf(format, args...)}
}

func describe(t token) string {
	if t.kind == tEOF {
		return "end of input"
	}
	return fmt.Sprintf("%q", t.text)
}
