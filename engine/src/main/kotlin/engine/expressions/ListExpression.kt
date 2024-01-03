package engine.expressions

import engine.operators.ListOperator

class ListExpression(
    elements: List<Expression>,
    meta: NodeMeta = BasicMeta(),
) : Expression(
        operator = ListOperator,
        operands = elements,
        meta = meta,
    ) {
    val elements get() = children
}
