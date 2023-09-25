package methods.equations

import engine.context.Context
import engine.methods.testMethod
import methods.general.GeneralExplanation
import methods.inequations.InequationsExplanation
import kotlin.test.Test

class MultivariateEquationsTest {

    @Test
    fun `test single step linear equation without constraint`() = testMethod {
        method = EquationsPlans.SolveEquation
        context = Context(solutionVariables = listOf("A"))
        inputExpr = "A - b h = 0"

        check {
            fromExpr = "A - b h = 0"
            toExpr = "SetSolution[A: {b h}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "A - b h = 0"
                toExpr = "A = b h"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRightAndSimplify
                }

                step {
                    fromExpr = "A - b h = 0"
                    toExpr = "A - b h + b h = 0 + b h"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRight
                    }
                }

                step {
                    fromExpr = "A - b h + b h = 0 + b h"
                    toExpr = "A = b h"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }
            }

            step {
                fromExpr = "A = b h"
                toExpr = "SetSolution[A: {b h}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test two step linear equation without constraint`() = testMethod {
        method = EquationsPlans.SolveEquation
        context = Context(solutionVariables = listOf("C"))
        inputExpr = "F = [9 / 5] C + 32"

        check {
            fromExpr = "F = [9 / 5] C + 32"
            toExpr = "SetSolution[C: {[5 / 9] (F - 32)}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "F = [9 / 5] C + 32"
                toExpr = "F - 32 = [9 / 5] C"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheLeftAndSimplify
                }

                step {
                    fromExpr = "F = [9 / 5] C + 32"
                    toExpr = "F - 32 = [9 / 5] C + 32 - 32"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheLeft
                    }
                }

                step {
                    fromExpr = "F - 32 = [9 / 5] C + 32 - 32"
                    toExpr = "F - 32 = [9 / 5] C"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }
            }

            step {
                fromExpr = "F - 32 = [9 / 5] C"
                toExpr = "[9 / 5] C = F - 32"
                explanation {
                    key = methods.solvable.EquationsExplanation.FlipEquation
                }
            }

            step {
                fromExpr = "[9 / 5] C = F - 32"
                toExpr = "C = [5 / 9] (F - 32)"
                explanation {
                    key = methods.solvable.EquationsExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                }

                step {
                    fromExpr = "[9 / 5] C = F - 32"
                    toExpr = "[9 / 5] C * [5 / 9] = (F - 32) * [5 / 9]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MultiplyByInverseCoefficientOfVariable
                    }
                }

                step {
                    fromExpr = "[9 / 5] C * [5 / 9] = (F - 32) * [5 / 9]"
                    toExpr = "C = [5 / 9] (F - 32)"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }
            }

            step {
                fromExpr = "C = [5 / 9] (F - 32)"
                toExpr = "SetSolution[C: {[5 / 9] (F - 32)}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test two step linear equation with constraint`() = testMethod {
        method = EquationsPlans.SolveEquation
        context = Context(solutionVariables = listOf("r"))
        inputExpr = "I (r + R) = E"

        check {
            fromExpr = "I (r + R) = E"
            toExpr = "SetSolution[r: {[E / I] - R}] GIVEN SetSolution[I: /reals/ \\ {0}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "I (r + R) = E"
                toExpr = "r + R = [E / I] GIVEN SetSolution[I: /reals/ \\ {0}]"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                }

                step {
                    fromExpr = "I (r + R) = E"
                    toExpr = "[I (r + R) / I] = [E / I] GIVEN I != 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableMultivariate
                    }
                }

                step {
                    fromExpr = "[I (r + R) / I] = [E / I] GIVEN I != 0"
                    toExpr = "r + R = [E / I] GIVEN I != 0"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }

                step {
                    fromExpr = "r + R = [E / I] GIVEN I != 0"
                    toExpr = "r + R = [E / I] GIVEN SetSolution[I: /reals/ \\ {0}]"
                    explanation {
                        key = EquationsExplanation.SimplifyConstraint
                    }

                    step {
                        fromExpr = "I != 0"
                        toExpr = "SetSolution[I: /reals/ \\ {0}]"
                        explanation {
                            key = InequationsExplanation.ExtractSolutionFromInequationInSolvedForm
                        }
                    }
                }
            }

            step {
                fromExpr = "r + R = [E / I] GIVEN SetSolution[I: /reals/ \\ {0}]"
                toExpr = "r = [E / I] - R GIVEN SetSolution[I: /reals/ \\ {0}]"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRightAndSimplify
                }

                step {
                    fromExpr = "r + R = [E / I]"
                    toExpr = "r + R - R = [E / I] - R"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRight
                    }
                }

                step {
                    fromExpr = "r + R - R = [E / I] - R"
                    toExpr = "r = [E / I] - R"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }
            }

            step {
                fromExpr = "r = [E / I] - R GIVEN SetSolution[I: /reals/ \\ {0}]"
                toExpr = "SetSolution[r: {[E / I] - R}] GIVEN SetSolution[I: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test linear equation with two constraints`() = testMethod {
        method = EquationsPlans.SolveEquation
        context = Context(solutionVariables = listOf("x"))
        inputExpr = "(x z + 1) (y + 2) = 1"

        check {
            fromExpr = "(x z + 1) (y + 2) = 1"
            toExpr = "SetSolution[x: {[[1 / y + 2] - 1 / z]}] " +
                "GIVEN SetSolution[y: /reals/ \\ {-2}] AND SetSolution[z: /reals/ \\ {0}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "(x z + 1) (y + 2) = 1"
                toExpr = "x z + 1 = [1 / y + 2] GIVEN SetSolution[y: /reals/ \\ {-2}]"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                }
            }

            step {
                fromExpr = "x z + 1 = [1 / y + 2] GIVEN SetSolution[y: /reals/ \\ {-2}]"
                toExpr = "x z = [1 / y + 2] - 1 GIVEN SetSolution[y: /reals/ \\ {-2}]"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "x z = [1 / y + 2] - 1 GIVEN SetSolution[y: /reals/ \\ {-2}]"
                toExpr = "x = [[1 / y + 2] - 1 / z] " +
                    "GIVEN SetSolution[y: /reals/ \\ {-2}] AND SetSolution[z: /reals/ \\ {0}]"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                }
            }

            step {
                fromExpr = "x = [[1 / y + 2] - 1 / z] " +
                    "GIVEN SetSolution[y: /reals/ \\ {-2}] AND SetSolution[z: /reals/ \\ {0}]"
                toExpr = "SetSolution[x: {[[1 / y + 2] - 1 / z]}] " +
                    "GIVEN SetSolution[y: /reals/ \\ {-2}] AND SetSolution[z: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }
}
