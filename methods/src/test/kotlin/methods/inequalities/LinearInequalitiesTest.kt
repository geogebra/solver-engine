/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods.inequalities

import engine.context.Context
import engine.methods.testMethod
import engine.methods.testMethodInX
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class LinearInequalitiesTest {
    @Test
    fun `test ax greater than b linear inequality with positive a`() =
        testMethodInX {
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
                        key = methods.solvable.InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
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
    fun `test ax less than or equal b linear inequality with negative a`() =
        testMethodInX {
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
                        key = methods.solvable.InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                    }

                    step {
                        fromExpr = "-3 x <= 1"
                        toExpr = "[-3 x / -3] >= [1 / -3]"
                        explanation {
                            key = methods.solvable.InequalitiesExplanation.DivideByCoefficientOfVariableAndFlipTheSign
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
    fun `test linear inequality with a negative logarithmic coefficient`() =
        testMethodInX {
            method = InequalitiesPlans.SolveLinearInequality
            inputExpr = "x * ln[2] < 2 x * ln[3] + ln[3]"

            check {
                toExpr = "SetSolution[x: ([ln[3] / ln[2] - 2 ln[3]], /infinity/)]"
                explanation {
                    key = InequalitiesExplanation.SolveLinearInequality
                }

                step {
                    toExpr = "x (ln[2] - 2 ln[3]) < ln[3]"
                }

                step {
                    fromExpr = "x (ln[2] - 2 ln[3]) < ln[3]"
                    toExpr = "x > [ln[3] / ln[2] - 2 ln[3]]"
                    explanation {
                        key = InequalitiesExplanation.DetermineCoefficientSignAndDivide
                    }

                    task {
                        taskId = "#1"
                        startExpr = "ln[2] - 2 ln[3] < 0"
                        explanation {
                            key = InequalitiesExplanation.DetermineSignOfCoefficient
                            param { expr = "(ln[2] - 2 ln[3])" }
                        }

                        step {
                            toExpr = "ln[[2 / 9]] < 0"
                            explanation {
                                key = methods.logs.LogsExplanation.CollectLogarithmsInSum
                            }
                        }

                        step {
                            toExpr = "Identity[ln[[2 / 9]] < 0]"
                            explanation {
                                key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x (ln[2] - 2 ln[3]) < ln[3]"
                        explanation {
                            key = methods.solvable.InequalitiesExplanation
                                .DivideByCoefficientOfVariableAndSimplify
                            param { expr = "(ln[2] - 2 ln[3])" }
                        }

                        step {
                            toExpr = "x > [ln[3] / ln[2] - 2 ln[3]]"
                            explanation {
                                key = methods.solvable.InequalitiesExplanation
                                    .DivideByCoefficientOfVariableAndSimplify
                            }
                        }
                    }
                }

                step {
                    toExpr = "SetSolution[x: ([ln[3] / ln[2] - 2 ln[3]], /infinity/)]"
                    explanation {
                        key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test linear inequality with a positive logarithmic coefficient`() =
        testMethodInX {
            method = InequalitiesPlans.SolveLinearInequality
            inputExpr = "x * ln[3] > x * ln[2] + ln[2]"

            check {
                toExpr = "SetSolution[x: ([ln[2] / ln[3] - ln[2]], /infinity/)]"
                explanation {
                    key = InequalitiesExplanation.SolveLinearInequality
                }
            }
        }

    @Test
    fun `test linear inequality with a positive coefficient of logs with base below one`() =
        testMethodInX {
            method = InequalitiesPlans.SolveLinearInequality
            inputExpr = "x * log_[[1 / 2]][2] < 2 x * log_[[1 / 2]][3] + 1"

            check {
                toExpr =
                    "SetSolution[x: (-/infinity/, [1 / log_[[1 / 2]][2] - 2 log_[[1 / 2]][3]])]"
                explanation {
                    key = InequalitiesExplanation.SolveLinearInequality
                }
            }
        }

    @Test
    fun `test linear inequality with a negative radical coefficient`() =
        testMethodInX {
            method = InequalitiesPlans.SolveLinearInequality
            inputExpr = "x * (sqrt[2] - sqrt[8]) > 1"

            check {
                toExpr = "SetSolution[x: (-/infinity/, -[sqrt[2] / 2])]"
                explanation {
                    key = InequalitiesExplanation.SolveLinearInequality
                }
            }
        }

    @Test
    fun `test linear inequality with a zero logarithmic coefficient is not divided`() =
        testMethodInX {
            method = InequalitiesPlans.SolveLinearInequality
            inputExpr = "x * ln[4] < 2 x * ln[2] + 1"

            check {
                toExpr = "Identity[x: 0 < 1]"
                explanation {
                    key = InequalitiesExplanation.SolveLinearInequality
                }
            }
        }

    @Test
    fun `test a greater than b + cx linear inequality`() =
        testMethodInX {
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
                        key = methods.solvable.InequalitiesExplanation.MoveConstantsToTheLeftBySubtraction
                    }
                }

                step {
                    fromExpr = "-7 > [x / 3]"
                    toExpr = "[x / 3] < -7"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.FlipInequality
                    }
                }

                step {
                    fromExpr = "[x / 3] < -7"
                    toExpr = "x < -21"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.MultiplyByDenominatorOfVariableLHSAndSimplify
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
    fun `test ax + b less than cx + d linear equation with c larger than a`() =
        testMethodInX {
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
                        key = methods.solvable.InequalitiesExplanation.MoveVariablesToTheRightBySubtraction
                    }
                }

                step {
                    fromExpr = "1 < x + 3"
                    toExpr = "-2 < x"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.MoveConstantsToTheLeftBySubtraction
                    }
                }

                step {
                    fromExpr = "-2 < x"
                    toExpr = "x > -2"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.FlipInequality
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
    fun `test ax greater than b linear inequality with a a negative fraction`() =
        testMethodInX {
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
                    toExpr = "[1 / 3] x < -7"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.NegateBothSidesAndFlipTheSign
                    }
                }

                step {
                    fromExpr = "[1 / 3] x < -7"
                    toExpr = " x < -21"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                    }

                    step {
                        fromExpr = "[1 / 3] x < -7"
                        toExpr = "3 * [1 / 3] x < 3 * (-7)"
                        explanation {
                            key = methods.solvable.InequalitiesExplanation.MultiplyByInverseCoefficientOfVariable
                        }
                    }

                    step {
                        fromExpr = "3 * [1 / 3] x < 3 * (-7)"
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
    fun `test linear inequality with no solutions`() =
        testMethodInX {
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
    fun `test linear inequality with infinitely many solutions`() =
        testMethodInX {
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
    fun `test multivariate linear inequality`() =
        testMethod {
            method = InequalitiesPlans.SolveLinearInequality
            context = Context(solutionVariables = listOf("b"))
            inputExpr = "3 a + 2 b < 9"

            check {
                fromExpr = "3 a + 2 b < 9"
                toExpr = "SetSolution[b: (-/infinity/, [9 - 3 a / 2])]"
                explanation {
                    key = InequalitiesExplanation.SolveLinearInequality
                }

                step {
                    fromExpr = "3 a + 2 b < 9"
                    toExpr = "2 b < 9 - 3 a"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation
                            .MoveConstantsToTheRightBySubtraction
                    }
                }

                step {
                    fromExpr = "2 b < 9 - 3 a"
                    toExpr = "b < [9 - 3 a / 2]"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation
                            .DivideByCoefficientOfVariableAndSimplify
                    }
                }

                step {
                    fromExpr = "b < [9 - 3 a / 2]"
                    toExpr = "SetSolution[b: (-/infinity/, [9 - 3 a / 2])]"
                    explanation {
                        key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test linear inequality with no variable`() =
        testMethodInX {
            method = InequalitiesPlans.SolveLinearInequality
            inputExpr = "1 < 2"

            check {
                noTransformation()
            }
        }

    @Test
    fun `test linear inequality without solution variable`() =
        testMethodInX {
            method = InequalitiesPlans.SolveLinearInequality
            inputExpr = "y + 1 < 2y"

            check {
                noTransformation()
            }
        }

    @Test
    fun `test linear inequality with solution variable having a non-constant coefficient`() =
        testMethodInX {
            method = InequalitiesPlans.SolveLinearInequality
            inputExpr = "ax < y"

            check {
                noTransformation()
            }
        }

    @Test
    fun `test unknown factored coefficient does not make fallback oscillate`() =
        testMethodInX {
            method = InequalitiesPlans.SolveLinearInequality
            inputExpr = "x (a + b) < 1"

            check {
                noTransformation()
            }
        }

    @Test
    fun `test divide task uses plan explanation for multivariate inequality`() {
        val context = Context(solutionVariables = listOf("x"))
        val expression = parseExpression("x (ln[2] - 2 ln[3]) < y")
        val coefficient = parseExpression("ln[2] - 2 ln[3]")
        val explanation = solvablePlansForInequalities.divideByCoefficientOfVariableAndSimplifyExplanation(
            context,
            expression,
            coefficient,
        )

        assertEquals(
            methods.solvable.InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify,
            explanation.key,
        )
    }
}
