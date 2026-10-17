package com.cbrew.fstruct.notation

import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.atn.PredictionMode
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * IntegratedParser parses a grammar with SLL prediction first, falling back
 * to full LL only on a syntax error. Every grammar among the test resources
 * should parse under SLL alone, to the same tree as full LL gives.
 */
class TwoStageParseTest {
    private val grammars = File(javaClass.getResource("/masc/masc.fcfg").toURI())
            .parentFile.parentFile.walk().filter { it.name.endsWith(".fcfg") }.toList()

    private fun tree(text: String, mode: PredictionMode): String {
        val parser = FeatParser(CommonTokenStream(FeatLexer(CharStreams.fromString(text))))
        parser.interpreter.predictionMode = mode
        if (mode == PredictionMode.SLL) {
            parser.removeErrorListeners()
            parser.errorHandler = BailErrorStrategy()
        }
        return parser.cfg().toStringTree(parser)
    }

    @Test
    fun testSllTreeIsLlTree() {
        assertTrue(grammars.size >= 9, "found only $grammars")
        for (g in grammars) {
            val text = g.readText()
            assertEquals(tree(text, PredictionMode.LL), tree(text, PredictionMode.SLL), g.name)
        }
    }

    @Test
    fun testSyntaxErrorStillRepaired() {
        // SLL bails at the stray ')'; full LL then reports and repairs it,
        // giving the tree full LL alone gives.
        val text = "S -> NP VP )\nNP -> Det N"
        assertEquals(tree(text, PredictionMode.LL), IntegratedParser.toTree(text))
    }
}
