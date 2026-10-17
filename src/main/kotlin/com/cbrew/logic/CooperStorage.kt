package com.cbrew.logic

import com.cbrew.unify.*

/**
 * Cooper storage retrieval, after NLTK's CooperStore.s_retrieve.
 *
 * A grammar in the storage style (see src/test/resources/storage.fcfg)
 * gives each phrase two features: core, its meaning with every quantified
 * noun phrase replaced by a placeholder variable @x, and store, a tuple of
 * binding operators bo(Q, @x), each pairing a quantifier with its
 * placeholder. Retrieval puts the quantifiers back. For each order of the
 * store, starting from the core, each operator in turn makes the current
 * term into a property by abstracting over its placeholder and applies its
 * quantifier to that; the operator taken first ends up with the narrowest
 * scope. Each order gives one scoping.
 *
 * Retrieval is a function the caller applies to a complete edge; the chart
 * knows nothing of it.
 */

/** The constant that marks a binding operator, bo(Q, @x). */
val BO = Constant("bo")

/** A binding operator: [quantifier] is to bind [variable] in the core. */
data class BindingOperator(val quantifier: Lambda, val variable: FstructVar)

/**
 * Read the binding operator bo(Q, @x), which is App(App(bo, Q), @x).
 *
 * @throws IllegalArgumentException if [term] has any other form
 */
fun bindingOperator(term: Lambda): BindingOperator {
    val f = (term as? App)?.e1 as? App
    val v = (term as? App)?.e2 as? FstructVar
    require(f != null && f.e1 == BO && v != null) {
        "a store holds binding operators bo(Q, @x), not ${term.pretty()}"
    }
    return BindingOperator(f.e2, v)
}

/**
 * The readings of [core] with the binding operators in [store] retrieved
 * in every order, one reading per permutation of the store, in the order
 * NLTK's s_retrieve gives them: permutations in lexicographic order of
 * store positions, the first operator of each retrieved first, so given
 * the narrowest scope. Readings may repeat when different orders give the
 * same term; see [retrieveDistinct].
 *
 * @throws IllegalArgumentException if an element of [store] is not bo(Q, @x)
 */
fun retrieve(core: Lambda, store: List<Lambda>): List<Lambda> {
    val operators = store.map(::bindingOperator)
    return permutations(operators).map { order ->
        order.fold(core) { term, bo -> createApp(bo.quantifier, term.abstractOver(bo.variable)) }
    }
}

/** The distinct readings of [retrieve], in order of first appearance. */
fun retrieveDistinct(core: Lambda, store: List<Lambda>): List<Lambda> =
        retrieve(core, store).distinct()

/**
 * The readings of a complete edge's category, from its core feature (a
 * semantic value) and its store feature (a tuple or list of semantic
 * values); see [retrieve].
 *
 * @throws IllegalArgumentException if either feature is missing or not of
 *         that form, for example a store that still contains a variable
 */
fun retrieve(category: FeatureMap): List<Lambda> {
    val core = category["core"]
    require(core is SemanticValue) { "core should be a semantic value, not $core" }
    return retrieve(core.value, semanticValues(category["store"]))
}

/**
 * The terms in a store feature, a tuple or list of semantic values.
 *
 * @throws IllegalArgumentException if [store] is missing or not of that form
 */
internal fun semanticValues(store: Unifiable?): List<Lambda> {
    val elements = when (store) {
        is FeatureTuple -> store.elements
        is FeatureList -> store.elements
        else -> throw IllegalArgumentException("store should be a tuple of semantic values, not $store")
    }
    return elements.map {
        require(it is SemanticValue) { "store should hold semantic values, not $it" }
        it.value
    }
}

/** Every ordering of [xs], in lexicographic order of positions. */
internal fun <T> permutations(xs: List<T>): List<List<T>> =
        if (xs.isEmpty()) listOf(listOf())
        else xs.indices.flatMap { i ->
            permutations(xs.subList(0, i) + xs.subList(i + 1, xs.size)).map { listOf(xs[i]) + it }
        }
