package methods.equations

import engine.methods.testMethodInX
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class DecimalLinearEquationsTest {

    @Test
    fun `test ax + b = cx + d decimal linear equation`() = testMethodInX {
        method = EquationsPlans.SolveDecimalLinearEquation
        inputExpr = "3.1 x + 2.2 = 2.9 x - 9.34"

        check {
            fromExpr = "3.1 x + 2.2 = 2.9 x - 9.34"
            toExpr = "SetSolution[x: {-57.7}]"
            explanation {
                key = EquationsExplanation.SolveDecimalLinearEquation
            }

            step {
                fromExpr = "3.1 x + 2.2 = 2.9 x - 9.34"
                toExpr = "0.2 x + 2.2 = -9.34"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "0.2 x + 2.2 = -9.34"
                toExpr = "0.2 x = -11.54"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "0.2 x = -11.54"
                toExpr = "x = -57.7"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -57.7"
                toExpr = "SetSolution[x: {-57.7}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test decimal linear equation with fractions in the initial expression`() = testMethodInX {
        method = EquationsPlans.SolveDecimalLinearEquation
        inputExpr = "3.6 x + 2.2 = [2 / 5] x + 1.2"

        check {
            fromExpr = "3.6 x + 2.2 = [2 / 5] x + 1.2"
            toExpr = "SetSolution[x: {-0.3125}]"
            explanation {
                key = EquationsExplanation.SolveDecimalLinearEquation
            }

            step {
                fromExpr = "3.6 x + 2.2 = [2 / 5] x + 1.2"
                toExpr = "3.6 x + 2.2 = 0.4 x + 1.2"
                explanation {
                    key = PolynomialsExplanation.SimplifyMonomial
                }
            }

            step {
                fromExpr = "3.6 x + 2.2 = 0.4 x + 1.2"
                toExpr = "3.2 x + 2.2 = 1.2"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "3.2 x + 2.2 = 1.2"
                toExpr = "3.2 x = -1"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3.2 x = -1"
                toExpr = "x = -0.3125"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -0.3125"
                toExpr = "SetSolution[x: {-0.3125}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }
}
