package com.cbrew.fstruct.notation

import com.cbrew.unify.FeatureStructure
import com.cbrew.unify.Grammar
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream


object FeatureNotation {
    fun toFs(s: String): FeatureStructure {
        val input = CharStreams.fromString(s)
        val visitor = FeatureNotationVisitor()
        val lexer = FeatureTermsLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = FeatureTermsParser(tokens)
        return visitor.visit(parser.expression())
    }


    fun toGrammar(s: String): Grammar {
        val input = CharStreams.fromString(s)
        val visitor = FeatureNotationVisitor()
        val lexer = FeatureTermsLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = FeatureTermsParser(tokens)
        return visitor.visit(parser.cfg()) as Grammar

    }


}