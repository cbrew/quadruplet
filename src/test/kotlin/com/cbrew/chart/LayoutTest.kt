package com.cbrew.chart

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class LayoutTest {

    @Test
    fun testLayoutSentence(){
        val layout = ChartLayout(arrayOf("I","need","an","umbrella"))

        println("Sentencez; ${layout.words.joinToString(" ")}")
    }
}