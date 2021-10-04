package com.cbrew.chart

import com.cbrew.fstruct.notation.FeatureNotation
import com.cbrew.fstruct.notation.FeatureNotation.toFs
import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.Grammar
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ChartTest {

    val ch = Chart(
            completes = arrayOf(),
            partials = arrayOf(),
            predecessors = mutableMapOf(),
            sentence = arrayOf())

    @Test
    fun testFundamental1() {


        val partial = Partial(toFs("Np"), 1, 1,
                listOf(toFs("Det"),
                        toFs("Nn")))
        val complete = Complete(toFs("Det"), 1, 2)

        assertEquals(Partial(toFs("Np"), 1, 2,
                listOf(toFs("Nn"))),
                ch.fundamental(partial, complete),
                "on atoms fundamental rule should succeed without introducing any bindings.")

    }

    @Test
    fun testFundamental2() {
        val partial = Partial(

                toFs("Np[a=?z]"), 1, 1,
                listOf(toFs("Det[a=?z]"),
                        toFs("Nn")))
        val complete = Complete(toFs("Det[a=sing]"), 1, 2)

        assertEquals(Partial(toFs("Np[a=sing]"), 1, 2,
                listOf(toFs("Nn"))),
                ch.fundamental(partial, complete),
                "fundamental rule should succeed when introducing a binding.")

    }


    @Test
    fun testReader() {
        val fileContent = Chart::class.java.getResource("/tiny.cfg").readText()
        val g = FeatureGrammar(FeatureNotation.toGrammar(fileContent))
        // N.B. Np[] is a feature map, whereas N is an atom, and the two
        // do not unify


        val chart = Chart(arrayOf("cats", "like", "dogs"))
        chart.parse(g)
        assertEquals(1, chart.solutions().size)
        assertEquals(Complete(FeatureNotation.toFs("S[num=pl,sem=<like (∀ x. cat(x),∀ y. dog(y))>]"), 0, 3),
                chart.solutions()[0])
    }

    @Test
    fun testReader2() {
        val fileContent = Chart::class.java.getResource("/tiny.cfg").readText()
        val g = FeatureGrammar(FeatureNotation.toGrammar(fileContent))



        val chart = Chart(arrayOf("Chloe", "likes", "John"))
        chart.parse(g)
        assertEquals(1, chart.solutions().size)
        assertEquals(Complete(FeatureNotation.toFs("S[num=sing,sem=<like(chloe,john)>]"), 0, 3),
            chart.solutions()[0])
    }

    @Test
    fun testReaderIntegratedParser() {
        val fileContent = Chart::class.java.getResource("/tiny2.cfg").readText()
        val g = FeatureGrammar(IntegratedParser.toGrammar(fileContent) as Grammar)
        val chart = Chart(arrayOf("Chloe", "likes", "John"))
        chart.parse(g)
        assertEquals(1, chart.solutions().size)
        assertEquals(Complete(FeatureNotation.toFs("S[num=sing,sem=<like(chloe,john)>]"), 0, 3),
            chart.solutions()[0])
    }

    @Test
    fun testDemoReader() {
        val fileContent = Chart::class.java.getResource("/demo.fcfg").readText()
        val g = FeatureGrammar(FeatureNotation.toGrammar(fileContent))



        val chart = Chart(arrayOf("cat", "cat", "dog"))
        chart.parse(g)
        assertNotEquals<Int>(0, chart.solutions().size)


    }
    @Test
    fun testPatio() {
        val fileContent = Chart::class.java.getResource("/patio.fcfg").readText()
        val g = FeatureGrammar(IntegratedParser.toGrammar(fileContent) as Grammar)
        val chart = Chart(arrayOf("I","need","an", "umbrella"))
        chart.parse(g)
        assertEquals(1,chart.solutions().size )
    }


    @Test
    fun testStepping() {
        val fileContent = Chart::class.java.getResource("/patio.fcfg").readText()
        val g = FeatureGrammar(IntegratedParser.toGrammar(fileContent) as Grammar)
        val chart = Chart(arrayOf("I","want","an", "umbrella"))
        chart.start(g)
        while(!chart.done()){
            chart.oneStep(g)
        }
        assertEquals(1,chart.solutions().size )
    }

    @Test
    fun testEdges() {
        val fileContent = Chart::class.java.getResource("/patio.fcfg").readText()
        val g = FeatureGrammar(IntegratedParser.toGrammar(fileContent) as Grammar)
        val chart = Chart(arrayOf("I","need","an", "umbrella"))
        chart.parse(g)
        for(x in chart.sortedEdges()){
            println(x)
        }
    }





}