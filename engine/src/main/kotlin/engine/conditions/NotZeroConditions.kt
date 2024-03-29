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

package engine.conditions

import engine.context.emptyContext
import engine.expressions.Expression
import engine.patterns.RationalPattern
import engine.patterns.RootMatch
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.integerOrderRootOf
import engine.patterns.oneOf
import engine.patterns.withOptionalRationalCoefficient
import engine.sign.Sign
import engine.utility.hasFactorOfDegree
import java.math.BigInteger

/**
 * Returns true if the expression is definitely known to be non-zero, according to some heuristics
 */
fun Expression.isNotZeroBySign(): Boolean {
    val sign = signOf()
    return sign != Sign.NONE && !sign.canBeZero
}

/**
 * Returns true if the [terms] are rational or integer roots (of any order) and are all incommensurable
 */
fun sumTermsAreIncommensurable(terms: List<Expression>): Boolean {
    // Define the pattern for acceptable terms
    val radicandPtn = UnsignedIntegerPattern()
    val rootPtn = integerOrderRootOf(radicandPtn)
    val rootTermPtn = withOptionalRationalCoefficient(rootPtn)
    val fractionTermPtn = RationalPattern()
    val termPtn = oneOf(fractionTermPtn, rootTermPtn)

    // We record the pair (a, n) for each term of the form x * root[a, n] where x is rational.  For rational
    // coefficients, we use the pair (1, 0).  For invalid terms we use the pair (0, 0). If the same pair is not
    // encountered twice then all subexpressions are incommensurable so we can return true.

    val invalidKey = Pair(BigInteger.ZERO, BigInteger.ZERO)
    val rationalKey = Pair(BigInteger.ONE, BigInteger.ONE)
    val visited = mutableSetOf(invalidKey)

    for (operand in terms) {
        val match = termPtn.findMatches(emptyContext, RootMatch, operand).firstOrNull()
        val fraction = match?.getBoundExpr(fractionTermPtn)
        val key = when {
            match == null -> invalidKey
            fraction != null -> if (fraction.isNotZeroBySign()) rationalKey else invalidKey
            else -> {
                val coeff = rootTermPtn.coefficient(match)
                val radicand = radicandPtn.getBoundInt(match)
                val order = rootPtn.order.getBoundInt(match)
                if (coeff.isNotZeroBySign() && order >= BigInteger.TWO && rootIsSimplified(radicand, order)) {
                    Pair(radicand, order)
                } else {
                    invalidKey
                }
            }
        }
        if (key in visited) {
            return false
        }
        visited.add(key)
    }
    return true
}

private fun rootIsSimplified(radicand: BigInteger, order: BigInteger): Boolean =
    !radicand.hasFactorOfDegree(
        order.toInt(),
    )
