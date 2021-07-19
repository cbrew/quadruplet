// Generated from com/cbrew/logic/notation/LogicTerms.g4 by ANTLR 4.7
package com.cbrew.logic.notation;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class LogicTermsLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, LINE_COMMENT=2, WS=3, Or=4, And=5, Implies=6, Iff=7, Not=8, NotEquals=9, 
		Equals=10, Comma=11, Lparen=12, Rparen=13, Lambda=14, Forall=15, Exists=16, 
		Dot=17, Box=18, Individual=19, Predicate=20, FstructVariable=21, Constant=22;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "LINE_COMMENT", "WS", "Or", "And", "Implies", "Iff", "Not", "NotEquals", 
		"Equals", "Comma", "Lparen", "Rparen", "Lambda", "Forall", "Exists", "Dot", 
		"Box", "Individual", "Predicate", "FstructVariable", "Constant"
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


	public LogicTermsLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "LogicTerms.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\30\u00a2\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\2\3"+
		"\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\7\3>\n\3\f\3\16\3A\13\3\3\3"+
		"\3\3\3\4\6\4F\n\4\r\4\16\4G\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\5\7S\n"+
		"\7\3\b\3\b\3\b\3\b\5\bY\n\b\3\t\3\t\3\n\3\n\3\n\3\n\3\n\5\nb\n\n\3\13"+
		"\3\13\3\13\5\13g\n\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3"+
		"\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20{\n\20\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\5\21\u0084\n\21\3\22\3\22\3\23\3\23\3\24\3\24\7\24\u008c"+
		"\n\24\f\24\16\24\u008f\13\24\3\25\3\25\7\25\u0093\n\25\f\25\16\25\u0096"+
		"\13\25\3\26\3\26\6\26\u009a\n\26\r\26\16\26\u009b\3\27\6\27\u009f\n\27"+
		"\r\27\16\27\u00a0\2\2\30\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f"+
		"\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30\3\2\16\4\2"+
		"\f\f\17\17\5\2\13\f\17\17\"\"\4\2~~\u222a\u222a\4\2((\u2229\u2229\4\2"+
		"//\u0080\u0080\4\2^^\u03bd\u03bd\3\2c|\3\2\62;\3\2C\\\3\2AB\4\2\62;c|"+
		"\13\2%%\'\')),-\61=B]__a|\u0082\0\2\u00af\2\3\3\2\2\2\2\5\3\2\2\2\2\7"+
		"\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2"+
		"\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2"+
		"\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2"+
		"\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\3/\3\2\2\2\5;\3\2\2\2\7E\3\2\2\2\tK"+
		"\3\2\2\2\13M\3\2\2\2\rR\3\2\2\2\17X\3\2\2\2\21Z\3\2\2\2\23a\3\2\2\2\25"+
		"f\3\2\2\2\27h\3\2\2\2\31j\3\2\2\2\33l\3\2\2\2\35n\3\2\2\2\37z\3\2\2\2"+
		"!\u0083\3\2\2\2#\u0085\3\2\2\2%\u0087\3\2\2\2\'\u0089\3\2\2\2)\u0090\3"+
		"\2\2\2+\u0097\3\2\2\2-\u009e\3\2\2\2/\60\7B\2\2\60\61\7K\2\2\61\62\7p"+
		"\2\2\62\63\7l\2\2\63\64\7g\2\2\64\65\7e\2\2\65\66\7v\2\2\66\67\7c\2\2"+
		"\678\7d\2\289\7n\2\29:\7g\2\2:\4\3\2\2\2;?\7%\2\2<>\n\2\2\2=<\3\2\2\2"+
		">A\3\2\2\2?=\3\2\2\2?@\3\2\2\2@B\3\2\2\2A?\3\2\2\2BC\b\3\2\2C\6\3\2\2"+
		"\2DF\t\3\2\2ED\3\2\2\2FG\3\2\2\2GE\3\2\2\2GH\3\2\2\2HI\3\2\2\2IJ\b\4\2"+
		"\2J\b\3\2\2\2KL\t\4\2\2L\n\3\2\2\2MN\t\5\2\2N\f\3\2\2\2OP\7/\2\2PS\7@"+
		"\2\2QS\7\u2194\2\2RO\3\2\2\2RQ\3\2\2\2S\16\3\2\2\2TU\7>\2\2UV\7/\2\2V"+
		"Y\7@\2\2WY\7\u2196\2\2XT\3\2\2\2XW\3\2\2\2Y\20\3\2\2\2Z[\t\6\2\2[\22\3"+
		"\2\2\2\\]\7#\2\2]b\7?\2\2^_\7>\2\2_b\7@\2\2`b\7\u2262\2\2a\\\3\2\2\2a"+
		"^\3\2\2\2a`\3\2\2\2b\24\3\2\2\2cd\7?\2\2dg\7?\2\2eg\7?\2\2fc\3\2\2\2f"+
		"e\3\2\2\2g\26\3\2\2\2hi\7.\2\2i\30\3\2\2\2jk\7*\2\2k\32\3\2\2\2lm\7+\2"+
		"\2m\34\3\2\2\2no\t\7\2\2o\36\3\2\2\2pq\7h\2\2qr\7q\2\2rs\7t\2\2st\7c\2"+
		"\2tu\7n\2\2u{\7n\2\2vw\7c\2\2wx\7n\2\2x{\7n\2\2y{\7\u2202\2\2zp\3\2\2"+
		"\2zv\3\2\2\2zy\3\2\2\2{ \3\2\2\2|}\7g\2\2}~\7z\2\2~\177\7k\2\2\177\u0080"+
		"\7u\2\2\u0080\u0081\7v\2\2\u0081\u0084\7u\2\2\u0082\u0084\7\u2205\2\2"+
		"\u0083|\3\2\2\2\u0083\u0082\3\2\2\2\u0084\"\3\2\2\2\u0085\u0086\7\60\2"+
		"\2\u0086$\3\2\2\2\u0087\u0088\7\u2612\2\2\u0088&\3\2\2\2\u0089\u008d\t"+
		"\b\2\2\u008a\u008c\t\t\2\2\u008b\u008a\3\2\2\2\u008c\u008f\3\2\2\2\u008d"+
		"\u008b\3\2\2\2\u008d\u008e\3\2\2\2\u008e(\3\2\2\2\u008f\u008d\3\2\2\2"+
		"\u0090\u0094\t\n\2\2\u0091\u0093\t\t\2\2\u0092\u0091\3\2\2\2\u0093\u0096"+
		"\3\2\2\2\u0094\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095*\3\2\2\2\u0096"+
		"\u0094\3\2\2\2\u0097\u0099\t\13\2\2\u0098\u009a\t\f\2\2\u0099\u0098\3"+
		"\2\2\2\u009a\u009b\3\2\2\2\u009b\u0099\3\2\2\2\u009b\u009c\3\2\2\2\u009c"+
		",\3\2\2\2\u009d\u009f\t\r\2\2\u009e\u009d\3\2\2\2\u009f\u00a0\3\2\2\2"+
		"\u00a0\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1.\3\2\2\2\20\2?GRXafz\u0083"+
		"\u008d\u0094\u009b\u009e\u00a0\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}