package com.cbrew.unify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SmallSetTest {

    @Test
    fun testDeduplicatesKeepingFirstOccurrence() {
        val s = SmallSet.of(listOf("b", "a", "b", "c", "a"))
        assertEquals(listOf("b", "a", "c"), s.toList())
        assertEquals(3, s.size)
        assertTrue("a" in s)
        assertFalse("d" in s)
    }

    @Test
    fun testSetEquality() {
        val s = SmallSet.of(listOf(Constant("p"), Constant("q")))
        assertEquals(setOf<Lambda>(Constant("q"), Constant("p")), s, "order does not matter")
        assertEquals(s, setOf<Lambda>(Constant("q"), Constant("p")))
        assertEquals(setOf<Lambda>(Constant("p"), Constant("q")).hashCode(), s.hashCode(), "Set hash contract")
        assertEquals(And(setOf(Constant("p"), Constant("q"))), And(s))
        assertEquals(And(setOf(Constant("p"), Constant("q"))).hashCode(), And(s).hashCode())
    }

    @Test
    fun testLargeSetsFallBack() {
        val big = (1..40).map { "x$it" } + listOf("x1")
        val s = SmallSet.of(big)
        assertTrue(s is LinkedHashSet<*>)
        assertEquals(40, s.size)
        assertEquals("x1", s.first())
    }

    @Test
    fun testEmpty() {
        val s = SmallSet.of(emptyList<String>())
        assertTrue(s.isEmpty())
        assertFalse(s.iterator().hasNext())
    }
}
