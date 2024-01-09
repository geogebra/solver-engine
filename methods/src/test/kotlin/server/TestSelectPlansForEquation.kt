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

/**
 * this file contains tests for all SelectPlanApiService
 * for where the input is an equation
 */
package server

import methods.algebra.AlgebraPlans
import methods.equations.EquationsPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForEquation {
    @Test
    fun `test constant equation`() {
        testSelectPlanApi(
            "3 + 4 = 7",
            setOf(
                EquationsPlans.SolveConstantEquation,
            ),
        )
    }

    @Test
    fun `test linear equation with fractions`() {
        testSelectPlanApiInX(
            "2x = [1/2]",
            setOf(
                EquationsPlans.SolveEquation,
            ),
        )
    }

    @Test
    fun `test linear equation with decimal coefficients but with no exact decimal solution`() {
        testSelectPlanApiInX(
            "3.1 x + 2.2 = 1.21",
            setOf(
                EquationsPlans.SolveEquation,
            ),
        )
    }

    @Test
    fun `test trivial linear equation containing recurring decimal`() {
        testSelectPlanApiInX(
            "2x + 2.2[3] = x",
            setOf(
                EquationsPlans.SolveEquation,
                EquationsPlans.SolveDecimalLinearEquation,
            ),
        )
    }

    @Test
    fun `test non-trivial linear equation containing recurring decimal`() {
        testSelectPlanApiInX(
            "3.1x + 2.2[3] = 1.21",
            setOf(
                EquationsPlans.SolveEquation,
            ),
        )
    }

    @Test
    fun `test linear equations with decimal coefficients with solution expressible as an exact decimal`() {
        testSelectPlanApiInX(
            "3.2x + 2.2 = 1.2",
            setOf(
                EquationsPlans.SolveEquation,
                EquationsPlans.SolveDecimalLinearEquation,
            ),
        )
    }

    @Test
    fun `test rational equation compute domain and solve`() {
        testSelectPlanApiInX(
            "1 + [1 / x] = [2 / x]",
            setOf(
                // this plan probably shouldn't be executed
                AlgebraPlans.ComputeDomainOfAlgebraicExpression,
                EquationsPlans.SolveEquation,
            ),
        )
    }
}
