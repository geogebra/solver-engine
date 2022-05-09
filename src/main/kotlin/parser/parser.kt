package parser

import expressions.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.geogebra.solver.parser.ExpressionBaseVisitor
import org.geogebra.solver.parser.ExpressionLexer
import org.geogebra.solver.parser.ExpressionParser

fun parseExpression(text: String): Expression {
    val lexer = ExpressionLexer(CharStreams.fromString(text))
    val parser = ExpressionParser(CommonTokenStream(lexer))
    val visitor = ExpressionVisitor()
    return visitor.visit(parser.expr())
}


private class ExpressionVisitor : ExpressionBaseVisitor<Expression>() {

    override fun visitExpr(ctx: ExpressionParser.ExprContext?): Expression {
        return visit(ctx?.getChild(0))
    }

    override fun visitSum(ctx: ExpressionParser.SumContext?): Expression {
        val first = visit(ctx!!.first)
        val rest = ctx.rest.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        val terms = mutableListOf<Expression>(first)
        terms.addAll(rest)
        return NaryExpr(NaryOperator.Sum, terms)
    }

    override fun visitFirstTerm(ctx: ExpressionParser.FirstTermContext?): Expression {
        val p = visit(ctx!!.product())
        if (ctx.sign == null) {
            return p
        }
        return when (ctx.sign.text) {
            "+" -> UnaryExpr(UnaryOperator.Plus, p)
            else -> UnaryExpr(UnaryOperator.Minus, p)
        }
    }

    override fun visitOtherTerm(ctx: ExpressionParser.OtherTermContext?): Expression {
        val p = visit(ctx!!.product())
        return when (ctx.sign.text) {
            "+" -> p
            else -> UnaryExpr(UnaryOperator.Minus, p)
        }
    }

    override fun visitProduct(ctx: ExpressionParser.ProductContext?): Expression {
        return visit(ctx?.getChild(0))
    }

    override fun visitExplicitProduct(ctx: ExpressionParser.ExplicitProductContext?): Expression {
        val factors = ctx!!.products.map { visit(it) }
        return NaryExpr(NaryOperator.Product, factors)
    }

    override fun visitImplicitProduct(ctx: ExpressionParser.ImplicitProductContext?): Expression {
        val first = visit(ctx!!.first)
        val rest = ctx!!.others.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        val factors = mutableListOf<Expression>(first)
        factors.addAll(rest)
        return NaryExpr(NaryOperator.ImplicitProduct, factors)
    }

    override fun visitNonNumericFactor(ctx: ExpressionParser.NonNumericFactorContext?): Expression {
        return visit(ctx?.getChild(0))
    }

    override fun visitFraction(ctx: ExpressionParser.FractionContext?): Expression {
        return fractionOf(visit(ctx!!.num), visit(ctx!!.den))
    }

    override fun visitFactor(ctx: ExpressionParser.FactorContext?): Expression {
        return visit(ctx?.getChild(-0))
    }

    override fun visitPower(ctx: ExpressionParser.PowerContext?): Expression {
        return powerOf(visit(ctx!!.base), visit(ctx!!.exp))
    }

    override fun visitBracket(ctx: ExpressionParser.BracketContext?): Expression {
        return bracketOf(visit(ctx!!.expr()))
    }

    override fun visitAtom(ctx: ExpressionParser.AtomContext?): Expression {
        return visit(ctx?.getChild(-0))
    }

    override fun visitNonNumericAtom(ctx: ExpressionParser.NonNumericAtomContext?): Expression {
        return visit(ctx?.getChild(-0))
    }

    override fun visitNaturalNumber(ctx: ExpressionParser.NaturalNumberContext?): Expression {
        return IntegerExpr(ctx!!.NATNUM().text.toBigInteger())
    }

    override fun visitVariable(ctx: ExpressionParser.VariableContext?): Expression {
        return VariableExpr(ctx!!.VARIABLE().text)
    }
}