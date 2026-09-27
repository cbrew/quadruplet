package com.cbrew.unify

import org.junit.Test
import kotlin.test.assertEquals


class LogicTest {


    @Test
    fun testForall() {
        assertEquals(Forall(QVar(1)), createUniversal(QVar(1)))

        // reject a vacuous binding
        assertEquals(Constant("a"), createUniversal(Constant("a")),
                "reject a vacuous binding")

        assertEquals(QVar(5), createUniversal(QVar(6)),
                "reject a vacuous binding to a term that has a free QVar." +
                        "We shift it down.")




    }


    @Test
    fun testExists() {
        assertEquals(Exists(QVar(1)), createExistential(QVar(1)))

        assertEquals(
                Exists(createAnd(QVar(1), Constant("a"))),
                createExistential(createAnd(QVar(1), Constant("a"))))
        // reject a vacuous binding
        assertEquals(Constant("a"), createExistential(Constant("a")),
                "reject a vacuous binding")
        // reject a vacuous binding
        assertEquals(QVar(1), createExistential(QVar(2)))
    }

    @Test
    fun testSingleDisjunct() {
        assertEquals(Constant("a"), createOr(Constant("a")))
    }

    @Test
    fun testSingleConjunct() {
        assertEquals(Constant("a"), createAnd(Constant("a")))
    }

    @Test
    fun testOrBehavesLikeSet() {
        assertEquals(
                Or(setOf(Constant("hello"), Constant("world"), Constant("championship"))),

                createOr(Constant("hello"),
                        Or(
                        setOf(Constant("world"),
                                Or(setOf(Constant("world"),
                                        Constant("championship")))))))
        // remove redundant conjunct
        assertEquals(Or(setOf(Constant("hello"), Constant("world"))),
            createOr(Constant("hello"),Constant("hello"),Constant("world")))


    }


    @Test
    fun testAndBehavesLikeSet() {
        assertEquals(
                And(setOf(Constant("hello"), Constant("world"), Constant("championship"))),

                createAnd(Constant("hello"), And(
                        setOf(Constant("world"),
                                And(setOf(Constant("world"),
                                        Constant("championship")))))))
    }

    @Test
    fun testSingleNegation() {
        assertEquals(Not(Constant("a")), createNegation(Constant("a")))
    }

    @Test
    fun testDoubleNegation() {
        assertEquals(Constant("a"), createNegation(createNegation(Constant("a"))))
    }

    @Test
    fun testNegationOfNegatedConjuncts() {
        assertEquals(Not(Or(setOf(Constant("a"), Constant("b")))),
                createNegation(
                        And(setOf(
                                Not(Constant("a")),
                                Not(Constant("b"))))))
    }

    @Test
    fun testNegationOfNegatedDisjuncts() {
        assertEquals(Not(And(setOf(Constant("a"), Constant("b")))),
                createNegation(
                Or(setOf(
                        Not(Constant("a")),
                        Not(Constant("b"))))))
    }

    @Test
    fun testVacuousQuantifierKeepsConnective() {
        val pq = createAnd(Constant("p"), Constant("q"))
        assertEquals(pq, createExistential(pq), "∃.(p ∧ q) with nothing bound is p ∧ q")
        assertEquals(pq, createUniversal(pq), "∀.(p ∧ q) with nothing bound is p ∧ q")
        val porq = createOr(Constant("p"), Constant("q"))
        assertEquals(porq, createExistential(porq))
        assertEquals(porq, createUniversal(porq))
        assertEquals(createAnd(QVar(1), Constant("a")),
                createExistential(createAnd(QVar(2), Constant("a"))),
                "free variables are shifted down, connective kept")
    }

    @Test
    fun testDuplicateConjunctsCollapse() {
        assertEquals(Constant("a"), createAnd(Constant("a"), Constant("a")))
    }

    @Test
    fun testNormalOrderReduceKeepsOtherConjuncts() {
        // a conjunction containing a redex: reducing it must keep the rest
        val redex = App(Lam(App(Constant("f"), Var(1))), Constant("c"))
        assertEquals(And(setOf(Constant("a"), App(Constant("f"), Constant("c")))),
                normalOrderReduce(And(setOf(Constant("a"), redex))))
        assertEquals(Or(setOf(Constant("a"), App(Constant("f"), Constant("c")))),
                normalOrderReduce(Or(setOf(Constant("a"), redex))))
        assertEquals(And(setOf(App(Constant("f"), Constant("c")), App(Constant("f"), Constant("d")))),
                normalOrderReduce(And(setOf(redex, App(Lam(App(Constant("f"), Var(1))), Constant("d"))))),
                "every redex is reduced, one step at a time")
    }
}
