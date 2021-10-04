package com.cbrew.chart

import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.FeatureMap
import com.cbrew.unify.Grammar
import io.javalin.Javalin
import io.javalin.plugin.rendering.vue.VueComponent
import java.util.regex.Pattern
import javax.xml.stream.Location


data class Sentence(val text: String)

fun main() {

    var sentence = Sentence("I want an umbrella")
    var edges = arrayOf(
        Span("Pn" ,0,1),
        Span("Tvb" ,1,2),
        Span("Det",2,3),
        Span("N",3,4))

    val app: Javalin = Javalin.create { config ->
        config.enableWebjars();
    }.start(7000)



    app.get("/sentence"){ctx ->
        ctx.json(sentence)
    }




    app.put("/sentence") { ctx ->
        val gs = """
            N[sem=<\x. product_type(umbrella,x)>] -> "umbrella"
            Tvb[sem=<\X y.X(\x.need(y,x))>] -> "need" | "want"
            Pn[sem=<\P.P(speaker)>] -> "I"
            Det[sem=<\P Q.exists x.(P(x) & Q(x))>] -> "an"|"a"
            # patio grammar
            S[sem=<?subj(?vp)>] -> NP[sem=<?subj>] VP[sem=<?vp>]
            NP[sem=<?det(?nbar)>] -> Det[sem=<?det>] NBAR[sem=<?nbar>]
            NP[sem=<?pn>] -> Pn[sem=<?pn>]
            NBAR[sem=?n] -> N[sem=?n]
            VP[sem=<?v(?obj)>] -> Tvb[sem=<?v>] NP[sem=<?obj>]
        """.trimIndent()
        val toparse = ctx.bodyAsClass<Sentence>()
        val g = FeatureGrammar(IntegratedParser.toGrammar(gs) as Grammar)
        val chart = Chart(toparse.text.split(Pattern.compile("\\s+")).toTypedArray())
        chart.parse(g)
        edges = chart.sortedEdges().toTypedArray().map {Span((it.category as FeatureMap)["cat"].toString(), it.start,it.end)}.toTypedArray()
        sentence = toparse
        ctx.status(204)
    }

    app.get("/edges") { ctx ->
            ctx.json(edges)
        }
    app.put("/edges") { ctx ->
            edges = ctx.bodyAsClass<Array<Span>>()
            ctx.status(204)
        }
    app.get("/", VueComponent("hello-world"))
}