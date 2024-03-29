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

import methods.approximation.ApproximationPlans
import methods.constantexpressions.ConstantExpressionsPlans
import methods.decimals.DecimalPlans
import methods.fallback.FallbackPlans
import methods.integerarithmetic.IntegerArithmeticPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForConstantExpressions {
    @Test
    fun testAdditionOfTwoIntegers() {
        testSelectPlanApi(
            "1 + 2",
            setOf(
                IntegerArithmeticPlans.EvaluateArithmeticExpression,
            ),
        )
    }

    @Test
    fun testAdditionOfFractions() {
        testSelectPlanApi(
            "[1/2] + [1/5]",
            setOf(
                ConstantExpressionsPlans.SimplifyConstantExpression,
                DecimalPlans.EvaluateExpressionAsDecimal,
            ),
        )
    }

    @Test
    fun testDivisionResultNonInteger() {
        testSelectPlanApi(
            "5 + 6*3:5",
            setOf(
                ApproximationPlans.ApproximateExpression,
                DecimalPlans.EvaluateExpressionAsDecimal,
                ConstantExpressionsPlans.SimplifyConstantExpression,
            ),
        )
    }

    @Test
    fun testConstantAbsoluteValues() {
        testSelectPlanApi(
            "abs[-4] + abs[-1.4 + 3.2]",
            setOf(
                ConstantExpressionsPlans.SimplifyConstantExpression,
                DecimalPlans.EvaluateExpressionAsDecimal,
            ),
        )

        testSelectPlanApi(
            "abs[1 + 2 - 4]",
            setOf(
                ConstantExpressionsPlans.SimplifyConstantExpression,
            ),
        )

        testSelectPlanApi(
            "abs[[1/2] + [1/4]]",
            setOf(
                DecimalPlans.EvaluateExpressionAsDecimal,
                ConstantExpressionsPlans.SimplifyConstantExpression,
            ),
        )

        testSelectPlanApi(
            "abs[[2/5]] + abs[[1/3]]",
            setOf(
                ConstantExpressionsPlans.SimplifyConstantExpression,
            ),
        )
    }

    @Test
    fun testSimplifiedConstant() {
        testSelectPlanApi(
            "2",
            setOf(
                FallbackPlans.ExpressionIsFullySimplified,
            ),
        )
    }

    @Test
    fun testSimplifyExpressionToUndefined() {
        testSelectPlanApiInX(
            "[0 / sqrt[2] - sqrt[2]]",
            setOf(
                ConstantExpressionsPlans.SimplifyConstantExpression,
            ),
        )
    }
}
