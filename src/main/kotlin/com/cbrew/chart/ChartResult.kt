package com.cbrew.chart
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
                        ) {

}