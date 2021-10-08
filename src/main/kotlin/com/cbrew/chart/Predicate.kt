package com.cbrew.chart

/**
 * A set of classes for representing basic semantics. No disjunction.
 * Would please Terry Parsons.
 */
sealed class Predicate
data class NullaryPredicate(val name:String): Predicate()
data class UnaryPredicate(val name:String, val argument1:String)   : Predicate()
data class BinaryPredicate(val name:String, val argument1:String, val argument2:String)   : Predicate()
data class NegatedPredicate(val body: Predicate): Predicate()
data class ConjoinedPredicate(val conjuncts: Set<Predicate>): Predicate()