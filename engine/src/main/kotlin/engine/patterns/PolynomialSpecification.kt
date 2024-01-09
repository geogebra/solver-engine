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
import engine.expressions.Expression
import engine.expressions.defaultConstantChecker
import engine.expressions.solutionVariableConstantChecker

/**
 * This encapsulate the data needed to check whether an expression is a polynomial.  The [variable] pattern
 * can detect instances of the variable and the [constantChecker] can verify that other parts are constant.
 */
class PolynomialSpecification(
    val variable: Pattern,
    val constantChecker: ConstantChecker,
) : ConstantChecker by constantChecker

/**
 * Returns the most appropriate [PolynomialSpecification] for the given [expression] in the given [context].
 */
fun defaultPolynomialSpecification(context: Context, expression: Expression): PolynomialSpecification? {
    return when {
        context.solutionVariables.size == 1 -> PolynomialSpecification(
            variable = SolutionVariablePattern(),
            constantChecker = solutionVariableConstantChecker,
        )
        expression.variables.size == 1 -> PolynomialSpecification(
            variable = ArbitraryVariablePattern(),
            constantChecker = defaultConstantChecker,
        )
        else -> null
    }
}
