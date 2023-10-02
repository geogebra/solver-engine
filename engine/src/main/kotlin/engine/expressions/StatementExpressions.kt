package engine.expressions

import engine.operators.Comparator
import engine.operators.ComparisonOperator
import engine.operators.DoubleComparisonOperator
import engine.operators.StatementSystemOperator
import engine.operators.StatementUnionOperator
import engine.sign.Sign

open class Comparison(
    lhs: Expression,
    val comparator: Comparator,
    rhs: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = ComparisonOperator(comparator),
    operands = listOf(lhs, rhs),
    meta = meta,
) {
    val lhs get() = firstChild
    val rhs get() = secondChild

    fun holds(expressionComparator: ExpressionComparator): Boolean? {
        val compSign = expressionComparator.compareExpressions(lhs, rhs)
        return when {
            compSign == Sign.NONE -> null
            compSign.implies(this.comparator.compareSign) -> true
            compSign.implies(this.comparator.compareSign.negation()) -> false
            else -> null
        }
    }
}

class Equation(lhs: Expression, rhs: Expression, meta: NodeMeta = BasicMeta()) : Comparison(
    lhs = lhs,
    comparator = Comparator.Equal,
    rhs = rhs,
    meta = meta,
)

class Inequation(lhs: Expression, rhs: Expression, meta: NodeMeta = BasicMeta()) : Comparison(
    lhs = lhs,
    comparator = Comparator.NotEqual,
    rhs = rhs,
    meta = meta,
)

class Inequality(
    lhs: Expression,
    comparator: Comparator,
    rhs: Expression,
    meta: NodeMeta = BasicMeta(),
) : Comparison(
    lhs = lhs,
    comparator = comparator,
    rhs = rhs,
    meta = meta,
)

class DoubleInequality(
    first: Expression,
    val leftComparator: Comparator,
    second: Expression,
    val rightComparator: Comparator,
    third: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
    operator = DoubleComparisonOperator(leftComparator, rightComparator),
    operands = listOf(first, second, third),
    meta = meta,
) {
    private val first get() = firstChild
    private val second get() = secondChild
    private val third get() = thirdChild

    fun getLeftInequality() = Inequality(first, leftComparator, second)

    fun getRightInequality() = Inequality(second, rightComparator, third)
}

class StatementUnion(statements: List<Expression>, meta: NodeMeta = BasicMeta()) : Expression(
    operator = StatementUnionOperator,
    operands = statements,
    meta = meta,
) {
    val statements get() = children
}

class StatementSystem(equations: List<Expression>, meta: NodeMeta = BasicMeta()) : Expression(
    operator = StatementSystemOperator,
    operands = equations,
    meta = meta,
) {
    val equations get() = children

    fun withNamedEquations(name: (Int) -> String) =
        statementSystemOf(equations.mapIndexed { i, eq -> eq.withName(name(i)) }).withOrigin(origin)
}
