package com.cbrew.chart

/**
 * a chart layout is a set of layers, each containing complete edges. The edges in each layer are
 * non-overlapping, and an edge cannot appear in a layer level with or higher than one that it
 * was responsible fpr creating.
 */



class ChartLayout(words: Array<String>){

  val size = words.size
  val layers = mutableListOf<MutableList<Edge>>()
  val words = words

}