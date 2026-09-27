package term

import (
	"strconv"
	"strings"
)

// Printing follows the Kotlin implementation exactly, so that output can be
// compared with it: maps print as cat[k=v, ...], lambda terms with de Bruijn
// indices (v:1, q:1), and Not, Implies and Equiv in Kotlin data-class style.

func (a *Atom) String() string      { return a.value }
func (s *Sem) String() string       { return "<" + s.value.String() + ">" }
func (v *SynVar) String() string    { return v.name }
func (l *List) String() string      { return seqString(l.elems) }
func (l *ListExpr) String() string  { return seqString(l.elems) }
func (t *Tuple) String() string     { return seqString(t.elems) }
func (t *TupleExpr) String() string { return seqString(t.elems) }
func (i *Int) String() string       { return strconv.Itoa(i.value) }

func seqString(elems []Term) string {
	var b strings.Builder
	b.WriteByte('[')
	for i, e := range elems {
		if i > 0 {
			b.WriteString(", ")
		}
		b.WriteString(e.String())
	}
	b.WriteByte(']')
	return b.String()
}

// String prints cat[k=v, ...], the other features in insertion order. A map
// without cat prints as [k=v, ...] (the Kotlin version recurses forever).
func (m *Map) String() string {
	var b strings.Builder
	if c, ok := m.Get("cat"); ok {
		b.WriteString(c.String())
	}
	b.WriteByte('[')
	first := true
	for i, k := range m.keys {
		if k == "cat" {
			continue
		}
		if !first {
			b.WriteString(", ")
		}
		first = false
		b.WriteString(k)
		b.WriteByte('=')
		b.WriteString(m.vals[i].String())
	}
	b.WriteByte(']')
	return b.String()
}

func (c *Const) String() string  { return c.name }
func (v *Var) String() string    { return "v:" + strconv.Itoa(v.index) }
func (q *QVar) String() string   { return "q:" + strconv.Itoa(q.index) }
func (v *SemVar) String() string { return v.name }
func (l *Lam) String() string    { return "λ.(" + l.body.String() + ")" }
func (f *Forall) String() string { return "∀.(" + f.body.String() + ")" }
func (e *Exists) String() string { return "∃.(" + e.body.String() + ")" }
func (n *Not) String() string    { return "Not(body=" + n.body.String() + ")" }
func (i *Implies) String() string {
	return "Implies(e1=" + i.e1.String() + ", e2=" + i.e2.String() + ")"
}
func (e *Equiv) String() string   { return "Equiv(e1=" + e.e1.String() + ", e2=" + e.e2.String() + ")" }
func (*boxTerm) String() string   { return "☐" }
func (*emptyTerm) String() string { return "∅" }
func (a *And) String() string     { return junctString(a.elems, " ∧ ") }
func (o *Or) String() string      { return junctString(o.elems, " ∨ ") }

func junctString(elems []Lambda, sep string) string {
	var b strings.Builder
	b.WriteByte('(')
	for i, e := range elems {
		if i > 0 {
			b.WriteString(sep)
		}
		b.WriteString(e.String())
	}
	b.WriteByte(')')
	return b.String()
}

// String prints an application uncurried: f(a)(b) prints as f(a, b).
func (a *App) String() string {
	var parts []Lambda
	var walk func(l Lambda)
	walk = func(l Lambda) {
		if app, ok := l.(*App); ok {
			walk(app.fn)
			parts = append(parts, app.arg)
		} else {
			parts = append(parts, l)
		}
	}
	walk(a)
	var b strings.Builder
	b.WriteString(parts[0].String())
	b.WriteByte('(')
	for i, p := range parts[1:] {
		if i > 0 {
			b.WriteString(", ")
		}
		b.WriteString(p.String())
	}
	b.WriteByte(')')
	return b.String()
}
