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
    @Test
    fun testIntervalContains() {
        val interval = Interval(xp(1), xp(2), closedLeft = true, closedRight = false)
        assertTrue(interval.contains(xp(1), exprDoubleValueComparator)!!)
    }

    @Test
    fun testIntervalIntersection() {
        // one interval subset of another
        val i0Closed3Open = Interval(xp(0), xp(3), closedLeft = true, closedRight = true)
        val i1Closed2Open = Interval(xp(1), xp(2), closedLeft = true, closedRight = false)
        assertIntersection(i0Closed3Open, i1Closed2Open, i1Closed2Open)

        // intersection is a single element
        val iMinus1Closed1Closed = Interval(xp(-1), xp(1), closedLeft = true, closedRight = true)
        val iMinusTwoOpenMinusOneClosed = Interval(xp(-2), xp(-1), closedLeft = false, closedRight = true)
        assertIntersection(iMinus1Closed1Closed, i1Closed2Open, FiniteSet(listOf(xp(1))))
        assertIntersection(iMinusTwoOpenMinusOneClosed, iMinus1Closed1Closed, FiniteSet(listOf(xp(-1))))

        // intersection is an empty set
        val iMinusTwoOpenMinusOneOpen = Interval(xp(-2), xp(-1), closedLeft = false, closedRight = false)
        assertIntersection(iMinusTwoOpenMinusOneOpen, i0Closed3Open, emptySet)
        assertIntersection(iMinus1Closed1Closed, iMinusTwoOpenMinusOneOpen, emptySet)

        // non-finite end points intersection
        val iMinusInf1Closed = Interval(Constants.NegativeInfinity, xp(1), closedLeft = false, closedRight = true)
        assertIntersection(iMinusInf1Closed, i1Closed2Open, FiniteSet(listOf(xp(1))))
        assertIntersection(iMinusInf1Closed, iMinusInf1Closed, iMinusInf1Closed)

        val iMinusInfPlusInf = Interval(
            Constants.NegativeInfinity,
            Constants.Infinity,
            closedLeft = false,
            closedRight = false,
        )
        val i0Closed1Open = Interval(xp(0), xp(1), closedLeft = true, closedRight = false)
        // infinite interval intersection
        assertIntersection(iMinusInfPlusInf, i0Closed1Open, i0Closed1Open)
    }
}
