package expressions

import java.math.BigInteger


data class Expression(val operator: Operator, val operands: List<Expression>) {
    init {
        require(operator.childrenAllowed(operands.map { it.operator }))
    }
}


fun xp(n: Int) = Expression(IntegerOperator(n.toBigInteger()), emptyList())
fun xp(n: BigInteger) = Expression(IntegerOperator(n), emptyList())
fun xp(v: String) = Expression(VariableOperator(v), emptyList())

fun mixedNumber(integer: BigInteger, numerator: BigInteger, denominator: BigInteger) =
    Expression(MixedNumberOperator(integer, numerator, denominator), emptyList())

fun bracketOf(expr: Expression) = Expression(UnaryOperator.Bracket, listOf(expr))

fun negOf(expr: Expression) = Expression(UnaryOperator.Minus, listOf(expr))

fun fractionOf(numerator: Expression, denominator: Expression) =
    Expression(BinaryOperator.Fraction, listOf(numerator, denominator))

fun powerOf(base: Expression, exponent: Expression) = Expression(BinaryOperator.Power, listOf(base, exponent))

fun sumOf(vararg terms: Expression) = Expression(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Expression) = Expression(NaryOperator.Product, factors.asList())

fun implicitProductOf(vararg factors: Expression) = Expression(NaryOperator.ImplicitProduct, factors.asList())
