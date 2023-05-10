package methods.equations

import engine.methods.testMethodInX
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class LinearEquationsWithFractionsTest {

    @Test
    fun `test multiplying through by the LCD in linear equations`() = testMethodInX {
        method = EquationsPlans.SolveLinearEquation
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
                    key = EquationsExplanation.FlipEquation
                }
            }

            step {
                fromExpr = "17 x = 0"
                toExpr = "x = 0"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
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
    fun `test consistently multiply equation by the LCD 1`() = testMethodInX {
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "[x / 2] + [11 / 3] = 0"

        check {
            fromExpr = "[x / 2] + [11 / 3] = 0"
            toExpr = "SetSolution[x: {-[22 / 3]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "[x / 2] + [11 / 3] = 0"
                toExpr = "3 x + 22 = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify
                }
            }

            step {
                fromExpr = "3 x + 22 = 0"
                toExpr = "3 x = -22"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3 x = -22"
                toExpr = "x = -[22 / 3]"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -[22 / 3]"
                toExpr = "SetSolution[x: {-[22 / 3]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test consistently multiply equation by the LCD 2`() = testMethodInX {
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "[1 / 2]x + [11 / 3] = 0"

        check {
            fromExpr = "[1 / 2]x + [11 / 3] = 0"
            toExpr = "SetSolution[x: {-[22 / 3]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "[1 / 2]x + [11 / 3] = 0"
                toExpr = "3 x + 22 = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify
                }
            }

            step {
                fromExpr = "3 x + 22 = 0"
                toExpr = "3 x = -22"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3 x = -22"
                toExpr = "x = -[22 / 3]"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -[22 / 3]"
                toExpr = "SetSolution[x: {-[22 / 3]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test no multiplication by the lcd when the equation has to be expanded first`() = testMethodInX {
        method = EquationsPlans.SolveLinearEquation
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
                    key = PolynomialsExplanation.ExpandPolynomialExpression
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
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
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
