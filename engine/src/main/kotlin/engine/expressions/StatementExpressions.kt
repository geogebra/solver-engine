package engine.expressions

import engine.operators.EquationOperator
import engine.operators.StatementWithConstraintOperator

class Equation(lhs: Expression, rhs: Expression, meta: NodeMeta = BasicMeta()) : Expression(
    operator = EquationOperator,
    operands = listOf(lhs, rhs),
    meta = meta,
) {
    val lhs get() = firstChild
    val rhs get() = secondChild
}

class StatementWithConstraint(statement: Expression, constraint: Expression, meta: NodeMeta = BasicMeta()) : Expression(
    operator = StatementWithConstraintOperator,
    operands = listOf(statement, constraint),
    meta = meta,
) {
    val statement get() = firstChild
    val constraint get() = secondChild
}
