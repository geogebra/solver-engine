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

package methods.equations

import engine.methods.testMethodInX
import methods.algebra.AlgebraExplanation
import methods.logs.LogsExplanation
import kotlin.test.Test

@Suppress("LargeClass")
class LogarithmicEquationsTest {
    // Right now these are only for the normalization part, but should be updated once
    // we have solutions implemented.
    @Suppress("LongMethod")
    @Test
    fun `solve equation with one log term`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 = -log[x + 1]"

            check {
                task {
                    startExpr = "2 = -log[x + 1]"
                }

                task {
                    startExpr = "2 = -log[x + 1]"

                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "2 + log[x + 1] = 0"
                        }

                        step {
                            toExpr = "log[x + 1] = -2"
                        }

                        step {
                            toExpr = "[10 ^ log[x + 1]] = [10 ^ -2]"
                        }

                        step {
                            toExpr = "x + 1 = [10 ^ -2]"
                        }

                        step {
                            toExpr = "SetSolution[x : {-[99 / 100]}]"
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x : {-[99 / 100]}] GIVEN SetSolution[x: (-1, /infinity/)]"
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "log_[2][2 x - 1] = 1"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: ([1 / 2], /infinity/)]"
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "[2 ^ log_[2][2 x - 1]] = [2 ^ 1]"
                            explanation {
                                key = LogsExplanation.ExponentiateBothSides
                            }
                        }

                        step {
                            toExpr = "2 x - 1 = [2 ^ 1]"
                            explanation {
                                key = LogsExplanation.SimplifyLogInExponentWithMatchingBase
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: {[3 / 2]}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: {[3 / 2]}] GIVEN SetSolution[x: ([1 / 2], /infinity/)]"
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "ln[x - 2] = 3"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: (2, /infinity/)]"
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "[/e/ ^ ln[x - 2]] = [/e/ ^ 3]"
                            explanation {
                                key = LogsExplanation.ExponentiateBothSides
                            }
                        }

                        step {
                            toExpr = "x - 2 = [/e/ ^ 3]"
                            explanation {
                                key = LogsExplanation.SimplifyLogInExponentWithMatchingBase
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: {[/e/ ^ 3] + 2}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: {[/e/ ^ 3] + 2}] GIVEN SetSolution[x: (2, /infinity/)]"
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "log[x + 7] - 5 = 0"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: (-7, /infinity/)]"
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "log[x + 7] = 5"
                        }

                        step {
                            toExpr = "[10 ^ log[x + 7]] = [10 ^ 5]"
                            explanation {
                                key = LogsExplanation.ExponentiateBothSides
                            }
                        }

                        step {
                            toExpr = "x + 7 = [10 ^ 5]"
                            explanation {
                                key = LogsExplanation.SimplifyLogInExponentWithMatchingBase
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: {99993}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: {99993}] GIVEN SetSolution[x: (-7, /infinity/)]"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }

                    step {
                        toExpr = "SetSolution[x: {99993}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `solve equation with multiple logs with same argument and base`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 + 3 * log[x + 1] + 5 * log[x + 1] + 4 = 3 - log[x + 1] + 2 * log[x + 1]"

            check {
                task {
                    startExpr = "2 + 3 * log[x + 1] + 5 * log[x + 1] + 4 = 3 - log[x + 1] + 2 * log[x + 1]"

                    step {
                        toExpr = "SetSolution[x: (-1, /infinity/)]"
                    }
                }

                task {
                    startExpr = "2 + 3 * log[x + 1] + 5 * log[x + 1] + 4 = 3 - log[x + 1] + 2 * log[x + 1]"

                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "6 + 8 * log[x + 1] = 3 + log[x + 1]"
                        }

                        step {
                            toExpr = "6 + 7 * log[x + 1] = 3"
                        }

                        step {
                            toExpr = "7 * log[x + 1] = -3"
                        }

                        step {
                            toExpr = "log[x + 1] = -[3 / 7]"
                        }

                        step {
                            toExpr = "[10 ^ log[x + 1]] = [10 ^ -[3 / 7]]"
                        }

                        step {
                            toExpr = "x + 1 = [10 ^ -[3 / 7]]"
                        }

                        step {
                            toExpr = "SetSolution[x : {[10 ^ -[3 / 7]] - 1}]"
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x : {[10 ^ -[3 / 7]] - 1}] GIVEN SetSolution[x: (-1, /infinity/)]"
                }
            }
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `move negative constant terms and express as logs`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "log_[2][x] = log_[2][7] - 2"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: (0, /infinity/)]"
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "log_[2][x] + 2 = log_[2][7]"
                            explanation {
                                key = LogsExplanation.MoveNegatedConstantTermsToTheOppositeSideAndSimplify
                            }
                        }

                        step {
                            toExpr = "log_[2][x] + log_[2][4] = log_[2][7]"
                            explanation {
                                key = LogsExplanation.ExpressConstantInSumAsLogAndSimplify
                            }
                        }

                        step {
                            toExpr = "log_[2][4 x] = log_[2][7]"
                            explanation {
                                key = LogsExplanation.CollectLogarithmsInSum
                            }
                        }

                        step {
                            toExpr = "4 x = 7"
                            explanation {
                                key = LogsExplanation.ApplyEqualityRuleOfLogs
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: {[7 / 4]}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }
                }

                task {
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }

                    step {
                        fromExpr = "SetSolution[x: {[7 / 4]}] GIVEN SetSolution[x: (0, /infinity/)]"
                        toExpr = "SetSolution[x: {[7 / 4]}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "log[x + 1] - log[x - 2] - 3 = 0"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: (2, /infinity/)]"
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "log[x + 1] - 3 = log[x - 2]"
                            explanation {
                                key = LogsExplanation.MoveNegatedLogarithmicTermsToOppositeSideAndSimplify
                            }
                        }

                        step {
                            toExpr = "log[x + 1] = log[x - 2] + 3"
                            explanation {
                                key = LogsExplanation.MoveNegatedConstantTermsToTheOppositeSideAndSimplify
                            }
                        }

                        step {
                            toExpr = "log[x + 1] = log[x - 2] + log[1000]"
                            explanation {
                                key = LogsExplanation.ExpressConstantInSumAsLogAndSimplify
                            }
                        }

                        step {
                            toExpr = "log[x + 1] = log[1000 (x - 2)]"
                            explanation {
                                key = LogsExplanation.CollectLogarithmsInSum
                            }
                        }

                        step {
                            toExpr = "x + 1 = 1000 (x - 2)"
                            explanation {
                                key = LogsExplanation.ApplyEqualityRuleOfLogs
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: {[667 / 333]}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }
                }

                task {
                    step {
                        toExpr = "SetSolution[x: {[667 / 333]}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `solve equation with multiple logs with different arguments`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "-2 * log_[2][x + 1] + log_[4][2 x] = log_[2][x + 1] + log_[2][5] - log_[2][x - 2]"

            check {
                task {
                    startExpr = "-2 * log_[2][x + 1] + log_[4][2 x] = log_[2][x + 1] + log_[2][5] - log_[2][x - 2]"

                    step {
                        toExpr = "SetSolution[x: (2, /infinity/)]"

                        task {
                            startExpr = "x + 1 > 0"
                            explanation {
                                key = AlgebraExplanation.LogArgumentMustBePositive
                            }

                            step {
                                fromExpr = "x + 1 > 0"
                                toExpr = "SetSolution[x: (-1, /infinity/)]"
                            }
                        }

                        task {
                            startExpr = "2 x > 0"
                            explanation {
                                key = AlgebraExplanation.LogArgumentMustBePositive
                            }

                            step {
                                fromExpr = "2 x > 0"
                                toExpr = "SetSolution[x: (0, /infinity/)]"
                            }
                        }

                        task {
                            taskId = "#3"
                            startExpr = "x - 2 > 0"
                            explanation {
                                key = AlgebraExplanation.LogArgumentMustBePositive
                            }

                            step {
                                fromExpr = "x - 2 > 0"
                                toExpr = "SetSolution[x: (2, /infinity/)]"
                            }
                        }

                        task {
                            startExpr = "SetSolution[x: (2, /infinity/)]"
                            explanation {
                                key = AlgebraExplanation.CollectDomainRestrictions
                            }
                        }
                    }
                }

                task {
                    startExpr = "-2 * log_[2][x + 1] + log_[4][2 x] = log_[2][x + 1] + log_[2][5] - log_[2][x - 2]"

                    step {
                        step {
                            toExpr =
                                "-2 * log_[2][x + 1] + [log_[2][2 x] / 2] =" +
                                " log_[2][x + 1] + log_[2][5] - log_[2][x - 2]"
                            explanation {
                                key = LogsExplanation.BringLogsToCommonBase
                            }
                        }

                        step {
                            toExpr =
                                "[log_[2][2 x] / 2] + log_[2][x - 2] " +
                                "= 3 * log_[2][x + 1] + log_[2][5]"
                            explanation {
                                key = LogsExplanation.MoveNegatedLogarithmicTermsToOppositeSideAndSimplify
                            }
                        }

                        step {
                            toExpr = "log_[2][2 x] + 2 * log_[2][x - 2] = 6 * log_[2][x + 1] + 2 * log_[2][5]"
                        }

                        step {
                            toExpr = "log_[2][2 x * [(x - 2) ^ 2]] = 6 * log_[2][x + 1] + 2 * log_[2][5]"
                            explanation {
                                key = LogsExplanation.CollectLogarithmsInSum
                            }
                        }

                        step {
                            toExpr = "log_[2][2 x * [(x - 2) ^ 2]] = log_[2][25 * [(x + 1) ^ 6]]"
                            explanation {
                                key = LogsExplanation.CollectLogarithmsInSum
                            }
                        }

                        step {
                            toExpr = "2 x * [(x - 2) ^ 2] = 25 * [(x + 1) ^ 6]"
                            explanation {
                                key = LogsExplanation.ApplyEqualityRuleOfLogs
                            }
                        }

                        step {
                            toExpr = "2 [x^3] - 8 [x^2] + 8x - 25 * [(x + 1) ^ 6] = 0"
                        }
                    }
                }

                task {
                    startExpr =
                        "2 [x^3] - 8 [x^2] + 8x - 25 * [(x + 1) ^ 6] = 0 " +
                        "GIVEN SetSolution[x: (2, /infinity/)]"
                }
            }
        }
    }

    @Test
    fun `solve already balanced equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "3 * log[x + 1] = log[[x ^ 3] + 3 x + 7]"

            check {
                task {
                    step {
                        toExpr = "SetSolution[x: (-1, /infinity/)] AND [x ^ 3] + 3 x + 7 > 0"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfEquation
                        }
                    }
                }

                task {
                    step {
                        step {
                            toExpr = "log[[(x + 1) ^ 3]] = log[[x ^ 3] + 3 x + 7]"
                            explanation {
                                key = LogsExplanation.RewriteCoefficientsAsExponents
                            }
                        }
                        step {
                            toExpr = "[(x + 1) ^ 3] = [x ^ 3] + 3 x + 7"
                        }
                        step {
                            toExpr = "SetSolution[x : {-sqrt[2], sqrt[2]}]"
                        }
                    }
                }

                task {
                    startExpr =
                        "SetSolution[x : {-sqrt[2], sqrt[2]}] " +
                        "GIVEN SetSolution[x: (-1, /infinity/)] AND [x ^ 3] + 3 x + 7 > 0"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }
                }
            }
        }
    }

    @Test
    fun `collects constant and non-constant log expressions`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[1 / 2] * log[9 - x] = log[3] + [1 / 2] * log[x]"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: (0, 9)]"
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "log[9 - x] = 2 * log[3] + log[x]"
                        }

                        step {
                            toExpr = "log[9 - x] = log[9 x]"
                            explanation {
                                key = LogsExplanation.CollectLogarithmsInSum
                            }
                        }

                        step {
                            toExpr = "9 - x = 9 x"
                        }

                        step {
                            toExpr = "SetSolution[x : {[9 / 10]}]"
                        }
                    }
                }

                task {
                    step {
                        toExpr = "SetSolution[x: {[9 / 10]}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `does not expand constant logarithms`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "log[x - 16] = log[105] - log[x]"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: (16, /infinity/)]"
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "log[x - 16] + log[x] = log[105]"
                        }

                        step {
                            toExpr = "log[x (x - 16)] = log[105]"
                        }

                        step {
                            toExpr = "x (x - 16) = 105"
                        }

                        step {
                            toExpr = "SetSolution[x : {-5, 21}]"
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x : {-5, 21}] GIVEN SetSolution[x: (16, /infinity/)]"

                    step {
                        toExpr = "SetSolution[x: {21}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `eliminate invalid solutions in eq with multiple constraints`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "3 * log[x + 1] = log[[x ^ 3] + 3 x + 7]"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: (-1, /infinity/)] AND [x ^ 3] + 3 x + 7 > 0"
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "log[[(x + 1) ^ 3]] = log[[x ^ 3] + 3 x + 7]"
                            explanation {
                                key = LogsExplanation.RewriteCoefficientsAsExponents
                            }
                        }

                        step {
                            toExpr = "[(x + 1) ^ 3] = [x ^ 3] + 3 x + 7"
                            explanation {
                                key = LogsExplanation.ApplyEqualityRuleOfLogs
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: {-sqrt[2], sqrt[2]}]"

                            explanation {
                                key = EquationsExplanation.SolveEquationUsingRootsMethod
                            }
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: {-sqrt[2], sqrt[2]}] GIVEN " +
                        "SetSolution[x: (-1, /infinity/)] AND [x ^ 3] + 3 x + 7 > 0"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }

                    step {
                        toExpr = "SetSolution[x: {sqrt[2]}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }

                        task {
                            taskId = "#1"
                            startExpr = "[(-sqrt[2]) ^ 3] + 3 (-sqrt[2]) + 7 > 0"
                            explanation {
                                key = EquationsExplanation.CheckIfSolutionSatisfiesConstraint
                            }

                            step {
                                toExpr = "-5 sqrt[2] + 7 > 0"
                            }

                            step {
                                toExpr = "-0.0710678119 > 0"
                            }

                            step {
                                toExpr = "Contradiction[-0.0710678119 > 0]"
                            }
                        }

                        task {
                            taskId = "#2"
                            startExpr = "[(sqrt[2]) ^ 3] + 3 sqrt[2] + 7 > 0"
                            explanation {
                                key = EquationsExplanation.CheckIfSolutionSatisfiesConstraint
                            }

                            step {
                                toExpr = "5 sqrt[2] + 7 > 0"
                            }

                            step {
                                toExpr = "Identity[5 sqrt[2] + 7 > 0]"
                            }
                        }

                        task {
                            startExpr = "SetSolution[x: {sqrt[2]}]"
                            explanation {
                                key = EquationsExplanation.SomeSolutionsDoNotSatisfyConstraint
                            }
                        }
                    }
                }
            }
        }
    }
}
