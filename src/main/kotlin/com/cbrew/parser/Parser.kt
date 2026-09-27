package com.cbrew.parser

import com.cbrew.chart.Chart
import com.cbrew.chart.TreeAsFeatureGrammar
import com.cbrew.chart.treestring
import com.cbrew.fstruct.notation.FeatureNotation
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {
    val args2 = Array<String>(30, { _ -> "a" })
    // The number of trees grows exponentially: 717,061,938 for 16 'a's,
    // ~4.95e18 for 30 and a 121-digit number for 170. Tree counting is
    // memoised over the packed chart, so it stays cheap even so, and
    // enumeration of the first few trees is fast because getTrees() is
    // lazy. A sentence of 170 'a's gives ~104k edges (14,535 complete,
    // 89,244 partial).

    val parseTime = measureTimeMillis {
        val chart = Chart(args2)
        chart.parse(TreeAsFeatureGrammar())
        for (e in chart.solutions(FeatureNotation.toFs("S[f=?s]"))) {
            var i = 0
            for (tree in chart.getTrees(e).take(100)) {
                print("Tree.kt $i:\n${tree.treestring(1)}")

                println()
                i += 1
            }
        }
        println(chart.stats())
    }

    println("Total time: ${parseTime} ms")

}
