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
import engine.expressions.ConstantChecker
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.defaultConstantChecker

class QuadraticPolynomialPattern(
    val variable: Pattern,
    constantChecker: ConstantChecker = defaultConstantChecker,
) : KeyedPattern, ConstantChecker by constantChecker {
    private fun NaryPattern.restIsConstant(context: Context, match: Match) =
        getRestSubexpressions(match).all { rest -> isConstant(context, rest) }

    private val quadraticTerm = withOptionalConstantCoefficient(
        powerOf(variable, FixedPattern(Constants.Two)),
        constantChecker,
    )

    private val linearTerm = withOptionalConstantCoefficient(variable, constantChecker)

    private val completeQuadraticPolynomial = commutativeSumContaining(quadraticTerm, linearTerm)

    private val incompleteQuadraticPolynomial = commutativeSumContaining(quadraticTerm)

    private val quadraticPolynomial = oneOf(
        ConditionPattern(completeQuadraticPolynomial) { context, match, _ ->
            completeQuadraticPolynomial.restIsConstant(context, match)
        },
        ConditionPattern(incompleteQuadraticPolynomial) { context, match, _ ->
            incompleteQuadraticPolynomial.restIsConstant(context, match)
        },
    )

    override val key = quadraticPolynomial

    fun quadraticCoefficient(match: Match) = quadraticTerm.coefficient(match)

    fun linearCoefficient(match: Match): Expression {
        return when {
            linearTerm.getBoundExpr(match) != null -> linearTerm.coefficient(match)
            else -> Constants.Zero
        }
    }

    fun constantTerm(match: Match): Expression {
        return when {
            completeQuadraticPolynomial.getBoundExpr(match) != null ->
                completeQuadraticPolynomial.substitute(match, arrayOf())
            else -> incompleteQuadraticPolynomial.substitute(match, arrayOf())
        }
    }
}
