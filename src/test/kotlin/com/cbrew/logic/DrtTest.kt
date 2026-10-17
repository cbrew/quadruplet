package com.cbrew.logic

import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/** Discourse representation theory: translation of DRSs to first-order logic. */
class DrtTest {

    private fun drs(s: String): Lambda = IntegratedParser.toDrs(s)

    @Test
    fun testFolOfBox() = assertEquals(
            "∃x1.∃x2.(angus(x1) ∧ dog(x2) ∧ own(x1, x2))",
            drs("([x, y], [angus(x), dog(y), own(x, y)])").fol().pretty())

    @Test
    fun testFolOfImplication() = assertEquals(
            "∀x1.(dog(x1) → ∃x2.(ankle(x2) ∧ bite(x1, x2)))",
            drs("([], [(([x], [dog(x)]) -> ([y], [ankle(y), bite(x, y)]))])").fol().pretty())

    @Test
    fun testFolOfEmptyBoxes() {
        assertEquals(TRUE, drs("([], [])").fol())
        assertEquals(TRUE, drs("([x], [])").fol())
    }

    @Test
    fun testFolOfResolved() = assertEquals(
            "∃x1.∃x2.(x2 = x1)",
            Drs(2, listOf(Resolved(QVar(1), listOf(QVar(2))))).fol().pretty())

    @Test
    fun testFolOfResolvedWithoutCandidates() {
        assertFailsWith<IllegalArgumentException> { Resolved(QVar(1), emptyList()).fol() }
    }

    @Test
    fun testResolveLists() {
        assertEquals(
                "([x1, x2, x3, x4], [angus(x1), dog(x2), own(x1, x2), (x3 = [x1, x2, x4]), irene(x4), bite(x3, x4)])",
                resolveAnaphora(drs("([x, y], [angus(x), dog(y), own(x, y)]) + " +
                        "([u, z], [PRO(u), irene(z), bite(u, z)])"))!!.pretty())
    }

    @Test
    fun testImplicationReferentsInaccessible() = assertNull(
            resolveAnaphora(drs("([u], [(([x], [dog(x)]) -> ([y], [boy(y), chase(x, y)])), PRO(u), run(u)])")))

    @Test
    fun testPronounInsideNegationSeesOuter() = assertEquals(
            "([x1], [angus(x1), ¬([x2], [(x2 = [x1]), walk(x2)])])",
            resolveAnaphora(drs("([x], [angus(x), -([y], [PRO(y), walk(y)])])"))!!.pretty())

    @Test
    fun testReferentInsideNegationInaccessible() = assertNull(
            resolveAnaphora(drs("([y], [-([x], [dog(x)]), PRO(y), run(y)])")))
}
