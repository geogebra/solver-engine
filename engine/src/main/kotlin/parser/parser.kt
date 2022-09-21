package parser

import engine.expressions.Expression
import engine.expressions.mixedNumber
import engine.expressions.xp
import engine.operators.BinaryExpressionOperator
import engine.operators.BracketOperator
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.UnaryExpressionOperator
import engine.operators.UndefinedOperator
import engine.utility.RecurringDecimal
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import parser.antlr.ExpressionBaseVisitor
import parser.antlr.ExpressionLexer
import parser.antlr.ExpressionParser
import java.math.BigDecimal

fun invisibleBracketOf(operand: Expression) = Expression(UnaryExpressionOperator.InvisibleBracket, listOf(operand))

fun parseExpression(text: String): Expression {
    val lexer = ExpressionLexer(CharStreams.fromString(text))
    val parser = ExpressionParser(CommonTokenStream(lexer))
    parser.errorHandler = BailErrorStrategy()
    val visitor = ExpressionVisitor()
    return visitor.visit(parser.wholeInput())
}

private fun makeExpression(operator: Operator, operands: List<Expression>) = Expression(
    operator,
    operands.mapIndexed { i, operand ->
        when {
            operator.nthChildAllowed(i, operand.operator) -> operand
            else -> invisibleBracketOf(operand)
        }
    }
)

private fun makeExpression(operator: Operator, vararg operands: Expression) =
    makeExpression(operator, operands.asList())

@Suppress("TooManyFunctions")
private class ExpressionVisitor : ExpressionBaseVisitor<Expression>() {

    override fun visitWholeInput(ctx: ExpressionParser.WholeInputContext): Expression {
        return visit(ctx.getChild(0))
    }

    override fun visitEquationSystem(ctx: ExpressionParser.EquationSystemContext): Expression {
        return makeExpression(EquationSystemOperator, ctx.equations.map { visit(it) })
    }

    override fun visitEquation(ctx: ExpressionParser.EquationContext): Expression {
        return makeExpression(EquationOperator, visit(ctx.lhs), visit((ctx.rhs)))
    }

    override fun visitExpr(ctx: ExpressionParser.ExprContext): Expression {
        return visit(ctx.getChild(0))
    }

    override fun visitSum(ctx: ExpressionParser.SumContext): Expression {
        val first = visit(ctx.first)
        val signedFirst = if (ctx.sign == null) first else when (ctx.sign.text) {
            "+" -> makeExpression(UnaryExpressionOperator.Plus, first)
            else -> makeExpression(UnaryExpressionOperator.Minus, first)
        }
        val rest = ctx.rest.map { visit(it) }
        if (rest.isEmpty()) {
            return signedFirst
        }
        val terms = mutableListOf<Expression>(signedFirst)
        terms.addAll(rest)
        return makeExpression(NaryOperator.Sum, terms)
    }

    override fun visitOtherTerm(ctx: ExpressionParser.OtherTermContext): Expression {
        val p = visit(ctx.explicitProduct())
        return when (ctx.sign.text) {
            "+" -> if (UnaryExpressionOperator.Plus.childAllowed(p.operator)) p else invisibleBracketOf(p)
            else -> makeExpression(UnaryExpressionOperator.Minus, p)
        }
    }

    override fun visitExplicitProduct(ctx: ExpressionParser.ExplicitProductContext): Expression {
        val first = visit(ctx.first)
        val rest = ctx.rest.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        val factors = mutableListOf<Expression>(first)
        factors.addAll(rest)
        return makeExpression(NaryOperator.Product, factors)
    }

    override fun visitOtherExplicitFactor(ctx: ExpressionParser.OtherExplicitFactorContext): Expression {
        val p = visit(ctx.implicitProduct())
        return when (ctx.op.text) {
            "*" -> p
            else -> makeExpression(UnaryExpressionOperator.DivideBy, p)
        }
    }

    override fun visitImplicitProduct(ctx: ExpressionParser.ImplicitProductContext): Expression {
        val first = visit(ctx.first)
        val rest = ctx.others.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        val factors = mutableListOf<Expression>(first)
        factors.addAll(rest)
        return makeExpression(NaryOperator.ImplicitProduct, factors)
    }

    override fun visitFirstFactorWithSign(ctx: ExpressionParser.FirstFactorWithSignContext): Expression {
        val factor = visit(ctx.factor)
        val operator = when (ctx.sign.text) {
            "+" -> UnaryExpressionOperator.Plus
            else -> UnaryExpressionOperator.Minus
        }
        return makeExpression(operator, factor)
    }

    override fun visitFraction(ctx: ExpressionParser.FractionContext): Expression {
        return makeExpression(BinaryExpressionOperator.Fraction, visit(ctx.num), visit(ctx.den))
    }

    override fun visitPower(ctx: ExpressionParser.PowerContext): Expression {
        return makeExpression(BinaryExpressionOperator.Power, visit(ctx.base), visit(ctx.exp))
    }

    override fun visitSqrt(ctx: ExpressionParser.SqrtContext): Expression {
        return makeExpression(UnaryExpressionOperator.SquareRoot, visit(ctx.radicand))
    }

    override fun visitRoot(ctx: ExpressionParser.RootContext): Expression {
        return makeExpression(BinaryExpressionOperator.Root, visit(ctx.radicand), visit(ctx.order))
    }

    override fun visitRoundBracket(ctx: ExpressionParser.RoundBracketContext): Expression {
        return makeExpression(BracketOperator.Bracket, visit(ctx.expr()))
    }

    override fun visitSquareBracket(ctx: ExpressionParser.SquareBracketContext): Expression {
        return makeExpression(BracketOperator.SquareBracket, visit(ctx.expr()))
    }

    override fun visitCurlyBracket(ctx: ExpressionParser.CurlyBracketContext): Expression {
        return makeExpression(BracketOperator.CurlyBracket, visit(ctx.expr()))
    }

    override fun visitMixedNumber(ctx: ExpressionParser.MixedNumberContext): Expression {
        return mixedNumber(
            ctx.integer.text.toBigInteger(),
            ctx.num.text.toBigInteger(),
            ctx.den.text.toBigInteger(),
        )
    }

    override fun visitNaturalNumber(ctx: ExpressionParser.NaturalNumberContext): Expression {
        return xp(ctx.NATNUM().text.toBigInteger())
    }

    override fun visitDecimalNumber(ctx: ExpressionParser.DecimalNumberContext): Expression {
        return xp(ctx.DECNUM().text.toBigDecimal())
    }

    override fun visitRecurringDecimalNumber(ctx: ExpressionParser.RecurringDecimalNumberContext): Expression {
        val text = ctx.RECURRING_DECNUM().text
        val startRep = text.indexOf('[')
        val endRep = text.indexOf(']')
        val decimal = BigDecimal(
            text.substring(0 until startRep) +
                text.substring(startRep + 1 until endRep)
        )
        return xp(RecurringDecimal(decimal, endRep - startRep - 1))
    }

    override fun visitVariable(ctx: ExpressionParser.VariableContext): Expression {
        return xp(ctx.VARIABLE().text)
    }

    override fun visitUndefined(ctx: ExpressionParser.UndefinedContext?): Expression {
        return Expression(UndefinedOperator, emptyList())
    }
}