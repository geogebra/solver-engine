package methods.equations

import engine.methods.testMethodInX
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class LinearEquationsWithFractionsTest {

    @Test
    fun `test multiplying through by the LCD in linear equations`() = testMethodInX {
        method = EquationsPlans.SolveEquation
        inputExpr = "[x - 14 / 12] - [2 x - 1 / 18] = [2 / 9] (2 x - 5)"

        check {
            fromExpr = "[x - 14 / 12] - [2 x - 1 / 18] = [2 / 9] (2 x - 5)"
            toExpr = "SetSolution[x: {0}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "[x - 14 / 12] - [2 x - 1 / 18] = [2 / 9] (2 x - 5)"
                toExpr = "-x - 40 = 16 x - 40"
                explanation {
                    key = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify
                }
            }

            step {
                fromExpr = "-x - 40 = 16 x - 40"
                toExpr = "-x = 16 x"
                explanation {
                    key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                }
            }

            step {
                fromExpr = "-x = 16 x"
                toExpr = "0 = 17 x"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "0 = 17 x"
                toExpr = "17 x = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.FlipEquation
                }
            }

            step {
                fromExpr = "17 x = 0"
                toExpr = "x = 0"
                explanation {
                    key = EquationsExplanation.EliminateConstantFactorOfLhsWithZeroRhs
                }
            }

            step {
                fromExpr = "x = 0"
                toExpr = "SetSolution[x: {0}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test no multiplication by the lcd when the equation has to be expanded first`() = testMethodInX {
        method = EquationsPlans.SolveEquation
        inputExpr = "180 ([x - 14 / 12] - [2 x - 1 / 10]) = 1"

        check {
            fromExpr = "180 ([x - 14 / 12] - [2 x - 1 / 10]) = 1"
            toExpr = "SetSolution[x: {-[193 / 21]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "180 ([x - 14 / 12] - [2 x - 1 / 10]) = 1"
                toExpr = "-21 x - 192 = 1"
                explanation {
                    key = PolynomialsExplanation.ExpandSingleBracketWithIntegerCoefficient
                }
            }

            step {
                fromExpr = "-21 x - 192 = 1"
                toExpr = "-21 x = 193"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "-21 x = 193"
                toExpr = "x = -[193 / 21]"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -[193 / 21]"
                toExpr = "SetSolution[x: {-[193 / 21]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }
}
