package com.cbrew.fstruct.notation

import com.cbrew.unify.Unifiable
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

object IntegratedParser {
    fun toTree(s: String): String {
        val input = CharStreams.fromString(s)
        val lexer = FeatLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = FeatParser(tokens)
        return parser.cfg().toStringTree(parser)
    }

    fun toGrammar(s: String): Unifiable {
        val input = CharStreams.fromString(s)
        val lexer = FeatLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = FeatParser(tokens)
        val visitor = IntegratedVisitor()
        val g1 = visitor.visit(parser.cfg())
        return g1
    }

}