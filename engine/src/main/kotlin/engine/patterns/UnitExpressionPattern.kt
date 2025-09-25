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
import engine.operators.UnitExpressionOperator
import engine.operators.UnitType

/**
 * A pattern that matches unit expressions with a specific unit type.
 * If [unitType] type is null, it will match any unit type.
 */
data class UnitExpressionPattern(
    val childPattern: Pattern,
    val unitType: UnitType? = null,
) : BasePattern() {
    override fun toString() = "${unitType?.toString() ?: "anyUnit"}[$childPattern]"

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (subexpression.operator !is UnitExpressionOperator ||
            (unitType != null && subexpression.operator.unit != unitType)
        ) {
            return emptySequence()
        }

        val matches = sequenceOf(match.newChild(this, subexpression))

        return childPattern.findMatches(context, matches.first(), subexpression.firstChild)
    }

    override val minDepth = 1

    fun getBoundUnitType(m: Match) = (getBoundExpr(m)?.operator as UnitExpressionOperator).unit
}

fun degreeOf(value: Pattern) = UnitExpressionPattern(value, UnitType.Degree)
