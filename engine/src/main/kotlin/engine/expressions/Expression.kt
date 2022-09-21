package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.BracketOperator
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.operators.LatexRenderable
import engine.operators.MixedNumberOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.RecurringDecimalOperator
import engine.operators.RenderContext
import engine.operators.UnaryExpressionOperator
import engine.operators.VariableOperator
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

data class Expression(val operator: Operator, val operands: List<Expression>) : LatexRenderable {
    init {
        require(operator.childrenAllowed(operands.map { it.operator }))
    }

    override fun toString(): String {
        return operator.readableString(operands)
    }

    /**
     * Returns a LaTeX string representation of the expression.  The string should be of the form "{...}".
     * Each operand should itself be enclosed in "{...}" and there should be no other curly brackets,
     * except for the pair "{}" itself.
     *
     * This ensures that paths can be followed in the string representation by counting instances of
     * "{" and "}" and discarding "{}".
     */
    override fun toLatexString(ctx: RenderContext): String {
        return operator.latexString(ctx, operands)
    }

    fun equiv(other: Expression): Boolean {
        return operator.equiv(other.operator) &&
            operands.size == other.operands.size &&
            operands.zip(other.operands).all { (op1, op2) -> op1.equiv(op2) }
    }
}

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

fun bracketOf(expr: Expression) = Expression(BracketOperator.Bracket, listOf(expr))
fun squareBracketOf(expr: Expression) = Expression(BracketOperator.SquareBracket, listOf(expr))
fun curlyBracketOf(expr: Expression) = Expression(BracketOperator.CurlyBracket, listOf(expr))

fun negOf(expr: Expression) = Expression(UnaryExpressionOperator.Minus, listOf(expr))
fun plusOf(expr: Expression) = Expression(UnaryExpressionOperator.Plus, listOf(expr))

fun divideBy(expr: Expression) = Expression(UnaryExpressionOperator.DivideBy, listOf(expr))

fun fractionOf(numerator: Expression, denominator: Expression) =
    Expression(BinaryExpressionOperator.Fraction, listOf(numerator, denominator))

fun powerOf(base: Expression, exponent: Expression) = Expression(BinaryExpressionOperator.Power, listOf(base, exponent))

fun squareRootOf(radicand: Expression) = Expression(UnaryExpressionOperator.SquareRoot, listOf(radicand))

fun rootOf(radicand: Expression, order: Expression) = Expression(BinaryExpressionOperator.Root, listOf(radicand, order))

fun sumOf(vararg terms: Expression) = Expression(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Expression) = Expression(NaryOperator.Product, factors.asList())

fun implicitProductOf(vararg factors: Expression) = Expression(NaryOperator.ImplicitProduct, factors.asList())
