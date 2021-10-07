package com.cbrew.chart


/**
 * A set of classes for representing basic semantics. No disjunction.
 * Would please Terry Parsons.
 */
sealed class Predicate
data class NullaryPredicate(val name:String): Predicate()
data class UnaryPredicate(val name:String, val argument1:String)   : Predicate()
data class BinaryPredicate(val name:String,val argument1:String,val argument2:String)   : Predicate()
data class NegatedPredicate(val body: Predicate): Predicate()
data class ConjoinedPredicate(val conjuncts: Set<Predicate>): Predicate()


/**
 * A class for returning results from a chart parser
 * in a simplified, accessible form. Puts the full
 * syntax and semantics into string form, because
 * we want to pass them to components that don't
 * need to understand the real data structure.
 */
data class ChartResult(val wordSpans: List<Span>,
                       val preTerminals:List<Span>,
                       val nonTerminals:List<Span>,
                       val simpleSemantics: Map<Int,Predicate>,
                       val fullSemantics: Map<Int, String>,
                       val fullSyntax: Map<Int, String>,
                        )