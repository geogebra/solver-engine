package methods.equations

import engine.methods.testMethod
import engine.methods.testMethodInX
import methods.factor.FactorExplanation
import org.junit.jupiter.api.Test

class EquationsPlansTest {

    @Test
    fun testSimplifyByFactoringNegativeSignOfLeadingCoefficient() = testMethodInX {
        method = EquationsPlans.SimplifyByFactoringNegativeSignOfLeadingCoefficient
        inputExpr = "-2[x^2] + 4x - 2 = 0"

        check {
            fromExpr = "-2 [x ^ 2] + 4 x - 2 = 0"
            toExpr = "2 [x ^ 2] - 4 x + 2 = 0"
            explanation {
                key = EquationsExplanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient
            }

            step {
                fromExpr = "-2 [x ^ 2] + 4 x - 2 = 0"
                toExpr = "-(2 [x ^ 2] - 4 x + 2) = 0"
                explanation {
                    key = FactorExplanation.FactorNegativeSignOfLeadingCoefficient
                }
            }

            step {
                fromExpr = "-(2 [x ^ 2] - 4 x + 2) = 0"
                toExpr = "2 [x ^ 2] - 4 x + 2 = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.NegateBothSides
                }
            }
        }
    }

    @Test
    fun testSimplifyByDividingByGcfOfCoefficients() = testMethodInX {
        method = EquationsPlans.SimplifyByDividingByGcfOfCoefficients
        inputExpr = "2 [x ^ 2] - 4 x + 2 = 0"

        check {
            fromExpr = "2 [x ^ 2] - 4 x + 2 = 0"
            toExpr = "[x ^ 2] - 2 x + 1 = 0"
            explanation {
                key = EquationsExplanation.SimplifyByDividingByGcfOfCoefficients
            }

            step {
                fromExpr = "2 [x ^ 2] - 4 x + 2 = 0"
                toExpr = "2 ([x ^ 2] - 2 x + 1) = 0"
                explanation {
                    key = FactorExplanation.FactorGreatestCommonIntegerFactor
                }
            }

            step {
                fromExpr = "2 ([x ^ 2] - 2 x + 1) = 0"
                toExpr = "[x ^ 2] - 2 x + 1 = 0"
                explanation {
                    key = EquationsExplanation.EliminateConstantFactorOfLhsWithZeroRhs
                }
            }
        }
    }

    @Test
    fun testSimplifyByDividingByGcfOfCoefficientsWithNoConstantTerm() = testMethodInX {
        method = EquationsPlans.SimplifyByDividingByGcfOfCoefficients
        inputExpr = "3[x^2] - 3x = 0"

        check {
            fromExpr = "3[x^2] - 3x = 0 "
            toExpr = "[x^2] - x = 0"
        }
    }

    @Test
    fun testSimplifyEquation() = testMethod {
        method = EquationsPlans.SimplifyEquation
        inputExpr = "8 + 2 sqrt[2] - 7 - 4 sqrt[2] = 0"

        check {
            toExpr = "1 - 2 sqrt[2] = 0"
        }
    }
}
