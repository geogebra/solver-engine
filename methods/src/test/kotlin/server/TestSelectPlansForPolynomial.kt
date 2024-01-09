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

package server

import methods.fallback.FallbackPlans
import methods.polynomials.PolynomialsPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForPolynomial {
    @Test
    fun testExpandSimpleExpression() {
        testSelectPlanApiInX(
            "x(x + 1)",
            setOf(
                PolynomialsPlans.ExpandPolynomialExpression,
            ),
        )
    }

    @Test
    fun testSimplifiedLinearExpression() {
        testSelectPlanApi(
            "2x + 1",
            setOf(
                FallbackPlans.ExpressionIsFullySimplified,
            ),
        )
    }

    @Test
    fun testSimplifiedMonomial() {
        testSelectPlanApi(
            "-5[x ^ 7]",
            setOf(
                FallbackPlans.ExpressionIsFullySimplified,
            ),
        )
    }

    @Test
    fun testIrreducibleQuadratic() {
        testSelectPlanApi(
            "[x^2] + 2x + 10",
            setOf(
                FallbackPlans.QuadraticIsIrreducible,
            ),
        )
    }
}
