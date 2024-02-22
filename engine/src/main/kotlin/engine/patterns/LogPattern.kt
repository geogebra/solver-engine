/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
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
import engine.expressions.Constants
import engine.expressions.Expression
import engine.operators.BinaryExpressionOperator
import engine.operators.UnaryExpressionOperator

class LogPattern(val base: Pattern, val argument: Pattern) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression.operator) {
            BinaryExpressionOperator.Log -> {
                val m = match.newChild(this, subexpression)
                base.findMatches(context, m, subexpression.firstChild).flatMap {
                    argument.findMatches(context, it, subexpression.secondChild)
                }
            }
            UnaryExpressionOperator.LogBase10 -> {
                val m = match.newChild(this, subexpression)
                base.findMatches(context, m, Constants.Ten).flatMap {
                    argument.findMatches(context, it, subexpression.firstChild)
                }
            }
            UnaryExpressionOperator.NaturalLog -> {
                val m = match.newChild(this, subexpression)
                base.findMatches(context, m, Constants.E).flatMap {
                    argument.findMatches(context, it, subexpression.firstChild)
                }
            }
            else -> emptySequence()
        }
    }
}

fun logOf(argument: Pattern, base: Pattern? = null) = LogPattern(base ?: AnyPattern(), argument)
