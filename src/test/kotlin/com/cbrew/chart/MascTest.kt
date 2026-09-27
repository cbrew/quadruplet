package com.cbrew.chart

import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.FeatureMap
import com.cbrew.unify.Grammar
import com.cbrew.unify.SemanticValue
import com.cbrew.unify.pretty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The MASC benchmark grammar and sample, in src/test/resources/masc.
 * go/testdata/golden/masc.golden records every sentence's chart and readings
 * (see GoldenDumpTest), readings.txt is a correctness suite of sentences
 * whose readings have been checked by hand, and examples.txt has more,
 * from outside the sample. To time the whole sample:
 *
 *   mvn test -Dtest=MascTest -Dmasc.bench=5
 */
class MascTest {
    private val grammar by lazy {
        FeatureGrammar(IntegratedParser.toGrammar(
                Chart::class.java.getResource("/masc/masc.fcfg").readText()) as Grammar)
    }

    private fun resource(name: String) = Chart::class.java.getResource("/masc/$name").readText()

    private val sample by lazy {
        resource("sample.txt").trim().lines().map { it.split("\t")[2].split(" ").toTypedArray() }
    }

    private fun readings(chart: Chart) =
            chart.solutions().filter { (it.category as FeatureMap)["cat"].toString() == "Top" }

    @Test
    fun testCoverage() {
        val parsed = sample.count { words -> readings(Chart(words).also { it.parse(grammar) }).isNotEmpty() }
        assertEquals(299, sample.size)
        assertEquals(284, parsed, "sentences with a reading")
    }

    @Test
    fun testReadings() = checkReadings("readings.txt", 50)

    /** Sentences outside the sample. */
    @Test
    fun testExamples() = checkReadings("examples.txt", 10)

    private fun checkReadings(file: String, atLeast: Int) {
        // sentence -> its readings, as Lambda.pretty() prints them, sorted
        val suite = linkedMapOf<String, MutableList<String>>()
        val intended = mutableMapOf<String, Int>()
        var sentence = ""
        for (line in resource(file).lines()) when {
            line.startsWith("> ") -> { sentence = line.substring(2); suite[sentence] = mutableListOf() }
            line.startsWith("* ") -> { suite.getValue(sentence).add(line.substring(2)); intended.merge(sentence, 1, Int::plus) }
            line.startsWith("  ") -> suite.getValue(sentence).add(line.substring(2))
        }
        assertTrue(suite.size >= atLeast, "sentences in $file")
        for ((s, want) in suite) {
            assertEquals(1, intended[s], "$s: readings marked intended")
            val chart = Chart(s.split(" ").toTypedArray()).also { it.parse(grammar) }
            val got = readings(chart).map { ((it.category as FeatureMap)["sem"] as SemanticValue).value.pretty() }.sorted()
            assertEquals(want, got, s)
        }
    }

    @Test
    fun benchmark() {
        val runs = System.getProperty("masc.bench")?.toInt() ?: return
        repeat(runs) {
            val start = System.nanoTime()
            for (words in sample) Chart(words).parse(grammar)
            println("MASC sample: %.0f ms".format((System.nanoTime() - start) / 1e6))
        }
    }
}
