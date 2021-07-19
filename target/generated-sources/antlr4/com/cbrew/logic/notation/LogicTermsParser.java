// Generated from com/cbrew/logic/notation/LogicTerms.g4 by ANTLR 4.7
package com.cbrew.logic.notation;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class LogicTermsParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, LINE_COMMENT=2, WS=3, Or=4, And=5, Implies=6, Iff=7, Not=8, NotEquals=9, 
		Equals=10, Comma=11, Lparen=12, Rparen=13, Lambda=14, Forall=15, Exists=16, 
		Dot=17, Box=18, Individual=19, Predicate=20, FstructVariable=21, Constant=22;
	public static final int
		RULE_semantics = 0, RULE_expression = 1, RULE_constantExpression = 2, 
		RULE_boolOp = 3, RULE_relationTail = 4, RULE_parenthesizedExpression = 5, 
		RULE_existsExpression = 6, RULE_forallExpression = 7, RULE_lambdaExpression = 8, 
		RULE_injectableExpression = 9, RULE_applicationTail = 10, RULE_negation = 11, 
		RULE_argument = 12, RULE_larg = 13;
	public static final String[] ruleNames = {
		"semantics", "expression", "constantExpression", "boolOp", "relationTail", 
		"parenthesizedExpression", "existsExpression", "forallExpression", "lambdaExpression", 
		"injectableExpression", "applicationTail", "negation", "argument", "larg"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'@Injectable'", null, null, null, null, null, null, null, null, 
		null, "','", "'('", "')'", null, null, null, "'.'", "'\u2610'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "LINE_COMMENT", "WS", "Or", "And", "Implies", "Iff", "Not", 
		"NotEquals", "Equals", "Comma", "Lparen", "Rparen", "Lambda", "Forall", 
		"Exists", "Dot", "Box", "Individual", "Predicate", "FstructVariable", 
		"Constant"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "LogicTerms.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public LogicTermsParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class SemanticsContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(LogicTermsParser.EOF, 0); }
		public List<TerminalNode> LINE_COMMENT() { return getTokens(LogicTermsParser.LINE_COMMENT); }
		public TerminalNode LINE_COMMENT(int i) {
			return getToken(LogicTermsParser.LINE_COMMENT, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public SemanticsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_semantics; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterSemantics(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitSemantics(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitSemantics(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SemanticsContext semantics() throws RecognitionException {
		SemanticsContext _localctx = new SemanticsContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_semantics);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(30);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LINE_COMMENT:
					{
					setState(28);
					match(LINE_COMMENT);
					}
					break;
				case T__0:
				case Not:
				case Lparen:
				case Lambda:
				case Forall:
				case Exists:
				case Box:
				case Individual:
				case Predicate:
				case FstructVariable:
				case Constant:
					{
					setState(29);
					expression(0);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(32); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << LINE_COMMENT) | (1L << Not) | (1L << Lparen) | (1L << Lambda) | (1L << Forall) | (1L << Exists) | (1L << Box) | (1L << Individual) | (1L << Predicate) | (1L << FstructVariable) | (1L << Constant))) != 0) );
			setState(34);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class InjectableContext extends ExpressionContext {
		public InjectableExpressionContext injectableExpression() {
			return getRuleContext(InjectableExpressionContext.class,0);
		}
		public InjectableContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterInjectable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitInjectable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitInjectable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class VariableContext extends ExpressionContext {
		public TerminalNode FstructVariable() { return getToken(LogicTermsParser.FstructVariable, 0); }
		public VariableContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OrContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Or() { return getToken(LogicTermsParser.Or, 0); }
		public OrContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitOr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ConstantExprContext extends ExpressionContext {
		public ConstantExpressionContext constantExpression() {
			return getRuleContext(ConstantExpressionContext.class,0);
		}
		public ConstantExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterConstantExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitConstantExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitConstantExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RelationalContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public RelationTailContext relationTail() {
			return getRuleContext(RelationTailContext.class,0);
		}
		public RelationalContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterRelational(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitRelational(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitRelational(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BoxContext extends ExpressionContext {
		public TerminalNode Box() { return getToken(LogicTermsParser.Box, 0); }
		public BoxContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterBox(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitBox(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitBox(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PredicateContext extends ExpressionContext {
		public TerminalNode Predicate() { return getToken(LogicTermsParser.Predicate, 0); }
		public PredicateContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class IndividualContext extends ExpressionContext {
		public TerminalNode Individual() { return getToken(LogicTermsParser.Individual, 0); }
		public IndividualContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterIndividual(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitIndividual(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitIndividual(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ParenthesizedContext extends ExpressionContext {
		public ParenthesizedExpressionContext parenthesizedExpression() {
			return getRuleContext(ParenthesizedExpressionContext.class,0);
		}
		public ParenthesizedContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterParenthesized(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitParenthesized(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitParenthesized(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExistsContext extends ExpressionContext {
		public ExistsExpressionContext existsExpression() {
			return getRuleContext(ExistsExpressionContext.class,0);
		}
		public ExistsContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitExists(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AndContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode And() { return getToken(LogicTermsParser.And, 0); }
		public AndContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitAnd(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NegatedContext extends ExpressionContext {
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public NegatedContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterNegated(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitNegated(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitNegated(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ForallContext extends ExpressionContext {
		public ForallExpressionContext forallExpression() {
			return getRuleContext(ForallExpressionContext.class,0);
		}
		public ForallContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterForall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitForall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitForall(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LambdaContext extends ExpressionContext {
		public LambdaExpressionContext lambdaExpression() {
			return getRuleContext(LambdaExpressionContext.class,0);
		}
		public LambdaContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterLambda(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitLambda(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitLambda(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ApplicationContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ApplicationTailContext applicationTail() {
			return getRuleContext(ApplicationTailContext.class,0);
		}
		public ApplicationContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterApplication(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitApplication(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitApplication(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_expression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Constant:
				{
				_localctx = new ConstantExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(37);
				constantExpression();
				}
				break;
			case FstructVariable:
				{
				_localctx = new VariableContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(38);
				match(FstructVariable);
				}
				break;
			case Individual:
				{
				_localctx = new IndividualContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(39);
				match(Individual);
				}
				break;
			case Predicate:
				{
				_localctx = new PredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(40);
				match(Predicate);
				}
				break;
			case Lparen:
				{
				_localctx = new ParenthesizedContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(41);
				parenthesizedExpression();
				}
				break;
			case Not:
				{
				_localctx = new NegatedContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(42);
				negation();
				}
				break;
			case Exists:
				{
				_localctx = new ExistsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(43);
				existsExpression();
				}
				break;
			case Forall:
				{
				_localctx = new ForallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(44);
				forallExpression();
				}
				break;
			case Lambda:
				{
				_localctx = new LambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(45);
				lambdaExpression();
				}
				break;
			case T__0:
				{
				_localctx = new InjectableContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(46);
				injectableExpression();
				}
				break;
			case Box:
				{
				_localctx = new BoxContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(47);
				match(Box);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(62);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(60);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
					case 1:
						{
						_localctx = new AndContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(50);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(51);
						match(And);
						setState(52);
						expression(11);
						}
						break;
					case 2:
						{
						_localctx = new OrContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(53);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(54);
						match(Or);
						setState(55);
						expression(10);
						}
						break;
					case 3:
						{
						_localctx = new ApplicationContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(56);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(57);
						applicationTail();
						}
						break;
					case 4:
						{
						_localctx = new RelationalContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(58);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(59);
						relationTail();
						}
						break;
					}
					} 
				}
				setState(64);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ConstantExpressionContext extends ParserRuleContext {
		public TerminalNode Constant() { return getToken(LogicTermsParser.Constant, 0); }
		public ConstantExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constantExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterConstantExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitConstantExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitConstantExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantExpressionContext constantExpression() throws RecognitionException {
		ConstantExpressionContext _localctx = new ConstantExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_constantExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			match(Constant);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BoolOpContext extends ParserRuleContext {
		public TerminalNode Iff() { return getToken(LogicTermsParser.Iff, 0); }
		public TerminalNode Equals() { return getToken(LogicTermsParser.Equals, 0); }
		public TerminalNode Implies() { return getToken(LogicTermsParser.Implies, 0); }
		public TerminalNode NotEquals() { return getToken(LogicTermsParser.NotEquals, 0); }
		public BoolOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterBoolOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitBoolOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitBoolOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BoolOpContext boolOp() throws RecognitionException {
		BoolOpContext _localctx = new BoolOpContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_boolOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Implies) | (1L << Iff) | (1L << NotEquals) | (1L << Equals))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationTailContext extends ParserRuleContext {
		public BoolOpContext boolOp() {
			return getRuleContext(BoolOpContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public RelationTailContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationTail; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterRelationTail(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitRelationTail(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitRelationTail(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationTailContext relationTail() throws RecognitionException {
		RelationTailContext _localctx = new RelationTailContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_relationTail);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			boolOp();
			setState(70);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParenthesizedExpressionContext extends ParserRuleContext {
		public TerminalNode Lparen() { return getToken(LogicTermsParser.Lparen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Rparen() { return getToken(LogicTermsParser.Rparen, 0); }
		public ParenthesizedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenthesizedExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterParenthesizedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitParenthesizedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitParenthesizedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParenthesizedExpressionContext parenthesizedExpression() throws RecognitionException {
		ParenthesizedExpressionContext _localctx = new ParenthesizedExpressionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_parenthesizedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			match(Lparen);
			setState(73);
			expression(0);
			setState(74);
			match(Rparen);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExistsExpressionContext extends ParserRuleContext {
		public TerminalNode Exists() { return getToken(LogicTermsParser.Exists, 0); }
		public TerminalNode Dot() { return getToken(LogicTermsParser.Dot, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<ArgumentContext> argument() {
			return getRuleContexts(ArgumentContext.class);
		}
		public ArgumentContext argument(int i) {
			return getRuleContext(ArgumentContext.class,i);
		}
		public ExistsExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_existsExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterExistsExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitExistsExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitExistsExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExistsExpressionContext existsExpression() throws RecognitionException {
		ExistsExpressionContext _localctx = new ExistsExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_existsExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			match(Exists);
			{
			setState(78); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(77);
				argument();
				}
				}
				setState(80); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Individual) | (1L << Predicate) | (1L << FstructVariable))) != 0) );
			}
			setState(82);
			match(Dot);
			setState(83);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForallExpressionContext extends ParserRuleContext {
		public TerminalNode Forall() { return getToken(LogicTermsParser.Forall, 0); }
		public TerminalNode Dot() { return getToken(LogicTermsParser.Dot, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<ArgumentContext> argument() {
			return getRuleContexts(ArgumentContext.class);
		}
		public ArgumentContext argument(int i) {
			return getRuleContext(ArgumentContext.class,i);
		}
		public ForallExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forallExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterForallExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitForallExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitForallExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForallExpressionContext forallExpression() throws RecognitionException {
		ForallExpressionContext _localctx = new ForallExpressionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_forallExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			match(Forall);
			{
			setState(87); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(86);
				argument();
				}
				}
				setState(89); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Individual) | (1L << Predicate) | (1L << FstructVariable))) != 0) );
			}
			setState(91);
			match(Dot);
			setState(92);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LambdaExpressionContext extends ParserRuleContext {
		public TerminalNode Lambda() { return getToken(LogicTermsParser.Lambda, 0); }
		public TerminalNode Dot() { return getToken(LogicTermsParser.Dot, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<LargContext> larg() {
			return getRuleContexts(LargContext.class);
		}
		public LargContext larg(int i) {
			return getRuleContext(LargContext.class,i);
		}
		public LambdaExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterLambdaExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitLambdaExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitLambdaExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaExpressionContext lambdaExpression() throws RecognitionException {
		LambdaExpressionContext _localctx = new LambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_lambdaExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			match(Lambda);
			{
			setState(96); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(95);
				larg();
				}
				}
				setState(98); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==Individual || _la==Predicate );
			}
			setState(100);
			match(Dot);
			setState(101);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InjectableExpressionContext extends ParserRuleContext {
		public TerminalNode Lparen() { return getToken(LogicTermsParser.Lparen, 0); }
		public TerminalNode FstructVariable() { return getToken(LogicTermsParser.FstructVariable, 0); }
		public TerminalNode Rparen() { return getToken(LogicTermsParser.Rparen, 0); }
		public List<TerminalNode> Comma() { return getTokens(LogicTermsParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(LogicTermsParser.Comma, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public InjectableExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_injectableExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterInjectableExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitInjectableExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitInjectableExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InjectableExpressionContext injectableExpression() throws RecognitionException {
		InjectableExpressionContext _localctx = new InjectableExpressionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_injectableExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			match(T__0);
			setState(104);
			match(Lparen);
			setState(105);
			match(FstructVariable);
			setState(110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(106);
				match(Comma);
				setState(107);
				expression(0);
				}
				}
				setState(112);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(113);
			match(Rparen);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ApplicationTailContext extends ParserRuleContext {
		public TerminalNode Lparen() { return getToken(LogicTermsParser.Lparen, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Rparen() { return getToken(LogicTermsParser.Rparen, 0); }
		public List<TerminalNode> Comma() { return getTokens(LogicTermsParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(LogicTermsParser.Comma, i);
		}
		public ApplicationTailContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_applicationTail; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterApplicationTail(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitApplicationTail(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitApplicationTail(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ApplicationTailContext applicationTail() throws RecognitionException {
		ApplicationTailContext _localctx = new ApplicationTailContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_applicationTail);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			match(Lparen);
			setState(116);
			expression(0);
			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(117);
				match(Comma);
				setState(118);
				expression(0);
				}
				}
				setState(123);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(124);
			match(Rparen);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NegationContext extends ParserRuleContext {
		public TerminalNode Not() { return getToken(LogicTermsParser.Not, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public NegationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_negation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterNegation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitNegation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitNegation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NegationContext negation() throws RecognitionException {
		NegationContext _localctx = new NegationContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_negation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			match(Not);
			setState(127);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArgumentContext extends ParserRuleContext {
		public TerminalNode Individual() { return getToken(LogicTermsParser.Individual, 0); }
		public TerminalNode Predicate() { return getToken(LogicTermsParser.Predicate, 0); }
		public TerminalNode FstructVariable() { return getToken(LogicTermsParser.FstructVariable, 0); }
		public ArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentContext argument() throws RecognitionException {
		ArgumentContext _localctx = new ArgumentContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_argument);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Individual) | (1L << Predicate) | (1L << FstructVariable))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LargContext extends ParserRuleContext {
		public TerminalNode Individual() { return getToken(LogicTermsParser.Individual, 0); }
		public TerminalNode Predicate() { return getToken(LogicTermsParser.Predicate, 0); }
		public LargContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_larg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).enterLarg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LogicTermsListener ) ((LogicTermsListener)listener).exitLarg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LogicTermsVisitor ) return ((LogicTermsVisitor<? extends T>)visitor).visitLarg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LargContext larg() throws RecognitionException {
		LargContext _localctx = new LargContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_larg);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			_la = _input.LA(1);
			if ( !(_la==Individual || _la==Predicate) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 10);
		case 1:
			return precpred(_ctx, 9);
		case 2:
			return precpred(_ctx, 11);
		case 3:
			return precpred(_ctx, 6);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\30\u0088\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\6\2!\n\2\r\2\16\2\""+
		"\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\63\n\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3?\n\3\f\3\16\3B\13\3\3\4\3\4"+
		"\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\b\3\b\6\bQ\n\b\r\b\16\bR\3\b\3"+
		"\b\3\b\3\t\3\t\6\tZ\n\t\r\t\16\t[\3\t\3\t\3\t\3\n\3\n\6\nc\n\n\r\n\16"+
		"\nd\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\7\13o\n\13\f\13\16\13r\13\13"+
		"\3\13\3\13\3\f\3\f\3\f\3\f\7\fz\n\f\f\f\16\f}\13\f\3\f\3\f\3\r\3\r\3\r"+
		"\3\16\3\16\3\17\3\17\3\17\2\3\4\20\2\4\6\b\n\f\16\20\22\24\26\30\32\34"+
		"\2\5\4\2\b\t\13\f\3\2\25\27\3\2\25\26\2\u008e\2 \3\2\2\2\4\62\3\2\2\2"+
		"\6C\3\2\2\2\bE\3\2\2\2\nG\3\2\2\2\fJ\3\2\2\2\16N\3\2\2\2\20W\3\2\2\2\22"+
		"`\3\2\2\2\24i\3\2\2\2\26u\3\2\2\2\30\u0080\3\2\2\2\32\u0083\3\2\2\2\34"+
		"\u0085\3\2\2\2\36!\7\4\2\2\37!\5\4\3\2 \36\3\2\2\2 \37\3\2\2\2!\"\3\2"+
		"\2\2\" \3\2\2\2\"#\3\2\2\2#$\3\2\2\2$%\7\2\2\3%\3\3\2\2\2&\'\b\3\1\2\'"+
		"\63\5\6\4\2(\63\7\27\2\2)\63\7\25\2\2*\63\7\26\2\2+\63\5\f\7\2,\63\5\30"+
		"\r\2-\63\5\16\b\2.\63\5\20\t\2/\63\5\22\n\2\60\63\5\24\13\2\61\63\7\24"+
		"\2\2\62&\3\2\2\2\62(\3\2\2\2\62)\3\2\2\2\62*\3\2\2\2\62+\3\2\2\2\62,\3"+
		"\2\2\2\62-\3\2\2\2\62.\3\2\2\2\62/\3\2\2\2\62\60\3\2\2\2\62\61\3\2\2\2"+
		"\63@\3\2\2\2\64\65\f\f\2\2\65\66\7\7\2\2\66?\5\4\3\r\678\f\13\2\289\7"+
		"\6\2\29?\5\4\3\f:;\f\r\2\2;?\5\26\f\2<=\f\b\2\2=?\5\n\6\2>\64\3\2\2\2"+
		">\67\3\2\2\2>:\3\2\2\2><\3\2\2\2?B\3\2\2\2@>\3\2\2\2@A\3\2\2\2A\5\3\2"+
		"\2\2B@\3\2\2\2CD\7\30\2\2D\7\3\2\2\2EF\t\2\2\2F\t\3\2\2\2GH\5\b\5\2HI"+
		"\5\4\3\2I\13\3\2\2\2JK\7\16\2\2KL\5\4\3\2LM\7\17\2\2M\r\3\2\2\2NP\7\22"+
		"\2\2OQ\5\32\16\2PO\3\2\2\2QR\3\2\2\2RP\3\2\2\2RS\3\2\2\2ST\3\2\2\2TU\7"+
		"\23\2\2UV\5\4\3\2V\17\3\2\2\2WY\7\21\2\2XZ\5\32\16\2YX\3\2\2\2Z[\3\2\2"+
		"\2[Y\3\2\2\2[\\\3\2\2\2\\]\3\2\2\2]^\7\23\2\2^_\5\4\3\2_\21\3\2\2\2`b"+
		"\7\20\2\2ac\5\34\17\2ba\3\2\2\2cd\3\2\2\2db\3\2\2\2de\3\2\2\2ef\3\2\2"+
		"\2fg\7\23\2\2gh\5\4\3\2h\23\3\2\2\2ij\7\3\2\2jk\7\16\2\2kp\7\27\2\2lm"+
		"\7\r\2\2mo\5\4\3\2nl\3\2\2\2or\3\2\2\2pn\3\2\2\2pq\3\2\2\2qs\3\2\2\2r"+
		"p\3\2\2\2st\7\17\2\2t\25\3\2\2\2uv\7\16\2\2v{\5\4\3\2wx\7\r\2\2xz\5\4"+
		"\3\2yw\3\2\2\2z}\3\2\2\2{y\3\2\2\2{|\3\2\2\2|~\3\2\2\2}{\3\2\2\2~\177"+
		"\7\17\2\2\177\27\3\2\2\2\u0080\u0081\7\n\2\2\u0081\u0082\5\4\3\2\u0082"+
		"\31\3\2\2\2\u0083\u0084\t\3\2\2\u0084\33\3\2\2\2\u0085\u0086\t\4\2\2\u0086"+
		"\35\3\2\2\2\f \"\62>@R[dp{";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}