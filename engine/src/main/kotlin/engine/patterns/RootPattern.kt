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

import engine.context.Context
import engine.expressions.Constants
import engine.expressions.Expression
import engine.operators.BinaryExpressionOperator
import engine.operators.UnaryExpressionOperator

/**
 * A pattern that matches either `rootOf(radicand, order)` or `squareRootOf(radicand)` by matching
 * order to a newly introduced expression with a value of 2.
 * It allows treating both in a uniform way in rules.
 */
class RootPattern<T : Pattern>(val radicand: Pattern, val order: T) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression.operator) {
            BinaryExpressionOperator.Root -> {
                val m = match.newChild(this, subexpression)
                radicand.findMatches(context, m, subexpression.firstChild)
                    .flatMap { order.findMatches(context, it, subexpression.secondChild) }
            }
            UnaryExpressionOperator.SquareRoot -> {
                val m = match.newChild(this, subexpression)
                radicand.findMatches(context, m, subexpression.firstChild)
                    .flatMap { order.findMatches(context, it, Constants.Two) }
            }
            else -> emptySequence()
        }
    }

    override val minDepth = 1 + maxOf(radicand.minDepth, order.minDepth)
}

fun rootOf(radicand: Pattern, index: Pattern = AnyPattern()) = RootPattern(radicand, index)

fun integerOrderRootOf(radicand: Pattern) = RootPattern(radicand, UnsignedIntegerPattern())
