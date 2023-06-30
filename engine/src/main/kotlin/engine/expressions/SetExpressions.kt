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
            is SetUnion -> intersectWithSetUnion(other, comparator)
            else -> null
        }
    }

    fun union(other: SetExpression, comparator: ExpressionComparator): SetExpression? {
        return when (other) {
            is FiniteSet -> unionWithFiniteSet(other, comparator)
            is Interval -> unionWithInterval(other, comparator)
            is CartesianProduct -> unionWithCartesianProduct(other, comparator)
            is SetUnion -> unionWithSetUnion(other, comparator)
            else -> null
        }
    }

    protected abstract fun intersectWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression?
    protected abstract fun intersectWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression?
    protected abstract fun intersectWithCartesianProduct(
        other: CartesianProduct,
        comparator: ExpressionComparator,
    ): SetExpression?
    protected abstract fun intersectWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression?
    protected abstract fun unionWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression?
    protected abstract fun unionWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression?
    protected abstract fun unionWithCartesianProduct(
        other: CartesianProduct,
        comparator: ExpressionComparator,
    ): SetExpression?
    protected abstract fun unionWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression?
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

    override fun isEmpty(comparator: ExpressionComparator): Boolean {
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

    @Suppress("CyclomaticComplexMethod")
    override fun intersectWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression? {
        // Find which interval starts "last" (left closed starts first)
        val iLeft = when (comparator.compare(leftBound, other.leftBound)) {
            Sign.NEGATIVE -> other
            Sign.ZERO -> if (this.closedLeft) other else this
            Sign.POSITIVE -> this
            else -> return null
        }
        // Find which interval stops "first" (right closed stops last)
        val iRight = when (comparator.compare(rightBound, other.rightBound)) {
            Sign.NEGATIVE -> this
            Sign.ZERO -> if (this.closedRight) other else this
            Sign.POSITIVE -> other
            else -> return null
        }
        // The intersection is what is between iLeft.leftBound and iRight.rightBound
        return when (comparator.compare(iLeft.leftBound, iRight.rightBound)) {
            Sign.NEGATIVE -> Interval(iLeft.leftBound, iRight.rightBound, iLeft.closedLeft, iRight.closedRight, meta)
            Sign.ZERO -> if (iLeft.closedLeft && iRight.closedRight) FiniteSet(listOf(iLeft.leftBound)) else emptySet
            Sign.POSITIVE -> emptySet
            else -> null
        }
    }

    override fun intersectWithCartesianProduct(
        other: CartesianProduct,
        comparator: ExpressionComparator,
    ): SetExpression? {
        return emptySet
    }

    override fun intersectWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    @Suppress("CyclomaticComplexMethod")
    override fun unionWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression? {
        // Find which interval starts "first" (left closed starts first)
        val iLeft = when (comparator.compare(leftBound, other.leftBound)) {
            Sign.NEGATIVE -> this
            Sign.ZERO -> if (this.closedLeft) this else other
            Sign.POSITIVE -> other
            else -> return null
        }
        // Find which interval stops "last" (right closed stops last)
        val iRight = when (comparator.compare(rightBound, other.rightBound)) {
            Sign.NEGATIVE -> other
            Sign.ZERO -> if (this.closedRight) this else other
            Sign.POSITIVE -> this
            else -> return null
        }

        return when (comparator.compare(iLeft.rightBound, iRight.leftBound)) {
            // there is no common element b/w the two intervals,
            // this just ends up sorting the two intervals
            Sign.NEGATIVE -> SetUnion(listOf(iLeft, iRight), meta)
            Sign.ZERO, Sign.POSITIVE -> Interval(
                iLeft.leftBound,
                iRight.rightBound,
                iLeft.closedLeft,
                iRight.closedRight,
                meta,
            )
            else -> null
        }
    }

    override fun unionWithCartesianProduct(other: CartesianProduct, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression? {
        return TODO()
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

    override fun intersectWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithCartesianProduct(other: CartesianProduct, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression? {
        return TODO()
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

    override fun intersectWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithCartesianProduct(other: CartesianProduct, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }
}

class SetUnion(
    components: List<Expression>,
    meta: NodeMeta = BasicMeta(),
) : SetExpression(
    operator = SetOperators.SetUnion,
    operands = components,
    meta,
) {
    val components get() = children as List<SetExpression>

    override fun isEmpty(comparator: ExpressionComparator): Boolean? {
        return components.map { it.isEmpty(comparator) }.all { it == true }
    }

    override fun intersectWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun intersectWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun intersectWithCartesianProduct(
        other: CartesianProduct,
        comparator: ExpressionComparator,
    ): SetExpression? {
        return TODO()
    }

    override fun intersectWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithFiniteSet(other: FiniteSet, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithInterval(other: Interval, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithCartesianProduct(other: CartesianProduct, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun unionWithSetUnion(other: SetUnion, comparator: ExpressionComparator): SetExpression? {
        return TODO()
    }

    override fun contains(element: Expression, comparator: ExpressionComparator): Boolean? {
        return components.map { it.contains(element, comparator) }.any()
    }
}

/**
 * this is a hack.  I don't know how to handle the interaction between this empty set and the Contradiction concept,
 * It will probably need to be revisited
 */
val emptySet = FiniteSet(emptyList())
