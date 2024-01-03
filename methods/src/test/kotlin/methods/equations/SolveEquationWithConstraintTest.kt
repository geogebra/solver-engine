package methods.equations

import engine.methods.testMethodInX
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.general.GeneralExplanation
import methods.inequalities.InequalitiesExplanation
import org.junit.jupiter.api.Test

class SolveEquationWithConstraintTest {
    @Test
    fun `test no overall solutions`() =
        testMethodInX {
            method = EquationsPlans.SolveEquationWithInequalityConstraint
            inputExpr = "x + 1 = 0 AND x > 0"

            check {
                fromExpr = "x + 1 = 0 AND x > 0"
                toExpr = "Contradiction[x: SetSolution[x: {-1}] AND SetSolution[x: ( 0, /infinity/ )]]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                task {
                    taskId = "#1"
                    startExpr = "x > 0"
                    explanation {
                        key = EquationsExplanation.SimplifyConstraint
                    }

                    step {
                        fromExpr = "x > 0"
                        toExpr = "SetSolution[x: ( 0, /infinity/ )]"
                        explanation {
                            key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "x + 1 = 0"
                    explanation {
                        key = EquationsExplanation.SolveEquationWithoutConstraint
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
                    taskId = "#3"
                    startExpr = "Contradiction[x: SetSolution[x: {-1}] AND SetSolution[x: ( 0, /infinity/ )]]"
                    explanation {
                        key = EquationsExplanation.NoSolutionSatisfiesConstraint
                    }
                }
            }
        }

    @Test
    fun `test one solution removed`() =
        testMethodInX {
            method = EquationsPlans.SolveEquationWithInequalityConstraint
            inputExpr = "[x ^ 2] = 4 AND x + 1 < 0"

            check {
                fromExpr = "[x ^ 2] = 4 AND x + 1 < 0"
                toExpr = "SetSolution[x: {-2}]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                task {
                    taskId = "#1"
                    startExpr = "x + 1 < 0"
                    explanation {
                        key = EquationsExplanation.SimplifyConstraint
                    }

                    step {
                        fromExpr = "x + 1 < 0"
                        toExpr = "SetSolution[x: ( -/infinity/, -1 )]"
                        explanation {
                            key = InequalitiesExplanation.SolveLinearInequality
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[x ^ 2] = 4"
                    explanation {
                        key = EquationsExplanation.SolveEquationWithoutConstraint
                    }

                    step {
                        fromExpr = "[x ^ 2] = 4"
                        toExpr = "SetSolution[x: {-2, 2}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUsingRootsMethod
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: {-2}]"
                    explanation {
                        key = EquationsExplanation.SomeSolutionsDoNotSatisfyConstraint
                    }
                }
            }
        }

    @Test
    fun `test constraint remains an inequality`() =
        testMethodInX {
            method = EquationsPlans.SolveEquationWithInequalityConstraint
            inputExpr = "[x ^ 2] = 1 AND [x ^ 2] < 0"

            check {
                fromExpr = "[x ^ 2] = 1 AND [x ^ 2] < 0"
                toExpr = "Contradiction[x: SetSolution[x: {-1, 1}] AND [x ^ 2] < 0]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] = 1"
                    explanation {
                        key = EquationsExplanation.SolveEquationWithoutConstraint
                    }

                    step {
                        fromExpr = "[x ^ 2] = 1"
                        toExpr = "SetSolution[x: {-1, 1}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUsingRootsMethod
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[(-1) ^ 2] < 0"
                    explanation {
                        key = EquationsExplanation.CheckIfSolutionSatisfiesConstraint
                    }

                    step {
                        fromExpr = "[(-1) ^ 2] < 0"
                        toExpr = "1 < 0"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                        }
                    }

                    step {
                        fromExpr = "1 < 0"
                        toExpr = "Contradiction[1 < 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "[1 ^ 2] < 0"
                    explanation {
                        key = EquationsExplanation.CheckIfSolutionSatisfiesConstraint
                    }

                    step {
                        fromExpr = "[1 ^ 2] < 0"
                        toExpr = "1 < 0"
                        explanation {
                            key = GeneralExplanation.EvaluateOneToAnyPower
                        }
                    }

                    step {
                        fromExpr = "1 < 0"
                        toExpr = "Contradiction[1 < 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#4"
                    startExpr = "Contradiction[x: SetSolution[x: {-1, 1}] AND [x ^ 2] < 0]"
                    explanation {
                        key = EquationsExplanation.NoSolutionSatisfiesConstraint
                    }
                }
            }
        }
}
