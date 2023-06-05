package methods.equations

import engine.methods.testMethodInX
import methods.factor.FactorExplanation
import org.junit.jupiter.api.Test

class EquationsByFactoringTest {

    @Test
    fun `test solving irreducible polynomial by factoring fails`() = testMethodInX {
        method = EquationsPlans.SolveEquationByFactoring
        inputExpr = "[x^2] + x + 1 = 0"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test solving quadratic equation by factoring it by guessing`() = testMethodInX {
        method = EquationsPlans.SolveEquationByFactoring
        inputExpr = "[x ^ 2] + 5 x + 6 = 0"

        check {
            fromExpr = "[x ^ 2] + 5 x + 6 = 0"
            toExpr = "SetSolution[x: {-3, -2}]"
            explanation {
                key = EquationsExplanation.SolveEquationByFactoring
            }

            step {
                fromExpr = "[x ^ 2] + 5 x + 6 = 0"
                toExpr = "(x + 2) (x + 3) = 0"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }
            }

            step {
                fromExpr = "(x + 2) (x + 3) = 0"
                toExpr = "x + 2 = 0 OR x + 3 = 0"
                explanation {
                    key = EquationsExplanation.SeparateFactoredEquation
                }
            }

            step {
                toExpr = "SetSolution[x: {-3, -2}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }

                task {
                    taskId = "#1"
                    startExpr = "x + 2 = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "x + 2 = 0"
                        toExpr = "SetSolution[x: {-2}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "x + 3 = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "x + 3 = 0"
                        toExpr = "SetSolution[x: {-3}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: {-3, -2}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test solving simple quadratic equation by factoring`() = testMethodInX {
        method = EquationsPlans.SolveEquationByFactoring
        inputExpr = "[x ^ 2] - 4 = 0"

        check {
            fromExpr = "[x ^ 2] - 4 = 0"
            toExpr = "SetSolution[x: {-2, 2}]"
            explanation {
                key = EquationsExplanation.SolveEquationByFactoring
            }

            step {
                fromExpr = "[x ^ 2] - 4 = 0"
                toExpr = "(x - 2) (x + 2) = 0"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }
            }

            step {
                fromExpr = "(x - 2) (x + 2) = 0"
                toExpr = "x - 2 = 0 OR x + 2 = 0"
                explanation {
                    key = EquationsExplanation.SeparateFactoredEquation
                }
            }

            step {
                fromExpr = "x - 2 = 0 OR x + 2 = 0"
                toExpr = "SetSolution[x: {-2, 2}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }

                task {
                    taskId = "#1"
                    startExpr = "x - 2 = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "x - 2 = 0"
                        toExpr = "SetSolution[x: {2}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "x + 2 = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "x + 2 = 0"
                        toExpr = "SetSolution[x: {-2}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: {-2, 2}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test solving tricky higher order equation by factoring`() = testMethodInX {
        method = EquationsPlans.SolveEquationByFactoring
        inputExpr = "[x ^ 6] = 5 [x ^ 5] - 3 [x ^ 4]"

        check {
            fromExpr = "[x ^ 6] = 5 [x ^ 5] - 3 [x ^ 4]"
            toExpr = "SetSolution[x: {0, [5 - sqrt[13] / 2], [5 + sqrt[13] / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquationByFactoring
            }

            step {
                fromExpr = "[x ^ 6] = 5 [x ^ 5] - 3 [x ^ 4]"
                toExpr = "[x ^ 6] - 5 [x ^ 5] + 3 [x ^ 4] = 0"
                explanation {
                    key = EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "[x ^ 6] - 5 [x ^ 5] + 3 [x ^ 4] = 0"
                toExpr = "[x ^ 4] ([x ^ 2] - 5 x + 3) = 0"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }
            }

            step {
                fromExpr = "[x ^ 4] ([x ^ 2] - 5 x + 3) = 0"
                toExpr = "[x ^ 4] = 0 OR [x ^ 2] - 5 x + 3 = 0"
                explanation {
                    key = EquationsExplanation.SeparateFactoredEquation
                }
            }

            step {
                toExpr = "SetSolution[x: {0, [5 - sqrt[13] / 2], [5 + sqrt[13] / 2]}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 4] = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "[x ^ 4] = 0"
                        toExpr = "SetSolution[x: {0}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUsingRootsMethod
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[x ^ 2] - 5 x + 3 = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "[x ^ 2] - 5 x + 3 = 0"
                        toExpr = "SetSolution[x: {[5 - sqrt[13] / 2], [5 + sqrt[13] / 2]}]"
                        explanation {
                            key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: {0, [5 - sqrt[13] / 2], [5 + sqrt[13] / 2]}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test solving higher order equation which factors into multiple terms`() = testMethodInX {
        method = EquationsPlans.SolveEquationByFactoring
        inputExpr = "[x ^ 6] - [x ^ 2] = 0"

        check {
            fromExpr = "[x ^ 6] - [x ^ 2] = 0"
            toExpr = "SetSolution[x: {-1, 0, 1}]"
            explanation {
                key = EquationsExplanation.SolveEquationByFactoring
            }

            step {
                fromExpr = "[x ^ 6] - [x ^ 2] = 0"
                toExpr = "[x ^ 2] (x - 1) (x + 1) ([x ^ 2] + 1) = 0"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }
            }

            step {
                fromExpr = "[x ^ 2] (x - 1) (x + 1) ([x ^ 2] + 1) = 0"
                toExpr = "[x ^ 2] = 0 OR x - 1 = 0 OR x + 1 = 0 OR [x ^ 2] + 1 = 0"
                explanation {
                    key = EquationsExplanation.SeparateFactoredEquation
                }
            }

            step {
                toExpr = "SetSolution[x: {-1, 0, 1}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "[x ^ 2] = 0"
                        toExpr = "SetSolution[x: {0}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUsingRootsMethod
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "x - 1 = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "x - 1 = 0"
                        toExpr = "SetSolution[x: {1}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "x + 1 = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "x + 1 = 0"
                        toExpr = "SetSolution[x: {-1}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#4"
                    startExpr = "[x ^ 2] + 1 = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "[x ^ 2] + 1 = 0"
                        toExpr = "Contradiction[x: [x ^ 2] = -1]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUsingRootsMethod
                        }
                    }
                }

                task {
                    taskId = "#5"
                    startExpr = "SetSolution[x: {-1, 0, 1}]"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }
}
