package engine.expressions

import engine.sign.Sign

fun interface ExpressionComparator {

    /**
     * Returns
     * - Sign.POSITIVE if e1 > e2,
     * - Sign.NEGATIVE if e1 < e2,
     * - Sign.ZERO if e1 == e2,
     * - Sign.UNKOWN if it can't tell but e1 and e2 don't have a sign equal to Sign.NONE
     * - Sign.NONE if either e1 or e2 have sign equal to Sign.NONE
     */
    fun compare(e1: Expression, e2: Expression): Sign
}

/**
 * A simple expression comparator that can compare expressions based on
 * 1. their sign
 * 2. whether they are the same verbatim or
 * 3. whether they are the same rational number.
 */
object SimpleComparator : ExpressionComparator {
    override fun compare(e1: Expression, e2: Expression): Sign {
        if (e1.isNeg() && e2.isNeg()) {
            return compare(e2.firstChild, e1.firstChild)
        }
        return signCompare(e1, e2) ?: verbatimCompare(e1, e2) ?: rationalCompare(e1, e2) ?: Sign.UNKNOWN
    }

    private inline fun signCompare(e1: Expression, e2: Expression): Sign? {
        val s1 = e1.signOf()
        val s2 = e2.signOf()
        val signDiff = s1 - s2
        return if (signDiff.isKnown() || !s1.isKnown() || !s2.isKnown()) signDiff else null
    }

    private inline fun verbatimCompare(e1: Expression, e2: Expression): Sign? {
        return if (e1 == e2) Sign.ZERO else null
    }
    private inline fun rationalCompare(e1: Expression, e2: Expression): Sign? {
        val q1 = e1.asRational()
        val q2 = e2.asRational()
        return if (q1 != null && q2 != null) {
            Sign.fromInt((q1 - q2).numerator.signum())
        } else {
            null
        }
    }
}
