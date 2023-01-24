package parser

import engine.expressions.Constants
import engine.expressions.Decorator
import engine.expressions.Expression
import engine.expressions.greaterThanEqualOf
import engine.expressions.greaterThanOf
import engine.expressions.lessThanEqualOf
import engine.expressions.lessThanOf
import engine.expressions.mixedNumber
import engine.expressions.negOf
import engine.expressions.solutionOf
import engine.expressions.solutionSetOf
import engine.expressions.xp
import engine.operators.BinaryExpressionOperator
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator
import engine.operators.IntervalOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.UnaryExpressionOperator
import engine.utility.RecurringDecimal
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import parser.antlr.ExpressionBaseVisitor
import parser.antlr.ExpressionLexer
import parser.antlr.ExpressionParser
import java.math.BigDecimal
import java.security.InvalidParameterException

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
            operand.hasBracket() || operator.nthChildAllowed(i, operand.operator) -> operand
            else -> operand.decorate(Decorator.MissingBracket)
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

    override fun visitInequality(ctx: ExpressionParser.InequalityContext): Expression {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)

        return when (ctx.comparator.text) {
            "<" -> lessThanOf(lhs, rhs)
            "<=" -> lessThanEqualOf(lhs, rhs)
            ">" -> greaterThanOf(lhs, rhs)
            ">=" -> greaterThanEqualOf(lhs, rhs)
            else -> throw InvalidParameterException("Comparator ${ctx.comparator.text} not recognized")
        }
    }

    override fun visitExpr(ctx: ExpressionParser.ExprContext): Expression {
        return visit(ctx.getChild(0))
    }

    override fun visitSolution(ctx: ExpressionParser.SolutionContext): Expression {
        return solutionOf(visit(ctx.variable()), visit(ctx.set()))
    }

    override fun visitEmptySet(ctx: ExpressionParser.EmptySetContext): Expression {
        return Constants.EmptySet
    }

    override fun visitFiniteSet(ctx: ExpressionParser.FiniteSetContext): Expression {
        return solutionSetOf(listOf(visit(ctx.first)) + ctx.rest.map { visit(it) })
    }

    override fun visitReals(ctx: ExpressionParser.RealsContext): Expression {
        return Constants.Reals
    }

    override fun visitInterval(ctx: ExpressionParser.IntervalContext): Expression {
        val leftClosed = ctx.leftBracket.text == "["
        val rightClosed = ctx.rightBracket.text == "]"
        return makeExpression(IntervalOperator(leftClosed, rightClosed), visit(ctx.left), visit(ctx.right))
    }

    override fun visitInfinity(ctx: ExpressionParser.InfinityContext?): Expression {
        return Constants.Infinity
    }

    override fun visitMinusInfinity(ctx: ExpressionParser.MinusInfinityContext?): Expression {
        return negOf(Constants.Infinity)
    }

    override fun visitSum(ctx: ExpressionParser.SumContext): Expression {
        val first = visit(ctx.first)
        val rest = ctx.rest.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        val terms = mutableListOf<Expression>(first)
        terms.addAll(rest)
        return makeExpression(NaryOperator.Sum, terms)
    }

    override fun visitRealFirstTerm(ctx: ExpressionParser.RealFirstTermContext): Expression {
        val p = visit(ctx.explicitProduct())
        return if (ctx.sign == null) p else when (ctx.sign.text) {
            "+" -> makeExpression(UnaryExpressionOperator.Plus, p)
            else -> makeExpression(UnaryExpressionOperator.Minus, p)
        }
    }

    override fun visitFirstPartialSum(ctx: ExpressionParser.FirstPartialSumContext): Expression {
        return visit(ctx.sum()).decorate(Decorator.PartialSumBracket)
    }

    override fun visitRealOtherTerm(ctx: ExpressionParser.RealOtherTermContext): Expression {
        val p = visit(ctx.explicitProduct())
        return when {
            ctx.sign.text == "-" -> makeExpression(UnaryExpressionOperator.Minus, p)
            UnaryExpressionOperator.Plus.childAllowed(p.operator) || p.hasBracket() -> p
            else -> p.decorate(Decorator.MissingBracket)
        }
    }

    override fun visitOtherPartialSum(ctx: ExpressionParser.OtherPartialSumContext): Expression {
        return makeExpression(NaryOperator.Sum, ctx.terms.map { visit(it) }).decorate(Decorator.PartialSumBracket)
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
        return visit(ctx.expr()).decorate(Decorator.RoundBracket)
    }

    override fun visitSquareBracket(ctx: ExpressionParser.SquareBracketContext): Expression {
        return visit(ctx.expr()).decorate(Decorator.SquareBracket)
    }

    override fun visitCurlyBracket(ctx: ExpressionParser.CurlyBracketContext): Expression {
        return visit(ctx.expr()).decorate(Decorator.CurlyBracket)
    }

    override fun visitMixedNumber(ctx: ExpressionParser.MixedNumberContext): Expression {
        return mixedNumber(
            ctx.integer.text.toBigInteger(),
            ctx.num.text.toBigInteger(),
            ctx.den.text.toBigInteger()
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
        return Constants.Undefined
    }
}
