package methods.equations

import engine.context.Context
import engine.methods.testMethod
import engine.methods.testMethodInX
import methods.collecting.CollectingExplanation
import methods.constantexpressions.ConstantExpressionsExplanation
import org.junit.jupiter.api.Test

class QuadraticEquationsWithRootsMethodTest {

    @Test
    fun `test square equals negative quadratic equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationUsingRootsMethod
        inputExpr = "2 [x ^ 2] - 3 = 3 [x ^ 2] + 4"

        check {
            fromExpr = "2 [x ^ 2] - 3 = 3 [x ^ 2] + 4"
            toExpr = "SetSolution[x: {}]"
            explanation {
                key = EquationsExplanation.SolveEquationUsingRootsMethod
            }

            step {
                fromExpr = "2 [x ^ 2] - 3 = 3 [x ^ 2] + 4"
                toExpr = "-[x ^ 2] - 3 = 4"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "-[x ^ 2] - 3 = 4"
                toExpr = "-[x ^ 2] = 7"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "-[x ^ 2] = 7"
                toExpr = "[x ^ 2] = -7"
                explanation {
                    key = EquationsExplanation.NegateBothSides
                }
            }

            step {
                fromExpr = "[x ^ 2] = -7"
                toExpr = "SetSolution[x: {}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEvenPowerEqualsNegative
                }
            }
        }
    }

    @Test
    fun `test square equals zero quadratic equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationUsingRootsMethod
        inputExpr = "[5 / 2] [x ^ 2] + 5 = [x ^ 2] + 5"

        check {
            fromExpr = "[5 / 2] [x ^ 2] + 5 = [x ^ 2] + 5"
            toExpr = "SetSolution[x: {0}]"
            explanation {
                key = EquationsExplanation.SolveEquationUsingRootsMethod
            }

            step {
                fromExpr = "[5 / 2] [x ^ 2] + 5 = [x ^ 2] + 5"
                toExpr = "[5 / 2] [x ^ 2] = [x ^ 2]"
                explanation {
                    key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                }
            }

            step {
                fromExpr = "[5 / 2] [x ^ 2] = [x ^ 2]"
                toExpr = "[3 / 2] [x ^ 2] = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "[3 / 2] [x ^ 2] = 0"
                toExpr = "[x ^ 2] = 0"
                explanation {
                    key = EquationsExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "[x ^ 2] = 0"
                toExpr = "x = 0"
                explanation {
                    key = EquationsExplanation.TakeRootOfBothSidesRHSIsZero
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
    fun `test square equals positive number quadratic equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationUsingRootsMethod
        inputExpr = "4 [x ^ 2] + 5 = 2 [x ^ 2] + 8"

        check {
            fromExpr = "4 [x ^ 2] + 5 = 2 [x ^ 2] + 8"
            toExpr = "SetSolution[x: {-[sqrt[6] / 2], [sqrt[6] / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquationUsingRootsMethod
            }

            step {
                fromExpr = "4 [x ^ 2] + 5 = 2 [x ^ 2] + 8"
                toExpr = "2 [x ^ 2] + 5 = 8"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "2 [x ^ 2] + 5 = 8"
                toExpr = "2 [x ^ 2] = 3"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "2 [x ^ 2] = 3"
                toExpr = "[x ^ 2] = [3 / 2]"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "[x ^ 2] = [3 / 2]"
                toExpr = "x = +/-sqrt[[3 / 2]]"
                explanation {
                    key = EquationsExplanation.TakeRootOfBothSides
                }
            }

            step {
                fromExpr = "x = +/-sqrt[[3 / 2]]"
                toExpr = "x = +/-[sqrt[6] / 2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
            }

            step {
                fromExpr = "x = +/-[sqrt[6] / 2]"
                toExpr = "SetSolution[x: {-[sqrt[6] / 2], [sqrt[6] / 2]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                }
            }
        }
    }

    @Test
    fun `test quadratic equation containing linear terms that can be eliminated`() = testMethod {
        context = Context(solutionVariables = listOf("y"))
        method = EquationsPlans.SolveEquationUsingRootsMethod
        inputExpr = "2[y^2] + 2y - 3 = y + y + 4"

        check {
            fromExpr = "2 [y ^ 2] + 2 y - 3 = y + y + 4"
            toExpr = "SetSolution[y: {-[sqrt[14] / 2], [sqrt[14] / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquationUsingRootsMethod
            }

            step {
                fromExpr = "2 [y ^ 2] + 2 y - 3 = y + y + 4"
                toExpr = "2 [y ^ 2] + 2 y - 3 = 2 y + 4"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }
            }

            step {
                fromExpr = "2 [y ^ 2] + 2 y - 3 = 2 y + 4"
                toExpr = "2 [y ^ 2] - 3 = 4"
                explanation {
                    key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                }
            }

            step {
                fromExpr = "2 [y ^ 2] - 3 = 4"
                toExpr = "2 [y ^ 2] = 7"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "2 [y ^ 2] = 7"
                toExpr = "[y ^ 2] = [7 / 2]"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "[y ^ 2] = [7 / 2]"
                toExpr = "y = +/-sqrt[[7 / 2]]"
                explanation {
                    key = EquationsExplanation.TakeRootOfBothSides
                }
            }

            step {
                fromExpr = "y = +/-sqrt[[7 / 2]]"
                toExpr = "y = +/-[sqrt[14] / 2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
            }

            step {
                fromExpr = "y = +/-[sqrt[14] / 2]"
                toExpr = "SetSolution[y: {-[sqrt[14] / 2], [sqrt[14] / 2]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                }
            }
        }
    }

    @Test
    fun `test quadratic equation containing linear terms that cannot be eliminated fails`() = testMethod {
        context = Context(solutionVariables = listOf("y"))
        method = EquationsPlans.SolveEquationUsingRootsMethod
        inputExpr = "2[y^2] + 2y - 3 = y + 4"

        check {
            noTransformation()
        }
    }
}
