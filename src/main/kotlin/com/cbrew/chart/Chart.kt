package com.cbrew.chart


import com.cbrew.unify.FeatureMap
import com.cbrew.unify.FeatureStructure
import com.cbrew.unify.Unifiable
import com.cbrew.unify.renamedApartFrom
import com.cbrew.unify.subst
import com.cbrew.unify.unify
import java.math.BigInteger
import java.util.*
import kotlin.Comparator
import kotlin.collections.set


/**
 * A chart is a container for edges. It contains two arrays: one for complete edges, with
 * edges being stored in a bucket at their start point, and one for partial
 * edges, with edges being stored at their end point. It also contains a mutable map
 * that is used to record which partial edges, if any, gave rise to each edge that
 * is created. Some edges will have been created from the lexicon: these are recorded in
 * lexical, since an edge can come from the lexicon and from rules both.
 */

class Chart(val completes: Array<MutableSet<Complete>>,
            val partials: Array<MutableSet<Partial>>,
            val predecessors: MutableMap<Edge, MutableList<Pair<Partial, Complete>>>,
            val spans: MutableList<Span>,
            val sentence: Array<String>) {

    private val agenda: PriorityQueue<Edge> = PriorityQueue(agendaOrder)

    /** The edges that came from the lexicon; some may also be built by rules. */
    val lexical: MutableSet<Edge> = HashSet()

    constructor(sentence: Array<String>) : this(
            completes = Array(sentence.size + 1, { _ -> mutableSetOf<Complete>() }),
            partials = Array(sentence.size + 1, { _ -> mutableSetOf<Partial>() }),
            predecessors = mutableMapOf(),
            spans = mutableListOf<Span>(),
            sentence = sentence)

    fun stats(doCount: Boolean = true) = mapOf(
            Pair("sentence", "" +
                    "\"${sentence.joinToString(" ")}\""),
            Pair("length", sentence.size),
            Pair("numSolutions", solutions().size),
            Pair("numCompletes", completes.sumOf { it.size }),
            Pair("numPartials", partials.sumOf { it.size }),
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

    // Record a predecessor relationship. Each (partial, complete) pair is
    // formed exactly once, when the later of the two enters the chart, so a
    // list holds an edge's predecessors without duplicates.

    private fun recordPredecessors(p: Partial, c: Complete, created: Edge) {
        predecessors.getOrPut(created) { ArrayList(2) }.add(Pair(p, c))
    }

    // count the number of distinct trees under an edge. Sub-forests are
    // shared between many parents, so counts are memoised; without that the
    // count takes time proportional to the (exponential) number of trees.
    // Counts are BigIntegers because they outgrow Int within ~17 words.
    fun countTrees(e: Edge): BigInteger = countTrees(e, HashMap())

    // An edge with no predecessors (a lexical entry, or a partial edge a rule
    // has just spawned) is one tree; so is a lexical edge that rules also build,
    // besides the trees they build.
    private fun countTrees(e: Edge, memo: MutableMap<Edge, BigInteger>): BigInteger =
            memo.getOrPut(e) {
                val preds = predecessors[e]
                val built = preds?.fold(BigInteger.ZERO) { acc, (p, c) ->
                    acc + countTrees(p, memo) * countTrees(c, memo)
                } ?: BigInteger.ZERO
                if (preds == null || e in lexical) built + BigInteger.ONE else built
            }

    fun countTrees(): BigInteger {
        val memo = HashMap<Edge, BigInteger>()
        return solutions().fold(BigInteger.ZERO) { acc, s -> acc + countTrees(s, memo) }
    }


    fun getTrees(e: Edge): Sequence<Tree> {
        val preds = predecessors[e]
        // the base case: a lexical entry, or a partial edge a rule has just spawned
        val base = if (preds == null || e in lexical)
            sequenceOf(if (e.start == e.end) empty(e) else leaf(e))
        else emptySequence()
        return base + (preds?.asSequence()?.flatMap { (p, c) -> getTrees(p, c) } ?: emptySequence())
    }


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
        completes[0].filter { c ->
            c.end == completes.size - 1 && unify(c.category, target.renamedApartFrom(c.category)) != null
        }



    /**
     * Agenda order: left to right by start, then end. The order in which
     * edges come off the agenda does not change the finished chart, so ties
     * are left to the queue rather than broken by comparing (expensive)
     * printed categories, as edgeComparator does for display.
     */
    object agendaOrder : Comparator<Edge> {
        override fun compare(o1: Edge, o2: Edge): Int =
                if (o1.start != o2.start) o1.start - o2.start else o1.end - o2.end
    }

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



    // comparator that puts complete edges first
    class CompleteComparator(val predecessors: MutableMap<Edge, MutableList<Pair<Partial, Complete>>>): Comparator<Complete> {
        fun creates(e1: Complete, e2: Complete): Boolean {
            // read as e1 creates e2
            val pairs = predecessors[e2]
            for ((_, c) in pairs ?: listOf()) {
                if (c == e1) {
                    return true
                }
            }

            return false
        }


        override fun compare(o1: Complete?, o2: Complete?): Int {
            val l1 = o1!!.end - o1.start
            val l2 = o2!!.end - o2.start
            if (l1 != l2)
                return l2 - l1
            else if (creates(o1, o2))
                return 1
            else if (creates(o2, o1))
                return -1
            else
                return edgeComparator.compare(o1, o2)
        }



    }

    fun sortedEdges() = completes.flatMap {it}.sortedWith(CompleteComparator(predecessors))

    fun wordSpans(): List<Span> = spans

    /**
     * bottom-up left-to-right chart parser.
     */
    fun parse(grammar: ChartGrammar) {

        start(grammar)
        while (!done()){
            oneStep(grammar)
        }
    }


    fun oneStep(grammar: ChartGrammar) : Edge? {
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
            return edge
        } else {
            return null
        }
    }


    fun done() = agenda.size == 0

    fun start(grammar:ChartGrammar) {
        for (j in 0 until sentence.size) {
            // 1. Single word lexical entries

            val cats = grammar.lookup(sentence.get(j), j)
            if(cats.size > 0) {
                spans.add(Span(label=sentence.get(j),start=j,end=j+1))
                lexical.addAll(cats)
                agenda.addAll(cats)
            }
            // 2. Multiple word lexical entries ending here
            for (i in 0 until j) {
                val prefix: List<String> = sentence.sliceArray(IntRange(i, j)).toList()
                val cats = grammar.lookup(prefix, i, j + 1)
                if(cats.size > 0) {
                    spans.add(Span(label = prefix.joinToString(" "), start = i, end = j + 1))
                    lexical.addAll(cats)
                    agenda.addAll(cats)
                }

            }
        }
    }

    fun debug(grammar: ChartGrammar) {
        start(grammar)

        while (!done()){
                val edge = oneStep(grammar)
                if(edge != null){
                    println(edge)
                }
        }
    }

    /**
     * fundamental rule of chart parsing.
     * Returns new edge if possible.
     * Returns null if partial and complete are incompatible.
     * The complete edge's variables are renamed apart from the partial's
     * first, since a shared name does not mean a shared variable.
     */
    fun fundamental(partial: Partial, complete: Complete): Edge? =
            unify(partial.needed.first(), renamedApart(complete.category, partial.category, partial.needed))
                    ?.let { (_, bindings) ->
                        makeEdge(bindings.subst(partial.category),
                                partial.start,
                                complete.end,
                                bindings.subst(partial.needed.subList(1, partial.needed.size)))
                    }



    // checks ground first, so the common case allocates nothing
    private fun renamedApart(term: Unifiable, category: Unifiable, needed: List<Unifiable>): Unifiable =
            if (term.ground) term else term.renamedApartFrom(listOf(category) + needed)

    fun nonterminals(): List<Span> {
        return sortedEdges().filter {predecessors.containsKey(it)}.map {Span((it.category as FeatureMap)["cat"].toString(),it.start,it.end)}
    }

    fun preterminals(): List<Span> {
        return sortedEdges().filter {! predecessors.containsKey(it)}.map {Span((it.category as FeatureMap)["cat"].toString(),it.start,it.end)}
    }

    fun fullSemantics(): Map<Int, String> {
        return mapOf()
    }

    fun simpleSemantics(): Map<Int, Predicate> {
        return mapOf()
    }

    fun fullSyntax(): Map<Int, String> {
        return mapOf()
    }

}


