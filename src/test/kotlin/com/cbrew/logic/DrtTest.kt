package com.cbrew.logic

import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
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

    // \A B.([], [A -> B]) applied to two boxes: the implication's antecedent
    // is a variable when read, and becomes a box only by substitution.
    private val substituted: Lambda =
            createApp(createApp(drs("\\A B.([], [A -> B])"), drs("([x], [farmer(x)])")),
                    drs("([y], [PRO(y), run(y)])"))

    @Test
    fun testSubstitutedAntecedentIsBoxImplication() = assertEquals(
            "([], [(([x1], [farmer(x1)]) → ([x2], [PRO(x2), run(x2)]))])",
            substituted.pretty())

    @Test
    fun testSubstitutedAntecedentReferentsAccessible() = assertEquals(
            "([], [(([x1], [farmer(x1)]) → ([x2], [(x2 = [x1]), run(x2)]))])",
            resolveAnaphora(substituted)!!.pretty())

    @Test
    fun testSubstitutedImplicationFol() = assertEquals(
            "∀x1.(farmer(x1) → ∃x2.((x2 = x1) ∧ run(x2)))",
            resolveAnaphora(substituted)!!.fol().pretty())

    @Test
    fun testDonkeyResolvedInBoxImplication() = assertEquals(
            "([], [(([x1, x2], [farmer(x1), donkey(x2), own(x1, x2)]) → " +
                    "([x3, x4], [(x3 = [x1, x2, x4]), (x4 = [x1, x2, x3]), beat(x3, x4)]))])",
            resolveAnaphora(drs("([], [(([x, y], [farmer(x), donkey(y), own(x, y)]) -> " +
                    "([u, v], [PRO(u), PRO(v), beat(u, v)]))])"))!!.pretty())

    @Test
    fun testFolThroughLogicConnectives() {
        val box = Drs(1, listOf(App(Constant("dog"), QVar(1)), App(App(Constant("own"), QVar(2)), QVar(1))))
        val p = App(Constant("p"), QVar(1))
        assertEquals("∃x1.(p(x1) → ∃x2.(dog(x2) ∧ own(x1, x2)))", Exists(Implies(p, box)).fol().pretty())
        assertEquals("∀x1.(p(x1) = ∃x2.(dog(x2) ∧ own(x1, x2)))", Forall(Equiv(p, box)).fol().pretty())
    }

    @Test
    fun testDrsKeyIgnoresReferentAndConditionOrder() = assertEquals(
            drsKey(drs("([x, y], [dog(x), angus(y), own(y, x)])")),
            drsKey(drs("([x, y], [angus(x), dog(y), own(x, y)])")))

    @Test
    fun testDrsKeyRenamesLikeReferents() = assertEquals(      // swapping x and y gives the converse
            drsKey(drs("([x, y], [dog(x), dog(y), chase(x, y)])")),
            drsKey(drs("([x, y], [dog(x), dog(y), chase(y, x)])")))

    @Test
    fun testDrsKeyKeepsArgumentOrder() = assertNotEquals(
            drsKey(drs("([x, y], [dog(x), boy(y), chase(x, y)])")),
            drsKey(drs("([x, y], [dog(x), boy(y), chase(y, x)])")))

    @Test
    fun testDrsKeyInsideImplication() = assertEquals(        // inner referents reorder, outer ones stay put
            drsKey(drs("([z], [boy(z), (([x, y], [dog(x), bone(y), own(x, y)]) -> ([], [like(x, z)]))])")),
            drsKey(drs("([z], [boy(z), (([y, x], [dog(x), bone(y), own(x, y)]) -> ([], [like(x, z)]))])")))

    @Test
    fun testDrsKeyKeepsScope() = assertNotEquals(            // a boy outside or inside the implication
            drsKey(drs("([y], [boy(y), (([x], [dog(x)]) -> ([], [chase(x, y)]))])")),
            drsKey(drs("([], [(([x], [dog(x)]) -> ([y], [boy(y), chase(x, y)]))])")))
}
