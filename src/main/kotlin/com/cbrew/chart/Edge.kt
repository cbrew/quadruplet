package com.cbrew.chart

import com.cbrew.unify.Unifiable

sealed class Edge
data class Complete(val category: Unifiable, val start: Int, val end: Int) : Edge()
data class Partial(val category: Unifiable, val start: Int, val end: Int, val needed: List<Unifiable>) : Edge()


val Edge.category: Unifiable
    inline get() = when (this) {
        is Complete -> category
        is Partial -> category
    }

val Edge.start: Int
    inline get() =
        when (this) {
            is Complete -> start
            is Partial -> start
        }

val Edge.end: Int
    inline get() = when (this) {
            is Complete -> end
            is Partial -> end
        }

val Edge.needed: List<Unifiable>
    inline get() = when (this) {
        is Partial -> needed
        is Complete -> listOf()
    }

fun makeEdge(category: Unifiable, start: Int, end: Int, needed: List<Unifiable>): Edge =
        if (needed.size == 0)
            Complete(category, start, end)
        else
            Partial(category, start, end, needed)

fun emptyEdge(category: Unifiable, start: Int, needed: List<Unifiable>): Edge =
        Partial(category, start, start, needed)