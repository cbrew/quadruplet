package com.cbrew.unify

sealed class Unifiable {

    /**
     * True if the term contains no variables, so that substitution and
     * renaming can return it unchanged. Composite terms compute this once,
     * when they are built (terms are immutable), and also cache their hash
     * codes, so hashing never re-walks the term. Feature structures use the
     * hash the data class would generate. Composite lambda terms mix in a
     * per-constructor salt, and And/Or scramble each element's hash before
     * summing, because the generated hashes made Exists(b), Forall(b), Lam(b)
     * and Not(b) all collide with b, and PP-attachment variants of the same
     * formula then share buckets. List and tuple expressions are never
     * ground, since substitution may still simplify them.
     */
    open val ground: Boolean get() = true


    fun label(): String =
            when (this) {
                is AtomicValue -> value
                is FeatureMap -> "${get("cat")?.label()}[${get("f")?.label()}]"
                else -> "??"
            }

    fun key(): String =
            when (this) {
                is AtomicValue -> value
                is FeatureMap -> get("cat")?.label() ?: "??"
                else -> "??"
            }

}


sealed class FeatureStructure : Unifiable()
data class AtomicValue(val value: String) : FeatureStructure() {
    override fun toString(): String {
        return value
    }
}
data class SemanticValue(val value: Lambda) : FeatureStructure() {
    override val ground: Boolean = value.ground
    private val hash: Int = value.hashCode()
    override fun hashCode(): Int = hash

    override fun toString(): String {
        return "<${value}>"
    }
}

data class QueryVariable(val name: String) : FeatureStructure() {
    override val ground: Boolean get() = false

    override fun toString(): String {
        return name
    }
}

data class FeatureList(val elements: List<Unifiable>) : FeatureStructure() {
    override val ground: Boolean = elements.all { it.ground }
    private val hash: Int = elements.hashCode()
    override fun hashCode(): Int = hash

    override fun toString(): String = "[${elements.joinToString()}]"
}


data class FeatureListExpression(val elements: List<Unifiable>) : FeatureStructure() {
    override val ground: Boolean = false
    private val hash: Int = elements.hashCode()
    override fun hashCode(): Int = hash

    override fun toString(): String = "[${elements.joinToString()}]"

    fun simplify(): FeatureStructure {

        // maybe we can simplify by collapsing adjacent elements
        // result might then be an aggregate or remain an expression
        val acc = mutableListOf<Unifiable>()
        var copyIsExpression = false
        for (element in elements) {

            when (element) {
                is FeatureTuple -> acc.addAll(element.elements)
                is FeatureList -> acc.addAll(element.elements)
                is QueryVariable -> {
                    copyIsExpression = true; acc.add(element)
                }
                else -> acc.add(element)
            }
        }

        if (copyIsExpression) {
            return FeatureListExpression(acc)
        } else {
            return FeatureList(acc)
        }
    }

}



data class FeatureTuple(val elements: List<Unifiable>) : FeatureStructure() {
    override val ground: Boolean = elements.all { it.ground }
    private val hash: Int = elements.hashCode()
    override fun hashCode(): Int = hash

    override fun toString(): String = "[${elements.joinToString()}]"
}


data class FeatureTupleExpression(val elements: List<Unifiable>) : FeatureStructure() {
    override val ground: Boolean = false
    private val hash: Int = elements.hashCode()
    override fun hashCode(): Int = hash

    override fun toString(): String = "[${elements.joinToString()}]"
    fun simplify(): FeatureStructure {

        // maybe we can simplify by collapsing adjacent elements
        // result might then be an aggregate or remain an expression
        val acc = mutableListOf<Unifiable>()
        var copyIsExpression = false
        for (element in elements) {

            when (element) {
                is FeatureTuple -> acc.addAll(element.elements)
                is FeatureList -> acc.addAll(element.elements)
                is QueryVariable -> {
                    copyIsExpression = true; acc.add(element)
                }
                else -> acc.add(element)
            }
        }

        if (copyIsExpression) {
            return FeatureTupleExpression(acc)
        } else {
            return FeatureTuple(acc)
        }
    }
}


fun emptyFeatureList(): FeatureList = FeatureList(listOf())


data class FeatureMap(private val delegate: Map<String, Unifiable>) :
        FeatureStructure(), Map<String, Unifiable> by delegate {
    override val ground: Boolean = delegate.values.all { it.ground }
    private val hash: Int = delegate.hashCode()
    override fun hashCode(): Int = hash

    override fun toString(): String {
        return if ("cat" in this)
            "${this["cat"]}${(this - "cat").asIterable()}"
        else
            "${this}"
    }
}


data class Grammar(val rules: Set<Rule>, val lexicon: Map<String, Set<FeatureMap>>) : FeatureStructure() {
    // not ground, so that substituting into a grammar still reports an error
    override val ground: Boolean get() = false

    override fun toString(): String =
            "rules\n${rules.joinToString(separator = "\n")}\nlexicon\n${lexicon.asIterable()
                    .joinToString(separator = "\n")}"
}

fun emptyGrammar(): Grammar = Grammar(setOf(), mapOf())

interface Rule

data class CfgRule(val lhs: FeatureMap, val rhs: List<FeatureMap>, val words: List<Unifiable>) : Rule, FeatureStructure() {
    override val ground: Boolean = lhs.ground && rhs.all { it.ground }
    private val hash: Int = (lhs.hashCode() * 31 + rhs.hashCode()) * 31 + words.hashCode()
    override fun hashCode(): Int = hash

    override fun toString(): String {
        return "${lhs} -> ${rhs.joinToString(separator = " ")}"
    }
}


data class McfgRule(val lhs: FeatureMap,
                    val rhs: List<FeatureMap>,
                    val linseq: FeatureList) : FeatureStructure(), Rule {
    override val ground: Boolean = lhs.ground && rhs.all { it.ground }
    private val hash: Int = (lhs.hashCode() * 31 + rhs.hashCode()) * 31 + linseq.hashCode()
    override fun hashCode(): Int = hash

    override fun toString(): String {
        return "${lhs} => ${rhs.joinToString(separator = " ")}: <${linseq.elements.joinToString()}>"
    }
}

sealed class Lambda : Unifiable() {
    /*
     * De Bruijn facts, computed once when a term is built, that let shift,
     * placeBoxes, qshift and substBoxes return a subterm unchanged after an
     * O(1) check instead of copying it.
     */

    /**
     * The largest i - d over occurrences of Var(i) under d enclosing Lams
     * within this term, or 0 if there is none. The term has a free Var
     * above index n exactly when freeVarDepth > n.
     */
    open val freeVarDepth: Int get() = 0

    /** As [freeVarDepth], for QVar and the quantifiers Exists and Forall. */
    open val freeQVarDepth: Int get() = 0

    /** Whether Box occurs in this term. */
    open val hasBox: Boolean get() = false
}
data class Constant(val name: String) : Lambda() {
    override fun toString(): String {
        return name
    }
}

data class Var(val index: Int) : Lambda() {
    override val freeVarDepth: Int get() = maxOf(0, index)

    override fun toString(): String {
        return "v:${index}"
    }
}

data class QVar(val index: Int) : Lambda() {
    override val freeQVarDepth: Int get() = maxOf(0, index)

    override fun toString(): String {
        return "q:${index}"
    }
}

data class Lam(val body: Lambda) : Lambda() {
    override val ground: Boolean = body.ground
    private val hash: Int = body.hashCode() * 31 + 1
    override fun hashCode(): Int = hash
    override val freeVarDepth: Int = maxOf(0, body.freeVarDepth - 1)
    override val freeQVarDepth: Int = body.freeQVarDepth
    override val hasBox: Boolean = body.hasBox

    override fun toString(): String {
        return "\u03BB.($body)"
    }
}

data class Forall(val body: Lambda) : Lambda() {
    override val ground: Boolean = body.ground
    private val hash: Int = body.hashCode() * 31 + 2
    override fun hashCode(): Int = hash
    override val freeVarDepth: Int = body.freeVarDepth
    override val freeQVarDepth: Int = maxOf(0, body.freeQVarDepth - 1)
    override val hasBox: Boolean = body.hasBox

    override fun toString(): String = "\u2200.(${body})"
    // TODO hide DeBruijn notation

}

data class Exists(val body: Lambda) : Lambda() {
    override val ground: Boolean = body.ground
    private val hash: Int = body.hashCode() * 31 + 3
    override fun hashCode(): Int = hash
    override val freeVarDepth: Int = body.freeVarDepth
    override val freeQVarDepth: Int = maxOf(0, body.freeQVarDepth - 1)
    override val hasBox: Boolean = body.hasBox

    override fun toString(): String = "\u2203.(${body})"
    // TODO hide DeBruijn notation

}

data class App(val e1: Lambda, val e2: Lambda) : Lambda() {
    override val ground: Boolean = e1.ground && e2.ground
    private val hash: Int = (e1.hashCode() * 31 + e2.hashCode()) * 31 + 7
    override fun hashCode(): Int = hash
    override val freeVarDepth: Int = maxOf(e1.freeVarDepth, e2.freeVarDepth)
    override val freeQVarDepth: Int = maxOf(e1.freeQVarDepth, e2.freeQVarDepth)
    override val hasBox: Boolean = e1.hasBox || e2.hasBox

    override fun toString(): String {
        val acc = uncurry()
        return "${acc.get(0)}(${acc.subList(1,acc.size).joinToString(", ")})"
    }
    private fun uncurry() :List<Lambda>{
        val acc = mutableListOf<Lambda>()
       // currried verstion is a left-branching tree
        when (e1) {
            is App -> acc.addAll(e1.uncurry())
            else -> acc.add(e1)
        }
        acc.add(e2)
        return acc
    }
}

data class Not(val body: Lambda) : Lambda() {
    override val ground: Boolean = body.ground
    private val hash: Int = body.hashCode() * 31 + 4
    override fun hashCode(): Int = hash
    override val freeVarDepth: Int = body.freeVarDepth
    override val freeQVarDepth: Int = body.freeQVarDepth
    override val hasBox: Boolean = body.hasBox
}
data class And(val conjuncts: Set<Lambda>) : Lambda() {
    override val ground: Boolean = conjuncts.all { it.ground }
    private val hash: Int = conjuncts.sumOf { mixHash(it.hashCode()) } * 31 + 8
    override fun hashCode(): Int = hash
    override val freeVarDepth: Int = conjuncts.maxOfOrNull { it.freeVarDepth } ?: 0
    override val freeQVarDepth: Int = conjuncts.maxOfOrNull { it.freeQVarDepth } ?: 0
    override val hasBox: Boolean = conjuncts.any { it.hasBox }

    override fun toString(): String {
        return "(${conjuncts.joinToString(separator = " \u2227 ")})"
    }
}

data class Or(val disjuncts: Set<Lambda>) : Lambda() {
    override val ground: Boolean = disjuncts.all { it.ground }
    private val hash: Int = disjuncts.sumOf { mixHash(it.hashCode()) } * 31 + 9
    override fun hashCode(): Int = hash
    override val freeVarDepth: Int = disjuncts.maxOfOrNull { it.freeVarDepth } ?: 0
    override val freeQVarDepth: Int = disjuncts.maxOfOrNull { it.freeQVarDepth } ?: 0
    override val hasBox: Boolean = disjuncts.any { it.hasBox }

    override fun toString(): String {
        return "(${disjuncts.joinToString(separator = " \u2228 ")})"
    }
}

data class Implies(val e1: Lambda, val e2: Lambda) : Lambda() {
    override val ground: Boolean = e1.ground && e2.ground
    private val hash: Int = (e1.hashCode() * 31 + e2.hashCode()) * 31 + 5
    override fun hashCode(): Int = hash
    override val freeVarDepth: Int = maxOf(e1.freeVarDepth, e2.freeVarDepth)
    override val freeQVarDepth: Int = maxOf(e1.freeQVarDepth, e2.freeQVarDepth)
    override val hasBox: Boolean = e1.hasBox || e2.hasBox
}
data class Equiv(val e1: Lambda, val e2: Lambda) : Lambda() {
    override val ground: Boolean = e1.ground && e2.ground
    private val hash: Int = (e1.hashCode() * 31 + e2.hashCode()) * 31 + 6
    override fun hashCode(): Int = hash
    override val freeVarDepth: Int = maxOf(e1.freeVarDepth, e2.freeVarDepth)
    override val freeQVarDepth: Int = maxOf(e1.freeQVarDepth, e2.freeQVarDepth)
    override val hasBox: Boolean = e1.hasBox || e2.hasBox
}

data class FstructVar(val name: String) : Lambda() {
    override val ground: Boolean get() = false

    override fun toString(): String {
        return name
    }
}

object Box : Lambda() {
    override val hasBox: Boolean get() = true
}
object Empty : Lambda()

data class Integer(val value: Int) : FeatureStructure() {
    override fun toString(): String {
        return "${value}"
    }
}

fun atomicMap(atom: String): FeatureMap =
        FeatureMap(mapOf(Pair("cat", AtomicValue(atom))))

// MurmurHash3 finalizer: spreads the bits of a hash before it is summed.
internal fun mixHash(h0: Int): Int {
    var h = h0
    h = h xor (h ushr 16)
    h *= -0x7a143595
    h = h xor (h ushr 13)
    h *= -0x3d4d51cb
    return h xor (h ushr 16)
}
