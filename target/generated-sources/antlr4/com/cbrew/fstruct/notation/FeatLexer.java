// Generated from com/cbrew/fstruct/notation/FeatLexer.g4 by ANTLR 4.7
package com.cbrew.fstruct.notation;
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
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LINE_COMMENT=1, WS=2, Equals=3, FstructVariable=4, Category=5, Fname=6, 
		Lparen=7, Rparen=8, Plus=9, Lsq=10, Rsq=11, Comma=12, Open=13, Arrow=14, 
		Arrow2=15, Colon=16, Pipe=17, Word=18, Close=19, Number=20, WS2=21, Or=22, 
		And=23, Implies=24, Iff=25, Not=26, NotEquals=27, SemEquals=28, SemComma=29, 
		SemLparen=30, SemRparen=31, Lambda=32, Forall=33, Exists=34, Dot=35, Box=36, 
		Individual=37, Predicate=38, FstructVariable2=39, Constant=40;
	public static final int
		ISLAND=1;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "ISLAND"
	};

	public static final String[] ruleNames = {
		"LINE_COMMENT", "WS", "Equals", "FstructVariable", "Category", "Fname", 
		"Lparen", "Rparen", "Plus", "Lsq", "Rsq", "Comma", "Open", "Arrow", "Arrow2", 
		"Colon", "Pipe", "Word", "Close", "Number", "WS2", "Or", "And", "Implies", 
		"Iff", "Not", "NotEquals", "SemEquals", "SemComma", "SemLparen", "SemRparen", 
		"Lambda", "Forall", "Exists", "Dot", "Box", "Individual", "Predicate", 
		"FstructVariable2", "Constant"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, "'='", null, null, null, null, null, "'+'", "'['", "']'", 
		null, "'<'", "'->'", "'=>'", "':'", "'|'", null, "'>'", null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		"'.'", "'\u2610'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LINE_COMMENT", "WS", "Equals", "FstructVariable", "Category", "Fname", 
		"Lparen", "Rparen", "Plus", "Lsq", "Rsq", "Comma", "Open", "Arrow", "Arrow2", 
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2*\u0101\b\1\b\1\4"+
		"\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n"+
		"\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\3\2\3\2\7"+
		"\2W\n\2\f\2\16\2Z\13\2\3\2\3\2\3\3\6\3_\n\3\r\3\16\3`\3\3\3\3\3\4\3\4"+
		"\3\5\3\5\6\5i\n\5\r\5\16\5j\3\6\3\6\7\6o\n\6\f\6\16\6r\13\6\3\7\6\7u\n"+
		"\7\r\7\16\7v\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3"+
		"\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3"+
		"\23\6\23\u0095\n\23\r\23\16\23\u0096\3\23\3\23\3\24\3\24\3\24\3\24\3\25"+
		"\6\25\u00a0\n\25\r\25\16\25\u00a1\3\26\6\26\u00a5\n\26\r\26\16\26\u00a6"+
		"\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\31\5\31\u00b2\n\31\3\32\3\32"+
		"\3\32\3\32\5\32\u00b8\n\32\3\33\3\33\3\34\3\34\3\34\3\34\3\34\5\34\u00c1"+
		"\n\34\3\35\3\35\3\35\5\35\u00c6\n\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3"+
		"\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\5\"\u00da\n\"\3#\3#\3#\3#\3#\3"+
		"#\3#\5#\u00e3\n#\3$\3$\3%\3%\3&\3&\7&\u00eb\n&\f&\16&\u00ee\13&\3\'\3"+
		"\'\7\'\u00f2\n\'\f\'\16\'\u00f5\13\'\3(\3(\6(\u00f9\n(\r(\16(\u00fa\3"+
		")\6)\u00fe\n)\r)\16)\u00ff\2\2*\4\3\6\4\b\5\n\6\f\7\16\b\20\t\22\n\24"+
		"\13\26\f\30\r\32\16\34\17\36\20 \21\"\22$\23&\24(\25*\26,\27.\30\60\31"+
		"\62\32\64\33\66\348\35:\36<\37> @!B\"D#F$H%J&L\'N(P)R*\4\2\3\20\4\2\f"+
		"\f\17\17\5\2\13\f\17\17\"\"\3\2AB\4\2\62;c|\3\2C\\\5\2\62;aac|\3\2$$\3"+
		"\2\62;\4\2~~\u222a\u222a\4\2((\u2229\u2229\4\2//\u0080\u0080\4\2^^\u03bd"+
		"\u03bd\3\2c|\13\2%%\'\')),-\61=B]__a|\u0082\0\2\u0113\2\4\3\2\2\2\2\6"+
		"\3\2\2\2\2\b\3\2\2\2\2\n\3\2\2\2\2\f\3\2\2\2\2\16\3\2\2\2\2\20\3\2\2\2"+
		"\2\22\3\2\2\2\2\24\3\2\2\2\2\26\3\2\2\2\2\30\3\2\2\2\2\32\3\2\2\2\2\34"+
		"\3\2\2\2\2\36\3\2\2\2\2 \3\2\2\2\2\"\3\2\2\2\2$\3\2\2\2\2&\3\2\2\2\3("+
		"\3\2\2\2\3*\3\2\2\2\3,\3\2\2\2\3.\3\2\2\2\3\60\3\2\2\2\3\62\3\2\2\2\3"+
		"\64\3\2\2\2\3\66\3\2\2\2\38\3\2\2\2\3:\3\2\2\2\3<\3\2\2\2\3>\3\2\2\2\3"+
		"@\3\2\2\2\3B\3\2\2\2\3D\3\2\2\2\3F\3\2\2\2\3H\3\2\2\2\3J\3\2\2\2\3L\3"+
		"\2\2\2\3N\3\2\2\2\3P\3\2\2\2\3R\3\2\2\2\4T\3\2\2\2\6^\3\2\2\2\bd\3\2\2"+
		"\2\nf\3\2\2\2\fl\3\2\2\2\16t\3\2\2\2\20x\3\2\2\2\22z\3\2\2\2\24|\3\2\2"+
		"\2\26~\3\2\2\2\30\u0080\3\2\2\2\32\u0082\3\2\2\2\34\u0084\3\2\2\2\36\u0088"+
		"\3\2\2\2 \u008b\3\2\2\2\"\u008e\3\2\2\2$\u0090\3\2\2\2&\u0092\3\2\2\2"+
		"(\u009a\3\2\2\2*\u009f\3\2\2\2,\u00a4\3\2\2\2.\u00aa\3\2\2\2\60\u00ac"+
		"\3\2\2\2\62\u00b1\3\2\2\2\64\u00b7\3\2\2\2\66\u00b9\3\2\2\28\u00c0\3\2"+
		"\2\2:\u00c5\3\2\2\2<\u00c7\3\2\2\2>\u00c9\3\2\2\2@\u00cb\3\2\2\2B\u00cd"+
		"\3\2\2\2D\u00d9\3\2\2\2F\u00e2\3\2\2\2H\u00e4\3\2\2\2J\u00e6\3\2\2\2L"+
		"\u00e8\3\2\2\2N\u00ef\3\2\2\2P\u00f6\3\2\2\2R\u00fd\3\2\2\2TX\7%\2\2U"+
		"W\n\2\2\2VU\3\2\2\2WZ\3\2\2\2XV\3\2\2\2XY\3\2\2\2Y[\3\2\2\2ZX\3\2\2\2"+
		"[\\\b\2\2\2\\\5\3\2\2\2]_\t\3\2\2^]\3\2\2\2_`\3\2\2\2`^\3\2\2\2`a\3\2"+
		"\2\2ab\3\2\2\2bc\b\3\2\2c\7\3\2\2\2de\7?\2\2e\t\3\2\2\2fh\t\4\2\2gi\t"+
		"\5\2\2hg\3\2\2\2ij\3\2\2\2jh\3\2\2\2jk\3\2\2\2k\13\3\2\2\2lp\t\6\2\2m"+
		"o\t\5\2\2nm\3\2\2\2or\3\2\2\2pn\3\2\2\2pq\3\2\2\2q\r\3\2\2\2rp\3\2\2\2"+
		"su\t\7\2\2ts\3\2\2\2uv\3\2\2\2vt\3\2\2\2vw\3\2\2\2w\17\3\2\2\2xy\7*\2"+
		"\2y\21\3\2\2\2z{\7+\2\2{\23\3\2\2\2|}\7-\2\2}\25\3\2\2\2~\177\7]\2\2\177"+
		"\27\3\2\2\2\u0080\u0081\7_\2\2\u0081\31\3\2\2\2\u0082\u0083\7.\2\2\u0083"+
		"\33\3\2\2\2\u0084\u0085\7>\2\2\u0085\u0086\3\2\2\2\u0086\u0087\b\16\3"+
		"\2\u0087\35\3\2\2\2\u0088\u0089\7/\2\2\u0089\u008a\7@\2\2\u008a\37\3\2"+
		"\2\2\u008b\u008c\7?\2\2\u008c\u008d\7@\2\2\u008d!\3\2\2\2\u008e\u008f"+
		"\7<\2\2\u008f#\3\2\2\2\u0090\u0091\7~\2\2\u0091%\3\2\2\2\u0092\u0094\7"+
		"$\2\2\u0093\u0095\n\b\2\2\u0094\u0093\3\2\2\2\u0095\u0096\3\2\2\2\u0096"+
		"\u0094\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u0099\7$"+
		"\2\2\u0099\'\3\2\2\2\u009a\u009b\7@\2\2\u009b\u009c\3\2\2\2\u009c\u009d"+
		"\b\24\4\2\u009d)\3\2\2\2\u009e\u00a0\t\t\2\2\u009f\u009e\3\2\2\2\u00a0"+
		"\u00a1\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2+\3\2\2\2"+
		"\u00a3\u00a5\t\3\2\2\u00a4\u00a3\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a4"+
		"\3\2\2\2\u00a6\u00a7\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8\u00a9\b\26\2\2"+
		"\u00a9-\3\2\2\2\u00aa\u00ab\t\n\2\2\u00ab/\3\2\2\2\u00ac\u00ad\t\13\2"+
		"\2\u00ad\61\3\2\2\2\u00ae\u00af\7/\2\2\u00af\u00b2\7@\2\2\u00b0\u00b2"+
		"\7\u2194\2\2\u00b1\u00ae\3\2\2\2\u00b1\u00b0\3\2\2\2\u00b2\63\3\2\2\2"+
		"\u00b3\u00b4\7>\2\2\u00b4\u00b5\7/\2\2\u00b5\u00b8\7@\2\2\u00b6\u00b8"+
		"\7\u2196\2\2\u00b7\u00b3\3\2\2\2\u00b7\u00b6\3\2\2\2\u00b8\65\3\2\2\2"+
		"\u00b9\u00ba\t\f\2\2\u00ba\67\3\2\2\2\u00bb\u00bc\7#\2\2\u00bc\u00c1\7"+
		"?\2\2\u00bd\u00be\7>\2\2\u00be\u00c1\7@\2\2\u00bf\u00c1\7\u2262\2\2\u00c0"+
		"\u00bb\3\2\2\2\u00c0\u00bd\3\2\2\2\u00c0\u00bf\3\2\2\2\u00c19\3\2\2\2"+
		"\u00c2\u00c3\7?\2\2\u00c3\u00c6\7?\2\2\u00c4\u00c6\7?\2\2\u00c5\u00c2"+
		"\3\2\2\2\u00c5\u00c4\3\2\2\2\u00c6;\3\2\2\2\u00c7\u00c8\7.\2\2\u00c8="+
		"\3\2\2\2\u00c9\u00ca\7*\2\2\u00ca?\3\2\2\2\u00cb\u00cc\7+\2\2\u00ccA\3"+
		"\2\2\2\u00cd\u00ce\t\r\2\2\u00ceC\3\2\2\2\u00cf\u00d0\7h\2\2\u00d0\u00d1"+
		"\7q\2\2\u00d1\u00d2\7t\2\2\u00d2\u00d3\7c\2\2\u00d3\u00d4\7n\2\2\u00d4"+
		"\u00da\7n\2\2\u00d5\u00d6\7c\2\2\u00d6\u00d7\7n\2\2\u00d7\u00da\7n\2\2"+
		"\u00d8\u00da\7\u2202\2\2\u00d9\u00cf\3\2\2\2\u00d9\u00d5\3\2\2\2\u00d9"+
		"\u00d8\3\2\2\2\u00daE\3\2\2\2\u00db\u00dc\7g\2\2\u00dc\u00dd\7z\2\2\u00dd"+
		"\u00de\7k\2\2\u00de\u00df\7u\2\2\u00df\u00e0\7v\2\2\u00e0\u00e3\7u\2\2"+
		"\u00e1\u00e3\7\u2205\2\2\u00e2\u00db\3\2\2\2\u00e2\u00e1\3\2\2\2\u00e3"+
		"G\3\2\2\2\u00e4\u00e5\7\60\2\2\u00e5I\3\2\2\2\u00e6\u00e7\7\u2612\2\2"+
		"\u00e7K\3\2\2\2\u00e8\u00ec\t\16\2\2\u00e9\u00eb\t\t\2\2\u00ea\u00e9\3"+
		"\2\2\2\u00eb\u00ee\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed"+
		"M\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ef\u00f3\t\6\2\2\u00f0\u00f2\t\t\2\2"+
		"\u00f1\u00f0\3\2\2\2\u00f2\u00f5\3\2\2\2\u00f3\u00f1\3\2\2\2\u00f3\u00f4"+
		"\3\2\2\2\u00f4O\3\2\2\2\u00f5\u00f3\3\2\2\2\u00f6\u00f8\t\4\2\2\u00f7"+
		"\u00f9\t\5\2\2\u00f8\u00f7\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00f8\3\2"+
		"\2\2\u00fa\u00fb\3\2\2\2\u00fbQ\3\2\2\2\u00fc\u00fe\t\17\2\2\u00fd\u00fc"+
		"\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u00fd\3\2\2\2\u00ff\u0100\3\2\2\2\u0100"+
		"S\3\2\2\2\27\2\3X`jpv\u0096\u00a1\u00a6\u00b1\u00b7\u00c0\u00c5\u00d9"+
		"\u00e2\u00ec\u00f3\u00fa\u00fd\u00ff\5\b\2\2\7\3\2\6\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}