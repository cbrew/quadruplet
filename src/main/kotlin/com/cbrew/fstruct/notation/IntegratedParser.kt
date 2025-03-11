package com.cbrew.fstruct.notation

import com.cbrew.unify.Unifiable
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

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

}