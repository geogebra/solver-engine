/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
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
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength", "ktlint:standard:max-line-length")
class QuadraticInequalitiesTest {
    @Test
    fun `test two solutions greater than zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - 3 x + 2 > 0"

            check {
                fromExpr = "[x ^ 2] - 3 x + 2 > 0"
                toExpr = "SetSolution[x: SetUnion[(-/infinity/, 1), (2, /infinity/)]]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - 3 x + 2 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "SetSolution[x: SetUnion[(-/infinity/, 1), (2, /infinity/)]]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForGreaterThanAndTwoSolutions
                    }
                }
            }
        }

    @Test
    fun `test one solution greater than zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - 2 x + 1 > 0"

            check {
                fromExpr = "[x ^ 2] - 2 x + 1 > 0"
                toExpr = "SetSolution[x: /reals/ \\ {1}]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - 2 x + 1 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "SetSolution[x: /reals/ \\ {1}]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForGreaterThanAndOneSolution
                    }
                }
            }
        }

    @Test
    fun `test no solution greater than zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] + 1 > 0"

            check {
                fromExpr = "[x ^ 2] + 1 > 0"
                toExpr = "Identity[x: [x ^ 2] + 1 > 0]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] + 1 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "Identity[x: [x ^ 2] + 1 > 0]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForGreaterThanAndNoSolution
                    }
                }
            }
        }

    @Test
    fun `test two solutions greater than or equal to zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] + x - 3 >= 0"

            check {
                fromExpr = "[x ^ 2] + x - 3 >= 0"
                toExpr = "SetSolution[x: SetUnion[(-/infinity/, -[1 + sqrt[13] / 2]], [[-1 + sqrt[13] / 2], /infinity/)]]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] + x - 3 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                @Suppress("ktlint:standard:max-line-length")
                task {
                    taskId = "#2"
                    startExpr = "SetSolution[x: SetUnion[(-/infinity/, -[1 + sqrt[13] / 2]], [[-1 + sqrt[13] / 2], /infinity/)]]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForGreaterThanOrEqualAndTwoSolutions
                    }
                }
            }
        }

    @Test
    fun `test one solution greater than or equal to zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] + 4 x + 4 >= 0"

            check {
                fromExpr = "[x ^ 2] + 4 x + 4 >= 0"
                toExpr = "Identity[x: [x ^ 2] + 4 x + 4 >= 0]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] + 4 x + 4 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "Identity[x: [x ^ 2] + 4 x + 4 >= 0]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForGreaterThanOrEqualAndOneSolution
                    }
                }
            }
        }

    @Test
    fun `test no solution for greater than or equal to zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - x + 10 >= 0"

            check {
                fromExpr = "[x ^ 2] - x + 10 >= 0"
                toExpr = "Identity[x: [x ^ 2] - x + 10 >= 0]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - x + 10 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "Identity[x: [x ^ 2] - x + 10 >= 0]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForGreaterThanOrEqualAndNoSolution
                    }
                }
            }
        }
    // Arnaud ^ Abel v

    @Test
    fun `test two solutions for less than zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] + 2 x - 4 < 0"

            check {
                fromExpr = "[x ^ 2] + 2 x - 4 < 0"
                toExpr = "SetSolution[x: (-1 - sqrt[5], -1 + sqrt[5])]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] + 2 x - 4 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "SetSolution[x: (-1 - sqrt[5], -1 + sqrt[5])]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForLessThanAndTwoSolutions
                    }
                }
            }
        }

    @Test
    fun `test one solution for less than zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - 2 x + 1 < 0"

            check {
                fromExpr = "[x ^ 2] - 2 x + 1 < 0"
                toExpr = "Contradiction[x: [x ^ 2] - 2 x + 1 < 0]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - 2 x + 1 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "Contradiction[x: [x ^ 2] - 2 x + 1 < 0]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForLessThanAndOneSolution
                    }
                }
            }
        }

    @Test
    fun `test no solution for less than zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - 2 x + 10 < 0"

            check {
                fromExpr = "[x ^ 2] - 2 x + 10 < 0"
                toExpr = "Contradiction[x: [x ^ 2] - 2 x + 10 < 0]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - 2 x + 10 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "Contradiction[x: [x ^ 2] - 2 x + 10 < 0]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForLessThanAndNoSolution
                    }
                }
            }
        }

    @Test
    fun `test two solutions for less than or equal to zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - x - 1 <= 0"

            check {
                fromExpr = "[x ^ 2] - x - 1 <= 0"
                toExpr = "SetSolution[x: [[1 - sqrt[5] / 2], [1 + sqrt[5] / 2]]]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - x - 1 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "SetSolution[x: [[1 - sqrt[5] / 2], [1 + sqrt[5] / 2]]]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForLessThanOrEqualAndTwoSolutions
                    }
                }
            }
        }

    @Test
    fun `test one solution for less than or equal zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - 2 x + 1 <= 0"

            check {
                fromExpr = "[x ^ 2] - 2 x + 1 <= 0"
                toExpr = "SetSolution[x: {1}]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - 2 x + 1 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "SetSolution[x: {1}]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForLessThanOrEqualAndOneSolution
                    }
                }
            }
        }

    @Test
    fun `test no solution for less than or equal to zero`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] + 2 <= 0"

            check {
                fromExpr = "[x ^ 2] + 2 <= 0"
                toExpr = "Contradiction[x: [x ^ 2] + 2 <= 0]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] + 2 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "Contradiction[x: [x ^ 2] + 2 <= 0]"
                    explanation {
                        key = InequalitiesExplanation.DeduceInequalitySolutionForLessThanOrEqualAndNoSolution
                    }
                }
            }
        }

    @Test
    fun `test rearranging inequality first`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "x > [x ^ 2]"

            check {
                fromExpr = "x > [x ^ 2]"
                toExpr = "SetSolution[x: (0, 1)]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequality
                }

                step {
                    fromExpr = "x > [x ^ 2]"
                    toExpr = "x - [x ^ 2] > 0"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "x - [x ^ 2] > 0"
                    toExpr = "-[x ^ 2] + x > 0"
                    explanation {
                        key = PolynomialsExplanation.NormalizePolynomial
                    }
                }

                step {
                    fromExpr = "-[x ^ 2] + x > 0"
                    toExpr = "[x ^ 2] - x < 0"
                    explanation {
                        key = InequalitiesExplanation.EnsureLeadCoefficientOfLHSIsPositive
                    }
                }

                step {
                    fromExpr = "[x ^ 2] - x < 0"
                    toExpr = "SetSolution[x: (0, 1)]"
                    explanation {
                        key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[x ^ 2] - x = 0"
                        explanation {
                            key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "SetSolution[x: (0, 1)]"
                        explanation {
                            key = InequalitiesExplanation.DeduceInequalitySolutionForLessThanAndTwoSolutions
                        }
                    }
                }
            }
        }

    @Test
    fun `test single quadratic term`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "0 < [x ^ 2]"

            check {
                fromExpr = "0 < [x ^ 2]"
                toExpr = "SetSolution[x: /reals/ \\ {0}]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequality
                }

                step {
                    fromExpr = "0 < [x ^ 2]"
                    toExpr = "-[x ^ 2] < 0"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "-[x ^ 2] < 0"
                    toExpr = "[x ^ 2] > 0"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.NegateBothSidesAndFlipTheSign
                    }
                }

                step {
                    fromExpr = "[x ^ 2] > 0"
                    toExpr = "SetSolution[x: /reals/ \\ {0}]"
                    explanation {
                        key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[x ^ 2] = 0"
                        explanation {
                            key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "SetSolution[x: /reals/ \\ {0}]"
                        explanation {
                            key = InequalitiesExplanation.DeduceInequalitySolutionForGreaterThanAndOneSolution
                        }
                    }
                }
            }
        }

    @Test
    fun `test inequality with brackets`() =
        testMethodInX {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "(x - 2) (x + 3) < x (1 - x)"

            check {
                fromExpr = "(x - 2) (x + 3) < x (1 - x)"
                toExpr = "SetSolution[x: (-sqrt[3], sqrt[3])]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequality
                }

                step {
                    fromExpr = "(x - 2) (x + 3) < x (1 - x)"
                    toExpr = "[x ^ 2] + x - 6 < -[x ^ 2] + x"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + x - 6 < -[x ^ 2] + x"
                    toExpr = "[x ^ 2] - 6 < -[x ^ 2]"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.CancelCommonTermsOnBothSides
                    }
                }

                step {
                    fromExpr = "[x ^ 2] - 6 < -[x ^ 2]"
                    toExpr = "2 [x ^ 2] - 6 < 0"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "2 [x ^ 2] - 6 < 0"
                    toExpr = "SetSolution[x: (-sqrt[3], sqrt[3])]"
                    explanation {
                        key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                    }

                    task {
                        taskId = "#1"
                        startExpr = "2 [x ^ 2] - 6 = 0"
                        explanation {
                            key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "SetSolution[x: (-sqrt[3], sqrt[3])]"
                        explanation {
                            key = InequalitiesExplanation.DeduceInequalitySolutionForLessThanAndTwoSolutions
                        }
                    }
                }
            }
        }
}
