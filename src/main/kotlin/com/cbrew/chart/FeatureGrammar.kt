package com.cbrew.chart

import com.cbrew.unify.*

class FeatureGrammar : ChartGrammar {

    val grammar: Grammar

    private val leftCorner: MutableMap<String, MutableSet<Rule>> = mutableMapOf()

    constructor(g: Grammar) {
        grammar = g
        grammar.rules.forEach {
            val k = it.firstNeeded().key()

            if (leftCorner[k]?.add(it) == null)
                leftCorner[k] = mutableSetOf(it)
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
        val rules = leftCorner[k]?.filter { r: Rule -> cat.unify(r.firstNeeded()) != null } ?: listOf()
        return rules.map { r -> emptyEdge(r.lhs(), lc.start, r.rhs()) }
    }

    override fun lookup(word: String, start: Int): List<Edge> =
            grammar.lexicon[word]?.map { e -> Complete(e, start, start + 1) } ?: listOf()


    override fun lookup(words: List<String>, start: Int, end: Int): List<Edge> =
            grammar.lexicon[words.joinToString(" ")]?.map { e -> Complete(e, start, end) } ?: listOf()


}