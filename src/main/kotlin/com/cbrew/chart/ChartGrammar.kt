package com.cbrew.chart


interface ChartGrammar {

    // spawn from left corner edge
    fun spawn(lc: Complete): List<Edge>

    // lexical lookup, word or list of words
    // make lexical edges for a sing;e wprd item such as "John" or "Cincinnati"
    fun lookup(word: String, start: Int): List<Edge>

    // make lexical edges for multi-word items such as "New York" or "Betty Sue"
    fun lookup(words: List<String>, start: Int, end: Int): List<Edge>
}