package methods.equations

import engine.methods.testMethodInX
import org.junit.jupiter.api.Test

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
}
