package com.cbrew.unify

/**
 * Prints a lambda term for people to read. Bound variables get names by
 * depth: x1, x2, ... for quantified variables, counting from the outermost
 * quantifier, and v1, v2, ... for λ-bound ones. Conjuncts and disjuncts are
 * sorted, so equal terms print the same however they were built. Equiv
 * prints as =, which is what the grammars use it for. A DRS prints as
 * ([referents], [conditions]), its referents named like quantified
 * variables; its conditions keep their order.
 *
 * For terms without DRS nodes, the Go port's term.Pretty prints
 * identically.
 */
fun Lambda.pretty(): String = pretty(this, 0, 0)

private fun pretty(t: Lambda, q: Int, l: Int): String = when (t) {
    is Constant -> t.name
    is QVar -> "x${q - t.index + 1}"
    is Var -> "v${l - t.index + 1}"
    is Lam -> "λv${l + 1}." + pretty(t.body, q, l + 1)
    is Exists -> "∃x${q + 1}." + pretty(t.body, q + 1, l)
    is Forall -> "∀x${q + 1}." + pretty(t.body, q + 1, l)
    is Not -> "¬" + pretty(t.body, q, l)
    is And -> t.conjuncts.map { pretty(it, q, l) }.sorted().joinToString(" ∧ ", "(", ")")
    is Or -> t.disjuncts.map { pretty(it, q, l) }.sorted().joinToString(" ∨ ", "(", ")")
    is Implies -> "(${pretty(t.e1, q, l)} → ${pretty(t.e2, q, l)})"
    is Equiv -> "(${pretty(t.e1, q, l)} = ${pretty(t.e2, q, l)})"
    is App -> {
        val args = mutableListOf<Lambda>()
        var head: Lambda = t
        while (head is App) {
            args.add(0, head.e2)
            head = head.e1
        }
        pretty(head, q, l) + args.joinToString(", ", "(", ")") { pretty(it, q, l) }
    }
    is Drs -> box(t.refs, t.conds, q, l)
    is DrsImp -> "(" + box(t.refs, t.antecedent, q, l) + " → " + pretty(t.consequent, q + t.refs, l) + ")"
    is Resolved -> "(" + pretty(t.pronoun, q, l) + " = " +
            t.antecedents.joinToString(", ", "[", "]") { pretty(it, q, l) } + ")"
    is FstructVar -> t.name
    is Box -> "☐"
    is Empty -> "∅"
}

// a box's referents are named as quantified variables, then its conditions
// are printed in order, within their scope
private fun box(refs: Int, conds: List<Lambda>, q: Int, l: Int): String =
        (q + 1..q + refs).joinToString(", ", "([", "]") { "x$it" } + ", " +
                conds.joinToString(", ", "[", "])") { pretty(it, q + refs, l) }
