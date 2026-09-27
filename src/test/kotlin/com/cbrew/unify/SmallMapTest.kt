package com.cbrew.unify

import com.cbrew.fstruct.notation.FeatureNotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SmallMapTest {

    private val plain = linkedMapOf("cat" to "NP", "num" to "sg", "sem" to "x")

    @Test
    fun testLookupAndOrder() {
        val m = SmallMap.of(plain)
        assertEquals(3, m.size)
        assertEquals("sg", m["num"])
        assertNull(m["loc"])
        assertTrue(m.containsKey("sem"))
        assertFalse(m.containsKey("loc"))
        assertEquals(listOf("cat", "num", "sem"), m.keys.toList())
        assertEquals("{cat=NP, num=sg, sem=x}", m.toString())
        assertSame(m, SmallMap.of(m))
    }

    @Test
    fun testMapContract() {
        val m = SmallMap.of(plain)
        val reordered = linkedMapOf("sem" to "x", "cat" to "NP", "num" to "sg")
        assertEquals(reordered, m)
        assertEquals(m, reordered)
        assertEquals(plain.hashCode(), m.hashCode())
        assertEquals(plain.entries.first(), m.entries.first())
    }

    @Test
    fun testLargeMapsFallBack() {
        val b = SmallMap.Builder<String, Int>(40)
        for (i in 1..40) b.put("f$i", i)
        val m = b.build()
        assertTrue(m is LinkedHashMap<*, *>)
        assertEquals(40, m.size)
        assertEquals(7, m["f7"])
    }

    @Test
    fun testFeatureMapsAgreeAcrossRepresentations() {
        val parsed = FeatureNotation.toFs("X[a=p, b=[q, r]]") as FeatureMap
        val compact = FeatureMap(SmallMap.of(parsed.toMap()))
        assertEquals(parsed, compact)
        assertEquals(compact, parsed)
        assertEquals(parsed.hashCode(), compact.hashCode())
        assertEquals(parsed.toString(), compact.toString())
        assertEquals(parsed, unify(parsed, compact)!!.first, "unification builds compact maps")
    }
}
