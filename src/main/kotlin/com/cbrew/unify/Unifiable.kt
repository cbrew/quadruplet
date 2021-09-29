package com.cbrew.unify

sealed class Unifiable {


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
    override fun toString(): String {
        return "<${value}>"
    }
}

data class QueryVariable(val name: String) : FeatureStructure() {
    override fun toString(): String {
        return name
    }
}

data class FeatureList(val elements: List<Unifiable>) : FeatureStructure() {
    override fun toString(): String = "[${elements.joinToString()}]"
}


data class FeatureListExpression(val elements: List<Unifiable>) : FeatureStructure() {
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
    override fun toString(): String = "[${elements.joinToString()}]"
}


data class FeatureTupleExpression(val elements: List<Unifiable>) : FeatureStructure() {
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
    override fun toString(): String {
        return if ("cat" in this)
            "${this["cat"]}${(this - "cat").asIterable()}"
        else
            "${this}"
    }
}


data class Grammar(val rules: Set<Rule>, val lexicon: Map<String, Set<FeatureMap>>) : FeatureStructure() {
    override fun toString(): String =
            "rules\n${rules.joinToString(separator = "\n")}\nlexicon\n${lexicon.asIterable()
                    .joinToString(separator = "\n")}"
}

fun emptyGrammar(): Grammar = Grammar(setOf(), mapOf())

interface Rule

data class CfgRule(val lhs: FeatureMap, val rhs: List<FeatureMap>, val words: List<Unifiable>) : Rule, FeatureStructure() {
    override fun toString(): String {
        return "${lhs} -> ${rhs.joinToString(separator = " ")}"
    }
}


data class McfgRule(val lhs: FeatureMap,
                    val rhs: List<FeatureMap>,
                    val linseq: FeatureList) : FeatureStructure(), Rule {
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
    override fun toString(): String {
        return "\u03BB.($body)"
    }
}

data class Forall(val body: Lambda) : Lambda() {
    override fun toString(): String = "\u2200.(${body})"
    // TODO hide DeBruijn notation

}

data class Exists(val body: Lambda) : Lambda() {
    override fun toString(): String = "\u2203.(${body})"
    // TODO hide DeBruijn notation

}

data class App(val e1: Lambda, val e2: Lambda) : Lambda() {
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

data class Not(val body: Lambda) : Lambda()
data class And(val conjuncts: Set<Lambda>) : Lambda() {
    override fun toString(): String {
        return "(${conjuncts.joinToString(separator = " \u2227 ")})"
    }
}

data class Or(val disjuncts: Set<Lambda>) : Lambda() {
    override fun toString(): String {
        return "(${disjuncts.joinToString(separator = " \u2228 ")})"
    }
}

data class Implies(val e1: Lambda, val e2: Lambda) : Lambda()
data class Equiv(val e1: Lambda, val e2: Lambda) : Lambda()

data class FstructVar(val name: String) : Lambda() {
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