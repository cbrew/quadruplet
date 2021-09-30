package com.cbrew.chart

import io.javalin.Javalin
import io.javalin.plugin.rendering.vue.VueComponent


fun main() {
    val app = Javalin.create { config ->
        config.enableWebjars()
    }.start(7000)


    app.get("/", VueComponent("hello-world"))
}