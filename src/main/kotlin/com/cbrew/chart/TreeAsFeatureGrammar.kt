package com.cbrew.chart


import com.cbrew.fstruct.notation.FeatureNotation


/**
 * simple grammar that generates lots of trees over sequence of "a"
 * s
 */
class TreeAsFeatureGrammar : ChartGrammar {
    val even = FeatureNotation.toFs("S[f=even]")
    val odd = FeatureNotation.toFs("S[f=odd]")

    override fun spawn(lc: Complete): List<Edge> =

            when (lc.category) {
                odd ->
                    listOf(emptyEdge(odd, lc.start, listOf(odd, odd, odd)),
                            emptyEdge(odd, lc.start, listOf(odd, even, even)),
                            emptyEdge(even, lc.start, listOf(odd, even, odd)),
                            emptyEdge(even, lc.start, listOf(odd, odd, even)),
                            emptyEdge(odd, lc.start, listOf(odd, even)),
                            emptyEdge(even, lc.start, listOf(odd, odd)))
                even ->
                    listOf(emptyEdge(even, lc.start, listOf(even, even)),
                            emptyEdge(odd, lc.start, listOf(even, odd)),
                            emptyEdge(odd, lc.start, listOf(even, odd, even)),
                            emptyEdge(odd, lc.start, listOf(even, even, odd)),
                            emptyEdge(even, lc.start, listOf(even, even, even)),
                            emptyEdge(even, lc.start, listOf(even, odd, odd))
                    )
                else -> listOf()
            }


    override fun lookup(word: String, start: Int): List<Edge> =
            when (word) {
                "a" -> listOf(Complete(odd, start, start + 1))
                else -> listOf()
            }


    override fun lookup(words: List<String>, start: Int, end: Int): List<Edge> = listOf()

}