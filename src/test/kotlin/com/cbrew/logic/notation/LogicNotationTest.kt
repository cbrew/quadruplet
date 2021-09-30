package com.cbrew.logic.notation

import com.cbrew.unify.*
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class LogicNotationTest {

    @Test
    fun testLambda() {
        assertEquals(Lam(Var(1)), LogicParser.toLogic("\\ P. P"),
                "should recognize lambda expression")



        assertEquals(Lam(App(Var(1), FstructVar("?x"))),
                LogicParser.toLogic("\\ P. P (?x)"),
                "should recognize lambda expression with argument no parens")

    }


    @Test
    fun testFstructVar() {
        assertEquals(FstructVar("?x"), LogicParser.toLogic("?x"),
                "should recognize Fstruct vars")


        assertEquals(FstructVar("@x"), LogicParser.toLogic("@x"),
                "should recognize Fstruct vars with @")
        assertNotEquals(FstructVar("?x"), LogicParser.toLogic("?y"),
                "different fstruct vars should be distinct")
        assertEquals(App(Constant("zombie"),
                FstructVar("?x")), LogicParser.toLogic("zombie(?x)"),
                "should recognize Fstruct vars in app")


    }

    @Test
    fun testDisjunction() {
        assertEquals(createOr(Constant("jim"),
                Constant("jeffy"),
                Constant("jonah")),
                LogicParser.toLogic("(jim | jonah | jeffy)"))
    }

    @Test
    fun testConjunction() {
        assertEquals(createAnd(Constant("jim"),
                Constant("jeffy"),
                Constant("jonah")),
                LogicParser.toLogic("jim & jonah & jeffy"),
                "should make flat conjuncts")
    }

    @Test
    fun testDisjunctionFlattening() {
        val r = LogicParser.toLogic("a|b|c")
        assertEquals(Or(setOf(Constant("a"), Constant("b"), Constant("c"))), r,
                "should flatten out multiple disjuncts")
    }

    @Test
    fun testDisjunctionFlatteningForall() {
        val r = LogicParser.toLogic("a|b|forall q . q")
        assertEquals(Or(setOf(Constant("a"), Constant("b"), Forall(QVar(1)))), r,
                "should flatten out multiple disjuncts")
    }

    @Test
    fun testExists () {
        val r = LogicParser.toLogic("exists x. x")
        assertNotNull(r)

    }

    @Test
    fun testForall () {

        val r = LogicParser.toLogic("forall x y. y(x)")
        assertNotNull(r)

    }





    @Test
    fun testParseApp() {
        val ex2 = "love(jules,mia)"
        assertEquals(App(App(Constant("love"), Constant("jules")), Constant("mia")),
                LogicParser.toLogic(ex2))
    }


}