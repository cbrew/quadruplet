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
		SemRparen=30, Lambda=31, Forall=32, Exists=33, Dot=34, Box=35, FstructVariable2=36, 
		Constant=37;
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
			"Lambda", "Forall", "Exists", "Dot", "Box", "FstructVariable2", "Constant"
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
			"SemRparen", "Lambda", "Forall", "Exists", "Dot", "Box", "FstructVariable2", 
			"Constant"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\'\u00eb\b\1\b\1\4"+
		"\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n"+
		"\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\3\2\3\2\7\2Q\n\2\f\2\16\2T\13"+
		"\2\3\2\3\2\3\3\6\3Y\n\3\r\3\16\3Z\3\3\3\3\3\4\3\4\3\5\3\5\6\5c\n\5\r\5"+
		"\16\5d\3\6\3\6\7\6i\n\6\f\6\16\6l\13\6\3\7\6\7o\n\7\r\7\16\7p\3\b\3\b"+
		"\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\17"+
		"\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\6\22\u008d\n\22\r\22\16\22\u008e"+
		"\3\22\3\22\3\23\3\23\3\23\3\23\3\24\6\24\u0098\n\24\r\24\16\24\u0099\3"+
		"\25\6\25\u009d\n\25\r\25\16\25\u009e\3\25\3\25\3\26\3\26\3\27\3\27\3\30"+
		"\3\30\3\30\5\30\u00aa\n\30\3\31\3\31\3\31\3\31\5\31\u00b0\n\31\3\32\3"+
		"\32\3\33\3\33\3\33\3\33\3\33\5\33\u00b9\n\33\3\34\3\34\3\34\5\34\u00be"+
		"\n\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3!\3!\3!\3!\3!\3!\3!\3"+
		"!\5!\u00d2\n!\3\"\3\"\3\"\3\"\3\"\3\"\3\"\5\"\u00db\n\"\3#\3#\3$\3$\3"+
		"%\3%\6%\u00e3\n%\r%\16%\u00e4\3&\6&\u00e8\n&\r&\16&\u00e9\2\2\'\4\3\6"+
		"\4\b\5\n\6\f\7\16\b\20\t\22\n\24\13\26\f\30\r\32\16\34\17\36\20 \21\""+
		"\22$\23&\24(\25*\26,\27.\30\60\31\62\32\64\33\66\348\35:\36<\37> @!B\""+
		"D#F$H%J&L\'\4\2\3\17\4\2\f\f\17\17\5\2\13\f\17\17\"\"\3\2AB\4\2\62;c|"+
		"\3\2C\\\5\2\62;aac|\3\2$$\3\2\62;\4\2~~\u222a\u222a\4\2((\u2229\u2229"+
		"\4\2//\u0080\u0080\4\2^^\u03bd\u03bd\13\2%%\'\')),-\61=B]__a|\u0082\0"+
		"\2\u00fb\2\4\3\2\2\2\2\6\3\2\2\2\2\b\3\2\2\2\2\n\3\2\2\2\2\f\3\2\2\2\2"+
		"\16\3\2\2\2\2\20\3\2\2\2\2\22\3\2\2\2\2\24\3\2\2\2\2\26\3\2\2\2\2\30\3"+
		"\2\2\2\2\32\3\2\2\2\2\34\3\2\2\2\2\36\3\2\2\2\2 \3\2\2\2\2\"\3\2\2\2\2"+
		"$\3\2\2\2\3&\3\2\2\2\3(\3\2\2\2\3*\3\2\2\2\3,\3\2\2\2\3.\3\2\2\2\3\60"+
		"\3\2\2\2\3\62\3\2\2\2\3\64\3\2\2\2\3\66\3\2\2\2\38\3\2\2\2\3:\3\2\2\2"+
		"\3<\3\2\2\2\3>\3\2\2\2\3@\3\2\2\2\3B\3\2\2\2\3D\3\2\2\2\3F\3\2\2\2\3H"+
		"\3\2\2\2\3J\3\2\2\2\3L\3\2\2\2\4N\3\2\2\2\6X\3\2\2\2\b^\3\2\2\2\n`\3\2"+
		"\2\2\ff\3\2\2\2\16n\3\2\2\2\20r\3\2\2\2\22t\3\2\2\2\24v\3\2\2\2\26x\3"+
		"\2\2\2\30z\3\2\2\2\32|\3\2\2\2\34\u0080\3\2\2\2\36\u0083\3\2\2\2 \u0086"+
		"\3\2\2\2\"\u0088\3\2\2\2$\u008a\3\2\2\2&\u0092\3\2\2\2(\u0097\3\2\2\2"+
		"*\u009c\3\2\2\2,\u00a2\3\2\2\2.\u00a4\3\2\2\2\60\u00a9\3\2\2\2\62\u00af"+
		"\3\2\2\2\64\u00b1\3\2\2\2\66\u00b8\3\2\2\28\u00bd\3\2\2\2:\u00bf\3\2\2"+
		"\2<\u00c1\3\2\2\2>\u00c3\3\2\2\2@\u00c5\3\2\2\2B\u00d1\3\2\2\2D\u00da"+
		"\3\2\2\2F\u00dc\3\2\2\2H\u00de\3\2\2\2J\u00e0\3\2\2\2L\u00e7\3\2\2\2N"+
		"R\7%\2\2OQ\n\2\2\2PO\3\2\2\2QT\3\2\2\2RP\3\2\2\2RS\3\2\2\2SU\3\2\2\2T"+
		"R\3\2\2\2UV\b\2\2\2V\5\3\2\2\2WY\t\3\2\2XW\3\2\2\2YZ\3\2\2\2ZX\3\2\2\2"+
		"Z[\3\2\2\2[\\\3\2\2\2\\]\b\3\2\2]\7\3\2\2\2^_\7?\2\2_\t\3\2\2\2`b\t\4"+
		"\2\2ac\t\5\2\2ba\3\2\2\2cd\3\2\2\2db\3\2\2\2de\3\2\2\2e\13\3\2\2\2fj\t"+
		"\6\2\2gi\t\5\2\2hg\3\2\2\2il\3\2\2\2jh\3\2\2\2jk\3\2\2\2k\r\3\2\2\2lj"+
		"\3\2\2\2mo\t\7\2\2nm\3\2\2\2op\3\2\2\2pn\3\2\2\2pq\3\2\2\2q\17\3\2\2\2"+
		"rs\7*\2\2s\21\3\2\2\2tu\7+\2\2u\23\3\2\2\2vw\7]\2\2w\25\3\2\2\2xy\7_\2"+
		"\2y\27\3\2\2\2z{\7.\2\2{\31\3\2\2\2|}\7>\2\2}~\3\2\2\2~\177\b\r\3\2\177"+
		"\33\3\2\2\2\u0080\u0081\7/\2\2\u0081\u0082\7@\2\2\u0082\35\3\2\2\2\u0083"+
		"\u0084\7?\2\2\u0084\u0085\7@\2\2\u0085\37\3\2\2\2\u0086\u0087\7<\2\2\u0087"+
		"!\3\2\2\2\u0088\u0089\7~\2\2\u0089#\3\2\2\2\u008a\u008c\7$\2\2\u008b\u008d"+
		"\n\b\2\2\u008c\u008b\3\2\2\2\u008d\u008e\3\2\2\2\u008e\u008c\3\2\2\2\u008e"+
		"\u008f\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0091\7$\2\2\u0091%\3\2\2\2\u0092"+
		"\u0093\7@\2\2\u0093\u0094\3\2\2\2\u0094\u0095\b\23\4\2\u0095\'\3\2\2\2"+
		"\u0096\u0098\t\t\2\2\u0097\u0096\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u0097"+
		"\3\2\2\2\u0099\u009a\3\2\2\2\u009a)\3\2\2\2\u009b\u009d\t\3\2\2\u009c"+
		"\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009c\3\2\2\2\u009e\u009f\3\2"+
		"\2\2\u009f\u00a0\3\2\2\2\u00a0\u00a1\b\25\2\2\u00a1+\3\2\2\2\u00a2\u00a3"+
		"\t\n\2\2\u00a3-\3\2\2\2\u00a4\u00a5\t\13\2\2\u00a5/\3\2\2\2\u00a6\u00a7"+
		"\7/\2\2\u00a7\u00aa\7@\2\2\u00a8\u00aa\7\u2194\2\2\u00a9\u00a6\3\2\2\2"+
		"\u00a9\u00a8\3\2\2\2\u00aa\61\3\2\2\2\u00ab\u00ac\7>\2\2\u00ac\u00ad\7"+
		"/\2\2\u00ad\u00b0\7@\2\2\u00ae\u00b0\7\u2196\2\2\u00af\u00ab\3\2\2\2\u00af"+
		"\u00ae\3\2\2\2\u00b0\63\3\2\2\2\u00b1\u00b2\t\f\2\2\u00b2\65\3\2\2\2\u00b3"+
		"\u00b4\7#\2\2\u00b4\u00b9\7?\2\2\u00b5\u00b6\7>\2\2\u00b6\u00b9\7@\2\2"+
		"\u00b7\u00b9\7\u2262\2\2\u00b8\u00b3\3\2\2\2\u00b8\u00b5\3\2\2\2\u00b8"+
		"\u00b7\3\2\2\2\u00b9\67\3\2\2\2\u00ba\u00bb\7?\2\2\u00bb\u00be\7?\2\2"+
		"\u00bc\u00be\7?\2\2\u00bd\u00ba\3\2\2\2\u00bd\u00bc\3\2\2\2\u00be9\3\2"+
		"\2\2\u00bf\u00c0\7.\2\2\u00c0;\3\2\2\2\u00c1\u00c2\7*\2\2\u00c2=\3\2\2"+
		"\2\u00c3\u00c4\7+\2\2\u00c4?\3\2\2\2\u00c5\u00c6\t\r\2\2\u00c6A\3\2\2"+
		"\2\u00c7\u00c8\7h\2\2\u00c8\u00c9\7q\2\2\u00c9\u00ca\7t\2\2\u00ca\u00cb"+
		"\7c\2\2\u00cb\u00cc\7n\2\2\u00cc\u00d2\7n\2\2\u00cd\u00ce\7c\2\2\u00ce"+
		"\u00cf\7n\2\2\u00cf\u00d2\7n\2\2\u00d0\u00d2\7\u2202\2\2\u00d1\u00c7\3"+
		"\2\2\2\u00d1\u00cd\3\2\2\2\u00d1\u00d0\3\2\2\2\u00d2C\3\2\2\2\u00d3\u00d4"+
		"\7g\2\2\u00d4\u00d5\7z\2\2\u00d5\u00d6\7k\2\2\u00d6\u00d7\7u\2\2\u00d7"+
		"\u00d8\7v\2\2\u00d8\u00db\7u\2\2\u00d9\u00db\7\u2205\2\2\u00da\u00d3\3"+
		"\2\2\2\u00da\u00d9\3\2\2\2\u00dbE\3\2\2\2\u00dc\u00dd\7\60\2\2\u00ddG"+
		"\3\2\2\2\u00de\u00df\7\u2612\2\2\u00dfI\3\2\2\2\u00e0\u00e2\t\4\2\2\u00e1"+
		"\u00e3\t\5\2\2\u00e2\u00e1\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00e2\3\2"+
		"\2\2\u00e4\u00e5\3\2\2\2\u00e5K\3\2\2\2\u00e6\u00e8\t\16\2\2\u00e7\u00e6"+
		"\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e9\u00e7\3\2\2\2\u00e9\u00ea\3\2\2\2\u00ea"+
		"M\3\2\2\2\25\2\3RZdjp\u008e\u0099\u009e\u00a9\u00af\u00b8\u00bd\u00d1"+
		"\u00da\u00e4\u00e7\u00e9\5\b\2\2\7\3\2\6\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}