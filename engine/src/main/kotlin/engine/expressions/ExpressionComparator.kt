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
 * 1. whether they are the same verbatim, or
 * 2. Whether they are the same decimal number, or
 * 3. whether they are the same rational number, or
 * 4. their sign
 */
object SimpleComparator : ExpressionComparator {
    override fun compare(e1: Expression, e2: Expression): Sign {
        if (e1 is Minus && e2 is Minus) {
            return compare(e2.argument, e1.argument)
        }
        return verbatimCompare(e1, e2)
            ?: decimalCompare(e1, e2)
            ?: rationalCompare(e1, e2)
            ?: signCompare(e1, e2)
    }

    private inline fun signCompare(e1: Expression, e2: Expression): Sign {
        val s1 = e1.signOf()
        val s2 = e2.signOf()
        return s1 - s2
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

    private inline fun decimalCompare(e1: Expression, e2: Expression): Sign? {
        val d1 = e1.asDecimal() ?: return null
        val d2 = e2.asDecimal() ?: return null
        return Sign.fromInt((d1 - d2).signum())
    }
}
