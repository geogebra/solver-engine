/*
 * Copyright (c) 2026 GeoGebra GmbH, office@geogebra.org
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

import engine.methods.testMethodInX
import methods.constantexpressions.ConstantExpressionsExplanation
import kotlin.test.Test

class ExponentialInequalitiesTest {
    @Test
    fun `exponential inequality with no solution`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[3 ^ 2 x + 2] < 0"

            check {
                fromExpr = "[3 ^ 2 x + 2] < 0"
                toExpr = "Contradiction[x: [3 ^ 2 x + 2] < 0]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromImpossibleExponentialInequality
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[3 ^ 2 x + 2] < -[3 ^ 5]"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[3 ^ 2 x + 2] < -243"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                    }
                }

                step {
                    toExpr = "Contradiction[x: [3 ^ 2 x + 2] < -243]"
                    explanation {
                        key = InequalitiesExplanation.ExtractSolutionFromImpossibleExponentialInequality
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[3 ^ 2 x + 2] + [4 ^ x + 1] < 0"

            check {
                step {
                    toExpr = "[3 ^ 2 x + 2] < -[4 ^ x + 1]"
                }

                step {
                    toExpr = "Contradiction[x: [3 ^ 2 x + 2] < -[4 ^ x + 1]]"
                    explanation {
                        key = InequalitiesExplanation
                            .ExtractSolutionFromImpossibleExponentialInequalityWithTwoExponentials
                    }
                }
            }
        }
    }

    @Test
    fun `exponential inequality with infinitely many solutions`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[3 ^ 2 x + 2] > 0"

            check {
                fromExpr = "[3 ^ 2 x + 2] > 0"
                toExpr = "Identity[x: [3 ^ 2 x + 2] > 0]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromAlwaysTrueInequality
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[3 ^ 2 x + 2] + [4 ^ x + 1] > 0"

            check {
                step {
                    toExpr = "[3 ^ 2 x + 2] > -[4 ^ x + 1]"
                }

                step {
                    toExpr = "Identity[x: [3 ^ 2 x + 2] > -[4 ^ x + 1]]"
                    explanation {
                        key = InequalitiesExplanation.ExtractSolutionFromAlwaysTrueInequalityWithTwoExponentials
                    }
                }
            }
        }
    }

    @Test
    fun `exponential inequality with rhs equal to one`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[3 ^ 2 x + 2] > 1"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[3 ^ 2 x + 2] > [3 ^ 0]"
                }

                step {
                    toExpr = "SetSolution[x: (-1, /infinity/)]"
                    explanation {
                        key = InequalitiesExplanation.SimplifyExponentialEquationWithSameBasesAndSolve
                    }
                }
            }
        }
    }

    @Test
    fun `exponential inequality with base in (0,1) and rhs equal to one`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[([1 / 2]) ^ 2 x + 2] - 1 > 0"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[([1 / 2]) ^ 2 x + 2] > 1"
                }

                step {
                    toExpr = "[([1 / 2]) ^ 2 x + 2] > [([1 / 2]) ^ 0]"
                }

                step {
                    toExpr = "SetSolution[x: (-/infinity/, -1)]"
                    explanation {
                        key = InequalitiesExplanation.SimplifyExponentialEquationWithSameBasesAndSolve
                    }
                }
            }
        }
    }

    @Test
    fun `exponential inequality with bases powers of each-other`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[1 / [2 ^ [x ^ 2]]] > [1 / 2]"

            check {
                toExpr = "SetSolution[x: (-1, 1)]"
            }
        }
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[2 ^ 3 x] > 8"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[2 ^ 3 x] > [2 ^ 3]"
                }

                step {
                    toExpr = "SetSolution[x: (1, /infinity/)]"
                    explanation {
                        key = InequalitiesExplanation.SimplifyExponentialEquationWithSameBasesAndSolve
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[2 ^ 3 x] < [8 ^ -x + 2]"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[2 ^ 3 x] < [2 ^ 3 (-x + 2)]"
                }

                step {
                    toExpr = "SetSolution[x: (-/infinity/, 1)]"
                    explanation {
                        key = InequalitiesExplanation.SimplifyExponentialEquationWithSameBasesAndSolve
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[([2 / 3]) ^ 3 x] < [([8 / 27]) ^ -x + 2]"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[([2 / 3]) ^ 3 x] < [([2 / 3]) ^ 3 (-x + 2)]"
                }

                step {
                    toExpr = "SetSolution[x: (1, /infinity/)]"
                    explanation {
                        key = InequalitiesExplanation.SimplifyExponentialEquationWithSameBasesAndSolve
                    }
                }
            }
        }
    }

    @Test
    fun `exponential inequality solvable with logarithm with base greater than one`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[2 ^ x] - 3 > 0"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[2 ^ x] > 3"
                }

                step {
                    toExpr = "x * ln[2] > ln[3]"
                }

                step {
                    toExpr = "SetSolution[x: ([ln[3] / ln[2]], /infinity/)]"
                    explanation {
                        key = InequalitiesExplanation.SolveLinearInequality
                    }
                }
            }
        }
    }

    @Test
    fun `exponential inequality solvable with logarithm with base in (0,1)`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[([2 / 3]) ^ x] - 3 > 0"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[([2 / 3]) ^ x] > 3"
                }

                step {
                    toExpr = "x * ln[[2 / 3]] > ln[3]"
                }

                step {
                    toExpr = "SetSolution[x: (-/infinity/, [ln[3] / ln[[2 / 3]]])]"
                    explanation {
                        key = InequalitiesExplanation.SolveLinearInequality
                    }
                }
            }
        }
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[([1 / 2]) ^ x] - 3 > 0"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[([1 / 2]) ^ x] > 3"
                }

                step {
                    toExpr = "-x * ln[2] > ln[3]"
                }

                step {
                    explanation {
                        key = InequalitiesExplanation.SolveLinearInequality
                    }

                    step {
                        toExpr = "x < -[ln[3] / ln[2]]"
                    }

                    step {
                        toExpr = "SetSolution[x: (-/infinity/, -[ln[3] / ln[2]])]"
                    }
                }
            }
        }
    }

    @Test
    fun `exponential inequality with coefficients`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "4 * [2 ^ x] < [1 / 4]"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[2 ^ x] < [1 / 16]"
                }

                step {
                    toExpr = "[2 ^ x] < [2 ^ -4]"
                }

                step {
                    toExpr = "SetSolution[x: (-/infinity/, -4)]"
                    explanation {
                        key = InequalitiesExplanation.SimplifyExponentialEquationWithSameBasesAndSolve
                    }
                }
            }
        }
    }

    @Test
    fun `exponential inequality with two exponentials with same exponents`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[3 ^ 2 x + 2] > [5 ^ 2 x + 2]"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[([3 / 5]) ^ 2 x + 2] > 1"
                    explanation {
                        key = InequalitiesExplanation.DivideInequalityByRhsAndSimplify
                    }
                }

                step {
                    toExpr = "[([3 / 5]) ^ 2 x + 2] > [([3 / 5]) ^ 0]"
                }

                step {
                    explanation {
                        key = InequalitiesExplanation.SimplifyExponentialEquationWithSameBasesAndSolve
                    }

                    step {
                        toExpr = "2 x + 2 < 0"
                    }

                    step {
                        toExpr = "SetSolution[x: (-/infinity/, -1)]"
                        explanation {
                            key = InequalitiesExplanation.SolveLinearInequality
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `exponential inequality with two exponentials with same bases`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[3 ^ 2 x + 2] > [3 ^ x - 1]"

            check {
                explanation {
                    key = InequalitiesExplanation.SimplifyExponentialEquationWithSameBasesAndSolve
                }

                step {
                    toExpr = "2 x + 2 > x - 1"
                }

                step {
                    toExpr = "SetSolution[x: (-3, /infinity/)]"
                    explanation {
                        key = InequalitiesExplanation.SolveLinearInequality
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[([2 / 5]) ^ 2 x + 2] > [([2 / 5]) ^ x - 1]"

            check {
                explanation {
                    key = InequalitiesExplanation.SimplifyExponentialEquationWithSameBasesAndSolve
                }

                step {
                    toExpr = "2 x + 2 < x - 1"
                }

                step {
                    toExpr = "SetSolution[x: (-/infinity/, -3)]"
                    explanation {
                        key = InequalitiesExplanation.SolveLinearInequality
                    }
                }
            }
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `exponential inequality with two exponentials - general case`() {
        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[2 ^ x] - [3 ^ 2 x + 1] > 0"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[2 ^ x] > [3 ^ 2 x + 1]"
                }

                step {
                    toExpr = "x * ln[2] > (2 x + 1) ln[3]"
                }

                step {
                    explanation {
                        key = InequalitiesExplanation.SolveLinearInequality
                    }

                    step {
                        toExpr = "x * ln[2] > 2 x * ln[3] + ln[3]"
                    }

                    step {
                        toExpr = "x (ln[2] - 2 * ln[3]) > ln[3]"
                    }

                    step {
                        toExpr = "x < [ln[3] / ln[2] - 2 * ln[3]]"
                        explanation {
                            key = InequalitiesExplanation.DetermineCoefficientSignAndDivide
                        }

                        task {
                            taskId = "#1"
                            startExpr = "ln[2] - 2 * ln[3] < 0"
                            explanation {
                                key = InequalitiesExplanation.DetermineSignOfCoefficient
                            }

                            step {
                                toExpr = "ln[[2 / 9]] < 0"
                            }

                            step {
                                toExpr = "Identity[ln[[2 / 9]] < 0]"
                            }
                        }

                        task {
                            taskId = "#2"
                            startExpr = "x (ln[2] - 2 * ln[3]) > ln[3]"

                            step {
                                toExpr = "x < [ln[3] / ln[2] - 2 * ln[3]]"
                            }
                        }
                    }

                    step {
                        toExpr = "SetSolution[x: (-/infinity/, [ln[3] / ln[2] - 2 * ln[3]])]"
                        explanation {
                            key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                        }
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[7 ^ x] - [2 ^ 2 x + 1] > 0"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[7 ^ x] > [2 ^ 2 x + 1]"
                }

                step {
                    toExpr = "x * ln[7] > (2 x + 1) ln[2]"
                }

                step {
                    explanation {
                        key = InequalitiesExplanation.SolveLinearInequality
                    }

                    step {
                        toExpr = "x * ln[7] > 2 x * ln[2] + ln[2]"
                    }

                    step {
                        toExpr = "x (ln[7] - 2 * ln[2]) > ln[2]"
                    }

                    step {
                        toExpr = "x > [ln[2] / ln[7] - 2 * ln[2]]"
                        explanation {
                            key = InequalitiesExplanation.DetermineCoefficientSignAndDivide
                        }

                        task {
                            taskId = "#1"
                            startExpr = "ln[7] - 2 * ln [2] > 0"
                            explanation {
                                key = InequalitiesExplanation.DetermineSignOfCoefficient
                            }

                            step {
                                toExpr = "ln[[7 / 4]] > 0"
                            }

                            step {
                                toExpr = "Identity[ln[[7 / 4]] > 0]"
                            }
                        }

                        task {
                            taskId = "#2"
                            startExpr = "x (ln[7] - 2 * ln[2]) > ln[2]"

                            step {
                                toExpr = "x > [ln[2] / ln[7] - 2 * ln[2]]"
                            }
                        }
                    }

                    step {
                        toExpr = "SetSolution[x: ([ln[2] / ln[7] - 2 * ln[2]], /infinity/)]"
                        explanation {
                            key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                        }
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveExponentialInequality
            inputExpr = "[3 ^ 2 x + 2] - [4 ^ x + 1] < 0"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveExponentialInequality
                }

                step {
                    toExpr = "[3 ^ 2 x + 2] < [4 ^ x + 1]"
                }

                step {
                    toExpr = "(2 x + 2) ln[3] < 2 (x + 1) ln[2]"
                }

                step {
                    toExpr = "SetSolution[x: (-/infinity/, -1)]"
                    explanation {
                        key = InequalitiesExplanation.SolveLinearInequality
                    }
                }
            }
        }
    }
}
