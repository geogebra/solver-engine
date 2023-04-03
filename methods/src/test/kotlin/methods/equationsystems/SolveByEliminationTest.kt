package methods.equationsystems

import engine.methods.testMethod
import methods.equations.EquationsExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class SolveByEliminationTest {

    private fun shortTest(equations: String, solution: String) {
        testMethod {
            method = EquationSystemsPlans.SolveEquationSystemByElimination
            inputExpr = equations

            check {
                toExpr = solution
            }
        }
    }

    @Test
    fun shortTestSimpleSystem() = shortTest(
        "x + y = 1, x - y = 1",
        "SetSolution[x, y : {(1, 0)}]",
    )

    @Test
    fun shortTestMoreComplexSystem() = shortTest(
        "2x + 3y = 1, 5x - 2y = 10",
        "SetSolution[x, y : {([32/19], -[15/19])}]",
    )

    @Test
    fun shortTestTwoIdentities() = shortTest(
        "x + y = y + x, 2x = x + x",
        "Identity[x, y : 0 = 0, 0 = 0]",
    )

    @Test
    fun shortTestSimpleContradiction() = shortTest(
        "x + y = x + y + 1, y = x",
        "Contradiction[x, y: 0 = 1]",
    )

    @Test
    fun shortTestCombinedContradiction() = shortTest(
        "x + y = 2, x + y = 3",
        "Contradiction[x, y: 0 = -1]",
    )

    @Test
    fun shortTestEquivalent() = shortTest(
        "x + y = 2, y = 2 - x",
        "ImplicitSolution[x, y: x = 2 - y]",
    )

    @Test
    fun shortTestOneEquationIsIdentity() = shortTest(
        "x + y = y + 2, y + y = 2y",
        "SetSolution[x, y: {2}*REALS]",
    )

    @Test
    fun shortTestAnotherEquationIsIdentity() = shortTest(
        "x + y - x = 5, x + y = y + x",
        "SetSolution[x, y: REALS*{5}]",
    )

    @Test
    fun testIdentityAndGenuineEquationRelatingXAndY() = shortTest(
        "y = y, x + y = 5",
        "ImplicitSolution[x, y: x + y = 5]",
    )

    @Test
    fun testIndependentEquations() = shortTest(
        "2y = 10, x + 2 = 5",
        "SetSolution[x, y: {(3, 5)}]",
    )

    @Test
    fun testOneUnivariateEquation() = shortTest(
        "x + y = 10, y + 2 = 1",
        "SetSolution[x, y: {(11, -1)}]",
    )

    @Test
    fun testSameXInBoth() = shortTest(
        "x + y = y + 10, x + 2 = 12",
        "SetSolution[x, y: {10}*REALS]",
    )

    @Test
    fun testDifferentYInBoth() = shortTest(
        "x + y = x + 10, y + 2 = 10",
        "Contradiction[x, y: 10 = 8]",
    )

    @Test
    fun testTwoSolutionsBySubtraction() = testMethod {
        method = EquationSystemsPlans.SolveEquationSystemByElimination
        inputExpr = "2 x + 3 y = 5, x - y = 2"

        check {
            fromExpr = "2 x + 3 y = 5, x - y = 2"
            toExpr = "SetSolution[x, y: {([11 / 5], [1 / 5])}]"
            explanation {
                key = EquationSystemsExplanation.SolveEquationSystemByElimination
            }

            task {
                taskId = "#1"
                startExpr = "2 (x - y) = 2 * 2"
                explanation {
                    key = EquationSystemsExplanation.MultiplyEquation
                }

                step {
                    fromExpr = "2 (x - y) = 2 * 2"
                    toExpr = "2 x - 2 y = 4"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "2 x + 3 y = 5 /-/ 2 x - 2 y = 4"
                explanation {
                    key = EquationSystemsExplanation.SubtractEquations
                }

                step {
                    fromExpr = "2 x + 3 y = 5 /-/ 2 x - 2 y = 4"
                    toExpr = "2 x + 3 y - (2 x - 2 y) = 5 - 4"
                    explanation {
                        key = EquationSystemsExplanation.RewriteEquationSubtraction
                    }
                }

                step {
                    fromExpr = "2 x + 3 y - (2 x - 2 y) = 5 - 4"
                    toExpr = "5 y = 1"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "5 y = 1"
                explanation {
                    key = EquationSystemsExplanation.SolveEliminatedEquation
                }

                step {
                    fromExpr = "5 y = 1"
                    toExpr = "Solution[y, {[1 / 5]}]"
                    explanation {
                        key = EquationsExplanation.SolveLinearEquation
                    }
                }
            }

            task {
                taskId = "#4"
                startExpr = "2 x + 3 * [1 / 5] = 5"
                explanation {
                    key = EquationSystemsExplanation.SubstituteAndSolveIn
                }

                step {
                    fromExpr = "2 x + 3 * [1 / 5] = 5"
                    toExpr = "Solution[x, {[11 / 5]}]"
                    explanation {
                        key = EquationsExplanation.SolveLinearEquation
                    }
                }
            }

            task {
                taskId = "#5"
                startExpr = "SetSolution[x, y: {([11 / 5], [1 / 5])}]"
                explanation {
                    key = EquationSystemsExplanation.BuildSolutionCombineUniqueSolutions
                }
            }
        }
    }

    @Test
    fun testTwoSolutionsByAddition() = testMethod {
        method = EquationSystemsPlans.SolveEquationSystemByElimination
        inputExpr = "2 x + 3 y = 5, x - 3 y = 2"

        check {
            fromExpr = "2 x + 3 y = 5, x - 3 y = 2"
            toExpr = "SetSolution[x, y: {([7 / 3], [1 / 9])}]"
            explanation {
                key = EquationSystemsExplanation.SolveEquationSystemByElimination
            }

            task {
                taskId = "#1"
                startExpr = "2 x + 3 y = 5 /+/ x - 3 y = 2"
                explanation {
                    key = EquationSystemsExplanation.AddEquations
                }

                step {
                    fromExpr = "2 x + 3 y = 5 /+/ x - 3 y = 2"
                    toExpr = "2 x + 3 y + x - 3 y = 5 + 2"
                    explanation {
                        key = EquationSystemsExplanation.RewriteEquationAddition
                    }
                }

                step {
                    fromExpr = "2 x + 3 y + x - 3 y = 5 + 2"
                    toExpr = "3 x = 7"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "3 x = 7"
                explanation {
                    key = EquationSystemsExplanation.SolveEliminatedEquation
                }

                step {
                    fromExpr = "3 x = 7"
                    toExpr = "Solution[x, {[7 / 3]}]"
                    explanation {
                        key = EquationsExplanation.SolveLinearEquation
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "2 * [7 / 3] + 3 y = 5"
                explanation {
                    key = EquationSystemsExplanation.SubstituteAndSolveIn
                }

                step {
                    fromExpr = "2 * [7 / 3] + 3 y = 5"
                    toExpr = "Solution[y, {[1 / 9]}]"
                    explanation {
                        key = EquationsExplanation.SolveLinearEquation
                    }
                }
            }

            task {
                taskId = "#4"
                startExpr = "SetSolution[x, y: {([7 / 3], [1 / 9])}]"
                explanation {
                    key = EquationSystemsExplanation.BuildSolutionCombineUniqueSolutions
                }
            }
        }
    }

    @Test
    fun testImplicitSolution() = testMethod {
        method = EquationSystemsPlans.SolveEquationSystemByElimination
        inputExpr = "x + y = 3, 2 x + 2 y = 6"

        check {
            fromExpr = "x + y = 3, 2 x + 2 y = 6"
            toExpr = "ImplicitSolution[x, y: x = 3 - y]"
            explanation {
                key = EquationSystemsExplanation.SolveEquationSystemByElimination
            }

            task {
                taskId = "#1"
                startExpr = "2 (x + y) = 2 * 3"
                explanation {
                    key = EquationSystemsExplanation.MultiplyEquation
                }

                step {
                    fromExpr = "2 (x + y) = 2 * 3"
                    toExpr = "2 x + 2 y = 6"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "2 x + 2 y = 6 /-/ 2 x + 2 y = 6"
                explanation {
                    key = EquationSystemsExplanation.SubtractEquations
                }

                step {
                    fromExpr = "2 x + 2 y = 6 /-/ 2 x + 2 y = 6"
                    toExpr = "2 x + 2 y - (2 x + 2 y) = 6 - 6"
                    explanation {
                        key = EquationSystemsExplanation.RewriteEquationSubtraction
                    }
                }

                step {
                    fromExpr = "2 x + 2 y - (2 x + 2 y) = 6 - 6"
                    toExpr = "0 = 0"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "0 = 0"
                explanation {
                    key = EquationSystemsExplanation.SolveEliminatedEquation
                }

                step {
                    fromExpr = "0 = 0"
                    toExpr = "Identity[y: 0 = 0]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromIdentity
                    }
                }
            }

            task {
                taskId = "#4"
                startExpr = "x + y = 3"
                explanation {
                    key = EquationSystemsExplanation.ExpressInTermsOf
                }

                step {
                    fromExpr = "x + y = 3"
                    toExpr = "x = 3 - y"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRightAndSimplify
                    }
                }
            }

            task {
                taskId = "#5"
                startExpr = "ImplicitSolution[x, y: x = 3 - y]"
                explanation {
                    key = EquationSystemsExplanation.BuildSolutionIdentityAndEquation
                }
            }
        }
    }

    @Test
    fun testSameSolution() = testMethod {
        method = EquationSystemsPlans.SolveEquationSystemByElimination
        inputExpr = "x + x = 2, x + y = y + 1"

        check {
            fromExpr = "x + x = 2, x + y = y + 1"
            toExpr = "SetSolution[x, y: {1}*REALS]"
            explanation {
                key = EquationSystemsExplanation.SolveEquationSystemByElimination
            }

            task {
                taskId = "#1"
                startExpr = "x + x = 2"
                explanation {
                    key = EquationSystemsExplanation.PrepareEquation
                }

                step {
                    fromExpr = "x + x = 2"
                    toExpr = "2 x = 2"
                }

                step {
                    fromExpr = "2 x = 2"
                    toExpr = "Solution[x, {1}]"
                    explanation {
                        key = EquationsExplanation.SolveLinearEquation
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "x + y = y + 1"
                explanation {
                    key = EquationSystemsExplanation.PrepareEquation
                }

                step {
                    fromExpr = "x + y = y + 1"
                    toExpr = "x = 1"
                }

                step {
                    fromExpr = "x = 1"
                    toExpr = "Solution[x, {1}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "SetSolution[x, y: {1}*REALS]"
                explanation {
                    key = EquationSystemsExplanation.BuildSolutionSameSolutionInOneVariable
                    param { expr = "x" }
                    param { expr = "1" }
                }
            }
        }
    }

    @Test
    fun testDifferentSolutions() = testMethod {
        method = EquationSystemsPlans.SolveEquationSystemByElimination
        inputExpr = "x = 2, x + y = y + 1"

        check {
            fromExpr = "x = 2, x + y = y + 1"
            toExpr = "Contradiction[x, y: 2 = 1]"
            explanation {
                key = EquationSystemsExplanation.SolveEquationSystemByElimination
            }

            task {
                taskId = "#1"
                startExpr = "x = 2"
                explanation {
                    key = EquationSystemsExplanation.PrepareEquation
                }

                step {
                    fromExpr = "x = 2"
                    toExpr = "Solution[x, {2}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "x + y = y + 1"
                explanation {
                    key = EquationSystemsExplanation.PrepareEquation
                }

                step {
                    fromExpr = "x + y = y + 1"
                    toExpr = "x = 1"
                }

                step {
                    fromExpr = "x = 1"
                    toExpr = "Solution[x, {1}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "Contradiction[x, y: 2 = 1]"
                explanation {
                    key = EquationSystemsExplanation.BuildSolutionDifferentSolutionsInOneVariable
                    param { expr = "x" }
                    param { expr = "2" }
                    param { expr = "1" }
                }
            }
        }
    }
}
