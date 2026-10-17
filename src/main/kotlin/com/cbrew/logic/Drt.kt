package com.cbrew.logic

import com.cbrew.unify.*

/**
 * Discourse representation theory: translating DRSs to first-order logic.
 *
 * A box with k referents becomes k existential quantifiers over the
 * conjunction of its conditions; a box implication becomes k universal
 * quantifiers over an implication from the conjunction of the antecedent
 * conditions to the consequent. Referents are QVar binders numbered as for
 * k nested quantifiers, so the translation needs no renaming. A box with
 * no conditions is true. The translation goes through negation,
 * conjunction, disjunction, implication, equivalence and quantifiers, so
 * no box is left inside them.
 *
 * @return the first-order translation of the receiver
 * @throws IllegalArgumentException if a [Resolved] has no antecedents
 */
fun Lambda.fol(): Lambda = when (this) {
    is Drs -> (1..refs).fold(conjunction(conds)) { body, _ -> createExistential(body) }
    is DrsImp -> (1..refs).fold(createImplication(conjunction(antecedent), consequent.fol())) { body, _ ->
        createUniversal(body)
    }
    is Not -> createNegation(body.fol())
    is And -> createAnd(conjuncts.map { it.fol() })
    is Or -> createOr(disjuncts.map { it.fol() })
    is Implies -> createImplication(e1.fol(), e2.fol())
    is Equiv -> createEquiv(e1.fol(), e2.fol())
    is Exists -> createExistential(body.fol())
    is Forall -> createUniversal(body.fol())
    is Resolved -> {
        require(antecedents.isNotEmpty()) { "pronoun $pronoun has no candidate antecedents" }
        createOr(antecedents.map { createEquiv(pronoun, it) })
    }
    else -> this
}

/** The conjunction of the translated [conds], or [TRUE] when there are none. */
private fun conjunction(conds: List<Lambda>): Lambda =
        if (conds.isEmpty()) TRUE else createAnd(conds.map { it.fol() })

/**
 * Resolve the pronouns of a DRS by accessibility.
 *
 * A pronoun condition `PRO(u)` is replaced by `Resolved(u, candidates)`.
 * The candidates are all the referents accessible from the condition, in the
 * order the boxes list them, other than u itself: those of the enclosing
 * boxes, including referents introduced later in the same box. The
 * referents of a box implication or a negated box are visible inside it
 * and not outside it. Picking among the candidates is left to
 * [fol], which makes them a disjunction of equalities.
 *
 * @param drs a DRS, or a term containing DRSs
 * @return the term with every pronoun resolved, or null if some pronoun has
 *         no candidate
 */
fun resolveAnaphora(drs: Lambda): Lambda? = resolve(drs, 0, emptyList())

/** Convert between a QVar index at [depth] binders and its absolute id; the map is its own inverse. */
private fun absolute(index: Int, depth: Int): Int = depth - index + 1

/**
 * Rewrite [term], found under [depth] referent and quantifier binders, whose
 * accessible referents are the absolute ids [scope], outermost first.
 */
private fun resolve(term: Lambda, depth: Int, scope: List<Int>): Lambda? = when {
    term is App && term.e1 == Constant("PRO") && term.e2 is QVar -> {
        val own = absolute(term.e2.index, depth)
        val candidates = scope.filter { it != own }.map { QVar(absolute(it, depth)) }
        if (candidates.isEmpty()) null else Resolved(term.e2, candidates)
    }
    term is Drs -> {
        val inner = depth + term.refs
        resolveAll(term.conds, inner, scope + (depth + 1..inner))?.let { Drs(term.refs, it) }
    }
    term is DrsImp -> {
        val inner = depth + term.refs
        val innerScope = scope + (depth + 1..inner)
        val antecedent = resolveAll(term.antecedent, inner, innerScope)
        val consequent = resolve(term.consequent, inner, innerScope)
        if (antecedent == null || consequent == null) null else DrsImp(term.refs, antecedent, consequent)
    }
    term is Exists -> resolve(term.body, depth + 1, scope)?.let { Exists(it) }
    term is Forall -> resolve(term.body, depth + 1, scope)?.let { Forall(it) }
    term is Not -> resolve(term.body, depth, scope)?.let { Not(it) }
    term is And -> resolveAll(term.conjuncts.toList(), depth, scope)?.let { createAnd(it) }
    term is Or -> resolveAll(term.disjuncts.toList(), depth, scope)?.let { createOr(it) }
    term is Implies -> {
        val premise = resolve(term.e1, depth, scope)
        val conclusion = resolve(term.e2, depth, scope)
        if (premise == null || conclusion == null) null else Implies(premise, conclusion)
    }
    else -> term
}

/** Resolve each of [terms] as [resolve] does, or null if any of them fails. */
private fun resolveAll(terms: List<Lambda>, depth: Int, scope: List<Int>): List<Lambda>? {
    val resolved = terms.map { resolve(it, depth, scope) }
    return if (resolved.any { it == null }) null else resolved.filterNotNull()
}

/**
 * The unresolved merged DRSs of a complete discourse edge, its threads.
 *
 * For a category with a semantic value in sem (drt.fcfg), that value
 * alone. For a category with sents (drt-storage.fcfg), a tuple of one
 * (core, store) pair per sentence, each sentence's scopings are retrieved
 * separately with [retrieve], so a quantifier takes scope only within its
 * own sentence. Scopings that differ only in the order of their referents
 * and conditions (see [drsKey]) count once: names, pronouns and
 * indefinites are stored, and retrieving them in different orders changes
 * nothing else. Each combination of one scoping per sentence,
 * the first sentence varying slowest, gives one thread: the merge of its
 * scopings left to right, so that earlier referents are accessible later.
 *
 * @param category the category of a complete D edge
 * @return the merged DRSs, with the pronouns still unresolved
 * @throws IllegalArgumentException if the category has neither sem nor
 *         sents, its sem is not a semantic value, or its sents is not a
 *         tuple of (core, store) pairs
 */
fun drtThreads(category: FeatureMap): List<Lambda> {
    val sem = category["sem"]
    val sents = category["sents"]
    return when {
        sem != null -> {
            require(sem is SemanticValue) { "sem should be a semantic value, not $sem" }
            listOf(sem.value)
        }
        sents != null -> {
            require(sents is FeatureTuple) { "sents should be a tuple of (core, store) pairs, not $sents" }
            cartesian(sents.elements.map(::scopings)).map { createDrs(0, it) }
        }
        else -> throw IllegalArgumentException("a DRT discourse edge has sem or sents, not just ${category.keys}")
    }
}

/**
 * The scopings of one sentence's (core, store) [pair], one per [drsKey], in
 * order of first appearance. Each is represented by the last order of the
 * store that gives it, so that the scoping from the last order, which
 * retrieves the store last to first and so lists the referents in surface
 * order, represents the scopings that differ from it only in that order.
 */
private fun scopings(pair: Unifiable): List<Lambda> {
    val core = (pair as? FeatureTuple)?.elements?.takeIf { it.size == 2 }?.get(0)
    require(pair is FeatureTuple && core is SemanticValue) {
        "a sentence in sents should be a (core, store) pair, not $pair"
    }
    return retrieve(core.value, semanticValues(pair.elements[1])).groupBy(::drsKey).values.map { it.last() }
}

/**
 * A key that two terms share exactly when they differ at most in the order
 * of each box's referents and of its conditions, which DRT takes to be
 * sets. For example ([x, y], [dog(x), Angus(y), own(y, x)]) and
 * ([x, y], [Angus(x), dog(y), own(x, y)]) share a key, and
 * ([x, y], [dog(x), dog(y), chase(x, y)]) and its converse share one too,
 * since renaming x and y turns one into the other. The conjuncts of And,
 * the disjuncts of Or and the candidates of a [Resolved] are sets as well.
 *
 * A box's key is the least, over every order of its referents, of its
 * sorted condition keys, so a box with k referents costs k! sortings.
 * The boxes of one sentence have few referents.
 */
fun drsKey(term: Lambda): String = key(term, 0, emptyMap())

/**
 * The key of [term], found under [depth] referent and quantifier binders,
 * whose box referents, as absolute ids, are renamed by [rename].
 */
private fun key(term: Lambda, depth: Int, rename: Map<Int, Int>): String = when (term) {
    is QVar -> absolute(term.index, depth).let { "q${rename[it] ?: it}" }
    is Drs -> "drs(${boxKey(term.refs, term.conds, null, depth, rename)})"
    is DrsImp -> "imp(${boxKey(term.refs, term.antecedent, term.consequent, depth, rename)})"
    is Exists -> "exists(${key(term.body, depth + 1, rename)})"
    is Forall -> "forall(${key(term.body, depth + 1, rename)})"
    is Lam -> "lam(${key(term.body, depth, rename)})"
    is Not -> "not(${key(term.body, depth, rename)})"
    is App -> "app(${key(term.e1, depth, rename)},${key(term.e2, depth, rename)})"
    is Implies -> "implies(${key(term.e1, depth, rename)},${key(term.e2, depth, rename)})"
    is Equiv -> "equiv(${key(term.e1, depth, rename)},${key(term.e2, depth, rename)})"
    is And -> "and(${setKey(term.conjuncts, depth, rename)})"
    is Or -> "or(${setKey(term.disjuncts, depth, rename)})"
    is Resolved -> "resolved(${key(term.pronoun, depth, rename)},${setKey(term.antecedents, depth, rename)})"
    is Constant -> "c:${term.name}"
    is Var -> "v${term.index}"
    is FstructVar -> "f:${term.name}"
    is Box -> "box"
    is Empty -> "empty"
}

/** The sorted keys of [terms], joined. */
private fun setKey(terms: Collection<Lambda>, depth: Int, rename: Map<Int, Int>): String =
        terms.map { key(it, depth, rename) }.sorted().joinToString(",")

/**
 * The least key, over every order of a box's [refs] referents, of its
 * [conds] (sorted) and its [consequent], if it is an implication.
 */
private fun boxKey(refs: Int, conds: List<Lambda>, consequent: Lambda?, depth: Int, rename: Map<Int, Int>): String =
        permutations((depth + 1..depth + refs).toList()).minOf { order ->
            val inner = rename + order.mapIndexed { i, id -> depth + 1 + i to id }
            val body = setKey(conds, depth + refs, inner)
            "$refs|$body" + (consequent?.let { "|" + key(it, depth + refs, inner) } ?: "")
        }

/** Every choice of one element from each of [lists], the first list varying slowest. */
private fun <T> cartesian(lists: List<List<T>>): List<List<T>> =
        lists.fold(listOf(listOf())) { acc, xs -> acc.flatMap { prefix -> xs.map { prefix + it } } }

/**
 * The readings of a complete discourse edge: its [drtThreads] with every
 * pronoun resolved by accessibility. A thread in which some pronoun has no
 * accessible referent gives no reading.
 *
 * @param category the category of a complete D edge
 * @return the resolved DRSs, in the order of the threads
 */
fun drtReadings(category: FeatureMap): List<Lambda> = drtThreads(category).mapNotNull(::resolveAnaphora)
