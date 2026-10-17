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
 * no conditions is true.
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
    is Resolved -> {
        require(antecedents.isNotEmpty()) { "pronoun $pronoun has no candidate antecedents" }
        createOr(antecedents.map { createEquiv(pronoun, it) })
    }
    else -> this
}

/** The conjunction of the translated [conds], or [TRUE] when there are none. */
private fun conjunction(conds: List<Lambda>): Lambda =
        if (conds.isEmpty()) TRUE else createAnd(conds.map { it.fol() })
