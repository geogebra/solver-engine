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

package methods.inequations

import engine.context.Context
import engine.methods.testMethod
import engine.methods.testMethodInX
import methods.equations.EquationsExplanation
import org.junit.jupiter.api.Test

class InequationsTest {
    @Test
    fun `test solving simple inequation`() =
        testMethodInX {
            method = InequationsPlans.SolveInequation
            inputExpr = "3 x - 1 != 2"

            check {
                fromExpr = "3 x - 1 != 2"
                toExpr = "SetSolution[x : /reals/ \\ {1}]"
                explanation {
                    key = InequationsExplanation.SolveInequationInOneVariable
                }

                step {
                    fromExpr = "3 x - 1 != 2"
                    toExpr = "3 x != 3"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.MoveConstantsToTheRightAndSimplify
                    }
                }

                step {
                    fromExpr = "3 x != 3"
                    toExpr = "x != 1"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                    }
                }

                step {
                    fromExpr = "x != 1"
                    toExpr = "SetSolution[x : /reals/ \\ {1}]"
                    explanation {
                        key = InequationsExplanation.ExtractSolutionFromInequationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test solving complex inequation`() =
        testMethodInX {
            method = InequationsPlans.SolveInequation
            inputExpr = "(2 x + 3) ([x ^ 2] + 3 x + 2) != 0"

            check {
                fromExpr = "(2 x + 3) ([x ^ 2] + 3 x + 2) != 0"
                toExpr = "SetSolution[x : /reals/ \\ {-2, -[3 / 2], -1}]"
                explanation {
                    key = InequationsExplanation.SolveInequationInOneVariable
                }

                task {
                    taskId = "#1"
                    startExpr = "(2 x + 3) ([x ^ 2] + 3 x + 2) = 0"
                    explanation {
                        key = InequationsExplanation.SolveEquationCorrespondingToInequation
                    }

                    step {
                        fromExpr = "(2 x + 3) ([x ^ 2] + 3 x + 2) = 0"
                        toExpr = "SetSolution[x : {-2, -[3 / 2], -1}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationByFactoring
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "SetSolution[x : /reals/ \\ {-2, -[3 / 2], -1}]"
                    explanation {
                        key = InequationsExplanation.TakeComplementOfSolution
                    }
                }
            }
        }

    @Test
    fun `test reducing multivariate inequation`() =
        testMethod {
            method = InequationsPlans.SolveInequation
            context = Context(solutionVariables = listOf("a", "b"))
            inputExpr = "-3 a [b ^ 2] != 0"

            check {
                fromExpr = "-3 a [b ^ 2] != 0"
                toExpr = "a [b ^ 2] != 0"
                explanation {
                    key = EquationsExplanation.EliminateConstantFactorOfLhsWithZeroRhs
                }
            }
        }
}
