// Generated from com/cbrew/logic/notation/LogicTerms.g4 by ANTLR 4.7
package com.cbrew.logic.notation;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LogicTermsParser}.
 */
public interface LogicTermsListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#semantics}.
	 * @param ctx the parse tree
	 */
	void enterSemantics(LogicTermsParser.SemanticsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#semantics}.
	 * @param ctx the parse tree
	 */
	void exitSemantics(LogicTermsParser.SemanticsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Injectable}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterInjectable(LogicTermsParser.InjectableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Injectable}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitInjectable(LogicTermsParser.InjectableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterVariable(LogicTermsParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitVariable(LogicTermsParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Or}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterOr(LogicTermsParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitOr(LogicTermsParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ConstantExpr}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterConstantExpr(LogicTermsParser.ConstantExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ConstantExpr}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitConstantExpr(LogicTermsParser.ConstantExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterRelational(LogicTermsParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitRelational(LogicTermsParser.RelationalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Box}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBox(LogicTermsParser.BoxContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Box}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBox(LogicTermsParser.BoxContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Predicate}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(LogicTermsParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Predicate}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(LogicTermsParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Individual}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIndividual(LogicTermsParser.IndividualContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Individual}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIndividual(LogicTermsParser.IndividualContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Parenthesized}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesized(LogicTermsParser.ParenthesizedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Parenthesized}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesized(LogicTermsParser.ParenthesizedContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Exists}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExists(LogicTermsParser.ExistsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Exists}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExists(LogicTermsParser.ExistsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code And}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAnd(LogicTermsParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code And}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAnd(LogicTermsParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Negated}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNegated(LogicTermsParser.NegatedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Negated}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNegated(LogicTermsParser.NegatedContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Forall}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterForall(LogicTermsParser.ForallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Forall}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitForall(LogicTermsParser.ForallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Lambda}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLambda(LogicTermsParser.LambdaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Lambda}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLambda(LogicTermsParser.LambdaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Application}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterApplication(LogicTermsParser.ApplicationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Application}
	 * labeled alternative in {@link LogicTermsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitApplication(LogicTermsParser.ApplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstantExpression(LogicTermsParser.ConstantExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstantExpression(LogicTermsParser.ConstantExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#boolOp}.
	 * @param ctx the parse tree
	 */
	void enterBoolOp(LogicTermsParser.BoolOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#boolOp}.
	 * @param ctx the parse tree
	 */
	void exitBoolOp(LogicTermsParser.BoolOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#relationTail}.
	 * @param ctx the parse tree
	 */
	void enterRelationTail(LogicTermsParser.RelationTailContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#relationTail}.
	 * @param ctx the parse tree
	 */
	void exitRelationTail(LogicTermsParser.RelationTailContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#parenthesizedExpression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedExpression(LogicTermsParser.ParenthesizedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#parenthesizedExpression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedExpression(LogicTermsParser.ParenthesizedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#existsExpression}.
	 * @param ctx the parse tree
	 */
	void enterExistsExpression(LogicTermsParser.ExistsExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#existsExpression}.
	 * @param ctx the parse tree
	 */
	void exitExistsExpression(LogicTermsParser.ExistsExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#forallExpression}.
	 * @param ctx the parse tree
	 */
	void enterForallExpression(LogicTermsParser.ForallExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#forallExpression}.
	 * @param ctx the parse tree
	 */
	void exitForallExpression(LogicTermsParser.ForallExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void enterLambdaExpression(LogicTermsParser.LambdaExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void exitLambdaExpression(LogicTermsParser.LambdaExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#injectableExpression}.
	 * @param ctx the parse tree
	 */
	void enterInjectableExpression(LogicTermsParser.InjectableExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#injectableExpression}.
	 * @param ctx the parse tree
	 */
	void exitInjectableExpression(LogicTermsParser.InjectableExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#applicationTail}.
	 * @param ctx the parse tree
	 */
	void enterApplicationTail(LogicTermsParser.ApplicationTailContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#applicationTail}.
	 * @param ctx the parse tree
	 */
	void exitApplicationTail(LogicTermsParser.ApplicationTailContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#negation}.
	 * @param ctx the parse tree
	 */
	void enterNegation(LogicTermsParser.NegationContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#negation}.
	 * @param ctx the parse tree
	 */
	void exitNegation(LogicTermsParser.NegationContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#argument}.
	 * @param ctx the parse tree
	 */
	void enterArgument(LogicTermsParser.ArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#argument}.
	 * @param ctx the parse tree
	 */
	void exitArgument(LogicTermsParser.ArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicTermsParser#larg}.
	 * @param ctx the parse tree
	 */
	void enterLarg(LogicTermsParser.LargContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicTermsParser#larg}.
	 * @param ctx the parse tree
	 */
	void exitLarg(LogicTermsParser.LargContext ctx);
}