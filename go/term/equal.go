package term

// Equal reports whether two terms are structurally equal. Maps compare as
// sets of features and And/Or as sets of elements; everything else compares
// in order. Hashes are compared first, so unequal terms are usually told
// apart at once.
func Equal(a, b Term) bool {
	if a == b {
		return true
	}
	if a == nil || b == nil || a.Hash() != b.Hash() {
		return false
	}
	switch x := a.(type) {
	case *Atom:
		y, ok := b.(*Atom)
		return ok && x.value == y.value
	case *Sem:
		y, ok := b.(*Sem)
		return ok && Equal(x.value, y.value)
	case *SynVar:
		y, ok := b.(*SynVar)
		return ok && x.name == y.name
	case *List:
		y, ok := b.(*List)
		return ok && equalSeq(x.elems, y.elems)
	case *ListExpr:
		y, ok := b.(*ListExpr)
		return ok && equalSeq(x.elems, y.elems)
	case *Tuple:
		y, ok := b.(*Tuple)
		return ok && equalSeq(x.elems, y.elems)
	case *TupleExpr:
		y, ok := b.(*TupleExpr)
		return ok && equalSeq(x.elems, y.elems)
	case *Int:
		y, ok := b.(*Int)
		return ok && x.value == y.value
	case *Map:
		y, ok := b.(*Map)
		if !ok || len(x.keys) != len(y.keys) {
			return false
		}
		for i, k := range x.keys {
			v, found := y.Get(k)
			if !found || !Equal(x.vals[i], v) {
				return false
			}
		}
		return true
	case *Const:
		y, ok := b.(*Const)
		return ok && x.name == y.name
	case *Var:
		y, ok := b.(*Var)
		return ok && x.index == y.index
	case *QVar:
		y, ok := b.(*QVar)
		return ok && x.index == y.index
	case *SemVar:
		y, ok := b.(*SemVar)
		return ok && x.name == y.name
	case *Lam:
		y, ok := b.(*Lam)
		return ok && Equal(x.body, y.body)
	case *Forall:
		y, ok := b.(*Forall)
		return ok && Equal(x.body, y.body)
	case *Exists:
		y, ok := b.(*Exists)
		return ok && Equal(x.body, y.body)
	case *Not:
		y, ok := b.(*Not)
		return ok && Equal(x.body, y.body)
	case *App:
		y, ok := b.(*App)
		return ok && Equal(x.fn, y.fn) && Equal(x.arg, y.arg)
	case *Implies:
		y, ok := b.(*Implies)
		return ok && Equal(x.e1, y.e1) && Equal(x.e2, y.e2)
	case *Equiv:
		y, ok := b.(*Equiv)
		return ok && Equal(x.e1, y.e1) && Equal(x.e2, y.e2)
	case *And:
		y, ok := b.(*And)
		return ok && equalSet(x.elems, y.elems)
	case *Or:
		y, ok := b.(*Or)
		return ok && equalSet(x.elems, y.elems)
	}
	// Box and Empty are singletons, handled by the identity check
	return false
}

func equalSeq(xs, ys []Term) bool {
	if len(xs) != len(ys) {
		return false
	}
	for i := range xs {
		if !Equal(xs[i], ys[i]) {
			return false
		}
	}
	return true
}

// equalSet compares duplicate-free slices as sets.
func equalSet(xs, ys []Lambda) bool {
	if len(xs) != len(ys) {
		return false
	}
	for _, x := range xs {
		found := false
		for _, y := range ys {
			if Equal(x, y) {
				found = true
				break
			}
		}
		if !found {
			return false
		}
	}
	return true
}
