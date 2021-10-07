package com.cbrew.quadruplet.servers
import com.cbrew.chart.*
import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.FeatureMap
import com.cbrew.unify.Grammar
import io.javalin.Javalin
import io.javalin.plugin.rendering.vue.VueComponent
import java.util.regex.Pattern

data class Sentence(val text: String)
data class Message(val id: Int, val msg: String)

val gs = """
            N[sem=<\x. product_type(umbrella,x)>] -> "umbrella" 
            N[sem=<\x. emotion(need,x)>] -> "need" 
            Tvb[sem=<\X y.X(\x.need(y,x))>] -> "need" | "want" | "needs" | "wants"
            Tvb[sem=<\X y.X(\x.have(y,x))>] -> "have" | "has"
            Pn[sem=<\P.P(speaker)>] -> "I" 
            Pn[sem=<\P.P(beth_ann)>] -> "Beth Ann" | "Beth" 
            Pn[sem=<\P.P(john)>] -> "John" | "John Paul"  
            Det[sem=<\P Q.exists x.(P(x) & Q(x))>] -> "an"|"a"
            # grammar rules
            S[sem=<?subj(?vp)>] -> NP[sem=<?subj>] VP[sem=<?vp>]
            NP[sem=<?det(?nbar)>] -> Det[sem=<?det>] NBAR[sem=<?nbar>]
            NP[sem=<?pn>] -> Pn[sem=<?pn>]
            NBAR[sem=?n] -> N[sem=?n]
            VP[sem=<?v(?obj)>] -> Tvb[sem=<?v>] NP[sem=<?obj>]
        """.trimIndent()

fun parse(text:String): Chart {
    val g = FeatureGrammar(IntegratedParser.toGrammar(gs) as Grammar)
    val chart = Chart(text.split(Pattern.compile("\\s+")).toTypedArray())
    chart.parse(g)
    return chart
}

fun parseToResult(text: String): ChartResult{
    val chart = parse(text)

    return ChartResult(wordSpans=chart.wordSpans(),
                        nonTerminals = chart.nonterminals(),
                        preTerminals = chart.preterminals(),
                        fullSemantics = chart.fullSemantics(),
                        simpleSemantics = chart.simpleSemantics(),
                        fullSyntax = chart.fullSyntax())
}


fun main(args: Array<String>) {

    var msg = "I want an umbrella"
    var edges: List<List<Span>> = listOf()
    var post:Message=Message(id=12,msg="Twelve angry men")
    var result: ChartResult


    val app = Javalin.create {
        it.enableWebjars()
    }.start(7000)

    app.get("/edges") {ctx ->
        ctx.json(edges)
    }

    app.get("/msg") { ctx ->
            ctx.json(Sentence(text=msg))
    }
    app.post("/msga") { ctx ->
        val chart = parse("I need an umbrella")
        val newEdges  = mutableListOf<List<Span>>()
        for (edgeset in chart.completes){
            val innerList: MutableList<Span> = mutableListOf()
            for(edge in edgeset){
                innerList.add(Span((edge.category as FeatureMap)["cat"].toString(),edge.start,edge.end))
            }
            newEdges.add(innerList)
        }
        edges = newEdges
        ctx.status(204)
    }


    app.get("/post") {ctx ->
        ctx.json(post)
    }

    app.post("/msg") {ctx ->
        val message = ctx.bodyAsClass<Message>()
        msg = message.msg
        ctx.json(message)
    }

    app.post("/parse") { ctx ->
        val r = parseToResult(msg)
        result = r
        ctx.json(r)
    }

    app.get("/", VueComponent("richnlg"))

}
