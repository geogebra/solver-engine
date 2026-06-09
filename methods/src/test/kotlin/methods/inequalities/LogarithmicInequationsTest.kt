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
import methods.algebra.AlgebraExplanation
import methods.equations.EquationsExplanation
import methods.logs.LogsExplanation
import kotlin.test.Test

class LogarithmicInequationsTest {
    @Suppress("LongMethod")
    @Test
    fun `elementary equation with base in (1, inf)`() {
        testMethodInX {
            method = InequalitiesPlans.SolveLogInequality
            inputExpr = "log_[2][2 x - 1] > 1"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveLogarithmicInequality
                }

                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: ([1 / 2], /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    step {
                        explanation {
                            key = InequalitiesExplanation.SolveLogarithmicInequality
                        }

                        step {
                            toExpr = "[2 ^ log_[2][2 x - 1]] > [2 ^ 1]"
                            explanation {
                                key = LogsExplanation.ExponentiateBothSides
                            }
                        }

                        step {
                            toExpr = "2 x - 1 > [2 ^ 1]"
                            explanation {
                                key = LogsExplanation.SimplifyLogInExponentWithMatchingBase
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: ([3 / 2], /infinity/)]"
                            explanation {
                                key = InequalitiesExplanation.SolveLinearInequality
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: ([3 / 2], /infinity/)] GIVEN SetSolution[x: ([1 / 2], /infinity/)]"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }

                    step {
                        toExpr = "SetSolution[x: ([3 / 2], /infinity/)]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveLogInequality
            inputExpr = "1 < log_[2][2 x - 1]"

            check {
                task {
                    step {
                        toExpr = "SetSolution[x: ([1 / 2], /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    step {
                        toExpr = "SetSolution[x: ([3 / 2], /infinity/)]"
                        explanation {
                            key = InequalitiesExplanation.SolveLogarithmicInequality
                        }

                        step {
                            toExpr = "log_[2][2 x - 1] > 1"
                        }

                        step {
                            toExpr = "[2 ^ log_[2][2 x - 1]] > [2 ^ 1]"
                            explanation {
                                key = LogsExplanation.ExponentiateBothSides
                            }
                        }

                        step {
                            toExpr = "2 x - 1 > [2 ^ 1]"
                            explanation {
                                key = LogsExplanation.SimplifyLogInExponentWithMatchingBase
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: ([3 / 2], /infinity/)]"
                            explanation {
                                key = InequalitiesExplanation.SolveLinearInequality
                            }
                        }
                    }
                }

                task {
                    step {
                        fromExpr = "SetSolution[x: ([3 / 2], /infinity/)] GIVEN SetSolution[x: ([1 / 2], /infinity/)]"
                        toExpr = "SetSolution[x: ([3 / 2], /infinity/)]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveLogInequality
            inputExpr = "ln[x] < 1"

            check {
                task {
                    step {
                        toExpr = "SetSolution[x: (0, /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    step {
                        explanation {
                            key = InequalitiesExplanation.SolveLogarithmicInequality
                        }

                        step {
                            toExpr = "[/e/ ^ ln[x]] < [/e/ ^ 1]"
                            explanation {
                                key = LogsExplanation.ExponentiateBothSides
                            }
                        }

                        step {
                            toExpr = "x < [/e/ ^ 1]"
                            explanation {
                                key = LogsExplanation.SimplifyLogInExponentWithMatchingBase
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: (-/infinity/, /e/)]"
                            explanation {
                                key = InequalitiesExplanation.SolveLinearInequality
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: (-/infinity/, /e/)] GIVEN SetSolution[x: (0, /infinity/)]"

                    step {
                        toExpr = "SetSolution[x: (0, /e/)]"
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
    fun `elementary equation with base in (0,1)`() {
        testMethodInX {
            method = InequalitiesPlans.SolveLogInequality
            inputExpr = "log_[[1 / 2]][x - 2] > 3"

            check {
                fromExpr = "log_[[1 / 2]][x - 2] > 3"
                toExpr = "SetSolution[x: (2, [17 / 8])]"
                explanation {
                    key = InequalitiesExplanation.SolveLogarithmicInequality
                }

                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "SetSolution[x: (2, /infinity/)]"
                    }
                }

                task {
                    explanation {
                        key = InequalitiesExplanation.SolveLogarithmicInequality
                    }

                    step {
                        explanation {
                            key = InequalitiesExplanation.SolveLogarithmicInequality
                        }

                        step {
                            toExpr = "[([1 / 2]) ^ log_[[1 / 2]][x - 2]] < [([1 / 2]) ^ 3]"
                            explanation {
                                key = LogsExplanation.ExponentiateBothSidesAndFlip
                            }
                        }

                        step {
                            toExpr = "x - 2 < [([1 / 2]) ^ 3]"
                            explanation {
                                key = LogsExplanation.SimplifyLogInExponentWithMatchingBase
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: (-/infinity/, [17 / 8])]"
                            explanation {
                                key = InequalitiesExplanation.SolveLinearInequality
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: (-/infinity/, [17 / 8])] GIVEN SetSolution[x: (2, /infinity/)]"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }

                    step {
                        toExpr = "SetSolution[x: (2, [17 / 8])]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveLogInequality
            inputExpr = "log_[0.5][x - 2] > 3"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveLogarithmicInequality
                }

                task {
                    step {
                        toExpr = "SetSolution[x: (2, /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    explanation {
                        key = InequalitiesExplanation.SolveLogarithmicInequality
                    }

                    step {
                        explanation {
                            key = InequalitiesExplanation.SolveLogarithmicInequality
                        }

                        step {
                            toExpr = "log_[[1/2]][x - 2] > 3"
                        }

                        step {
                            toExpr = "[([1 / 2]) ^ log_[[1 / 2]][x - 2]] < [([1 / 2]) ^ 3]"
                            explanation {
                                key = LogsExplanation.ExponentiateBothSidesAndFlip
                            }
                        }

                        step {
                            toExpr = "x - 2 < [([1/2]) ^ 3]"
                            explanation {
                                key = LogsExplanation.SimplifyLogInExponentWithMatchingBase
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: (-/infinity/, [17 / 8])]"
                            explanation {
                                key = InequalitiesExplanation.SolveLinearInequality
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: (-/infinity/, [17 / 8])] GIVEN SetSolution[x: (2, /infinity/)]"

                    step {
                        toExpr = "SetSolution[x: (2, [17 / 8])]"
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
    fun `inequality with two logs with base in (1, inf)`() {
        testMethodInX {
            method = InequalitiesPlans.SolveLogInequality
            inputExpr = "log[x + 3] > log[2 x - 1]"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveLogarithmicInequality
                }

                task {
                    step {
                        toExpr = "SetSolution[x: ([1 / 2], /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    explanation {
                        key = InequalitiesExplanation.SolveLogarithmicInequality
                    }

                    step {
                        explanation {
                            key = InequalitiesExplanation.SolveLogarithmicInequality
                        }

                        step {
                            toExpr = "x + 3 > 2 x - 1"
                            explanation {
                                key = LogsExplanation.ApplyEqualityRuleOfLogsWithoutSignFlip
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: (-/infinity/, 4)]"
                            explanation {
                                key = InequalitiesExplanation.SolveLinearInequality
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: (-/infinity/, 4)] GIVEN SetSolution[x: ([1 / 2], /infinity/)]"

                    step {
                        toExpr = "SetSolution[x: ([1 / 2], 4)]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }

        testMethodInX {
            method = InequalitiesPlans.SolveLogInequality
            inputExpr = "log[x (x + 3)] > log[x - 2]"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveLogarithmicInequality
                }

                task {
                    step {
                        toExpr = "SetSolution[x: (2, /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    explanation {
                        key = InequalitiesExplanation.SolveLogarithmicInequality
                    }

                    step {
                        explanation {
                            key = InequalitiesExplanation.SolveLogarithmicInequality
                        }

                        step {
                            toExpr = "x (x + 3) > x - 2"
                            explanation {
                                key = LogsExplanation.ApplyEqualityRuleOfLogsWithoutSignFlip
                            }
                        }

                        step {
                            toExpr = "Identity[x: [x ^ 2] + 2 x + 2 > 0]"
                            explanation {
                                key = InequalitiesExplanation.SolveQuadraticInequality
                            }
                        }
                    }
                }

                task {
                    startExpr = "Identity[x: [x ^ 2] + 2 x + 2 > 0] GIVEN SetSolution[x: (2, /infinity/)]"

                    step {
                        toExpr = "SetSolution[x: (2, /infinity/)]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `inequality with two logs with base in (0,1)`() {
        testMethodInX {
            method = InequalitiesPlans.SolveLogInequality
            inputExpr = "log_[[1 / 3]][x + 1] >= log_[[1 / 3]][4 x]"

            check {
                explanation {
                    key = InequalitiesExplanation.SolveLogarithmicInequality
                }

                task {
                    step {
                        toExpr = "SetSolution[x: (0, /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    explanation {
                        key = InequalitiesExplanation.SolveLogarithmicInequality
                    }

                    step {
                        explanation {
                            key = InequalitiesExplanation.SolveLogarithmicInequality
                        }

                        step {
                            toExpr = "x + 1 <= 4 x"
                            explanation {
                                key = LogsExplanation.ApplyEqualityRuleOfLogsWithSignFlip
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: [[1 / 3], /infinity/)]"
                            explanation {
                                key = InequalitiesExplanation.SolveLinearInequality
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: [[1 / 3], /infinity/)] GIVEN SetSolution[x: (0, /infinity/)]"

                    step {
                        toExpr = "SetSolution[x: [[1 / 3], /infinity/)]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `inequality with impossible constraint`() {
        testMethodInX {
            method = InequalitiesPlans.SolveLogInequality
            inputExpr = "log[x + 3] > log[-x - 7]"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfEquation
                    }

                    step {
                        toExpr = "Contradiction[x: log[x + 3] > log[-x - 7]]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    explanation {
                        key = Explanation.EvaluateInequalityWithImpossibleConstraint
                    }

                    step {
                        fromExpr = "log[x + 3] > log[-x - 7] GIVEN Contradiction[x: log[x + 3] > log[-x - 7]]"
                        toExpr = "Contradiction[x: log[x + 3] > log[-x - 7]]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }
}
