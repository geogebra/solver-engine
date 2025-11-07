/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.TrigonometricExpression
import engine.operators.TrigonometricFunctionOperator
import engine.operators.TrigonometricFunctionType

/**
 * Matches a trigonometric expression with the given type
 * If [functionType] is null, it matches any trigonometric expression
 */
data class TrigonometricExpressionPattern(
    val childPattern: Pattern,
    val functionType: List<TrigonometricFunctionType>? = null,
    val powerInside: Boolean? = null,
) : BasePattern() {
    override fun toString() = "${functionType?.toString() ?: "anyTrigonometricExpression"}[$childPattern]"

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        @Suppress("ComplexCondition")
        if (subexpression.operator !is TrigonometricFunctionOperator ||
            (functionType != null && !functionType.contains(subexpression.operator.type)) ||
            (powerInside != null && powerInside != subexpression.operator.powerInside)
        ) {
            return emptySequence()
        }

        val match = match.newChild(this, subexpression)

        return childPattern.findMatches(context, match, subexpression.firstChild)
    }

    fun getBoundFunctionType(m: Match) = (getBoundExpr(m) as TrigonometricExpression).functionType

    override val minDepth = 1
}
