// Generated from com/cbrew/fstruct/notation/FeatureTerms.g4 by ANTLR 4.7
package com.cbrew.fstruct.notation;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FeatureTermsParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FeatureTermsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#cfg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCfg(FeatureTermsParser.CfgContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#mcfgrule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMcfgrule(FeatureTermsParser.McfgruleContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#cfgrule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCfgrule(FeatureTermsParser.CfgruleContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#cfgrhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCfgrhs(FeatureTermsParser.CfgrhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#mcfgrhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMcfgrhs(FeatureTermsParser.McfgrhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#rhspart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRhspart(FeatureTermsParser.RhspartContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#mcfgrhspart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMcfgrhspart(FeatureTermsParser.McfgrhspartContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#linseq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLinseq(FeatureTermsParser.LinseqContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#lexentry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLexentry(FeatureTermsParser.LexentryContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWord(FeatureTermsParser.WordContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(FeatureTermsParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#categoryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCategoryExpression(FeatureTermsParser.CategoryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#mapping}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapping(FeatureTermsParser.MappingContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#fpair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFpair(FeatureTermsParser.FpairContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#fvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFvalue(FeatureTermsParser.FvalueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#flist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlist(FeatureTermsParser.FlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureTermsParser#semantics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSemantics(FeatureTermsParser.SemanticsContext ctx);
}