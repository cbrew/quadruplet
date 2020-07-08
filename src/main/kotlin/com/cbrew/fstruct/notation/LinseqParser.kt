package com.cbrew.fstruct.notation

import com.cbrew.unify.FeatureStructure
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

object LinseqParser {
    fun toSequence(s: String): FeatureStructure {
        val input = CharStreams.fromString(s)
        val visitor = LinseqVisitor()
        val lexer = LinearizationLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = LinearizationParser(tokens)
        val result = visitor.visit(parser.numseqs())
        return result
    }
}