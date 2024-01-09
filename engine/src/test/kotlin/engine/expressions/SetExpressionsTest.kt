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

import engine.sign.Sign
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * asserts whether s1.intersection(s2) == s2.intersection(s1) == intersection
 */
private fun assertIntersection(s1: SetExpression, s2: SetExpression, intersection: SetExpression?) {
    assert(intersection == s1.intersect(s2, exprDoubleValueComparator)) {
        "Expected intersection: $intersection, but got ${s1.intersect(s2, exprDoubleValueComparator)}"
    }

    assert(intersection == s2.intersect(s1, exprDoubleValueComparator)) {
        "Expected intersection: $intersection, but got ${s2.intersect(s1, exprDoubleValueComparator)}"
    }
}

/**
 * asserts whether s1.union(s2) == s2.union(s1) == union
 */
private fun assertUnion(s1: SetExpression, s2: SetExpression, union: SetExpression?) {
    assert(union == s1.union(s2, exprDoubleValueComparator)) {
        "Expected union: $union, but got ${s1.union(s2, exprDoubleValueComparator)}"
    }

    assert(union == s2.union(s1, exprDoubleValueComparator)) {
        "Expected union: $union, but got ${s2.union(s1, exprDoubleValueComparator)}"
    }
}

private val exprDoubleValueComparator = ExpressionComparator { e1: Expression, e2: Expression ->
    when {
        e1 == Constants.Infinity -> Sign.POSITIVE
        e1 == Constants.NegativeInfinity -> Sign.NEGATIVE
        e2 == Constants.NegativeInfinity -> Sign.POSITIVE
        e2 == Constants.Infinity -> Sign.NEGATIVE
        else -> {
            val diff = sumOf(e1, negOf(e2)).withOrigin(RootOrigin())
            val numericValue = diff.doubleValue
            when {
                numericValue.isNaN() -> Sign.NONE
                numericValue > 0 -> Sign.POSITIVE
                numericValue < 0 -> Sign.NEGATIVE
                else -> Sign.ZERO
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
class SetExpressionsTest {
    private fun F(vararg a: Int) = FiniteSet(a.map { xp(it) })

    // (-inf, x)
    private fun XO(x: Int) = Interval(Constants.NegativeInfinity, xp(x), closedLeft = false, closedRight = false)

    // (-inf, x]
    private fun XC(x: Int) = Interval(Constants.NegativeInfinity, xp(x), closedLeft = false, closedRight = true)

    // (x, inf)
    private fun OX(x: Int) = Interval(xp(x), Constants.Infinity, closedLeft = false, closedRight = false)

    // [x, inf)
    private fun CX(x: Int) = Interval(xp(x), Constants.Infinity, closedLeft = true, closedRight = false)

    // (x, y)
    private fun OO(x: Int, y: Int) = Interval(xp(x), xp(y), closedLeft = false, closedRight = false)

    // [x, y)
    private fun CO(x: Int, y: Int) = Interval(xp(x), xp(y), closedLeft = true, closedRight = false)

    // (x, y]
    private fun OC(x: Int, y: Int) = Interval(xp(x), xp(y), closedLeft = false, closedRight = true)

    // [x. y]
    private fun CC(x: Int, y: Int) = Interval(xp(x), xp(y), closedLeft = true, closedRight = true)

    // a \ b
    private fun diff(a: SetExpression, b: SetExpression) = SetDifference(a, b)

    // U a
    private fun union(vararg a: SetExpression) = SetUnion(a.toList())

    @Test
    fun `test interval contains point`() {
        assertTrue(CC(1, 2).contains(xp(1), exprDoubleValueComparator)!!)
    }

    @Test
    fun `test union of finite sets`() {
        assertUnion(
            s1 = F(-1, 1),
            s2 = FiniteSet(listOf(negOf(squareRootOf(xp(2))), squareRootOf(xp(2)))),
            union = FiniteSet(listOf(negOf(squareRootOf(xp(2))), xp(-1), xp(1), squareRootOf(xp(2)))),
        )
    }

    @Test
    fun `test union of intervals with points`() {
        assertUnion(s1 = OO(1, 2), s2 = F(1, 2), union = CC(1, 2))
        assertUnion(s1 = XO(2), s2 = F(2), union = XC(2))
        assertUnion(s1 = union(OO(1, 2), OO(2, 3)), s2 = F(2), union = OO(1, 3))
        assertUnion(s1 = union(OO(1, 2), OO(2, 3)), s2 = F(1, 2, 3), union = CC(1, 3))
    }

    @Test
    fun `test union of interval with interval`() {
        // one interval subset of another
        assertUnion(s1 = CO(0, 3), s2 = CO(1, 2), union = CO(0, 3))

        // union, where ends are joined to form interval union
        assertUnion(s1 = CC(-1, 1), s2 = CO(1, 2), union = CO(-1, 2))
        assertUnion(s1 = OC(-2, -1), s2 = CC(-1, 1), union = OC(-2, 1))

        assertUnion(s1 = OO(-2, -1), s2 = CO(0, 3), union = union(OO(-2, -1), CO(0, 3)))
        assertUnion(s1 = CC(-1, 1), s2 = OO(-2, -1), union = OC(-2, 1))

        // non-finite end points intersection
        assertUnion(s1 = XC(1), s2 = CO(1, 2), union = XO(2))
        assertUnion(s1 = XC(1), s2 = XC(1), union = XC(1))

        // infinite interval union
        assertUnion(s1 = Constants.Reals, s2 = CO(0, 1), Constants.Reals)
    }

    @Test
    fun `test union of interval with cofinite set`() {
        assertUnion(
            s1 = union(OO(2, 3), OX(3)),
            s2 = diff(Constants.Reals, F(3)),
            union = diff(Constants.Reals, F(3)),
        )

        assertUnion(
            s1 = union(OO(2, 3), OX(3)),
            s2 = diff(Constants.Reals, F(2)),
            union = diff(Constants.Reals, F(2)),
        )
    }

    @Test
    fun `test union of incomparable intervals`() {
        assertUnion(
            s1 = Interval(xp("m"), sumOf(xp("m"), xp(1)), false, false),
            s2 = Interval(xp("n"), sumOf(xp("n"), xp(1)), false, false),
            union = null,
        )
    }

    @Test
    fun `test intersection of intervals`() {
        // one interval subset of another
        assertIntersection(s1 = CO(0, 3), s2 = CO(1, 2), intersection = CO(1, 2))

        // intersection is a single element
        assertIntersection(s1 = CC(-1, 1), s2 = CO(1, 2), intersection = F(1))
        assertIntersection(s1 = OC(-2, -1), s2 = CC(-1, 1), intersection = F(-1))

        // intersection is an empty set
        assertIntersection(s1 = OO(-2, -1), s2 = CO(0, 3), intersection = Constants.EmptySet)
        assertIntersection(s1 = CC(-1, 1), s2 = OO(-2, -1), intersection = Constants.EmptySet)

        // non-finite end points intersection
        assertIntersection(s1 = XC(1), s2 = CO(1, 2), intersection = F(1))
        assertIntersection(s1 = XC(1), s2 = XC(1), intersection = XC(1))

        // infinite interval intersection
        assertIntersection(s1 = Constants.Reals, s2 = CO(0, 1), intersection = CO(0, 1))
    }

    @Test
    fun `test intersection of interval with cofinite set`() {
        assertIntersection(
            s1 = union(OO(2, 3), OX(3)),
            s2 = diff(Constants.Reals, F(2)),
            intersection = union(OO(2, 3), OX(3)),
        )

        assertIntersection(
            s1 = union(CO(2, 3), OX(3)),
            s2 = diff(Constants.Reals, F(3)),
            intersection = union(CO(2, 3), OX(3)),
        )
    }

    @Test
    fun `test intersection of unions of intervals`() {
        assertIntersection(
            s1 = union(XO(5), CX(7)),
            s2 = CC(1, 10),
            intersection = union(CO(1, 5), CC(7, 10)),
        )
    }
}
