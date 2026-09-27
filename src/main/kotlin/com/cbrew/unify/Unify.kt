package com.cbrew.unify

/**
 * In the following functions we spell out exactly how unification works.
 * For feature structures, this is term unification with named variables:
 * reentrancy is expressed by using the same ?x variable in several places,
 * and bindings are an immutable association list (Bindings) from variable
 * names to values that is threaded through the computation; each new binding
 * shares the list it extends. Failure is signalled by null, so no binding
 * ever needs to be undone. A bound variable's value is unified with
 * whatever it meets, so bindings can be refined, and an occurs check keeps
 * bindings acyclic.
 * For semantic terms, things are more restricted: we can bind a semantic
 * term against a ?x variable, and check consistency with an existing binding,
 * but we don't recurse into semantic terms (two semantic terms unify only if
 * they are equal).
 *
 * Nested feature maps (a feature whose value is itself a feature map) are not
 * supported and there are no current plans to support them; see README.md.
 *
 * The code makes heavy use of pattern matching over types using when() and is().
 * ?x variables are intercepted early and fed to checkBinding, other instances
 * of Unifiable are sent to functions specialized for subtypes. For greater
 * transparency, these are given distinct names even though Kotlin's pattern
 * matching ensures that they could all be called unify.
 */


private typealias UM = Map<CharSequence, Unifiable>

typealias UR = Pair<Unifiable, UM>

// what the unifier returns internally: a UR whose bindings are a Bindings list
private typealias BR = Pair<Unifiable, Bindings>

fun Unifiable.unify(other: Unifiable): Unifiable? =
        unify(this, other)?.subst()?.canonicalize()


// take a feature structure and rename the ? variables
// ?x0, ?x1 ...
fun Unifiable.canonicalize(): Unifiable {
    val variables: MutableSet<Unifiable> = mutableSetOf()



    findVariables(this, variables)

    // make a map from old variables to canonically named new ones

    val newVariables = variables.mapIndexed { i, v ->
        when (v) {
            is QueryVariable -> QueryVariable("?x$i")
            is FstructVar -> FstructVar("?x$i")
            else -> throw Exception("should not be possible")
        }
    }
    val names = variables.map(::name)

    return renameVariables(names.zip(newVariables).toMap())
}

/**
 * Rename variables simultaneously: each variable is looked up once in
 * [renaming], so a renaming that swaps two names does not chain.
 */
fun Unifiable.renameVariables(renaming: Map<CharSequence, Unifiable>): Unifiable =
        mapVariables(this) { v -> renaming[name(v)] ?: v }

/**
 * Rename the variables of this term that also occur in [others], so that it
 * can be unified with them without identifying unrelated variables that
 * happen to share a name ("standardizing apart"). Variables that do not
 * clash keep their names.
 */
fun Unifiable.renamedApartFrom(vararg others: Unifiable): Unifiable = renamedApartFrom(others.asList())

fun Unifiable.renamedApartFrom(others: Iterable<Unifiable>): Unifiable {
    if (ground || others.all { it.ground }) return this
    val taken = mutableSetOf<Unifiable>()
    others.forEach { findVariables(it, taken) }
    if (taken.isEmpty()) return this
    val takenNames = taken.map { name(it).toString() }.toSet()
    val mine = mutableSetOf<Unifiable>()
    findVariables(this, mine)
    val myNames = mine.map { name(it).toString() }.distinct()
    val used = (takenNames + myNames).toMutableSet()
    val renaming = mutableMapOf<String, String>()
    for (old in myNames.filter { it in takenNames }) {
        var i = 1
        while ("$old$i" in used) i++
        renaming[old] = "$old$i"
        used += "$old$i"
    }
    // keyed by name, but each variable keeps its own kind
    return if (renaming.isEmpty()) this else mapVariables(this) { v ->
        when (val fresh = renaming[name(v).toString()]) {
            null -> v
            else -> if (v is FstructVar) FstructVar(fresh) else QueryVariable(fresh)
        }
    }
}

private fun name(v: Unifiable): CharSequence =
        when (v) {
            is FstructVar -> v.name
            is QueryVariable -> v.name
            else -> throw Exception("should not be possible")
        }


fun findVariables(item: Unifiable, variables: MutableSet<Unifiable>) {
    if (item.ground)
        return
    else if (isVariable(item))
        variables.add(item)
    else
        subterms(item).forEach { findVariables(it, variables) }
}

private fun isVariable(item: Unifiable): Boolean =
        item is QueryVariable || item is FstructVar

// the immediate subterms that can contain variables
private fun subterms(item: Unifiable): List<Unifiable> =
        when (item) {
            is SemanticValue -> listOf(item.value)  // reach into semantic value
            is FeatureList -> item.elements
            is FeatureTuple -> item.elements
            is FeatureListExpression -> item.elements
            is FeatureTupleExpression -> item.elements
            is FeatureMap -> item.values.toList()
            is QueryVariable -> listOf()
            is AtomicValue -> listOf()
            is Integer -> listOf()
            is Grammar -> listOf()
            is CfgRule -> listOf()
            is McfgRule -> listOf()
            is Implies -> listOf(item.e1, item.e2)
            is Equiv -> listOf(item.e1, item.e2)
            is App -> listOf(item.e1, item.e2)
            is And -> item.conjuncts.toList()
            is Or -> item.disjuncts.toList()
            is Not -> listOf(item.body)
            is Lam -> listOf(item.body)
            is Exists -> listOf(item.body)
            is Forall -> listOf(item.body)
            is Constant -> listOf()
            is Var -> listOf()
            is QVar -> listOf()
            is Box -> listOf()
            is Empty -> listOf()
            is FstructVar -> listOf()
        }


fun unify(uf1: Unifiable, uf2: Unifiable): UR? =
        unify(uf1, uf2, Bindings.EMPTY)

private fun unify(uf1: Unifiable, uf2: Unifiable, bindings: Bindings): BR? =
        when (uf1) {
            is FstructVar -> bindings.bindVariable(uf1, uf2)
            is QueryVariable -> bindings.bindVariable(uf1, uf2)
            is Lambda -> unifyLU(uf1, uf2, bindings)
            is FeatureStructure -> unifyFU(uf1, uf2, bindings)
        }


private fun unifyLU(uf1: Lambda, uf2: Unifiable, bindings: Bindings): BR? =
        when (uf2) {
            is FstructVar -> bindings.bindVariable(uf2, uf1)
            is QueryVariable -> bindings.bindVariable(uf2, uf1)
            is Lambda -> unifyLL(uf1, uf2, bindings)
            is FeatureStructure -> unifyLF(uf1, uf2, bindings)
        }

private fun unifyFU(uf1: FeatureStructure, uf2: Unifiable, bindings: Bindings): BR? =
        when (uf2) {
            is FstructVar -> bindings.bindVariable(uf2, uf1)
            is QueryVariable -> bindings.bindVariable(uf2, uf1)
            is Lambda -> unifyFL(uf1, uf2, bindings)
            is FeatureStructure -> unifyFF(uf1, uf2, bindings)
        }

private fun unifyLL(uf1: Lambda, uf2: Lambda, bindings: Bindings): BR? =
        if (uf1 == uf2) BR(uf1, bindings) else null
@Suppress("UNUSED_PARAMETER")
private fun unifyLF(uf1: Lambda, uf2: FeatureStructure, bindings: Bindings): BR? = null
@Suppress("UNUSED_PARAMETER")
private fun unifyFL(uf1: FeatureStructure, uf2: Lambda, bindings: Bindings): BR? = null
private fun unifyFF(fs1: FeatureStructure, fs2: FeatureStructure, bindings: Bindings): BR? =
        if (fs1 is QueryVariable || fs2 is QueryVariable)
            throw Exception("variables should have been caught earlier")
        else if (fs1 == fs2)
            BR(fs1, bindings)
        else if (fs1 is FeatureMap && fs2 is FeatureMap)
            unifyMaps(fs1, fs2, bindings)
        else if (fs1 is FeatureList && fs2 is FeatureList && fs1.elements.size == fs2.elements.size) {


            var newBindings = bindings
            var newElements = mutableListOf<FeatureStructure>()


            fs1.elements.zip(fs2.elements).forEach { (x1, x2) ->
                val x12 = unify(x1, x2, newBindings)
                if (x12 == null)
                    return null
                else {
                    newElements.add(x12.first as FeatureStructure)
                    newBindings = x12.second
                }

            }

            BR(FeatureList(newElements), newBindings)

        } else if (fs1 is SemanticValue && fs2 is SemanticValue)
            unify(fs1.value, fs2.value, bindings)?.let { (v, bs) -> BR(SemanticValue(v as Lambda), bs) }
        else
            null


// The result is built in a local map that never escapes except inside the
// (immutable) FeatureMap. Features only in fs1 come first, then those only in
// fs2, then shared ones, as in earlier versions.
private fun unifyMaps(fs1: FeatureMap, fs2: FeatureMap, bindings: Bindings): BR? {
    val result = LinkedHashMap<String, Unifiable>(fs1.size + fs2.size)
    for ((k, v) in fs1) if (k !in fs2) result[k] = v
    for ((k, v) in fs2) if (k !in fs1) result[k] = v
    var newBindings = bindings
    for ((k, v1) in fs1) {
        val v2 = fs2[k] ?: continue
        val (fs, bs) = unify(v1, v2, newBindings) ?: return null
        newBindings = bs
        result[k] = fs
    }
    return BR(FeatureMap(result), newBindings)
}

fun UM.checkBinding(uf1: FstructVar, uf2: Unifiable): UR? = Bindings.of(this).bindVariable(uf1, uf2)

fun UM.checkBinding(uf1: QueryVariable, uf2: Unifiable): UR? = Bindings.of(this).bindVariable(uf1, uf2)

/**
 * Unify the variable [v] with [other]. Both are dereferenced first, so chains
 * of variable-to-variable bindings are followed to their end. If v is already
 * bound, its value is unified with other, which may refine the binding. If v
 * is unbound it is bound to other, unless other contains v (occurs check).
 * QueryVariables range over feature structures and FstructVars over semantic
 * terms; a binding that crosses the two fails.
 */
private fun Bindings.bindVariable(v: Unifiable, other: Unifiable): BR? {
    val value = deref(v)
    val target = deref(other)
    return if (!inDomain(v, value) || !inDomain(v, target))
        null
    else if (!isVariable(value))
        unify(value, other, this)
    else if (value == target)
        BR(value, this)
    else if (occurs(value, target))
        null
    else
        BR(target, bind(name(value), target))
}

private fun inDomain(v: Unifiable, value: Unifiable): Boolean =
        when (v) {
            is QueryVariable -> value is FeatureStructure
            is FstructVar -> value is Lambda
            else -> throw Exception("should not be possible")
        }

// does the (unbound) variable v occur in term under these bindings?
private fun UM.occurs(v: Unifiable, term: Unifiable): Boolean =
        if (term.ground)
            false
        else if (isVariable(term)) {
            val t = deref(term)
            if (isVariable(t)) name(t) == name(v) else occurs(v, t)
        } else
            subterms(term).any { occurs(v, it) }


// dereference a variable using a set of bindings
// Yields either an unbound variable or a substantive
// term, never a bound variable.
tailrec fun UM.deref(v: Unifiable): Unifiable =
        when (v) {
            is FstructVar -> {
                val bound = this[v.name]
                if (bound == null) v else deref(bound)
            }
            is QueryVariable -> {
                val bound = this[v.name]
                if (bound == null) v else deref(bound)
            }
            else -> v
        }


fun UR.subst(): Unifiable =
        second.subst(first)

/*
 * Substitution applies the bindings exhaustively: a bound variable is replaced
 * by its value, and that value is itself substituted, so a chain such as
 * ?x -> [p, ?y], ?y -> sg resolves completely. This terminates because the
 * occurs check in bindVariable keeps bindings acyclic.
 */

fun UM.subst(uf: Unifiable): Unifiable = mapVariables(uf) { resolve(it) }

fun UM.subst(ufs: List<Unifiable>): List<Unifiable> = ufs.map { subst(it) }

fun UM.subst(fs: FeatureStructure): FeatureStructure = mapVariablesFs(fs, { resolve(it) }, share = true)

fun UM.subst(lam: Lambda): Lambda = mapVariablesLam(lam, { resolve(it) }, share = true)

// the fully substituted value of a variable; an unbound variable is itself
private fun UM.resolve(v: Unifiable): Unifiable {
    val r = deref(v)
    return if (isVariable(r)) r else subst(r)
}

/**
 * Rebuild a term, replacing every variable v by onVar(v). Used both for
 * applying bindings (subst) and for renaming variables.
 *
 * Structure is shared: a ground subterm, or one in which no variable was
 * replaced, is returned as the same object rather than copied. Nodes that do
 * change are rebuilt with the factory functions, so lambda terms stay
 * simplified. This relies on lambda terms already being in the simplified
 * form the factories produce (see normalized()); FeatureGrammar ensures that
 * for its rules.
 */
private fun mapVariables(uf: Unifiable, onVar: (Unifiable) -> Unifiable): Unifiable =
        when (uf) {
            is FeatureStructure -> mapVariablesFs(uf, onVar, share = true)
            is Lambda -> mapVariablesLam(uf, onVar, share = true)
        }

/**
 * Rebuild every node of a term with the factory functions, simplifying any
 * lambda terms that were built directly with constructors (as the notation
 * parsers do). Idempotent.
 */
fun Unifiable.normalized(): Unifiable =
        when (this) {
            is FeatureStructure -> mapVariablesFs(this, { it }, share = false)
            is Lambda -> mapVariablesLam(this, { it }, share = false)
        }

// map f over a list, returning the original list if no element changed
private inline fun <T> List<T>.mapShared(f: (T) -> T): List<T> {
    var out: MutableList<T>? = null
    for ((i, x) in withIndex()) {
        val y = f(x)
        if (out == null && y !== x) {
            out = ArrayList(size)
            out.addAll(subList(0, i))
        }
        out?.add(y)
    }
    return out ?: this
}

private fun mapVariablesFs(fs: FeatureStructure, onVar: (Unifiable) -> Unifiable, share: Boolean): FeatureStructure {
    if (share && fs.ground) return fs
    fun map(u: Unifiable) = when (u) {
        is FeatureStructure -> mapVariablesFs(u, onVar, share)
        is Lambda -> mapVariablesLam(u, onVar, share)
    }
    fun mapMap(m: FeatureMap) = mapVariablesFs(m, onVar, share) as FeatureMap
    fun <T> same(old: List<T>, new: List<T>) = share && old === new
    return when (fs) {
        is AtomicValue -> fs
        is Integer -> fs
        is SemanticValue -> {
            val v = mapVariablesLam(fs.value, onVar, share)
            if (share && v === fs.value) fs else SemanticValue(v)
        }
        is QueryVariable -> {
            val r = onVar(fs)
            when (r) {
                is FeatureStructure -> r
                is Lambda -> throw Exception("Trying to substitute sem term into syn variable")
            }
        }
        is FeatureList -> fs.elements.mapShared(::map).let { if (same(fs.elements, it)) fs else FeatureList(it) }
        is FeatureTuple -> fs.elements.mapShared(::map).let { if (same(fs.elements, it)) fs else FeatureTuple(it) }
        // expressions are always rebuilt, so that they get simplified
        is FeatureListExpression -> FeatureListExpression(fs.elements.mapShared(::map)).simplify()
        is FeatureTupleExpression -> FeatureTupleExpression(fs.elements.mapShared(::map)).simplify()
        is CfgRule -> {
            val lhs = mapMap(fs.lhs)
            val rhs = fs.rhs.mapShared(::mapMap)
            if (share && lhs === fs.lhs && same(fs.rhs, rhs)) fs else CfgRule(lhs, rhs, fs.words)
        }
        is McfgRule -> {
            val lhs = mapMap(fs.lhs)
            val rhs = fs.rhs.mapShared(::mapMap)
            if (share && lhs === fs.lhs && same(fs.rhs, rhs)) fs else McfgRule(lhs, rhs, fs.linseq)
        }
        is FeatureMap -> {
            var changed = !share
            val m = fs.mapValues { (_, v) -> map(v).also { if (it !== v) changed = true } }
            if (changed) FeatureMap(m) else fs
        }
        is Grammar -> throw Exception("does not make sense to call subst on Grammar")
    }
}

/**
 * substitution for lambda terms, with automatic
 * simplifications.
 */
private fun mapVariablesLam(lam: Lambda, onVar: (Unifiable) -> Unifiable, share: Boolean): Lambda {
    if (share && lam.ground) return lam
    fun map(l: Lambda) = mapVariablesLam(l, onVar, share)
    fun unchanged(vararg pairs: Pair<Lambda, Lambda>) = share && pairs.all { (a, b) -> a === b }
    return when (lam) {
        is Constant -> lam
        is QVar -> lam
        is FstructVar -> {
            val r = onVar(lam)
            when (r) {
                is Lambda -> r
                is FeatureStructure -> throw Exception("binding of sem term $lam to syn term $r")
            }
        }
        is Empty -> lam
        is Box -> lam
        is Var -> lam
        is Lam -> map(lam.body).let { if (unchanged(lam.body to it)) lam else createLam(it) }
        is Exists -> map(lam.body).let { if (unchanged(lam.body to it)) lam else createExistential(it) }
        is Not -> map(lam.body).let { if (unchanged(lam.body to it)) lam else createNegation(it) }
        is Forall -> map(lam.body).let { if (unchanged(lam.body to it)) lam else createUniversal(it) }
        is Implies -> {
            val a = map(lam.e1); val b = map(lam.e2)
            if (unchanged(lam.e1 to a, lam.e2 to b)) lam else createImplication(a, b)
        }
        is Equiv -> {
            val a = map(lam.e1); val b = map(lam.e2)
            if (unchanged(lam.e1 to a, lam.e2 to b)) lam else createEquiv(a, b)
        }
        is App -> {
            val a = map(lam.e1); val b = map(lam.e2)
            if (unchanged(lam.e1 to a, lam.e2 to b)) lam else createApp(a, b)
        }
        is Or -> {
            val ds = lam.disjuncts.toList()
            val mapped = ds.mapShared(::map)
            if (share && mapped === ds) lam else createOr(mapped)
        }
        is And -> {
            val cs = lam.conjuncts.toList()
            val mapped = cs.mapShared(::map)
            if (share && mapped === cs) lam else createAnd(mapped)
        }
    }
}


/**
 * Create an implication, possibly applying these rewrites
 * (∃xP(x) → ∀xQ(x))	=>	∀x(P(x) → Q(x))
 * (∀xP(x) → ∃xQ(x))	=>	∃x(P(x) → Q(x))
 *
 * @param premise
 * @param conclusion
 * @return
 */
fun createImplication(premise: Lambda, conclusion: Lambda): Lambda {
    if (premise is Forall && conclusion is Exists) {
        return createExistential(createImplication(premise.body, conclusion.body))
    } else if (premise is Exists && conclusion is Forall) {
        return createUniversal(createImplication(premise.body, conclusion.body))
    } else {
        return Implies(premise, conclusion)
    }

}

fun createEquiv(premise: Lambda, conclusion: Lambda): Lambda = Equiv(premise, conclusion)


/**
 * Make a universally quantified expression
 * around body. Simplify the quantifier away
 * if it fails to bind anything. Propagate the
 * quantifier into conjunctions when appropriate.
 *
 * @param body
 * @return either a non-vacuous universal quantifier or the original body
 */
fun createUniversal(body: Lambda): Lambda {
    return createQuantified(true, body)
}

/**
 * Make an existentially quantified expression
 * around body. Simplify the quantifier away
 * if it fails to bind anything. Propagate the
 * universal quantifier into conjunctions and
 * the existential into disjunctions.
 *
 * This is tricky code, because of potential free quantified variables.
 *
 *
 * @param body
 * @return either a non-vacuous existential quantifier or the original body
 */
fun createExistential(body: Lambda): Lambda {
    return createQuantified(false, body)
}

private fun createQuantified(isUniversal: Boolean, body: Lambda): Lambda =
// possibly the quantifier would not bind anything.
// if so we omit it
        if (!quantifierBinds(body, 1)) shiftQuantifiers(body, 1)
        else if (body is And && isUniversal) And(body.conjuncts.map(::createUniversal).toSet())
        else if (body is Or && !isUniversal) Or(body.disjuncts.map(::createExistential).toSet())
        else if (isUniversal) Forall(body)
        else Exists(body)


/**
 * Check whether a quantifier wrapped around body would bind
 * anything.
 *
 * @param input the term that might be wrapped
 * @param bvi   the bound variable index
 * @return whether the quantifier binds
 */
private fun quantifierBinds(input: Lambda, bvi: Int): Boolean =
        when (input) {
            is Box -> false
            is Empty -> false
            is Constant -> false
            is FstructVar -> false
            is Var -> false
            is QVar -> input.index == bvi
            is Exists -> quantifierBinds(input.body, bvi + 1)
            is Forall -> quantifierBinds(input.body, bvi + 1)
            is Not -> quantifierBinds(input.body, bvi)
            is Lam -> quantifierBinds(input.body, bvi)
            is App -> quantifierBinds(input.e1, bvi) || quantifierBinds(input.e2, bvi)
            is Equiv -> quantifierBinds(input.e1, bvi) || quantifierBinds(input.e2, bvi)
            is Implies -> quantifierBinds(input.e1, bvi) || quantifierBinds(input.e2, bvi)
            is Or -> input.disjuncts.any { quantifierBinds(it, bvi) }
            is And -> input.conjuncts.any { quantifierBinds(it, bvi) }
        }


/**
 * Reduce the index of all quantifiers that are free in input
 * by one. Do not change quantifiers bound in input.
 *
 * @param input the term to change
 * @param bvi   bound variable index
 * @return changed copy of body.
 */
private fun shiftQuantifiers(input: Lambda, bvi: Int): Lambda =
        when (input) {
            is QVar ->
                if (input.index < bvi)
                // variable is bound in current expression
                    input
                else if (input.index > bvi)
                // Variable is free in current expression, and
                // would remain free if we wrapped the expression.
                //
                // If we get to this function, we are not in fact
                // going to wrap, because we have decided that
                // the wrapper would be vacuous.
                //
                // The variable must be bound higher up, so we need to shift
                // the index down one, so that it will stay bound
                // to the same thing in the unwrapped result.
                    QVar(input.index - 1)
                else {
                    assert(input.index == bvi)
                    // this shouldn't ever happen, because the precondition is that
                    // quantifierBinds should have returned false. If we get here, something
                    // must be wrong.
                    throw IllegalArgumentException("variable should not be bound: $input")
                }
            is Exists ->
                createExistential(shiftQuantifiers(input.body, bvi + 1))
            is Forall ->
                createUniversal(shiftQuantifiers(input.body, bvi + 1))
            is And -> createAnd(input.conjuncts.map { shiftQuantifiers(it, bvi) })
            is Or -> createOr(input.disjuncts.map { shiftQuantifiers(it, bvi) })
            is Lam -> createLam(shiftQuantifiers(input.body, bvi))
            is Not -> createNegation(shiftQuantifiers(input.body, bvi))
            is Implies -> createImplication(shiftQuantifiers(input.e1, bvi), shiftQuantifiers(input.e2, bvi))
            is Equiv -> Equiv(shiftQuantifiers(input.e1, bvi), shiftQuantifiers(input.e2, bvi))
            is App -> createApp(shiftQuantifiers(input.e1, bvi), shiftQuantifiers(input.e2, bvi))
            is Var -> input
            is FstructVar -> input
            is Constant -> input
            is Box -> Box
            is Empty -> Empty
        }

/**
 * Create an application of predicate to argument, or, if predicate
 * is a lambda abstract, do the appropriate β-reduction.
 * betaReduce uses factory functions, which guarantees the absence
 * of reducible sub-terms.
 *
 * @param predicate
 * @param argument
 * @return the result.
 */
fun createApp(predicate: Lambda, argument: Lambda): Lambda {
    return if (predicate is Lam) {
        betaReduce(App(predicate, argument))
    } else {
        App(predicate, argument)
    }
}


/**
 * Create a lambda abstraction. This has no real
 * action in it, and is done only for uniformity
 *
 * @param body
 * @return the lambda abstract,
 */
fun createLam(body: Lambda): Lambda {
    return Lam(body)
}


/**
 * Make a negation, simplifying away double negation.
 * and applying DeMorgan equivalences
 * @param body
 * @return
 */
fun createNegation(body: Lambda): Lambda =
        if (body is Not) // two negatives make a positive, return the body's body.
            body.body
        else if (body is And && allNegated(body.conjuncts))
            createNegation(createOr(body.conjuncts.map { (it as Not).body }))
        else if (body is Or && allNegated(body.disjuncts))
            createNegation(createAnd(body.disjuncts.map { (it as Not).body }))
        else
        // make a real negation.
            Not(body)


/**
 * Create a conjunction from a collection of conjuncts, simplifying
 * as needed.
 *
 * @param conjuncts the conjuncts
 * @return the conjunction.
 */


/**
 * Make a conjunction possibly simplifying it
 * if the opportunity arises.
 *
 * @param conjunct  first conjunct in conjunction.
 * @param conjuncts the second and subsequent conjunctions
 * @return the conjunction, possibly simplified
 */
fun createAnd(conjunct: Lambda, vararg conjuncts: Lambda): Lambda =
        if (conjuncts.size == 0)
        // no need to build the And
            conjunct
        else
            createAnd(listOf(conjunct) + conjuncts.toList())

fun createAnd(conjuncts: List<Lambda>): Lambda {
    fun andYield(item: Lambda): List<Lambda> =
            when (item) {
                is And -> item.conjuncts.flatMap(::andYield)
                else -> listOf(item)
            }

    val newConjuncts = conjuncts.flatMap(::andYield).toSet()

    return if (newConjuncts.size == 1)
        newConjuncts.single()
    else And(newConjuncts)

}

/**
 * Create a disjunction, possibly simplifying it away if
 * not needed
 *
 * @param disjunct  the first disjunct.
 * @param disjuncts any remaining disjuncts.
 * @return the disjunction
 */

fun createOr(disjunct: Lambda, vararg disjuncts: Lambda): Lambda =
        if (disjuncts.size == 0)
            disjunct
        else
            createOr(listOf(disjunct) + disjuncts.toList())


/**
 * Create a disjunction from a collection of disjuncts, simplifying
 * when possible
 *
 * @param disjuncts the disjuncts
 * @return the
 */

fun createOr(disjuncts: List<Lambda>): Lambda {
    fun orYield(item: Lambda): List<Lambda> =
            when (item) {
                is Or -> item.disjuncts.flatMap(::orYield)
                else -> listOf(item)
            }

    val newDisjuncts = disjuncts.flatMap(::orYield).toSet()

    return if (newDisjuncts.size == 1)
        newDisjuncts.single()
    else Or(newDisjuncts)

}

/**
 * Utility for checking if all items in a list are negated.
 *
 * @param conjuncts
 * @return
 */

private fun allNegated(conjuncts: Collection<Lambda>): Boolean {

    for (conjunct in conjuncts) {
        if (conjunct !is Not) {
            return false
        }
    }
    return true
}

private fun denegate(juncts: Set<Lambda>): List<Lambda> = juncts.map { (it as Not).body }


/**
 * Check whether expression has a betaRedex.
 *
 * @receiver the expression to be tested for reducibility
 * @return true if reduction is possible.
 */
fun Lambda.betaReducible(): Boolean =
        when (this) {
            is Constant -> false
            is FstructVar -> false
            is Var -> false
            is QVar -> false
            is Box -> false
            is Empty -> false
            is And -> conjuncts.any { it -> it.betaReducible() }
            is Or -> disjuncts.any { it -> it.betaReducible() }
            is App -> e1 is Lam || e1.betaReducible() || e2.betaReducible()
            is Exists -> body.betaReducible()
            is Forall -> body.betaReducible()
            is Not -> body.betaReducible()
            is Lam -> body.betaReducible()
            is Equiv -> e1.betaReducible() || e2.betaReducible()
            is Implies -> e1.betaReducible() || e2.betaReducible()
        }


fun normalOrderReduce(ex: Lambda): Lambda {
    var r = ex
    while (r.betaReducible()) {
        r = betaReduce(r)

    }
    return r
}

/**
 * Do beta reduction on the outermost
 * leftmost beta redex. Raise exception
 * if not possible.
 *
 * @param ex the expression to be reduced.
 * @return the result of beta reduction
 */

private fun betaReduce(ex: Lambda): Lambda =
        when (ex) {
            is App -> {
                val p = ex.e1
                val arg = ex.e2
                if (p is Lam) {
                    substBoxes(shift(placeBoxes(p.body), -1), arg)

                } else if (p.betaReducible()) {
                    App(betaReduce(p), arg)
                } else if (arg.betaReducible()) {
                    App(p, betaReduce(arg))
                } else {
                    throw IllegalArgumentException("unexpected fail in beta reduce: $ex")
                }
            }
            is And -> {
                var reductionNeeded = true
                And(ex.conjuncts.map(
                        {
                            if (reductionNeeded && it.betaReducible()) {
                                reductionNeeded = false
                                return betaReduce(it)
                            } else it
                        })
                        .toSet())

            }
            is Or -> {
                var reductionNeeded = true
                Or(ex.disjuncts.map(
                        {
                            if (reductionNeeded && it.betaReducible()) {
                                reductionNeeded = false
                                return betaReduce(it)
                            } else it
                        })
                        .toSet())
            }
            is Forall -> Forall(betaReduce(ex.body))
            is Exists -> Exists(betaReduce(ex.body))
            is Implies ->
                if (ex.e1.betaReducible())
                    Implies(betaReduce(ex.e1), ex.e2)
                else
                    Implies(ex.e1, betaReduce(ex.e2))
            is Equiv -> if (ex.e1.betaReducible())
                Equiv(betaReduce(ex.e1), ex.e2)
            else
                Equiv(ex.e1, betaReduce(ex.e2))
            is Lam -> Lam(betaReduce(ex.body))
            else -> throw Exception("unexpected: $ex")
        }


/**
 * Place boxes (i.e. markers for substitution) in all the places
 * in ex whose binder is the Lam that was just
 * removed by betaReduce.
 *
 * @param em
 * @return the new expression.
 */
fun placeBoxes(em: Lambda): Lambda {
    return placeBoxes(em, 1)
}

private fun placeBoxes(e: Lambda, bvi: Int): Lambda =
        when (e) {
            is And -> And(e.conjuncts.map { it -> placeBoxes(it, bvi) }.toSet())
            is Or -> Or(e.disjuncts.map { it -> placeBoxes(it, bvi) }.toSet())
            is Constant -> e
            is FstructVar -> e
            is Var -> if (bvi == e.index) Box else e
            is QVar -> e
            is Box -> e
            is Empty -> e
            is Forall -> Forall(placeBoxes(e.body, bvi))
            is Exists -> Exists(placeBoxes(e.body, bvi))
            is Not -> Not(placeBoxes(e.body, bvi))
            is Lam -> Lam(placeBoxes(e.body, bvi + 1))
            is App -> App(placeBoxes(e.e1, bvi), placeBoxes(e.e2, bvi))
            is Equiv -> Equiv(placeBoxes(e.e1, bvi), placeBoxes(e.e2, bvi))
            is Implies -> Implies(placeBoxes(e.e1, bvi), placeBoxes(e.e2, bvi))
        }


fun substBoxes(e: Lambda, x: Lambda) =
        substBoxes(e, x, 0, 0)


/**
 * substitute an expression into the boxes. Because this is
 * the last step in beta reduction, it uses factory functions
 * to implement simplifications.
 *
 * @param e  expression that will have en substituted into it
 * @param x  expression to substitute into e
 * @param bvi bound variable index, tracks number of lambdas found
 * @param qvi quantified variable index, tracks number of quantifiers found in em
 * @return expression with substitution done.
 */

private fun substBoxes(e: Lambda, x: Lambda, bvi: Int, qvi: Int): Lambda =
        when (e) {
            is And -> createAnd(e.conjuncts.map { it -> substBoxes(it, x, bvi, qvi) })
            is Or -> createOr(e.disjuncts.map { it -> substBoxes(it, x, bvi, qvi) })
            is Constant -> e
            is FstructVar -> e
            is Var -> e
            is QVar -> e
            is Box -> qshift(shift(x, bvi), qvi)
            is Empty -> e
            is Forall -> createUniversal(substBoxes(e.body, x, bvi, qvi + 1))
            is Exists -> createExistential(substBoxes(e.body, x, bvi, qvi + 1))
            is Not -> createNegation(substBoxes(e.body, x, bvi, qvi))
            is Lam -> createLam(substBoxes(e.body, x, bvi + 1, qvi))
            is App -> createApp(substBoxes(e.e1, x, bvi, qvi), substBoxes(e.e2, x, bvi, qvi))
            is Equiv -> createEquiv(substBoxes(e.e1, x, bvi, qvi), substBoxes(e.e2, x, bvi, qvi))
            is Implies -> createImplication(substBoxes(e.e1, x, bvi, qvi), substBoxes(e.e2, x, bvi, qvi))
        }

/**
 * shift the quantified variables of en
 */

fun qshift(en: Lambda, amount: Int): Lambda {
    return qshift(en, amount, 0)
}

/**
 * shift the quantified variables of en
 *
 * @param e
 * @param amount
 * @return changed expression.
 */
private fun qshift(e: Lambda, amount: Int, qvi: Int): Lambda =
        when (e) {
            is And -> createAnd(e.conjuncts.map { it -> qshift(it, amount, qvi) })
            is Or -> createOr(e.disjuncts.map { it -> qshift(it, amount, qvi) })
            is Constant -> e
            is FstructVar -> e
            is Var -> e
            is QVar -> if (e.index > qvi) QVar(e.index + amount) else e
            is Box -> e
            is Empty -> e
            is Forall -> createUniversal(qshift(e.body, amount, qvi + 1))
            is Exists -> createExistential(qshift(e.body, amount, qvi + 1))
            is Not -> Not(qshift(e.body, amount, qvi))
            is Lam -> createLam(qshift(e.body, amount, qvi))
            is App -> App(qshift(e.e1, amount, qvi), qshift(e.e2, amount, qvi))
            is Equiv -> createEquiv(qshift(e.e1, amount, qvi), qshift(e.e2, amount, qvi))
            is Implies -> createImplication(qshift(e.e1, amount, qvi), qshift(e.e2, amount, qvi))
        }


/**
 * Shift the free variables of ex by n.
 *
 * @param em expression to shift
 * @param n  amount to shift
 * @return new shifted expression
 */
fun shift(em: Lambda, n: Int): Lambda {
    return shift(em, n, 0)
}

/**
 * Shift the free variables of ex by n.
 *
 * @param e  expression to shift
 * @param amount   amount to shift
 * @param bvi largest bound index
 * @return new shifted expression
 */

private fun shift(e: Lambda, amount: Int, bvi: Int): Lambda =
        when (e) {
            is And -> And(e.conjuncts.map { it -> shift(it, amount, bvi) }.toSet())
            is Or -> Or(e.disjuncts.map { it -> shift(it, amount, bvi) }.toSet())
            is Constant -> e
            is FstructVar -> e
            is Var -> if (e.index > bvi) Var(e.index + amount) else e
            is QVar -> e
            is Box -> e
            is Empty -> e
            is Forall -> Forall(shift(e.body, amount, bvi))
            is Exists -> Exists(shift(e.body, amount, bvi))
            is Not -> Not(shift(e.body, amount, bvi))
            is Lam -> Lam(shift(e.body, amount, bvi + 1))
            is App -> App(shift(e.e1, amount, bvi), shift(e.e2, amount, bvi))
            is Equiv -> Equiv(shift(e.e1, amount, bvi), shift(e.e2, amount, bvi))
            is Implies -> Implies(shift(e.e1, amount, bvi), shift(e.e2, amount, bvi))
        }

fun closed(term: Lambda) = term.closed(0, 0)

private fun Lambda.closed(bvi: Int, qvi: Int): Boolean =
        when (this) {
            is And -> conjuncts.all { it.closed(bvi, qvi) }
            is Or -> disjuncts.all { it.closed(bvi, qvi) }
            is Constant -> true
            is FstructVar -> true
            is Var -> index <= bvi
            is QVar -> index <= qvi
            is Box -> true
            is Empty -> true
            is Forall -> body.closed(bvi, qvi + 1)
            is Exists -> body.closed(bvi, qvi + 1)
            is Not -> body.closed(bvi, qvi)
            is Lam -> body.closed(bvi + 1, qvi)
            is App -> e1.closed(bvi, qvi) && e2.closed(bvi, qvi)
            is Equiv -> e1.closed(bvi, qvi) && e2.closed(bvi, qvi)
            is Implies -> e1.closed(bvi, qvi) && e2.closed(bvi, qvi)
        }


/**
 * utility for accessing the components of a term. intended for debugging
 */

operator fun Lambda.get(index: Int): Lambda =
        when (this) {
            is Constant -> null
            is QVar -> null
            is FstructVar -> null
            is Empty -> null
            is Box -> null
            is Var -> null
            is Lam -> if (index == 0) body else null
            is Exists -> if (index == 0) body else null
            is Not -> if (index == 0) body else null
            is Forall -> if (index == 0) body else null
            is Implies -> when (index) {
                0 -> e1
                1 -> e2
                else -> null
            }
            is Equiv -> when (index) {
                0 -> e1
                1 -> e2
                else -> null
            }
            is App -> when (index) {
                0 -> e1
                1 -> e2
                else -> null
            }
            is Or -> disjuncts.toList().get(index)
            is And -> conjuncts.toList().get(index)
        } ?: Empty