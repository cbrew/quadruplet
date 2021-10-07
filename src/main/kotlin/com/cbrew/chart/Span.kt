package com.cbrew.chart

/**
 * Simple data structure for parse results.
 */
data class Span(val id: Int, val label:String,val start:Int,val end:Int) {
  constructor(label:String,start: Int, end:Int): this(
    id= id(),
    label=label,
    start=start,
    end=end)

  companion object {
    private var nextId = -1
    fun id(): Int {
      nextId += 1
      return nextId
    }
  }
}