package com.cbrew.chart

import com.cbrew.unify.AtomicValue



/**
 * simple grammar that generates all binary trees over sequence of "a"
 * s
 */
class BinaryTreeOfAGrammar : ChartGrammar {
    override fun spawn(lc: Complete): List<Edge> =
            when (lc.category) {
                AtomicValue("s") ->
                    listOf(makeEdge(AtomicValue("s"),
                            lc.start, lc.start,
                            listOf(AtomicValue("s"),
                                    AtomicValue("s"))))
                else -> listOf()
            }


    override fun lookup(word: String, start: Int): List<Edge> =
            when (word) {
                "a" -> listOf(Complete(AtomicValue("s"), start, start + 1))
                else -> listOf()
            }

    override fun lookup(words: List<String>, start: Int, end: Int): List<Edge> = listOf()

}