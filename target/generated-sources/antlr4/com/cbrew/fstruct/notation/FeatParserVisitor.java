// Generated from com/cbrew/fstruct/notation/FeatParser.g4 by ANTLR 4.7
package com.cbrew.fstruct.notation;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FeatParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FeatParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FeatParser#cfg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCfg(FeatParser.CfgContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#mcfgrule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMcfgrule(FeatParser.McfgruleContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#cfgrule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCfgrule(FeatParser.CfgruleContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#cfgrhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCfgrhs(FeatParser.CfgrhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#mcfgrhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMcfgrhs(FeatParser.McfgrhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#rhspart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRhspart(FeatParser.RhspartContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#mcfgrhspart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMcfgrhspart(FeatParser.McfgrhspartContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#linseq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLinseq(FeatParser.LinseqContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#numseq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumseq(FeatParser.NumseqContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#lexentry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLexentry(FeatParser.LexentryContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWord(FeatParser.WordContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#featureMap}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFeatureMap(FeatParser.FeatureMapContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#mapping}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapping(FeatParser.MappingContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#fpair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFpair(FeatParser.FpairContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#fvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFvalue(FeatParser.FvalueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#flist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlist(FeatParser.FlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#semantics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSemantics(FeatParser.SemanticsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(FeatParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(FeatParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Parenthesized}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesized(FeatParser.ParenthesizedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Exists}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExists(FeatParser.ExistsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Constant}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(FeatParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code And}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(FeatParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelational(FeatParser.RelationalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Negated}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegated(FeatParser.NegatedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Box}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBox(FeatParser.BoxContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Forall}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForall(FeatParser.ForallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Lambda}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambda(FeatParser.LambdaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Application}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplication(FeatParser.ApplicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#boolOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolOp(FeatParser.BoolOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#relationTail}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationTail(FeatParser.RelationTailContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#parenthesizedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedExpression(FeatParser.ParenthesizedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#existsExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistsExpression(FeatParser.ExistsExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#forallExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForallExpression(FeatParser.ForallExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#lambdaExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaExpression(FeatParser.LambdaExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#applicationTail}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplicationTail(FeatParser.ApplicationTailContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#negation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegation(FeatParser.NegationContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatParser#larg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLarg(FeatParser.LargContext ctx);
}