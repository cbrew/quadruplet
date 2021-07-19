// Generated from com/cbrew/fstruct/notation/FeatureTerms.g4 by ANTLR 4.7
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
public class FeatureTermsParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LINE_COMMENT=1, WS=2, Equals=3, FstructVariable=4, Category=5, Fname=6, 
		Lparen=7, Rparen=8, Lsq=9, Rsq=10, Comma=11, Sem=12, Arrow=13, Arrow2=14, 
		Colon=15, Pipe=16, Word=17, Number=18;
	public static final int
		RULE_cfg = 0, RULE_mcfgrule = 1, RULE_cfgrule = 2, RULE_cfgrhs = 3, RULE_mcfgrhs = 4, 
		RULE_rhspart = 5, RULE_mcfgrhspart = 6, RULE_linseq = 7, RULE_lexentry = 8, 
		RULE_word = 9, RULE_expression = 10, RULE_categoryExpression = 11, RULE_mapping = 12, 
		RULE_fpair = 13, RULE_fvalue = 14, RULE_flist = 15, RULE_semantics = 16;
	public static final String[] ruleNames = {
		"cfg", "mcfgrule", "cfgrule", "cfgrhs", "mcfgrhs", "rhspart", "mcfgrhspart", 
		"linseq", "lexentry", "word", "expression", "categoryExpression", "mapping", 
		"fpair", "fvalue", "flist", "semantics"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, "'='", null, null, null, "'('", "')'", "'['", "']'", 
		"','", null, "'->'", "'=>'", "':'", "'|'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LINE_COMMENT", "WS", "Equals", "FstructVariable", "Category", "Fname", 
		"Lparen", "Rparen", "Lsq", "Rsq", "Comma", "Sem", "Arrow", "Arrow2", "Colon", 
		"Pipe", "Word", "Number"
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
	public String getGrammarFileName() { return "FeatureTerms.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public FeatureTermsParser(TokenStream input) {
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
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterCfg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitCfg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitCfg(this);
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
			setState(39);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Category || _la==Word) {
				{
				setState(37);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(34);
					lexentry();
					}
					break;
				case 2:
					{
					setState(35);
					cfgrule();
					}
					break;
				case 3:
					{
					setState(36);
					mcfgrule();
					}
					break;
				}
				}
				setState(41);
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
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Arrow2() { return getToken(FeatureTermsParser.Arrow2, 0); }
		public McfgrhsContext mcfgrhs() {
			return getRuleContext(McfgrhsContext.class,0);
		}
		public McfgruleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mcfgrule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterMcfgrule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitMcfgrule(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitMcfgrule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final McfgruleContext mcfgrule() throws RecognitionException {
		McfgruleContext _localctx = new McfgruleContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_mcfgrule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(42);
			expression();
			setState(43);
			match(Arrow2);
			setState(44);
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
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Arrow() { return getToken(FeatureTermsParser.Arrow, 0); }
		public CfgrhsContext cfgrhs() {
			return getRuleContext(CfgrhsContext.class,0);
		}
		public CfgruleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cfgrule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterCfgrule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitCfgrule(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitCfgrule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CfgruleContext cfgrule() throws RecognitionException {
		CfgruleContext _localctx = new CfgruleContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_cfgrule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			expression();
			setState(47);
			match(Arrow);
			setState(48);
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
		public List<TerminalNode> Pipe() { return getTokens(FeatureTermsParser.Pipe); }
		public TerminalNode Pipe(int i) {
			return getToken(FeatureTermsParser.Pipe, i);
		}
		public CfgrhsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cfgrhs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterCfgrhs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitCfgrhs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitCfgrhs(this);
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
			setState(50);
			rhspart();
			setState(55);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Pipe) {
				{
				{
				setState(51);
				match(Pipe);
				setState(52);
				rhspart();
				}
				}
				setState(57);
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
		public List<TerminalNode> Pipe() { return getTokens(FeatureTermsParser.Pipe); }
		public TerminalNode Pipe(int i) {
			return getToken(FeatureTermsParser.Pipe, i);
		}
		public McfgrhsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mcfgrhs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterMcfgrhs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitMcfgrhs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitMcfgrhs(this);
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
			setState(58);
			mcfgrhspart();
			setState(63);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Pipe) {
				{
				{
				setState(59);
				match(Pipe);
				setState(60);
				mcfgrhspart();
				}
				}
				setState(65);
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
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public RhspartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rhspart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterRhspart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitRhspart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitRhspart(this);
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
			setState(67); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(66);
					expression();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(69); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
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
		public TerminalNode Colon() { return getToken(FeatureTermsParser.Colon, 0); }
		public LinseqContext linseq() {
			return getRuleContext(LinseqContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public McfgrhspartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mcfgrhspart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterMcfgrhspart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitMcfgrhspart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitMcfgrhspart(this);
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
			setState(72); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(71);
				expression();
				}
				}
				setState(74); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==Category );
			setState(76);
			match(Colon);
			setState(77);
			linseq();
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
		public List<TerminalNode> Sem() { return getTokens(FeatureTermsParser.Sem); }
		public TerminalNode Sem(int i) {
			return getToken(FeatureTermsParser.Sem, i);
		}
		public List<TerminalNode> Comma() { return getTokens(FeatureTermsParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FeatureTermsParser.Comma, i);
		}
		public LinseqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_linseq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterLinseq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitLinseq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitLinseq(this);
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
			setState(79);
			match(Sem);
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(80);
				match(Comma);
				setState(81);
				match(Sem);
				}
				}
				setState(86);
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

	public static class LexentryContext extends ParserRuleContext {
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public TerminalNode Colon() { return getToken(FeatureTermsParser.Colon, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> Pipe() { return getTokens(FeatureTermsParser.Pipe); }
		public TerminalNode Pipe(int i) {
			return getToken(FeatureTermsParser.Pipe, i);
		}
		public LexentryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lexentry; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterLexentry(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitLexentry(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitLexentry(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LexentryContext lexentry() throws RecognitionException {
		LexentryContext _localctx = new LexentryContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_lexentry);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			word();
			setState(88);
			match(Colon);
			setState(89);
			expression();
			setState(94);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Pipe) {
				{
				{
				setState(90);
				match(Pipe);
				setState(91);
				expression();
				}
				}
				setState(96);
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
		public TerminalNode Word() { return getToken(FeatureTermsParser.Word, 0); }
		public WordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WordContext word() throws RecognitionException {
		WordContext _localctx = new WordContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_word);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
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

	public static class ExpressionContext extends ParserRuleContext {
		public CategoryExpressionContext categoryExpression() {
			return getRuleContext(CategoryExpressionContext.class,0);
		}
		public TerminalNode Lsq() { return getToken(FeatureTermsParser.Lsq, 0); }
		public MappingContext mapping() {
			return getRuleContext(MappingContext.class,0);
		}
		public TerminalNode Rsq() { return getToken(FeatureTermsParser.Rsq, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_expression);
		try {
			setState(105);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(99);
				categoryExpression();
				{
				setState(100);
				match(Lsq);
				setState(101);
				mapping();
				setState(102);
				match(Rsq);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(104);
				categoryExpression();
				}
				break;
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

	public static class CategoryExpressionContext extends ParserRuleContext {
		public TerminalNode Category() { return getToken(FeatureTermsParser.Category, 0); }
		public CategoryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_categoryExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterCategoryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitCategoryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitCategoryExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CategoryExpressionContext categoryExpression() throws RecognitionException {
		CategoryExpressionContext _localctx = new CategoryExpressionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_categoryExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			match(Category);
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
		public List<TerminalNode> Comma() { return getTokens(FeatureTermsParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FeatureTermsParser.Comma, i);
		}
		public MappingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapping; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterMapping(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitMapping(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitMapping(this);
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
			setState(110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Fname) {
				{
				setState(109);
				fpair();
				}
			}

			setState(116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(112);
				match(Comma);
				setState(113);
				fpair();
				}
				}
				setState(118);
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
		public TerminalNode Fname() { return getToken(FeatureTermsParser.Fname, 0); }
		public TerminalNode Equals() { return getToken(FeatureTermsParser.Equals, 0); }
		public FvalueContext fvalue() {
			return getRuleContext(FvalueContext.class,0);
		}
		public FpairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fpair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterFpair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitFpair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitFpair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FpairContext fpair() throws RecognitionException {
		FpairContext _localctx = new FpairContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_fpair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			match(Fname);
			setState(120);
			match(Equals);
			setState(121);
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
		public TerminalNode Fname() { return getToken(FeatureTermsParser.Fname, 0); }
		public TerminalNode FstructVariable() { return getToken(FeatureTermsParser.FstructVariable, 0); }
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
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterFvalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitFvalue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitFvalue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FvalueContext fvalue() throws RecognitionException {
		FvalueContext _localctx = new FvalueContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_fvalue);
		try {
			setState(127);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Fname:
				enterOuterAlt(_localctx, 1);
				{
				setState(123);
				match(Fname);
				}
				break;
			case FstructVariable:
				enterOuterAlt(_localctx, 2);
				{
				setState(124);
				match(FstructVariable);
				}
				break;
			case Lsq:
				enterOuterAlt(_localctx, 3);
				{
				setState(125);
				flist();
				}
				break;
			case Sem:
				enterOuterAlt(_localctx, 4);
				{
				setState(126);
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
		public TerminalNode Lsq() { return getToken(FeatureTermsParser.Lsq, 0); }
		public List<FvalueContext> fvalue() {
			return getRuleContexts(FvalueContext.class);
		}
		public FvalueContext fvalue(int i) {
			return getRuleContext(FvalueContext.class,i);
		}
		public TerminalNode Rsq() { return getToken(FeatureTermsParser.Rsq, 0); }
		public List<TerminalNode> Comma() { return getTokens(FeatureTermsParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FeatureTermsParser.Comma, i);
		}
		public FlistContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flist; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterFlist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitFlist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitFlist(this);
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
			setState(129);
			match(Lsq);
			setState(130);
			fvalue();
			setState(135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(131);
				match(Comma);
				setState(132);
				fvalue();
				}
				}
				setState(137);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(138);
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
		public TerminalNode Sem() { return getToken(FeatureTermsParser.Sem, 0); }
		public SemanticsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_semantics; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).enterSemantics(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureTermsListener ) ((FeatureTermsListener)listener).exitSemantics(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureTermsVisitor ) return ((FeatureTermsVisitor<? extends T>)visitor).visitSemantics(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SemanticsContext semantics() throws RecognitionException {
		SemanticsContext _localctx = new SemanticsContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_semantics);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
			match(Sem);
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\24\u0091\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\2\7\2(\n\2\f\2\16\2+\13\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3"+
		"\5\3\5\3\5\7\58\n\5\f\5\16\5;\13\5\3\6\3\6\3\6\7\6@\n\6\f\6\16\6C\13\6"+
		"\3\7\6\7F\n\7\r\7\16\7G\3\b\6\bK\n\b\r\b\16\bL\3\b\3\b\3\b\3\t\3\t\3\t"+
		"\7\tU\n\t\f\t\16\tX\13\t\3\n\3\n\3\n\3\n\3\n\7\n_\n\n\f\n\16\nb\13\n\3"+
		"\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\5\fl\n\f\3\r\3\r\3\16\5\16q\n\16\3\16"+
		"\3\16\7\16u\n\16\f\16\16\16x\13\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20"+
		"\3\20\5\20\u0082\n\20\3\21\3\21\3\21\3\21\7\21\u0088\n\21\f\21\16\21\u008b"+
		"\13\21\3\21\3\21\3\22\3\22\3\22\2\2\23\2\4\6\b\n\f\16\20\22\24\26\30\32"+
		"\34\36 \"\2\2\2\u008f\2)\3\2\2\2\4,\3\2\2\2\6\60\3\2\2\2\b\64\3\2\2\2"+
		"\n<\3\2\2\2\fE\3\2\2\2\16J\3\2\2\2\20Q\3\2\2\2\22Y\3\2\2\2\24c\3\2\2\2"+
		"\26k\3\2\2\2\30m\3\2\2\2\32p\3\2\2\2\34y\3\2\2\2\36\u0081\3\2\2\2 \u0083"+
		"\3\2\2\2\"\u008e\3\2\2\2$(\5\22\n\2%(\5\6\4\2&(\5\4\3\2\'$\3\2\2\2\'%"+
		"\3\2\2\2\'&\3\2\2\2(+\3\2\2\2)\'\3\2\2\2)*\3\2\2\2*\3\3\2\2\2+)\3\2\2"+
		"\2,-\5\26\f\2-.\7\20\2\2./\5\n\6\2/\5\3\2\2\2\60\61\5\26\f\2\61\62\7\17"+
		"\2\2\62\63\5\b\5\2\63\7\3\2\2\2\649\5\f\7\2\65\66\7\22\2\2\668\5\f\7\2"+
		"\67\65\3\2\2\28;\3\2\2\29\67\3\2\2\29:\3\2\2\2:\t\3\2\2\2;9\3\2\2\2<A"+
		"\5\16\b\2=>\7\22\2\2>@\5\16\b\2?=\3\2\2\2@C\3\2\2\2A?\3\2\2\2AB\3\2\2"+
		"\2B\13\3\2\2\2CA\3\2\2\2DF\5\26\f\2ED\3\2\2\2FG\3\2\2\2GE\3\2\2\2GH\3"+
		"\2\2\2H\r\3\2\2\2IK\5\26\f\2JI\3\2\2\2KL\3\2\2\2LJ\3\2\2\2LM\3\2\2\2M"+
		"N\3\2\2\2NO\7\21\2\2OP\5\20\t\2P\17\3\2\2\2QV\7\16\2\2RS\7\r\2\2SU\7\16"+
		"\2\2TR\3\2\2\2UX\3\2\2\2VT\3\2\2\2VW\3\2\2\2W\21\3\2\2\2XV\3\2\2\2YZ\5"+
		"\24\13\2Z[\7\21\2\2[`\5\26\f\2\\]\7\22\2\2]_\5\26\f\2^\\\3\2\2\2_b\3\2"+
		"\2\2`^\3\2\2\2`a\3\2\2\2a\23\3\2\2\2b`\3\2\2\2cd\7\23\2\2d\25\3\2\2\2"+
		"ef\5\30\r\2fg\7\13\2\2gh\5\32\16\2hi\7\f\2\2il\3\2\2\2jl\5\30\r\2ke\3"+
		"\2\2\2kj\3\2\2\2l\27\3\2\2\2mn\7\7\2\2n\31\3\2\2\2oq\5\34\17\2po\3\2\2"+
		"\2pq\3\2\2\2qv\3\2\2\2rs\7\r\2\2su\5\34\17\2tr\3\2\2\2ux\3\2\2\2vt\3\2"+
		"\2\2vw\3\2\2\2w\33\3\2\2\2xv\3\2\2\2yz\7\b\2\2z{\7\5\2\2{|\5\36\20\2|"+
		"\35\3\2\2\2}\u0082\7\b\2\2~\u0082\7\6\2\2\177\u0082\5 \21\2\u0080\u0082"+
		"\5\"\22\2\u0081}\3\2\2\2\u0081~\3\2\2\2\u0081\177\3\2\2\2\u0081\u0080"+
		"\3\2\2\2\u0082\37\3\2\2\2\u0083\u0084\7\13\2\2\u0084\u0089\5\36\20\2\u0085"+
		"\u0086\7\r\2\2\u0086\u0088\5\36\20\2\u0087\u0085\3\2\2\2\u0088\u008b\3"+
		"\2\2\2\u0089\u0087\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008c\3\2\2\2\u008b"+
		"\u0089\3\2\2\2\u008c\u008d\7\f\2\2\u008d!\3\2\2\2\u008e\u008f\7\16\2\2"+
		"\u008f#\3\2\2\2\17\')9AGLV`kpv\u0081\u0089";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}