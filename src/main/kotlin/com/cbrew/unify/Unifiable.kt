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

sealed class Lambda : Unifiable()
data class Constant(val name: String) : Lambda() {
    override fun toString(): String {
        return name
    }
}

data class Var(val index: Int) : Lambda() {
    override fun toString(): String {
        return "v:${index}"
    }
}

data class QVar(val index: Int) : Lambda() {
    override fun toString(): String {
        return "q:${index}"
    }
}

data class Lam(val body: Lambda) : Lambda() {
    override val ground: Boolean = body.ground
    private val hash: Int = body.hashCode() * 31 + 1
    override fun hashCode(): Int = hash

    override fun toString(): String {
        return "\u03BB.($body)"
    }
}

data class Forall(val body: Lambda) : Lambda() {
    override val ground: Boolean = body.ground
    private val hash: Int = body.hashCode() * 31 + 2
    override fun hashCode(): Int = hash

    override fun toString(): String = "\u2200.(${body})"
    // TODO hide DeBruijn notation

}

data class Exists(val body: Lambda) : Lambda() {
    override val ground: Boolean = body.ground
    private val hash: Int = body.hashCode() * 31 + 3
    override fun hashCode(): Int = hash

    override fun toString(): String = "\u2203.(${body})"
    // TODO hide DeBruijn notation

}

data class App(val e1: Lambda, val e2: Lambda) : Lambda() {
    override val ground: Boolean = e1.ground && e2.ground
    private val hash: Int = (e1.hashCode() * 31 + e2.hashCode()) * 31 + 7
    override fun hashCode(): Int = hash

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
}
data class And(val conjuncts: Set<Lambda>) : Lambda() {
    override val ground: Boolean = conjuncts.all { it.ground }
    private val hash: Int = conjuncts.sumOf { mixHash(it.hashCode()) } * 31 + 8
    override fun hashCode(): Int = hash

    override fun toString(): String {
        return "(${conjuncts.joinToString(separator = " \u2227 ")})"
    }
}

data class Or(val disjuncts: Set<Lambda>) : Lambda() {
    override val ground: Boolean = disjuncts.all { it.ground }
    private val hash: Int = disjuncts.sumOf { mixHash(it.hashCode()) } * 31 + 9
    override fun hashCode(): Int = hash

    override fun toString(): String {
        return "(${disjuncts.joinToString(separator = " \u2228 ")})"
    }
}

data class Implies(val e1: Lambda, val e2: Lambda) : Lambda() {
    override val ground: Boolean = e1.ground && e2.ground
    private val hash: Int = (e1.hashCode() * 31 + e2.hashCode()) * 31 + 5
    override fun hashCode(): Int = hash
}
data class Equiv(val e1: Lambda, val e2: Lambda) : Lambda() {
    override val ground: Boolean = e1.ground && e2.ground
    private val hash: Int = (e1.hashCode() * 31 + e2.hashCode()) * 31 + 6
    override fun hashCode(): Int = hash
}

data class FstructVar(val name: String) : Lambda() {
    override val ground: Boolean get() = false

    override fun toString(): String {
        return name
    }
}

object Box : Lambda()
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
