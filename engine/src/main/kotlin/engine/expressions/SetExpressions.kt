/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package engine.expressions

import engine.operators.IntervalOperator
import engine.operators.Operator
import engine.operators.SetOperators
import engine.operators.TupleOperator
import engine.sign.Sign
import kotlin.math.sign

data class Boundary(
    val value: Expression,
    // -1 for left, 1 for right
    val side: Int,
    // 1 for entering a set, -1 for leaving a set
    val gradient: Int,
) {
    override fun toString() = "($value, $side, $gradient)"

    fun isStarting() = gradient > 0

    fun isEnding() = gradient < 0

    fun isClosed() = gradient * side < 0

    fun isOpen() = gradient * side > 0
}

data class BoundarySet(
    val boundaries: List<Boundary>,
    val initialHeight: Int,
) {
    fun complement(): BoundarySet =
        BoundarySet(
            boundaries = boundaries.map { it.copy(gradient = -it.gradient) },
            initialHeight = 1 - initialHeight,
        )

    @Suppress("CyclomaticComplexMethod")
    fun toSetExpression(): SetExpression {
        if (boundaries.isEmpty()) {
            return if (initialHeight > 0) Constants.Reals else Constants.EmptySet
        }

        // Let's find the components of this set.

        // - the intervals it contains
        val intervals = mutableListOf<Interval>()
        // - the discret points it contains
        val discretePoints = mutableListOf<Expression>()
        // - the discrete points its complement contains
        val codiscretePoints = mutableListOf<Expression>()

        var lastBoundary = Boundary(Constants.NegativeInfinity, 1, if (initialHeight > 0) 1 else -1)

        for (boundary in boundaries) {
            if (lastBoundary.isStarting()) {
                assert(boundary.isEnding())
                if (lastBoundary.value == boundary.value) {
                    assert(boundary.isClosed() && lastBoundary.isClosed())
                    discretePoints.add(boundary.value)
                } else {
                    intervals.add(
                        Interval(
                            lastBoundary.value,
                            boundary.value,
                            closedLeft = lastBoundary.isClosed(),
                            closedRight = boundary.isClosed(),
                        ),
                    )
                }
            } else {
                assert(lastBoundary.isEnding())
                assert(boundary.isStarting())
                if (lastBoundary.value == boundary.value) {
                    assert(boundary.isOpen() && lastBoundary.isOpen())
                    codiscretePoints.add(boundary.value)
                }
            }
            lastBoundary = boundary
        }

        // If the last boundary is starting we need to end it
        if (lastBoundary.isStarting()) {
            intervals.add(
                Interval(
                    lastBoundary.value,
                    Constants.Infinity,
                    closedLeft = lastBoundary.isClosed(),
                    closedRight = false,
                ),
            )
        }

        return when {
            intervals.isEmpty() -> FiniteSet(discretePoints)
            2 * codiscretePoints.size == boundaries.size -> SetDifference(Constants.Reals, FiniteSet(codiscretePoints))
            discretePoints.isEmpty() && intervals.size == 1 -> intervals[0]
            discretePoints.isEmpty() -> SetUnion(intervals)
            else -> SetUnion(listOf(FiniteSet(discretePoints)) + intervals)
        }
    }
}

/**
 * Join a number of sets - numSets is the number of sets we want to be in
 */
private fun joinBoundarySets(
    boundarySets: List<BoundarySet>,
    numSets: Int,
    comparator: Comparator<Expression>,
): BoundarySet {
    val boundaryComparator =
        compareBy<Boundary, Expression>(comparator) { it.value }
            // If we could sort by value (e.g. the difference is defined) this sorting does nothing, as the values are
            // the same, otherwise it will sort the expressions which could not be compared in alphabetical order.
            // This is done so that we never sort primarily by side, causing the below algorithm to break
            .thenBy { it.value.toString() }
            .thenBy { it.side }
    val boundaries = boundarySets.flatMap { it.boundaries }.sortedWith(boundaryComparator)
    val initialHeight = boundarySets.sumOf { it.initialHeight } - numSets + 1

    var height = 2 * initialHeight - 1
    val flatBoundaries = mutableListOf<Boundary>()

    val iter = boundaries.listIterator()
    while (iter.hasNext()) {
        var simplestBoundary = iter.next()
        var gradient = simplestBoundary.gradient

        for (boundary in iter) {
            if (boundaryComparator.compare(boundary, simplestBoundary) != 0) {
                iter.previous()
                break
            }

            gradient += boundary.gradient
            if (boundary.value.complexity() < simplestBoundary.value.complexity()) {
                simplestBoundary = boundary
            }
        }

        if (height * (height + 2 * gradient) < 0) {
            flatBoundaries.add(simplestBoundary.copy(gradient = gradient.sign))
        }
        height += 2 * gradient
    }

    return BoundarySet(flatBoundaries.toList(), if (initialHeight > 0) 1 else 0)
}

fun boundarySetUnion(boundarySets: List<BoundarySet>, comparator: Comparator<Expression>) =
    joinBoundarySets(
        boundarySets,
        1,
        comparator,
    )

fun boundarySetIntersection(boundarySets: List<BoundarySet>, comparator: Comparator<Expression>) =
    joinBoundarySets(boundarySets, boundarySets.size, comparator)

abstract class SetExpression internal constructor(
    operator: Operator,
    operands: List<Expression>,
    meta: NodeMeta,
) : Expression(operator, operands, meta) {
    abstract fun contains(element: Expression, comparator: ExpressionComparator): Boolean?

    abstract fun isEmpty(comparator: ExpressionComparator): Boolean?

    abstract fun toBoundarySet(comparator: Comparator<Expression>): BoundarySet

    open fun intersect(other: SetExpression, comparator: Comparator<Expression>): SetExpression? {
        return try {
            boundarySetIntersection(
                listOf(
                    toBoundarySet(comparator),
                    other.toBoundarySet(comparator),
                ),
                comparator,
            ).toSetExpression()
        } catch (e: IncomparableExpressionsException) {
            null
        }
    }

    open fun union(other: SetExpression, comparator: Comparator<Expression>): SetExpression? {
        return try {
            boundarySetUnion(
                listOf(
                    toBoundarySet(comparator),
                    other.toBoundarySet(comparator),
                ),
                comparator,
            ).toSetExpression()
        } catch (e: IncomparableExpressionsException) {
            null
        }
    }
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
        val leftSign = comparator.compareExpressions(element, leftBound)
        val rightSign = comparator.compareExpressions(rightBound, element)
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

    override fun toBoundarySet(comparator: Comparator<Expression>): BoundarySet {
        val leftBoundary = Boundary(leftBound, if (closedLeft) -1 else 1, 1)
        val rightBoundary = Boundary(rightBound, if (closedRight) 1 else -1, -1)
        return when {
            leftBound == Constants.NegativeInfinity && rightBound == Constants.Infinity -> BoundarySet(emptyList(), 1)
            leftBound == Constants.NegativeInfinity -> BoundarySet(listOf(rightBoundary), 1)
            rightBound == Constants.Infinity -> BoundarySet(listOf(leftBoundary), 0)
            else -> BoundarySet(listOf(leftBoundary, rightBoundary), 0)
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
            val sign = comparator.compareExpressions(member, element)
            when {
                sign == Sign.ZERO -> return true
                sign == Sign.UNKNOWN -> defaultResult = null
            }
        }
        return defaultResult
    }

    override fun toBoundarySet(comparator: Comparator<Expression>): BoundarySet {
        return BoundarySet(
            elements.flatMap {
                listOf(
                    Boundary(it, -1, 1),
                    Boundary(it, 1, -1),
                )
            },
            0,
        )
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

    override fun union(other: SetExpression, comparator: Comparator<Expression>): SetExpression? {
        if (other !is CartesianProduct || other.components.size != components.size) return null

        val unionComponents = components.zip(other.components)
            .map { (a, b) -> a.union(b, comparator) ?: return null }
        return CartesianProduct(unionComponents)
    }

    override fun intersect(other: SetExpression, comparator: Comparator<Expression>): SetExpression? {
        if (other !is CartesianProduct || other.components.size != components.size) return null

        val intersectionComponents = components.zip(other.components)
            .map { (a, b) -> a.intersect(b, comparator) ?: return null }
        return CartesianProduct(intersectionComponents)
    }

    override fun toBoundarySet(comparator: Comparator<Expression>) =
        throw UnsupportedOperationException("Cartesian product cannot be converted to canonical set representation")
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

    override fun isEmpty(comparator: ExpressionComparator): Boolean {
        return components.map { it.isEmpty(comparator) }.all { it == true }
    }

    override fun contains(element: Expression, comparator: ExpressionComparator): Boolean {
        return components.map { it.contains(element, comparator) }.any()
    }

    override fun toBoundarySet(comparator: Comparator<Expression>): BoundarySet {
        return boundarySetUnion(components.map { it.toBoundarySet(comparator) }, comparator)
    }
}

class SetDifference(
    left: SetExpression,
    right: SetExpression,
    meta: NodeMeta = BasicMeta(),
) : SetExpression(
        operator = SetOperators.SetDifference,
        operands = listOf(left, right),
        meta,
    ) {
    val left get() = firstChild as SetExpression
    val right get() = secondChild as SetExpression

    override fun contains(element: Expression, comparator: ExpressionComparator): Boolean? {
        val leftContains = left.contains(element, comparator) ?: return null
        val rightContains = right.contains(element, comparator) ?: return null
        return leftContains && !rightContains
    }

    override fun isEmpty(comparator: ExpressionComparator): Boolean? {
        return null
    }

    override fun toBoundarySet(comparator: Comparator<Expression>): BoundarySet {
        return boundarySetIntersection(
            listOf(
                left.toBoundarySet(comparator),
                right.toBoundarySet(comparator).complement(),
            ),
            comparator,
        )
    }
}

class Reals(
    meta: NodeMeta = BasicMeta(),
) : SetExpression(
        operator = SetOperators.Reals,
        operands = listOf(),
        meta,
    ) {
    override fun contains(element: Expression, comparator: ExpressionComparator): Boolean {
        return true
    }

    override fun isEmpty(comparator: ExpressionComparator): Boolean {
        return false
    }

    override fun toBoundarySet(comparator: Comparator<Expression>): BoundarySet {
        return BoundarySet(emptyList(), 1)
    }
}

class Integers(
    meta: NodeMeta = BasicMeta(),
) : SetExpression(
        operator = SetOperators.Integers,
        operands = listOf(),
        meta,
    ) {
    override fun contains(element: Expression, comparator: ExpressionComparator): Boolean {
        return element is IntegerExpression
    }

    override fun isEmpty(comparator: ExpressionComparator): Boolean {
        return false
    }

    override fun toBoundarySet(comparator: Comparator<Expression>): BoundarySet {
        return BoundarySet(emptyList(), 1)
    }
}
