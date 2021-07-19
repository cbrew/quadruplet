// Generated from com/cbrew/fstruct/notation/Linearization.g4 by ANTLR 4.7
package com.cbrew.fstruct.notation;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LinearizationParser}.
 */
public interface LinearizationListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link LinearizationParser#numbers}.
	 * @param ctx the parse tree
	 */
	void enterNumbers(LinearizationParser.NumbersContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinearizationParser#numbers}.
	 * @param ctx the parse tree
	 */
	void exitNumbers(LinearizationParser.NumbersContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinearizationParser#numseq}.
	 * @param ctx the parse tree
	 */
	void enterNumseq(LinearizationParser.NumseqContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinearizationParser#numseq}.
	 * @param ctx the parse tree
	 */
	void exitNumseq(LinearizationParser.NumseqContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinearizationParser#numseqs}.
	 * @param ctx the parse tree
	 */
	void enterNumseqs(LinearizationParser.NumseqsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinearizationParser#numseqs}.
	 * @param ctx the parse tree
	 */
	void exitNumseqs(LinearizationParser.NumseqsContext ctx);
}