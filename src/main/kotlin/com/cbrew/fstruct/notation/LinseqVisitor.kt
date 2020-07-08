package com.cbrew.fstruct.notation

import com.cbrew.unify.FeatureList
import com.cbrew.unify.FeatureStructure
import com.cbrew.unify.Integer

class LinseqVisitor : LinearizationBaseVisitor<FeatureStructure>() {
    override fun visitNumseqs(ctx: LinearizationParser.NumseqsContext?): FeatureStructure {
        return FeatureList(ctx?.numseq()?.map(::visit) ?: listOf())
    }

    override fun visitNumseq(ctx: LinearizationParser.NumseqContext?): FeatureStructure {
        return FeatureList(ctx?.numbers()?.Number()?.map { n -> Integer(n.text.toInt()) } ?: listOf())
    }
}