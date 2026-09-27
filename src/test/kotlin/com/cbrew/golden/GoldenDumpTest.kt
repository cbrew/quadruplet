package com.cbrew.golden

import com.cbrew.chart.*
import com.cbrew.fstruct.notation.FeatureNotation
import com.cbrew.fstruct.notation.FeatureNotationVisitor
import com.cbrew.fstruct.notation.FeatureTermsLexer
import com.cbrew.fstruct.notation.FeatureTermsParser
import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.logic.notation.LogicTermsLexer
import com.cbrew.logic.notation.LogicTermsParser
import com.cbrew.logic.notation.SemanticVisitor
import com.cbrew.unify.*
import java.io.File
import org.antlr.v4.runtime.*
import kotlin.test.Test

/**
 * Writes golden files that the Go port's tests compare against. Does nothing
 * unless run with -Dgolden.dir=<dir>, for example:
 *
 *   mvn test -Dtest=GoldenDumpTest -Dgolden.dir=$PWD/go/testdata/golden
 *
 * Inputs are read from <dir>/NAME.in and outputs written to <dir>/NAME.golden.
 * Inputs the ANTLR parsers reject, or do not consume entirely, are recorded
 * as ERROR, since the Go parsers are strict where ANTLR recovers.
 */
class GoldenDumpTest {

    private val resources = "src/test/resources"

    private class Errors : BaseErrorListener() {
        var seen = false
        override fun syntaxError(r: Recognizer<*, *>?, s: Any?, line: Int, pos: Int, msg: String?, e: RecognitionException?) {
            seen = true
        }
    }

    // Box and Empty are objects whose default toString includes a hash code
    private fun show(x: Any): String =
            x.toString().replace(Regex("com\\.cbrew\\.unify\\.Box@[0-9a-f]+"), "☐")
                    .replace(Regex("com\\.cbrew\\.unify\\.Empty@[0-9a-f]+"), "∅")

    private fun lines(f: File): List<String> = f.readText().removeSuffix("\n").split("\n")

    private fun logic(s: String): Lambda? {
        val errors = Errors()
        val lexer = LogicTermsLexer(CharStreams.fromString(s)).apply { removeErrorListeners(); addErrorListener(errors) }
        val parser = LogicTermsParser(CommonTokenStream(lexer)).apply { removeErrorListeners(); addErrorListener(errors) }
        val tree = parser.expression()
        if (errors.seen || parser.currentToken.type != Token.EOF) return null
        return try { SemanticVisitor().let { it.binarize(it.visit(tree)) } } catch (e: Exception) { null }
    }

    private fun fs(s: String): FeatureStructure? {
        val errors = Errors()
        val lexer = FeatureTermsLexer(CharStreams.fromString(s)).apply { removeErrorListeners(); addErrorListener(errors) }
        val parser = FeatureTermsParser(CommonTokenStream(lexer)).apply { removeErrorListeners(); addErrorListener(errors) }
        val tree = parser.expression()
        if (errors.seen || parser.currentToken.type != Token.EOF) return null
        return try { FeatureNotationVisitor().visit(tree) } catch (e: Exception) { null }
    }

    private fun grammar(file: String, notation: String): Grammar {
        val text = File("$resources/$file").readText()
        return if (notation == "features") FeatureNotation.toGrammar(text)
        else IntegratedParser.toGrammar(text) as Grammar
    }

    @Test
    fun dump() {
        val dir = File(System.getProperty("golden.dir") ?: return)

        File(dir, "logic.golden").printWriter().use { out ->
            for (s in lines(File(dir, "logic.in"))) {
                val t = logic(s)
                out.println(if (t == null) "$s\tERROR" else "$s\t${show(t)}\t${show(t.normalized())}")
            }
        }

        File(dir, "fs.golden").printWriter().use { out ->
            for (s in lines(File(dir, "fs.in"))) {
                val t = fs(s)
                out.println(if (t == null) "$s\tERROR" else "$s\t${show(t)}")
            }
        }

        File(dir, "unify.golden").printWriter().use { out ->
            for (line in lines(File(dir, "unify.in"))) {
                val (a, b) = line.split("\t")
                val r = fs(a)!!.unify(fs(b)!!)
                out.println("$a\t$b\t${if (r == null) "FAIL" else show(r)}")
            }
        }

        File(dir, "grammar.golden").printWriter().use { out ->
            for ((file, notation) in listOf("demo.fcfg" to "features", "patio.fcfg" to "integrated",
                    "sem2.fcfg" to "integrated", "tiny.cfg" to "integrated", "tiny2.cfg" to "integrated",
                    "alternatives.fcfg" to "integrated")) {
                val g = grammar(file, notation)
                for (r in g.rules) out.println("$file\trule\t${show(r)}\t${show((r as Unifiable).normalized())}")
                for ((word, entries) in g.lexicon)
                    for (e in entries) out.println("$file\tlex\t$word\t${show(e)}\t${show(e.normalized())}")
            }
        }

        File(dir, "parse.golden").printWriter().use { out ->
            for (line in lines(File(dir, "parse.in"))) {
                val (file, notation, sentence) = line.split("\t")
                val chart = Chart(sentence.split(" ").toTypedArray())
                chart.parse(FeatureGrammar(grammar(file, notation)))
                val stats = chart.stats()
                out.println("$file\t$sentence\tstats\t${stats["numCompletes"]}\t${stats["numPartials"]}\t" +
                        "${stats["numSolutions"]}\t${stats["numTrees"]}")
                for (s in chart.solutions().map { show(it.category) }.sorted())
                    out.println("$file\t$sentence\tsolution\t$s")
                val trees = chart.solutions().flatMap { chart.getTrees(it).take(20).toList() }
                        .map { show(it.treestring(0)).replace("\n", "|") }.sorted()
                for (t in trees) out.println("$file\t$sentence\ttree\t$t")
            }
        }

        // The MASC benchmark: each sentence's chart size, trees and readings
        // of the start category Top.
        File(dir, "masc.golden").printWriter().use { out ->
            val g = FeatureGrammar(grammar("masc/masc.fcfg", "integrated"))
            for (line in lines(File("$resources/masc/sample.txt"))) {
                val (id, _, sentence) = line.split("\t")
                val chart = Chart(sentence.split(" ").toTypedArray())
                chart.parse(g)
                val stats = chart.stats()
                val top = chart.solutions().filter { (it.category as FeatureMap)["cat"].toString() == "Top" }
                val trees = top.fold(java.math.BigInteger.ZERO) { n, e -> n + chart.countTrees(e) }
                out.println("$id\tstats\t${stats["numCompletes"]}\t${stats["numPartials"]}\t${top.size}\t$trees")
                for (s in top.map { show(it.category) }.sorted()) out.println("$id\treading\t$s")
            }
        }

        File(dir, "treeas.golden").printWriter().use { out ->
            for (n in 1..20) {
                val chart = Chart(Array(n) { "a" })
                chart.parse(TreeAsFeatureGrammar())
                val stats = chart.stats()
                out.println("$n\t${stats["numCompletes"]}\t${stats["numPartials"]}\t${stats["numTrees"]}")
            }
        }
    }
}
