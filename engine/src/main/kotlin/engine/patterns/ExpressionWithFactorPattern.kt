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

import engine.expressions.Expression
import engine.expressions.negOf
import engine.expressions.productOf

class ExpressionWithFactorPattern(val factor: Pattern, val positiveOnly: Boolean = false) :
    KeyedPattern, SubstitutablePattern {
    private val productWithFactor = productContaining(factor)
    private val options = oneOf(factor, productWithFactor)
    private val optionalNegPattern = optionalNegOf(options)

    override val key = if (positiveOnly) options.key else optionalNegPattern.key

    override fun substitute(match: Match, newVals: Array<out Expression>): Expression {
        val substituted = if (match.isBound(productWithFactor)) {
            productWithFactor.substitute(match, newVals)
        } else {
            productOf(newVals.toList())
        }

        return if (positiveOnly || !optionalNegPattern.isNeg(match)) substituted else negOf(substituted)
    }
}

fun expressionWithFactor(factor: Pattern, positiveOnly: Boolean = false) =
    ExpressionWithFactorPattern(
        factor,
        positiveOnly,
    )
