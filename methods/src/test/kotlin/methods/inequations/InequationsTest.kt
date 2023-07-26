package methods.inequations

import engine.methods.testMethodInX
import methods.equations.EquationsExplanation
import org.junit.jupiter.api.Test

class InequationsTest {

    @Test
    fun `test solving simple inequation`() = testMethodInX {
        method = InequationsPlans.SolveInequationInOneVariable
        inputExpr = "3 x - 1 != 2"

        check {
            fromExpr = "3 x - 1 != 2"
            toExpr = "SetSolution[x : /reals/ \\ {1}]"
            explanation {
                key = InequationsExplanation.SolveInequationInOneVariable
            }

            step {
                fromExpr = "3 x - 1 != 2"
                toExpr = "3 x != 3"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3 x != 3"
                toExpr = "x != 1"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x != 1"
                toExpr = "SetSolution[x : /reals/ \\ {1}]"
                explanation {
                    key = InequationsExplanation.ExtractSolutionFromInequationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test solving complex inequation`() = testMethodInX {
        method = InequationsPlans.SolveInequationInOneVariable
        inputExpr = "(2 x + 3) ([x ^ 2] + 3 x + 2) != 0"

        check {
            fromExpr = "(2 x + 3) ([x ^ 2] + 3 x + 2) != 0"
            toExpr = "SetSolution[x : /reals/ \\ {-2, -[3 / 2], -1}]"
            explanation {
                key = InequationsExplanation.SolveInequationInOneVariable
            }

            task {
                taskId = "#1"
                startExpr = "(2 x + 3) ([x ^ 2] + 3 x + 2) = 0"
                explanation {
                    key = InequationsExplanation.SolveEquationCorrespondingToInequation
                }

                step {
                    fromExpr = "(2 x + 3) ([x ^ 2] + 3 x + 2) = 0"
                    toExpr = "SetSolution[x : {-2, -[3 / 2], -1}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationByFactoring
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "SetSolution[x : /reals/ \\ {-2, -[3 / 2], -1}]"
                explanation {
                    key = InequationsExplanation.TakeComplementOfSolution
                }
            }
        }
    }
}
