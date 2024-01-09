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

class FractionPattern : KeyedPattern {
    val numerator = AnyPattern()
    val denominator = AnyPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key
}

class IntegerFractionPattern : KeyedPattern {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key
}

class RationalPattern : KeyedPattern {
    private val numerator = UnsignedIntegerPattern()
    private val denominator = UnsignedIntegerPattern()

    private val options = oneOf(
        numerator,
        fractionOf(numerator, denominator),
    )

    private val ptn = optionalNegOf(options)

    override val key = ptn.key
}
