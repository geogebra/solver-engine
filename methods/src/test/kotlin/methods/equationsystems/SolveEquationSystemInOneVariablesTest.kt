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

package methods.equationsystems

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class SolveEquationSystemInOneVariablesTest {
    private fun shortTest(equations: String, solution: String) {
        testMethod {
            method = EquationSystemsPlans.SolveEquationSystemInOneVariable
            inputExpr = equations

            check {
                toExpr = solution
            }
        }
    }

    @Test
    fun testSameSolution() =
        shortTest(
            "2x + 1 = 3 AND 4x - 1 = 3",
            "SetSolution[x : {1}]",
        )

    @Test
    fun testDifferentSolutions() =
        shortTest(
            "2x + 1 = 3 AND 3x + 2 = 1",
            "Contradiction[x : 2x + 1 = 3 AND 3x + 2 = 1]",
        )

    @Test
    fun testOneCommonSolution() =
        shortTest(
            "[x ^ 2] = 1 AND x(x - 1) = 0",
            "SetSolution[x : {1}]",
        )

    @Test
    fun testFirstEquationIsIdentity() =
        shortTest(
            "x = x AND 2x =  6",
            "SetSolution[x : {3}]",
        )

    @Test
    fun testSecondEquationIsIdentity() =
        shortTest(
            "[x^2] = 4 AND 2x = x + x",
            "SetSolution[x : {-2, 2}]",
        )

    @Test
    fun testFirstEquationIsContradiction() =
        shortTest(
            "x + 1 = x AND 2x = 3",
            "Contradiction[x : 1 = 0]",
        )

    @Test
    fun testSecondEquationIsContradiction() =
        shortTest(
            "x = x AND x + 1 = x + 2",
            "Contradiction[x : 1 = 2]",
        )
}
