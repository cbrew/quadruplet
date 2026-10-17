package com.cbrew.fstruct.notation

import com.cbrew.unify.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * DRT notation: the `%semantics drt` declaration, boxes, merge and box
 * implication, read with [IntegratedParser.toDrs] and printed with
 * [pretty]. "Book items" are the NLTK book's DRT examples.
 */
class DrtNotationTest {

    private fun drs(s: String): Lambda = IntegratedParser.toDrs(s)

    @Test
    fun testBoxReadAndPrinted() = assertEquals(                     // book item 2
        "([x1, x2], [angus(x1), dog(x2), own(x1, x2)])",
        drs("([x, y], [angus(x), dog(y), own(x, y)])").pretty())

    @Test
    fun testMergeOnReading() = assertEquals(                        // book items 5, 6
        "([x1, x2], [walk(x1), run(x2)])", drs("([x], [walk(x)]) + ([y], [run(y)])").pretty())

    @Test
    fun testMergeWithPronoun() = assertEquals(                      // book item 8
        "([x1, x2, x3, x4], [angus(x1), dog(x2), own(x1, x2), PRO(x3), irene(x4), bite(x3, x4)])",
        drs("([x, y], [angus(x), dog(y), own(x, y)]) + ([u, z], [PRO(u), irene(z), bite(u, z)])").pretty())

    @Test
    fun testImplicationBindsAntecedentReferents() = assertEquals(   // book item 7
        "([], [(([x1], [dog(x1)]) → ([x2], [ankle(x2), bite(x1, x2)]))])",
        drs("([], [(([x], [dog(x)]) -> ([y], [ankle(y), bite(x, y)]))])").pretty())

    @Test
    fun testReferentBindsAcrossMerge() = assertEquals(              // capture through +
        Lam(Drs(1, listOf(App(Constant("Angus"), QVar(1)), App(Var(1), QVar(1))))),
        drs("\\P.(([x], [Angus(x)]) + P(x))"))

    @Test
    fun testNegatedBox() = assertEquals("¬([x1], [dog(x1)])", drs("-([x], [dog(x)])").pretty())

    @Test
    fun testDeclarationSetsSemantics() {
        val g = IntegratedParser.toGrammar("%semantics drt\nN[sem=<\\x.([], [dog(x)])>] -> \"dog\"\n") as Grammar
        assertEquals(Semantics.DRT, g.semantics)
        assertEquals(SemanticValue(Lam(Drs(0, listOf(App(Constant("dog"), Var(1)))))),
                     g.lexicon["dog"]!!.single()["sem"])
        assertEquals(Semantics.LOGIC, (IntegratedParser.toGrammar("N[sem=<dog>] -> \"dog\"") as Grammar).semantics)
    }

    @Test
    fun testUnknownSemanticsRejected() {
        val e = assertFailsWith<IllegalArgumentException> { IntegratedParser.toGrammar("%semantics foo\nN[] -> \"x\"") }
        assertTrue("unknown semantics" in e.message!!, e.message)
    }

    @Test
    fun testLaterMergedBoxesBindOnward() = assertEquals(            // K1 may itself be a merge
        "([x1, x2, x3], [a(x1), b(x2), c(x2, x3)])",
        drs("([x], [a(x)]) + ([y], [b(y)]) + ([z], [c(y, z)])").pretty())

    @Test
    fun testMergedAntecedentBindsConsequent() = assertEquals(
        "([], [(([x1, x2], [a(x1), b(x2)]) → ([], [c(x1, x2)]))])",
        drs("([], [(([x], [a(x)]) + ([y], [b(y)])) -> ([], [c(x, y)])])").pretty())

    @Test
    fun testDuplicateMergedReferentRejected() {
        val e = assertFailsWith<IllegalArgumentException> { drs("([x], [a(x)]) + ([x], [b(x)])") }
        assertTrue("referent x " in e.message!!, e.message)
    }

    @Test
    fun testNonBoxAntecedentStaysImplies() =
        assertEquals(Implies(Constant("p"), Constant("q")), drs("p -> q"))

    @Test
    fun testPlusIsMergeInDrtMode() =
        assertEquals(Drs(0, listOf(Constant("a"), Constant("b"))), drs("a + b"))

    @Test
    fun testExplicitLogicDeclaration() = assertEquals(Semantics.LOGIC,
        (IntegratedParser.toGrammar("%semantics logic\nN[sem=<dog>] -> \"dog\"\n") as Grammar).semantics)

    @Test
    fun testUpperCaseSemanticsNameRejected() {
        val e = assertFailsWith<IllegalArgumentException> { IntegratedParser.toGrammar("%semantics DRT\nN[] -> \"x\"") }
        assertTrue("unknown semantics: DRT" in e.message!!, e.message)
    }

    @Test
    fun testLogicConstantsKeepBrackets() {                          // review focus
        val g = IntegratedParser.toGrammar("N[sem=<f[1](a+b)>] -> \"w\"") as Grammar
        assertEquals(SemanticValue(App(Constant("f[1]"), Constant("a+b"))), g.lexicon["w"]!!.single()["sem"])
    }
}
