package engine.expressions

import java.math.BigDecimal
import java.math.BigInteger

data class Expression(val operator: Operator, val operands: List<Expression>) {
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
    fun toLatexString(): String {
        return operator.latexString(operands)
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
    val posExpr = Expression(DecimalOperator(x.abs()), emptyList())
    return if (x.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(x: BigDecimal, repeatingDigits: Int): Expression {
    val posExpr = Expression(RecurringDecimalOperator(x.abs(), repeatingDigits), emptyList())
    return if (x.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(v: String) = Expression(VariableOperator(v), emptyList())

fun mixedNumber(integer: BigInteger, numerator: BigInteger, denominator: BigInteger) =
    Expression(MixedNumberOperator, listOf(xp(integer), xp(numerator), xp(denominator)))

fun bracketOf(expr: Expression) = Expression(BracketOperator.Bracket, listOf(expr))
fun squareBracketOf(expr: Expression) = Expression(BracketOperator.SquareBracket, listOf(expr))
fun curlyBracketOf(expr: Expression) = Expression(BracketOperator.CurlyBracket, listOf(expr))

fun negOf(expr: Expression) = Expression(UnaryOperator.Minus, listOf(expr))
fun plusOf(expr: Expression) = Expression(UnaryOperator.Plus, listOf(expr))

fun divideBy(expr: Expression) = Expression(UnaryOperator.DivideBy, listOf(expr))

fun fractionOf(numerator: Expression, denominator: Expression) =
    Expression(BinaryOperator.Fraction, listOf(numerator, denominator))

fun powerOf(base: Expression, exponent: Expression) = Expression(BinaryOperator.Power, listOf(base, exponent))

fun squareRootOf(radicand: Expression) = Expression(UnaryOperator.SquareRoot, listOf(radicand))

fun rootOf(radicand: Expression, order: Expression) = Expression(BinaryOperator.Root, listOf(radicand, order))

fun sumOf(vararg terms: Expression) = Expression(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Expression) = Expression(NaryOperator.Product, factors.asList())

fun implicitProductOf(vararg factors: Expression) = Expression(NaryOperator.ImplicitProduct, factors.asList())
