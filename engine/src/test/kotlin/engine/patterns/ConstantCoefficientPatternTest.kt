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
import engine.expressions.RootOrigin
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class ConstantCoefficientPatternTest {
    private fun assertCoefficientEquals(expr: String, coefficient: String?) {
        val solutionVariablePattern = SolutionVariablePattern()
        val constantCoefficientPattern = withOptionalConstantCoefficient(solutionVariablePattern)

        val exprValue = parseExpression(expr).withOrigin(RootOrigin())

        val matches = constantCoefficientPattern
            .findMatches(Context(solutionVariables = listOf("x")), RootMatch, exprValue)

        if (coefficient == null) {
            assertEquals(0, matches.count())
        } else {
            val coefficientValue = parseExpression(coefficient)
            assertEquals(coefficientValue, constantCoefficientPattern.coefficient(matches.single()))
        }
    }

    @Test
    fun testConstantCoefficientPattern() {
        assertCoefficientEquals("x", "1")
        assertCoefficientEquals("y", null)
        assertCoefficientEquals("x * y", null)
        assertCoefficientEquals("[3 * x / 2]", "[3 / 2]")
        assertCoefficientEquals("[3 * x / 2 * sqrt[3]]", "[3 / 2 * sqrt[3]]")
        assertCoefficientEquals("[x / 2 * sqrt[3]]", "[1 / 2 * sqrt[3]]")
        assertCoefficientEquals("[x / 2 * y]", null)
        assertCoefficientEquals("2x * 3", "2 * 3")
    }
}
