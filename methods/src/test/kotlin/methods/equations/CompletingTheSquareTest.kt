package methods.equations

import engine.methods.testMethodInX
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.general.GeneralExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class CompletingTheSquareTest {

    private fun shortTest(inputExpr: String, toExpr: String?) = testMethodInX {
        method = EquationsPlans.SolveByCompletingTheSquare
        this.inputExpr = inputExpr

        check {
            if (toExpr == null) {
                noTransformation()
            } else {
                this.toExpr = toExpr
                explanation {
                    key = EquationsExplanation.SolveByCompletingTheSquare
                }
            }
        }
    }

    @Test
    fun `short test integer solutions`() = shortTest(
        inputExpr = "[x ^ 2] + 2 x = 8",
        toExpr = "SetSolution[x: {-4, 2}]",
    )

    @Test
    fun `short test surd solutions`() = shortTest(
        inputExpr = "[x ^ 2] = x + 3",
        toExpr = "SetSolution[x: {[-sqrt[13] + 1 / 2], [sqrt[13] + 1 / 2]}]",
    )

    @Test
    fun `short test no constant term`() = shortTest(
        inputExpr = "x = [x ^ 2]",
        toExpr = "SetSolution[x: {0, 1}]",
    )

    @Test
    fun `short test single solution`() = shortTest(
        inputExpr = "[x ^ 2] - 10 x = -25",
        toExpr = "SetSolution[x: {5}]",
    )

    @Test
    fun `short test no solution`() = shortTest(
        inputExpr = "[x ^ 2] + x + 10 = 0",
        toExpr = "Contradiction[x: [(x + [1 / 2]) ^ 2] = -[39 / 4]]",
    )

    @Test
    fun `short test no linear term`() = shortTest(
        inputExpr = "[x ^ 2] = 100",
        toExpr = null,
    )

    @Test
    fun `short test quadratic coefficient not 1`() = shortTest(
        inputExpr = "2 [x ^ 2] - 45 = x",
        toExpr = "SetSolution[x: {-[9 / 2], 5}]",
    )

    @Test
    fun `short test with rational quadratic coefficient`() = shortTest(
        inputExpr = "[1/2][x^2] + x + [1/2] = 0",
        toExpr = "SetSolution[x: {-1}]",
    )

    @Test
    fun `short test with rearranging first`() = shortTest(
        inputExpr = "[x^2] = 1 - x - [x^2]",
        toExpr = "SetSolution[x: {-1, [1/2]}]",
    )

    @Test
    fun `short test with biquadratic equation`() = shortTest(
        inputExpr = "[x^4] - 3[x^2] + 2 = 0",
        toExpr = "SetSolution[x: {-sqrt[2], -1, 1, sqrt[2]}]",
    )

    @Test
    fun `short test 6th power`() = shortTest(
        inputExpr = "[x^6] + 2 = 3[x^3]",
        toExpr = "SetSolution[x: {1, root[2, 3]}]",
    )

    @Test
    fun `test simple case in details`() = testMethodInX {
        method = EquationsPlans.SolveByCompletingTheSquare
        inputExpr = "[x ^ 2] = 6 x + 5"

        check {
            fromExpr = "[x ^ 2] = 6 x + 5"
            toExpr = "SetSolution[x: {-sqrt[14] + 3, sqrt[14] + 3}]"
            explanation {
                key = EquationsExplanation.SolveByCompletingTheSquare
            }

            step {
                fromExpr = "[x ^ 2] = 6 x + 5"
                toExpr = "[x ^ 2] - 6 x = 5"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "[x ^ 2] - 6 x = 5"
                toExpr = "[(x - 3) ^ 2] = 14"
                explanation {
                    key = EquationsExplanation.RewriteToXPLusASquareEqualsBForm
                }

                step {
                    fromExpr = "[x ^ 2] - 6 x = 5"
                    toExpr = "[x ^ 2] - 6 x + 9 = 14"
                    explanation {
                        key = EquationsExplanation.CompleteTheSquareAndSimplify
                    }

                    step {
                        fromExpr = "[x ^ 2] - 6 x = 5"
                        toExpr = "[x ^ 2] - 6 x + [([-6 / 2]) ^ 2] = 5 + [([-6 / 2]) ^ 2]"
                        explanation {
                            key = EquationsExplanation.CompleteTheSquare
                        }
                    }

                    step {
                        fromExpr = "[x ^ 2] - 6 x + [([-6 / 2]) ^ 2] = 5 + [([-6 / 2]) ^ 2]"
                        toExpr = "[x ^ 2] - 6 x + 9 = 14"
                        explanation {
                            key = PolynomialsExplanation.SimplifyAlgebraicExpression
                        }
                    }
                }

                step {
                    fromExpr = "[x ^ 2] - 6 x + 9 = 14"
                    toExpr = "[(x - 3) ^ 2] = 14"
                    explanation {
                        key = PolynomialsExplanation.FactorTrinomialToSquareAndSimplify
                    }

                    step {
                        fromExpr = "[x ^ 2] - 6 x + 9"
                        toExpr = "[(x + [1 / 2] * (-6)) ^ 2]"
                        explanation {
                            key = PolynomialsExplanation.FactorTrinomialToSquare
                        }
                    }

                    step {
                        fromExpr = "[(x + [1 / 2] * (-6)) ^ 2]"
                        toExpr = "[(x - 3) ^ 2]"
                        explanation {
                            key = PolynomialsExplanation.SimplifyAlgebraicExpression
                        }
                    }
                }
            }

            step {
                fromExpr = "[(x - 3) ^ 2] = 14"
                toExpr = "x - 3 = +/-sqrt[14]"
                explanation {
                    key = EquationsExplanation.TakeRootOfBothSides
                }
            }

            step {
                fromExpr = "x - 3 = +/-sqrt[14]"
                toExpr = "x = +/-sqrt[14] + 3"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "x = +/-sqrt[14] + 3"
                toExpr = "SetSolution[x: {-sqrt[14] + 3, sqrt[14] + 3}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                }
            }
        }
    }

    @Test
    fun `test complex case in details`() = testMethodInX {
        method = EquationsPlans.SolveByCompletingTheSquare
        inputExpr = "2 [x ^ 2] + 5 x = 7"

        check {
            fromExpr = "2 [x ^ 2] + 5 x = 7"
            toExpr = "SetSolution[x: {-[7 / 2], 1}]"
            explanation {
                key = EquationsExplanation.SolveByCompletingTheSquare
            }

            step {
                fromExpr = "2 [x ^ 2] + 5 x = 7"
                toExpr = "[x ^ 2] + [5 / 2] x = [7 / 2]"
                explanation {
                    key = EquationsExplanation.MultiplyByInverseOfLeadingCoefficientAndSimplify
                }

                step {
                    fromExpr = "2 [x ^ 2] + 5 x = 7"
                    toExpr = "(2 [x ^ 2] + 5 x) * [1 / 2] = 7 * [1 / 2]"
                    explanation {
                        key = EquationsExplanation.MultiplyByInverseOfLeadingCoefficient
                    }
                }

                step {
                    fromExpr = "(2 [x ^ 2] + 5 x) * [1 / 2] = 7 * [1 / 2]"
                    toExpr = "[x ^ 2] + [5 / 2] x = [7 / 2]"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }
            }

            step {
                fromExpr = "[x ^ 2] + [5 / 2] x = [7 / 2]"
                toExpr = "[(x + [5 / 4]) ^ 2] = [81 / 16]"
                explanation {
                    key = EquationsExplanation.RewriteToXPLusASquareEqualsBForm
                }

                step {
                    fromExpr = "[x ^ 2] + [5 / 2] x = [7 / 2]"
                    toExpr = "[x ^ 2] + [5 / 2] x + [25 / 16] = [81 / 16]"
                    explanation {
                        key = EquationsExplanation.CompleteTheSquareAndSimplify
                    }

                    step {
                        fromExpr = "[x ^ 2] + [5 / 2] x = [7 / 2]"
                        toExpr = "[x ^ 2] + [5 / 2] x + [([[5 / 2] / 2]) ^ 2] = [7 / 2] + [([[5 / 2] / 2]) ^ 2]"
                        explanation {
                            key = EquationsExplanation.CompleteTheSquare
                        }
                    }

                    step {
                        fromExpr = "[x ^ 2] + [5 / 2] x + [([[5 / 2] / 2]) ^ 2] = [7 / 2] + [([[5 / 2] / 2]) ^ 2]"
                        toExpr = "[x ^ 2] + [5 / 2] x + [25 / 16] = [81 / 16]"
                        explanation {
                            key = PolynomialsExplanation.SimplifyAlgebraicExpression
                        }
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + [5 / 2] x + [25 / 16] = [81 / 16]"
                    toExpr = "[(x + [5 / 4]) ^ 2] = [81 / 16]"
                    explanation {
                        key = PolynomialsExplanation.FactorTrinomialToSquareAndSimplify
                    }
                }
            }

            step {
                fromExpr = "[(x + [5 / 4]) ^ 2] = [81 / 16]"
                toExpr = "x + [5 / 4] = +/-sqrt[[81 / 16]]"
                explanation {
                    key = EquationsExplanation.TakeRootOfBothSides
                }
            }

            step {
                fromExpr = "x + [5 / 4] = +/-sqrt[[81 / 16]]"
                toExpr = "x + [5 / 4] = +/-[9 / 4]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
            }

            step {
                fromExpr = "x + [5 / 4] = +/-[9 / 4]"
                toExpr = "x = +/-[9 / 4] - [5 / 4]"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "x = +/-[9 / 4] - [5 / 4]"
                toExpr = "x = -[9 / 4] - [5 / 4] OR x = [9 / 4] - [5 / 4]"
                explanation {
                    key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                }
            }

            step {
                fromExpr = "x = -[9 / 4] - [5 / 4] OR x = [9 / 4] - [5 / 4]"
                toExpr = "SetSolution[x: {-[7 / 2], 1}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }

                task {
                    taskId = "#1"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "x = -[9 / 4] - [5 / 4]"
                        toExpr = "x = -[7 / 2]"
                    }

                    step {
                        toExpr = "SetSolution[x: {-[7 / 2]}]"
                        explanation {
                            key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                        }
                    }
                }

                task {
                    taskId = "#2"
                    explanation {
                        key = EquationsExplanation.SolveEquationInEquationUnion
                    }

                    step {
                        fromExpr = "x = [9 / 4] - [5 / 4]"
                        toExpr = "x = 1"
                    }

                    step {
                        toExpr = "SetSolution[x: {1}]"
                        explanation {
                            key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                        }
                    }
                }

                task {
                    taskId = "#3"
                    explanation {
                        key = EquationsExplanation.CollectSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test expand before multiplying by leading coefficient inverse`() = testMethodInX {
        method = EquationsPlans.SolveByCompletingTheSquare
        inputExpr = "3x+(-x-1)+2x*(x+2)=[11/4]"

        check {
            fromExpr = "3 x + (-x - 1) + 2 x * (x + 2) = [11 / 4]"
            toExpr = "SetSolution[x: {-[sqrt[66] + 6 / 4], [sqrt[66] - 6 / 4]}]"
            explanation {
                key = EquationsExplanation.SolveByCompletingTheSquare
            }

            step {
                fromExpr = "3 x + (-x - 1) + 2 x * (x + 2) = [11 / 4]"
                toExpr = "3 x + (-x - 1) + 2 x (x + 2) = [11 / 4]"
                explanation {
                    key = GeneralExplanation.NormalizeProducts
                }
            }

            step {
                fromExpr = "3 x + (-x - 1) + 2 x (x + 2) = [11 / 4]"
                toExpr = "2 x - 1 + 2 x (x + 2) = [11 / 4]"
                explanation {
                    key = EquationsExplanation.SimplifyEquation
                }
            }

            step {
                fromExpr = "2 x - 1 + 2 x (x + 2) = [11 / 4]"
                toExpr = "6 x - 1 + 2 [x ^ 2] = [11 / 4]"
                explanation {
                    key = PolynomialsExplanation.ExpandPolynomialExpression
                }
            }

            step {
                fromExpr = "6 x - 1 + 2 [x ^ 2] = [11 / 4]"
                toExpr = "6 x + 2 [x ^ 2] = [15 / 4]"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step { }
            step { }
            step { }
            step { }
            step { }
            step { }
            step { }
        }
    }
}
