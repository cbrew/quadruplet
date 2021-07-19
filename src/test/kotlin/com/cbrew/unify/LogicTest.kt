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


}
