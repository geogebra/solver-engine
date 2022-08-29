package methods.fractionroots

import methods.constantexpressions.simplifyConstantExpression
import methods.general.GeneralExplanation
import methods.integerroots.IntegerRootsExplanation
import methods.plans.testPlan
import org.junit.jupiter.api.Test

class FractionRootsPlansTest {

    @Test
    fun testRationalizationOfSimpleDenominators() = testPlan {
        plan = rationalizeDenominators
        inputExpr = "sqrt[[3 / 2]]"

        check {
            toExpr = "[sqrt[6] / 2]"
            explanation {
                key = FractionRootsExplanation.RationalizeDenominator
            }

            step {
                toExpr = "[sqrt[3] / sqrt[2]]"
                explanation {
                    key = FractionRootsExplanation.DistributeRadicalOverFraction
                }
            }

            step {
                toExpr = "[sqrt[3] / sqrt[2]] * [sqrt[2] / sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[3] * sqrt[2] / sqrt[2] * sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[6] / sqrt[2] * sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[6] / 2]"
            }
        }
    }

    @Test
    fun testRationalizationRadicalWithCoefficient() = testPlan {
        plan = rationalizeDenominators
        inputExpr = "[sqrt[3] / 3 * sqrt[2]]"

        check {
            toExpr = "[sqrt[6] / 6]"
            explanation {
                key = FractionRootsExplanation.RationalizeDenominator
            }

            step {
                toExpr = "[sqrt[3] / 3 * sqrt[2]] * [sqrt[2] / sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[3] * sqrt[2] / 3 * sqrt[2] * sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[6] / 3 * sqrt[2] * sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[6] / 6]"
            }
        }
    }

    @Test
    fun testRationalizationWithSumOfRadicalsInNumerator() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"

        check {
            fromExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"
            toExpr = "[2 + sqrt[6] / 2]"

            step {
                fromExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"
                toExpr = "[(sqrt[2] + sqrt[3]) * sqrt[2] / 2]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }
            }

            step {
                fromExpr = "[(sqrt[2] + sqrt[3]) * sqrt[2] / 2]"
                toExpr = "[sqrt[2] * sqrt[2] + sqrt[3] * sqrt[2] / 2]"
                explanation {
                    key = GeneralExplanation.DistributeMultiplicationOverSum
                }
            }

            step {
                fromExpr = "[sqrt[2] * sqrt[2] + sqrt[3] * sqrt[2] / 2]"
                toExpr = "[2 + sqrt[3] * sqrt[2] / 2]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }

                step {
                    fromExpr = "sqrt[2] * sqrt[2]"
                    toExpr = "2"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyMultiplicationOfSquareRoots
                    }
                }
            }

            step {
                fromExpr = "[2 + sqrt[3] * sqrt[2] / 2]"
                toExpr = "[2 + sqrt[6] / 2]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }
        }
    }
}
