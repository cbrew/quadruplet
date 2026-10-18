package com.cbrew.logic

import com.cbrew.chart.Chart
import com.cbrew.chart.FeatureGrammar
import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * bb2.fcfg, Blackburn and Bos's BB2 grammar with its lambda-DRT lexicon,
 * on BB2's sentence test suite (sentenceTestSuite.pl).
 *
 * The suite gives each sentence a number of readings. BB2 does not use it
 * itself: its lambda-DRT driver runs lambdaTestSuite.pl, the same sentences
 * without the questions, with the same numbers. The numbers count scopings,
 * as in BB2's first-order predecessor, where the suite tested Cooper
 * storage and hole semantics; lambda-DRT, BB2's and this grammar's, gives
 * one reading per parse, with every quantifier in its surface position,
 * and each sentence has one parse. So the 30 sentences the suite gives one
 * reading have the suite's number, and the 15 it gives more have one.
 *
 * Each reading is also checked against BB2. For the sentences marked BB2,
 * the expected box is BB2's own output (SWI-Prolog, lambdaDRT/2), printed
 * the way quadruplet prints boxes. BB2 finds no reading for the sentences
 * marked REPAIRED: wh-questions (no semantics in BB2's lambda-DRT
 * lexicon), coordinated verb phrases and the copula (BB2's entries for
 * them do not fit its event semantics); their boxes come from the repaired
 * and added entries described in bb2.fcfg.
 */
class Bb2GrammarTest {

    private val bb2Grammar by lazy {
        IntegratedParser.toGrammar(Chart::class.java.getResource("/bb2.fcfg").readText()) as Grammar
    }

    private val bb2 by lazy { FeatureGrammar(bb2Grammar) }

    /** The readings of a text: the semantics of each complete T edge. */
    private fun readings(text: String): List<String> =
            Chart(text.split(" ").toTypedArray()).also { it.parse(bb2) }.solutions()
                    .map { it.category as FeatureMap }
                    .filter { it["cat"] == AtomicValue("T") }
                    .flatMap(::drtReadings)
                    .map { it.pretty() }

    private enum class Source { BB2, REPAIRED }

    /** A sentence of the suite, the suite's number of readings, and the expected reading. */
    private data class Case(val sentence: String, val suite: Int, val source: Source, val drs: String)

    private val cases = listOf(
        Case("a man walks", 1, Source.BB2,
             "([x1, x2], [man(x1), walk(x2), agent(x2, x1), event(x2)])"),
        Case("mia dances", 1, Source.BB2,
             "([x1, x2], [mia(x1), dance(x2), agent(x2, x1), event(x2)])"),
        Case("if vincent dances then mia dances", 1, Source.BB2,
             "([], [(([x1, x2], [vincent(x1), dance(x2), agent(x2, x1), event(x2)]) → ([x3, x4], [mia(x3), dance(x4), agent(x4, x3), event(x4)]))])"),
        Case("who dances", 1, Source.REPAIRED,
             "([x1, x2], [person(x1), wh(x1), dance(x2), agent(x2, x1), event(x2)])"),
        Case("which robber dies", 1, Source.REPAIRED,
             "([x1, x2], [wh(x1), robber(x1), die(x2), agent(x2, x1), event(x2)])"),
        Case("mia or vincent dances", 1, Source.BB2,
             "([], [(([x1, x2], [mia(x1), dance(x2), agent(x2, x1), event(x2)]) ∨ ([x1, x2], [vincent(x1), dance(x2), agent(x2, x1), event(x2)]))])"),
        Case("every customer smokes", 1, Source.BB2,
             "([], [(([x1], [customer(x1)]) → ([x2], [smoke(x2), agent(x2, x1), event(x2)]))])"),
        Case("a customer smokes", 1, Source.BB2,
             "([x1, x2], [customer(x1), smoke(x2), agent(x2, x1), event(x2)])"),
        Case("mia or a man dances", 1, Source.BB2,
             "([], [(([x1, x2], [man(x1), dance(x2), agent(x2, x1), event(x2)]) ∨ ([x1, x2], [mia(x1), dance(x2), agent(x2, x1), event(x2)]))])"),
        Case("every woman or a man dances", 2, Source.BB2,
             "([], [(([], [(([x1], [woman(x1)]) → ([x2], [dance(x2), agent(x2, x1), event(x2)]))]) ∨ ([x1, x2], [man(x1), dance(x2), agent(x2, x1), event(x2)]))])"),
        Case("every man or woman dances", 1, Source.BB2,
             "([], [(([x1], [(([], [man(x1)]) ∨ ([], [woman(x1)]))]) → ([x2], [dance(x2), agent(x2, x1), event(x2)]))])"),
        Case("every man that dances smokes", 1, Source.BB2,
             "([], [(([x1, x2], [dance(x2), agent(x2, x1), event(x2), man(x1)]) → ([x3], [smoke(x3), agent(x3, x1), event(x3)]))])"),
        Case("every customer in a restaurant smokes", 2, Source.BB2,
             "([], [(([x1, x2], [restaurant(x2), in(x1, x2), customer(x1)]) → ([x3], [smoke(x3), agent(x3, x1), event(x3)]))])"),
        Case("mia knows a man", 1, Source.BB2,
             "([x1, x2, x3], [mia(x1), man(x2), know(x3), agent(x3, x1), patient(x3, x2), event(x3)])"),
        Case("if butch shoots vincent then vincent dies", 1, Source.BB2,
             "([], [(([x1, x2, x3], [butch(x1), vincent(x2), shoot(x3), agent(x3, x1), patient(x3, x2), event(x3)]) → ([x4, x5], [vincent(x4), die(x5), agent(x5, x4), event(x5)]))])"),
        Case("who likes mia", 1, Source.REPAIRED,
             "([x1, x2, x3], [person(x1), wh(x1), mia(x2), like(x3), agent(x3, x1), patient(x3, x2), event(x3)])"),
        Case("which boxer shoots vincent", 1, Source.REPAIRED,
             "([x1, x2, x3], [wh(x1), boxer(x1), vincent(x2), shoot(x3), agent(x3, x1), patient(x3, x2), event(x3)])"),
        Case("which boxer shoots a criminal", 1, Source.REPAIRED,
             "([x1, x2, x3], [wh(x1), boxer(x1), criminal(x2), shoot(x3), agent(x3, x1), patient(x3, x2), event(x3)])"),
        Case("mia or vincent eats a quarter pounder with cheese", 2, Source.BB2,
             "([], [(([x1, x2, x3], [mia(x1), qpwc(x2), eat(x3), agent(x3, x1), patient(x3, x2), event(x3)]) ∨ ([x1, x2, x3], [vincent(x1), qpwc(x2), eat(x3), agent(x3, x1), patient(x3, x2), event(x3)]))])"),
        Case("every customer drinks a five dollar shake", 2, Source.BB2,
             "([], [(([x1], [customer(x1)]) → ([x2, x3], [fdshake(x2), drink(x3), agent(x3, x1), patient(x3, x2), event(x3)]))])"),
        Case("a customer knows mia or a man", 2, Source.BB2,
             "([x1], [customer(x1), (([x2, x3], [man(x2), know(x3), agent(x3, x1), patient(x3, x2), event(x3)]) ∨ ([x2, x3], [mia(x2), know(x3), agent(x3, x1), patient(x3, x2), event(x3)]))])"),
        Case("vincent knows every woman or a man", 2, Source.BB2,
             "([x1], [vincent(x1), (([], [(([x2], [woman(x2)]) → ([x3], [know(x3), agent(x3, x1), patient(x3, x2), event(x3)]))]) ∨ ([x2, x3], [man(x2), know(x3), agent(x3, x1), patient(x3, x2), event(x3)]))])"),
        Case("vincent knows every man or woman", 1, Source.BB2,
             "([x1], [vincent(x1), (([x2], [(([], [man(x2)]) ∨ ([], [woman(x2)]))]) → ([x3], [know(x3), agent(x3, x1), patient(x3, x2), event(x3)]))])"),
        Case("mia dates every man that dances", 1, Source.BB2,
             "([x1], [mia(x1), (([x2, x3], [dance(x3), agent(x3, x2), event(x3), man(x2)]) → ([x4], [date(x4), agent(x4, x1), patient(x4, x2), event(x4)]))])"),
        Case("a robber likes every customer in a restaurant", 5, Source.BB2,
             "([x1], [robber(x1), (([x2, x3], [restaurant(x3), in(x2, x3), customer(x2)]) → ([x4], [like(x4), agent(x4, x1), patient(x4, x2), event(x4)]))])"),
        Case("butch growls or dies", 1, Source.REPAIRED,
             "([x1], [butch(x1), (([x2], [die(x2), agent(x2, x1), event(x2)]) ∨ ([x2], [growl(x2), agent(x2, x1), event(x2)]))])"),
        Case("every boxer growls or dies", 1, Source.REPAIRED,
             "([], [(([x1], [boxer(x1)]) → ([], [(([x2], [die(x2), agent(x2, x1), event(x2)]) ∨ ([x2], [growl(x2), agent(x2, x1), event(x2)]))]))])"),
        Case("butch kills a criminal or dies", 1, Source.REPAIRED,
             "([x1], [butch(x1), (([x2, x3], [criminal(x2), kill(x3), agent(x3, x1), patient(x3, x2), event(x3)]) ∨ ([x2], [die(x2), agent(x2, x1), event(x2)]))])"),
        Case("butch kills a criminal or shoots vincent", 1, Source.REPAIRED,
             "([x1], [butch(x1), (([x2, x3], [criminal(x2), kill(x3), agent(x3, x1), patient(x3, x2), event(x3)]) ∨ ([x2, x3], [vincent(x2), shoot(x3), agent(x3, x1), patient(x3, x2), event(x3)]))])"),
        Case("butch kills a criminal or shoots a criminal", 2, Source.REPAIRED,
             "([x1], [butch(x1), (([x2, x3], [criminal(x2), kill(x3), agent(x3, x1), patient(x3, x2), event(x3)]) ∨ ([x2, x3], [criminal(x2), shoot(x3), agent(x3, x1), patient(x3, x2), event(x3)]))])"),
        Case("butch is a boxer", 1, Source.REPAIRED,
             "([x1, x2], [butch(x1), boxer(x2), (x1 = x2)])"),
        Case("butch is not vincent", 1, Source.REPAIRED,
             "([x1], [butch(x1), ¬([x2], [vincent(x2), (x1 = x2)])])"),
        Case("butch does not die", 1, Source.BB2,
             "([], [¬([x1, x2], [butch(x1), die(x2), agent(x2, x1), event(x2)])])"),
        Case("butch does die", 1, Source.BB2,
             "([x1, x2], [butch(x1), die(x2), agent(x2, x1), event(x2)])"),
        Case("a boxer does not die", 2, Source.BB2,
             "([], [¬([x1, x2], [boxer(x1), die(x2), agent(x2, x1), event(x2)])])"),
        Case("every boxer does not die", 2, Source.BB2,
             "([], [¬([], [(([x1], [boxer(x1)]) → ([x2], [die(x2), agent(x2, x1), event(x2)]))])])"),
        Case("vincent knows mia or does not dance", 1, Source.REPAIRED,
             "([x1], [vincent(x1), (([], [¬([x2], [dance(x2), agent(x2, x1), event(x2)])]) ∨ ([x2, x3], [mia(x2), know(x3), agent(x3, x1), patient(x3, x2), event(x3)]))])"),
        Case("vincent does not smoke or dance", 1, Source.REPAIRED,
             "([], [¬([x1], [vincent(x1), (([x2], [dance(x2), agent(x2, x1), event(x2)]) ∨ ([x2], [smoke(x2), agent(x2, x1), event(x2)]))])])"),
        Case("a man does not smoke or dance", 2, Source.REPAIRED,
             "([], [¬([x1], [man(x1), (([x2], [dance(x2), agent(x2, x1), event(x2)]) ∨ ([x2], [smoke(x2), agent(x2, x1), event(x2)]))])])"),
        Case("every customer in a restaurant eats a big kahuna burger", 5, Source.BB2,
             "([], [(([x1, x2], [restaurant(x2), in(x1, x2), customer(x1)]) → ([x3, x4], [big(x3), kahuna(x3), burger(x3), eat(x4), agent(x4, x1), patient(x4, x3), event(x4)]))])"),
        Case("every customer in a restaurant does not eat a big kahuna burger", 18, Source.BB2,
             "([], [¬([], [(([x1, x2], [restaurant(x2), in(x1, x2), customer(x1)]) → ([x3, x4], [big(x3), kahuna(x3), burger(x3), eat(x4), agent(x4, x1), patient(x4, x3), event(x4)]))])])"),
        Case("every man in a restaurant knows a woman with a car", 14, Source.BB2,
             "([], [(([x1, x2], [restaurant(x2), in(x1, x2), man(x1)]) → ([x3, x4, x5], [car(x4), with(x3, x4), woman(x3), know(x5), agent(x5, x1), patient(x5, x3), event(x5)]))])"),
        Case("if every man knows a woman then every woman knows a man", 4, Source.BB2,
             "([], [(([], [(([x1], [man(x1)]) → ([x2, x3], [woman(x2), know(x3), agent(x3, x1), patient(x3, x2), event(x3)]))]) → ([], [(([x1], [woman(x1)]) → ([x2, x3], [man(x2), know(x3), agent(x3, x1), patient(x3, x2), event(x3)]))]))])"),
        Case("vincent eats a big kahuna burger", 1, Source.BB2,
             "([x1, x2, x3], [vincent(x1), big(x2), kahuna(x2), burger(x2), eat(x3), agent(x3, x1), patient(x3, x2), event(x3)])"),
        Case("either vincent eats a big kahuna burger or jules smokes", 1, Source.BB2,
             "([], [(([x1, x2, x3], [vincent(x1), big(x2), kahuna(x2), burger(x2), eat(x3), agent(x3, x1), patient(x3, x2), event(x3)]) ∨ ([x1, x2], [jules(x1), smoke(x2), agent(x2, x1), event(x2)]))])"),
    )

    @Test fun testGrammarIsDrt() = assertEquals(Semantics.DRT, bb2Grammar.semantics)

    @Test fun testSuiteHas45Sentences() = assertEquals(45, cases.size)

    @Test fun testEachSentenceHasItsReading() {
        val wrong = cases.filter { readings(it.sentence) != listOf(it.drs) }
        assertEquals(listOf(), wrong.map { "${it.sentence}: ${readings(it.sentence)}" })
    }

    @Test fun testOneReadingCountsMatchTheSuite() {
        val matching = cases.filter { it.suite == 1 }
        assertEquals(30, matching.size)
        assertEquals(matching.map { it.sentence to 1 }, matching.map { it.sentence to readings(it.sentence).size })
    }

    // The suite counts scopings; lambda-DRT has surface scope only.
    @Test fun testScopeAmbiguousSentencesHaveOneReading() {
        val scoped = cases.filter { it.suite > 1 }
        assertEquals(
            listOf("every woman or a man dances" to 2,
                   "every customer in a restaurant smokes" to 2,
                   "mia or vincent eats a quarter pounder with cheese" to 2,
                   "every customer drinks a five dollar shake" to 2,
                   "a customer knows mia or a man" to 2,
                   "vincent knows every woman or a man" to 2,
                   "a robber likes every customer in a restaurant" to 5,
                   "butch kills a criminal or shoots a criminal" to 2,
                   "a boxer does not die" to 2,
                   "every boxer does not die" to 2,
                   "a man does not smoke or dance" to 2,
                   "every customer in a restaurant eats a big kahuna burger" to 5,
                   "every customer in a restaurant does not eat a big kahuna burger" to 18,
                   "every man in a restaurant knows a woman with a car" to 14,
                   "if every man knows a woman then every woman knows a man" to 4),
            scoped.map { it.sentence to it.suite })
        assertEquals(scoped.map { 1 }, scoped.map { readings(it.sentence).size })
    }

    @Test fun testBb2GivesTwoThirdsOfTheBoxes() =
        assertEquals(30, cases.count { it.source == Source.BB2 })

    // ---- beyond the suite

    @Test fun testTextMergesSentences() = assertEquals(            // BB2's t -> s t, no punctuation
        listOf("([x1, x2, x3, x4], [mia(x1), dance(x2), agent(x2, x1), event(x2), " +
               "vincent(x3), smoke(x4), agent(x4, x3), event(x4)])"),
        readings("mia dances vincent smokes"))

    @Test fun testItIsNotTheCaseThat() = assertEquals(              // repaired
        listOf("([], [¬([x1, x2], [mia(x1), dance(x2), agent(x2, x1), event(x2)])])"),
        readings("it is not the case that mia dances"))

    @Test fun testCoordinatedSubjectsHaveTwoEvents() = assertEquals( // BB2, referents in another order
        listOf("([x1, x2, x3, x4], [mia(x1), dance(x3), agent(x3, x1), event(x3), " +
               "vincent(x2), dance(x4), agent(x4, x2), event(x4)])"),
        readings("mia and vincent dance"))

    @Test fun testAdverbAndPpModifyTheEvent() {                     // BB2
        assertEquals(listOf("([x1, x2], [vincent(x1), dance(x2), agent(x2, x1), quick(x2), event(x2)])"),
                     readings("vincent dances quickly"))
        assertEquals(listOf("([x1, x2, x3], [vincent(x1), walk(x2), agent(x2, x1), restaurant(x3), " +
                            "in(x2, x3), event(x2)])"),
                     readings("vincent walks in a restaurant"))
    }

    @Test fun testObjectQuestionFillsTheGap() {                     // added
        assertEquals(listOf("([x1, x2, x3], [mia(x1), person(x2), wh(x2), like(x3), agent(x3, x1), " +
                            "patient(x3, x2), event(x3)])"),
                     readings("who does mia like"))
        assertEquals(listOf("([x1, x2, x3], [vincent(x1), thing(x2), wh(x2), eat(x3), agent(x3, x1), " +
                            "patient(x3, x2), event(x3)])"),
                     readings("what does vincent eat"))
    }

    @Test fun testMultiWordEntries() = assertEquals(                // BB2
        listOf("([x1, x2, x3], [mia(x1), gun(x2), pickup(x3), agent(x3, x1), patient(x3, x2), event(x3)])"),
        readings("mia picks up a gun"))

    @Test fun testAgreement() {
        assertEquals(listOf(), readings("mia dance"))
        assertEquals(listOf(), readings("a man walk"))
        assertEquals(listOf(), readings("mia and vincent dances"))
        assertEquals(1, readings("mia and vincent dance").size)
    }

    @Test fun testLeftOutWordsGiveNoReading() {                     // no lambda-DRT semantics in BB2
        assertEquals(listOf(), readings("the boxer dies"))
        assertEquals(listOf(), readings("he dies"))
        assertEquals(listOf(), readings("two boxers die"))
    }

    @Test fun testFol() = assertEquals(
        "∀x1.(customer(x1) → ∃x2.(agent(x2, x1) ∧ event(x2) ∧ smoke(x2)))",
        IntegratedParser.toDrs("([], [(([x], [customer(x)]) -> ([e], [smoke(e), agent(e, x), event(e)]))])")
                .fol().pretty())
}
