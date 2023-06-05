package engine.expressions

import engine.operators.UnaryExpressionOperator

class Minus(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = UnaryExpressionOperator.Minus,
    operands = listOf(argument),
    meta,
) {
    val argument get() = firstChild
}

class Plus(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = UnaryExpressionOperator.Plus,
    operands = listOf(argument),
    meta,
) {
    val argument get() = firstChild
}

class PlusMinus(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = UnaryExpressionOperator.PlusMinus,
    operands = listOf(argument),
    meta,
) {
    val argument get() = firstChild
}
