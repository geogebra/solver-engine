package engine.expressions

import engine.operators.SumOperator

class Sum(
    terms: List<Expression>,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = SumOperator,
    operands = terms,
    meta,
) {
    val terms get() = children
}
