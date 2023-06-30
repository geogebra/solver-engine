package engine.expressions

import engine.sign.Sign
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertTrue

/**
 * asserts whether s1.intersection(s2) == s2.intersection(s1) == intersection
 */
private fun assertIntersection(s1: SetExpression, s2: SetExpression, intersection: SetExpression) {
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
private fun assertUnion(s1: SetExpression, s2: SetExpression, union: Expression) {
    val setUnion = union as SetExpression
    assert(setUnion == s1.union(s2, exprDoubleValueComparator)) {
        "Expected union: $union, but got ${s1.union(s2, exprDoubleValueComparator)}"
    }

    assert(setUnion == s2.union(s1, exprDoubleValueComparator)) {
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
            val diff = sumOf(e1, negOf(e2)).withOrigin(Root())
            val result = diff.doubleValue.toBigDecimal().setScale(5)
            val compareWithZero = result.compareTo(BigDecimal.ZERO)
            when {
                compareWithZero > 0 -> Sign.POSITIVE
                compareWithZero < 0 -> Sign.NEGATIVE
                else -> Sign.ZERO
            }
        }
    }
}

class SetExpressionsTest {
    private val i0Closed3Open = Interval(xp(0), xp(3), closedLeft = true, closedRight = true)
    private val i1Closed2Open = Interval(xp(1), xp(2), closedLeft = true, closedRight = false)
    private val iMinus1Closed1Closed = Interval(xp(-1), xp(1), closedLeft = true, closedRight = true)
    private val iMinus2OpenMinus1Closed = Interval(xp(-2), xp(-1), closedLeft = false, closedRight = true)
    private val iMinus2OpenMinus1Open = Interval(xp(-2), xp(-1), closedLeft = false, closedRight = false)
    private val iMinusInf1Closed = Interval(Constants.NegativeInfinity, xp(1), closedLeft = false, closedRight = true)
    private val iMinus1Closed2Open = Interval(xp(-1), xp(2), closedLeft = true, closedRight = false)
    private val iMinus2Open1Closed = Interval(xp(-2), xp(1), closedLeft = false, closedRight = true)
    private val iMinusInf2Open = Interval(Constants.NegativeInfinity, xp(2), closedLeft = false, closedRight = false)
    private val iMinusInfPlusInf = Interval(
        Constants.NegativeInfinity,
        Constants.Infinity,
        closedLeft = false,
        closedRight = false,
    )
    private val i0Closed1Open = Interval(xp(0), xp(1), closedLeft = true, closedRight = false)

    @Test
    fun testIntervalContains() {
        val interval = Interval(xp(1), xp(2), closedLeft = true, closedRight = false)
        assertTrue(interval.contains(xp(1), exprDoubleValueComparator)!!)
    }

    @Test
    fun testIntervalIntersectionWithInterval() {
        // one interval subset of another
        assertIntersection(i0Closed3Open, i1Closed2Open, i1Closed2Open)

        // intersection is a single element
        assertIntersection(iMinus1Closed1Closed, i1Closed2Open, FiniteSet(listOf(xp(1))))
        assertIntersection(iMinus2OpenMinus1Closed, iMinus1Closed1Closed, FiniteSet(listOf(xp(-1))))

        // intersection is an empty set
        assertIntersection(iMinus2OpenMinus1Open, i0Closed3Open, emptySet)
        assertIntersection(iMinus1Closed1Closed, iMinus2OpenMinus1Open, emptySet)

        // non-finite end points intersection
        assertIntersection(iMinusInf1Closed, i1Closed2Open, FiniteSet(listOf(xp(1))))
        assertIntersection(iMinusInf1Closed, iMinusInf1Closed, iMinusInf1Closed)

        // infinite interval intersection
        assertIntersection(iMinusInfPlusInf, i0Closed1Open, i0Closed1Open)
    }

    @Test
    fun testIntervalUnionWithInterval() {
        // one interval subset of another
        assertUnion(i0Closed3Open, i1Closed2Open, i0Closed3Open)

        // union, where ends are joined to form interval union
        assertUnion(iMinus1Closed1Closed, i1Closed2Open, iMinus1Closed2Open)
        assertUnion(iMinus2OpenMinus1Closed, iMinus1Closed1Closed, iMinus2Open1Closed)

        assertUnion(iMinus2OpenMinus1Open, i0Closed3Open, SetUnion(listOf(iMinus2OpenMinus1Open, i0Closed3Open)))
        assertUnion(iMinus1Closed1Closed, iMinus2OpenMinus1Open, iMinus2Open1Closed)

        // non-finite end points intersection
        assertUnion(iMinusInf1Closed, i1Closed2Open, iMinusInf2Open)
        assertUnion(iMinusInf1Closed, iMinusInf1Closed, iMinusInf1Closed)

        // infinite interval union
        assertUnion(iMinusInfPlusInf, i0Closed1Open, iMinusInfPlusInf)
    }
}
