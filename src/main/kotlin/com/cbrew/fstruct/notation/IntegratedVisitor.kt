package com.cbrew.fstruct.notation

import com.cbrew.unify.*

class IntegratedVisitor : FeatParserBaseVisitor<Unifiable>() {


    override fun visitCfg(ctx: FeatParser.CfgContext?): Unifiable {
        val cfgrules = ctx?.cfgrule()?.map(::visit)?.map { it as CfgRule }?.toSet() ?: setOf()
        val mcfgrules = ctx?.mcfgrule()?.map(::visit)?.map { it as McfgRule }?.toSet() ?: setOf()

        val lexentries = ctx
                ?.lexentry()
                ?.map {
                    Pair(
                            it.word().text.substring(1, it.word().text.length - 1),
                            it.featureMap().map { visit(it) as FeatureMap }.toSet())
                }
                ?: listOf()
        val lexMap = lexentries.groupingBy { it.first }.fold(setOf(), { R: Set<FeatureMap>, E -> R + E.second })
        return Grammar(cfgrules + mcfgrules, lexMap)
    }

    override fun visitCfgrule(ctx: FeatParser.CfgruleContext?): Unifiable {
        val lhs: FeatureMap = visit(ctx?.featureMap()) as FeatureMap
        val rhs: List<FeatureMap> = (ctx?.cfgrhs()?.rhspart()?.flatMap { rhs ->
            rhs?.featureMap()?.map(::visit)
                    ?: listOf()
        } ?: listOf()).map { it as FeatureMap }

        return CfgRule(lhs, rhs)
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
        val q = ctx?.expression()
        return visit(q)
    }

    override fun visitVariable(ctx: FeatParser.VariableContext?): Unifiable {
        return Constant(ctx?.text ?: "??")
    }

    override fun visitConstant(ctx: FeatParser.ConstantContext?): Unifiable {
        return Constant(ctx?.text ?: "??")
    }

    override fun visitApplication(ctx: FeatParser.ApplicationContext?): Unifiable {
        val bodies = ctx?.applicationTail()?.expression()?.map { visit(it) } ?: listOf()
        var functor: Lambda = visit(ctx?.expression()) as Lambda
        bodies.subList(0, bodies.size - 1).forEach {
            functor = App(functor, it as Lambda)
        }

        return App(functor, bodies[bodies.size - 1] as Lambda)
    }

    override fun visitAnd(ctx: FeatParser.AndContext?): Unifiable = Constant("and")
    override fun visitOr(ctx: FeatParser.OrContext?): Unifiable = Constant("or")

    override fun visitExists(ctx: FeatParser.ExistsContext?): Unifiable = Constant("exists")

    override fun visitForallExpression(ctx: FeatParser.ForallExpressionContext?): Unifiable =
            Forall(visit(ctx?.expression()) as Lambda)

    override fun visitLambda(ctx: FeatParser.LambdaContext?): Unifiable =
            Constant("lambda")

    override fun visitNegated(ctx: FeatParser.NegatedContext?): Unifiable = Constant("not")

    override fun visitRelational(ctx: FeatParser.RelationalContext?): Unifiable = Constant("rel")


}