package com.cbrew.logic.notation

import com.cbrew.unify.*
import org.junit.Test
import kotlin.test.assertEquals

class ToLogicTest {

    @Test
    fun testLambda() {
        assertEquals(Lam(Var(1)), LogicParser.toLogic("\\ a. a"))
    }


    @Test
    fun testExists() {
        assertEquals(Exists(QVar(1)), LogicParser.toLogic("exists a. a"))
    }


    @Test
    fun testForall() {
        assertEquals(Forall(QVar(1)), LogicParser.toLogic("forall a. a"))
    }

    @Test
    fun testNot() {
        assertEquals(Not(Constant("a")), LogicParser.toLogic("~ a"))
    }

    @Test
    fun testEquals() {
        assertEquals(Equiv(Constant("a"), Constant("b")), LogicParser.toLogic("a == b"))
    }


    @Test
    fun testImplies() {
        assertEquals(Implies(Constant("a"), Constant("b")), LogicParser.toLogic("a -> b"))
    }

    @Test
    fun testIff() {
        assertEquals(Equiv(Constant("a"), Constant("b")), LogicParser.toLogic("a <-> b"))
    }

    @Test
    fun testBinarizeAtoms() {

        assertEquals(Constant("a"), LogicParser.toLogic("a"))
        assertEquals(Constant("P"), LogicParser.toLogic("P"))
        assertEquals(FstructVar("?x"), LogicParser.toLogic("?x"))

    }



}