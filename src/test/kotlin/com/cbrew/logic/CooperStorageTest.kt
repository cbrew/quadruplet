package com.cbrew.logic

import com.cbrew.chart.Chart
import com.cbrew.chart.FeatureGrammar
import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Cooper storage with storage.fcfg, NLTK's storage grammar with its
 * SEM=[CORE=..., STORE=...] flattened into two features, core and store.
 */
class CooperStorageTest {

    private val grammar by lazy {
        IntegratedParser.toGrammar(
                Chart::class.java.getResource("/storage.fcfg").readText()) as Grammar
    }

    private val storage by lazy { FeatureGrammar(grammar) }

    private fun parse(sentence: String): Chart =
            Chart(sentence.split(" ").toTypedArray()).also { it.parse(storage) }

    private fun bo(q: Lambda, x: String): Lambda = App(App(Constant("bo"), q), FstructVar(x))

    // ---- the notation reads the storage style

    @Test
    fun testEmptyStoreNotation() {
        val a = grammar.lexicon["a"]!!.single()
        assertEquals(FeatureTuple(listOf()), a["store"])
    }

    @Test
    fun testPlaceholderAndStoreExpression() {
        val np = grammar.rules.map { it as CfgRule }
                .single { it.lhs["cat"] == AtomicValue("NP") }
        assertEquals(SemanticValue(FstructVar("@x")), np.lhs["core"])
        val det = App(FstructVar("?det"), FstructVar("?n"))
        assertEquals(FeatureTupleExpression(listOf(
                FeatureTuple(listOf(SemanticValue(bo(det, "@x")))),
                QueryVariable("?b1"),
                QueryVariable("?b2"))),
                np.lhs["store"])
    }

    @Test
    fun testCompleteSentenceKeepsPlaceholdersFree() {
        val s = parse("every girl chases a dog").solutions().single().category as FeatureMap
        val core = (s["core"] as SemanticValue).value
        val store = (s["store"] as FeatureTuple).elements.map { (it as SemanticValue).value }
        assertEquals(2, store.size)
        val placeholders = store.map { term ->
            assertTrue(term is App && term.e1 is App && (term.e1 as App).e1 == Constant("bo"), "$term")
            term.e2 as FstructVar
        }
        assertNotEquals(placeholders[0], placeholders[1])
        val free = mutableSetOf<Unifiable>().also { findVariables(core, it) }
        assertEquals<Set<Unifiable>>(placeholders.toSet(), free)
    }

    // ---- retrieval

    private fun readings(sentence: String): List<String> =
            retrieve(parse(sentence).solutions().single().category as FeatureMap).map { it.pretty() }

    @Test
    fun testEveryGirlChasesADog() {
        // The store is (every girl, a dog). Taken in that order, every girl is
        // retrieved first, so it has narrow scope: the inverse reading comes
        // first, as in the NLTK book.
        assertEquals(listOf(
                "∃x1.(dog(x1) ∧ ∀x2.(girl(x2) → chase(x2, x1)))",
                "∀x1.(girl(x1) → ∃x2.(chase(x1, x2) ∧ dog(x2)))"),
                readings("every girl chases a dog"))
    }

    @Test
    fun testCyrilSmiles() {
        assertEquals(listOf("smile(cyril)"), readings("Cyril smiles"))
    }

    @Test
    fun testAngusGivesABoneToEveryMan() {
        // Store (Angus, a bone, every man): 3! = 6 orders. Where the name
        // goes does not matter, so there are two scopings, each three times.
        val everyOutside = "∀x1.(man(x1) → ∃x2.(bone(x2) ∧ give(angus, x2, x1)))"
        val aOutside = "∃x1.(bone(x1) ∧ ∀x2.(man(x2) → give(angus, x1, x2)))"
        val all = readings("Angus gives a bone to every man")
        assertEquals(listOf(everyOutside, aOutside, everyOutside, everyOutside, aOutside, aOutside), all)
        assertEquals(listOf(everyOutside, aOutside), all.distinct())
    }

    @Test
    fun testRetrieveDistinct() {
        val s = parse("Angus gives a bone to every man").solutions().single().category as FeatureMap
        val core = (s["core"] as SemanticValue).value
        val store = (s["store"] as FeatureTuple).elements.map { (it as SemanticValue).value }
        assertEquals(2, retrieveDistinct(core, store).size)
    }

    @Test
    fun testRetrieveFromEmptyStore() {
        // nothing to retrieve: one reading, the core
        val core = App(Constant("walk"), Constant("angus"))
        assertEquals(listOf<Lambda>(core), retrieve(core, listOf()))
    }

    @Test
    fun testRejectsNonOperator() {
        val e = assertFailsWith<IllegalArgumentException> {
            retrieve(FstructVar("@x"), listOf(App(Constant("dog"), FstructVar("@x"))))
        }
        assertTrue("bo(Q, @x)" in e.message!!, e.message)
    }

    @Test
    fun testPermutationOrder() {
        assertEquals(listOf(listOf(1, 2, 3), listOf(1, 3, 2), listOf(2, 1, 3),
                listOf(2, 3, 1), listOf(3, 1, 2), listOf(3, 2, 1)),
                permutations(listOf(1, 2, 3)))
    }
}
