package com.cbrew.logic.notation


import com.cbrew.unify.Lambda
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree


object LogicParser {


    fun getParser(s: String): LogicTermsParser {
        return getLogicTermsParser(s)
    }

    private fun getLogicTermsParser(s: String): LogicTermsParser {
        val input = CharStreams.fromString(s)
        val lexer = LogicTermsLexer(input)
        val tokens = CommonTokenStream(lexer)
        return LogicTermsParser(tokens)
    }

    /**
     * Parses the string.
     *
     * @param s the s
     * @return the parses the tree
     */
    fun parse(s: String): ParseTree {
        val parser = getLogicTermsParser(s)
        return parser.expression()
    }

    fun toLogic(tree: String): Lambda {
        val visitor = SemanticVisitor()
        val parsed = parse(tree)
        return visitor.binarize(visitor.visit(parsed))
    }


}
