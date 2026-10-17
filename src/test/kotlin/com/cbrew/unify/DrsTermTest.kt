package com.cbrew.unify

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * DRS boxes are de Bruijn binders for QVars: a box with k referents binds
 * QVar(1)..QVar(k) in its conditions. The factories merge a box that
 * appears directly among another box's conditions into it, and the usual
 * operations (β-reduction, substitution, abstraction) respect the binders.
 */
class DrsTermTest {

    private fun app(f: String, vararg args: Lambda): Lambda =
            args.fold(Constant(f) as Lambda) { acc, a -> App(acc, a) }

    private fun q(i: Int) = QVar(i)

    @Test
    fun testFlattenAppendsReferents() = assertEquals(
            Drs(4, listOf(app("angus", q(4)), app("dog", q(3)), app("own", q(4), q(3)),
                    app("PRO", q(2)), app("irene", q(1)), app("bite", q(2), q(1)))),
            createDrs(2, listOf(app("angus", q(2)), app("dog", q(1)), app("own", q(2), q(1)),
                    Drs(2, listOf(app("PRO", q(2)), app("irene", q(1)), app("bite", q(2), q(1)))))))

    @Test
    fun testFlattenShiftsOtherConditions() = assertEquals(
            Drs(1, listOf(app("p", q(2)), app("dog", q(1)))),
            createDrs(0, listOf(app("p", q(1)), Drs(1, listOf(app("dog", q(1)))))))

    @Test
    fun testDrsImpFlattensAntecedentAndShiftsConsequent() = assertEquals(
            DrsImp(2, listOf(app("donkey", q(1)), app("own", q(2), q(1))), app("beat", q(2))),
            createDrsImp(1, listOf(Drs(1, listOf(app("donkey", q(1)), app("own", q(2), q(1))))),
                    app("beat", q(1))))

    // (λP.([x],[P(x)]))(λy.([],[dog(y)]))
    @Test
    fun testBetaReductionMergesBoxes() = assertEquals(
            Drs(1, listOf(app("dog", q(1)))),
            createApp(Lam(Drs(1, listOf(App(Var(1), q(1))))), Lam(Drs(0, listOf(app("dog", Var(1)))))))

    @Test
    fun testArgumentShiftedUnderReferents() = assertEquals(
            Drs(1, listOf(app("chase", q(2), q(1)))),
            createApp(Lam(Drs(1, listOf(app("chase", Var(1), q(1))))), q(1)))

    @Test
    fun testSubstitutionFlattens() = assertEquals(
            Drs(2, listOf(app("walk", q(2)), app("run", q(1)))),
            Drs(0, listOf(FstructVar("?s"), FstructVar("?d"))).renameVariables(mapOf<CharSequence, Unifiable>(
                    "?s" to Drs(1, listOf(app("walk", q(1)))), "?d" to Drs(1, listOf(app("run", q(1)))))))

    @Test
    fun testAbstractOverInsideBox() = assertEquals(
            Lam(Drs(1, listOf(app("chase", Var(1), q(1))))),
            Drs(1, listOf(app("chase", FstructVar("@x"), q(1)))).abstractOver(FstructVar("@x")))

    @Test
    fun testDoubleNegationOfBoxKept() {
        val k = Drs(1, listOf(app("dog", q(1))))
        assertEquals(Not(Not(k)), createNegation(Not(k)))
        assertEquals(app("p"), createNegation(Not(app("p"))))   // logic unchanged
    }

    @Test
    fun testDeBruijnFacts() {
        assertEquals(1, Drs(1, listOf(app("p", q(2)))).freeQVarDepth)
        assertEquals(0, Drs(1, listOf(app("dog", q(1)))).freeQVarDepth)
        assertEquals(2, DrsImp(1, listOf(app("dog", q(1))), app("p", q(3))).freeQVarDepth)
    }
}
