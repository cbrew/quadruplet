package com.cbrew.quadruplet.servers
import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.openapiDsl
import com.cbrew.chart.*
import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.Grammar
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.core.JavalinConfig
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import java.util.regex.Pattern

fun getConfiguredOpenApiPlugin() = OpenApiPlugin(
    OpenApiOptions {
        openapiDsl {
            info {
                title = "User API"
                description = "Demo API with 5 operations"
                version = "1.0.0"
            }
        }
    }.apply {
        path("/swagger-docs") // endpoint for OpenAPI json
        swagger(SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
        reDoc(ReDocOptions("/redoc")) // endpoint for redoc
        defaultDocumentation { doc ->
            doc.json("500", ErrorResponse::class.java)
            doc.json("503", ErrorResponse::class.java)
        }
    }
)

fun main() {




    Javalin.create {config: JavalinConfig->
        // config.registerPlugin(getConfiguredOpenApiPlugin())
        config.defaultContentType = "application/json"
        config.enableWebjars()

    }.routes {
        path("conversations"){
            post(ConversationController::startConversation)
            path("{conversationId}"){
                get(ConversationController::getConversationInfo)
                path("activities"){
                    get(ConversationController::getActivities)
                    post(ConversationController::sendActivity)
                }
                path("parse"){
                    path("{messageId}"){
                        get(ConversationController::parse)
                    }
                }
                path("ref"){
                    path("{messageId}"){
                        get(ConversationController::reference)
                    }

                }

            }
        }
    }.start(7001)

    /*
    println("Check out ReDoc docs at http://localhost:7001/redoc")
    println("Check out Swagger UI docs at http://localhost:7001/swagger-ui")
    */



    /*

    app.get("/edges") {ctx ->
        ctx.json(edges)
    }


    // List the available grammars
    // Currently only one.
    app.get("/grammars") {ctx ->


        val name = "patio.fcfg"
        val g = loadGrammar(name)
        grammars.put(name,g)
        ctx.json(grammars.keys)
    }


    app.post("/msg") {ctx ->
        val message = ctx.bodyAsClass<Sentence>()
        msg = message.text
        ctx.json(message)
    }


    app.get("/msg") { ctx ->
        ctx.json(Sentence(text=msg))
    }

    app.routes{
        path("conversation"){
            get(ConversationController::getAllConversations)

        }
    }


    */


    /*
    app.get("/msg") { ctx ->
            ctx.json(Sentence(text=msg))
    }
    app.post("/msg") { ctx ->
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



    app.post("/parse") { ctx ->
        val r = parseToResult(msg)
        result = r
        ctx.json(r)
    }

    app.get("/", VueComponent("richnlg"))

    */

}
