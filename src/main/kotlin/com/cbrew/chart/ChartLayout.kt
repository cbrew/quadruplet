package com.cbrew.chart

import com.cbrew.unify.FeatureMap

/**
 * a chart layout is a set of layers, each containing complete edges. The edges in each layer are
 * non-overlapping, and an edge cannot appear in a layer level with or higher than one that it
 * was responsible fpr creating.
 *
 */



class ChartLayout(val chart:Chart) {

  val layers = mutableListOf<MutableList<Span>>()

  private fun overlaps(e1:Edge,e2:Span) : Boolean{
    if(e1.start >= e2.end){
      return false
    } else if(e2.start >= e1.end) {
      return false
    }

    return true
  }

  private fun overlaps(e1:Edge,edges:List<Span>): Boolean = edges.any { overlaps(e1, it) }

  fun layout(): List<List<Span>> {
    chart.sortedEdges().forEach {edge -> add(edge)}
    return layers
  }

  fun add(e:Edge): Unit {
    val s = Span((e.category as FeatureMap)["cat"].toString(),
                e.start,
                e.end)
    for(layer in layers) {
      if (!overlaps(e, layer)) {
        layer.add(s)
        return
      }
    }
    // didn't find a layer to add it to
    layers.add(mutableListOf(s))
  }



}