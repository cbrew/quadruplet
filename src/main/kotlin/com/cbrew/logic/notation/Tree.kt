package com.cbrew.logic.notation

sealed class Tree
data class Node(val label: String, val children: List<Tree>) : Tree()
data class Leaf(val label: String, val type: String) : Tree()