// Generated from /Users/cbrew/Documents/GitHub/quadruplet/src/main/antlr4/com/cbrew/demo/demo.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class demoParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LINE_COMMENT=1, WS=2, Arrow=3, Pipe=4, Quoted=5, Equals=6, Category=7;
	public static final int
		RULE_cfgrule = 0, RULE_cfgrhs = 1, RULE_rhspart = 2;
	private static String[] makeRuleNames() {
		return new String[] {
			"cfgrule", "cfgrhs", "rhspart"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, "'->'", "'|'", null, "'='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LINE_COMMENT", "WS", "Arrow", "Pipe", "Quoted", "Equals", "Category"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
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
	public String getGrammarFileName() { return "demo.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public demoParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class CfgruleContext extends ParserRuleContext {
		public TerminalNode Category() { return getToken(demoParser.Category, 0); }
		public TerminalNode Arrow() { return getToken(demoParser.Arrow, 0); }
		public CfgrhsContext cfgrhs() {
			return getRuleContext(CfgrhsContext.class,0);
		}
		public CfgruleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cfgrule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof demoListener ) ((demoListener)listener).enterCfgrule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof demoListener ) ((demoListener)listener).exitCfgrule(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof demoVisitor ) return ((demoVisitor<? extends T>)visitor).visitCfgrule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CfgruleContext cfgrule() throws RecognitionException {
		CfgruleContext _localctx = new CfgruleContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_cfgrule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(6);
			match(Category);
			setState(7);
			match(Arrow);
			setState(8);
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
		public List<TerminalNode> Pipe() { return getTokens(demoParser.Pipe); }
		public TerminalNode Pipe(int i) {
			return getToken(demoParser.Pipe, i);
		}
		public CfgrhsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cfgrhs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof demoListener ) ((demoListener)listener).enterCfgrhs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof demoListener ) ((demoListener)listener).exitCfgrhs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof demoVisitor ) return ((demoVisitor<? extends T>)visitor).visitCfgrhs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CfgrhsContext cfgrhs() throws RecognitionException {
		CfgrhsContext _localctx = new CfgrhsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_cfgrhs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(10);
			rhspart();
			setState(15);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Pipe) {
				{
				{
				setState(11);
				match(Pipe);
				setState(12);
				rhspart();
				}
				}
				setState(17);
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
		public List<TerminalNode> Category() { return getTokens(demoParser.Category); }
		public TerminalNode Category(int i) {
			return getToken(demoParser.Category, i);
		}
		public List<TerminalNode> Quoted() { return getTokens(demoParser.Quoted); }
		public TerminalNode Quoted(int i) {
			return getToken(demoParser.Quoted, i);
		}
		public RhspartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rhspart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof demoListener ) ((demoListener)listener).enterRhspart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof demoListener ) ((demoListener)listener).exitRhspart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof demoVisitor ) return ((demoVisitor<? extends T>)visitor).visitRhspart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RhspartContext rhspart() throws RecognitionException {
		RhspartContext _localctx = new RhspartContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_rhspart);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Quoted || _la==Category) {
				{
				{
				setState(18);
				_la = _input.LA(1);
				if ( !(_la==Quoted || _la==Category) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(23);
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\t\33\4\2\t\2\4\3"+
		"\t\3\4\4\t\4\3\2\3\2\3\2\3\2\3\3\3\3\3\3\7\3\20\n\3\f\3\16\3\23\13\3\3"+
		"\4\7\4\26\n\4\f\4\16\4\31\13\4\3\4\2\2\5\2\4\6\2\3\4\2\7\7\t\t\2\31\2"+
		"\b\3\2\2\2\4\f\3\2\2\2\6\27\3\2\2\2\b\t\7\t\2\2\t\n\7\5\2\2\n\13\5\4\3"+
		"\2\13\3\3\2\2\2\f\21\5\6\4\2\r\16\7\6\2\2\16\20\5\6\4\2\17\r\3\2\2\2\20"+
		"\23\3\2\2\2\21\17\3\2\2\2\21\22\3\2\2\2\22\5\3\2\2\2\23\21\3\2\2\2\24"+
		"\26\t\2\2\2\25\24\3\2\2\2\26\31\3\2\2\2\27\25\3\2\2\2\27\30\3\2\2\2\30"+
		"\7\3\2\2\2\31\27\3\2\2\2\4\21\27";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}