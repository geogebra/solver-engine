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

import engine.expressions.Expression
import engine.sign.Sign

/**
 * Returns true if the expression is definitely known to be non-positive (negative or zero),
 * according to some heuristics
 */
fun Expression.isDefinitelyNotPositive(): Boolean = isDefinitely(Sign.NON_POSITIVE)

fun Expression.isDefinitelyNotNegative(): Boolean = isDefinitely(Sign.NON_NEGATIVE)

fun Expression.isDefinitelyNegative() = isDefinitely(Sign.NEGATIVE)

fun Expression.isDefinitelyPositive() = isDefinitely(Sign.POSITIVE)

fun Expression.isDefinitely(wantedSign: Sign): Boolean {
    val sign = signOf()
    return when {
        sign.implies(wantedSign) -> true
        sign.implies(wantedSign.negation()) -> false
        else -> Sign.fromDouble(doubleValue).implies(wantedSign)
    }
}
