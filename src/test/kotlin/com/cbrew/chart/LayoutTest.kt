package com.cbrew.chart

import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.Grammar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class LayoutTest {

    @Test
    fun testLayoutSentence() {

        val fileContent = Chart::class.java.getResource("/patio.fcfg").readText()
        val g = FeatureGrammar(IntegratedParser.toGrammar(fileContent) as Grammar)
        val chart = Chart(arrayOf("I", "need", "an", "umbrella"))
        val layout = ChartLayout(chart)
        chart.parse(g)
        for (x in chart.sortedEdges()) {
            layout.add(x)
        }

        layout.layers.forEachIndexed { index, s ->
            println("${index} ${s}")
        }
    }
}