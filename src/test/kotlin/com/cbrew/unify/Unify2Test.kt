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

    // Regression tests for the unifier fixes. Nested feature maps are not
    // supported (see README), so compound values here are lists.

    private fun fs(s: String) = FeatureNotation.toFs(s)

    @Test
    fun testBoundVariableIsRefinedNotJustCompared() {
        assertEquals(fs("X[a=[p, s], b=[p, s]]"),
                fs("X[a=?x, b=?x]").unify(fs("X[a=[p, ?q], b=[?r, s]]")),
                "a bound variable's value should be unified with the new value")
        assertNull(fs("X[a=?x, b=?x]").unify(fs("X[a=[p, ?q], b=[r, s]]")),
                "refinement still fails on a genuine clash")
    }

    @Test
    fun testAliasedVariablesKeepConstraints() {
        assertNull(fs("X[a=?x, b=?x, c=?y]").unify(fs("X[a=?y, b=sg, c=pl]")),
                "?x = ?y = sg clashes with ?y = pl")
        assertEquals(fs("X[a=sg, b=sg, c=sg]"),
                fs("X[a=?x, b=?x, c=?y]").unify(fs("X[a=?y, b=sg, c=?z]")))
    }

    @Test
    fun testUnificationIsOrderIndependent() {
        val a = fs("X[a=?x, b=?x]")
        val b = fs("X[a=sg, b=?z]")
        assertEquals(fs("X[a=sg, b=sg]"), a.unify(b))
        assertEquals(fs("X[a=sg, b=sg]"), b.unify(a))
    }

    @Test
    fun testVariableUnifiesWithItself() {
        assertEquals(QueryVariable("?x0"), QueryVariable("?x").unify(QueryVariable("?x")))
        assertEquals(fs("X[a=?x0, b=?x0]"), fs("X[a=?x, b=?x]").unify(fs("X[a=?y, b=?y]")))
    }

    @Test
    fun testOccursCheck() {
        assertNull(fs("X[a=?x, b=?x]").unify(fs("X[a=?y, b=[?y]]")),
                "binding ?y to a list containing ?y would make a cyclic term")
    }

    @Test
    fun testSubstFollowsChains() {
        val bindings = mapOf<CharSequence, Unifiable>(
                Pair("?x", (fs("X[a=[p, ?y]]") as FeatureMap)["a"]!!),
                Pair("?y", AtomicValue("sg")))
        assertEquals(fs("X[a=[p, sg]]"), bindings.subst(fs("X[a=?x]")))

        val sem = mapOf<CharSequence, Unifiable>(
                Pair("?f", FstructVar("?g")),
                Pair("?g", App(Constant("p"), FstructVar("?h"))),
                Pair("?h", Constant("c")))
        assertEquals(SemanticValue(App(Constant("p"), Constant("c"))),
                sem.subst(SemanticValue(FstructVar("?f"))))
    }

    @Test
    fun testCanonicalizeRenamesEveryVariable() {
        assertEquals(SemanticValue(App(FstructVar("?x0"), FstructVar("?x1"))),
                SemanticValue(App(FstructVar("?f"), FstructVar("?g"))).canonicalize(),
                "variables in the second half of an application are renamed too")
        assertEquals(fs("X[a=?x0, b=?x1]"), fs("X[a=?x1, b=?x0]").canonicalize(),
                "renaming is simultaneous, so swapping names does not chain")
    }

    @Test
    fun testRenamedApartFrom() {
        assertEquals(fs("X[a=?b, b=?a2, c=?a11]"),
                fs("X[a=?b, b=?a, c=?a1]").renamedApartFrom(fs("Y[d=?a, e=?a1]")),
                "only clashing variables are renamed, to names unused on either side")
        val mixed = FeatureMap(mapOf(Pair("cat", AtomicValue("X")),
                Pair("a", QueryVariable("?s")), Pair("sem", SemanticValue(FstructVar("?s")))))
        assertEquals(FeatureMap(mapOf(Pair("cat", AtomicValue("X")),
                Pair("a", QueryVariable("?s1")), Pair("sem", SemanticValue(FstructVar("?s1"))))),
                mixed.renamedApartFrom(QueryVariable("?s")),
                "a syntactic and a semantic variable sharing a name each keep their kind")
    }
}
