package com.cbrew.chart

import com.cbrew.unify.*

class FeatureGrammar : ChartGrammar {

    val grammar: Grammar

    private val leftCorner: MutableMap<String, MutableSet<Rule>> = mutableMapOf()

    // the grammar's lexicon, normalized (see the constructor)
    private val lexicon: Map<String, Set<FeatureMap>>

    constructor(g: Grammar) {
        grammar = g
        // Rules and lexical entries are normalized once here, so that
        // substitution and beta reduction, which share unchanged subterms
        // instead of rebuilding them, still yield the simplified lambda terms
        // that a full rebuild would produce.
        lexicon = g.lexicon.mapValues { (_, entries) -> entries.map { it.normalized() as FeatureMap }.toSet() }
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
            lexicon[word]?.map { e -> Complete(e, start, start + 1) } ?: listOf()


    override fun lookup(words: List<String>, start: Int, end: Int): List<Edge> =
            lexicon[words.joinToString(" ")]?.map { e -> Complete(e, start, end) } ?: listOf()


}