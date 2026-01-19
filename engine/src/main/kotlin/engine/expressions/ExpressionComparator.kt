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

class IncomparableExpressionsException(e1: Expression, e2: Expression) :
    Exception("$e1 and $e2 are incomparable")

fun interface ExpressionComparator : Comparator<Expression> {
    /**
     * Returns
     * - Sign.POSITIVE if e1 > e2,
     * - Sign.NEGATIVE if e1 < e2,
     * - Sign.ZERO if e1 == e2,
     * - Sign.UNKNOWN if it can't tell but e1 and e2 don't have a sign equal to Sign.NONE
     * - Sign.NONE if either e1 or e2 have sign equal to Sign.NONE
     */
    fun compareExpressions(e1: Expression, e2: Expression): Sign

    override fun compare(e1: Expression, e2: Expression): Int {
        val sign = compareExpressions(e1, e2)
        if (!sign.isKnown() || sign.signum != 0 && sign.canBeZero) {
            throw IncomparableExpressionsException(e1, e2)
        }
        return sign.signum
    }
}

/**
 * A simple expression comparator that can compare expressions based on
 * 1. whether they are the same verbatim, or
 * 2. Whether they are the same decimal number, or
 * 3. whether they are the same rational number, or
 * 4. their sign
 */
object SimpleComparator : ExpressionComparator {
    override fun compareExpressions(e1: Expression, e2: Expression): Sign {
        if (e1 is Minus && e2 is Minus) {
            return compareExpressions(e2.argument, e1.argument)
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
        val q1 = e1.asRational() ?: return null
        val q2 = e2.asRational() ?: return null
        return Sign.fromInt((q1 - q2).numerator.signum())
    }

    private inline fun decimalCompare(e1: Expression, e2: Expression): Sign? {
        val d1 = e1.asDecimal() ?: return null
        val d2 = e2.asDecimal() ?: return null
        return Sign.fromInt((d1 - d2).signum())
    }
}
