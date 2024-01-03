package engine.expressions

import engine.operators.ExpressionWithConstraintOperator

class ExpressionWithConstraint(
    expression: Expression,
    constraint: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
        operator = ExpressionWithConstraintOperator,
        operands = listOf(expression, constraint),
        meta = meta,
    ) {
    val expression get() = firstChild
    val constraint get() = secondChild
}

fun expressionWithConstraintOf(expression: Expression, constraint: Expression?) =
    if (constraint == null) expression else ExpressionWithConstraint(expression, constraint)
