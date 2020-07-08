package com.cbrew.chart

import com.cbrew.unify.FeatureMap

sealed class Tree
data class Node(val category: FeatureMap, val children: List<Tree>) : Tree()
data class Leaf(val category: FeatureMap, val terminals: Array<String>) : Tree()

fun Tree.treestring(nTabs: Int): String =
        treeparts(nTabs).joinToString(separator = "")

fun Tree.treeparts(nTabs: Int): Sequence<String> {
    val prefix = (1..nTabs).map { " " }.asSequence()
    val rep = when (this) {
        is Leaf -> listOf(category.label()) + ":" + terminals.asList() + listOf("\n")
        is Node -> listOf(category.label(), "\n") + children.map { it -> it.treestring(nTabs + 1) }
    }
    return prefix + rep

}