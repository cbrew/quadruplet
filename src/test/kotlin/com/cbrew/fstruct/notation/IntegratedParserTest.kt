package com.cbrew.fstruct.notation

import com.cbrew.chart.Chart
import com.cbrew.chart.FeatureGrammar
import com.cbrew.unify.Grammar
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

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
    fun testTree(){
        // the tree for this grammar looks good.
        val tree = IntegratedParser.toTree(s3)

    }

    @Test
    fun testCfg() {
        val grammar = IntegratedParser.toGrammar(s)

    }

    @Test
    fun testCfg2() {

        val grammar = IntegratedParser.toGrammar(s2)
    }

    @Test
    fun testCfgFromFile() {
        val fileContent = IntegratedParserTest::class.java.getResource("/tiny.cfg").readText()
        val grammar = IntegratedParser.toGrammar(fileContent)
    }


    @Test
    fun testCfgFromFile2() {
        val fileContent = IntegratedParserTest::class.java.getResource("/tiny2.cfg").readText()
        val grammar = FeatureGrammar(IntegratedParser.toGrammar(fileContent) as Grammar)
        val chart = Chart(arrayOf("Chloe", "likes", "John"))
        chart.parse(grammar)

        println(chart.solutions())
    }



}