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

package methods.solvable

import engine.expressions.Expression
import engine.expressions.Power
import engine.expressions.asRational
import engine.expressions.simplifiedFractionOf
import engine.expressions.xp
import engine.utility.Rational
import engine.utility.asPower
import java.math.BigInteger

private data class IntegerPower(
    val base: BigInteger,
    val exponent: BigInteger,
)

private data class RationalPower(
    val base: Rational,
    val exponent: BigInteger,
)

/**
 * Finds if [n1] and [n2] can be written (simply) with the same base. If not, returns null, else returns
 * (b, e1, e2) such that n1 = b^e1 and n2 = b^e2.
 */
internal fun withSameBase(n1: Rational, n2: Rational): Triple<Rational, BigInteger, BigInteger>? {
    val n1Powers = n1.asPowerCandidates()
    val n2Powers = n2.asPowerCandidates()

    val matches = n1Powers.flatMap { p1 ->
        n2Powers
            .filter { p2 -> p2.base == p1.base }
            .map { p2 -> Triple(p1.base, p1.exponent, p2.exponent) }
    }

    if ((n1.denominator == BigInteger.ONE) != (n2.denominator == BigInteger.ONE)) {
        return matches.firstOrNull { (base, _, _) -> base.denominator == BigInteger.ONE }
    }

    return matches.firstOrNull()
}

private fun Rational.asPowerCandidates(): List<RationalPower> {
    val rational = simplify()
    if (rational.numerator <= BigInteger.ZERO || rational.denominator <= BigInteger.ZERO) {
        return emptyList()
    }

    return buildList {
        addAll(rationalPowerCandidates(rational, BigInteger.ONE))
        addAll(rational.decomposedPowerCandidates())
    }
}

private fun Rational.decomposedPowerCandidates(): List<RationalPower> {
    val numeratorPowers = numerator.integerPowerCandidates()
    val denominatorPowers = denominator.integerPowerCandidates()

    return when {
        denominator == BigInteger.ONE -> numeratorPowers.flatMap { numeratorPower ->
            rationalPowerCandidates(Rational(numeratorPower.base), numeratorPower.exponent)
        }
        numerator == BigInteger.ONE -> denominatorPowers.flatMap { denominatorPower ->
            rationalPowerCandidates(Rational(BigInteger.ONE, denominatorPower.base), denominatorPower.exponent)
        }
        else -> matchingRationalPowerCandidates(numeratorPowers, denominatorPowers)
    }
}

private fun matchingRationalPowerCandidates(
    numeratorPowers: List<IntegerPower>,
    denominatorPowers: List<IntegerPower>,
): List<RationalPower> {
    return numeratorPowers.flatMap { numeratorPower ->
        denominatorPowers
            .filter { denominatorPower -> denominatorPower.exponent == numeratorPower.exponent }
            .flatMap { denominatorPower ->
                rationalPowerCandidates(
                    Rational(numeratorPower.base, denominatorPower.base),
                    numeratorPower.exponent,
                )
            }
    }
}

private fun rationalPowerCandidates(base: Rational, exponent: BigInteger): List<RationalPower> {
    val simplifiedBase = base.simplify()
    return listOf(
        RationalPower(simplifiedBase, exponent),
        RationalPower(simplifiedBase.inverse(), -exponent),
    )
}

private fun BigInteger.integerPowerCandidates(): List<IntegerPower> {
    return asPower().map { IntegerPower(it.base, it.exponent) } + IntegerPower(this, BigInteger.ONE)
}

internal fun Expression.isPowerOfTen(): Boolean =
    when (this) {
        is Power -> base.isPowerOfTen()
        else ->
            asRational()
                ?.asPowerCandidates()
                ?.any { it.base.sameNumber(BigInteger.TEN) }
                ?: false
    }

internal fun Rational.isSupportedExponentialBase(): Boolean {
    return !isZero() && !isNeg() && !sameNumber(BigInteger.ONE)
}

private fun Rational.inverse() = Rational(denominator, numerator).simplify()

internal fun xp(rational: Rational): Expression {
    val simplified = rational.simplify()
    return simplifiedFractionOf(xp(simplified.numerator), xp(simplified.denominator))
}
