package term

// Unification is term unification with named variables: reentrancy is
// written by repeating a variable, and bindings are threaded through as an
// immutable Bindings list. Failure returns ok == false; nothing needs
// undoing. A bound variable's value is unified with whatever it meets, so a
// binding can be refined, and an occurs check keeps bindings acyclic.
// SynVars range over feature structures and SemVars over lambda terms. Two
// lambda terms unify only if they are equal; unification does not look
// inside them.

// Unify unifies a and b and returns the result with the bindings applied
// and its variables renamed canonically (?x0, ?x1, ...).
func Unify(a, b Term) (Term, bool) {
	r, bs, ok := UnifyBindings(a, b, nil)
	if !ok {
		return nil, false
	}
	return Canonicalize(bs.Subst(r)), true
}

// UnifyBindings unifies a and b under the given bindings, returning the
// unified term (not yet substituted) and the extended bindings.
func UnifyBindings(a, b Term, bs *Bindings) (Term, *Bindings, bool) {
	switch x := a.(type) {
	case *SynVar:
		return bs.bindVariable(x, b)
	case *SemVar:
		return bs.bindVariable(x, b)
	case Lambda:
		switch y := b.(type) {
		case *SynVar:
			return bs.bindVariable(y, a)
		case *SemVar:
			return bs.bindVariable(y, a)
		case Lambda:
			if Equal(x, y) {
				return x, bs, true
			}
		}
		return nil, nil, false
	case FS:
		switch y := b.(type) {
		case *SynVar:
			return bs.bindVariable(y, a)
		case *SemVar:
			return bs.bindVariable(y, a)
		case FS:
			return unifyFS(x, y, bs)
		}
	}
	return nil, nil, false
}

func unifyFS(a, b FS, bs *Bindings) (Term, *Bindings, bool) {
	if Equal(a, b) {
		return a, bs, true
	}
	switch x := a.(type) {
	case *Map:
		if y, ok := b.(*Map); ok {
			return unifyMaps(x, y, bs)
		}
	case *List:
		y, ok := b.(*List)
		if !ok || len(x.elems) != len(y.elems) {
			return nil, nil, false
		}
		elems := make([]Term, len(x.elems))
		for i := range x.elems {
			e, nbs, ok := UnifyBindings(x.elems[i], y.elems[i], bs)
			if !ok {
				return nil, nil, false
			}
			elems[i], bs = e, nbs
		}
		return NewList(elems...), bs, true
	case *Sem:
		if y, ok := b.(*Sem); ok {
			v, nbs, ok := UnifyBindings(x.value, y.value, bs)
			if !ok {
				return nil, nil, false
			}
			if v == Term(x.value) {
				return x, nbs, true
			}
			return NewSem(v.(Lambda)), nbs, true
		}
	}
	return nil, nil, false
}

// unifyMaps orders the result's features: those only in a, then those only
// in b, then shared ones, as the Kotlin version does.
func unifyMaps(a, b *Map, bs *Bindings) (Term, *Bindings, bool) {
	out := NewMapBuilder(len(a.keys) + len(b.keys))
	for i, k := range a.keys {
		if !b.Has(k) {
			out.Put(k, a.vals[i])
		}
	}
	for i, k := range b.keys {
		if !a.Has(k) {
			out.Put(k, b.vals[i])
		}
	}
	for i, k := range a.keys {
		v2, ok := b.Get(k)
		if !ok {
			continue
		}
		v, nbs, ok := UnifyBindings(a.vals[i], v2, bs)
		if !ok {
			return nil, nil, false
		}
		bs = nbs
		out.Put(k, v)
	}
	return out.Build(), bs, true
}

// bindVariable unifies the variable v with other. Both are dereferenced
// first, so chains of variable-to-variable bindings are followed to their
// end. If v is bound, its value is unified with other; if unbound, it is
// bound to other unless other contains it.
func (bs *Bindings) bindVariable(v, other Term) (Term, *Bindings, bool) {
	value := bs.Deref(v)
	target := bs.Deref(other)
	if !inDomain(v, value) || !inDomain(v, target) {
		return nil, nil, false
	}
	if !isVar(value) {
		return UnifyBindings(value, other, bs)
	}
	if Equal(value, target) {
		return value, bs, true
	}
	if bs.occurs(value, target) {
		return nil, nil, false
	}
	name, _ := varName(value)
	return target, bs.Bind(name, target), true
}

func inDomain(v, value Term) bool {
	if _, ok := v.(*SemVar); ok {
		_, isLambda := value.(Lambda)
		return isLambda
	}
	_, isFS := value.(FS)
	return isFS
}

// occurs reports whether the unbound variable v occurs in term under these
// bindings.
func (bs *Bindings) occurs(v, term Term) bool {
	if term.Ground() {
		return false
	}
	if isVar(term) {
		t := bs.Deref(term)
		if isVar(t) {
			n1, _ := varName(t)
			n2, _ := varName(v)
			return n1 == n2
		}
		return bs.occurs(v, t)
	}
	for _, s := range subterms(term) {
		if bs.occurs(v, s) {
			return true
		}
	}
	return false
}
