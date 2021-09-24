// Generated from /Users/cbrew/Documents/GitHub/quadruplet/src/main/antlr4/com/cbrew/fstruct/notation/FeatLexer.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FeatLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

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
		ISLAND=1;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "ISLAND"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"LINE_COMMENT", "WS", "Equals", "FstructVariable", "Category", "Fname", 
			"Lparen", "Rparen", "Lsq", "Rsq", "Comma", "Open", "Arrow", "Arrow2", 
			"Colon", "Pipe", "Word", "Close", "Number", "WS2", "Or", "And", "Implies", 
			"Iff", "Not", "NotEquals", "SemEquals", "SemComma", "SemLparen", "SemRparen", 
			"Lambda", "Forall", "Exists", "Dot", "Box", "Individual", "Predicate", 
			"FstructVariable2", "Constant"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, "'='", null, null, null, null, null, "'['", "']'", 
			null, "'<'", "'->'", "'=>'", "':'", "'|'", null, "'>'", null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"'.'", "'\u2610'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LINE_COMMENT", "WS", "Equals", "FstructVariable", "Category", 
			"Fname", "Lparen", "Rparen", "Lsq", "Rsq", "Comma", "Open", "Arrow", 
			"Arrow2", "Colon", "Pipe", "Word", "Close", "Number", "WS2", "Or", "And", 
			"Implies", "Iff", "Not", "NotEquals", "SemEquals", "SemComma", "SemLparen", 
			"SemRparen", "Lambda", "Forall", "Exists", "Dot", "Box", "Individual", 
			"Predicate", "FstructVariable2", "Constant"
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


	public FeatLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "FeatLexer.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2)\u00fd\b\1\b\1\4"+
		"\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n"+
		"\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\3\2\3\2\7\2U\n\2"+
		"\f\2\16\2X\13\2\3\2\3\2\3\3\6\3]\n\3\r\3\16\3^\3\3\3\3\3\4\3\4\3\5\3\5"+
		"\6\5g\n\5\r\5\16\5h\3\6\3\6\7\6m\n\6\f\6\16\6p\13\6\3\7\6\7s\n\7\r\7\16"+
		"\7t\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16"+
		"\3\16\3\17\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\6\22\u0091\n\22\r\22"+
		"\16\22\u0092\3\22\3\22\3\23\3\23\3\23\3\23\3\24\6\24\u009c\n\24\r\24\16"+
		"\24\u009d\3\25\6\25\u00a1\n\25\r\25\16\25\u00a2\3\25\3\25\3\26\3\26\3"+
		"\27\3\27\3\30\3\30\3\30\5\30\u00ae\n\30\3\31\3\31\3\31\3\31\5\31\u00b4"+
		"\n\31\3\32\3\32\3\33\3\33\3\33\3\33\3\33\5\33\u00bd\n\33\3\34\3\34\3\34"+
		"\5\34\u00c2\n\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3!\3!\3!\3"+
		"!\3!\3!\3!\3!\5!\u00d6\n!\3\"\3\"\3\"\3\"\3\"\3\"\3\"\5\"\u00df\n\"\3"+
		"#\3#\3$\3$\3%\3%\7%\u00e7\n%\f%\16%\u00ea\13%\3&\3&\7&\u00ee\n&\f&\16"+
		"&\u00f1\13&\3\'\3\'\6\'\u00f5\n\'\r\'\16\'\u00f6\3(\6(\u00fa\n(\r(\16"+
		"(\u00fb\2\2)\4\3\6\4\b\5\n\6\f\7\16\b\20\t\22\n\24\13\26\f\30\r\32\16"+
		"\34\17\36\20 \21\"\22$\23&\24(\25*\26,\27.\30\60\31\62\32\64\33\66\34"+
		"8\35:\36<\37> @!B\"D#F$H%J&L\'N(P)\4\2\3\20\4\2\f\f\17\17\5\2\13\f\17"+
		"\17\"\"\3\2AB\4\2\62;c|\3\2C\\\5\2\62;aac|\3\2$$\3\2\62;\4\2~~\u222a\u222a"+
		"\4\2((\u2229\u2229\4\2//\u0080\u0080\4\2^^\u03bd\u03bd\3\2c|\13\2%%\'"+
		"\')),-\61=B]__a|\u0082\0\2\u010f\2\4\3\2\2\2\2\6\3\2\2\2\2\b\3\2\2\2\2"+
		"\n\3\2\2\2\2\f\3\2\2\2\2\16\3\2\2\2\2\20\3\2\2\2\2\22\3\2\2\2\2\24\3\2"+
		"\2\2\2\26\3\2\2\2\2\30\3\2\2\2\2\32\3\2\2\2\2\34\3\2\2\2\2\36\3\2\2\2"+
		"\2 \3\2\2\2\2\"\3\2\2\2\2$\3\2\2\2\3&\3\2\2\2\3(\3\2\2\2\3*\3\2\2\2\3"+
		",\3\2\2\2\3.\3\2\2\2\3\60\3\2\2\2\3\62\3\2\2\2\3\64\3\2\2\2\3\66\3\2\2"+
		"\2\38\3\2\2\2\3:\3\2\2\2\3<\3\2\2\2\3>\3\2\2\2\3@\3\2\2\2\3B\3\2\2\2\3"+
		"D\3\2\2\2\3F\3\2\2\2\3H\3\2\2\2\3J\3\2\2\2\3L\3\2\2\2\3N\3\2\2\2\3P\3"+
		"\2\2\2\4R\3\2\2\2\6\\\3\2\2\2\bb\3\2\2\2\nd\3\2\2\2\fj\3\2\2\2\16r\3\2"+
		"\2\2\20v\3\2\2\2\22x\3\2\2\2\24z\3\2\2\2\26|\3\2\2\2\30~\3\2\2\2\32\u0080"+
		"\3\2\2\2\34\u0084\3\2\2\2\36\u0087\3\2\2\2 \u008a\3\2\2\2\"\u008c\3\2"+
		"\2\2$\u008e\3\2\2\2&\u0096\3\2\2\2(\u009b\3\2\2\2*\u00a0\3\2\2\2,\u00a6"+
		"\3\2\2\2.\u00a8\3\2\2\2\60\u00ad\3\2\2\2\62\u00b3\3\2\2\2\64\u00b5\3\2"+
		"\2\2\66\u00bc\3\2\2\28\u00c1\3\2\2\2:\u00c3\3\2\2\2<\u00c5\3\2\2\2>\u00c7"+
		"\3\2\2\2@\u00c9\3\2\2\2B\u00d5\3\2\2\2D\u00de\3\2\2\2F\u00e0\3\2\2\2H"+
		"\u00e2\3\2\2\2J\u00e4\3\2\2\2L\u00eb\3\2\2\2N\u00f2\3\2\2\2P\u00f9\3\2"+
		"\2\2RV\7%\2\2SU\n\2\2\2TS\3\2\2\2UX\3\2\2\2VT\3\2\2\2VW\3\2\2\2WY\3\2"+
		"\2\2XV\3\2\2\2YZ\b\2\2\2Z\5\3\2\2\2[]\t\3\2\2\\[\3\2\2\2]^\3\2\2\2^\\"+
		"\3\2\2\2^_\3\2\2\2_`\3\2\2\2`a\b\3\2\2a\7\3\2\2\2bc\7?\2\2c\t\3\2\2\2"+
		"df\t\4\2\2eg\t\5\2\2fe\3\2\2\2gh\3\2\2\2hf\3\2\2\2hi\3\2\2\2i\13\3\2\2"+
		"\2jn\t\6\2\2km\t\5\2\2lk\3\2\2\2mp\3\2\2\2nl\3\2\2\2no\3\2\2\2o\r\3\2"+
		"\2\2pn\3\2\2\2qs\t\7\2\2rq\3\2\2\2st\3\2\2\2tr\3\2\2\2tu\3\2\2\2u\17\3"+
		"\2\2\2vw\7*\2\2w\21\3\2\2\2xy\7+\2\2y\23\3\2\2\2z{\7]\2\2{\25\3\2\2\2"+
		"|}\7_\2\2}\27\3\2\2\2~\177\7.\2\2\177\31\3\2\2\2\u0080\u0081\7>\2\2\u0081"+
		"\u0082\3\2\2\2\u0082\u0083\b\r\3\2\u0083\33\3\2\2\2\u0084\u0085\7/\2\2"+
		"\u0085\u0086\7@\2\2\u0086\35\3\2\2\2\u0087\u0088\7?\2\2\u0088\u0089\7"+
		"@\2\2\u0089\37\3\2\2\2\u008a\u008b\7<\2\2\u008b!\3\2\2\2\u008c\u008d\7"+
		"~\2\2\u008d#\3\2\2\2\u008e\u0090\7$\2\2\u008f\u0091\n\b\2\2\u0090\u008f"+
		"\3\2\2\2\u0091\u0092\3\2\2\2\u0092\u0090\3\2\2\2\u0092\u0093\3\2\2\2\u0093"+
		"\u0094\3\2\2\2\u0094\u0095\7$\2\2\u0095%\3\2\2\2\u0096\u0097\7@\2\2\u0097"+
		"\u0098\3\2\2\2\u0098\u0099\b\23\4\2\u0099\'\3\2\2\2\u009a\u009c\t\t\2"+
		"\2\u009b\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009b\3\2\2\2\u009d\u009e"+
		"\3\2\2\2\u009e)\3\2\2\2\u009f\u00a1\t\3\2\2\u00a0\u009f\3\2\2\2\u00a1"+
		"\u00a2\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00a4\3\2"+
		"\2\2\u00a4\u00a5\b\25\2\2\u00a5+\3\2\2\2\u00a6\u00a7\t\n\2\2\u00a7-\3"+
		"\2\2\2\u00a8\u00a9\t\13\2\2\u00a9/\3\2\2\2\u00aa\u00ab\7/\2\2\u00ab\u00ae"+
		"\7@\2\2\u00ac\u00ae\7\u2194\2\2\u00ad\u00aa\3\2\2\2\u00ad\u00ac\3\2\2"+
		"\2\u00ae\61\3\2\2\2\u00af\u00b0\7>\2\2\u00b0\u00b1\7/\2\2\u00b1\u00b4"+
		"\7@\2\2\u00b2\u00b4\7\u2196\2\2\u00b3\u00af\3\2\2\2\u00b3\u00b2\3\2\2"+
		"\2\u00b4\63\3\2\2\2\u00b5\u00b6\t\f\2\2\u00b6\65\3\2\2\2\u00b7\u00b8\7"+
		"#\2\2\u00b8\u00bd\7?\2\2\u00b9\u00ba\7>\2\2\u00ba\u00bd\7@\2\2\u00bb\u00bd"+
		"\7\u2262\2\2\u00bc\u00b7\3\2\2\2\u00bc\u00b9\3\2\2\2\u00bc\u00bb\3\2\2"+
		"\2\u00bd\67\3\2\2\2\u00be\u00bf\7?\2\2\u00bf\u00c2\7?\2\2\u00c0\u00c2"+
		"\7?\2\2\u00c1\u00be\3\2\2\2\u00c1\u00c0\3\2\2\2\u00c29\3\2\2\2\u00c3\u00c4"+
		"\7.\2\2\u00c4;\3\2\2\2\u00c5\u00c6\7*\2\2\u00c6=\3\2\2\2\u00c7\u00c8\7"+
		"+\2\2\u00c8?\3\2\2\2\u00c9\u00ca\t\r\2\2\u00caA\3\2\2\2\u00cb\u00cc\7"+
		"h\2\2\u00cc\u00cd\7q\2\2\u00cd\u00ce\7t\2\2\u00ce\u00cf\7c\2\2\u00cf\u00d0"+
		"\7n\2\2\u00d0\u00d6\7n\2\2\u00d1\u00d2\7c\2\2\u00d2\u00d3\7n\2\2\u00d3"+
		"\u00d6\7n\2\2\u00d4\u00d6\7\u2202\2\2\u00d5\u00cb\3\2\2\2\u00d5\u00d1"+
		"\3\2\2\2\u00d5\u00d4\3\2\2\2\u00d6C\3\2\2\2\u00d7\u00d8\7g\2\2\u00d8\u00d9"+
		"\7z\2\2\u00d9\u00da\7k\2\2\u00da\u00db\7u\2\2\u00db\u00dc\7v\2\2\u00dc"+
		"\u00df\7u\2\2\u00dd\u00df\7\u2205\2\2\u00de\u00d7\3\2\2\2\u00de\u00dd"+
		"\3\2\2\2\u00dfE\3\2\2\2\u00e0\u00e1\7\60\2\2\u00e1G\3\2\2\2\u00e2\u00e3"+
		"\7\u2612\2\2\u00e3I\3\2\2\2\u00e4\u00e8\t\16\2\2\u00e5\u00e7\t\t\2\2\u00e6"+
		"\u00e5\3\2\2\2\u00e7\u00ea\3\2\2\2\u00e8\u00e6\3\2\2\2\u00e8\u00e9\3\2"+
		"\2\2\u00e9K\3\2\2\2\u00ea\u00e8\3\2\2\2\u00eb\u00ef\t\6\2\2\u00ec\u00ee"+
		"\t\t\2\2\u00ed\u00ec\3\2\2\2\u00ee\u00f1\3\2\2\2\u00ef\u00ed\3\2\2\2\u00ef"+
		"\u00f0\3\2\2\2\u00f0M\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f2\u00f4\t\4\2\2"+
		"\u00f3\u00f5\t\5\2\2\u00f4\u00f3\3\2\2\2\u00f5\u00f6\3\2\2\2\u00f6\u00f4"+
		"\3\2\2\2\u00f6\u00f7\3\2\2\2\u00f7O\3\2\2\2\u00f8\u00fa\t\17\2\2\u00f9"+
		"\u00f8\3\2\2\2\u00fa\u00fb\3\2\2\2\u00fb\u00f9\3\2\2\2\u00fb\u00fc\3\2"+
		"\2\2\u00fcQ\3\2\2\2\27\2\3V^hnt\u0092\u009d\u00a2\u00ad\u00b3\u00bc\u00c1"+
		"\u00d5\u00de\u00e8\u00ef\u00f6\u00f9\u00fb\5\b\2\2\7\3\2\6\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}