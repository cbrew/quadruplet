// Generated from com/cbrew/fstruct/notation/FeatParser.g4 by ANTLR 4.7
package com.cbrew.fstruct.notation;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FeatParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LINE_COMMENT=1, WS=2, Equals=3, FstructVariable=4, Category=5, Fname=6, 
		Lparen=7, Rparen=8, Lsq=9, Rsq=10, Comma=11, Open=12, Arrow=13, Arrow2=14, 
		Colon=15, Pipe=16, Word=17, Close=18, Number=19, WS2=20, Or=21, And=22, 
		Implies=23, Iff=24, Not=25, NotEquals=26, SemEquals=27, SemComma=28, SemLparen=29, 
		SemRparen=30, Lambda=31, Forall=32, Exists=33, Dot=34, Box=35, Individual=36, 
		Predicate=37, FstructVariable2=38, Constant=39;
	public static final int
		RULE_cfg = 0, RULE_mcfgrule = 1, RULE_cfgrule = 2, RULE_cfgrhs = 3, RULE_mcfgrhs = 4, 
		RULE_rhspart = 5, RULE_mcfgrhspart = 6, RULE_linseq = 7, RULE_numseq = 8, 
		RULE_lexentry = 9, RULE_word = 10, RULE_featureMap = 11, RULE_mapping = 12, 
		RULE_fpair = 13, RULE_fvalue = 14, RULE_flist = 15, RULE_semantics = 16, 
		RULE_expression = 17, RULE_predicateExpression = 18, RULE_individualExpression = 19, 
		RULE_constantExpression = 20, RULE_boolOp = 21, RULE_relationTail = 22, 
		RULE_parenthesizedExpression = 23, RULE_existsExpression = 24, RULE_forallExpression = 25, 
		RULE_lambdaExpression = 26, RULE_applicationTail = 27, RULE_negation = 28, 
		RULE_argument = 29, RULE_larg = 30;
	public static final String[] ruleNames = {
		"cfg", "mcfgrule", "cfgrule", "cfgrhs", "mcfgrhs", "rhspart", "mcfgrhspart", 
		"linseq", "numseq", "lexentry", "word", "featureMap", "mapping", "fpair", 
		"fvalue", "flist", "semantics", "expression", "predicateExpression", "individualExpression", 
		"constantExpression", "boolOp", "relationTail", "parenthesizedExpression", 
		"existsExpression", "forallExpression", "lambdaExpression", "applicationTail", 
		"negation", "argument", "larg"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, "'='", null, null, null, null, null, "'['", "']'", null, 
		"'<'", "'->'", "'=>'", "':'", "'|'", null, "'>'", null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, "'.'", 
		"'\u2610'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LINE_COMMENT", "WS", "Equals", "FstructVariable", "Category", "Fname", 
		"Lparen", "Rparen", "Lsq", "Rsq", "Comma", "Open", "Arrow", "Arrow2", 
		"Colon", "Pipe", "Word", "Close", "Number", "WS2", "Or", "And", "Implies", 
		"Iff", "Not", "NotEquals", "SemEquals", "SemComma", "SemLparen", "SemRparen", 
		"Lambda", "Forall", "Exists", "Dot", "Box", "Individual", "Predicate", 
		"FstructVariable2", "Constant"
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
	public String getGrammarFileName() { return "FeatParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public FeatParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class CfgContext extends ParserRuleContext {
		public List<LexentryContext> lexentry() {
			return getRuleContexts(LexentryContext.class);
		}
		public LexentryContext lexentry(int i) {
			return getRuleContext(LexentryContext.class,i);
		}
		public List<CfgruleContext> cfgrule() {
			return getRuleContexts(CfgruleContext.class);
		}
		public CfgruleContext cfgrule(int i) {
			return getRuleContext(CfgruleContext.class,i);
		}
		public List<McfgruleContext> mcfgrule() {
			return getRuleContexts(McfgruleContext.class);
		}
		public McfgruleContext mcfgrule(int i) {
			return getRuleContext(McfgruleContext.class,i);
		}
		public CfgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cfg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterCfg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitCfg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitCfg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CfgContext cfg() throws RecognitionException {
		CfgContext _localctx = new CfgContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_cfg);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Category || _la==Word) {
				{
				setState(65);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(62);
					lexentry();
					}
					break;
				case 2:
					{
					setState(63);
					cfgrule();
					}
					break;
				case 3:
					{
					setState(64);
					mcfgrule();
					}
					break;
				}
				}
				setState(69);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class McfgruleContext extends ParserRuleContext {
		public FeatureMapContext featureMap() {
			return getRuleContext(FeatureMapContext.class,0);
		}
		public TerminalNode Arrow2() { return getToken(FeatParser.Arrow2, 0); }
		public McfgrhsContext mcfgrhs() {
			return getRuleContext(McfgrhsContext.class,0);
		}
		public McfgruleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mcfgrule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterMcfgrule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitMcfgrule(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitMcfgrule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final McfgruleContext mcfgrule() throws RecognitionException {
		McfgruleContext _localctx = new McfgruleContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_mcfgrule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			featureMap();
			setState(71);
			match(Arrow2);
			setState(72);
			mcfgrhs();
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

	public static class CfgruleContext extends ParserRuleContext {
		public FeatureMapContext featureMap() {
			return getRuleContext(FeatureMapContext.class,0);
		}
		public TerminalNode Arrow() { return getToken(FeatParser.Arrow, 0); }
		public CfgrhsContext cfgrhs() {
			return getRuleContext(CfgrhsContext.class,0);
		}
		public CfgruleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cfgrule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterCfgrule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitCfgrule(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitCfgrule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CfgruleContext cfgrule() throws RecognitionException {
		CfgruleContext _localctx = new CfgruleContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_cfgrule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			featureMap();
			setState(75);
			match(Arrow);
			setState(76);
			cfgrhs();
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

	public static class CfgrhsContext extends ParserRuleContext {
		public List<RhspartContext> rhspart() {
			return getRuleContexts(RhspartContext.class);
		}
		public RhspartContext rhspart(int i) {
			return getRuleContext(RhspartContext.class,i);
		}
		public List<TerminalNode> Pipe() { return getTokens(FeatParser.Pipe); }
		public TerminalNode Pipe(int i) {
			return getToken(FeatParser.Pipe, i);
		}
		public CfgrhsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cfgrhs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterCfgrhs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitCfgrhs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitCfgrhs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CfgrhsContext cfgrhs() throws RecognitionException {
		CfgrhsContext _localctx = new CfgrhsContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_cfgrhs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			rhspart();
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Pipe) {
				{
				{
				setState(79);
				match(Pipe);
				setState(80);
				rhspart();
				}
				}
				setState(85);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class McfgrhsContext extends ParserRuleContext {
		public List<McfgrhspartContext> mcfgrhspart() {
			return getRuleContexts(McfgrhspartContext.class);
		}
		public McfgrhspartContext mcfgrhspart(int i) {
			return getRuleContext(McfgrhspartContext.class,i);
		}
		public List<TerminalNode> Pipe() { return getTokens(FeatParser.Pipe); }
		public TerminalNode Pipe(int i) {
			return getToken(FeatParser.Pipe, i);
		}
		public McfgrhsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mcfgrhs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterMcfgrhs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitMcfgrhs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitMcfgrhs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final McfgrhsContext mcfgrhs() throws RecognitionException {
		McfgrhsContext _localctx = new McfgrhsContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_mcfgrhs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			mcfgrhspart();
			setState(91);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Pipe) {
				{
				{
				setState(87);
				match(Pipe);
				setState(88);
				mcfgrhspart();
				}
				}
				setState(93);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class RhspartContext extends ParserRuleContext {
		public List<FeatureMapContext> featureMap() {
			return getRuleContexts(FeatureMapContext.class);
		}
		public FeatureMapContext featureMap(int i) {
			return getRuleContext(FeatureMapContext.class,i);
		}
		public List<WordContext> word() {
			return getRuleContexts(WordContext.class);
		}
		public WordContext word(int i) {
			return getRuleContext(WordContext.class,i);
		}
		public RhspartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rhspart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterRhspart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitRhspart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitRhspart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RhspartContext rhspart() throws RecognitionException {
		RhspartContext _localctx = new RhspartContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_rhspart);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(96); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					setState(96);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case Category:
						{
						setState(94);
						featureMap();
						}
						break;
					case Word:
						{
						setState(95);
						word();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(98); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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

	public static class McfgrhspartContext extends ParserRuleContext {
		public TerminalNode Colon() { return getToken(FeatParser.Colon, 0); }
		public List<LinseqContext> linseq() {
			return getRuleContexts(LinseqContext.class);
		}
		public LinseqContext linseq(int i) {
			return getRuleContext(LinseqContext.class,i);
		}
		public List<FeatureMapContext> featureMap() {
			return getRuleContexts(FeatureMapContext.class);
		}
		public FeatureMapContext featureMap(int i) {
			return getRuleContext(FeatureMapContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(FeatParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FeatParser.Comma, i);
		}
		public McfgrhspartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mcfgrhspart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterMcfgrhspart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitMcfgrhspart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitMcfgrhspart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final McfgrhspartContext mcfgrhspart() throws RecognitionException {
		McfgrhspartContext _localctx = new McfgrhspartContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_mcfgrhspart);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(100);
				featureMap();
				}
				}
				setState(103); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==Category );
			setState(105);
			match(Colon);
			setState(106);
			linseq();
			setState(111);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(107);
				match(Comma);
				setState(108);
				linseq();
				}
				}
				setState(113);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class LinseqContext extends ParserRuleContext {
		public TerminalNode Open() { return getToken(FeatParser.Open, 0); }
		public TerminalNode Close() { return getToken(FeatParser.Close, 0); }
		public List<NumseqContext> numseq() {
			return getRuleContexts(NumseqContext.class);
		}
		public NumseqContext numseq(int i) {
			return getRuleContext(NumseqContext.class,i);
		}
		public LinseqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_linseq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterLinseq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitLinseq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitLinseq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LinseqContext linseq() throws RecognitionException {
		LinseqContext _localctx = new LinseqContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_linseq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(114);
			match(Open);
			setState(116); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(115);
				numseq();
				}
				}
				setState(118); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==SemLparen );
			setState(120);
			match(Close);
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

	public static class NumseqContext extends ParserRuleContext {
		public TerminalNode SemLparen() { return getToken(FeatParser.SemLparen, 0); }
		public TerminalNode SemRparen() { return getToken(FeatParser.SemRparen, 0); }
		public List<TerminalNode> Number() { return getTokens(FeatParser.Number); }
		public TerminalNode Number(int i) {
			return getToken(FeatParser.Number, i);
		}
		public NumseqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numseq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterNumseq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitNumseq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitNumseq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumseqContext numseq() throws RecognitionException {
		NumseqContext _localctx = new NumseqContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_numseq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(122);
			match(SemLparen);
			setState(124); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(123);
				match(Number);
				}
				}
				setState(126); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==Number );
			setState(128);
			match(SemRparen);
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

	public static class LexentryContext extends ParserRuleContext {
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public TerminalNode Colon() { return getToken(FeatParser.Colon, 0); }
		public List<FeatureMapContext> featureMap() {
			return getRuleContexts(FeatureMapContext.class);
		}
		public FeatureMapContext featureMap(int i) {
			return getRuleContext(FeatureMapContext.class,i);
		}
		public List<TerminalNode> Pipe() { return getTokens(FeatParser.Pipe); }
		public TerminalNode Pipe(int i) {
			return getToken(FeatParser.Pipe, i);
		}
		public LexentryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lexentry; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterLexentry(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitLexentry(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitLexentry(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LexentryContext lexentry() throws RecognitionException {
		LexentryContext _localctx = new LexentryContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_lexentry);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			word();
			setState(131);
			match(Colon);
			setState(132);
			featureMap();
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Pipe) {
				{
				{
				setState(133);
				match(Pipe);
				setState(134);
				featureMap();
				}
				}
				setState(139);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class WordContext extends ParserRuleContext {
		public TerminalNode Word() { return getToken(FeatParser.Word, 0); }
		public WordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WordContext word() throws RecognitionException {
		WordContext _localctx = new WordContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_word);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
			match(Word);
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

	public static class FeatureMapContext extends ParserRuleContext {
		public TerminalNode Category() { return getToken(FeatParser.Category, 0); }
		public TerminalNode Lsq() { return getToken(FeatParser.Lsq, 0); }
		public MappingContext mapping() {
			return getRuleContext(MappingContext.class,0);
		}
		public TerminalNode Rsq() { return getToken(FeatParser.Rsq, 0); }
		public FeatureMapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_featureMap; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterFeatureMap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitFeatureMap(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitFeatureMap(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FeatureMapContext featureMap() throws RecognitionException {
		FeatureMapContext _localctx = new FeatureMapContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_featureMap);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(142);
			match(Category);
			{
			setState(143);
			match(Lsq);
			setState(144);
			mapping();
			setState(145);
			match(Rsq);
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

	public static class MappingContext extends ParserRuleContext {
		public List<FpairContext> fpair() {
			return getRuleContexts(FpairContext.class);
		}
		public FpairContext fpair(int i) {
			return getRuleContext(FpairContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(FeatParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FeatParser.Comma, i);
		}
		public MappingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapping; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterMapping(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitMapping(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitMapping(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MappingContext mapping() throws RecognitionException {
		MappingContext _localctx = new MappingContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_mapping);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Fname) {
				{
				setState(147);
				fpair();
				}
			}

			setState(154);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(150);
				match(Comma);
				setState(151);
				fpair();
				}
				}
				setState(156);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class FpairContext extends ParserRuleContext {
		public TerminalNode Fname() { return getToken(FeatParser.Fname, 0); }
		public TerminalNode Equals() { return getToken(FeatParser.Equals, 0); }
		public FvalueContext fvalue() {
			return getRuleContext(FvalueContext.class,0);
		}
		public FpairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fpair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterFpair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitFpair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitFpair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FpairContext fpair() throws RecognitionException {
		FpairContext _localctx = new FpairContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_fpair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			match(Fname);
			setState(158);
			match(Equals);
			setState(159);
			fvalue();
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

	public static class FvalueContext extends ParserRuleContext {
		public TerminalNode Fname() { return getToken(FeatParser.Fname, 0); }
		public TerminalNode FstructVariable() { return getToken(FeatParser.FstructVariable, 0); }
		public FlistContext flist() {
			return getRuleContext(FlistContext.class,0);
		}
		public SemanticsContext semantics() {
			return getRuleContext(SemanticsContext.class,0);
		}
		public FvalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fvalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterFvalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitFvalue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitFvalue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FvalueContext fvalue() throws RecognitionException {
		FvalueContext _localctx = new FvalueContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_fvalue);
		try {
			setState(165);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Fname:
				enterOuterAlt(_localctx, 1);
				{
				setState(161);
				match(Fname);
				}
				break;
			case FstructVariable:
				enterOuterAlt(_localctx, 2);
				{
				setState(162);
				match(FstructVariable);
				}
				break;
			case Lsq:
				enterOuterAlt(_localctx, 3);
				{
				setState(163);
				flist();
				}
				break;
			case Open:
				enterOuterAlt(_localctx, 4);
				{
				setState(164);
				semantics();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class FlistContext extends ParserRuleContext {
		public TerminalNode Lsq() { return getToken(FeatParser.Lsq, 0); }
		public List<FvalueContext> fvalue() {
			return getRuleContexts(FvalueContext.class);
		}
		public FvalueContext fvalue(int i) {
			return getRuleContext(FvalueContext.class,i);
		}
		public TerminalNode Rsq() { return getToken(FeatParser.Rsq, 0); }
		public List<TerminalNode> Comma() { return getTokens(FeatParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FeatParser.Comma, i);
		}
		public FlistContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flist; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterFlist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitFlist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitFlist(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlistContext flist() throws RecognitionException {
		FlistContext _localctx = new FlistContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_flist);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			match(Lsq);
			setState(168);
			fvalue();
			setState(173);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(169);
				match(Comma);
				setState(170);
				fvalue();
				}
				}
				setState(175);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(176);
			match(Rsq);
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

	public static class SemanticsContext extends ParserRuleContext {
		public TerminalNode Open() { return getToken(FeatParser.Open, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Close() { return getToken(FeatParser.Close, 0); }
		public SemanticsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_semantics; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterSemantics(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitSemantics(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitSemantics(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SemanticsContext semantics() throws RecognitionException {
		SemanticsContext _localctx = new SemanticsContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_semantics);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			match(Open);
			setState(179);
			expression(0);
			setState(180);
			match(Close);
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
	public static class VariableContext extends ExpressionContext {
		public TerminalNode FstructVariable2() { return getToken(FeatParser.FstructVariable2, 0); }
		public VariableContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitVariable(this);
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
		public TerminalNode Or() { return getToken(FeatParser.Or, 0); }
		public OrContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitOr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ConstantContext extends ExpressionContext {
		public ConstantExpressionContext constantExpression() {
			return getRuleContext(ConstantExpressionContext.class,0);
		}
		public ConstantContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitConstant(this);
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterRelational(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitRelational(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitRelational(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BoxContext extends ExpressionContext {
		public TerminalNode Box() { return getToken(FeatParser.Box, 0); }
		public BoxContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterBox(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitBox(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitBox(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PredicateContext extends ExpressionContext {
		public PredicateExpressionContext predicateExpression() {
			return getRuleContext(PredicateExpressionContext.class,0);
		}
		public PredicateContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitPredicate(this);
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterParenthesized(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitParenthesized(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitParenthesized(this);
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitExists(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class IndividualContext extends ExpressionContext {
		public IndividualExpressionContext individualExpression() {
			return getRuleContext(IndividualExpressionContext.class,0);
		}
		public IndividualContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterIndividual(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitIndividual(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitIndividual(this);
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
		public TerminalNode And() { return getToken(FeatParser.And, 0); }
		public AndContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitAnd(this);
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterNegated(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitNegated(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitNegated(this);
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterForall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitForall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitForall(this);
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterLambda(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitLambda(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitLambda(this);
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterApplication(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitApplication(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitApplication(this);
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
		int _startState = 34;
		enterRecursionRule(_localctx, 34, RULE_expression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Constant:
				{
				_localctx = new ConstantContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(183);
				constantExpression();
				}
				break;
			case FstructVariable2:
				{
				_localctx = new VariableContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(184);
				match(FstructVariable2);
				}
				break;
			case SemLparen:
				{
				_localctx = new ParenthesizedContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(185);
				parenthesizedExpression();
				}
				break;
			case Not:
				{
				_localctx = new NegatedContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(186);
				negation();
				}
				break;
			case Exists:
				{
				_localctx = new ExistsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(187);
				existsExpression();
				}
				break;
			case Forall:
				{
				_localctx = new ForallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(188);
				forallExpression();
				}
				break;
			case Lambda:
				{
				_localctx = new LambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(189);
				lambdaExpression();
				}
				break;
			case Box:
				{
				_localctx = new BoxContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(190);
				match(Box);
				}
				break;
			case Individual:
				{
				_localctx = new IndividualContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(191);
				individualExpression();
				}
				break;
			case Predicate:
				{
				_localctx = new PredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(192);
				predicateExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(207);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(205);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
					case 1:
						{
						_localctx = new AndContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(195);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(196);
						match(And);
						setState(197);
						expression(12);
						}
						break;
					case 2:
						{
						_localctx = new OrContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(198);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(199);
						match(Or);
						setState(200);
						expression(11);
						}
						break;
					case 3:
						{
						_localctx = new ApplicationContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(201);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(202);
						applicationTail();
						}
						break;
					case 4:
						{
						_localctx = new RelationalContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(203);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(204);
						relationTail();
						}
						break;
					}
					} 
				}
				setState(209);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
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

	public static class PredicateExpressionContext extends ParserRuleContext {
		public TerminalNode Predicate() { return getToken(FeatParser.Predicate, 0); }
		public PredicateExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicateExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterPredicateExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitPredicateExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitPredicateExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateExpressionContext predicateExpression() throws RecognitionException {
		PredicateExpressionContext _localctx = new PredicateExpressionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_predicateExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(210);
			match(Predicate);
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

	public static class IndividualExpressionContext extends ParserRuleContext {
		public TerminalNode Individual() { return getToken(FeatParser.Individual, 0); }
		public IndividualExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_individualExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterIndividualExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitIndividualExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitIndividualExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndividualExpressionContext individualExpression() throws RecognitionException {
		IndividualExpressionContext _localctx = new IndividualExpressionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_individualExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			match(Individual);
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

	public static class ConstantExpressionContext extends ParserRuleContext {
		public TerminalNode Constant() { return getToken(FeatParser.Constant, 0); }
		public ConstantExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constantExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterConstantExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitConstantExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitConstantExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantExpressionContext constantExpression() throws RecognitionException {
		ConstantExpressionContext _localctx = new ConstantExpressionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_constantExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
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
		public TerminalNode Iff() { return getToken(FeatParser.Iff, 0); }
		public TerminalNode SemEquals() { return getToken(FeatParser.SemEquals, 0); }
		public TerminalNode Implies() { return getToken(FeatParser.Implies, 0); }
		public TerminalNode NotEquals() { return getToken(FeatParser.NotEquals, 0); }
		public BoolOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterBoolOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitBoolOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitBoolOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BoolOpContext boolOp() throws RecognitionException {
		BoolOpContext _localctx = new BoolOpContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_boolOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(216);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Implies) | (1L << Iff) | (1L << NotEquals) | (1L << SemEquals))) != 0)) ) {
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterRelationTail(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitRelationTail(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitRelationTail(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationTailContext relationTail() throws RecognitionException {
		RelationTailContext _localctx = new RelationTailContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_relationTail);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			boolOp();
			setState(219);
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
		public TerminalNode SemLparen() { return getToken(FeatParser.SemLparen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SemRparen() { return getToken(FeatParser.SemRparen, 0); }
		public ParenthesizedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenthesizedExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterParenthesizedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitParenthesizedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitParenthesizedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParenthesizedExpressionContext parenthesizedExpression() throws RecognitionException {
		ParenthesizedExpressionContext _localctx = new ParenthesizedExpressionContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_parenthesizedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			match(SemLparen);
			setState(222);
			expression(0);
			setState(223);
			match(SemRparen);
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
		public TerminalNode Exists() { return getToken(FeatParser.Exists, 0); }
		public TerminalNode Dot() { return getToken(FeatParser.Dot, 0); }
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterExistsExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitExistsExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitExistsExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExistsExpressionContext existsExpression() throws RecognitionException {
		ExistsExpressionContext _localctx = new ExistsExpressionContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_existsExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(225);
			match(Exists);
			{
			setState(227); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(226);
				argument();
				}
				}
				setState(229); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FstructVariable) | (1L << Individual) | (1L << Predicate))) != 0) );
			}
			setState(231);
			match(Dot);
			setState(232);
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
		public TerminalNode Forall() { return getToken(FeatParser.Forall, 0); }
		public TerminalNode Dot() { return getToken(FeatParser.Dot, 0); }
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterForallExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitForallExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitForallExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForallExpressionContext forallExpression() throws RecognitionException {
		ForallExpressionContext _localctx = new ForallExpressionContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_forallExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			match(Forall);
			{
			setState(236); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(235);
				argument();
				}
				}
				setState(238); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FstructVariable) | (1L << Individual) | (1L << Predicate))) != 0) );
			}
			setState(240);
			match(Dot);
			setState(241);
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
		public TerminalNode Lambda() { return getToken(FeatParser.Lambda, 0); }
		public TerminalNode Dot() { return getToken(FeatParser.Dot, 0); }
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
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterLambdaExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitLambdaExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitLambdaExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaExpressionContext lambdaExpression() throws RecognitionException {
		LambdaExpressionContext _localctx = new LambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_lambdaExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(243);
			match(Lambda);
			{
			setState(245); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(244);
				larg();
				}
				}
				setState(247); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==Individual || _la==Predicate );
			}
			setState(249);
			match(Dot);
			setState(250);
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

	public static class ApplicationTailContext extends ParserRuleContext {
		public TerminalNode SemLparen() { return getToken(FeatParser.SemLparen, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode SemRparen() { return getToken(FeatParser.SemRparen, 0); }
		public List<TerminalNode> SemComma() { return getTokens(FeatParser.SemComma); }
		public TerminalNode SemComma(int i) {
			return getToken(FeatParser.SemComma, i);
		}
		public ApplicationTailContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_applicationTail; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterApplicationTail(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitApplicationTail(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitApplicationTail(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ApplicationTailContext applicationTail() throws RecognitionException {
		ApplicationTailContext _localctx = new ApplicationTailContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_applicationTail);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(252);
			match(SemLparen);
			setState(253);
			expression(0);
			setState(258);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SemComma) {
				{
				{
				setState(254);
				match(SemComma);
				setState(255);
				expression(0);
				}
				}
				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(261);
			match(SemRparen);
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
		public TerminalNode Not() { return getToken(FeatParser.Not, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public NegationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_negation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterNegation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitNegation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitNegation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NegationContext negation() throws RecognitionException {
		NegationContext _localctx = new NegationContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_negation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263);
			match(Not);
			setState(264);
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
		public TerminalNode Individual() { return getToken(FeatParser.Individual, 0); }
		public TerminalNode Predicate() { return getToken(FeatParser.Predicate, 0); }
		public TerminalNode FstructVariable() { return getToken(FeatParser.FstructVariable, 0); }
		public ArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentContext argument() throws RecognitionException {
		ArgumentContext _localctx = new ArgumentContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_argument);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FstructVariable) | (1L << Individual) | (1L << Predicate))) != 0)) ) {
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
		public TerminalNode Individual() { return getToken(FeatParser.Individual, 0); }
		public TerminalNode Predicate() { return getToken(FeatParser.Predicate, 0); }
		public LargContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_larg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).enterLarg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatParserListener ) ((FeatParserListener)listener).exitLarg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatParserVisitor ) return ((FeatParserVisitor<? extends T>)visitor).visitLarg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LargContext larg() throws RecognitionException {
		LargContext _localctx = new LargContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_larg);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(268);
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
		case 17:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 11);
		case 1:
			return precpred(_ctx, 10);
		case 2:
			return precpred(_ctx, 12);
		case 3:
			return precpred(_ctx, 7);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3)\u0111\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \3\2"+
		"\3\2\3\2\7\2D\n\2\f\2\16\2G\13\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3"+
		"\5\3\5\7\5T\n\5\f\5\16\5W\13\5\3\6\3\6\3\6\7\6\\\n\6\f\6\16\6_\13\6\3"+
		"\7\3\7\6\7c\n\7\r\7\16\7d\3\b\6\bh\n\b\r\b\16\bi\3\b\3\b\3\b\3\b\7\bp"+
		"\n\b\f\b\16\bs\13\b\3\t\3\t\6\tw\n\t\r\t\16\tx\3\t\3\t\3\n\3\n\6\n\177"+
		"\n\n\r\n\16\n\u0080\3\n\3\n\3\13\3\13\3\13\3\13\3\13\7\13\u008a\n\13\f"+
		"\13\16\13\u008d\13\13\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\16\5\16\u0097\n\16"+
		"\3\16\3\16\7\16\u009b\n\16\f\16\16\16\u009e\13\16\3\17\3\17\3\17\3\17"+
		"\3\20\3\20\3\20\3\20\5\20\u00a8\n\20\3\21\3\21\3\21\3\21\7\21\u00ae\n"+
		"\21\f\21\16\21\u00b1\13\21\3\21\3\21\3\22\3\22\3\22\3\22\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\5\23\u00c4\n\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\7\23\u00d0\n\23\f\23\16\23\u00d3\13"+
		"\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\30\3\31\3\31\3"+
		"\31\3\31\3\32\3\32\6\32\u00e6\n\32\r\32\16\32\u00e7\3\32\3\32\3\32\3\33"+
		"\3\33\6\33\u00ef\n\33\r\33\16\33\u00f0\3\33\3\33\3\33\3\34\3\34\6\34\u00f8"+
		"\n\34\r\34\16\34\u00f9\3\34\3\34\3\34\3\35\3\35\3\35\3\35\7\35\u0103\n"+
		"\35\f\35\16\35\u0106\13\35\3\35\3\35\3\36\3\36\3\36\3\37\3\37\3 \3 \3"+
		" \2\3$!\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:"+
		"<>\2\5\4\2\31\32\34\35\4\2\6\6&\'\3\2&\'\2\u0114\2E\3\2\2\2\4H\3\2\2\2"+
		"\6L\3\2\2\2\bP\3\2\2\2\nX\3\2\2\2\fb\3\2\2\2\16g\3\2\2\2\20t\3\2\2\2\22"+
		"|\3\2\2\2\24\u0084\3\2\2\2\26\u008e\3\2\2\2\30\u0090\3\2\2\2\32\u0096"+
		"\3\2\2\2\34\u009f\3\2\2\2\36\u00a7\3\2\2\2 \u00a9\3\2\2\2\"\u00b4\3\2"+
		"\2\2$\u00c3\3\2\2\2&\u00d4\3\2\2\2(\u00d6\3\2\2\2*\u00d8\3\2\2\2,\u00da"+
		"\3\2\2\2.\u00dc\3\2\2\2\60\u00df\3\2\2\2\62\u00e3\3\2\2\2\64\u00ec\3\2"+
		"\2\2\66\u00f5\3\2\2\28\u00fe\3\2\2\2:\u0109\3\2\2\2<\u010c\3\2\2\2>\u010e"+
		"\3\2\2\2@D\5\24\13\2AD\5\6\4\2BD\5\4\3\2C@\3\2\2\2CA\3\2\2\2CB\3\2\2\2"+
		"DG\3\2\2\2EC\3\2\2\2EF\3\2\2\2F\3\3\2\2\2GE\3\2\2\2HI\5\30\r\2IJ\7\20"+
		"\2\2JK\5\n\6\2K\5\3\2\2\2LM\5\30\r\2MN\7\17\2\2NO\5\b\5\2O\7\3\2\2\2P"+
		"U\5\f\7\2QR\7\22\2\2RT\5\f\7\2SQ\3\2\2\2TW\3\2\2\2US\3\2\2\2UV\3\2\2\2"+
		"V\t\3\2\2\2WU\3\2\2\2X]\5\16\b\2YZ\7\22\2\2Z\\\5\16\b\2[Y\3\2\2\2\\_\3"+
		"\2\2\2][\3\2\2\2]^\3\2\2\2^\13\3\2\2\2_]\3\2\2\2`c\5\30\r\2ac\5\26\f\2"+
		"b`\3\2\2\2ba\3\2\2\2cd\3\2\2\2db\3\2\2\2de\3\2\2\2e\r\3\2\2\2fh\5\30\r"+
		"\2gf\3\2\2\2hi\3\2\2\2ig\3\2\2\2ij\3\2\2\2jk\3\2\2\2kl\7\21\2\2lq\5\20"+
		"\t\2mn\7\r\2\2np\5\20\t\2om\3\2\2\2ps\3\2\2\2qo\3\2\2\2qr\3\2\2\2r\17"+
		"\3\2\2\2sq\3\2\2\2tv\7\16\2\2uw\5\22\n\2vu\3\2\2\2wx\3\2\2\2xv\3\2\2\2"+
		"xy\3\2\2\2yz\3\2\2\2z{\7\24\2\2{\21\3\2\2\2|~\7\37\2\2}\177\7\25\2\2~"+
		"}\3\2\2\2\177\u0080\3\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2\2\u0081\u0082"+
		"\3\2\2\2\u0082\u0083\7 \2\2\u0083\23\3\2\2\2\u0084\u0085\5\26\f\2\u0085"+
		"\u0086\7\21\2\2\u0086\u008b\5\30\r\2\u0087\u0088\7\22\2\2\u0088\u008a"+
		"\5\30\r\2\u0089\u0087\3\2\2\2\u008a\u008d\3\2\2\2\u008b\u0089\3\2\2\2"+
		"\u008b\u008c\3\2\2\2\u008c\25\3\2\2\2\u008d\u008b\3\2\2\2\u008e\u008f"+
		"\7\23\2\2\u008f\27\3\2\2\2\u0090\u0091\7\7\2\2\u0091\u0092\7\13\2\2\u0092"+
		"\u0093\5\32\16\2\u0093\u0094\7\f\2\2\u0094\31\3\2\2\2\u0095\u0097\5\34"+
		"\17\2\u0096\u0095\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u009c\3\2\2\2\u0098"+
		"\u0099\7\r\2\2\u0099\u009b\5\34\17\2\u009a\u0098\3\2\2\2\u009b\u009e\3"+
		"\2\2\2\u009c\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d\33\3\2\2\2\u009e"+
		"\u009c\3\2\2\2\u009f\u00a0\7\b\2\2\u00a0\u00a1\7\5\2\2\u00a1\u00a2\5\36"+
		"\20\2\u00a2\35\3\2\2\2\u00a3\u00a8\7\b\2\2\u00a4\u00a8\7\6\2\2\u00a5\u00a8"+
		"\5 \21\2\u00a6\u00a8\5\"\22\2\u00a7\u00a3\3\2\2\2\u00a7\u00a4\3\2\2\2"+
		"\u00a7\u00a5\3\2\2\2\u00a7\u00a6\3\2\2\2\u00a8\37\3\2\2\2\u00a9\u00aa"+
		"\7\13\2\2\u00aa\u00af\5\36\20\2\u00ab\u00ac\7\r\2\2\u00ac\u00ae\5\36\20"+
		"\2\u00ad\u00ab\3\2\2\2\u00ae\u00b1\3\2\2\2\u00af\u00ad\3\2\2\2\u00af\u00b0"+
		"\3\2\2\2\u00b0\u00b2\3\2\2\2\u00b1\u00af\3\2\2\2\u00b2\u00b3\7\f\2\2\u00b3"+
		"!\3\2\2\2\u00b4\u00b5\7\16\2\2\u00b5\u00b6\5$\23\2\u00b6\u00b7\7\24\2"+
		"\2\u00b7#\3\2\2\2\u00b8\u00b9\b\23\1\2\u00b9\u00c4\5*\26\2\u00ba\u00c4"+
		"\7(\2\2\u00bb\u00c4\5\60\31\2\u00bc\u00c4\5:\36\2\u00bd\u00c4\5\62\32"+
		"\2\u00be\u00c4\5\64\33\2\u00bf\u00c4\5\66\34\2\u00c0\u00c4\7%\2\2\u00c1"+
		"\u00c4\5(\25\2\u00c2\u00c4\5&\24\2\u00c3\u00b8\3\2\2\2\u00c3\u00ba\3\2"+
		"\2\2\u00c3\u00bb\3\2\2\2\u00c3\u00bc\3\2\2\2\u00c3\u00bd\3\2\2\2\u00c3"+
		"\u00be\3\2\2\2\u00c3\u00bf\3\2\2\2\u00c3\u00c0\3\2\2\2\u00c3\u00c1\3\2"+
		"\2\2\u00c3\u00c2\3\2\2\2\u00c4\u00d1\3\2\2\2\u00c5\u00c6\f\r\2\2\u00c6"+
		"\u00c7\7\30\2\2\u00c7\u00d0\5$\23\16\u00c8\u00c9\f\f\2\2\u00c9\u00ca\7"+
		"\27\2\2\u00ca\u00d0\5$\23\r\u00cb\u00cc\f\16\2\2\u00cc\u00d0\58\35\2\u00cd"+
		"\u00ce\f\t\2\2\u00ce\u00d0\5.\30\2\u00cf\u00c5\3\2\2\2\u00cf\u00c8\3\2"+
		"\2\2\u00cf\u00cb\3\2\2\2\u00cf\u00cd\3\2\2\2\u00d0\u00d3\3\2\2\2\u00d1"+
		"\u00cf\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d2%\3\2\2\2\u00d3\u00d1\3\2\2\2"+
		"\u00d4\u00d5\7\'\2\2\u00d5\'\3\2\2\2\u00d6\u00d7\7&\2\2\u00d7)\3\2\2\2"+
		"\u00d8\u00d9\7)\2\2\u00d9+\3\2\2\2\u00da\u00db\t\2\2\2\u00db-\3\2\2\2"+
		"\u00dc\u00dd\5,\27\2\u00dd\u00de\5$\23\2\u00de/\3\2\2\2\u00df\u00e0\7"+
		"\37\2\2\u00e0\u00e1\5$\23\2\u00e1\u00e2\7 \2\2\u00e2\61\3\2\2\2\u00e3"+
		"\u00e5\7#\2\2\u00e4\u00e6\5<\37\2\u00e5\u00e4\3\2\2\2\u00e6\u00e7\3\2"+
		"\2\2\u00e7\u00e5\3\2\2\2\u00e7\u00e8\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e9"+
		"\u00ea\7$\2\2\u00ea\u00eb\5$\23\2\u00eb\63\3\2\2\2\u00ec\u00ee\7\"\2\2"+
		"\u00ed\u00ef\5<\37\2\u00ee\u00ed\3\2\2\2\u00ef\u00f0\3\2\2\2\u00f0\u00ee"+
		"\3\2\2\2\u00f0\u00f1\3\2\2\2\u00f1\u00f2\3\2\2\2\u00f2\u00f3\7$\2\2\u00f3"+
		"\u00f4\5$\23\2\u00f4\65\3\2\2\2\u00f5\u00f7\7!\2\2\u00f6\u00f8\5> \2\u00f7"+
		"\u00f6\3\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\u00f7\3\2\2\2\u00f9\u00fa\3\2"+
		"\2\2\u00fa\u00fb\3\2\2\2\u00fb\u00fc\7$\2\2\u00fc\u00fd\5$\23\2\u00fd"+
		"\67\3\2\2\2\u00fe\u00ff\7\37\2\2\u00ff\u0104\5$\23\2\u0100\u0101\7\36"+
		"\2\2\u0101\u0103\5$\23\2\u0102\u0100\3\2\2\2\u0103\u0106\3\2\2\2\u0104"+
		"\u0102\3\2\2\2\u0104\u0105\3\2\2\2\u0105\u0107\3\2\2\2\u0106\u0104\3\2"+
		"\2\2\u0107\u0108\7 \2\2\u01089\3\2\2\2\u0109\u010a\7\33\2\2\u010a\u010b"+
		"\5$\23\2\u010b;\3\2\2\2\u010c\u010d\t\3\2\2\u010d=\3\2\2\2\u010e\u010f"+
		"\t\4\2\2\u010f?\3\2\2\2\30CEU]bdiqx\u0080\u008b\u0096\u009c\u00a7\u00af"+
		"\u00c3\u00cf\u00d1\u00e7\u00f0\u00f9\u0104";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}