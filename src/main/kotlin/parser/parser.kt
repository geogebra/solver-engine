package parser

import engine.expressions.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import parser.antlr.ExpressionBaseVisitor
import parser.antlr.ExpressionLexer
import parser.antlr.ExpressionParser

fun invisibleBracketOf(operand: Expression) = Expression(UnaryOperator.InvisibleBracket, listOf(operand))

fun parseExpression(text: String): Expression {
    val lexer = ExpressionLexer(CharStreams.fromString(text))
    val parser = ExpressionParser(CommonTokenStream(lexer))
    val visitor = ExpressionVisitor()
    return visitor.visit(parser.expr())
}

private fun makeExpression(operator: Operator, operands: List<Expression>) = Expression(
    operator,
    operands.mapIndexed() { i, operand ->
        when {
            operator.nthChildAllowed(i, operand.operator) -> operand
            else -> invisibleBracketOf(operand)
        }
    }
)

private fun makeExpression(operator: Operator, vararg operands: Expression) =
    makeExpression(operator, operands.asList())


private class ExpressionVisitor : ExpressionBaseVisitor<Expression>() {

    override fun visitExpr(ctx: ExpressionParser.ExprContext?): Expression {
        return visit(ctx?.getChild(0))
    }

    override fun visitSum(ctx: ExpressionParser.SumContext?): Expression {
        val first = visit(ctx!!.first)
        val signedFirst = if (ctx.sign == null) first else when (ctx.sign.text) {
            "+" -> makeExpression(UnaryOperator.Plus, first)
            else -> makeExpression(UnaryOperator.Minus, first)
        }
        val rest = ctx.rest.map { visit(it) }
        if (rest.isEmpty()) {
            return signedFirst
        }
        val terms = mutableListOf<Expression>(signedFirst)
        terms.addAll(rest)
        return makeExpression(NaryOperator.Sum, terms)
    }

    override fun visitOtherTerm(ctx: ExpressionParser.OtherTermContext?): Expression {
        val p = visit(ctx!!.explicitProduct())
        return when (ctx.sign.text) {
            "+" -> if (UnaryOperator.Plus.childAllowed(p.operator)) p else invisibleBracketOf(p)
            else -> makeExpression(UnaryOperator.Minus, p)
        }
    }

    override fun visitExplicitProduct(ctx: ExpressionParser.ExplicitProductContext?): Expression {
        val first = visit(ctx!!.first)
        val rest = ctx.rest.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        val factors = mutableListOf<Expression>(first)
        factors.addAll(rest)
        return makeExpression(NaryOperator.Product, factors)
    }

    override fun visitOtherExplicitFactor(ctx: ExpressionParser.OtherExplicitFactorContext?): Expression {
        val p = visit(ctx!!.implicitProduct())
        return when (ctx.op.text) {
            "*" -> p
            else -> makeExpression(UnaryOperator.DivideBy, p)
        }
    }

    override fun visitImplicitProduct(ctx: ExpressionParser.ImplicitProductContext?): Expression {
        val first = visit(ctx!!.first)
        val rest = ctx.others.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        val factors = mutableListOf<Expression>(first)
        factors.addAll(rest)
        return makeExpression(NaryOperator.ImplicitProduct, factors)
    }

    override fun visitFirstFactorWithSign(ctx: ExpressionParser.FirstFactorWithSignContext?): Expression {
        val factor = visit(ctx!!.factor)
        val operator = when (ctx.sign.text) {
            "+" -> UnaryOperator.Plus
            else -> UnaryOperator.Minus
        }
        return makeExpression(operator, factor)
    }

    override fun visitFraction(ctx: ExpressionParser.FractionContext?): Expression {
        return makeExpression(BinaryOperator.Fraction, visit(ctx!!.num), visit(ctx.den))
    }

    override fun visitPower(ctx: ExpressionParser.PowerContext?): Expression {
        return makeExpression(BinaryOperator.Power, visit(ctx!!.base), visit(ctx.exp))
    }

    override fun visitRoundBracket(ctx: ExpressionParser.RoundBracketContext?): Expression {
        return makeExpression(BracketOperator.Bracket, visit(ctx!!.expr()))
    }

    override fun visitSquareBracket(ctx: ExpressionParser.SquareBracketContext?): Expression {
        return makeExpression(BracketOperator.SquareBracket, visit(ctx!!.expr()))
    }

    override fun visitCurlyBracket(ctx: ExpressionParser.CurlyBracketContext?): Expression {
        return makeExpression(BracketOperator.CurlyBracket, visit(ctx!!.expr()))
    }

    override fun visitMixedNumber(ctx: ExpressionParser.MixedNumberContext?): Expression {
        return mixedNumber(
            ctx!!.integer.text.toBigInteger(),
            ctx.num.text.toBigInteger(),
            ctx.den.text.toBigInteger(),
        )
    }

    override fun visitNaturalNumber(ctx: ExpressionParser.NaturalNumberContext?): Expression {
        return xp(ctx!!.NATNUM().text.toBigInteger())
    }

    override fun visitVariable(ctx: ExpressionParser.VariableContext?): Expression {
        return xp(ctx!!.VARIABLE().text)
    }
}