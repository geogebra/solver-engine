package methods.fractionroots

import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerroots.IntegerRootsExplanation
import methods.plans.testPlan
import org.junit.jupiter.api.Test

class FractionRootsPlansTest {

    @Test
    fun testRationalizationOfSimpleDenominators() = testPlan {
        plan = rationalizeDenominators
        inputExpr = "sqrt[[3 / 2]]"

        check {
            fromExpr = "sqrt[[3 / 2]]"
            toExpr = "[sqrt[6] / 2]"

            step {
                fromExpr = "sqrt[[3 / 2]]"
                toExpr = "[sqrt[3] / sqrt[2]]"
                explanation {
                    key = FractionRootsExplanation.DistributeRadicalOverFraction
                }
            }

            step {
                fromExpr = "[sqrt[3] / sqrt[2]]"
                toExpr = "[sqrt[3] * sqrt[2] / sqrt[2] * sqrt[2]]"
                explanation {
                    key = FractionRootsExplanation.RationalizeSimpleDenominator
                }
            }

            step {
                fromExpr = "[sqrt[3] * sqrt[2] / sqrt[2] * sqrt[2]]"
                toExpr = "[sqrt[6] / sqrt[2] * sqrt[2]]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }

            step {
                fromExpr = "[sqrt[6] / sqrt[2] * sqrt[2]]"
                toExpr = "[sqrt[6] / 2]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }
        }
    }

    @Test
    fun testRationalizationRadicalWithCoefficient() = testPlan {
        plan = rationalizeDenominators
        inputExpr = "[sqrt[3] / 3 * sqrt[2]]"

        check {
            fromExpr = "[sqrt[3] / 3 * sqrt[2]]"
            toExpr = "[sqrt[6] / 6]"

            step {
                fromExpr = "[sqrt[3] / 3 * sqrt[2]]"
                toExpr = "[sqrt[3] * sqrt[2] / 3 * sqrt[2] * sqrt[2]]"
                explanation {
                    key = FractionRootsExplanation.RationalizeSimpleDenominatorWithCoefficient
                }
            }

            step {
                fromExpr = "[sqrt[3] * sqrt[2] / 3 * sqrt[2] * sqrt[2]]"
                toExpr = "[sqrt[6] / 3 * sqrt[2] * sqrt[2]]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }

                step {
                    fromExpr = "sqrt[3] * sqrt[2]"
                    toExpr = "sqrt[3 * 2]"
                    explanation {
                        key = IntegerRootsExplanation.MultiplyNthRoots
                    }
                }

                step {
                    fromExpr = "sqrt[3 * 2]"
                    toExpr = "sqrt[6]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                    }

                    step {
                        fromExpr = "3 * 2"
                        toExpr = "6"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }
            }

            step {
                fromExpr = "[sqrt[6] / 3 * sqrt[2] * sqrt[2]]"
                toExpr = "[sqrt[6] / 6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }
        }
    }

    @Test
    fun testRationalizationWithSumOfRadicalsInNumerator() = testPlan {
        plan = rationalizeDenominators
        inputExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"

        check {
            fromExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"
            toExpr = "[(sqrt[2] + sqrt[3]) * sqrt[2] / 2]"

            step {
                fromExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"
                toExpr = "[(sqrt[2] + sqrt[3]) * sqrt[2] / sqrt[2] * sqrt[2]]"
                explanation {
                    key = FractionRootsExplanation.RationalizeSimpleDenominator
                }
            }

            step {
                fromExpr = "[(sqrt[2] + sqrt[3]) * sqrt[2] / sqrt[2] * sqrt[2]]"
                toExpr = "[(sqrt[2] + sqrt[3]) * sqrt[2] / 2]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }
        }
    }
}
