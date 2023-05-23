package engine.expressions

import engine.conditions.Sign
import engine.operators.IntervalOperator
import engine.operators.Operator
import engine.operators.SetOperators

fun interface ExpressionComparator {
    fun compare(e1: Expression, e2: Expression): Sign
}

abstract class SetExpression(
    operator: Operator,
    operands: List<Expression>,
    meta: NodeMeta,
) :
    Expression(operator, operands, meta) {
    abstract fun contains(element: Expression, compare: ExpressionComparator): Boolean?
    abstract fun intersect(other: SetExpression, comparator: ExpressionComparator): SetExpression?

    abstract fun isEmpty(comparator: ExpressionComparator): Boolean?
}

class Interval(
    leftBound: Expression,
    rightBound: Expression,
    val closedLeft: Boolean,
    val closedRight: Boolean,
    meta: NodeMeta = BasicMeta(),
) : SetExpression(
    operator = IntervalOperator(closedLeft, closedRight),
    operands = listOf(leftBound, rightBound),
    meta,
) {
    val leftBound get() = firstChild
    val rightBound get() = secondChild

    override fun isEmpty(comparator: ExpressionComparator): Boolean? {
        return false
    }

    override fun contains(element: Expression, comparator: ExpressionComparator): Boolean? {
        val leftSign = comparator.compare(element, leftBound)
        val rightSign = comparator.compare(rightBound, element)
        return when {
            !leftSign.isKnown() -> null
            !rightSign.isKnown() -> null
            leftSign == Sign.NEGATIVE -> false
            leftSign == Sign.ZERO && !closedLeft -> false
            rightSign == Sign.NEGATIVE -> false
            rightSign == Sign.ZERO && !closedRight -> false
            else -> true
        }
    }

    override fun intersect(other: SetExpression, comparator: ExpressionComparator): SetExpression? {
        return when (other) {
            is FiniteSet -> other.intersect(this, comparator)
            else -> null
        }
    }
}

class FiniteSet(
    elements: List<Expression>,
    meta: NodeMeta = BasicMeta(),
) : SetExpression(
    operator = SetOperators.FiniteSet,
    operands = elements,
    meta,
) {
    val elements get() = children

    override fun isEmpty(comparator: ExpressionComparator) = elements.isEmpty()

    override fun contains(element: Expression, comparator: ExpressionComparator): Boolean? {
        var defaultResult: Boolean? = false
        for (member in elements) {
            val sign = comparator.compare(member, element)
            when {
                sign == Sign.ZERO -> return true
                sign == Sign.UNKNOWN -> defaultResult = null
            }
        }
        return defaultResult
    }

    override fun intersect(other: SetExpression, comparator: ExpressionComparator): FiniteSet? {
        val commonElements = mutableListOf<Expression>()
        for (member in elements) {
            if (other.contains(member, comparator) ?: return null) {
                commonElements.add(member)
            }
        }
        return FiniteSet(commonElements)
    }
}
