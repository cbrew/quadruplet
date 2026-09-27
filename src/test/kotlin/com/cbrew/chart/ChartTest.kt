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

    val ch = Chart(sentence = arrayOf())

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

    private fun countAs(n: Int): java.math.BigInteger {
        val chart = Chart(Array(n) { "a" })
        chart.parse(TreeAsFeatureGrammar())
        return chart.countTrees()
    }

    @Test
    fun testCountTrees() {
        // 16 matches the old unmemoised Int count; 20 overflowed Int and
        // 40 would overflow Long. Values cross-checked against the Go port.
        assertEquals(java.math.BigInteger("717061938"), countAs(16))
        assertEquals(java.math.BigInteger("434299921440"), countAs(20))
        assertEquals(java.math.BigInteger("67640307007394294146092847"), countAs(40))
    }

    @Test
    fun testFundamentalStandardizesApart() {
        // ?a in the partial and ?a in the complete are unrelated variables.
        val partial = Partial(toFs("Z[h=?a]"), 0, 0, listOf(toFs("Y[f=u, g=?a]")))
        val complete = Complete(toFs("Y[f=?a, g=v]"), 0, 1)
        assertEquals(Complete(toFs("Z[h=v]"), 0, 1), Chart(arrayOf("w")).fundamental(partial, complete))
    }

    @Test
    fun testSpawnStandardizesApart() {
        val rule = com.cbrew.unify.CfgRule(toFs("Z[h=?a]") as com.cbrew.unify.FeatureMap,
                listOf(toFs("Y[f=u, g=?a]") as com.cbrew.unify.FeatureMap), listOf())
        val grammar = FeatureGrammar(Grammar(setOf(rule), mapOf()))
        assertEquals(1, grammar.spawn(Complete(toFs("Y[f=?a, g=v]"), 0, 1)).size)
    }

    @Test
    fun testSolutionsStandardizesApart() {
        val chart = Chart(arrayOf("w"))
        chart.add(Complete(toFs("S[f=?s, g=b]"), 0, 1))
        assertEquals(1, chart.solutions(toFs("S[f=a, g=?s]")).size)
    }

    @Test
    fun testFeatureGrammarNormalizesRules() {
        // A ground but unsimplified semantic term in a rule (the parsers build
        // Not(Not(...)) directly). Substitution now shares ground subterms, so
        // the grammar simplifies its rules once when it is built.
        val lhs = com.cbrew.unify.FeatureMap(mapOf(
                Pair("cat", com.cbrew.unify.AtomicValue("Z")),
                Pair("x", com.cbrew.unify.QueryVariable("?x")),
                Pair("sem", com.cbrew.unify.SemanticValue(
                        com.cbrew.unify.Not(com.cbrew.unify.Not(com.cbrew.unify.Constant("p")))))))
        val rule = com.cbrew.unify.CfgRule(lhs, listOf(toFs("Y[x=?x]") as com.cbrew.unify.FeatureMap), listOf())
        val grammar = FeatureGrammar(Grammar(setOf(rule), mapOf()))
        val complete = Complete(toFs("Y[x=u]"), 0, 1)
        val spawned = grammar.spawn(complete).single() as Partial
        val result = Chart(arrayOf("w")).fundamental(spawned, complete)
        assertEquals(Complete(com.cbrew.unify.FeatureMap(mapOf(
                Pair("cat", com.cbrew.unify.AtomicValue("Z")),
                Pair("x", com.cbrew.unify.AtomicValue("u")),
                Pair("sem", com.cbrew.unify.SemanticValue(com.cbrew.unify.Constant("p"))))), 0, 1), result)
    }

    private val sem2 by lazy {
        FeatureGrammar(IntegratedParser.toGrammar(
                Chart::class.java.getResource("/sem2.fcfg").readText()) as Grammar)
    }

    private fun parseSem2(sentence: String): Chart =
            Chart(sentence.split(" ").toTypedArray()).also { it.parse(sem2) }

    @Test
    fun testSem2() {
        val chart = parseSem2("John sees a dog")
        assertEquals(listOf("<∃.((dog(q:1) ∧ see(john, q:1)))>"),
                chart.solutions().map { (it.category as com.cbrew.unify.FeatureMap)["sem"].toString() })
        assertEquals(0, parseSem2("a dog bark").solutions().size, "number agreement")
        assertEquals(1, parseSem2("Mary walks in Noosa").solutions().size, "in needs a +loc object")
    }

    @Test
    fun testSem2PPAttachment() {
        // Each PP attaches to the verb phrase or to the preceding noun, giving
        // 2^k readings, each with its own semantics (and its own hash code).
        val chart = parseSem2("John sees a dog with a boy with a girl with a dog")
        val sems = chart.solutions().map { (it.category as com.cbrew.unify.FeatureMap)["sem"]!! }
        assertEquals(8, sems.size)
        assertEquals(8, sems.toSet().size)
        assertEquals(8, sems.map { it.hashCode() }.toSet().size)
    }

    @Test
    fun testPredecessorPairsAreDistinct() {
        // predecessors are kept in lists, relying on each pair being formed once
        val charts = listOf(
                Chart(Array(12) { "a" }).also { it.parse(TreeAsFeatureGrammar()) },
                parseSem2("John sees a dog with a boy with a girl with a dog"))
        for (chart in charts) {
            for ((edge, pairs) in chart.predecessors)
                assertEquals(pairs.size, pairs.toSet().size, "duplicate predecessor pair for $edge")
        }
    }
}
