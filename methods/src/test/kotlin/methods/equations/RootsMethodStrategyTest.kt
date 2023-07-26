package methods.equations

import engine.context.Context
import engine.context.strategyChoice
import engine.methods.MethodTestCase
import engine.methods.getPlan
import engine.methods.testMethod
import engine.methods.testMethodInX
import methods.collecting.CollectingExplanation
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.equations.EquationSolvingStrategy.RootsMethod
import org.junit.jupiter.api.Test

class RootsMethodStrategyTest {

    fun shortTest(inputExpr: String, toExpr: String?) = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        context = Context(
            solutionVariables = listOf("x"),
            preferredStrategies = mapOf(strategyChoice(RootsMethod)),
        )
        this.inputExpr = inputExpr

        check {
            if (toExpr != null) {
                this.toExpr = toExpr
                explanation {
                    key = EquationsExplanation.SolveEquationUsingRootsMethod
                }
            } else {
                noTransformation()
            }
        }
    }

    private fun testRootsMethod(init: MethodTestCase.() -> Unit) {
        val testCase = MethodTestCase()
        testCase.method = EquationsPlans.SolveEquationInOneVariable
        testCase.context = Context(
            solutionVariables = listOf("x"),
            preferredStrategies = mapOf(strategyChoice(RootsMethod)),
        )
        testCase.init()
    }

    @Test
    fun testCubeEqualsNegative() = shortTest(
        inputExpr = "[x ^ 3] + 10 = 0",
        toExpr = "SetSolution[x: {-root[10, 3]}]",
    )

    @Test
    fun testCubeEqualsPositive() = shortTest(
        inputExpr = "8 - [x ^ 3] = 0",
        toExpr = "SetSolution[x: {2}]",
    )

    @Test
    fun testPowerOf5Equals0() = shortTest(
        inputExpr = "2 [x ^ 5] + 3 = 1 + 2",
        toExpr = "SetSolution[x: {0}]",
    )

    @Test
    fun testSquareOfSquareEqualsNegative() = shortTest(
        inputExpr = "[([x ^ 2]) ^ 2] + 1 = 0",
        toExpr = "Contradiction[x: [x ^ 4] = -1]",
    )

    @Test
    fun testRootMethodsAppliesWhenBracketsCouldBeExpanded() = shortTest(
        inputExpr = "3[(x + 1)^2] = [1/3]",
        toExpr = "SetSolution[x: {-[4/3], -[2/3]}]",
    )

    @Test
    fun `test square equals negative quadratic equation`() = testRootsMethod {
        inputExpr = "2 [x ^ 2] - 3 = 3 [x ^ 2] + 4"

        check {
            fromExpr = "2 [x ^ 2] - 3 = 3 [x ^ 2] + 4"
            toExpr = "Contradiction[x: [x ^ 2] = -7]"
            explanation {
                key = EquationsExplanation.SolveEquationUsingRootsMethod
            }

            step {
                fromExpr = "2 [x ^ 2] - 3 = 3 [x ^ 2] + 4"
                toExpr = "-3 = [x ^ 2] + 4"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "-3 = [x ^ 2] + 4"
                toExpr = "-7 = [x ^ 2]"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "-7 = [x ^ 2]"
                toExpr = "[x ^ 2] = -7"
                explanation {
                    key = methods.solvable.EquationsExplanation.FlipEquation
                }
            }

            step {
                fromExpr = "[x ^ 2] = -7"
                toExpr = "Contradiction[x: [x ^ 2] = -7]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEvenPowerEqualsNegative
                }
            }
        }
    }

    @Test
    fun `test square equals zero quadratic equation`() = testRootsMethod {
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
                    key = EquationsExplanation.EliminateConstantFactorOfLhsWithZeroRhs
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
    fun `test square equals positive number quadratic equation`() = testRootsMethod {
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
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
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
                    key = EquationsExplanation.SimplifyEquation
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
    fun `test quadratic equation containing linear terms that can be eliminated`() = testRootsMethod {
        context = context.copy(solutionVariables = listOf("y"))
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
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
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
                    key = EquationsExplanation.SimplifyEquation
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
        context = context.copy(solutionVariables = listOf("y"))
        method = RootsMethod.getPlan()
        inputExpr = "2[y^2] + y = 7"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test roots method followed by roots method`() = testRootsMethod {
        inputExpr = "[([x ^ 2] + 1) ^ 2] = 4"

        check {
            fromExpr = "[([x ^ 2] + 1) ^ 2] = 4"
            toExpr = "SetSolution[x : {-1, 1}]"
            explanation {
                key = EquationsExplanation.SolveEquationUsingRootsMethod
            }

            step {
                fromExpr = "[([x ^ 2] + 1) ^ 2] = 4"
                toExpr = "[x ^ 2] + 1 = +/-sqrt[4]"
                explanation {
                    key = EquationsExplanation.TakeRootOfBothSides
                }
            }

            step {
                fromExpr = "[x ^ 2] + 1 = +/-sqrt[4]"
                toExpr = "[x ^ 2] + 1 = +/-2"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyRootsInExpression
                }
            }

            step {
                fromExpr = "[x ^ 2] + 1 = +/-2"
                toExpr = "[x ^ 2] = +/-2 - 1"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "[x ^ 2] = +/-2 - 1"
                toExpr = "[x ^ 2] = -2 - 1 OR [x ^ 2] = 2 - 1"
                explanation {
                    key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                }
            }

            step {
                fromExpr = "[x ^ 2] = -2 - 1 OR [x ^ 2] = 2 - 1"
                toExpr = "SetSolution[x : {-1, 1}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] = -2 - 1"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "[x ^ 2] = -2 - 1"
                        toExpr = "Contradiction[x : [x ^ 2] = -3]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUsingRootsMethod
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[x ^ 2] = 2 - 1"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "[x ^ 2] = 2 - 1"
                        toExpr = "SetSolution[x : {-1, 1}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUsingRootsMethod
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : {-1, 1}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test roots method followed by quadratic formula`() = testRootsMethod {
        inputExpr = "[([x ^ 2] + 3 x + 1) ^ 3] = 3"

        check {
            fromExpr = "[([x ^ 2] + 3 x + 1) ^ 3] = 3"
            toExpr = "SetSolution[x : {-[3 + sqrt[5 + 4 root[3, 3]] / 2], [-3 + sqrt[5 + 4 root[3, 3]] / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquationUsingRootsMethod
            }

            step {
                fromExpr = "[([x ^ 2] + 3 x + 1) ^ 3] = 3"
                toExpr = "[x ^ 2] + 3 x + 1 = root[3, 3]"
                explanation {
                    key = EquationsExplanation.TakeRootOfBothSides
                }
            }

            step {
                fromExpr = "[x ^ 2] + 3 x + 1 = root[3, 3]"
                toExpr = "[x ^ 2] + 3 x + 1 - root[3, 3] = 0"
                explanation {
                    key = EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "[x ^ 2] + 3 x + 1 - root[3, 3] = 0"
                toExpr = "x = [-3 +/- sqrt[[3 ^ 2] - 4 * 1 (1 - root[3, 3])] / 2 * 1]"
                explanation {
                    key = EquationsExplanation.ApplyQuadraticFormula
                }
            }

            step {
                fromExpr = "x = [-3 +/- sqrt[[3 ^ 2] - 4 * 1 (1 - root[3, 3])] / 2 * 1]"
                toExpr = "x = [-3 +/- sqrt[5 + 4 root[3, 3]] / 2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
            }

            step {
                fromExpr = "x = [-3 +/- sqrt[5 + 4 root[3, 3]] / 2]"
                toExpr = "x = [-3 - sqrt[5 + 4 root[3, 3]] / 2] OR x = [-3 + sqrt[5 + 4 root[3, 3]] / 2]"
                explanation {
                    key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                }
            }

            step {
                fromExpr = "x = [-3 - sqrt[5 + 4 root[3, 3]] / 2] OR x = [-3 + sqrt[5 + 4 root[3, 3]] / 2]"
                toExpr = "SetSolution[x : {-[3 + sqrt[5 + 4 root[3, 3]] / 2], [-3 + sqrt[5 + 4 root[3, 3]] / 2]}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
            }
        }
    }
}
