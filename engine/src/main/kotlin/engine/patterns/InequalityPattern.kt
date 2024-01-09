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
import engine.expressions.Expression
import engine.expressions.Inequality
import engine.operators.Comparator

class InequalityPattern internal constructor(
    val lhs: Pattern,
    val rhs: Pattern,
    private val comparator: Comparator? = null,
) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (subexpression !is Inequality) {
            return emptySequence()
        }

        if (comparator != null && (comparator != subexpression.comparator)) {
            return emptySequence()
        }

        val matchedSolvable = match.newChild(this, subexpression)
        return lhs.findMatches(context, matchedSolvable, subexpression.firstChild).flatMap {
            rhs.findMatches(context, it, subexpression.secondChild)
        }
    }

    override val minDepth = 1 + maxOf(lhs.minDepth, rhs.minDepth)
}

fun inequalityOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs)

fun lessThanOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs, Comparator.LessThan)

fun lessThanEqualOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs, Comparator.LessThanOrEqual)

fun greaterThanOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs, Comparator.GreaterThan)

fun greaterThanEqualOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs, Comparator.GreaterThanOrEqual)
