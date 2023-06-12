package engine.expressions

import engine.operators.EquationOperator
import engine.operators.InequalityOperators
import engine.operators.StatementWithConstraintOperator
import engine.sign.Sign

class Equation(lhs: Expression, rhs: Expression, meta: NodeMeta = BasicMeta()) : Expression(
    operator = EquationOperator,
    operands = listOf(lhs, rhs),
    meta = meta,
) {
    val lhs get() = firstChild
    val rhs get() = secondChild

    fun holds(comparator: ExpressionComparator): Boolean? {
        return when (comparator.compare(lhs, rhs)) {
            Sign.ZERO -> true
            Sign.UNKNOWN -> null
            else -> false
        }
    }
}

class Inequality(
    lhs: Expression,
    rhs: Expression,
    operator: InequalityOperators,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = operator,
    operands = listOf(lhs, rhs),
    meta = meta,
) {
    val lhs get() = firstChild
    val rhs get() = secondChild

    fun holds(): Boolean {
        return (operator as InequalityOperators).holdsFor(
            lhs.doubleValue.toBigDecimal(),
            rhs.doubleValue.toBigDecimal(),
        )
    }

    fun holds(comparator: ExpressionComparator): Boolean? {
        return (operator as InequalityOperators).holdsFor(comparator.compare(lhs, rhs))
    }
}

class StatementWithConstraint(statement: Expression, constraint: Expression, meta: NodeMeta = BasicMeta()) : Expression(
    operator = StatementWithConstraintOperator,
    operands = listOf(statement, constraint),
    meta = meta,
) {
    val statement get() = firstChild
    val constraint get() = secondChild
}
