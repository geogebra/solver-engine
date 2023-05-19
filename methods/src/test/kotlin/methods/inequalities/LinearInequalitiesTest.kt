package methods.inequalities

import engine.methods.testMethodInX
import org.junit.jupiter.api.Test

class LinearInequalitiesTest {

    @Test
    fun `test ax greater than b linear inequality with positive a`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "3 x > 1"

        check {
            fromExpr = "3 x > 1"
            toExpr = "SetSolution[x: ( [1 / 3], /infinity/ )]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "3 x > 1"
                toExpr = "x > [1 / 3]"
                explanation {
                    key = InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x > [1 / 3]"
                toExpr = "SetSolution[x: ( [1 / 3], /infinity/ )]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test ax less than or equal b linear inequality with negative a`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "-3 x <= 1"

        check {
            fromExpr = "-3 x <= 1"
            toExpr = "SetSolution[x: [ -[1 / 3], /infinity/ )]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "-3 x <= 1"
                toExpr = "x >= -[1 / 3]"
                explanation {
                    key = InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                }

                step {
                    fromExpr = "-3 x <= 1"
                    toExpr = "[-3 x / -3] >= [1 / -3]"
                    explanation {
                        key = InequalitiesExplanation.DivideByCoefficientOfVariableAndFlipTheSign
                    }
                }

                step {
                    fromExpr = "[-3 x / -3] >= [1 / -3]"
                    toExpr = "x >= -[1 / 3]"
                    explanation {
                        key = InequalitiesExplanation.SimplifyInequality
                    }
                }
            }

            step {
                fromExpr = "x >= -[1 / 3]"
                toExpr = "SetSolution[x: [ -[1 / 3], /infinity/ )]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test a greater than b + cx linear inequality`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "4 > 11 + [x / 3]"

        check {
            fromExpr = "4 > 11 + [x / 3]"
            toExpr = "SetSolution[x: ( -/infinity/, -21 )]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "4 > 11 + [x / 3]"
                toExpr = "-7 > [x / 3]"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveConstantsToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "-7 > [x / 3]"
                toExpr = "[x / 3] < -7"
                explanation {
                    key = InequalitiesExplanation.FlipInequality
                }
            }

            step {
                fromExpr = "[x / 3] < -7"
                toExpr = "x < -21"
                explanation {
                    key = InequalitiesExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x < -21"
                toExpr = "SetSolution[x: ( -/infinity/, -21 )]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test ax + b less than cx + d linear equation with c larger than a`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "x + 1 < 2 x + 3"

        check {
            fromExpr = "x + 1 < 2 x + 3"
            toExpr = "SetSolution[x: ( -2, /infinity/ )]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "x + 1 < 2 x + 3"
                toExpr = "1 < x + 3"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveVariablesToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "1 < x + 3"
                toExpr = "-2 < x"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveConstantsToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "-2 < x"
                toExpr = "x > -2"
                explanation {
                    key = InequalitiesExplanation.FlipInequality
                }
            }

            step {
                fromExpr = "x > -2"
                toExpr = "SetSolution[x: ( -2, /infinity/ )]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test ax greater than b linear inequality with a a negative fraction`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "-[1 / 3] x > 7"

        check {
            fromExpr = "-[1 / 3] x > 7"
            toExpr = "SetSolution[x: ( -/infinity/, -21 )]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "-[1 / 3] x > 7"
                toExpr = "x < -21"
                explanation {
                    key = InequalitiesExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                }

                step {
                    fromExpr = "-[1 / 3] x > 7"
                    toExpr = "(-[1 / 3] x) * (-3) < 7 * (-3)"
                    explanation {
                        key = InequalitiesExplanation.MultiplyByInverseCoefficientOfVariableAndFlipTheSign
                    }
                }

                step {
                    fromExpr = "(-[1 / 3] x) * (-3) < 7 * (-3)"
                    toExpr = "x < -21"
                    explanation {
                        key = InequalitiesExplanation.SimplifyInequality
                    }
                }
            }

            step {
                fromExpr = "x < -21"
                toExpr = "SetSolution[x: ( -/infinity/, -21 )]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test linear inequality with no solutions`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "3 x - 2 > 3 x + 3"

        check {
            fromExpr = "3 x - 2 > 3 x + 3"
            toExpr = "Contradiction[x: -2 > 3]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "3 x - 2 > 3 x + 3"
                toExpr = "-2 > 3"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.CancelCommonTermsOnBothSides
                }
            }

            step {
                fromExpr = "-2 > 3"
                toExpr = "Contradiction[x: -2 > 3]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromFalseInequality
                }
            }
        }
    }

    @Test
    fun `test linear inequality with infinitely many solutions`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "3 x - 2 <= 3 x + 3"

        check {
            fromExpr = "3 x - 2 <= 3 x + 3"
            toExpr = "Identity[x: -2 <= 3]"
            explanation {
                key = InequalitiesExplanation.SolveLinearInequality
            }

            step {
                fromExpr = "3 x - 2 <= 3 x + 3"
                toExpr = "-2 <= 3"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.CancelCommonTermsOnBothSides
                }
            }

            step {
                fromExpr = "-2 <= 3"
                toExpr = "Identity[x: -2 <= 3]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromTrueInequality
                }
            }
        }
    }

    @Test
    fun `test linear inequality with no variable`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "1 < 2"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test linear inequality with two variables`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "x < y"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test linear inequality without solution variable`() = testMethodInX {
        method = InequalitiesPlans.SolveLinearInequality
        inputExpr = "y + 1 < 2y"

        check {
            noTransformation()
        }
    }
}
