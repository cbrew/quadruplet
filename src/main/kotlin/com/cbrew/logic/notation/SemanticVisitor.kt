package com.cbrew.logic.notation

import com.cbrew.unify.*
import java.util.*




class SemanticVisitor : LogicTermsBaseVisitor<Tree>() {

    private fun makeIndividual(text: String) = Leaf(text, "I")
private fun makePredicate(text: String) = Leaf(text, "P")
private fun makeConstant(text: String) = Leaf(text, "C")
private fun makeArgument(text: String) = Leaf(text, "A")
private fun makeVariable(text: String) = Leaf(text, "V")

    private fun makeBindingNode(subtype: String, arguments: List<String>, body: Tree): Node {
        val children = arguments.map { makeArgument(it) }
        return Node(subtype, children + listOf(body))
    }


    fun Tree.isBindable(bindingStack: List<Pair<String, String>>): Boolean =
            (this is Leaf) && bindingStack.any { label == it.first }


    /**
     * Converts raw notation to internal Lambda data structure, handling
     * binding and so on.
     * Binding stack is a list of pairs whose first element is a name and
     * whose second elements specifies whether it is a quantifier binding
     * or a lambda binding
     */

    fun Leaf.bind(bindingStack: List<Pair<String, String>>): Lambda {
        var qCounter = 1
        var lCounter = 1

        for (element in bindingStack) {
            if (element.first == label) {
                if (element.second == "Q") {
                    return QVar(qCounter)
                } else {
                    return Var(lCounter)
                }
            } else if (element.second == "Q") {
                qCounter++
            } else {
                lCounter++
            }
        }

        throw Exception("Unexpected unbound variable")
    }


    private fun orYield(item: Lambda): Set<Lambda> =
            when (item) {
                is Or -> flattenOr(item.disjuncts)
                else -> setOf(item)
            }

    private fun andYield(item: Lambda): Set<Lambda> =
            when (item) {
                is And -> flattenAnd(item.conjuncts)
                else -> setOf(item)
            }


    private fun flattenAnd(conjuncts: Set<Lambda>): Set<Lambda> = conjuncts.flatMap(::andYield).toSet()
private fun flattenOr(disjuncts: Set<Lambda>): Set<Lambda> = disjuncts.flatMap(::orYield).toSet()


    fun binarize(e: Tree): Lambda {
        // binding stack is managed explicitly
        val bindingStack = LinkedList<Pair<String, String>>()
        fun bin(n: Tree): Lambda {
            return when (n) {
                is Leaf ->
                    if (n.isBindable(bindingStack))
                        n.bind(bindingStack)
                    else
                        when (n.type) {
                            "Box" -> Box
                            "V" -> FstructVar(n.label)
                            else -> Constant(n.label)
                        }
                is Node ->
                    when (n.label) {
                        "and" -> And(flattenAnd(n.children.map(::bin).toSet()))
                        "or" -> Or(flattenOr(n.children.map(::bin).toSet()))
                        "exists" -> {
                            val arguments = n.children.subList(0, n.children.size - 1)
                            val body = n.children.last()
                            for (child in arguments) {
                                bindingStack.push(Pair((child as Leaf).label, "Q"))
                            }
                            var r = bin(body)
                            for (child in arguments) {
                                r = Exists(r)
                                bindingStack.pop()
                            }
                            return r
                        }
                        "forall" -> {
                            val arguments = n.children.subList(0, n.children.size - 1)
                            val body = n.children.last()
                            for (child in arguments) {
                                bindingStack.push(Pair((child as Leaf).label, "Q"))
                            }
                            var r = bin(body)
                            for (child in arguments) {
                                r = Forall(r)
                                bindingStack.pop()
                            }
                            return r
                        }

                        "lambda" -> {
                            val arguments = n.children.subList(0, n.children.size - 1)
                            val body = n.children.last()
                            for (child in arguments) {
                                bindingStack.push(Pair((child as Leaf).label, "L"))
                            }
                            var r = bin(body)
                            for (child in n.children.subList(0, n.children.size - 1)) {
                                r = Lam(r)
                                bindingStack.pop()
                            }
                            return r
                        }
                        "application" -> makeApp(n.children.map { bin(it) })

                        "iff" -> Equiv(bin(n.children.get(0)), bin(n.children.get(1)))
                        "implies" -> Implies(bin(n.children.get(0)), bin(n.children.get(1)))
                        "negated" -> Not(bin(n.children.get(0)))
                        "equals" -> Equiv(bin(n.children.get(0)), bin(n.children.get(1)))
                        "not_equals" -> Not(Equiv(bin(n.children.get(0)), bin(n.children.get(1))))
                        else -> throw Exception("Unexpected label on Node: " + n.label)
                    }
            }
        }
        return bin(e)
    }

    fun makeApp(children: List<Lambda>): Lambda {
        return makeApp(children[0],
                children.subList(1, children.size))
    }

    fun makeApp(pred: Lambda, args: List<Lambda>): Lambda {
        var result = pred
        for (arg in args) {
            result = createApp(result, arg)
        }
        return result
}


    override fun visitIndividual(ctx: LogicTermsParser.IndividualContext): Tree = makeIndividual(ctx.text)
    override fun visitPredicate(ctx: LogicTermsParser.PredicateContext): Tree = makePredicate(ctx.text)
    // ?x variables
    override fun visitVariable(ctx: LogicTermsParser.VariableContext): Tree = makeVariable(ctx.text)

    override fun visitConstantExpr(ctx: LogicTermsParser.ConstantExprContext): Tree = makeConstant(ctx.text)


    override fun visitLambdaExpression(ctx: LogicTermsParser.LambdaExpressionContext?): Tree {
        return makeBindingNode("lambda", ctx?.larg()?.map { it.text } ?: listOf(),
                visit(ctx?.expression()))
    }

    override fun visitApplication(ctx: LogicTermsParser.ApplicationContext?): Tree {
        val predList = listOf(visit(ctx?.expression()))
        val argList = ctx?.applicationTail()?.expression()?.map(::visit) ?: emptyList()
        return Node(label = "application", children = predList + argList)
    }


    override fun visitForallExpression(ctx: LogicTermsParser.ForallExpressionContext?): Tree {
        return makeBindingNode("forall", ctx?.argument()?.map { it.text } ?: listOf(),
                visit(ctx?.expression()))
    }


    override fun visitExistsExpression(ctx: LogicTermsParser.ExistsExpressionContext?): Tree {
        return makeBindingNode("exists", ctx?.argument()?.map { it.text } ?: listOf(),
                visit(ctx?.expression()))
    }


    override fun visitAnd(ctx: LogicTermsParser.AndContext?): Tree {
        return Node("and", ctx?.expression()?.map { visit(it) } ?: emptyList())
    }

    override fun visitOr(ctx: LogicTermsParser.OrContext?): Tree {
        return Node("or", ctx?.expression()?.map { visit(it) } ?: emptyList())
    }

    override fun visitNegated(ctx: LogicTermsParser.NegatedContext?): Tree {
        return Node("negated", listOf(visit(ctx?.negation()?.expression())))
    }

    override fun visitRelational(ctx: LogicTermsParser.RelationalContext?): Tree {
        val op = ctx?.relationTail()?.boolOp()?.text ?: "default"
        val standard_op = when (op) {
            "->" -> "implies"
            "\u2192" -> "implies"
            "<->" -> "iff"
            "\u2194" -> "iff"
            "!=" -> "not_equals"
            "<>" -> "not_equals"
            "\u2260" -> "not_equals"
            "==" -> "equals"
            "=" -> "equals"
            else -> "???"
        }

        return Node(standard_op, listOf(visit(ctx?.expression()), visit(ctx?.relationTail()?.expression())))
    }

    override fun visitParenthesized(ctx: LogicTermsParser.ParenthesizedContext?): Tree =
            visit(ctx?.parenthesizedExpression()?.expression())

    override fun visitBox(ctx: LogicTermsParser.BoxContext?): Tree {
        return Leaf("", type = "Box")
    }

}
