package com.cbrew.quadruplet.servers

import com.cbrew.chart.Chart
import com.cbrew.chart.ChartResult
import com.cbrew.chart.FeatureGrammar
import com.cbrew.fstruct.notation.IntegratedParser
import com.cbrew.unify.Grammar
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.plugin.openapi.annotations.*
import java.util.regex.Pattern
import kotlin.math.min

// The design is close to that of the Microsoft Direct Line API 3.0
// https://docs.microsoft.com/en-us/azure/bot-service/rest-api/bot-framework-rest-direct-line-3-0-concepts

data class TokenParameters(val conversationId:String)



var grammarName = "patio.fcfg"

fun parse(text:String): Chart {
    val g = loadGrammar(grammarName)
    val chart = Chart(text.split(Pattern.compile("\\s+")).toTypedArray())
    chart.parse(g)
    return chart
}

fun parseToResult(text: String): ChartResult {
    val chart = parse(text)
    return ChartResult(wordSpans=chart.wordSpans(),
        nonTerminals = chart.nonterminals(),
        preTerminals = chart.preterminals(),
        fullSemantics = chart.fullSemantics(),
        simpleSemantics = chart.simpleSemantics(),
        fullSyntax = chart.fullSyntax())
}

fun loadGrammar(name: String) : FeatureGrammar {
    val gs =  object {}.javaClass.getResource("/grammars/${name}")?.readText() ?: throw NotFoundResponse("grammar not found")

    return FeatureGrammar(IntegratedParser.toGrammar(gs) as Grammar)
}

// Activities may be created by both the system and the user.
// c.f. https://docs.microsoft.com/en-us/azure/bot-service/rest-api/bot-framework-rest-connector-api-reference
// ?view=azure-bot-service-4.0#activity-object
// for how complex this thing becomes in the Microsoft API
sealed class Activity
data class UserTextActivity(val text: String) : Activity()
data class SystemTextActivity(val text: String): Activity()
data class ActivityRequest(val text:String, val speaker: String)
data class ActivitySet(val watermark: Int, val activities: MutableList<Activity>)

data class Conversation(val conversationId: String,
                        val activities: MutableList<Activity> ) {
    constructor(conversationId: String) : this(conversationId = conversationId, activities = mutableListOf())
}

// class for displaying info about a conversation, showing watermark
// will work even if no activities yet.
data class InfoWithWatermark(val conversationId: String,
                             val watermark: Int,
    val pre: List<Activity>,
    val post:List<Activity>) {
    constructor(conv:Conversation, watermark: Int): this(
        conv.conversationId,
        watermark,
        conv.activities.slice(0 until watermark),
        if (conv.activities.size > 0) conv.activities.slice(
            min(conv.activities.size,watermark)
                    until conv.activities.size)
        else listOf())

}



object ConversationController {

    private val conversations = mutableMapOf<String,Conversation>()


    @OpenApi(
        summary = "Create conversation",
        operationId = "startConversation",
        tags = ["Conversation"],
        requestBody = OpenApiRequestBody([OpenApiContent(TokenParameters::class)]),
        responses = [
            OpenApiResponse("201"),
            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)])
        ]
    )
    // call:  `curl -X POST localhost:7001/conversations   -d '{"conversationId":"conv1"}`
    fun startConversation(ctx: Context){
        val params = ctx.bodyAsClass<TokenParameters>()
        val conv = Conversation(params.conversationId)

        conversations[conv.conversationId] =conv
        ctx.json(conv)
        ctx.status(201)
    }

    @OpenApi(
        summary = "Show the activities for a conversation",
        operationId = "getActivities",
        tags = ["Conversation"],
        queryParams = [
            OpenApiParam(name = "watermark", type = Integer::class, description= "The last activity the client saw")
        ],
        pathParams = [OpenApiParam("conversationId", String::class, "The conversation ID")],
        responses = [
            OpenApiResponse("200"),
            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)])
        ]
    )
    fun getActivities(ctx: Context){
        val conversationId = ctx.pathParam("conversationId")
        val conversation = conversations[conversationId]
            ?: throw NotFoundResponse("Conversation not found")
        val watermark: Int = ctx.queryParam("watermark")?.toIntOrNull() ?: 0

        ctx.json(ActivitySet(watermark=watermark,activities=conversation.activities))

    }

    // curl -X POST 'localhost:7001/conversations/za/activities'  -d '{"text":"hello","speaker":"user"}'
    @OpenApi(
        summary = "Post an activity",
        operationId = "sendActivity",
        tags = ["Conversation"],
        requestBody = OpenApiRequestBody([OpenApiContent(ActivityRequest::class)]),
        responses = [
            OpenApiResponse("201"),
            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)])
        ]
    )
    fun sendActivity(ctx: Context){
        val conversationId = ctx.pathParam("conversationId")
        val conversation = conversations[conversationId]
            ?: throw NotFoundResponse("Conversation not found")


        val activityRequest = ctx.bodyAsClass<ActivityRequest>()
        val activity = when (activityRequest.speaker){
            "system" -> SystemTextActivity(text=activityRequest.text)
            "user" -> UserTextActivity(text=activityRequest.text)
            else -> throw BadRequestResponse("speaker should be system or user")
        }
        conversation.activities.add(activity)

        ctx.json(ResourceResponse(id=conversation.activities.size-1))
        ctx.status(201)
    }

    // curl -X GET localhost:7001/conversations/{conversationId}?watermark={watermark_value}
    // which will return a conversation or raise a Not Found error response.
    @OpenApi(
        summary = "Show the information about a conversation",
        operationId = "getConversationInfo",
        tags = ["Conversation"],
        queryParams = [
            OpenApiParam(name = "watermark", type = Integer::class,description= "The last activity the client saw")
        ],
        pathParams = [

            OpenApiParam("conversationId", String::class, "The conversation ID")],
        responses = [
            OpenApiResponse("200"),
            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)])
        ]
    )
    fun getConversationInfo(ctx: Context){
        val conversationId = ctx.pathParam("conversationId")
        val conversation = conversations[conversationId]
            ?: throw NotFoundResponse("Conversation not found")
        val watermark: Int = ctx.queryParam("watermark")?.toIntOrNull() ?: 0

        ctx.json(InfoWithWatermark(conversation,watermark))


    }

    // parsing is a GET operation, does not change state
    // of conversation. However, when a parse is requested
    // the server caches the result, and will re-use it when
    // the same result is re-requested.



    @OpenApi(
        summary = "Show the parse of the  given activity of the given conversation",
        operationId = "parse",
        tags = ["Conversation"],
        pathParams = [OpenApiParam("conversationId", String::class, "The conversation ID"),
            OpenApiParam("messageId", Int::class, "The message ID")],
        responses = [
            OpenApiResponse("200"),
            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)])
        ]
    )
    fun parse(ctx:Context){
        val conversationId = ctx.pathParam("conversationId")
        val conversation = conversations[conversationId]
            ?: throw NotFoundResponse("Conversation not found")
        val activityId = ctx.pathParamAsClass<Int>("messageId").check({it < conversation.activities.size},
                "Activity ID should be within bounds 0:${conversation.activities.size}").get()
        val activity = conversation.activities[activityId]
        ctx.json(parseToResult(
            when(activity) {
                is UserTextActivity -> activity.text
                is SystemTextActivity -> activity.text
            }))
    }
    // reference is a GET operation, does not change state
    // of conversation. However, it may trigger parsing of
    // utterances previous to the current one.
    @OpenApi(
        summary = "Show the reference info of the  given activity of the given conversation",
        operationId = "reference",
        tags = ["Conversation"],
        pathParams = [OpenApiParam("conversationId", String::class, "The conversation ID"),
            OpenApiParam("messageId", Int::class, "The message ID")],
        responses = [
            OpenApiResponse("200"),
            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)])
        ]
    )
    fun reference(ctx:Context){
        ctx.json("reference")
    }

}


