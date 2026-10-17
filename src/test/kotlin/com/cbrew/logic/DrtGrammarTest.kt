package com.cbrew.logic

import com.cbrew.chart.Chart
import com.cbrew.chart.FeatureGrammar
import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Discourse readings with drt.fcfg, NLTK's DRT grammar plus discourse rules,
 * and with drt-storage.fcfg, its storage-style version.
 */
class DrtGrammarTest {

    private val drtGrammar by lazy {
        IntegratedParser.toGrammar(
                Chart::class.java.getResource("/drt.fcfg").readText()) as Grammar
    }

    private val drt by lazy { FeatureGrammar(drtGrammar) }

    private fun parse(sentence: String): Chart =
            Chart(sentence.split(" ").toTypedArray()).also { it.parse(drt) }

    private fun readings(sentence: String): List<String> =
            parse(sentence).solutions().flatMap { drtReadings(it.category as FeatureMap) }.map { it.pretty() }

    private val storageGrammar by lazy {
        IntegratedParser.toGrammar(
                Chart::class.java.getResource("/drt-storage.fcfg").readText()) as Grammar
    }

    private val storage by lazy { FeatureGrammar(storageGrammar) }

    private fun parseStorage(sentence: String): Chart =
            Chart(sentence.split(" ").toTypedArray()).also { it.parse(storage) }

    private fun storageThreads(sentence: String): List<String> =
            parseStorage(sentence).solutions().flatMap { drtThreads(it.category as FeatureMap) }.map { it.pretty() }

    private fun storageReadings(sentence: String): List<String> =
            parseStorage(sentence).solutions().flatMap { drtReadings(it.category as FeatureMap) }.map { it.pretty() }

    @Test fun testGrammarIsDrt() = assertEquals(Semantics.DRT, drtGrammar.semantics)

    @Test fun testAngusOwnsADog() = assertEquals(                           // book item 10
        listOf("([x1, x2], [Angus(x1), dog(x2), own(x1, x2)])"),
        readings("Angus owns a dog ."))

    @Test fun testDiscourseResolvesAcrossSentences() = assertEquals(
        listOf("([x1, x2, x3, x4], [Angus(x1), dog(x2), own(x1, x2), (x3 = [x1, x2, x4]), (x4 = [x1, x2, x3]), bite(x3, x4)])"),
        readings("Angus owns a dog . It bites him ."))

    @Test fun testSurfaceScopeOnlyGivesNoReading() =                        // book items 16, 17, plain grammar
        assertEquals(listOf<String>(), readings("Every dog chases a boy . He runs ."))

    @Test fun testLonePronounHasNoReading() =                               // review focus
        assertEquals(listOf<String>(), readings("He runs ."))

    @Test fun testThreadsKeepPronounsUnresolved() =                         // the lone pronoun does parse
        assertEquals(listOf("([x1], [PRO(x1), run(x1)])"),
            parse("He runs .").solutions().flatMap { drtThreads(it.category as FeatureMap) }.map { it.pretty() })

    @Test fun testCategoryWithoutSemIsRejected() {
        assertFailsWith<IllegalArgumentException> { drtThreads(FeatureMap(mapOf())) }
    }

    // ---- the storage-style grammar

    @Test fun testStorageGrammarIsDrt() = assertEquals(Semantics.DRT, storageGrammar.semantics)

    @Test fun testSentsNotation() {           // the pair survives tuple concatenation
        val s = parseStorage("A dog barks . He runs .").solutions().single().category as FeatureMap
        val sents = (s["sents"] as FeatureTuple).elements
        assertEquals(2, sents.size)
        assertTrue(sents.all { it is FeatureTuple && it.elements.size == 2 && it.elements[0] is SemanticValue })
    }

    @Test fun testStorageGivesBothScopings() = assertEquals(                // two threads before filtering
        setOf("([x1, x2], [boy(x1), (([x3], [dog(x3)]) → ([], [chase(x3, x1)])), PRO(x2), run(x2)])",
              "([x1], [(([x2], [dog(x2)]) → ([x3], [boy(x3), chase(x2, x3)])), PRO(x1), run(x1)])"),
        storageThreads("Every dog chases a boy . He runs .").toSet())

    @Test fun testStorageKeepsTheBooksReading() = assertEquals(             // book items 16, 17 (d1)
        listOf("([x1, x2], [boy(x1), (([x3], [dog(x3)]) → ([], [chase(x3, x1)])), (x2 = [x1]), run(x2)])"),
        storageReadings("Every dog chases a boy . He runs ."))

    @Test fun testRetrieveOnDrtTerms() {      // the placeholder ends up bound by the referent
        val aBoy = IntegratedParser.toDrs("\\Q.((([x], []) + boy(x)) + Q(x))")
        val core = Drs(0, listOf(App(App(Constant("chase"), Constant("fido")), FstructVar("@b"))))
        assertEquals(listOf("([x1], [boy(x1), chase(fido, x1)])"),
                     retrieve(core, listOf(App(App(BO, aBoy), FstructVar("@b")))).map { it.pretty() })
    }
}
