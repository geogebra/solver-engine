package engine.expressions

import engine.operators.BinaryExpressionOperator

class Fraction(
    numerator: Expression,
    denominator: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = BinaryExpressionOperator.Fraction,
    operands = listOf(numerator, denominator),
    meta,
) {
    val numerator get() = firstChild
    val denominator get() = secondChild
}

class Power(
    base: Expression,
    exponent: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = BinaryExpressionOperator.Power,
    operands = listOf(base, exponent),
    meta,
) {
    val base get() = firstChild
    val exponent get() = secondChild
}
