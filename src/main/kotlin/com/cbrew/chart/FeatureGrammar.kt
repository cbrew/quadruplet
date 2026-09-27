package com.cbrew.chart

import com.cbrew.unify.*

class FeatureGrammar : ChartGrammar {

    val grammar: Grammar

    private val leftCorner: MutableMap<String, MutableSet<Rule>> = mutableMapOf()

    constructor(g: Grammar) {
        grammar = g
        // Rules are normalized once here, so that substitution, which shares
        // unchanged subterms instead of rebuilding them, still yields the
        // simplified lambda terms that a full rebuild would produce.
        grammar.rules.forEach {
            val rule = (it as Unifiable).normalized() as Rule
            val k = rule.firstNeeded().key()

            if (leftCorner[k]?.add(rule) == null)
                leftCorner[k] = mutableSetOf(rule)
        }
    }


    fun Rule.lhs(): Unifiable =
            when (this) {
                is CfgRule -> lhs
                is McfgRule -> lhs
                else -> throw Exception("all members of rules should be rules")
            }

    fun Rule.firstNeeded(): Unifiable =
            when (this) {
                is CfgRule -> rhs.first()
                is McfgRule -> rhs.first()
                else -> throw Exception("all members of rules should be rules")
            }

    fun Rule.rhs(): List<Unifiable> =
            when (this) {
                is CfgRule -> rhs
                is McfgRule -> rhs
                else -> throw Exception("all members of rules should be rules")
            }

    override fun spawn(lc: Complete): List<Edge> {
        val cat = lc.category
        val k = cat.key()
        // same test as Chart.fundamental, so a spawned edge always combines
        // with the edge that spawned it
        val rules = leftCorner[k]?.filter { r: Rule ->
            val renamed = if (cat.ground) cat else cat.renamedApartFrom(listOf(r.lhs()) + r.rhs())
            unify(r.firstNeeded(), renamed) != null
        } ?: listOf()
        return rules.map { r -> emptyEdge(r.lhs(), lc.start, r.rhs()) }
    }

    override fun lookup(word: String, start: Int): List<Edge> =
            grammar.lexicon[word]?.map { e -> Complete(e, start, start + 1) } ?: listOf()


    override fun lookup(words: List<String>, start: Int, end: Int): List<Edge> =
            grammar.lexicon[words.joinToString(" ")]?.map { e -> Complete(e, start, end) } ?: listOf()


}