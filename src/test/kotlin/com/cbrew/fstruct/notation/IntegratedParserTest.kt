package com.cbrew.fstruct.notation

import com.cbrew.chart.Chart
import com.cbrew.chart.FeatureGrammar
import com.cbrew.unify.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IntegratedParserTest {

    //  This grammar format is deprecated, want to be closer to NLTK, see below
    val s = "\"John\" : Np[num=sing,sem=<john>]\n" +
            "\"Jim\" : Np[num=sing,sem=<jim>]\n" +
            "\"Mary\" : Np[num=sing,sem=<mary>]\n" +
            "\"Tina\" : Np[num=sing,sem=<tina>]\n" +
            "\"Chloe\" : Np[num=sing,sem=<chloe>]\n" +
            "\"Sarah\": Np[num=sing,sem=<sarah>]\n" +
            "\"Sarah Beth\": Np[num=sing,sem=<sarah_beth>]\n" +
            "\"dogs\": Np[num=pl,sem=<forall x . Hünde(x)>]\n" +
            "\"cats\": Np[num=pl,sem=<forall x . Katzen(x)>]\n" +
            "\"a dog\": Np[num=pl,sem=<forall x . dog(x)>]\n" +
            "\"a cat\": Np[num=pl,sem=<forall x . cat(x)>]\n" +
            "\"and\": Conj[type=and]\n" +
            "\"or\" : Conj[type=or]\n" +
            "\"liked\": V[sem=<\\x y. like(y,x)>]\n" +
            "\"likes\": V[sem=<\\x y. like(y,x)>,num=sing]\n" +
            "\"like\": V[sem=<\\x y. like(y,x)>,num=pl]\n" +
            "S[num=?n,sem=<?vp(?np)>] => Np[sem=<?np>,num=?n] Vp[sem=<?vp>,num=?n] : <(0 0) (1 0)>\n" +
            "Np[sem=<?np1 & ?np2>,num=pl] -> Np[sem=<?np1>] Conj[type=and] Np[sem=<?np2>]\n" +
            "Np[sem=<?np1 | ?np2>,num=?n] ->  Np[sem=<?np1>] Conj[type=or] Np[sem=<?np2>,num=?n]\n" +
            "Vp[num=?n,sem=<?v(?np)>] -> V[num=?n,sem=<?v>] Np[sem=<?np>]\n" +
            "# Bridge semantics: https://web.stanford.edu/~laurik/publications/bridge.pdf\n" +
            "# needs packed rewrites."

    // This grammar is in NLTK format
    val s2 =
            "Np[num=sing,sem=<john>] -> \"John\"" +
            "Np[num=sing,sem=<jim>] -> \"Jim\"" +
            "Np[num=sing,sem=<mary>] -> \"Mary\"" +
            "Np[num=sing,sem=<tina>] -> \"Tina\"\n" +
            " Np[num=sing,sem=<chloe>] -> \"Chloe\" \n" +
            "Np[num=sing,sem=<sarah>] -> \"Sarah\"" +
            "Np[num=sing,sem=<sarah_beth>] -> \"Sarah Beth\"\n" +
            "Np[num=pl,sem=<forall x . Hünde(x)>] -> \"dogs\"\n" +
            "Np[num=pl,sem=<forall x . Katzen(x)>] -> \"cats\"\n" +
            "Np[num=pl,sem=<exists x . Hund(x)>] -> \"a dog\"\n" +
            "Np[num=pl,sem=<exists x . Katze(x)>] -> \"a cat\"\n" +
            "Conj[type=and] -> \"and\"\n" +
            "Conj[type=or] -> \"or\"\n" +
            "V[sem=<\\x y. like(y,x)>] -> \"liked\"\n" +
            "V[sem=<\\x y. like(y,x)>,num=sing]-> \"likes\"\n" +
            "V[sem=<\\a b. like(a,b)>,num=pl] -> \"like\"\n" +
            "S[num=?n,sem=<?vp(?np)>] -> Np[sem=<?np>,num=?n] Vp[sem=<?vp>,num=?n]\n" +
            "Np[sem=<?np1 & ?np2>,num=pl] -> Np[sem=<?np1>] Conj[type=and] Np[sem=<?np2>]\n" +
            "Np[sem=<?np1 | ?np2>,num=?n] ->  Np[sem=<?np1>] Conj[type=or] Np[sem=<?np2>,num=?n]\n" +
            "Vp[num=?n,sem=<?v(?np)>] -> V[num=?n,sem=<?v>] Np[sem=<?np>]\n"


    val s3 = """
    "a dog": Np[num=pl,sem=<exists x . dog(x)>]
    "a cat": Np[num=pl,sem=<exists x . cat(x)>]
    Np[num=pl,sem=<exists x . pelican(x)>] -> "a pelican"
    """





    @Test
    fun testListValue(){

        val gs = """
        Np[agr=[a,b,c]] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureList
        assertEquals(FeatureList(listOf(AtomicValue("a"), AtomicValue("b"), AtomicValue("c"))),cat)
    }


    @Test
    fun testTupleValue(){

        val gs = """
        Np[agr=(a,b,c)] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureTuple
        assertEquals(FeatureTuple(listOf(AtomicValue("a"), AtomicValue("b"),AtomicValue("c"))),cat)
    }


    @Test
    fun testListExpression(){

        val gs = """
        Np[agr=[?x + ?y]] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureListExpression
        assertEquals(FeatureListExpression(listOf(QueryVariable("?x"),
            QueryVariable("?y"))),cat)
    }


    @Test
    fun testListExpressionWithList(){

        val gs = """
        Np[agr=[?x + [a]]] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureListExpression
        assertEquals(FeatureListExpression(listOf(
            QueryVariable("?x"),
            FeatureList(listOf(AtomicValue("a"))))),cat)
    }

    @Test
    fun testListExpressionSimplify(){

        val gs = """
        Np[agr=[[b] + [a]]] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureListExpression
        val simplified = cat.simplify()
        assertEquals(
            FeatureList(listOf(AtomicValue("b"),AtomicValue("a"))),
            simplified)


    }

    @Test
    fun testListExpressionSimplify2(){
        // simplify where second element is tuple
        val gs = """
        Np[agr=[[b] + (a)]] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureListExpression
        val simplified = cat.simplify()
        assertEquals(
            FeatureList(listOf(AtomicValue("b"),AtomicValue("a"))),
            simplified)

    }


    @Test
    fun testListExpressionSimplify3(){
        // simplify where second element is atom
        val gs = """
        Np[agr=[[b] + a]] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureListExpression
        val simplified = cat.simplify()
        assertEquals(
            FeatureList(listOf(AtomicValue("b"),AtomicValue("a"))),
            simplified)

    }

    @Test
    fun testListExpressionSimplify4(){
        // dont simplify where second element is variable
        val gs = """
        Np[agr=[[b] + ?a]] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureListExpression
        val simplified = cat.simplify()
        assertEquals(
            FeatureListExpression(listOf(AtomicValue("b"),QueryVariable("?a"))),
            simplified)

    }


    @Test
    fun testListExpressionSimplify5(){
        // simplify where second element is category
        val gs = """
        Np[agr=[[a] + S[num=sing]]] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cat: Unifiable? = grammar.lexicon["alphabet"]?.elementAt(0)?.get("agr")
        val simplified = (cat as FeatureListExpression).simplify()
        assertEquals(FeatureList(listOf(
            AtomicValue("a"),
            FeatureMap(mapOf("cat" to AtomicValue("S"), "num" to AtomicValue("sing")))
        )),simplified)
    }





    @Test
    fun testTupleExpression(){

        val gs = """
        Np[agr=(?x + ?y)] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureTupleExpression
        assertEquals(FeatureTupleExpression(listOf(QueryVariable("?x"),
            QueryVariable("?y"))),cat.simplify())
    }


    @Test
    fun testTupleExpression1(){

        val gs = """
        Np[agr=(a + ?y)] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureTupleExpression
        assertEquals(FeatureTupleExpression(listOf(AtomicValue("a"),
            QueryVariable("?y"))),cat.simplify())
    }

    @Test
    fun testTupleExpression2(){

        val gs = """
        Np[agr=(?y + a)] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureTupleExpression
        assertEquals(FeatureTupleExpression(listOf(QueryVariable("?y"),
            AtomicValue("a"))),cat.simplify())
    }


    @Test
    fun testTupleExpressionSimplify(){

        val gs = """
        Np[agr=((a,b) + (c,d))] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureTupleExpression
        val simplified = cat.simplify()
        assertEquals(
            FeatureTuple(listOf(
                AtomicValue("a"),
                AtomicValue("b"),
                AtomicValue("c"),
                AtomicValue("d"))),
            simplified)

    }

    @Test
    fun testTupleExpressionSimplify1(){

        val gs = """
        Np[agr=((a,b) + c)] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureTupleExpression
        val simplified = cat.simplify()
        assertEquals(
            FeatureTuple(listOf(
                AtomicValue("a"),
                AtomicValue("b"),
                AtomicValue("c"))),
            simplified)

    }

    @Test
    fun testTupleExpressionSimplify2(){

        val gs = """
        Np[agr=(a + (b,c))] -> "alphabet"
        """
        val grammar  = IntegratedParser.toGrammar(gs) as Grammar
        val cats: Set<FeatureMap>? = grammar.lexicon["alphabet"]
        assertNotNull(cats)
        val cat = cats.elementAt(0)["agr"] as FeatureTupleExpression
        val simplified = cat.simplify()
        assertEquals(
            FeatureTuple(listOf(
                AtomicValue("a"),
                AtomicValue("b"),
                AtomicValue("c"))),
            simplified)

    }

    @Test
    fun testTree(){
        // the tree for this grammar looks good.
        val tree = IntegratedParser.toTree(s3)
        assertNotNull(tree)
    }

    @Test
    fun testCfg() {
        val grammar = IntegratedParser.toGrammar(s)
        assertNotNull(grammar)
    }

    @Test
    fun testCfg2() {

        val grammar = IntegratedParser.toGrammar(s2)
        assertNotNull(grammar)
    }

    @Test
    fun testCfgFromFile() {
        val fileContent = IntegratedParserTest::class.java.getResource("/tiny.cfg").readText()
        val grammar = IntegratedParser.toGrammar(fileContent)
        assertNotNull(grammar)
    }


    @Test
    fun testCfgFromFile2() {
        val fileContent = IntegratedParserTest::class.java.getResource("/tiny2.cfg").readText()
        val grammar = FeatureGrammar(IntegratedParser.toGrammar(fileContent) as Grammar)
        val chart = Chart(arrayOf("Chloe", "likes", "John"))
        chart.parse(grammar)
        val sols = chart.solutions()
        assertNotNull(sols)
    }

    @Test
    fun testCfgEquality() {
        val fileContent1 = IntegratedParserTest::class.java.getResource("/tiny.cfg").readText()
        val grammar1 = FeatureGrammar(IntegratedParser.toGrammar(fileContent1) as Grammar)
        val fileContent2 = IntegratedParserTest::class.java.getResource("/tiny2.cfg").readText()
        val grammar2 = FeatureGrammar(IntegratedParser.toGrammar(fileContent2) as Grammar)
        assertEquals(grammar2.grammar, grammar1.grammar)


    }




}