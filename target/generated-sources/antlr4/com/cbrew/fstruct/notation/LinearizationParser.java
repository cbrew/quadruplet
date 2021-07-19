// Generated from com/cbrew/fstruct/notation/Linearization.g4 by ANTLR 4.7
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
public class LinearizationParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LINE_COMMENT=1, WS=2, Number=3, Lparen=4, Rparen=5, Comma=6;
	public static final int
		RULE_numbers = 0, RULE_numseq = 1, RULE_numseqs = 2;
	public static final String[] ruleNames = {
		"numbers", "numseq", "numseqs"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, "'('", "')'", "','"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LINE_COMMENT", "WS", "Number", "Lparen", "Rparen", "Comma"
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
	public String getGrammarFileName() { return "Linearization.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public LinearizationParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class NumbersContext extends ParserRuleContext {
		public List<TerminalNode> Number() { return getTokens(LinearizationParser.Number); }
		public TerminalNode Number(int i) {
			return getToken(LinearizationParser.Number, i);
		}
		public List<TerminalNode> Comma() { return getTokens(LinearizationParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(LinearizationParser.Comma, i);
		}
		public NumbersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numbers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinearizationListener ) ((LinearizationListener)listener).enterNumbers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinearizationListener ) ((LinearizationListener)listener).exitNumbers(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinearizationVisitor ) return ((LinearizationVisitor<? extends T>)visitor).visitNumbers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumbersContext numbers() throws RecognitionException {
		NumbersContext _localctx = new NumbersContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_numbers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(6);
			match(Number);
			setState(11);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(7);
				match(Comma);
				setState(8);
				match(Number);
				}
				}
				setState(13);
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

	public static class NumseqContext extends ParserRuleContext {
		public TerminalNode Lparen() { return getToken(LinearizationParser.Lparen, 0); }
		public NumbersContext numbers() {
			return getRuleContext(NumbersContext.class,0);
		}
		public TerminalNode Rparen() { return getToken(LinearizationParser.Rparen, 0); }
		public NumseqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numseq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinearizationListener ) ((LinearizationListener)listener).enterNumseq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinearizationListener ) ((LinearizationListener)listener).exitNumseq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinearizationVisitor ) return ((LinearizationVisitor<? extends T>)visitor).visitNumseq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumseqContext numseq() throws RecognitionException {
		NumseqContext _localctx = new NumseqContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_numseq);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			match(Lparen);
			setState(15);
			numbers();
			setState(16);
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

	public static class NumseqsContext extends ParserRuleContext {
		public List<NumseqContext> numseq() {
			return getRuleContexts(NumseqContext.class);
		}
		public NumseqContext numseq(int i) {
			return getRuleContext(NumseqContext.class,i);
		}
		public NumseqsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numseqs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinearizationListener ) ((LinearizationListener)listener).enterNumseqs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinearizationListener ) ((LinearizationListener)listener).exitNumseqs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinearizationVisitor ) return ((LinearizationVisitor<? extends T>)visitor).visitNumseqs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumseqsContext numseqs() throws RecognitionException {
		NumseqsContext _localctx = new NumseqsContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_numseqs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(19); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(18);
				numseq();
				}
				}
				setState(21); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==Lparen );
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\b\32\4\2\t\2\4\3"+
		"\t\3\4\4\t\4\3\2\3\2\3\2\7\2\f\n\2\f\2\16\2\17\13\2\3\3\3\3\3\3\3\3\3"+
		"\4\6\4\26\n\4\r\4\16\4\27\3\4\2\2\5\2\4\6\2\2\2\30\2\b\3\2\2\2\4\20\3"+
		"\2\2\2\6\25\3\2\2\2\b\r\7\5\2\2\t\n\7\b\2\2\n\f\7\5\2\2\13\t\3\2\2\2\f"+
		"\17\3\2\2\2\r\13\3\2\2\2\r\16\3\2\2\2\16\3\3\2\2\2\17\r\3\2\2\2\20\21"+
		"\7\6\2\2\21\22\5\2\2\2\22\23\7\7\2\2\23\5\3\2\2\2\24\26\5\4\3\2\25\24"+
		"\3\2\2\2\26\27\3\2\2\2\27\25\3\2\2\2\27\30\3\2\2\2\30\7\3\2\2\2\4\r\27";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}