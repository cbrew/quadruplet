package com.cbrew.unify

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * shift, qshift, placeBoxes, substBoxes and quantifierBinds skip subterms
 * they cannot change, using the de Bruijn facts cached on each term. These
 * tests compare them with the original full-traversal versions (kept below
 * as references) on many random terms.
 */
class LambdaSharingTest {

    // ---- reference implementations: the versions before sharing

    private fun refPlaceBoxes(e: Lambda, bvi: Int): Lambda = when (e) {
        is And -> And(e.conjuncts.map { refPlaceBoxes(it, bvi) }.toSet())
        is Or -> Or(e.disjuncts.map { refPlaceBoxes(it, bvi) }.toSet())
        is Constant, is FstructVar, is QVar, is Box, is Empty -> e
        is Var -> if (bvi == e.index) Box else e
        is Forall -> Forall(refPlaceBoxes(e.body, bvi))
        is Exists -> Exists(refPlaceBoxes(e.body, bvi))
        is Not -> Not(refPlaceBoxes(e.body, bvi))
        is Lam -> Lam(refPlaceBoxes(e.body, bvi + 1))
        is App -> App(refPlaceBoxes(e.e1, bvi), refPlaceBoxes(e.e2, bvi))
        is Equiv -> Equiv(refPlaceBoxes(e.e1, bvi), refPlaceBoxes(e.e2, bvi))
        is Implies -> Implies(refPlaceBoxes(e.e1, bvi), refPlaceBoxes(e.e2, bvi))
    }

    private fun refSubstBoxes(e: Lambda, x: Lambda, bvi: Int, qvi: Int): Lambda = when (e) {
        is And -> createAnd(e.conjuncts.map { refSubstBoxes(it, x, bvi, qvi) })
        is Or -> createOr(e.disjuncts.map { refSubstBoxes(it, x, bvi, qvi) })
        is Constant, is FstructVar, is Var, is QVar, is Empty -> e
        is Box -> refQshift(refShift(x, bvi, 0), qvi, 0)
        is Forall -> createUniversal(refSubstBoxes(e.body, x, bvi, qvi + 1))
        is Exists -> createExistential(refSubstBoxes(e.body, x, bvi, qvi + 1))
        is Not -> createNegation(refSubstBoxes(e.body, x, bvi, qvi))
        is Lam -> createLam(refSubstBoxes(e.body, x, bvi + 1, qvi))
        is App -> createApp(refSubstBoxes(e.e1, x, bvi, qvi), refSubstBoxes(e.e2, x, bvi, qvi))
        is Equiv -> createEquiv(refSubstBoxes(e.e1, x, bvi, qvi), refSubstBoxes(e.e2, x, bvi, qvi))
        is Implies -> createImplication(refSubstBoxes(e.e1, x, bvi, qvi), refSubstBoxes(e.e2, x, bvi, qvi))
    }

    private fun refQshift(e: Lambda, amount: Int, qvi: Int): Lambda = when (e) {
        is And -> createAnd(e.conjuncts.map { refQshift(it, amount, qvi) })
        is Or -> createOr(e.disjuncts.map { refQshift(it, amount, qvi) })
        is Constant, is FstructVar, is Var, is Box, is Empty -> e
        is QVar -> if (e.index > qvi) QVar(e.index + amount) else e
        is Forall -> createUniversal(refQshift(e.body, amount, qvi + 1))
        is Exists -> createExistential(refQshift(e.body, amount, qvi + 1))
        is Not -> Not(refQshift(e.body, amount, qvi))
        is Lam -> createLam(refQshift(e.body, amount, qvi))
        is App -> App(refQshift(e.e1, amount, qvi), refQshift(e.e2, amount, qvi))
        is Equiv -> createEquiv(refQshift(e.e1, amount, qvi), refQshift(e.e2, amount, qvi))
        is Implies -> createImplication(refQshift(e.e1, amount, qvi), refQshift(e.e2, amount, qvi))
    }

    private fun refShift(e: Lambda, amount: Int, bvi: Int): Lambda = when (e) {
        is And -> And(e.conjuncts.map { refShift(it, amount, bvi) }.toSet())
        is Or -> Or(e.disjuncts.map { refShift(it, amount, bvi) }.toSet())
        is Constant, is FstructVar, is QVar, is Box, is Empty -> e
        is Var -> if (e.index > bvi) Var(e.index + amount) else e
        is Forall -> Forall(refShift(e.body, amount, bvi))
        is Exists -> Exists(refShift(e.body, amount, bvi))
        is Not -> Not(refShift(e.body, amount, bvi))
        is Lam -> Lam(refShift(e.body, amount, bvi + 1))
        is App -> App(refShift(e.e1, amount, bvi), refShift(e.e2, amount, bvi))
        is Equiv -> Equiv(refShift(e.e1, amount, bvi), refShift(e.e2, amount, bvi))
        is Implies -> Implies(refShift(e.e1, amount, bvi), refShift(e.e2, amount, bvi))
    }

    private fun refQuantifierBinds(e: Lambda, bvi: Int): Boolean = when (e) {
        is Box, is Empty, is Constant, is FstructVar, is Var -> false
        is QVar -> e.index == bvi
        is Exists -> refQuantifierBinds(e.body, bvi + 1)
        is Forall -> refQuantifierBinds(e.body, bvi + 1)
        is Not -> refQuantifierBinds(e.body, bvi)
        is Lam -> refQuantifierBinds(e.body, bvi)
        is App -> refQuantifierBinds(e.e1, bvi) || refQuantifierBinds(e.e2, bvi)
        is Equiv -> refQuantifierBinds(e.e1, bvi) || refQuantifierBinds(e.e2, bvi)
        is Implies -> refQuantifierBinds(e.e1, bvi) || refQuantifierBinds(e.e2, bvi)
        is Or -> e.disjuncts.any { refQuantifierBinds(it, bvi) }
        is And -> e.conjuncts.any { refQuantifierBinds(it, bvi) }
    }

    // ---- random terms, normalized as the grammar's terms are

    private fun term(r: Random, depth: Int, boxes: Boolean): Lambda {
        if (depth == 0 || r.nextInt(4) == 0) return when (r.nextInt(if (boxes) 5 else 4)) {
            0 -> Constant(listOf("a", "b", "c")[r.nextInt(3)])
            1 -> Var(1 + r.nextInt(3))
            2 -> QVar(1 + r.nextInt(3))
            3 -> FstructVar("?f")
            else -> Box
        }
        fun sub() = term(r, depth - 1, boxes)
        return when (r.nextInt(9)) {
            0 -> Lam(sub())
            1 -> Exists(sub())
            2 -> Forall(sub())
            3 -> Not(sub())
            4 -> And(setOf(sub(), sub()))
            5 -> Or(setOf(sub(), sub()))
            6 -> Implies(sub(), sub())
            7 -> Equiv(sub(), sub())
            else -> App(sub(), sub())
        }
    }

    private fun normalTerm(r: Random, boxes: Boolean = false): Lambda =
            term(r, 5, boxes).normalized() as Lambda

    // compare outcomes, including any exception the reference throws
    private fun same(label: String, t: Lambda, expected: () -> Any, actual: () -> Any) {
        val e = runCatching(expected)
        val a = runCatching(actual)
        assertEquals(e.getOrNull(), a.getOrNull(), "$label on $t")
        assertEquals(e.exceptionOrNull()?.javaClass, a.exceptionOrNull()?.javaClass, "$label (exception) on $t")
    }

    @Test
    fun testMatchesReferenceOnRandomTerms() {
        val r = Random(20260927)
        repeat(3000) {
            val t = normalTerm(r)
            for (amount in listOf(-1, 0, 1, 2)) {
                same("shift $amount", t, { refShift(t, amount, 0) }, { shift(t, amount) })
                same("qshift $amount", t, { refQshift(t, amount, 0) }, { qshift(t, amount) })
            }
            same("placeBoxes", t, { refPlaceBoxes(t, 1) }, { placeBoxes(t) })
            for (bvi in 1..3) same("quantifierBinds $bvi", t, { refQuantifierBinds(t, bvi) }, { quantifierBinds(t, bvi) })

            val withBoxes = normalTerm(r, boxes = true)
            val x = normalTerm(r)
            same("substBoxes", withBoxes, { refSubstBoxes(withBoxes, x, 0, 0) }, { substBoxes(withBoxes, x) })

            // a whole beta reduction, as createApp does it (in one pass now),
            // including bodies that already contain Boxes
            same("beta", t, { refSubstBoxes(refShift(refPlaceBoxes(t, 1), -1, 0), x, 0, 0) },
                    { createApp(Lam(t), x) })
            same("beta with boxes", withBoxes,
                    { refSubstBoxes(refShift(refPlaceBoxes(withBoxes, 1), -1, 0), x, 0, 0) },
                    { createApp(Lam(withBoxes), x) })
        }
    }

    @Test
    fun testUnchangedTermsAreShared() {
        val closed = Lam(App(Constant("f"), Var(1)))
        assertSame(closed, shift(closed, 1), "a closed term has nothing to shift")
        assertSame(closed, qshift(closed, 1))
        val q = Exists(App(Constant("p"), QVar(1)))
        assertSame(q, qshift(q, 2), "a bound QVar is not shifted")
        val noBox = App(Constant("f"), Constant("c"))
        assertSame(noBox, substBoxes(noBox, Constant("x")))

        // only the path to the replaced variable is rebuilt
        val big = And(setOf(App(Constant("p"), Constant("c")), App(Constant("q"), Var(1))))
        val boxed = placeBoxes(big) as And
        val kept = big.conjuncts.first { it.freeVarDepth == 0 }
        assertSame(kept, boxed.conjuncts.first { !it.hasBox })
    }
}
