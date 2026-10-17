package com.cbrew.fstruct.notation

import com.cbrew.unify.Lambda
import com.cbrew.unify.Unifiable
import com.cbrew.unify.normalized
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

object IntegratedParser {
    fun toTree(s: String): String {
        // Validate and sanitize input
        if (s.isEmpty()) {
            throw IllegalArgumentException("Empty input string")
        }
        
        try {
            val input = CharStreams.fromString(s)
            val lexer = FeatLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = FeatParser(tokens)
            return parser.cfg().toStringTree(parser)
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
            val input = CharStreams.fromString(s)
            val lexer = FeatLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = FeatParser(tokens)
            val visitor = IntegratedVisitor()
            val g1 = visitor.visit(parser.cfg())
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
