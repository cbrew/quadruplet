package com.cbrew.chart

import com.cbrew.fstruct.notation.FeatureNotation
import com.cbrew.unify.AtomicValue
import com.cbrew.unify.Constant
import com.cbrew.unify.FeatureMap
import com.cbrew.unify.SemanticValue
import kotlin.test.Test
import kotlin.test.assertEquals

class LookupTest {
    @Test
    fun lookupSingleWord() {
        val fileContent = Chart::class.java.getResource("/tiny.cfg").readText()
        val g = FeatureGrammar(FeatureNotation.toGrammar(fileContent))
        assertEquals(listOf(Complete(FeatureMap(mapOf(
                Pair("cat", AtomicValue("Np")),
                Pair("sem", SemanticValue(Constant("sarah"))),
                Pair("num", AtomicValue("sing"))
        )),
                1, 2)), g.lookup("Sarah", 1),
                "simple lookup")

    }

    @Test
    fun lookupMultiWord() {
        val fileContent = Chart::class.java.getResource("/tiny.cfg").readText()
        val g = FeatureGrammar(FeatureNotation.toGrammar(fileContent))
        assertEquals(listOf(Complete(FeatureMap(mapOf(
                Pair("cat", AtomicValue("Np")),
                Pair("sem", SemanticValue(Constant("sarah_beth"))),
                Pair("num", AtomicValue("sing"))
        )),
                1, 3)), g.lookup(listOf("Sarah", "Beth"), 1, 3),
                "multiword lookup")

    }


    @Test
    fun testReader() {
        val fileContent = Chart::class.java.getResource("/tiny.cfg").readText()
        val g = FeatureGrammar(FeatureNotation.toGrammar(fileContent))
        // N.B. Np[] is a feature map, whereas N is an atom, and the two
        // do not unify


        val chart = Chart(arrayOf("Sarah", "Beth", "likes", "Chloe"))
        chart.parse(g)
        assertEquals(1, chart.solutions().size)
        assertEquals(Complete(FeatureNotation.toFs("S[num=sing,sem=<like(sarah_beth,chloe)>]"), 0, 4),
                chart.solutions()[0])
    }
}