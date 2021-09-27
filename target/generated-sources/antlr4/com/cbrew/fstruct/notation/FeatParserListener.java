// Generated from com/cbrew/fstruct/notation/FeatParser.g4 by ANTLR 4.7
package com.cbrew.fstruct.notation;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FeatParser}.
 */
public interface FeatParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FeatParser#cfg}.
	 * @param ctx the parse tree
	 */
	void enterCfg(FeatParser.CfgContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#cfg}.
	 * @param ctx the parse tree
	 */
	void exitCfg(FeatParser.CfgContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#mcfgrule}.
	 * @param ctx the parse tree
	 */
	void enterMcfgrule(FeatParser.McfgruleContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#mcfgrule}.
	 * @param ctx the parse tree
	 */
	void exitMcfgrule(FeatParser.McfgruleContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#cfgrule}.
	 * @param ctx the parse tree
	 */
	void enterCfgrule(FeatParser.CfgruleContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#cfgrule}.
	 * @param ctx the parse tree
	 */
	void exitCfgrule(FeatParser.CfgruleContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#cfgrhs}.
	 * @param ctx the parse tree
	 */
	void enterCfgrhs(FeatParser.CfgrhsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#cfgrhs}.
	 * @param ctx the parse tree
	 */
	void exitCfgrhs(FeatParser.CfgrhsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#mcfgrhs}.
	 * @param ctx the parse tree
	 */
	void enterMcfgrhs(FeatParser.McfgrhsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#mcfgrhs}.
	 * @param ctx the parse tree
	 */
	void exitMcfgrhs(FeatParser.McfgrhsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#rhspart}.
	 * @param ctx the parse tree
	 */
	void enterRhspart(FeatParser.RhspartContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#rhspart}.
	 * @param ctx the parse tree
	 */
	void exitRhspart(FeatParser.RhspartContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#mcfgrhspart}.
	 * @param ctx the parse tree
	 */
	void enterMcfgrhspart(FeatParser.McfgrhspartContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#mcfgrhspart}.
	 * @param ctx the parse tree
	 */
	void exitMcfgrhspart(FeatParser.McfgrhspartContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#linseq}.
	 * @param ctx the parse tree
	 */
	void enterLinseq(FeatParser.LinseqContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#linseq}.
	 * @param ctx the parse tree
	 */
	void exitLinseq(FeatParser.LinseqContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#numseq}.
	 * @param ctx the parse tree
	 */
	void enterNumseq(FeatParser.NumseqContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#numseq}.
	 * @param ctx the parse tree
	 */
	void exitNumseq(FeatParser.NumseqContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#lexentry}.
	 * @param ctx the parse tree
	 */
	void enterLexentry(FeatParser.LexentryContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#lexentry}.
	 * @param ctx the parse tree
	 */
	void exitLexentry(FeatParser.LexentryContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#word}.
	 * @param ctx the parse tree
	 */
	void enterWord(FeatParser.WordContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#word}.
	 * @param ctx the parse tree
	 */
	void exitWord(FeatParser.WordContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#featureMap}.
	 * @param ctx the parse tree
	 */
	void enterFeatureMap(FeatParser.FeatureMapContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#featureMap}.
	 * @param ctx the parse tree
	 */
	void exitFeatureMap(FeatParser.FeatureMapContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#mapping}.
	 * @param ctx the parse tree
	 */
	void enterMapping(FeatParser.MappingContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#mapping}.
	 * @param ctx the parse tree
	 */
	void exitMapping(FeatParser.MappingContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#fpair}.
	 * @param ctx the parse tree
	 */
	void enterFpair(FeatParser.FpairContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#fpair}.
	 * @param ctx the parse tree
	 */
	void exitFpair(FeatParser.FpairContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#fvalue}.
	 * @param ctx the parse tree
	 */
	void enterFvalue(FeatParser.FvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#fvalue}.
	 * @param ctx the parse tree
	 */
	void exitFvalue(FeatParser.FvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#flist}.
	 * @param ctx the parse tree
	 */
	void enterFlist(FeatParser.FlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#flist}.
	 * @param ctx the parse tree
	 */
	void exitFlist(FeatParser.FlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#ftuple}.
	 * @param ctx the parse tree
	 */
	void enterFtuple(FeatParser.FtupleContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#ftuple}.
	 * @param ctx the parse tree
	 */
	void exitFtuple(FeatParser.FtupleContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#fvalues}.
	 * @param ctx the parse tree
	 */
	void enterFvalues(FeatParser.FvaluesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#fvalues}.
	 * @param ctx the parse tree
	 */
	void exitFvalues(FeatParser.FvaluesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#semantics}.
	 * @param ctx the parse tree
	 */
	void enterSemantics(FeatParser.SemanticsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#semantics}.
	 * @param ctx the parse tree
	 */
	void exitSemantics(FeatParser.SemanticsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterVariable(FeatParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitVariable(FeatParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Or}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterOr(FeatParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitOr(FeatParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Constant}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterConstant(FeatParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Constant}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitConstant(FeatParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterRelational(FeatParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitRelational(FeatParser.RelationalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Box}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBox(FeatParser.BoxContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Box}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBox(FeatParser.BoxContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Predicate}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(FeatParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Predicate}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(FeatParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Parenthesized}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesized(FeatParser.ParenthesizedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Parenthesized}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesized(FeatParser.ParenthesizedContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Exists}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExists(FeatParser.ExistsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Exists}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExists(FeatParser.ExistsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Individual}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIndividual(FeatParser.IndividualContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Individual}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIndividual(FeatParser.IndividualContext ctx);
	/**
	 * Enter a parse tree produced by the {@code And}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAnd(FeatParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code And}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAnd(FeatParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Negated}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNegated(FeatParser.NegatedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Negated}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNegated(FeatParser.NegatedContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Forall}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterForall(FeatParser.ForallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Forall}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitForall(FeatParser.ForallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Lambda}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLambda(FeatParser.LambdaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Lambda}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLambda(FeatParser.LambdaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Application}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterApplication(FeatParser.ApplicationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Application}
	 * labeled alternative in {@link FeatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitApplication(FeatParser.ApplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#predicateExpression}.
	 * @param ctx the parse tree
	 */
	void enterPredicateExpression(FeatParser.PredicateExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#predicateExpression}.
	 * @param ctx the parse tree
	 */
	void exitPredicateExpression(FeatParser.PredicateExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#individualExpression}.
	 * @param ctx the parse tree
	 */
	void enterIndividualExpression(FeatParser.IndividualExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#individualExpression}.
	 * @param ctx the parse tree
	 */
	void exitIndividualExpression(FeatParser.IndividualExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstantExpression(FeatParser.ConstantExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstantExpression(FeatParser.ConstantExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#boolOp}.
	 * @param ctx the parse tree
	 */
	void enterBoolOp(FeatParser.BoolOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#boolOp}.
	 * @param ctx the parse tree
	 */
	void exitBoolOp(FeatParser.BoolOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#relationTail}.
	 * @param ctx the parse tree
	 */
	void enterRelationTail(FeatParser.RelationTailContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#relationTail}.
	 * @param ctx the parse tree
	 */
	void exitRelationTail(FeatParser.RelationTailContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#parenthesizedExpression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedExpression(FeatParser.ParenthesizedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#parenthesizedExpression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedExpression(FeatParser.ParenthesizedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#existsExpression}.
	 * @param ctx the parse tree
	 */
	void enterExistsExpression(FeatParser.ExistsExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#existsExpression}.
	 * @param ctx the parse tree
	 */
	void exitExistsExpression(FeatParser.ExistsExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#forallExpression}.
	 * @param ctx the parse tree
	 */
	void enterForallExpression(FeatParser.ForallExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#forallExpression}.
	 * @param ctx the parse tree
	 */
	void exitForallExpression(FeatParser.ForallExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void enterLambdaExpression(FeatParser.LambdaExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void exitLambdaExpression(FeatParser.LambdaExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#applicationTail}.
	 * @param ctx the parse tree
	 */
	void enterApplicationTail(FeatParser.ApplicationTailContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#applicationTail}.
	 * @param ctx the parse tree
	 */
	void exitApplicationTail(FeatParser.ApplicationTailContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#negation}.
	 * @param ctx the parse tree
	 */
	void enterNegation(FeatParser.NegationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#negation}.
	 * @param ctx the parse tree
	 */
	void exitNegation(FeatParser.NegationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#argument}.
	 * @param ctx the parse tree
	 */
	void enterArgument(FeatParser.ArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#argument}.
	 * @param ctx the parse tree
	 */
	void exitArgument(FeatParser.ArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link FeatParser#larg}.
	 * @param ctx the parse tree
	 */
	void enterLarg(FeatParser.LargContext ctx);
	/**
	 * Exit a parse tree produced by {@link FeatParser#larg}.
	 * @param ctx the parse tree
	 */
	void exitLarg(FeatParser.LargContext ctx);
}