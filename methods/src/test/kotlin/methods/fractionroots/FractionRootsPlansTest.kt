package methods.fractionroots

import methods.plans.testPlan

class FractionRootsPlansTest {

    // @Test
    fun testRationalizationWithRadicalInDenominator() = testPlan {
        plan = rationalizationWithRadicalInDenominator
        inputExpr = "[ 5 / 3 * sqrt[3] ]"

        check {
            toExpr = "[5 * sqrt[3] / 9]"
        }
    }

    // @Test
    fun testEvaluateMultiplicationOfFractionWithUnitaryRadicalFraction() = testPlan {
        plan = evaluateMultiplicationOfFractionWithUnitaryRadicalFraction
        inputExpr = "[4 / 3 * sqrt[3]] * [sqrt[3] / sqrt[3]]"

        check {
            toExpr = "[4 * sqrt[3] / 9]"

            step {
                toExpr = "[4 * sqrt[3] / 3 * sqrt[3] * sqrt[3]]"
            }

            step {
                toExpr = "[4 * sqrt[3] / 3 * 3]"
            }

            step {
                toExpr = "[4 * sqrt[3] / 9]"
            }
        }
    }

    // @Test
    fun testEvaluateSquareRootFractions() = testPlan {
        plan = evaluateSquareRootFractions
        inputExpr = "sqrt[ [ 4 / 27 ] ]"

        check {
            toExpr = "[ 2 * sqrt[3] / 9 ]"

            step {
                fromExpr = "sqrt[ [ 4 / 27 ] ]"
                toExpr = "[ sqrt[4] / sqrt[27] ]"

                explanation {
                    key = Explanation.DistributeRadicalRuleOverFractionsToNumeratorAndDenominator
                }
            }

            step {
                fromExpr = "[ sqrt[4] / sqrt[27] ]"
                toExpr = "[ 2 / 3 * sqrt[3] ]"
            }

            step {
                fromExpr = "[ 2 / 3 * sqrt[3] ]"
                toExpr = "[ 2 * sqrt[3] / 9]"
            }
        }
    }
}
