package com.cbrew.chart

import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.FeatureMap
import com.cbrew.unify.Grammar
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The MASC benchmark grammar and sample, in src/test/resources/masc.
 * go/testdata/golden/masc.golden records every sentence's chart and readings
 * (see GoldenDumpTest). To time the whole sample:
 *
 *   mvn test -Dtest=MascTest -Dmasc.bench=5
 */
class MascTest {
    private val grammar by lazy {
        FeatureGrammar(IntegratedParser.toGrammar(
                Chart::class.java.getResource("/masc/masc.fcfg").readText()) as Grammar)
    }

    private val sample by lazy {
        Chart::class.java.getResource("/masc/sample.txt").readText().trim().lines()
                .map { it.split("\t")[2].split(" ").toTypedArray() }
    }

    private fun readings(chart: Chart) =
            chart.solutions().filter { (it.category as FeatureMap)["cat"].toString() == "Top" }

    @Test
    fun testCoverage() {
        val parsed = sample.count { words -> readings(Chart(words).also { it.parse(grammar) }).isNotEmpty() }
        assertEquals(299, sample.size)
        assertEquals(279, parsed, "sentences with a reading")
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
