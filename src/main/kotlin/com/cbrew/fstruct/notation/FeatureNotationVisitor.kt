package com.cbrew.fstruct.notation

import com.cbrew.logic.notation.LogicParser
import com.cbrew.unify.*

class FeatureNotationVisitor : FeatureTermsBaseVisitor<FeatureStructure>() {

    override fun visitExpression(ctx: FeatureTermsParser.ExpressionContext?): FeatureStructure {
        if (ctx?.mapping() == null)
            return FeatureMap(mapOf(Pair("cat", AtomicValue(ctx?.categoryExpression()?.text ?: "default text"))))
        else {
            val subMap: FeatureMap = (visit(ctx.mapping()) as FeatureMap)
            return FeatureMap(subMap +
                    mapOf(Pair("cat", AtomicValue(ctx.categoryExpression()?.text ?: "default text"))))
        }
    }

    private fun makeMaplet(ctx: FeatureTermsParser.FpairContext): Pair<String, FeatureStructure> =
            Pair(ctx.Fname().text, visit(ctx.fvalue()))

    override fun visitMapping(ctx: FeatureTermsParser.MappingContext?): FeatureStructure {
        val pairs: List<Pair<String, FeatureStructure>> = ctx?.fpair()?.map(::makeMaplet) ?: listOf()
        return FeatureMap(mapOf(*pairs.toTypedArray()))

    }


    override fun visitFvalue(ctx: FeatureTermsParser.FvalueContext?): FeatureStructure =
            if (ctx?.Fname() != null)
                AtomicValue(ctx.Fname().text)
            else if (ctx?.FstructVariable() != null)
                QueryVariable(ctx.FstructVariable().text)
            else if (ctx?.semantics()?.text != null)
                SemanticValue(LogicParser.toLogic(ctx.semantics().text.substring(1, ctx.semantics().text.length - 1)))
            else if (ctx?.flist()?.fvalue() != null)
                FeatureList(ctx.flist().fvalue().map(::visit))
            else
                throw Exception("unexpected form of fvalue")


    override fun visitCfg(ctx: FeatureTermsParser.CfgContext?): FeatureStructure {
        val lexentries = ctx
                ?.lexentry()
                ?.map {
                    Pair(
                            it.word().text.substring(1, it.word().text.length - 1),
                            it.expression().map { visit(it) as FeatureMap }.toSet())
                }
                ?: listOf()
        val lexMap = lexentries.groupingBy { it.first }.fold(setOf(), { R: Set<FeatureMap>, E -> R + E.second })
        val cfgrules = ctx?.cfgrule()?.map(::visit)
        val mcfgrules = ctx?.mcfgrule()?.map(::visit)

        val rules: Set<Unifiable> =
                (cfgrules?.flatMap { rhs -> (rhs as FeatureList).elements }?.toSet() ?: setOf()) +
                        (mcfgrules?.flatMap { rhs -> (rhs as FeatureList).elements }?.toSet() ?: setOf())


        return Grammar(
                rules = rules.map { it as Rule }.toSet(),
                lexicon = lexMap)

    }

    override fun visitLexentry(ctx: FeatureTermsParser.LexentryContext?): FeatureStructure {
        val key = ctx?.word()?.text ?: "???"
        val pairs = ctx?.expression()?.map { Pair(key, visit(it)) } ?: listOf()
        return FeatureMap(mapOf(*pairs.toTypedArray()))
    }

    override fun visitMcfgrhspart(ctx: FeatureTermsParser.McfgrhspartContext?): FeatureStructure =
            FeatureMap(mapOf(
                    Pair("rhs", FeatureList(ctx?.expression()?.map(::visit) ?: listOf())),
                    Pair("linearization", visit(ctx?.linseq()))))

    override fun visitRhspart(ctx: FeatureTermsParser.RhspartContext?): FeatureStructure =
            FeatureList(ctx?.expression()?.map(::visit) ?: listOf())


    override fun visitCfgrule(ctx: FeatureTermsParser.CfgruleContext?): FeatureStructure {
        val lhs: FeatureMap = visit(ctx?.expression()) as FeatureMap
        val rhses: List<FeatureList> = ctx?.cfgrhs()?.rhspart()?.map { visit(it) as FeatureList } ?: listOf()
        val cfgrules: List<FeatureStructure> = rhses.map { rhs: FeatureList ->
            CfgRule(
                lhs,
                rhs.elements.map { it as FeatureMap },
                listOf()) }

        return FeatureList(cfgrules)

    }


    override fun visitMcfgrule(ctx: FeatureTermsParser.McfgruleContext?): FeatureStructure {
        val lhs: FeatureMap = visit(ctx?.expression()) as FeatureMap
        val rhses: List<FeatureStructure> = ctx?.mcfgrhs()?.mcfgrhspart()?.map(::visit) ?: listOf()
        val mcfgrules: List<FeatureStructure> = rhses.map { rhs ->
            val rm = rhs as FeatureMap
            val elements = (rm["rhs"] as FeatureList).elements.map { it as FeatureMap }
            val linseq = rm["linearization"] as FeatureList
            McfgRule(lhs, elements, linseq)
        }
        return FeatureList(mcfgrules)
    }

    override fun visitLinseq(ctx: FeatureTermsParser.LinseqContext?): FeatureStructure {
        return FeatureList(ctx?.Sem()?.map { LinseqParser.toSequence(it.text.substring(1, it.text.length - 1)) }
                ?: listOf())
    }
}