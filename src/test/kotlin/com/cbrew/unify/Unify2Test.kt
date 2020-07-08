package com.cbrew.unify


import com.cbrew.fstruct.notation.FeatureNotation
import com.cbrew.unify.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class Unify2Test {
    @Test
    fun testUnify1() {
        assertEquals(Pair(AtomicValue("a"), mapOf()),
                unify(AtomicValue("a"), AtomicValue("a")),
                "compatible values should succeed")
    }

    @Test
    fun testUnify2() {
        assertEquals(null,
                unify(AtomicValue("a"), AtomicValue("b")),
                "incompatible values should fail")
    }

    @Test
    fun testUnify3() {
        assertEquals(Pair(AtomicValue("b"), mapOf<CharSequence, Unifiable>(Pair("?x", AtomicValue("b")))),
                unify(QueryVariable("?x"), AtomicValue("b")),
                "incompatible values should succeed and bind")
    }

    @Test
    fun testUnify4() {
        assertEquals(Pair(AtomicValue("c"), mapOf(Pair<CharSequence, Unifiable>("?x", AtomicValue("c")))),
                unify(AtomicValue("c"), QueryVariable("?x")),
                "Compatible values should succeed and bind")
    }


    @Test
    fun testUnify5() {
        assertEquals(null,
                unify(
                        FeatureMap(mapOf(Pair("a", QueryVariable("?x")), Pair("b", QueryVariable("?x")))),
                        FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", AtomicValue("q"))))),
                "inconsistent mapping should fail")
    }

    @Test
    fun testUnify6() {
        val target = UR(
                FeatureMap(mapOf<String, Unifiable>(Pair("a", AtomicValue("p")),
                        Pair("b", AtomicValue("p")))),
                mapOf(Pair("?x", AtomicValue("p"))))
        assertEquals(target,
                unify(
                        FeatureMap(mapOf(Pair("a", QueryVariable("?x")), Pair("b", QueryVariable("?x")))),
                        FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", AtomicValue("p"))))),
                "Consistent mapping should succeed")
    }

    @Test
    fun testUnify7() {
        val targetFs = FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", AtomicValue("q"))))
        val finalFs = FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", AtomicValue("q"))))
        val targetMapping = mapOf<CharSequence, Unifiable>(Pair("?x", AtomicValue("p")), Pair("?y", AtomicValue("q")))
        val target = UR(targetFs, targetMapping)
        assertEquals(target,
                unify(
                        FeatureMap(mapOf(Pair("a", QueryVariable("?x")), Pair("b", QueryVariable("?y")))),
                        FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", AtomicValue("q"))))),
                "Consistent mapping with two QueryVariables should succeed")

        assertEquals(finalFs,
                targetMapping.subst(targetFs),
                "substitution of consistent mapping should work")
    }


    @Test
    fun testUnify8() {
        val fs1 = FeatureMap(mapOf(Pair("a", QueryVariable("?x")), Pair("b", QueryVariable("?y"))))
        val fs2 = FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", AtomicValue("q"))))
        val finalFs = FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", AtomicValue("q"))))

        assertEquals(finalFs, fs1.unify(fs2),
                "Consistent mapping with two QueryVariables followed by subst should succeed.")

    }

    @Test
    fun testUnify9() {
        val fs1 = FeatureMap(mapOf(Pair("a", QueryVariable("?x")), Pair("b", QueryVariable("?x"))))
        val fs2 = FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", AtomicValue("q"))))

        assertNull(fs1.unify(fs2),
                "Inconsistent mapping with two QueryVariables followed by subst should fail.")

    }

    @Test
    fun testUnify10() {
        val fs1 = FeatureMap(mapOf(Pair("a", QueryVariable("?x")), Pair("b", QueryVariable("?y"))))
        val fs2 = FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", SemanticValue(Constant("q")))))
        val finalFs = FeatureMap(mapOf(Pair("a", AtomicValue("p")), Pair("b", SemanticValue(Constant("q")))))
        assertEquals(finalFs, fs1.unify(fs2),
                "Consistent mapping with semantic value should work.")


    }

    @Test
    fun testUnify11() {
        val fs1 = FstructVar("?y")
        val fs2 = Constant("q")
        val finalFs = Constant(name = "q")
        assertEquals(finalFs, fs1.unify(fs2),
                "Binding of semantic variables")
    }

    @Test
    fun testUnify11a() {
        val fs1 = FeatureMap(mapOf(Pair("a", FstructVar("?y"))))
        val fs2 = FeatureMap(mapOf(Pair("a", Constant(name = "q"))))
        val finalFs = FeatureMap(mapOf(Pair("a", Constant(name = "q"))))
        assertEquals(finalFs, fs1.unify(fs2),
                "Binding of semantic variables")
    }


    @Test
    fun testUnify11b() {
        val fs1 = FeatureMap(mapOf(
                Pair("b", AtomicValue("pl")),
                Pair("a", FstructVar("?y"))))
        val fs2 = FeatureMap(mapOf(Pair("a", Constant(name = "q"))))
        val finalFs = FeatureMap(mapOf(Pair("b", AtomicValue("pl")),
                Pair("a", Constant(name = "q"))))
        assertEquals(finalFs, fs1.unify(fs2),
                "Binding of semantic variables works")
    }

    @Test
    fun testUnify12() {
        val fs1 = SemanticValue(Constant("q"))
        val fs2 = SemanticValue(FstructVar("?x"))
        val finalFs = SemanticValue(Constant("q"))
        assertEquals(finalFs, fs1.unify(fs2),
                "Information transfer into semantic value should work.")

    }


    @Test
    fun testUnify12a() {
        val fs1 = SemanticValue(Constant("q"))
        val fs2 = SemanticValue(FstructVar("?x"))
        val finalFs = SemanticValue(Constant("q"))
        assertEquals(UR(finalFs, mapOf(Pair("?x", Constant("q")))),
                unify(fs1, fs2),
                "Information transfer into semantic value should work and produce binding.")

    }

    @Test
    fun testUnify13() {
        val fs1 = FeatureList(listOf(QueryVariable("?x"), AtomicValue("b")))
        val fs2 = FeatureList(listOf(AtomicValue("a"), QueryVariable("?y")))
        val finalFs = FeatureList(listOf(AtomicValue("a"), AtomicValue("b")))
        assertEquals(finalFs, fs1.unify(fs2),
                "List values can unify")
    }

    @Test
    fun testUnify14() {
        val fs1 = FeatureList(listOf(QueryVariable("?y"), AtomicValue("b")))
        val fs2 = FeatureList(listOf(AtomicValue("a"), QueryVariable("?y")))
        assertNull(fs1.unify(fs2),
                "List values can fail to unify because of inconsistent bindings")
    }

    @Test
    fun testUnify15() {
        val fs1 = FeatureList(listOf(QueryVariable("?y")))
        val fs2 = FeatureList(listOf(AtomicValue("a"), QueryVariable("?y")))
        assertNull(fs1.unify(fs2),
                "List values can fail to unify if different lengths")

    }

    @Test
    fun testUnify16() {
        val fs1 = FeatureList(listOf(AtomicValue("b"), AtomicValue("a")))
        val fs2 = FeatureList(listOf(AtomicValue("a"), AtomicValue("b")))
        assertNull(fs1.unify(fs2),
                "List values can fail to unify if different order")

    }


    @Test
    fun testUnify17() {
        val fs1 = FeatureList(listOf(QueryVariable("?a"), QueryVariable("?b")))
        val fs2 = FeatureList(listOf(QueryVariable("?x"), QueryVariable("?x")))

        assertEquals(FeatureList(listOf(QueryVariable("?x0"), QueryVariable("?x0"))), fs1.unify(fs2))
        assertEquals(FeatureList(listOf(QueryVariable("?x0"), QueryVariable("?x0"))), fs2.unify(fs1))
        assertEquals(fs1.unify(fs2),
                fs2.unify(fs1),
                "result should be symmetric and names canonicalized")
    }


    @Test
    fun testUnify18() {
        val fs1 = FeatureList(listOf(QueryVariable("?a"), QueryVariable("?b"),
                SemanticValue(FstructVar("?z"))))
        val fs2 = FeatureList(listOf(QueryVariable("?x"), QueryVariable("?x"),
                SemanticValue(FstructVar("?z"))))




        assertEquals(fs1.unify(fs2),
                fs2.unify(fs1),
                "result should be symmetric and names canonicalized even in semantic terms")
    }

    @Test
    fun testUnifySem1() {
        val fs1: Lambda = FstructVar("?z")
        val fs2: Lambda = Constant("a")
        assertEquals(fs1.unify(fs2), fs2.unify(fs1),
                "semantic unification should work")
    }

    @Test
    fun testUnifyAtomVsFmap() {
        val fs1 = FeatureNotation.toFs("N")
        val fs2 = FeatureNotation.toFs("N[]")
        assertNotNull(fs1.unify(fs2), "no features feature map can be written two equivalent ways.")
    }




}