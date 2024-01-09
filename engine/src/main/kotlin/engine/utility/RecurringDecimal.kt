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

import java.math.BigDecimal

private const val SIGNIFICANT_DIGITS_IN_DOUBLE = 15

data class RecurringDecimal(
    val nonRepeatingValue: BigDecimal,
    val repeatingDigits: Int,
) {
    init {
        require(nonRepeatingValue.signum() > 0)
        require(repeatingDigits > 0)
        require(repeatingDigits <= nonRepeatingValue.scale())
    }

    val decimalDigits = nonRepeatingValue.scale()
    val nonRepeatingDigits = decimalDigits - repeatingDigits
    val repetend = nonRepeatingValue
        .remainder(BigDecimal.ONE.movePointLeft(decimalDigits - repeatingDigits))

    constructor(s: String, repeatingDigits: Int) : this(BigDecimal(s), repeatingDigits)

    private fun expandToDecimal(n: Int): BigDecimal {
        var decimal = nonRepeatingValue
        // E.g. 0.065 for 3.5[65]
        var repetend = repetend

        while (decimal.scale() < n) {
            repetend = repetend.movePointLeft(repeatingDigits)
            decimal += repetend
        }
        return decimal
    }

    /**
     * Returns a RecurringDecimal equivalent to the current such that
     * its [decimalDigits] are at least [n]
     */
    fun expand(n: Int) = RecurringDecimal(expandToDecimal(n), repeatingDigits)

    /**
     * Moves the decimal point [n] places to the right, expanding the recurring decimal as necessary.
     */
    fun movePointRight(n: Int): RecurringDecimal {
        return when {
            n <= decimalDigits - repeatingDigits -> RecurringDecimal(
                nonRepeatingValue.movePointRight(n),
                repeatingDigits,
            )

            else -> expand(n + repeatingDigits).movePointRight(n)
        }
    }

    override fun toString(): String {
        val s = nonRepeatingValue.toPlainString()
        val repeatingStartIndex = s.length - repeatingDigits
        return "${s.substring(0, repeatingStartIndex)}[${s.substring(repeatingStartIndex)}]"
    }

    /**
     * Convert to a Double
     */
    fun toDouble() = expandToDecimal(SIGNIFICANT_DIGITS_IN_DOUBLE).toDouble()
}
