// Generated from com/cbrew/logic/notation/LogicTerms.g4 by ANTLR 4.7
package com.cbrew.logic.notation;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link LogicTermsParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface LogicTermsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#semantics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSemantics(LogicTermsParser.SemanticsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Injectable}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInjectable(LogicTermsParser.InjectableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(LogicTermsParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(LogicTermsParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ConstantExpr}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpr(LogicTermsParser.ConstantExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelational(LogicTermsParser.RelationalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Box}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBox(LogicTermsParser.BoxContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Predicate}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(LogicTermsParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Individual}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndividual(LogicTermsParser.IndividualContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Parenthesized}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesized(LogicTermsParser.ParenthesizedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Exists}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExists(LogicTermsParser.ExistsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code And}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(LogicTermsParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Negated}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegated(LogicTermsParser.NegatedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Forall}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForall(LogicTermsParser.ForallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Lambda}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambda(LogicTermsParser.LambdaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Application}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplication(LogicTermsParser.ApplicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#constantExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpression(LogicTermsParser.ConstantExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#boolOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolOp(LogicTermsParser.BoolOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#relationTail}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationTail(LogicTermsParser.RelationTailContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#parenthesizedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedExpression(LogicTermsParser.ParenthesizedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#existsExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistsExpression(LogicTermsParser.ExistsExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#forallExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForallExpression(LogicTermsParser.ForallExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#lambdaExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaExpression(LogicTermsParser.LambdaExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#injectableExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInjectableExpression(LogicTermsParser.InjectableExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#applicationTail}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplicationTail(LogicTermsParser.ApplicationTailContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#negation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegation(LogicTermsParser.NegationContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgument(LogicTermsParser.ArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicTermsParser#larg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLarg(LogicTermsParser.LargContext ctx);
}