package methods.equations

import engine.methods.testMethodInX
import methods.general.GeneralExplanation
import methods.inequalities.InequalitiesExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class SolveEquationWithConstraintTest {

    @Test
    fun `test no overall solutions`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithConstraint
        inputExpr = "x + 1 = 0 GIVEN x > 0"
        check {
            fromExpr = "x + 1 = 0 GIVEN x > 0"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }
            toExpr = "Contradiction[x: SetSolution[x: {-1}] GIVEN SetSolution[x: ( 0, /infinity/ )]]"

            task {
                taskId = "#1"
                startExpr = "x > 0"
                explanation {
                    key = EquationsExplanation.SimplifyConstraint
                }

                step {
                    fromExpr = "x > 0"
                    explanation {
                        key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                    }
                    toExpr = "SetSolution[x: ( 0, /infinity/ )]"
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
                    explanation {
                        key = EquationsExplanation.SolveLinearEquation
                    }
                    toExpr = "SetSolution[x: {-1}]"
                }
            }

            task {
                taskId = "#3"
                startExpr = "Contradiction[x: SetSolution[x: {-1}] GIVEN SetSolution[x: ( 0, /infinity/ )]]"
                explanation {
                    key = EquationsExplanation.NoSolutionSatisfiesConstraint
                }
            }
        }
    }

    @Test
    fun `test one solution removed`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithConstraint
        inputExpr = "[x ^ 2] = 4 GIVEN x + 1 < 0"
        check {
            fromExpr = "[x ^ 2] = 4 GIVEN x + 1 < 0"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }
            toExpr = "SetSolution[x: {-2}]"

            task {
                taskId = "#1"
                startExpr = "x + 1 < 0"
                explanation {
                    key = EquationsExplanation.SimplifyConstraint
                }

                step {
                    fromExpr = "x + 1 < 0"
                    explanation {
                        key = InequalitiesExplanation.SolveLinearInequality
                    }
                    toExpr = "SetSolution[x: ( -/infinity/, -1 )]"
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
                    explanation {
                        key = EquationsExplanation.SolveEquationUsingRootsMethod
                    }
                    toExpr = "SetSolution[x: {-2, 2}]"
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
    fun `test constraint remains an inequality`() = testMethodInX {
        method = EquationsPlans.SolveEquationWithConstraint
        inputExpr = "[x ^ 2] = 1 GIVEN [x ^ 2] < 0"
        check {
            fromExpr = "[x ^ 2] = 1 GIVEN [x ^ 2] < 0"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }
            toExpr = "Contradiction[x: SetSolution[x: {-1, 1}] GIVEN [x ^ 2] < 0]"

            task {
                taskId = "#1"
                startExpr = "[x ^ 2] = 1"
                explanation {
                    key = EquationsExplanation.SolveEquationWithoutConstraint
                }

                step {
                    fromExpr = "[x ^ 2] = 1"
                    explanation {
                        key = EquationsExplanation.SolveEquationUsingRootsMethod
                    }
                    toExpr = "SetSolution[x: {-1, 1}]"
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
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyEvenPowerOfNegative
                    }
                    toExpr = "[1 ^ 2] < 0"
                }

                step {
                    fromExpr = "[1 ^ 2] < 0"
                    toExpr = "1 < 0"
                }
                step {
                    fromExpr = "1 < 0"
                    explanation {
                        key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                    }
                    toExpr = "Contradiction[1 < 0]"
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
                    explanation {
                        key = GeneralExplanation.EvaluateOneToAnyPower
                    }
                    toExpr = "1 < 0"
                }

                step {
                    fromExpr = "1 < 0"
                    explanation {
                        key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                    }
                    toExpr = "Contradiction[1 < 0]"
                }
            }

            task {
                taskId = "#4"
                startExpr = "Contradiction[x: SetSolution[x: {-1, 1}] GIVEN [x ^ 2] < 0]"
                explanation {
                    key = EquationsExplanation.NoSolutionSatisfiesConstraint
                }
            }
        }
    }
}
