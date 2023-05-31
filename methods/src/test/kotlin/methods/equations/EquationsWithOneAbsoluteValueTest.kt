package methods.equations

import engine.methods.testMethodInX
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength")
class EquationsWithOneAbsoluteValueTest {
    @Test
    fun `test simple linear equation in modulus`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithVariablesInOneAbsoluteValue
        inputExpr = "abs[2 x - 1] = 3"

        check {
            fromExpr = "abs[2 x - 1] = 3"
            toExpr = "SetSolution[x : {-1, 2}]"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }

            step {
                fromExpr = "abs[2 x - 1] = 3"
                toExpr = "2 x - 1 = 3 OR 2 x - 1 = -3"
                explanation {
                    key = EquationsExplanation.SeparateModulusEqualsPositiveConstant
                }
            }

            step {
                fromExpr = "2 x - 1 = 3 OR 2 x - 1 = -3"
                toExpr = "SetSolution[x : {-1, 2}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }

                task {
                    taskId = "#1"
                    startExpr = "2 x - 1 = 3"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "2 x - 1 = 3"
                        toExpr = "SetSolution[x : {2}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "2 x - 1 = -3"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "2 x - 1 = -3"
                        toExpr = "SetSolution[x : {-1}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : {-1, 2}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test simple rearrangement needed`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithVariablesInOneAbsoluteValue
        inputExpr = "3 * abs[1 - x] + 2 = 10"

        check {
            fromExpr = "3 * abs[1 - x] + 2 = 10"
            toExpr = "SetSolution[x : {-[5 / 3], [11 / 3]}]"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }

            step {
                fromExpr = "3 * abs[1 - x] + 2 = 10"
                toExpr = "3 * abs[1 - x] = 8"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3 * abs[1 - x] = 8"
                toExpr = "3 (1 - x) = 8 OR 3 (1 - x) = -8"
                explanation {
                    key = EquationsExplanation.SeparateModulusEqualsPositiveConstant
                }
            }

            step {
                fromExpr = "3 (1 - x) = 8 OR 3 (1 - x) = -8"
                toExpr = "SetSolution[x : {-[5 / 3], [11 / 3]}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }

                task {
                    taskId = "#1"
                    startExpr = "3 (1 - x) = 8"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "3 (1 - x) = 8"
                        toExpr = "SetSolution[x : {-[5 / 3]}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "3 (1 - x) = -8"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "3 (1 - x) = -8"
                        toExpr = "SetSolution[x : {[11 / 3]}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : {-[5 / 3], [11 / 3]}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test modulus in fraction and negative`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithVariablesInOneAbsoluteValue
        inputExpr = "10 - [abs[1 - x] / 5] = 2"

        check {
            fromExpr = "10 - [abs[1 - x] / 5] = 2"
            toExpr = "SetSolution[x : {-39, 41}]"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }

            step {
                fromExpr = "10 - [abs[1 - x] / 5] = 2"
                toExpr = "-[abs[1 - x] / 5] = -8"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "-[abs[1 - x] / 5] = -8"
                toExpr = "[abs[1 - x] / 5] = 8"
                explanation {
                    key = EquationsExplanation.NegateBothSides
                }
            }

            step {
                fromExpr = "[abs[1 - x] / 5] = 8"
                toExpr = "[1 / 5] (1 - x) = 8 OR [1 / 5] (1 - x) = -8"
                explanation {
                    key = EquationsExplanation.SeparateModulusEqualsPositiveConstant
                }
            }

            step {
                fromExpr = "[1 / 5] (1 - x) = 8 OR [1 / 5] (1 - x) = -8"
                toExpr = "SetSolution[x : {-39, 41}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
            }
        }
    }

    @Test
    fun `test modulus equals negative`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithVariablesInOneAbsoluteValue
        inputExpr = "3 * abs[1 - x] + 5 = 2"

        check {
            fromExpr = "3 * abs[1 - x] + 5 = 2"
            toExpr = "Contradiction[x: 3 * abs[1 - x] = -3]"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }

            step {
                fromExpr = "3 * abs[1 - x] + 5 = 2"
                toExpr = "3 * abs[1 - x] = -3"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "3 * abs[1 - x] = -3"
                toExpr = "Contradiction[x: 3 * abs[1 - x] = -3]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromModulusEqualsNegativeConstant
                }
            }
        }
    }

    @Test
    fun `test modulus equals 0`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithVariablesInOneAbsoluteValue
        inputExpr = "abs[[x ^ 2] - 3] + 2 = 2"

        check {
            fromExpr = "abs[[x ^ 2] - 3] + 2 = 2"
            toExpr = "SetSolution[x : {-sqrt[3], sqrt[3]}]"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }

            step {
                fromExpr = "abs[[x ^ 2] - 3] + 2 = 2"
                toExpr = "abs[[x ^ 2] - 3] = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                }
            }

            step {
                fromExpr = "abs[[x ^ 2] - 3] = 0"
                toExpr = "[x ^ 2] - 3 = 0"
                explanation {
                    key = EquationsExplanation.ResolveModulusEqualsZero
                }
            }

            step {
                fromExpr = "[x ^ 2] - 3 = 0"
                toExpr = "SetSolution[x : {-sqrt[3], sqrt[3]}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUsingRootsMethod
                }
            }
        }
    }

    @Test
    fun `test linear modulus with x on other side, two solutions`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithOneAbsoluteValue
        inputExpr = "abs[2 x - 1] = x + 5"
        check {
            fromExpr = "abs[2 x - 1] = x + 5"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }
            toExpr = "SetSolution[x : {-[4 / 3], 6}]"
            step {
                fromExpr = "abs[2 x - 1] = x + 5"
                toExpr = "2 x - 1 = x + 5 GIVEN 2 x - 1 >= 0 OR -(2 x - 1) = x + 5 GIVEN 2 x - 1 < 0"
            }
            step {
                fromExpr = "2 x - 1 = x + 5 GIVEN 2 x - 1 >= 0 OR -(2 x - 1) = x + 5 GIVEN 2 x - 1 < 0"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
                toExpr = "SetSolution[x : {-[4 / 3], 6}]"

                task {
                    taskId = "#1"
                    startExpr = "2 x - 1 = x + 5 GIVEN 2 x - 1 >= 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "2 x - 1 = x + 5 GIVEN 2 x - 1 >= 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "SetSolution[x : {6}]"
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "-(2 x - 1) = x + 5 GIVEN 2 x - 1 < 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "-(2 x - 1) = x + 5 GIVEN 2 x - 1 < 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "SetSolution[x : {-[4 / 3]}]"
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : {-[4 / 3], 6}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test linear modulus with x on other side, one solution`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithOneAbsoluteValue
        inputExpr = "abs[x + 1] = 2 x + 1"
        check {
            fromExpr = "abs[x + 1] = 2 x + 1"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }
            toExpr = "SetSolution[x : {0}]"
            step {
                fromExpr = "abs[x + 1] = 2 x + 1"
                toExpr = "x + 1 = 2 x + 1 GIVEN x + 1 >= 0 OR -(x + 1) = 2 x + 1 GIVEN x + 1 < 0"
            }
            step {
                fromExpr = "x + 1 = 2 x + 1 GIVEN x + 1 >= 0 OR -(x + 1) = 2 x + 1 GIVEN x + 1 < 0"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
                toExpr = "SetSolution[x : {0}]"

                task {
                    taskId = "#1"
                    startExpr = "x + 1 = 2 x + 1 GIVEN x + 1 >= 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "x + 1 = 2 x + 1 GIVEN x + 1 >= 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "SetSolution[x : {0}]"
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "-(x + 1) = 2 x + 1 GIVEN x + 1 < 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "-(x + 1) = 2 x + 1 GIVEN x + 1 < 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "Contradiction[x : SetSolution[x : {-[2 / 3]}] GIVEN SetSolution[x : ( -/infinity/, -1 )]]"
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : {0}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test linear modulus with x on other side, no solution`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithOneAbsoluteValue
        inputExpr = "abs[4 x + 3] = x - 2"
        check {
            fromExpr = "abs[4 x + 3] = x - 2"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }
            toExpr = "SetSolution[x : {}]"
            step {
                fromExpr = "abs[4 x + 3] = x - 2"
                toExpr = "4 x + 3 = x - 2 GIVEN 4 x + 3 >= 0 OR -(4 x + 3) = x - 2 GIVEN 4 x + 3 < 0"
            }
            step {
                fromExpr = "4 x + 3 = x - 2 GIVEN 4 x + 3 >= 0 OR -(4 x + 3) = x - 2 GIVEN 4 x + 3 < 0"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
                toExpr = "SetSolution[x : {}]"

                task {
                    taskId = "#1"
                    startExpr = "4 x + 3 = x - 2 GIVEN 4 x + 3 >= 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "4 x + 3 = x - 2 GIVEN 4 x + 3 >= 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "Contradiction[x : SetSolution[x : {-[5 / 3]}] GIVEN SetSolution[x : [ -[3 / 4], /infinity/ )]]"
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "-(4 x + 3) = x - 2 GIVEN 4 x + 3 < 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "-(4 x + 3) = x - 2 GIVEN 4 x + 3 < 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "Contradiction[x : SetSolution[x : {-[1 / 5]}] GIVEN SetSolution[x : ( -/infinity/, -[3 / 4] )]]"
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : {}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `linear modulus with quadratic on other side`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithOneAbsoluteValue
        inputExpr = "2 * abs[x] - [x ^ 2] = 1"
        check {
            fromExpr = "2 * abs[x] - [x ^ 2] = 1"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }
            toExpr = "SetSolution[x : {-1, 1}]"
            step {
                fromExpr = "2 * abs[x] - [x ^ 2] = 1"
                explanation {
                    key = EquationsExplanation.IsolateAbsoluteValue
                }
                toExpr = "2 * abs[x] = 1 + [x ^ 2]"
                step {
                    fromExpr = "2 * abs[x] - [x ^ 2] = 1"
                    explanation {
                        key = EquationsExplanation.MoveTermsNotContainingModulusToTheRight
                    }
                    toExpr = "2 * abs[x] - [x ^ 2] + [x ^ 2] = 1 + [x ^ 2]"
                }
                step {
                    fromExpr = "2 * abs[x] - [x ^ 2] + [x ^ 2] = 1 + [x ^ 2]"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                    toExpr = "2 * abs[x] = 1 + [x ^ 2]"
                }
            }
            step {
                fromExpr = "2 * abs[x] = 1 + [x ^ 2]"
                toExpr = "2 x = 1 + [x ^ 2] GIVEN x >= 0 OR -2 x = 1 + [x ^ 2] GIVEN x < 0"
            }
            step {
                fromExpr = "2 x = 1 + [x ^ 2] GIVEN x >= 0 OR -2 x = 1 + [x ^ 2] GIVEN x < 0"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
                toExpr = "SetSolution[x : {-1, 1}]"

                task {
                    taskId = "#1"
                    startExpr = "2 x = 1 + [x ^ 2] GIVEN x >= 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "2 x = 1 + [x ^ 2] GIVEN x >= 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "SetSolution[x : {1}]"
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "-2 x = 1 + [x ^ 2] GIVEN x < 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "-2 x = 1 + [x ^ 2] GIVEN x < 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "SetSolution[x : {-1}]"
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
    fun `quadratic modulus with x on other side`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithOneAbsoluteValue
        inputExpr = "abs[[x ^ 2] - 1] = x"
        check {
            fromExpr = "abs[[x ^ 2] - 1] = x"
            explanation {
                key = EquationsExplanation.SolveEquationWithVariablesInOneAbsoluteValue
            }
            toExpr = "SetSolution[x : {[-1 + sqrt[5] / 2], [1 + sqrt[5] / 2]}]"
            step {
                fromExpr = "abs[[x ^ 2] - 1] = x"
                toExpr = "[x ^ 2] - 1 = x GIVEN [x ^ 2] - 1 >= 0 OR -([x ^ 2] - 1) = x GIVEN [x ^ 2] - 1 < 0"
            }
            step {
                fromExpr = "[x ^ 2] - 1 = x GIVEN [x ^ 2] - 1 >= 0 OR -([x ^ 2] - 1) = x GIVEN [x ^ 2] - 1 < 0"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
                toExpr = "SetSolution[x : {[-1 + sqrt[5] / 2], [1 + sqrt[5] / 2]}]"

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - 1 = x GIVEN [x ^ 2] - 1 >= 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "[x ^ 2] - 1 = x GIVEN [x ^ 2] - 1 >= 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "SetSolution[x : {[1 + sqrt[5] / 2]}]"
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "-([x ^ 2] - 1) = x GIVEN [x ^ 2] - 1 < 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "-([x ^ 2] - 1) = x GIVEN [x ^ 2] - 1 < 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInOneVariable
                        }
                        toExpr = "SetSolution[x : {[-1 + sqrt[5] / 2]}]"
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : {[-1 + sqrt[5] / 2], [1 + sqrt[5] / 2]}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }
}
