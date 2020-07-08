package com.cbrew.fstruct.notation


import com.cbrew.unify.*
import kotlin.test.Test
import kotlin.test.assertEquals


class FstructNotationTest {
    @Test
    fun testNotation1() {
        assertEquals(FeatureMap(mapOf(Pair("cat", AtomicValue("Np")))),
                FeatureNotation.toFs("Np"))
    }

    @Test
    fun testNotation2() {
        assertEquals(FeatureMap(mapOf(
                Pair("cat", AtomicValue("Np")),
                Pair("a", AtomicValue("p")),
                Pair("b", AtomicValue("q"))

        )),
                FeatureNotation.toFs("Np[a=p,b=q]"))

    }


    @Test
    fun testNotation3() {
        assertEquals(FeatureMap(mapOf(
                Pair("cat", AtomicValue("Np")),
                Pair("a", AtomicValue("p")),
                Pair("b", QueryVariable("?x"))

        )),
                FeatureNotation.toFs("Np[a=p,b=?x]"))

    }

    @Test
    fun testNotation4() {
        assertEquals(FeatureMap(mapOf(
                Pair("cat", AtomicValue("Np")),
                Pair("a", AtomicValue("p")),
                Pair("b", SemanticValue(Constant("jim"))))

        ),
                FeatureNotation.toFs("Np[a=p,b=<jim>]"))

    }

    @Test
    fun testNotation5() {
        assertEquals(FeatureMap(mapOf(
                Pair("cat", AtomicValue("Np")),
                Pair("a", AtomicValue("p")),
                Pair("b", FeatureList(listOf(AtomicValue("1"),
                        AtomicValue("2"),
                        QueryVariable("?x")))))),
                FeatureNotation.toFs("Np[a=p,b=[1,2,?x]]"))

    }

    @Test
    fun testNotation6() {
        assertEquals(FeatureMap(mapOf(
                Pair("cat", AtomicValue("Np")),
                Pair("a", AtomicValue("p")),
                Pair("b", SemanticValue(Forall(Implies(FstructVar("?x"), QVar(1))))))),
                FeatureNotation.toFs("Np[a=p,b=<forall y . (?x -> y)>]"),
                "semantic values can be complicated things")

    }


    @Test
    fun testGrammar1() {
        assertEquals(Grammar(rules = setOf(
                CfgRule(atomicMap("S"), listOf(atomicMap("Np"), atomicMap("Vp"))),
                CfgRule(atomicMap("S"), listOf(atomicMap("S"), atomicMap("Conj"), atomicMap("S")))
        ),
                lexicon = (mapOf())),
                FeatureNotation.toGrammar("S -> Np Vp | S Conj S"),
                "Two rule grammar.")

    }

    @Test
    fun testGrammar2() {
        assertEquals(Grammar(rules = setOf(
                CfgRule(atomicMap("Np"), listOf(atomicMap("Det"), atomicMap("Nn"))),
                CfgRule(atomicMap("S"), listOf(atomicMap("Np"), atomicMap("Vp"))),
                CfgRule(atomicMap("S"), listOf(atomicMap("S"), atomicMap("Conj"), atomicMap("S")))
        ),
                lexicon = (mapOf())),
                FeatureNotation.toGrammar("S -> Np Vp | S Conj S\nNp -> Det Nn\n\n"),
                "Three rule grammar.")

    }

    @Test
    fun testGrammar3() {
        assertEquals(Grammar(
                rules = setOf(),
                lexicon = (mapOf(
                        Pair("cat", setOf(atomicMap("Nn"))),
                        Pair("dog",
                                setOf(atomicMap("Q"),
                                        atomicMap("Nn"),
                                        atomicMap("V")))))),
                FeatureNotation.toGrammar("\"dog\": Nn | V \n \"dog\": Q \n \"cat\" : Nn \"cat\" : Nn"),
                "lexicon merges and separates rules correctly. get one category for cat and three for dog")

    }

    @Test
    fun testLinseq1() {
        val target = FeatureList(listOf(
                FeatureList(listOf(Integer(0),
                        Integer(1), Integer(2), Integer(3))),
                FeatureList(
                        listOf(Integer(1),
                                Integer(2), Integer(5), Integer(7)))))
        assertEquals(target, LinseqParser.toSequence("(0,1,2,3) (1,2,5,7)"),
                "check sub-parser")
    }


    @Test
    fun testLinseq2() {
        assertEquals(Grammar(rules = setOf(
                McfgRule(atomicMap("S"), listOf(atomicMap("Np"), atomicMap("Vp")),
                        FeatureList(listOf(FeatureList(listOf(
                                FeatureList(listOf(Integer(0), Integer(0))),
                                FeatureList(listOf(Integer(1), Integer(0))))))))),
                lexicon = (mapOf())),
                FeatureNotation.toGrammar("S => Np Vp : <(0,0) (1,0)>"),
                "Grammar with MCFG rule.")

    }


    @Test
    fun testLinseq3() {
        assertEquals(Grammar(rules = setOf(
                McfgRule(atomicMap("Nvp"), listOf(atomicMap("N"), atomicMap("V")),
                        FeatureList(listOf(FeatureList(listOf(
                                FeatureList(listOf(Integer(0), Integer(0))))),
                                FeatureList(listOf(
                                        FeatureList(listOf(Integer(1), Integer(0))))))))),
                lexicon = (mapOf())),
                FeatureNotation.toGrammar("Nvp => N V    : <(0,0)>,<(1,0)>"),
                "Grammar with MCFG having two dimensional yield.")

    }



}
