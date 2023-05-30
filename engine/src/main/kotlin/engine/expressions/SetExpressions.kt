package engine.expressions

import engine.operators.IntervalOperator
import engine.operators.Operator
import engine.operators.SetOperators
import engine.operators.TupleOperator
import engine.sign.Sign

abstract class SetExpression(
    operator: Operator,
    operands: List<Expression>,
    meta: NodeMeta,
) : Expression(operator, operands, meta) {

    abstract fun contains(element: Expression, comparator: ExpressionComparator): Boolean?

    abstract fun isEmpty(comparator: ExpressionComparator): Boolean?

    fun intersect(other: SetExpression, comparator: ExpressionComparator): SetExpression? {
        return when (other) {
            is FiniteSet -> intersectWithFiniteSet(other, comparator)
            is Interval -> intersectWithInterval(other, comparator)
            is CartesianProduct -> intersectWithCartesianProduct(other, comparator)
            else -> null
        }
    }
    protected abstract fun intersectWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression?
    protected abstract fun intersectWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression?
    protected abstract fun intersectWithCartesianProduct(
        other: CartesianProduct,
        comparator: ExpressionComparator,
    ): SetExpression?
}

fun interface ExpressionComparator {
    fun compare(e1: Expression, e2: Expression): Sign
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

    fun leftClosed() = Interval(leftBound, rightBound, true, closedRight, meta)
    fun rightClosed() = Interval(leftBound, rightBound, closedLeft, true, meta)

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

    override fun intersectWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression? {
        return other.intersect(this, comparator)
    }

    override fun intersectWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun intersectWithCartesianProduct(
        other: CartesianProduct,
        comparator: ExpressionComparator,
    ): SetExpression? {
        return emptySet
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

    private fun filterElements(other: SetExpression, comparator: ExpressionComparator): FiniteSet? {
        val commonElements = mutableListOf<Expression>()
        for (member in elements) {
            if (other.contains(member, comparator) ?: return null) {
                commonElements.add(member)
            }
        }
        return if (commonElements.isEmpty()) emptySet else FiniteSet(commonElements)
    }

    override fun intersectWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression? {
        return filterElements(other, comparator)
    }

    override fun intersectWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression? {
        return filterElements(other, comparator)
    }

    override fun intersectWithCartesianProduct(
        other: CartesianProduct,
        comparator: ExpressionComparator,
    ): SetExpression? {
        return filterElements(other, comparator)
    }
}

class CartesianProduct(
    components: List<Expression>,
    meta: NodeMeta = BasicMeta(),
) : SetExpression(
    operator = SetOperators.CartesianProduct,
    operands = components,
    meta,
) {
    val components get() = children as List<SetExpression>

    override fun isEmpty(comparator: ExpressionComparator): Boolean? {
        val componentsEmptiness = components.map { it.isEmpty(comparator) }
        return when {
            componentsEmptiness.all { it == false } -> false
            componentsEmptiness.any { it == true } -> true
            else -> null
        }
    }

    override fun contains(element: Expression, comparator: ExpressionComparator): Boolean? {
        if (element.operator != TupleOperator || element.childCount != components.size) {
            return false
        }
        val membershipByComponent = element.children
            .zip(components)
            .map { (coord, component) -> component.contains(coord, comparator) }
        return when {
            membershipByComponent.all { it == true } -> true
            membershipByComponent.any { it == false } -> false
            else -> null
        }
    }

    override fun intersectWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression? {
        return other.intersect(this, comparator)
    }

    override fun intersectWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression? {
        return emptySet
    }
    override fun intersectWithCartesianProduct(
        other: CartesianProduct,
        comparator: ExpressionComparator,
    ): SetExpression? {
        if (other.childCount != childCount) {
            return emptySet
        }
        val componentIntersections = components.zip(other.components).map { (x, y) -> x.intersect(y, comparator) }
        val validComponentIntersections = componentIntersections.filterNotNull()
        return when {
            validComponentIntersections.size < componentIntersections.size -> null
            validComponentIntersections == components -> this
            else -> CartesianProduct(validComponentIntersections)
        }
    }
}

/**
 * his is a hack.  I don't know how to handle the interaction between this empty set and the Contradiction concept,
 * It will probably need to be revisited
 */
private val emptySet = FiniteSet(emptyList())
