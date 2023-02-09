package methods.equations

import engine.context.Context
import engine.methods.testMethod
import methods.decimals.DecimalsExplanation
import org.junit.jupiter.api.Test

class DecimalLinearEquationsTest {

    @Test
    fun `test ax + b = cx + d decimal linear equation`() = testMethod {
        method = EquationsPlans.SolveLinearEquation
        context = Context(solutionVariable = "x", preferDecimals = true)
        inputExpr = "3.1 x + 2.2 = 2.9 x - 9.34"

        check {
            fromExpr = "3.1 x + 2.2 = 2.9 x - 9.34"
            toExpr = "Solution[x, {-57.7}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "3.1 x + 2.2 = 2.9 x - 9.34"
                toExpr = "0.2 x + 2.2 = -9.34"
                explanation {
                    key = EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "0.2 x + 2.2 = -9.34"
                toExpr = "0.2 x = -11.54"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "0.2 x = -11.54"
                toExpr = "x = -57.7"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -57.7"
                toExpr = "Solution[x, {-57.7}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test decimal linear equation with solution not expressible as terminating decimal`() = testMethod {
        method = EquationsPlans.SolveLinearEquation
        context = Context(solutionVariable = "x", preferDecimals = true)
        inputExpr = "3.1 x + 2.2 = 1.21"

        check {
            fromExpr = "3.1 x + 2.2 = 1.21"
            toExpr = "Solution[x, {-[99 / 310]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "3.1 x + 2.2 = 1.21"
                toExpr = "3.1 x = -0.99"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3.1 x = -0.99"
                toExpr = "x = -[99 / 310]"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -[99 / 310]"
                toExpr = "Solution[x, {-[99 / 310]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test decimal linear equation fails for recurring decimal in solution`() = testMethod {
        method = EquationsPlans.SolveLinearEquation
        context = Context(solutionVariable = "x", preferDecimals = true)
        inputExpr = "3.1x + 2.2[3] = 1.21"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test decimal linear equation with fractions in the initial expression`() = testMethod {
        method = EquationsPlans.SolveLinearEquation
        context = Context(solutionVariable = "x", preferDecimals = true)
        inputExpr = "3.6 x + 2.2 = [2 / 5] x + 1.2"

        check {
            fromExpr = "3.6 x + 2.2 = [2 / 5] x + 1.2"
            toExpr = "Solution[x, {-0.3125}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "3.6 x + 2.2 = [2 / 5] x + 1.2"
                toExpr = "3.6 x + 2.2 = 0.4 x + 1.2"
                explanation {
                    key = DecimalsExplanation.ConvertNiceFractionToDecimal
                }
            }

            step {
                fromExpr = "3.6 x + 2.2 = 0.4 x + 1.2"
                toExpr = "3.2 x + 2.2 = 1.2"
                explanation {
                    key = EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "3.2 x + 2.2 = 1.2"
                toExpr = "3.2 x = -1"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3.2 x = -1"
                toExpr = "x = -0.3125"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -0.3125"
                toExpr = "Solution[x, {-0.3125}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }
}
