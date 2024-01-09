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

package engine.patterns

import engine.expressions.Constants
import java.math.BigInteger

class OptionalPowerPattern(val base: Pattern, exponent: Pattern) : KeyedPattern {
    private val ptn = oneOf(powerOf(base, exponent), base)

    /**
     * This is either the exponent or just a path provider that returns 1 if the pattern wasn't a power.
     */
    val exponent = ProviderWithDefault(exponent, Constants.One)

    override val key = ptn
}

class OptionalIntegerPowerPattern(val base: Pattern, exponent: IntegerPattern) : KeyedPattern {
    private val ptn = oneOf(powerOf(base, exponent), base)

    /**
     * This is either the exponent or just a path provider that returns 1 if the pattern wasn't a power.
     */
    val exponent = IntegerProviderWithDefault(exponent, BigInteger.ONE)

    override val key = ptn
}

fun optionalPowerOf(base: Pattern, exponent: Pattern = AnyPattern()) = OptionalPowerPattern(base, exponent)

fun optionalIntegerPowerOf(base: Pattern, exponent: IntegerPattern = UnsignedIntegerPattern()) =
    OptionalIntegerPowerPattern(
        base,
        exponent,
    )
