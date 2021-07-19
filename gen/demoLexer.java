// Generated from /Users/cbrew/Documents/GitHub/quadruplet/src/main/antlr4/com/cbrew/demo/demo.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class demoLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LINE_COMMENT=1, WS=2, Arrow=3, Pipe=4, Quoted=5, Equals=6, Category=7;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"LINE_COMMENT", "WS", "Arrow", "Pipe", "Quoted", "Equals", "Category"
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


	public demoLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "demo.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\tB\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\7\2\24\n\2\f\2"+
		"\16\2\27\13\2\3\2\3\2\3\3\6\3\34\n\3\r\3\16\3\35\3\3\3\3\3\4\3\4\3\4\3"+
		"\5\3\5\3\6\3\6\6\6)\n\6\r\6\16\6*\3\6\3\6\6\6/\n\6\r\6\16\6\60\7\6\63"+
		"\n\6\f\6\16\6\66\13\6\3\6\3\6\3\7\3\7\3\b\3\b\7\b>\n\b\f\b\16\bA\13\b"+
		"\2\2\t\3\3\5\4\7\5\t\6\13\7\r\b\17\t\3\2\b\4\2\f\f\17\17\5\2\13\f\17\17"+
		"\"\"\4\2C\\c|\3\2\"\"\3\2C\\\5\2\62;C\\c|\2G\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\3\21\3\2"+
		"\2\2\5\33\3\2\2\2\7!\3\2\2\2\t$\3\2\2\2\13&\3\2\2\2\r9\3\2\2\2\17;\3\2"+
		"\2\2\21\25\7%\2\2\22\24\n\2\2\2\23\22\3\2\2\2\24\27\3\2\2\2\25\23\3\2"+
		"\2\2\25\26\3\2\2\2\26\30\3\2\2\2\27\25\3\2\2\2\30\31\b\2\2\2\31\4\3\2"+
		"\2\2\32\34\t\3\2\2\33\32\3\2\2\2\34\35\3\2\2\2\35\33\3\2\2\2\35\36\3\2"+
		"\2\2\36\37\3\2\2\2\37 \b\3\2\2 \6\3\2\2\2!\"\7/\2\2\"#\7@\2\2#\b\3\2\2"+
		"\2$%\7~\2\2%\n\3\2\2\2&(\7)\2\2\')\t\4\2\2(\'\3\2\2\2)*\3\2\2\2*(\3\2"+
		"\2\2*+\3\2\2\2+\64\3\2\2\2,.\t\5\2\2-/\t\4\2\2.-\3\2\2\2/\60\3\2\2\2\60"+
		".\3\2\2\2\60\61\3\2\2\2\61\63\3\2\2\2\62,\3\2\2\2\63\66\3\2\2\2\64\62"+
		"\3\2\2\2\64\65\3\2\2\2\65\67\3\2\2\2\66\64\3\2\2\2\678\7)\2\28\f\3\2\2"+
		"\29:\7?\2\2:\16\3\2\2\2;?\t\6\2\2<>\t\7\2\2=<\3\2\2\2>A\3\2\2\2?=\3\2"+
		"\2\2?@\3\2\2\2@\20\3\2\2\2A?\3\2\2\2\t\2\25\35*\60\64?\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}