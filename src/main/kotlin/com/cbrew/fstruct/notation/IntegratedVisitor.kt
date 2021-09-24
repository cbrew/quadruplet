package com.cbrew.fstruct.notation

import com.cbrew.logic.notation.Leaf
import com.cbrew.logic.notation.LogicTermsParser
import com.cbrew.logic.notation.Node
import com.cbrew.unify.*
import com.cbrew.logic.notation.SemanticVisitor
import java.util.*



class IntegratedVisitor : FeatParserBaseVisitor<Unifiable>() {



    override fun visitCfg(ctx: FeatParser.CfgContext?): Unifiable {
        val cfgrules = ctx?.cfgrule()?.map(::visit)?.map { it as CfgRule }?.toSet() ?: setOf()
        val mcfgrules = ctx?.mcfgrule()?.map(::visit)?.map { it as McfgRule }?.toSet() ?: setOf()

        // lexentries may be in word: term format
        val lex1 = ctx
                ?.lexentry()
                ?.map {
                    Pair(
                            it.word().text.substring(1, it.word().text.length - 1),
                            it.featureMap().map { visit(it) as FeatureMap }.toSet())
                }
                ?: listOf()

        // or embedded in CFG rules
        val lex2 = cfgrules.flatMap {cfg -> cfg.words.map {
            Pair(it.toString(), listOf(cfg.lhs))
        } }

        val lexentries = lex1 + lex2

        val lexMap = lexentries.groupingBy { it.first }.fold(setOf(), { R: Set<FeatureMap>, E -> R + E.second })


        return Grammar(cfgrules.filter {it.rhs.size > 0} .toSet(), lexMap)
    }

    override fun visitWord(ctx: FeatParser.WordContext?): Unifiable {
        return Constant(ctx?.Word()?.text ?: "xxx")
    }

    override fun visitCfgrule(ctx: FeatParser.CfgruleContext?): Unifiable {
        val lhs: FeatureMap = visit(ctx?.featureMap()) as FeatureMap
        val rhs: List<FeatureMap> = (ctx?.cfgrhs()?.rhspart()?.flatMap { rhs ->
            rhs?.featureMap()?.map(::visit)
                    ?: listOf()
        } ?: listOf()).map { it as FeatureMap }

        // we'll include an extra field in CfgRules for the lexical RHS elements
        val words =
            ctx?.cfgrhs()?.rhspart()?.flatMap { rhs -> rhs?.word()?.map(::visit) ?: listOf()}
                ?: listOf()

        // add in the words to the CfgRule.
        return CfgRule(lhs, rhs, words)
    }





    override fun visitMcfgrule(ctx: FeatParser.McfgruleContext?): Unifiable {
        val lhs: FeatureMap = visit(ctx?.featureMap()) as FeatureMap
        val rhs: List<FeatureMap> = (ctx?.mcfgrhs()?.mcfgrhspart()?.flatMap { rhs ->
            rhs?.featureMap()?.map(::visit)
                    ?: listOf()
        } ?: listOf()).map { it as FeatureMap }
        val linseqs = ctx?.mcfgrhs()?.mcfgrhspart()?.flatMap { r -> r.linseq().map(::visit) } ?: listOf()

        return McfgRule(lhs, rhs, FeatureList(linseqs))
    }

    override fun visitFeatureMap(ctx: FeatParser.FeatureMapContext?): Unifiable {
        val cat = ctx?.Category()?.text ?: "??"
        val fs: Map<String, Unifiable> = ctx?.mapping()?.fpair()?.map { fp ->
            Pair(
                    fp.Fname().text,
                    visit(fp.fvalue()))
        }?.toMap() ?: mapOf()
        return FeatureMap(fs + Pair("cat", AtomicValue(cat)))
    }



    override fun visitLinseq(ctx: FeatParser.LinseqContext?): Unifiable =
            FeatureList(ctx?.numseq()?.map(::visit) ?: listOf())

    override fun visitNumseq(ctx: FeatParser.NumseqContext?): Unifiable =
            FeatureList(ctx?.Number()?.map { n -> Integer(n.text.toInt()) } ?: listOf())

    override fun visitFvalue(ctx: FeatParser.FvalueContext?): Unifiable =
            if (ctx?.Fname() != null)
                AtomicValue(ctx.Fname().text)
            else if (ctx?.FstructVariable() != null)
                QueryVariable(ctx.FstructVariable().text)
            else if (ctx?.semantics() != null)
                SemanticValue(visit(ctx.semantics()) as Lambda)
            else if (ctx?.flist()?.fvalue() != null)
                FeatureList(ctx.flist().fvalue().map(::visit))
            else
                throw Exception("unexpected form of fvalue")

    override fun visitSemantics(ctx: FeatParser.SemanticsContext?): Unifiable {
        val logicVisitor = LogicVisitor()
        val q = ctx?.expression()
        val t = logicVisitor.visit(q)
        return logicVisitor.binarize(t)
    }

    override fun visitVariable(ctx: FeatParser.VariableContext?): Unifiable {
        return Constant(ctx?.text ?: "??")
    }



    override fun visitConstant(ctx: FeatParser.ConstantContext?): Unifiable {
        return Constant(ctx?.text ?: "??")
    }



}