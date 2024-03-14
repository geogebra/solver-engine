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

import engine.context.Preset
import engine.methods.testMethodInX
import methods.equations.EquationsExplanation
import org.junit.jupiter.api.Test

@Suppress("LargeClass", "MaxLineLength", "ktlint:standard:max-line-length")
class QuadraticInequalitiesTestUS {
    // US Content

    @Test
    fun `test two solutions greater than zero US`() =
        testMethodInX(Preset.USCurriculum) {
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

                    step {
                        fromExpr = "[x ^ 2] - 3 x + 2 = 0"
                        toExpr = "SetSolution[x: {1, 2}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationByFactoring
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[0 ^ 2] - 3 * 0 + 2 > 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] - 3 * 0 + 2 > 0"
                        toExpr = "2 > 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "2 > 0"
                        toExpr = "Identity[2 > 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "[1.5 ^ 2] - 3 * 1.5 + 2 > 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[1.5 ^ 2] - 3 * 1.5 + 2 > 0"
                        toExpr = "-[1 / 4] > 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "-[1 / 4] > 0"
                        toExpr = "Contradiction[-[1 / 4] > 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#4"
                    startExpr = "[3 ^ 2] - 3 * 3 + 2 > 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[3 ^ 2] - 3 * 3 + 2 > 0"
                        toExpr = "2 > 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "2 > 0"
                        toExpr = "Identity[2 > 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#5"
                    startExpr = "SetSolution[x: SetUnion[(-/infinity/, 1), (2, /infinity/)]]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test one solution greater than zero US`() =
        testMethodInX(Preset.USCurriculum) {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - 2 x + 1 > 0"

            check {
                fromExpr = "[x ^ 2] - 2 x + 1 > 0"
                toExpr = "SetSolution[x: SetUnion[(-/infinity/, 1), (1, /infinity/)]]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - 2 x + 1 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }

                    step {
                        fromExpr = "[x ^ 2] - 2 x + 1 = 0"
                        toExpr = "SetSolution[x: {1}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationByFactoring
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[0 ^ 2] - 2 * 0 + 1 > 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] - 2 * 0 + 1 > 0"
                        toExpr = "1 > 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "1 > 0"
                        toExpr = "Identity[1 > 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "[2 ^ 2] - 2 * 2 + 1 > 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[2 ^ 2] - 2 * 2 + 1 > 0"
                        toExpr = "1 > 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "1 > 0"
                        toExpr = "Identity[1 > 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#4"
                    startExpr = "SetSolution[x: SetUnion[(-/infinity/, 1), (1, /infinity/)]]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test no solution greater than zero US`() =
        testMethodInX(Preset.USCurriculum) {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] + 1 > 0"

            check {
                fromExpr = "[x ^ 2] + 1 > 0"
                toExpr = "SetSolution[x: (-/infinity/, /infinity/)]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] + 1 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
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
                    taskId = "#2"
                    startExpr = "[0 ^ 2] + 1 > 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] + 1 > 0"
                        toExpr = "1 > 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "1 > 0"
                        toExpr = "Identity[1 > 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: (-/infinity/, /infinity/)]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test two solutions greater than or equal to zero US`() =
        testMethodInX(Preset.USCurriculum) {
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

                    step {
                        fromExpr = "[x ^ 2] + x - 3 = 0"
                        toExpr = "SetSolution[x: {-[1 + sqrt[13] / 2], [-1 + sqrt[13] / 2]}]"
                        explanation {
                            key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[(-3) ^ 2] - 3 - 3 >= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[(-3) ^ 2] - 3 - 3 >= 0"
                        toExpr = "3 >= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "3 >= 0"
                        toExpr = "Identity[3 >= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "[0 ^ 2] + 0 - 3 >= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] + 0 - 3 >= 0"
                        toExpr = "-3 >= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "-3 >= 0"
                        toExpr = "Contradiction[-3 >= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#4"
                    startExpr = "[2 ^ 2] + 2 - 3 >= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[2 ^ 2] + 2 - 3 >= 0"
                        toExpr = "3 >= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "3 >= 0"
                        toExpr = "Identity[3 >= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#5"
                    startExpr = "SetSolution[x: SetUnion[(-/infinity/, -[1 + sqrt[13] / 2]], [[-1 + sqrt[13] / 2], /infinity/)]]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test one solution greater than or equal to zero US`() =
        testMethodInX(Preset.USCurriculum) {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] + 4 x + 4 >= 0"

            check {
                fromExpr = "[x ^ 2] + 4 x + 4 >= 0"
                toExpr = "SetSolution[x: (-/infinity/, /infinity/)]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] + 4 x + 4 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }

                    step {
                        fromExpr = "[x ^ 2] + 4 x + 4 = 0"
                        toExpr = "SetSolution[x: {-2}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationByFactoring
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[0 ^ 2] + 4 * 0 + 4 >= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] + 4 * 0 + 4 >= 0"
                        toExpr = "4 >= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "4 >= 0"
                        toExpr = "Identity[4 >= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: (-/infinity/, /infinity/)]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test no solution for greater than or equal to zero US`() =
        testMethodInX(Preset.USCurriculum) {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - x + 10 >= 0"

            check {
                fromExpr = "[x ^ 2] - x + 10 >= 0"
                toExpr = "SetSolution[x: (-/infinity/, /infinity/)]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - x + 10 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }

                    step {
                        fromExpr = "[x ^ 2] - x + 10 = 0"
                        toExpr = "Contradiction[x: x = [1 +/- sqrt[-39] / 2]]"
                        explanation {
                            key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[0 ^ 2] - 0 + 10 >= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] - 0 + 10 >= 0"
                        toExpr = "10 >= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "10 >= 0"
                        toExpr = "Identity[10 >= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: (-/infinity/, /infinity/)]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }
    // Arnaud ^ Abel v

    @Test
    fun `test two solutions for less than zero US`() =
        testMethodInX(Preset.USCurriculum) {
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

                    step {
                        fromExpr = "[x ^ 2] + 2 x - 4 = 0"
                        toExpr = "SetSolution[x: {-1 - sqrt[5], -1 + sqrt[5]}]"
                        explanation {
                            key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[(-4) ^ 2] + 2 * (-4) - 4 < 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[(-4) ^ 2] + 2 * (-4) - 4 < 0"
                        toExpr = "4 < 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "4 < 0"
                        toExpr = "Contradiction[4 < 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "[0 ^ 2] + 2 * 0 - 4 < 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] + 2 * 0 - 4 < 0"
                        toExpr = "-4 < 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "-4 < 0"
                        toExpr = "Identity[-4 < 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#4"
                    startExpr = "[2 ^ 2] + 2 * 2 - 4 < 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[2 ^ 2] + 2 * 2 - 4 < 0"
                        toExpr = "4 < 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "4 < 0"
                        toExpr = "Contradiction[4 < 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#5"
                    startExpr = "SetSolution[x: (-1 - sqrt[5], -1 + sqrt[5])]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test one solution for less than zero US`() =
        testMethodInX(Preset.USCurriculum) {
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

                    step {
                        fromExpr = "[x ^ 2] - 2 x + 1 = 0"
                        toExpr = "SetSolution[x: {1}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationByFactoring
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[0 ^ 2] - 2 * 0 + 1 < 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] - 2 * 0 + 1 < 0"
                        toExpr = "1 < 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
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
                    startExpr = "[2 ^ 2] - 2 * 2 + 1 < 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[2 ^ 2] - 2 * 2 + 1 < 0"
                        toExpr = "1 < 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
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
                    startExpr = "Contradiction[x: [x ^ 2] - 2 x + 1 < 0]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test no solution for less than zero US`() =
        testMethodInX(Preset.USCurriculum) {
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

                    step {
                        fromExpr = "[x ^ 2] - 2 x + 10 = 0"
                        toExpr = "Contradiction[x: x = [2 +/- sqrt[-36] / 2]]"
                        explanation {
                            key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[0 ^ 2] - 2 * 0 + 10 < 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] - 2 * 0 + 10 < 0"
                        toExpr = "10 < 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "10 < 0"
                        toExpr = "Contradiction[10 < 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "Contradiction[x: [x ^ 2] - 2 x + 10 < 0]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test two solutions for less than or equal to zero US`() =
        testMethodInX(Preset.USCurriculum) {
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

                    step {
                        fromExpr = "[x ^ 2] - x - 1 = 0"
                        toExpr = "SetSolution[x: {[1 - sqrt[5] / 2], [1 + sqrt[5] / 2]}]"
                        explanation {
                            key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[(-1) ^ 2] - (-1) - 1 <= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[(-1) ^ 2] - (-1) - 1 <= 0"
                        toExpr = "1 <= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "1 <= 0"
                        toExpr = "Contradiction[1 <= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "[0 ^ 2] - 0 - 1 <= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] - 0 - 1 <= 0"
                        toExpr = "-1 <= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "-1 <= 0"
                        toExpr = "Identity[-1 <= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#4"
                    startExpr = "[2 ^ 2] - 2 - 1 <= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[2 ^ 2] - 2 - 1 <= 0"
                        toExpr = "1 <= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "1 <= 0"
                        toExpr = "Contradiction[1 <= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#5"
                    startExpr = "SetSolution[x: [[1 - sqrt[5] / 2], [1 + sqrt[5] / 2]]]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test one solution for less than or equal zero US`() =
        testMethodInX(Preset.USCurriculum) {
            method = InequalitiesPlans.SolveQuadraticInequality
            inputExpr = "[x ^ 2] - 2 x + 1 <= 0"

            check {
                fromExpr = "[x ^ 2] - 2 x + 1 <= 0"
                toExpr = "Contradiction[x: [x ^ 2] - 2 x + 1 <= 0]"
                explanation {
                    key = InequalitiesExplanation.SolveQuadraticInequalityInCanonicalForm
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - 2 x + 1 = 0"
                    explanation {
                        key = InequalitiesExplanation.SolveCorrespondingQuadraticEquation
                    }

                    step {
                        fromExpr = "[x ^ 2] - 2 x + 1 = 0"
                        toExpr = "SetSolution[x: {1}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationByFactoring
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[0 ^ 2] - 2 * 0 + 1 <= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] - 2 * 0 + 1 <= 0"
                        toExpr = "1 <= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "1 <= 0"
                        toExpr = "Contradiction[1 <= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "Contradiction[x: [x ^ 2] - 2 x + 1 <= 0]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }

    @Test
    fun `test no solution for less than or equal to zero US`() =
        testMethodInX(Preset.USCurriculum) {
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

                    step {
                        fromExpr = "[x ^ 2] + 2 = 0"
                        toExpr = "Contradiction[x: [x ^ 2] = -2]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUsingRootsMethod
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[0 ^ 2] + 2 <= 0"
                    explanation {
                        key = InequalitiesExplanation.CheckSolutionIntervalUsingTestPoint
                    }

                    step {
                        fromExpr = "[0 ^ 2] + 2 <= 0"
                        toExpr = "2 <= 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "2 <= 0"
                        toExpr = "Contradiction[2 <= 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "Contradiction[x: [x ^ 2] + 2 <= 0]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }
}
