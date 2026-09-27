package term

import (
	"slices"
	"strconv"
	"strings"
)

// Pretty prints a lambda term for people to read. Bound variables get names
// by depth: x1, x2, ... for quantified variables, counting from the
// outermost quantifier, and v1, v2, ... for λ-bound ones. Conjuncts and
// disjuncts are sorted, so equal terms print the same however they were
// built. Equiv prints as =, which is what the grammars use it for.
//
// The Kotlin Lambda.pretty() prints identically.
func Pretty(t Lambda) string { return pretty(t, 0, 0) }

func pretty(t Lambda, q, l int) string {
	switch t := t.(type) {
	case *Const:
		return t.name
	case *QVar:
		return "x" + strconv.Itoa(q-t.index+1)
	case *Var:
		return "v" + strconv.Itoa(l-t.index+1)
	case *Lam:
		return "λv" + strconv.Itoa(l+1) + "." + pretty(t.body, q, l+1)
	case *Exists:
		return "∃x" + strconv.Itoa(q+1) + "." + pretty(t.body, q+1, l)
	case *Forall:
		return "∀x" + strconv.Itoa(q+1) + "." + pretty(t.body, q+1, l)
	case *Not:
		return "¬" + pretty(t.body, q, l)
	case *And:
		return prettyJunct(t.elems, " ∧ ", q, l)
	case *Or:
		return prettyJunct(t.elems, " ∨ ", q, l)
	case *Implies:
		return "(" + pretty(t.e1, q, l) + " → " + pretty(t.e2, q, l) + ")"
	case *Equiv:
		return "(" + pretty(t.e1, q, l) + " = " + pretty(t.e2, q, l) + ")"
	case *App:
		var args []string
		var head Lambda = t
		for app, ok := head.(*App); ok; app, ok = head.(*App) {
			args = append(args, pretty(app.arg, q, l))
			head = app.fn
		}
		slices.Reverse(args)
		return pretty(head, q, l) + "(" + strings.Join(args, ", ") + ")"
	}
	return t.String() // SemVar, Box, Empty
}

func prettyJunct(elems []Lambda, op string, q, l int) string {
	parts := make([]string, len(elems))
	for i, e := range elems {
		parts[i] = pretty(e, q, l)
	}
	slices.Sort(parts)
	return "(" + strings.Join(parts, op) + ")"
}
