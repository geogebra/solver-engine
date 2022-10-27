@file:Suppress("TooManyFunctions")
package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.DecimalOperator
import engine.operators.EquationOperator
import engine.operators.IntegerOperator
import engine.operators.MixedNumberOperator
import engine.operators.NaryOperator
import engine.operators.RecurringDecimalOperator
import engine.operators.UnaryExpressionOperator
import engine.operators.VariableOperator
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

fun xp(n: Int) = xp(n.toBigInteger())

fun xp(n: BigInteger): Expression {
    val posExpr = Expression(IntegerOperator(n.abs()), emptyList())
    return if (n.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(x: BigDecimal): Expression {
    val operator =
        if (x.scale() <= 0) IntegerOperator(x.abs().toBigInteger()) else DecimalOperator(x.abs())
    val posExpr = Expression(operator, emptyList())
    return if (x.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(x: RecurringDecimal): Expression {
    return Expression(RecurringDecimalOperator(x), emptyList())
}

fun xp(v: String) = Expression(VariableOperator(v), emptyList())

fun mixedNumber(integer: BigInteger, numerator: BigInteger, denominator: BigInteger) =
    Expression(MixedNumberOperator, listOf(xp(integer), xp(numerator), xp(denominator)))

fun bracketOf(expr: Expression, decorator: Decorator? = null) = expr.decorate(decorator ?: Decorator.RoundBracket)
fun squareBracketOf(expr: Expression) = expr.decorate(Decorator.SquareBracket)
fun curlyBracketOf(expr: Expression) = expr.decorate(Decorator.CurlyBracket)
fun missingBracketOf(expr: Expression) = expr.decorate(Decorator.MissingBracket)

fun negOf(expr: Expression) = Expression(UnaryExpressionOperator.Minus, listOf(expr))
fun plusOf(expr: Expression) = Expression(UnaryExpressionOperator.Plus, listOf(expr))

fun fractionOf(numerator: Expression, denominator: Expression) =
    Expression(BinaryExpressionOperator.Fraction, listOf(numerator, denominator))

fun powerOf(base: Expression, exponent: Expression) = Expression(BinaryExpressionOperator.Power, listOf(base, exponent))

fun squareRootOf(radicand: Expression) = Expression(UnaryExpressionOperator.SquareRoot, listOf(radicand))

fun rootOf(radicand: Expression, order: Expression) = Expression(BinaryExpressionOperator.Root, listOf(radicand, order))

fun sumOf(vararg terms: Expression) = Expression(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Expression) = Expression(NaryOperator.Product, factors.asList())

fun implicitProductOf(vararg factors: Expression) = Expression(NaryOperator.ImplicitProduct, factors.asList())

fun equationOf(lhs: Expression, rhs: Expression) = Expression(EquationOperator, listOf(lhs, rhs))
