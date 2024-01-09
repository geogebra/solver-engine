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

package engine.utility

import java.math.BigInteger

/**
 * Convenience data structure for arithmetic operations on rational numbers.
 */
data class Rational(val numerator: BigInteger, val denominator: BigInteger = BigInteger.ONE) {
    init {
        check(!denominator.isZero())
    }

    constructor(numerator: Int, denominator: Int = 1) : this(numerator.toBigInteger(), denominator.toBigInteger())

    operator fun unaryMinus() = Rational(-numerator, denominator)

    operator fun plus(other: Rational) =
        Rational(
            numerator * other.denominator + denominator * other.numerator,
            denominator * other.denominator,
        ).simplify()

    operator fun minus(other: Rational) =
        Rational(
            numerator * other.denominator - denominator * other.numerator,
            denominator * other.denominator,
        ).simplify()

    operator fun times(other: Rational) =
        Rational(
            numerator * other.numerator,
            denominator * other.denominator,
        ).simplify()

    operator fun div(other: Rational) =
        Rational(
            numerator * other.denominator,
            denominator * other.numerator,
        ).simplify()

    fun simplify(): Rational {
        val gcd = numerator.gcd(denominator)
        if (gcd == BigInteger.ONE) {
            return this
        }
        return Rational(numerator / gcd, denominator / gcd)
    }

    fun squared() = this * this

    fun cubed() = this * this * this

    fun isZero() = numerator.isZero()

    fun isNeg() = numerator * denominator < BigInteger.ZERO

    fun sameNumber(other: BigInteger) = numerator == denominator * other

    fun sameNumber(other: Int) = numerator == denominator * other.toBigInteger()

    fun sameNumber(other: Rational) = numerator * other.denominator == denominator * other.numerator

    override fun equals(other: Any?): Boolean {
        return this === other || other is Rational && sameNumber(other)
    }
}

operator fun Int.times(r: Rational) = Rational(this) * r
