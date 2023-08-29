package methods.equations

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
    fun `test equation reducible to quadratic equation with integerRationalExponent`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "[3^[1/3]]([x^2]-[3^[1/3]])=2x"

        check {
            toExpr = "SetSolution[x : {-[3 ^ -[1 / 3]], [3 ^ [2 / 3]]}]"
        }
    }

    @Test
    fun `test undefined equation cannot be solved`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "x = [1 / 1 - 1]"

        check {
            fromExpr = "x = [1 / 1 - 1]"
            toExpr = "/void/"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            step {
                fromExpr = "x = [1 / 1 - 1]"
                toExpr = "/undefined/"
                explanation {
                    key = EquationsExplanation.SimplifyEquation
                }
            }

            step {
                fromExpr = "/undefined/"
                toExpr = "/void/"
                explanation {
                    key = EquationsExplanation.UndefinedEquationCannotBeSolved
                }
            }
        }
    }
}
