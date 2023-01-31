package methods.equations

import engine.context.Context
import engine.methods.testMethod
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class QuadraticEquationsWithRootsMethodTest {

    @Test
    fun `test square equals negative quadratic equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveQuadraticEquationUsingRootsMethod
        inputExpr = "2 [x ^ 2] - 3 = 3 [x ^ 2] + 4"

        check {
            fromExpr = "2 [x ^ 2] - 3 = 3 [x ^ 2] + 4"
            toExpr = "Solution[x, {}]"
            explanation {
                key = EquationsExplanation.SolveQuadraticEquationUsingRootsMethod
            }

            step {
                fromExpr = "2 [x ^ 2] - 3 = 3 [x ^ 2] + 4"
                toExpr = "-[x ^ 2] - 3 = 4"
                explanation {
                    key = EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "-[x ^ 2] - 3 = 4"
                toExpr = "-[x ^ 2] = 7"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
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
                toExpr = "Solution[x, {}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromSquareEqualsNegative
                }
            }
        }
    }

    @Test
    fun `test square equals zero quadratic equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveQuadraticEquationUsingRootsMethod
        inputExpr = "[5 / 2] [x ^ 2] + 5 = [x ^ 2] + 5"

        check {
            fromExpr = "[5 / 2] [x ^ 2] + 5 = [x ^ 2] + 5"
            toExpr = "Solution[x, {0}]"
            explanation {
                key = EquationsExplanation.SolveQuadraticEquationUsingRootsMethod
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
                    key = EquationsExplanation.MoveVariablesToTheLeftAndSimplify
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
                    key = EquationsExplanation.TakeSquareRootOfBothSidesRHSIsZero
                }
            }

            step {
                fromExpr = "x = 0"
                toExpr = "Solution[x, {0}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test square equals positive number quadratic equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveQuadraticEquationUsingRootsMethod
        inputExpr = "4 [x ^ 2] + 5 = 2 [x ^ 2] + 8"

        check {
            fromExpr = "4 [x ^ 2] + 5 = 2 [x ^ 2] + 8"
            toExpr = "Solution[x, {-[sqrt[6] / 2], [sqrt[6] / 2]}]"
            explanation {
                key = EquationsExplanation.SolveQuadraticEquationUsingRootsMethod
            }

            step {
                fromExpr = "4 [x ^ 2] + 5 = 2 [x ^ 2] + 8"
                toExpr = "2 [x ^ 2] + 5 = 8"
                explanation {
                    key = EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "2 [x ^ 2] + 5 = 8"
                toExpr = "2 [x ^ 2] = 3"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
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
                    key = EquationsExplanation.TakeSquareRootOfBothSides
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
                toExpr = "Solution[x, {-[sqrt[6] / 2], [sqrt[6] / 2]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                }
            }
        }
    }

    @Test
    fun `test quadratic equation containing linear terms that can be eliminated`() = testMethod {
        context = Context(solutionVariable = "y")
        method = EquationsPlans.SolveQuadraticEquationUsingRootsMethod
        inputExpr = "2[y^2] + 2y - 3 = y + y + 4"

        check {
            fromExpr = "2 [y ^ 2] + 2 y - 3 = y + y + 4"
            toExpr = "Solution[y, {-[sqrt[14] / 2], [sqrt[14] / 2]}]"
            explanation {
                key = EquationsExplanation.SolveQuadraticEquationUsingRootsMethod
            }

            step {
                fromExpr = "2 [y ^ 2] + 2 y - 3 = y + y + 4"
                toExpr = "2 [y ^ 2] + 2 y - 3 = 2 y + 4"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTermsAndSimplify
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
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
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
                    key = EquationsExplanation.TakeSquareRootOfBothSides
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
                toExpr = "Solution[y, {-[sqrt[14] / 2], [sqrt[14] / 2]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                }
            }
        }
    }

    @Test
    fun `test quadratic equation containing linear terms that cannot be eliminated fails`() = testMethod {
        context = Context(solutionVariable = "y")
        method = EquationsPlans.SolveQuadraticEquationUsingRootsMethod
        inputExpr = "2[y^2] + 2y - 3 = y + 4"

        check {
            noTransformation()
        }
    }
}
