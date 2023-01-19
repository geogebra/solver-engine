package methods.inequalities

import engine.context.Context
import engine.methods.testMethod
import methods.decimals.DecimalsExplanation
import org.junit.jupiter.api.Test

class DecimalLinearInequalitiesTest {

    @Test
    fun `test ax + b less than cx + d decimal linear inequality`() = testMethod {
        method = InequalitiesPlans.SolveLinearInequality
        context = Context(solutionVariable = "x", preferDecimals = true)
        inputExpr = "3.1 x + 2.2 < 2.9 x - 9.34"

        check {
            fromExpr = "3.1 x + 2.2 < 2.9 x - 9.34"
            toExpr = "Solution[x, ( -INFINITY, -57.7 )]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "3.1 x + 2.2 < 2.9 x - 9.34"
                toExpr = "0.2 x + 2.2 < -9.34"
                explanation {
                    key = InequalitiesExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "0.2 x + 2.2 < -9.34"
                toExpr = "0.2 x < -11.54"
                explanation {
                    key = InequalitiesExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "0.2 x < -11.54"
                toExpr = "x < -57.7"
                explanation {
                    key = InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x < -57.7"
                toExpr = "Solution[x, ( -INFINITY, -57.7 )]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test decimal linear inequality with solution not expressible as terminating decimal`() = testMethod {
        method = InequalitiesPlans.SolveLinearInequality
        context = Context(solutionVariable = "x", preferDecimals = true)
        inputExpr = "3.1 x + 2.2 >= 1.21"

        check {
            fromExpr = "3.1 x + 2.2 >= 1.21"
            toExpr = "Solution[x, [ -[99 / 310], INFINITY )]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "3.1 x + 2.2 >= 1.21"
                toExpr = "3.1 x >= -0.99"
                explanation {
                    key = InequalitiesExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3.1 x >= -0.99"
                toExpr = "x >= -[99 / 310]"
                explanation {
                    key = InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x >= -[99 / 310]"
                toExpr = "Solution[x, [ -[99 / 310], INFINITY )]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test decimal linear inequality fails for recurring decimal in solution`() = testMethod {
        method = InequalitiesPlans.SolveLinearInequality
        context = Context(solutionVariable = "x", preferDecimals = true)
        inputExpr = "3.1x + 2.2[3] > 1.21"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test decimal linear inequality with fractions in the initial expression`() = testMethod {
        method = InequalitiesPlans.SolveLinearInequality
        context = Context(solutionVariable = "x", preferDecimals = true)
        inputExpr = "3.6 x + 2.2 <= [2 / 5] x + 1.2"

        check {
            fromExpr = "3.6 x + 2.2 <= [2 / 5] x + 1.2"
            toExpr = "Solution[x, ( -INFINITY, -0.3125 ]]"
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
                    key = InequalitiesExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "3.2 x + 2.2 <= 1.2"
                toExpr = "3.2 x <= -1.0"
                explanation {
                    key = InequalitiesExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3.2 x <= -1.0"
                toExpr = "x <= -0.3125"
                explanation {
                    key = InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x <= -0.3125"
                toExpr = "Solution[x, ( -INFINITY, -0.3125 ]]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }
}
