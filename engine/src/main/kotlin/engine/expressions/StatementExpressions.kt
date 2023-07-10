package engine.expressions

import engine.operators.DoubleInequalityOperator
import engine.operators.EquationOperator
import engine.operators.InequalityOperators
import engine.operators.StatementUnionOperator
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

class Inequality internal constructor(
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

class DoubleInequality internal constructor(
    first: Expression,
    second: Expression,
    third: Expression,
    operator: DoubleInequalityOperator,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = operator,
    operands = listOf(first, second, third),
    meta = meta,
) {
    private val first get() = firstChild
    private val second get() = secondChild
    private val third get() = thirdChild

    fun getInequalities(): List<Expression> {
        val op = (operator as DoubleInequalityOperator)
        return listOf(
            buildExpression(op.leftOp, listOf(first, second)),
            buildExpression(op.rightOp, listOf(second, third)),
        )
    }

    fun getLeftInequality(): Expression {
        val op = (operator as DoubleInequalityOperator)
        return buildExpression(op.leftOp, listOf(first, second))
    }

    fun getRightInequality(): Expression {
        val op = (operator as DoubleInequalityOperator)
        return buildExpression(op.rightOp, listOf(second, third))
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

class StatementUnion(statements: List<Expression>, meta: NodeMeta = BasicMeta()) : Expression(
    operator = StatementUnionOperator,
    operands = statements,
    meta = meta,
) {
    val statements get() = children
}
