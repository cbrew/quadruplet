package com.cbrew.parser

import com.cbrew.chart.Chart
import com.cbrew.chart.TreeAsFeatureGrammar
import com.cbrew.chart.treestring
import com.cbrew.fstruct.notation.FeatureNotation
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {
    val args2 = Array<String>(30, { _ -> "a" })
    // we can go up to 20 'a's without breaking, though it does take a while
    // to count the trees at that point. Unsurprising, since there are over
    // 1.76 billion trees to count.
    // Parsing itself is still fast, as is enumeration of first few trees,
    // since getTrees() is efficient. Parsing a sentence of 170 'a's takes
    // 36s and produces ~ 44k edges.

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
        println(chart.stats(doCount = args2.size < 16))
    }

    println("Total time: ${parseTime} ms")

}