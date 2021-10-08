package com.cbrew.logic.notation

import com.cbrew.unify.*
import org.junit.Test
import kotlin.test.assertEquals


/**
 * set of tests for the semantic visitor.
 */


class SemanticVisitorTest {
    @Test
    fun testNegation() {


        assertEquals(Not(Constant("x")), LogicParser.toLogic("~ x"))

    }


    @Test
    fun testAtoms() {
        assertEquals(Constant("x"), LogicParser.toLogic("x"))
        assertEquals(FstructVar("?x"), LogicParser.toLogic("?x"))
        assertEquals(FstructVar("@x"), LogicParser.toLogic("@x"))
        assertEquals(Constant("P"), LogicParser.toLogic("P"))
        assertEquals(Constant("Q12"), LogicParser.toLogic("Q12"))
        assertEquals(Constant("a91"), LogicParser.toLogic("a91"))
        assertEquals(Constant("abc"), LogicParser.toLogic("abc"))
        assertEquals(Constant("Abc"), LogicParser.toLogic("Abc"))
        assertEquals(FstructVar("?abc"), LogicParser.toLogic("?abc"))
    }

    @Test
    fun testApplication() {

        assertEquals(App(Constant("P"), Constant("x")),
                LogicParser.toLogic("P(x)"))

        val target = App(App(App(Constant("P"), Constant("x")), Constant("y")), Constant("z"))
        assertEquals(target,
                LogicParser.toLogic("P(x,y,z)"))

        assertEquals(
                App(App(Constant("x"), Constant("y")), Constant("z")),
                LogicParser.toLogic("(\\P . P)(x,y,z)"),
                "this setup triggers beta reduction")

        assertEquals(
                App(App(App(Constant("x"), Constant("a")),
                        Constant("y")), Constant("z")),
                LogicParser.toLogic("(\\P . P(a))(x,y,z)"),
                "this setup triggers beta reduction.")
    }


    @Test
    fun testConjunctions() {

        assertEquals(And(setOf(Constant("a"), Constant("b"))),
                LogicParser.toLogic("a & b"))

        assertEquals(And(setOf(Constant("a"), Constant("b"))),
                LogicParser.toLogic("a \u2227 b"))

        assertEquals(Or(setOf(Constant("a"), Constant("b"))),
                LogicParser.toLogic("a | b"))


        assertEquals(Or(setOf(Constant("a"), Constant("b"))),
                LogicParser.toLogic("a \u2228 b"))


        assertEquals(Or(setOf(Constant("a"), Constant("b"), Constant("c"))),
                LogicParser.toLogic("a | b | c"),
                "multiple | = left-branching")

        assertEquals(And(setOf(Constant("a"), Constant("b"), Constant("c"))),
                LogicParser.toLogic("a & b & c"), "multiple & = left-branching")


        assertEquals(Or(setOf(Constant("a"), And(setOf(Constant("b"), Constant("c"))))),
                LogicParser.toLogic("a | b & c"),
                "and binds tighter than or")


        assertEquals(Or(setOf(Constant("c"), And(setOf(Constant("a"), Constant("b"))))),
                LogicParser.toLogic("a & b | c"),
                "and binds tighter than or")
    }

    @Test
    fun testQuantified() {
        assertEquals(Forall(QVar(1))
                , LogicParser.toLogic("forall x . x"))

        assertEquals(Forall(QVar(1))
                , LogicParser.toLogic("\u2200 x . x"))


        assertEquals(Exists(QVar(1))
                , LogicParser.toLogic("exists x . x"))


        assertEquals(Exists(QVar(1))
                , LogicParser.toLogic("\u2203 x . x"))


    }

    @Test
    fun testLambdaAbstraction() {
        assertEquals(Lam(Var(1))
                , LogicParser.toLogic("\\x . x"))

        assertEquals(Lam(Var(1))
                , LogicParser.toLogic("\u03BB x . x"),
                "Unicode synonym for lambda should work.")


        assertEquals(Lam(Lam(Var(2)))
                , LogicParser.toLogic("\\x y . x"))

        assertEquals(Lam(Lam(App(Var(2), Var(1)))),
                LogicParser.toLogic("\\P x . P(x)"),
                "lambda expression binding predicate and argument in application")
    }

    @Test
    fun testRelational() {


        assertEquals(Equiv(Constant("x"), Constant("y")),
                LogicParser.toLogic("x == y"))
        assertEquals(Equiv(Constant("x"), Constant("y")),
                LogicParser.toLogic("x = y"))


        assertEquals(Equiv(Constant("x"), Constant("y")),
                LogicParser.toLogic("x <-> y"))
        assertEquals(Equiv(Constant("x"), Constant("y")),
                LogicParser.toLogic("x \u2194 y"))


        assertEquals(Implies(Constant("x"), Constant("y")),
                LogicParser.toLogic("x \u2192 y"))
        assertEquals(Implies(Constant("x"), Constant("y")),
                LogicParser.toLogic("x -> y"))


        assertEquals(Not(Equiv(Constant("x"), Constant("y"))),
                LogicParser.toLogic("x != y"))
        assertEquals(Not(Equiv(Constant("x"), Constant("y"))),
                LogicParser.toLogic("x <>  y"))
        assertEquals(Not(Equiv(Constant("x"), Constant("y"))),
                LogicParser.toLogic("x \u2260 y"))
    }
}

