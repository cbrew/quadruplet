package com.cbrew.unify

/**
 * In the following functions we spell out exactly how unification works.
 * For feature structures, this is completely standard graph unification.
 * For semantic terms, things are more restricted: we can bind a semantic
 * term against a ?x variable, and check consistency with an existing binding,
 * but we don't recurse into semantic terms.
 *
 * The code makes heavy use of pattern matching over types using when() and is().
 * ?x variables are intercepted early and fed to checkBinding, other instances
 * of Unifiable are sent to functions specialized for subtypes. For greater
 * transparency, these are given distinct names even though Kotlin's pattern
 * matching ensures that they could all be called unify.
 */


private typealias UM = Map<CharSequence, Unifiable>

typealias UR = Pair<Unifiable, UM>

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

    val m2: Map<CharSequence, Unifiable> = mapOf<CharSequence, Unifiable>(*names.zip(newVariables).toTypedArray())
    return m2.subst(this)
}

private fun name(v: Unifiable): CharSequence =
        when (v) {
            is FstructVar -> v.name
            is QueryVariable -> v.name
            else -> throw Exception("should not be possible")
        }


fun findVariables(item: Unifiable, variables: MutableSet<Unifiable>) {
    when (item) {
        is SemanticValue -> findVariables(item.value, variables)  // reach into semantic value
        is QueryVariable -> variables.add(item)
        is FeatureList -> item.elements.forEach { findVariables(it, variables) }
        is FeatureMap -> item.forEach { _, v -> findVariables(v, variables) }
        is AtomicValue -> {
        }
        is Grammar -> {
        }
        is CfgRule -> {
        }
        is McfgRule -> {
        }
        is Integer -> {
        }
        is Lambda -> {
            val lam: Lambda = item

            when (lam) {
                is Constant -> {
                }
                is Var -> {
                }
                is QVar -> {
                }
                is Box -> {
                }
                is Empty -> {
                }
                is Implies -> {
                    findVariables(lam.e1, variables)
                    findVariables(lam.e1, variables)
                }
                is Equiv -> {
                    findVariables(lam.e1, variables)
                    findVariables(lam.e1, variables)
                }
                is App -> {
                    findVariables(lam.e1, variables)
                    findVariables(lam.e1, variables)
                }
                is And -> lam.conjuncts.forEach { findVariables(it, variables) }
                is Or -> lam.disjuncts.forEach { findVariables(it, variables) }
                is Not -> findVariables(lam.body, variables)
                is Lam -> findVariables(lam.body, variables)
                is Exists -> findVariables(lam.body, variables)
                is Forall -> findVariables(lam.body, variables)
                is FstructVar -> variables.add(item)
            }
        }
    }
}


fun unify(uf1: Unifiable, uf2: Unifiable): UR? =
        unify(uf1, uf2, mapOf())

private fun unify(uf1: Unifiable, uf2: Unifiable, bindings: UM): UR? =
        when (uf1) {
            is FstructVar -> bindings.checkBinding(uf1, uf2)
            is QueryVariable -> bindings.checkBinding(uf1, uf2)
            is Lambda -> unifyLU(uf1, uf2, bindings)
            is FeatureStructure -> unifyFU(uf1, uf2, bindings)
        }


private fun unifyLU(uf1: Lambda, uf2: Unifiable, bindings: UM): UR? =
        when (uf2) {
            is FstructVar -> bindings.checkBinding(uf2, uf1)
            is QueryVariable -> bindings.checkBinding(uf2, uf1)
            is Lambda -> unifyLL(uf1, uf2, bindings)
            is FeatureStructure -> unifyLF(uf1, uf2, bindings)
        }

private fun unifyFU(uf1: FeatureStructure, uf2: Unifiable, bindings: UM): UR? =
        when (uf2) {
            is FstructVar -> bindings.checkBinding(uf2, uf1)
            is QueryVariable -> bindings.checkBinding(uf2, uf1)
            is Lambda -> unifyFL(uf1, uf2, bindings)
            is FeatureStructure -> unifyFF(uf1, uf2, bindings)
        }


private fun unifyLL(uf1: Lambda, uf2: Lambda, bindings: UM): UR? = null
private fun unifyLF(uf1: Lambda, uf2: FeatureStructure, bindings: UM): UR? = null
private fun unifyFL(uf1: FeatureStructure, uf2: Lambda, bindings: UM): UR? = null
private fun unifyFF(fs1: FeatureStructure, fs2: FeatureStructure, bindings: UM): UR? =
        if (fs1 is QueryVariable || fs2 is QueryVariable)
            throw Exception("variables should have been caught earlier")
        else if (fs1 == fs2)
            UR(fs1, bindings)
        else if (fs1 is FeatureMap && fs2 is FeatureMap) {
            val sharedFromFs1: Map<String, Unifiable> = fs1.filterKeys { k -> k in fs2 }
            val sharedFromBoth: Map<String, Pair<Unifiable, Unifiable>> =
                    sharedFromFs1.mapValues { (k, v1) -> Pair(v1, fs2[k]!!) }
            val symdiff: Map<String, Unifiable> =
                    (fs1.filterKeys { k -> !(k in fs2) }) +
                            (fs2.filterKeys { k -> !(k in fs1) })

            var result = symdiff
            var newBindings = bindings

            sharedFromBoth.forEach {
                val subresult: UR? = unify(it.value.first, it.value.second, newBindings)
                if (subresult == null)
                    return null
                else {
                    val (fs, bs) = subresult
                    newBindings = bs
                    result += Pair(it.key, fs)
                }
            }

            UR(FeatureMap(result), newBindings)
        } else if (fs1 is FeatureList && fs2 is FeatureList && fs1.elements.size == fs2.elements.size) {


            var newBindings = bindings
            var newElements = mutableListOf<FeatureStructure>()


            fs1.elements.zip(fs2.elements).forEach { (x1, x2) ->
                val x12 = unify(x1, x2, newBindings)
                if (x12 == null)
                    return null
                else {
                    newElements.add(x12.first as FeatureStructure)
                    newBindings += x12.second
                }

            }

            UR(FeatureList(newElements), newBindings)

        } else if (fs1 is SemanticValue && fs2 is SemanticValue)
            unify(fs1.value, fs2.value, bindings)?.let { (v, bs) -> UR(SemanticValue(v as Lambda), bs) }
        else
            null


fun UM.checkBinding(uf1: FstructVar, uf2: Unifiable): UR? =
        if (uf2 is Lambda && uf1.name in this)
            checkConsistency(deref(uf1), deref(uf2))
        else if (uf2 is Lambda)
            UR(uf2, this + Pair(uf1.name, uf2))
        else
            null

fun UM.checkBinding(uf1: QueryVariable, uf2: Unifiable): UR? =
        if (uf2 is FeatureStructure && uf1.name in this)
            checkConsistency(deref(uf1), deref(uf2))
        else if (uf2 is FeatureStructure)
            UR(uf2, this + Pair(uf1.name, uf2))
        else
            null


fun UM.checkConsistency(ufa: Unifiable, ufb: Unifiable): UR? =
        if (
                (ufa == ufb) ||
                (ufa is FstructVar && ufb is FstructVar) ||
                (ufa is QueryVariable && ufb is QueryVariable) ||
                (ufa is QueryVariable && ufb is FeatureStructure) ||
                (ufa is FstructVar && ufb is Lambda)
        )
            UR(ufa, this)
        else
            null


// dereference a variable using a set of bindings
// Yields either an unbound variable or a substantive
// term, never a bound variable.
tailrec fun UM.deref(v: Unifiable): Unifiable =
        when (v) {
            is FstructVar ->
                if (v.name in this)
                    deref(this[v.name]!!)
                else
                    v
            is QueryVariable ->
                if (v.name in this)
                    deref(this[v.name]!!)
                else
                    v
            else -> v
        }


fun UR.subst(): Unifiable =
        second.subst(first)

fun UM.subst(uf: Unifiable): Unifiable =
        when (uf) {
            is FeatureStructure -> subst(uf)
            is Lambda -> subst(uf)
        }


fun UM.subst(ufs: List<Unifiable>): List<Unifiable> = ufs.map(::subst)

fun UM.subst(fs: FeatureStructure): FeatureStructure =
        when (fs) {
            is AtomicValue -> fs
            is Integer -> fs
            is SemanticValue -> SemanticValue(subst(fs.value))
            is QueryVariable -> if (fs.name in this) {
                val r = this[fs.name]!!
                when (r) {
                    is FeatureStructure -> r
                    is Lambda -> throw Exception("Trying to substitute sem term into syn variable")
                }

            } else
                fs
            is FeatureList -> FeatureList(fs.elements.map(::subst))
            is CfgRule -> CfgRule(subst(fs.lhs) as FeatureMap, fs.rhs.map { subst(it) as FeatureMap })
            is McfgRule -> McfgRule(subst(fs.lhs) as FeatureMap, fs.rhs.map { subst(it) as FeatureMap }, fs.linseq)
            is FeatureMap -> FeatureMap(fs.mapValues { subst(it.value) })
            is Grammar -> throw Exception("does not make sense to call subst on Grammar")
        }

/**
 * substitution for lambda terms, with automatic
 * simplifications.
 */

fun UM.subst(lam: Lambda): Lambda =
        when (lam) {
            is Constant -> lam
            is QVar -> lam
            is FstructVar -> {
                val r: Unifiable = deref(lam)
                when (r) {
                    is Lambda -> r
                    is FeatureStructure -> throw Exception("binding of syn term to sem variable")
                }

            }
            is Empty -> lam
            is Box -> lam
            is Var -> lam
            is Lam -> createLam(subst(lam.body))
            is Exists -> createExistential(subst(lam.body))
            is Not -> createNegation(subst(lam.body))
            is Forall -> createUniversal(subst(lam.body))
            is Implies -> createImplication(subst(lam.e1), subst(lam.e2))
            is Equiv -> createEquiv(subst(lam.e1), subst(lam.e2))
            is App -> createApp(subst(lam.e1), subst(lam.e2))
            is Or -> createOr(lam.disjuncts.map(::subst))
            is And -> createAnd(lam.conjuncts.map(::subst))
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
            is And -> createOr(input.conjuncts.map { shiftQuantifiers(it, bvi) })
            is Or -> createAnd(input.disjuncts.map { shiftQuantifiers(it, bvi) })
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
 *
 *
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
 *
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

    return if (conjuncts.size == 1)
        conjuncts.single()
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