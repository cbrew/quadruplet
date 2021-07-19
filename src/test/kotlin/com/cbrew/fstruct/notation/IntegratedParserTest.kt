package com.cbrew.fstruct.notation

import org.junit.Ignore
import org.junit.Test

class IntegratedParserTest {

    val s = "\"John\" : Np[num=sing,sem=<john>]\n" +
            "\"Jim\" : Np[num=sing,sem=<jim>]\n" +
            "\"Mary\" : Np[num=sing,sem=<mary>]\n" +
            "\"Tina\" : Np[num=sing,sem=<tina>]\n" +
            "\"Chloe\" : Np[num=sing,sem=<chloe>]\n" +
            "\"Sarah\": Np[num=sing,sem=<sarah>]\n" +
            "\"Sarah Beth\": Np[num=sing,sem=<sarah_beth>]\n" +
            "\"dogs\": Np[num=pl,sem=<forall x . Hünde(x)>]\n" +
            "\"cats\": Np[num=pl,sem=<forall x . Katzem(x)>]\n" +
            "\"a dog\": Np[num=pl,sem=<exists x . dog(x)>]\n" +
            "\"a cat\": Np[num=pl,sem=<exists x . cat(x)>]\n" +
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

    @Test
    fun testCfg() {

        println(IntegratedParser.toGrammar(s))
    }

    @Test
    fun testCfgFromFile() {
        val fileContent = IntegratedParserTest::class.java.getResource("/tiny.cfg").readText()
        println(IntegratedParser.toGrammar(fileContent))
    }


    @Ignore
    @Test
    fun testCfgFromFile2() {
        val fileContent = IntegratedParserTest::class.java.getResource("/tiny.fcfg").readText()
        val grammar = IntegratedParser.toGrammar(fileContent)
    }


}