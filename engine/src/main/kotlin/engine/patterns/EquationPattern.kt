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
import engine.expressions.Equation
import engine.expressions.Expression
import engine.operators.ExpressionWithConstraintOperator
import engine.operators.StatementSystemOperator
import engine.operators.StatementUnionOperator

data class EquationPattern(
    val lhs: Pattern,
    val rhs: Pattern,
) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (subexpression !is Equation) {
            return emptySequence()
        }

        val matchedEquation = match.newChild(this, subexpression)
        return sequence {
            lhs.findMatches(context, matchedEquation, subexpression.firstChild).forEach {
                yieldAll(rhs.findMatches(context, it, subexpression.secondChild))
            }

            lhs.findMatches(context, matchedEquation, subexpression.secondChild).forEach {
                yieldAll(rhs.findMatches(context, it, subexpression.firstChild))
            }
        }
    }

    override val minDepth = 1 + maxOf(lhs.minDepth, rhs.minDepth)
}

fun commutativeEquationOf(lhs: Pattern, rhs: Pattern) = EquationPattern(lhs, rhs)

fun statementSystemOf(eq1: Pattern, eq2: Pattern) = OperatorPattern(StatementSystemOperator, listOf(eq1, eq2))

fun statementUnionOf(eq1: Pattern, eq2: Pattern) = OperatorPattern(StatementUnionOperator, listOf(eq1, eq2))

fun expressionWithConstraintOf(statement: Pattern, constraint: Pattern) =
    OperatorPattern(ExpressionWithConstraintOperator, listOf(statement, constraint))
