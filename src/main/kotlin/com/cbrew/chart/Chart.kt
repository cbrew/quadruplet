package com.cbrew.chart

import com.cbrew.unify.FeatureMap
import com.cbrew.unify.FeatureStructure
import com.cbrew.unify.subst
import com.cbrew.unify.unify
import java.util.*
import kotlin.collections.set

/**
 * A chart is a container for edges. It contains two arrays: one for complete edges, with
 * edges being stored in a bucket at their start point, and one for partial
 * edges, with edges being stored at their end point. It also contains a mutable map
 * that is used to record which partial edges, if any, gave rise to each edge that
 * is created. Some edges will have been created from the lexicon, so will have no predecessors.
 */

data class Chart(val completes: Array<MutableSet<Complete>>,
                 val partials: Array<MutableSet<Partial>>,
                 val predecessors: MutableMap<Edge, MutableSet<Pair<Partial, Complete>>>,
                 val sentence: Array<String>) {

    constructor(sentence: Array<String>) : this(
            completes = Array(sentence.size + 1, { _ -> mutableSetOf<Complete>() }),
            partials = Array(sentence.size + 1, { _ -> mutableSetOf<Partial>() }),
            predecessors = mutableMapOf(),
            sentence = sentence)

    fun stats(doCount: Boolean = true) = mapOf(
            Pair("sentence", "" +
                    "\"${sentence.joinToString(" ")}\""),
            Pair("length", sentence.size),
            Pair("numSolutions", solutions().size),
            Pair("numCompletes", completes.sumBy { it.size }),
            Pair("numPartials", partials.sumBy { it.size }),
            Pair("numTrees", if (doCount) countTrees() else "\"not counted\""))

    fun add(e: Edge): Boolean =
            when (e) {
                is Complete -> completes[e.start].add(e)
                is Partial -> partials[e.end].add(e)
            }

    fun pairwithpartials(c: Complete): List<Edge> =
            partials[c.start].mapNotNull { p -> fundamental(p, c)?.let { e -> recordPredecessors(p, c, e); e } }

    fun pairwithcompletes(p: Partial): List<Edge> =
            completes[p.end].mapNotNull { c -> fundamental(p, c)?.let { e -> recordPredecessors(p, c, e); e } }

    // record a predecessor relationship

    private fun recordPredecessors(p: Partial, c: Complete, created: Edge) {
        val pair = Pair(p, c)
        if (created in predecessors)
            predecessors[created]?.add(pair)
        else
            predecessors[created] = mutableSetOf(pair)
    }

    // count the number of distinct trees under an edge
    fun countTrees(e: Edge): Int =
            if (e in predecessors)
                predecessors[e]!!.sumBy { (p, c) -> countTrees(p) * countTrees(c) }
            else
                1

    fun countTrees(): Int =
            solutions().sumBy { countTrees(it) }


    fun getTrees(e: Edge): Sequence<Tree> =
            predecessors[e]?.asSequence()?.flatMap { (p, c) -> getTrees(p, c) }
                    ?:

                    /**
                     * if there were no predecessors, we will have gotten null
                     * above, and we have the base case
                     */

                    sequenceOf(if (e.start == e.end)
                        empty(e)
                    else
                        leaf(e))


    fun getTrees(p: Partial, c: Complete): Sequence<Tree> =
        getTrees(p).flatMap { t1 -> getTrees(c).map { t2 ->
                val node = t1 as Node
                Node(node.category,
                        node.children + listOf(t2))
            }
        }

    fun empty(e: Edge): Node = Node(e.category as FeatureMap, emptyList<Tree>())

    // leaf() creates
    // EITHER a lexical edge with a label and words.
    // OR an empty edge with a label but no words.
    // The code is the same either way.
    fun leaf(e: Edge) =
            Leaf(e.category as FeatureMap, sentence.sliceArray(IntRange(e.start, e.end - 1)))

    fun solutions(): List<Complete> {
        return completes[0].filter { c -> c.end == completes.size - 1 }
    }


    fun solutions(target: FeatureStructure): List<Complete> =
        completes[0].filter { c -> c.end == completes.size - 1 && (unify(c.category, target) != null) }



    object edgeComparator : Comparator<Edge> {
        override fun compare(o1: Edge?, o2: Edge?): Int =
                if (o1!!.start != o2!!.start)
                    o1.start - o2.start
                else if (o1.end != o2.end)
                    o1.end - o2.end
                else if (o1.category != o2.category)
                    o1.category.toString().compareTo(o2.category.toString())
                else
                    o1.needed.toString().compareTo(o2.needed.toString())
    }


    /**
     * bottom-up left-to-right chart parser.
     */
    fun parse(grammar: ChartGrammar) {
        // val agenda: MutableList<Edge> = mutableListOf()
        val agenda: PriorityQueue<Edge> = PriorityQueue(edgeComparator)

        for (j in 0..sentence.size - 1) {
            // 1. Single word lexical entries
            agenda.addAll(grammar.lookup(sentence.get(j), j))
            // 2. Multiple word lexical entries ending here
            for (i in 0..j - 1) {
                val prefix: List<String> = sentence.sliceArray(IntRange(i, j)).toList()
                agenda.addAll(grammar.lookup(prefix, i, j + 1))
            }
            while (agenda.size > 0) {
                val edge = agenda.remove()
                if (add(edge)) {
                    when (edge) {
                        is Complete -> {
                            agenda.addAll(grammar.spawn(edge))
                            agenda.addAll(pairwithpartials(edge))
                        }
                        is Partial -> {
                            agenda.addAll(pairwithcompletes(edge))
                        }

                    }
                }
            }
        }
    }


    /**
     * fundamental rule of chart parsing.
     * Returns new edge if possible.
     * Returns null if partial and complete are incompatible.
     */
    fun fundamental(partial: Partial, complete: Complete): Edge? =
            unify(partial.needed.first(), complete.category)
                    ?.let { (_, bindings) ->
                        makeEdge(bindings.subst(partial.category),
                                partial.start,
                                complete.end,
                                bindings.subst(partial.needed.subList(1, partial.needed.size)))
                    }

}


