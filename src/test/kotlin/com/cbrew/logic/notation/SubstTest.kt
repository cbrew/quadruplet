package com.cbrew.logic.notation

import com.cbrew.unify.Unifiable
import com.cbrew.unify.closed
import com.cbrew.unify.get
import com.cbrew.unify.subst
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class SubstTest {
    @Test
    fun testClosed() {
        assertTrue(closed(LogicParser.toLogic("a")))
        assertTrue(closed(LogicParser.toLogic("a & b")))
        assertTrue(closed(LogicParser.toLogic("a | b")))
        assertTrue(closed(LogicParser.toLogic("a -> b")))
        assertTrue(closed(LogicParser.toLogic("a <-> b")))
    }


    @Test
    fun testClosed2() {


        assertTrue(closed(LogicParser.toLogic("forall x . (x <-> b)")))
        assertTrue(closed(LogicParser.toLogic("forall x . (a <-> x)")))
        assertTrue(closed(LogicParser.toLogic("forall x y . (y <-> x)")))
        assertFalse(closed(LogicParser.toLogic("forall x y . (y <-> x)")[0]),
                "should have a free quantified variable")


        assertTrue(closed(LogicParser.toLogic("exists x . (x <-> b)")))
        assertTrue(closed(LogicParser.toLogic("exists x . (a <-> x)")))
        assertTrue(closed(LogicParser.toLogic("exists x y . (y <-> x)")))
        assertFalse(closed(LogicParser.toLogic("exists x y . (y <-> x)")[0]),
                "should have a free quantified variable")


    }


    @Test
    fun testClosed3() {


        assertTrue(closed(LogicParser.toLogic("\\ x . (x <-> b)")))
        assertTrue(closed(LogicParser.toLogic("\\ x . (a <-> x)")))
        assertTrue(closed(LogicParser.toLogic("\\ x y . (y <-> x)")))
        assertFalse(closed(LogicParser.toLogic("\\ x y . (y <-> x)")[0]),
                "should have a free lambda variable")


    }

    @Test
    fun testSubst() {
        assertEquals(LogicParser.toLogic("a"),
                mapOf(Pair<CharSequence, Unifiable>("?x", LogicParser.toLogic("a"))).subst(LogicParser.toLogic("?x")))

        assertEquals(LogicParser.toLogic("a(a)"),
                mapOf(Pair<CharSequence, Unifiable>("?x", LogicParser.toLogic("a"))).subst(LogicParser.toLogic("a(?x)")))

        assertEquals(LogicParser.toLogic("a(a)"),
                (mapOf(Pair<CharSequence, Unifiable>("?x", LogicParser.toLogic("a")))).subst(LogicParser.toLogic("?x(?x)")))


        assertEquals(LogicParser.toLogic("a"),
                (mapOf(Pair<CharSequence, Unifiable>("?x", LogicParser.toLogic("a")))).subst(LogicParser.toLogic("?x & ?x")),
                "instantiation produces a single conjunct, which we unwrap")

        assertEquals(LogicParser.toLogic("a"),
                mapOf(Pair<CharSequence, Unifiable>("?x", LogicParser.toLogic("a"))).subst(LogicParser.toLogic("?x | ?x")),
                "instantiation produces a single disjunct, which we unwrap")
    }


}