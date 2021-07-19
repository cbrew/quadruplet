// Generated from com/cbrew/fstruct/notation/Linearization.g4 by ANTLR 4.7
package com.cbrew.fstruct.notation;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link LinearizationParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface LinearizationVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link LinearizationParser#numbers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumbers(LinearizationParser.NumbersContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinearizationParser#numseq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumseq(LinearizationParser.NumseqContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinearizationParser#numseqs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumseqs(LinearizationParser.NumseqsContext ctx);
}