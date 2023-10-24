package methods.inequalities

import engine.methods.testMethodInX
import methods.decimals.DecimalsExplanation
import org.junit.jupiter.api.Test

class DecimalLinearInequalitiesTest {

    @Test
    fun `test ax + b less than cx + d decimal linear inequality`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        context = context.copy(preferDecimals = true)
        inputExpr = "3.1 x + 2.2 < 2.9 x - 9.34"

        check {
            fromExpr = "3.1 x + 2.2 < 2.9 x - 9.34"
            toExpr = "SetSolution[x: ( -/infinity/, -57.7 )]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "3.1 x + 2.2 < 2.9 x - 9.34"
                toExpr = "0.2 x + 2.2 < -9.34"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "0.2 x + 2.2 < -9.34"
                toExpr = "0.2 x < -11.54"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "0.2 x < -11.54"
                toExpr = "x < -57.7"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x < -57.7"
                toExpr = "SetSolution[x: ( -/infinity/, -57.7 )]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test decimal linear inequality with solution not expressible as terminating decimal`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        context = context.copy(preferDecimals = true)
        inputExpr = "3.1 x + 2.2 >= 1.21"

        check {
            fromExpr = "3.1 x + 2.2 >= 1.21"
            toExpr = "SetSolution[x: [ -[99 / 310], /infinity/ )]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "3.1 x + 2.2 >= 1.21"
                toExpr = "3.1 x >= -0.99"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3.1 x >= -0.99"
                toExpr = "x >= -[99 / 310]"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x >= -[99 / 310]"
                toExpr = "SetSolution[x: [ -[99 / 310], /infinity/ )]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test decimal linear inequality fails for recurring decimal in solution`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        context = context.copy(preferDecimals = true)
        inputExpr = "3.1x + 2.2[3] > 1.21"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test decimal linear inequality with fractions in the initial expression`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        context = context.copy(preferDecimals = true)
        inputExpr = "3.6 x + 2.2 <= [2 / 5] x + 1.2"

        check {
            fromExpr = "3.6 x + 2.2 <= [2 / 5] x + 1.2"
            toExpr = "SetSolution[x: ( -/infinity/, -0.3125 ]]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "3.6 x + 2.2 <= [2 / 5] x + 1.2"
                toExpr = "3.6 x + 2.2 <= 0.4 x + 1.2"
                explanation {
                    key = DecimalsExplanation.ConvertNiceFractionToDecimal
                }
            }

            step {
                fromExpr = "3.6 x + 2.2 <= 0.4 x + 1.2"
                toExpr = "3.2 x + 2.2 <= 1.2"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "3.2 x + 2.2 <= 1.2"
                toExpr = "3.2 x <= -1"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3.2 x <= -1"
                toExpr = "x <= -0.3125"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x <= -0.3125"
                toExpr = "SetSolution[x: ( -/infinity/, -0.3125 ]]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }
}
