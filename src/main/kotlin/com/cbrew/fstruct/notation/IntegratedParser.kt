package com.cbrew.fstruct.notation

import com.cbrew.unify.Lambda
import com.cbrew.unify.Unifiable
import com.cbrew.unify.normalized
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException

object IntegratedParser {
    /**
     * Parses [s] as a grammar file, returning the parser (for its rule
     * names) and the tree.
     *
     * The expression rule is ambiguous (how far a lambda, quantifier or
     * negation body extends), and full LL prediction re-resolves each such
     * ambiguity with the whole parser stack, which is most of the cost of
     * loading a grammar. So ANTLR's SLL prediction is tried first: when it
     * succeeds it gives the tree full LL would. Only when it fails is the
     * input parsed again with full LL and the usual error recovery, so syntax
     * errors are reported and repaired as before.
     */
    private fun parseCfg(s: String): Pair<FeatParser, FeatParser.CfgContext> {
        val tokens = CommonTokenStream(FeatLexer(CharStreams.fromString(s)))
        val sll = FeatParser(tokens).apply {
            interpreter.predictionMode = PredictionMode.SLL
            removeErrorListeners()
            errorHandler = BailErrorStrategy()
        }
        return try {
            sll to sll.cfg()
        } catch (e: ParseCancellationException) {
            tokens.seek(0)
            val ll = FeatParser(tokens)
            ll to ll.cfg()
        }
    }

    fun toTree(s: String): String {
        // Validate and sanitize input
        if (s.isEmpty()) {
            throw IllegalArgumentException("Empty input string")
        }
        
        try {
            val (parser, tree) = parseCfg(s)
            return tree.toStringTree(parser)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse input: ${e.message}", e)
        }
    }

    fun toGrammar(s: String): Unifiable {
        // Validate and sanitize input
        if (s.isEmpty()) {
            throw IllegalArgumentException("Empty input string")
        }
        
        try {
            val visitor = IntegratedVisitor()
            val g1 = visitor.visit(parseCfg(s).second)
            return g1
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse grammar: ${e.message}", e)
        }
    }

    /**
     * Reads one DRT term, the text that would sit between `<` and `>` in a
     * `%semantics drt` grammar, and returns it normalized.
     *
     * @throws IllegalArgumentException if [s] is empty or is not a well-formed
     *         term; unlike [toGrammar], a syntax error is not repaired
     */
    fun toDrs(s: String): Lambda {
        if (s.isEmpty()) {
            throw IllegalArgumentException("Empty input string")
        }

        try {
            val lexer = FeatLexer(CharStreams.fromString(s)).apply {
                pushMode(FeatLexer.DRT_ISLAND)
                removeErrorListeners()
                addErrorListener(ThrowingErrorListener)
            }
            val parser = FeatParser(CommonTokenStream(lexer)).apply {
                removeErrorListeners()
                addErrorListener(ThrowingErrorListener)
            }
            val logicVisitor = LogicVisitor(drt = true)
            val term = logicVisitor.visit(parser.semanticTerm().expression())
            return logicVisitor.binarize(term).normalized() as Lambda
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse DRT term: ${e.message}", e)
        }
    }

    /** Turns any lexer or parser syntax error into an [IllegalArgumentException]. */
    private object ThrowingErrorListener : BaseErrorListener() {
        override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int,
                                 charPositionInLine: Int, msg: String?, e: RecognitionException?) {
            throw IllegalArgumentException("line $line:$charPositionInLine $msg")
        }
    }
}
