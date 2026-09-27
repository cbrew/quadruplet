package term

import "strings"

// Bindings maps unification variable names to values. It is an immutable
// association list: Bind returns a new cell pointing at the existing list, so
// earlier bindings stay valid and a failed unification attempt is simply
// dropped. Lookup is linear, which suits the handful of bindings one
// unification makes. The nil *Bindings is the empty list.
type Bindings struct {
	name  string
	value Term
	rest  *Bindings
	size  int
}

// Bind returns these bindings plus name bound to value. The caller ensures
// name is not already bound; unification only binds unbound variables.
func (b *Bindings) Bind(name string, value Term) *Bindings {
	return &Bindings{name: name, value: value, rest: b, size: b.Len() + 1}
}

// Lookup returns the value bound to name, if any.
func (b *Bindings) Lookup(name string) (Term, bool) {
	for c := b; c != nil; c = c.rest {
		if c.name == name {
			return c.value, true
		}
	}
	return nil, false
}

func (b *Bindings) Len() int {
	if b == nil {
		return 0
	}
	return b.size
}

// Each calls f for each binding, oldest first.
func (b *Bindings) Each(f func(name string, value Term)) {
	var cells []*Bindings
	for c := b; c != nil; c = c.rest {
		cells = append(cells, c)
	}
	for i := len(cells) - 1; i >= 0; i-- {
		f(cells[i].name, cells[i].value)
	}
}

// String prints {?x=a, ?y=b}, oldest binding first.
func (b *Bindings) String() string {
	var s strings.Builder
	s.WriteByte('{')
	first := true
	b.Each(func(name string, value Term) {
		if !first {
			s.WriteString(", ")
		}
		first = false
		s.WriteString(name)
		s.WriteByte('=')
		s.WriteString(value.String())
	})
	s.WriteByte('}')
	return s.String()
}

// Deref follows v through the bindings, returning an unbound variable or a
// non-variable term, never a bound variable.
func (b *Bindings) Deref(v Term) Term {
	for {
		name, ok := varName(v)
		if !ok {
			return v
		}
		next, bound := b.Lookup(name)
		if !bound {
			return v
		}
		v = next
	}
}

func varName(t Term) (string, bool) {
	switch v := t.(type) {
	case *SynVar:
		return v.name, true
	case *SemVar:
		return v.name, true
	}
	return "", false
}

func isVar(t Term) bool {
	_, ok := varName(t)
	return ok
}
