// Package term implements the terms quadruplet parses with: feature
// structures, the lambda terms that carry semantics inside them, and their
// unification.
//
// Terms are immutable. Every constructor computes, once, the facts later
// operations need: a 64-bit hash, whether the term contains unification
// variables, and for lambda terms the depth of their free de Bruijn
// variables and whether a Box occurs. Operations that rebuild terms
// (substitution, renaming, beta reduction) use these facts to return
// unchanged subterms as they are, so new terms share structure with old
// ones. Because nothing is ever mutated, terms can be shared freely between
// chart edges and between goroutines.
//
// Do not modify the slices returned by accessors such as Elems or Values.
package term

// Term is a feature structure (FS) or a lambda term (Lambda).
type Term interface {
	// Hash is consistent with Equal: equal terms have equal hashes.
	Hash() uint64
	// Ground reports whether the term contains no unification variables
	// (SynVar or SemVar), so that substitution leaves it unchanged. List and
	// tuple expressions are never ground, since substitution may still
	// simplify them.
	Ground() bool
	String() string
	facts() *termFacts
}

// FS is a feature structure.
type FS interface {
	Term
	isFS()
}

// Lambda is a lambda-calculus term, used for semantics.
type Lambda interface {
	Term
	// FreeVarDepth is the largest i - d over occurrences of Var(i) under d
	// enclosing Lams within the term, or 0. The term has a free Var above
	// index n exactly when FreeVarDepth() > n.
	FreeVarDepth() int
	// FreeQVarDepth is the same for QVar and the quantifiers.
	FreeQVarDepth() int
	// HasBox reports whether Box occurs in the term.
	HasBox() bool
	isLambda()
}

type termFacts struct {
	hash   uint64
	ground bool
	fv, fq int32
	box    bool
}

func (f *termFacts) Hash() uint64       { return f.hash }
func (f *termFacts) Ground() bool       { return f.ground }
func (f *termFacts) FreeVarDepth() int  { return int(f.fv) }
func (f *termFacts) FreeQVarDepth() int { return int(f.fq) }
func (f *termFacts) HasBox() bool       { return f.box }
func (f *termFacts) facts() *termFacts  { return f }

type fsMarker struct{}

func (fsMarker) isFS() {}

type lambdaMarker struct{}

func (lambdaMarker) isLambda() {}

// ---------------------------------------------------------------- hashing

// constructor tags, mixed into every hash
const (
	tagAtom uint64 = iota + 1
	tagSem
	tagSynVar
	tagList
	tagListExpr
	tagTuple
	tagTupleExpr
	tagMap
	tagInt
	tagConst
	tagVar
	tagQVar
	tagLam
	tagForall
	tagExists
	tagApp
	tagNot
	tagAnd
	tagOr
	tagImplies
	tagEquiv
	tagSemVar
	tagBox
	tagEmpty
)

// mix is the splitmix64 finalizer.
func mix(h uint64) uint64 {
	h ^= h >> 30
	h *= 0xbf58476d1ce4e5b9
	h ^= h >> 27
	h *= 0x94d049bb133111eb
	h ^= h >> 31
	return h
}

func strHash(s string) uint64 {
	h := uint64(14695981039346656037)
	for i := 0; i < len(s); i++ {
		h ^= uint64(s[i])
		h *= 1099511628211
	}
	return h
}

func hashOrdered(tag uint64, xs ...uint64) uint64 {
	h := mix(tag)
	for _, x := range xs {
		h = mix(h*31 + x)
	}
	return h
}

// ---------------------------------------------------------------- feature structures

// Atom is an atomic feature value such as sg.
type Atom struct {
	termFacts
	fsMarker
	value string
}

func NewAtom(value string) *Atom {
	return &Atom{termFacts: termFacts{hash: hashOrdered(tagAtom, strHash(value)), ground: true}, value: value}
}

func (a *Atom) Value() string { return a.value }

// Sem wraps a lambda term as a feature value, written <...>.
type Sem struct {
	termFacts
	fsMarker
	value Lambda
}

func NewSem(value Lambda) *Sem {
	return &Sem{termFacts: termFacts{hash: hashOrdered(tagSem, value.Hash()), ground: value.Ground()}, value: value}
}

func (s *Sem) Value() Lambda { return s.value }

// SynVar is a unification variable over feature structures, written ?x.
type SynVar struct {
	termFacts
	fsMarker
	name string
}

func NewSynVar(name string) *SynVar {
	return &SynVar{termFacts: termFacts{hash: hashOrdered(tagSynVar, strHash(name))}, name: name}
}

func (v *SynVar) Name() string { return v.name }

// List is a feature value [a, b, ...]; Tuple has the same form but its own
// type. ListExpr and TupleExpr are concatenations still containing
// variables; substitution simplifies them (see Simplify).
type List struct {
	termFacts
	fsMarker
	elems []Term
}

type ListExpr struct {
	termFacts
	fsMarker
	elems []Term
}

type Tuple struct {
	termFacts
	fsMarker
	elems []Term
}

type TupleExpr struct {
	termFacts
	fsMarker
	elems []Term
}

func seqFacts(tag uint64, elems []Term, groundable bool) termFacts {
	hs := make([]uint64, len(elems))
	g := groundable
	for i, e := range elems {
		hs[i] = e.Hash()
		g = g && e.Ground()
	}
	return termFacts{hash: hashOrdered(tag, hs...), ground: g}
}

func NewList(elems ...Term) *List {
	return &List{termFacts: seqFacts(tagList, elems, true), elems: elems}
}

func NewListExpr(elems ...Term) *ListExpr {
	return &ListExpr{termFacts: seqFacts(tagListExpr, elems, false), elems: elems}
}

func NewTuple(elems ...Term) *Tuple {
	return &Tuple{termFacts: seqFacts(tagTuple, elems, true), elems: elems}
}

func NewTupleExpr(elems ...Term) *TupleExpr {
	return &TupleExpr{termFacts: seqFacts(tagTupleExpr, elems, false), elems: elems}
}

func (l *List) Elems() []Term      { return l.elems }
func (l *ListExpr) Elems() []Term  { return l.elems }
func (t *Tuple) Elems() []Term     { return t.elems }
func (t *TupleExpr) Elems() []Term { return t.elems }

// Simplify collapses adjacent lists and tuples in an expression. The result
// stays an expression while it contains a variable element.
func (e *ListExpr) Simplify() FS {
	elems, isExpr := flattenExpr(e.elems)
	if isExpr {
		return NewListExpr(elems...)
	}
	return NewList(elems...)
}

func (e *TupleExpr) Simplify() FS {
	elems, isExpr := flattenExpr(e.elems)
	if isExpr {
		return NewTupleExpr(elems...)
	}
	return NewTuple(elems...)
}

func flattenExpr(elems []Term) ([]Term, bool) {
	var acc []Term
	isExpr := false
	for _, e := range elems {
		switch x := e.(type) {
		case *Tuple:
			acc = append(acc, x.elems...)
		case *List:
			acc = append(acc, x.elems...)
		case *SynVar:
			isExpr = true
			acc = append(acc, x)
		default:
			acc = append(acc, e)
		}
	}
	return acc, isExpr
}

// Int is an integer, used in linearization sequences.
type Int struct {
	termFacts
	fsMarker
	value int
}

func NewInt(value int) *Int {
	return &Int{termFacts: termFacts{hash: hashOrdered(tagInt, uint64(value)), ground: true}, value: value}
}

func (i *Int) Value() int { return i.value }

// Map is a feature structure: features and their values, in insertion
// order (which only affects printing; equality ignores order). Feature maps
// hold a handful of features, so lookup is a linear scan.
type Map struct {
	termFacts
	fsMarker
	keys []string
	vals []Term
}

// NewMap builds a map from parallel slices. A repeated key keeps its first
// position and takes its last value.
func NewMap(keys []string, vals []Term) *Map {
	b := NewMapBuilder(len(keys))
	for i, k := range keys {
		b.Put(k, vals[i])
	}
	return b.Build()
}

// MapBuilder collects the features of a new Map.
type MapBuilder struct {
	keys []string
	vals []Term
}

func NewMapBuilder(capacity int) MapBuilder {
	return MapBuilder{keys: make([]string, 0, capacity), vals: make([]Term, 0, capacity)}
}

// Put adds a feature. If the key is already present its value is replaced
// and it keeps its position, as when Kotlin maps are combined.
func (b *MapBuilder) Put(key string, val Term) *MapBuilder {
	for i, k := range b.keys {
		if k == key {
			b.vals[i] = val
			return b
		}
	}
	b.keys = append(b.keys, key)
	b.vals = append(b.vals, val)
	return b
}

// Build makes the map; the builder must not be used afterwards.
func (b *MapBuilder) Build() *Map {
	m := newMap(b.keys, b.vals)
	b.keys, b.vals = nil, nil
	return m
}

// newMap makes a map that owns keys and vals, which must have distinct keys.
func newMap(keys []string, vals []Term) *Map {
	var sum uint64
	g := true
	for i, k := range keys {
		sum += mix(strHash(k) ^ mix(vals[i].Hash()+0x9e3779b97f4a7c15))
		g = g && vals[i].Ground()
	}
	return &Map{termFacts: termFacts{hash: hashOrdered(tagMap, sum), ground: g}, keys: keys, vals: vals}
}

func (m *Map) Len() int          { return len(m.keys) }
func (m *Map) Keys() []string    { return m.keys }
func (m *Map) Values() []Term    { return m.vals }
func (m *Map) Has(k string) bool { return m.index(k) >= 0 }

func (m *Map) Get(k string) (Term, bool) {
	if i := m.index(k); i >= 0 {
		return m.vals[i], true
	}
	return nil, false
}

func (m *Map) index(k string) int {
	for i, key := range m.keys {
		if key == k {
			return i
		}
	}
	return -1
}

// ---------------------------------------------------------------- lambda terms

// Const is a constant or an unbound name: john, P, walk.
type Const struct {
	termFacts
	lambdaMarker
	name string
}

func NewConst(name string) *Const {
	return &Const{termFacts: termFacts{hash: hashOrdered(tagConst, strHash(name)), ground: true}, name: name}
}

func (c *Const) Name() string { return c.name }

// Var is a lambda-bound variable, as a de Bruijn index: Var(1) is bound by
// the innermost enclosing Lam.
type Var struct {
	termFacts
	lambdaMarker
	index int
}

func NewVar(index int) *Var {
	return &Var{termFacts: termFacts{hash: hashOrdered(tagVar, uint64(index)), ground: true, fv: int32(max(0, index))}, index: index}
}

func (v *Var) Index() int { return v.index }

// QVar is a quantifier-bound variable, as a de Bruijn index counting only
// Exists and Forall.
type QVar struct {
	termFacts
	lambdaMarker
	index int
}

func NewQVar(index int) *QVar {
	return &QVar{termFacts: termFacts{hash: hashOrdered(tagQVar, uint64(index)), ground: true, fq: int32(max(0, index))}, index: index}
}

func (q *QVar) Index() int { return q.index }

// SemVar is a unification variable over lambda terms, written ?x inside <...>.
type SemVar struct {
	termFacts
	lambdaMarker
	name string
}

func NewSemVar(name string) *SemVar {
	return &SemVar{termFacts: termFacts{hash: hashOrdered(tagSemVar, strHash(name))}, name: name}
}

func (v *SemVar) Name() string { return v.name }

// Lam, Forall, Exists and Not have a single body.
type Lam struct {
	termFacts
	lambdaMarker
	body Lambda
}

type Forall struct {
	termFacts
	lambdaMarker
	body Lambda
}

type Exists struct {
	termFacts
	lambdaMarker
	body Lambda
}

type Not struct {
	termFacts
	lambdaMarker
	body Lambda
}

// NewLam, NewForall, NewExists and NewNot build the node as given; the
// Create... functions in logic.go simplify.
func NewLam(body Lambda) *Lam {
	return &Lam{termFacts: termFacts{hash: hashOrdered(tagLam, body.Hash()), ground: body.Ground(),
		fv: int32(max(0, body.FreeVarDepth()-1)), fq: int32(body.FreeQVarDepth()), box: body.HasBox()}, body: body}
}

func NewForall(body Lambda) *Forall {
	return &Forall{termFacts: quantFacts(tagForall, body), body: body}
}

func NewExists(body Lambda) *Exists {
	return &Exists{termFacts: quantFacts(tagExists, body), body: body}
}

func quantFacts(tag uint64, body Lambda) termFacts {
	return termFacts{hash: hashOrdered(tag, body.Hash()), ground: body.Ground(),
		fv: int32(body.FreeVarDepth()), fq: int32(max(0, body.FreeQVarDepth()-1)), box: body.HasBox()}
}

func NewNot(body Lambda) *Not {
	return &Not{termFacts: termFacts{hash: hashOrdered(tagNot, body.Hash()), ground: body.Ground(),
		fv: int32(body.FreeVarDepth()), fq: int32(body.FreeQVarDepth()), box: body.HasBox()}, body: body}
}

func (l *Lam) Body() Lambda    { return l.body }
func (f *Forall) Body() Lambda { return f.body }
func (e *Exists) Body() Lambda { return e.body }
func (n *Not) Body() Lambda    { return n.body }

// App, Implies and Equiv have two parts.
type App struct {
	termFacts
	lambdaMarker
	fn, arg Lambda
}

type Implies struct {
	termFacts
	lambdaMarker
	e1, e2 Lambda
}

type Equiv struct {
	termFacts
	lambdaMarker
	e1, e2 Lambda
}

func binaryFacts(tag uint64, a, b Lambda) termFacts {
	return termFacts{hash: hashOrdered(tag, a.Hash(), b.Hash()), ground: a.Ground() && b.Ground(),
		fv: int32(max(a.FreeVarDepth(), b.FreeVarDepth())), fq: int32(max(a.FreeQVarDepth(), b.FreeQVarDepth())),
		box: a.HasBox() || b.HasBox()}
}

func NewApp(fn, arg Lambda) *App {
	return &App{termFacts: binaryFacts(tagApp, fn, arg), fn: fn, arg: arg}
}

func NewImplies(e1, e2 Lambda) *Implies {
	return &Implies{termFacts: binaryFacts(tagImplies, e1, e2), e1: e1, e2: e2}
}

func NewEquiv(e1, e2 Lambda) *Equiv {
	return &Equiv{termFacts: binaryFacts(tagEquiv, e1, e2), e1: e1, e2: e2}
}

func (a *App) Fn() Lambda     { return a.fn }
func (a *App) Arg() Lambda    { return a.arg }
func (i *Implies) E1() Lambda { return i.e1 }
func (i *Implies) E2() Lambda { return i.e2 }
func (e *Equiv) E1() Lambda   { return e.e1 }
func (e *Equiv) E2() Lambda   { return e.e2 }

// And and Or hold a set of terms: distinct (by Equal), kept in first
// occurrence order.
type And struct {
	termFacts
	lambdaMarker
	elems []Lambda
}

type Or struct {
	termFacts
	lambdaMarker
	elems []Lambda
}

// NewAnd builds a conjunction of the distinct elements, without flattening
// nested conjunctions (see CreateAnd).
func NewAnd(elems ...Lambda) *And {
	elems = distinct(elems)
	return &And{termFacts: setFacts(tagAnd, elems), elems: elems}
}

func NewOr(elems ...Lambda) *Or {
	elems = distinct(elems)
	return &Or{termFacts: setFacts(tagOr, elems), elems: elems}
}

func (a *And) Elems() []Lambda { return a.elems }
func (o *Or) Elems() []Lambda  { return o.elems }

func setFacts(tag uint64, elems []Lambda) termFacts {
	f := termFacts{ground: true}
	var sum uint64
	for _, e := range elems {
		sum += mix(e.Hash())
		f.ground = f.ground && e.Ground()
		f.fv = max(f.fv, int32(e.FreeVarDepth()))
		f.fq = max(f.fq, int32(e.FreeQVarDepth()))
		f.box = f.box || e.HasBox()
	}
	f.hash = hashOrdered(tag, sum)
	return f
}

// distinct drops repeated elements, keeping the first of each. Sets are
// almost always tiny, so it scans; larger ones are indexed by hash.
func distinct(elems []Lambda) []Lambda {
	if len(elems) > 16 {
		return distinctLarge(elems)
	}
	firstDup := -1
	for i := 1; i < len(elems) && firstDup < 0; i++ {
		for _, o := range elems[:i] {
			if Equal(o, elems[i]) {
				firstDup = i
				break
			}
		}
	}
	if firstDup < 0 {
		return elems
	}
	out := append(make([]Lambda, 0, len(elems)-1), elems[:firstDup]...)
	for _, e := range elems[firstDup+1:] {
		dup := false
		for _, o := range out {
			if Equal(o, e) {
				dup = true
				break
			}
		}
		if !dup {
			out = append(out, e)
		}
	}
	return out
}

func distinctLarge(elems []Lambda) []Lambda {
	seen := make(map[uint64][]Lambda, len(elems))
	out := make([]Lambda, 0, len(elems))
next:
	for _, e := range elems {
		for _, o := range seen[e.Hash()] {
			if Equal(o, e) {
				continue next
			}
		}
		seen[e.Hash()] = append(seen[e.Hash()], e)
		out = append(out, e)
	}
	return out
}

// Box marks the places where beta reduction substitutes an argument.
type boxTerm struct {
	termFacts
	lambdaMarker
}

// Empty is the empty lambda term.
type emptyTerm struct {
	termFacts
	lambdaMarker
}

var (
	Box   Lambda = &boxTerm{termFacts: termFacts{hash: hashOrdered(tagBox), ground: true, box: true}}
	Empty Lambda = &emptyTerm{termFacts: termFacts{hash: hashOrdered(tagEmpty), ground: true}}
)

// ---------------------------------------------------------------- labels

// Label is a short name for display: an atom's value, or cat[f] for a map
// (with "null" for a missing feature, as the Kotlin version prints).
func Label(t Term) string {
	switch x := t.(type) {
	case *Atom:
		return x.value
	case *Map:
		return optLabel(x, "cat") + "[" + optLabel(x, "f") + "]"
	}
	return "??"
}

func optLabel(m *Map, k string) string {
	if v, ok := m.Get(k); ok {
		return Label(v)
	}
	return "null"
}

// Key is the index a grammar files a category under: an atom's value or a
// map's cat label.
func Key(t Term) string {
	switch x := t.(type) {
	case *Atom:
		return x.value
	case *Map:
		if v, ok := x.Get("cat"); ok {
			return Label(v)
		}
	}
	return "??"
}
