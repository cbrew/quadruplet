package com.cbrew.unify

import com.cbrew.fstruct.notation.FeatureNotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BindingsTest {

    private val a = AtomicValue("a")
    private val b = AtomicValue("b")

    @Test
    fun testEmpty() {
        assertEquals(0, Bindings.EMPTY.size)
        assertTrue(Bindings.EMPTY.isEmpty())
        assertNull(Bindings.EMPTY["?x"])
        assertEquals(emptyMap<CharSequence, Unifiable>(), Bindings.EMPTY)
    }

    @Test
    fun testBindIsPersistent() {
        val b1 = Bindings.EMPTY.bind("?x", a)
        val b2 = b1.bind("?y", b)
        assertEquals(1, b1.size, "extending leaves the original untouched")
        assertNull(b1["?y"])
        assertEquals(a, b2["?x"], "earlier bindings are visible through later ones")
        assertEquals(b, b2["?y"])
        assertEquals(mapOf<CharSequence, Unifiable>(Pair("?x", a), Pair("?y", b)), b2)
        assertEquals(b2, mapOf<CharSequence, Unifiable>(Pair("?y", b), Pair("?x", a)))
        assertEquals("{?x=a, ?y=b}", b2.toString(), "printed oldest first")
    }

    @Test
    fun testOf() {
        val b1 = Bindings.EMPTY.bind("?x", a)
        assertSame(b1, Bindings.of(b1))
        assertEquals(mapOf<CharSequence, Unifiable>(Pair("?x", a)), Bindings.of(mapOf(Pair("?x", a))))
    }

    @Test
    fun testUnifyExtendsBindings() {
        val result = unify(FeatureNotation.toFs("X[a=?x, b=?y]"), FeatureNotation.toFs("X[a=p, b=q]"))!!
        assertTrue(result.second is Bindings)
        assertEquals(2, result.second.size)

        val before = Bindings.EMPTY.bind("?x", a)
        val after = before.checkBinding(QueryVariable("?y"), b)!!.second
        assertEquals(1, before.size)
        assertEquals(mapOf<CharSequence, Unifiable>(Pair("?x", a), Pair("?y", b)), after)
    }
}
