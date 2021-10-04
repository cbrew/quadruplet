package com.cbrew.chart


data class Span(val label:String,val start:Int,val end:Int)

/**
 * a chart layout is a set of layers, each containing complete edges. The edges in each layer are
 * non-overlapping, and an edge cannot appear in a layer level with or higher than one that it
 * was responsible fpr creating.
 *
 * TODO this is presentation code, does it belongs on the client side?
 */



class ChartLayout(words: Array<String>){

  val size = words.size
  val layers = mutableListOf<MutableList<Edge>>()
  val words = words.mapIndexed { index, s  -> Span(s,index,index+1)}


  private fun overlaps(e1:Edge,e2:Edge) : Boolean{
    if(e1.start >= e2.end){
      return false
    } else if(e2.start >= e1.end) {
      return false
    }

    return true
  }

  private fun overlaps(e1:Edge,edges:List<Edge>): Boolean = edges.any { overlaps(e1, it) }



  fun add(e:Edge): Unit {
    for(layer in layers) {
      if (!overlaps(e, layer)) {
        layer.add(e)
        return
      }
    }
    // didn't find a layer to add it to
    layers.add(mutableListOf(e))
  }



}