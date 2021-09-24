package com.cbrew.chart

import com.cbrew.fstruct.notation.FeatureNotation
import com.cbrew.fstruct.notation.FeatureNotation.toFs
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

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
        // N.B. Np[] is a feature map, whereas N is an atom, and the two
        // do not unify


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
        print(chart.solutions())


    }


    @Ignore
    @Test
    fun testKleiSem2() {
        val fileContent = Chart::class.java.getResource("/sem2.fcfg").readText()
        val g = FeatureGrammar(FeatureNotation.toGrammar(fileContent))
        val chart = Chart(arrayOf("Susie", "barks"))
        chart.parse(g)
        print(chart.solutions())


    }


}