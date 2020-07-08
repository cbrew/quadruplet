package com.cbrew.unify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UnifyTest {

    @Test
    fun testDeref1() {
        assertEquals(FstructVar("?x"),
                emptyMap<CharSequence, Unifiable>().deref(FstructVar("?x")),
                "deref should be non-op for fstruct variable not in bindings")


        assertEquals(QueryVariable("?x"),
                emptyMap<CharSequence, Unifiable>().deref(QueryVariable("?x")),
                "deref should be non-op for semantic variable not in bindings")
    }

    @Test
    fun testDeref2() {


        assertEquals(
                QueryVariable("?y"),
                mapOf(Pair<CharSequence, Unifiable>("?x", QueryVariable("?y"))).deref(QueryVariable("?x")),
                "follow chain to semantic variable not in bindings")
    }


    @Test
    fun testDeref3() {
        assertEquals(FstructVar("?z"),
                mapOf(Pair<CharSequence, Unifiable>("?x", FstructVar("?y")),
                        Pair<CharSequence, Unifiable>("?y", FstructVar("?z"))).deref(QueryVariable("?x")),
                "follow chain to fstruct variable not in bindings")

    }


    @Test
    fun testCheckBinding1() {
        assertEquals(
                UR(AtomicValue(value = "a"), mapOf(Pair("?x", AtomicValue("a")))),
                emptyMap<CharSequence, Unifiable>().checkBinding(
                        QueryVariable("?x"),
                        AtomicValue("a")),
                "binding of atom into empty map should work")

    }

    @Test
    fun testCheckBinding2() {
        assertEquals(
                UR(Constant("a"), mapOf(Pair("?x", Constant("a")))),
                emptyMap<CharSequence, Unifiable>().checkBinding(
                        FstructVar("?x"),
                        Constant("a")),
                "binding of constant into empty map should work")
    }

    @Test
    fun testCheckBinding3() {
        assertNull(emptyMap<CharSequence, Unifiable>().checkBinding(
                FstructVar("?x"),
                AtomicValue("a")),
                "binding of semantic variable to fstruct constant should fail")
    }

    @Test
    fun testCheckBinding4() {
        assertNull(emptyMap<CharSequence, Unifiable>().checkBinding(
                QueryVariable("?x"),
                Constant("a")),
                "binding of fstruct variable to semantic constant should fail")

    }

    @Test
    fun testCheckBinding5() {
        assertEquals(
                UR(QueryVariable(name = "?y"), mapOf(Pair("?x", QueryVariable("?y")))),
                emptyMap<CharSequence, Unifiable>().checkBinding(
                        QueryVariable("?x"),
                        QueryVariable("?y")),
                "binding of variable into empty map should work")
    }

    @Test
    fun testCheckBinding6() {

        assertNull(emptyMap<CharSequence, Unifiable>().checkBinding(
                FstructVar("?x"),
                QueryVariable("?y")),
                "binding of semantic variable to fstruct variable should fail")
    }

    @Test
    fun testCheckBinding7() {
        assertNull(emptyMap<CharSequence, Unifiable>().checkBinding(
                QueryVariable("?x"),
                FstructVar("?y")),
                "binding of query variable to semantic variable should fail")


    }

    @Test
    fun testCheckCompatibleBinding() {
        assertEquals(
                UR(Constant("a"), mapOf(Pair("?x", Constant("a")))),
                mapOf<CharSequence, Unifiable>(Pair("?x", Constant("a"))).checkBinding(
                        FstructVar("?x"),
                        Constant("a")),
                "consistent sem -> sem binding")


        assertEquals(
                UR(AtomicValue("a"), mapOf(Pair("?x", AtomicValue("a")))),
                mapOf<CharSequence, Unifiable>(Pair("?x", AtomicValue("a"))).checkBinding(
                        QueryVariable("?x"),
                        AtomicValue("a")),
                "consistent syn -> syn binding")
    }


    @Test
    fun testCheckIncompatibleBinding() {
        assertNull(
                mapOf<CharSequence, Unifiable>(Pair("?x", Constant("a"))).checkBinding(
                        FstructVar("?x"),
                        Constant("b")),
                "inconsistent sem -> sem binding")


        assertNull(
                mapOf<CharSequence, Unifiable>(Pair("?x", AtomicValue("a"))).checkBinding(
                        QueryVariable("?x"),
                        AtomicValue("b")),
                "inconsistent syn -> syn binding")

        assertNull(
                mapOf<CharSequence, Unifiable>(
                        Pair("?x", FstructVar("?y")),
                        Pair("?y", Constant("a"))).checkBinding(
                        FstructVar("?x"),
                        Constant("b")),
                "indirect inconsistent sem -> sem binding")


        assertNull(
                mapOf<CharSequence, Unifiable>(
                        Pair("?x", QueryVariable("?y")),
                        Pair("?y", AtomicValue("a"))).checkBinding(
                        QueryVariable("?x"),
                        AtomicValue("b")),
                "indirect inconsistent syn -> syn binding")
    }

    @Test
    fun testCheckNoCrossDomainBinding() {
        assertNull(
                mapOf<CharSequence, Unifiable>(Pair("?x", Constant("a"))).checkBinding(
                        QueryVariable("?x"),
                        Constant("a")),
                "syn -> sem binding fails")

        assertNull(
                mapOf<CharSequence, Unifiable>(Pair("?x", AtomicValue("a"))).checkBinding(
                        FstructVar("?x"),
                        AtomicValue("a")),
                "sem -> syn binding fails")

    }


}