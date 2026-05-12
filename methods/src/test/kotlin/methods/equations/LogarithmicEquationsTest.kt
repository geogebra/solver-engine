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

class LogarithmicEquationsTest {
    // Right now these are only for the normalization part, but should be updated once
    // we have solutions implemented.
    @Test
    fun `solve equation with one log term`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 = -log (x + 1)"

            check {
                task {
                    startExpr = "2 = -log (x + 1)"
                }

                task {
                    startExpr = "2 = -log (x + 1)"

                    step {
                        fromExpr = "2 = -log (x + 1)"
                        toExpr = "log (x + 1) = -2"
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "2 + log (x + 1) = 0"
                        }

                        step {
                            toExpr = "log (x + 1) = -2"
                        }
                    }
                }

                task {
                    startExpr = "log (x + 1) = -2 GIVEN SetSolution[x: (-1, /infinity/)]"
                }
            }
        }
    }

    @Test
    fun `solve equation with multiple logs with same argument and base`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 + 3 * log (x + 1) + 5 * log (x + 1) + 4 = 3 - log (x + 1) + 2 * log (x + 1)"

            check {
                task {
                    startExpr = "2 + 3 * log (x + 1) + 5 * log (x + 1) + 4 = 3 - log (x + 1) + 2 * log (x + 1)"

                    step {
                        toExpr = "SetSolution[x: (-1, /infinity/)]"
                    }
                }

                task {
                    startExpr = "2 + 3 * log (x + 1) + 5 * log (x + 1) + 4 = 3 - log (x + 1) + 2 * log (x + 1)"

                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "6 + 8 * log (x + 1) = 3 + log (x + 1)"
                        }

                        step {
                            toExpr = "6 + 7 * log (x + 1) = 3"
                        }

                        step {
                            toExpr = "7 * log (x + 1) = -3"
                        }

                        step {
                            toExpr = "log (x + 1) = -[3 / 7]"
                        }
                    }
                }

                task {
                    startExpr = "log (x + 1) = -[3 / 7] GIVEN SetSolution[x: (-1, /infinity/)]"
                }
            }
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `solve equation with multiple logs with different arguments`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "-2 * log_[2] (x + 1) + log_[4] (2 x) = log_[2] (x + 1) + log_[2] 5 - log_[2] (x - 2)"

            check {
                task {
                    startExpr = "-2 * log_[2] (x + 1) + log_[4] (2 x) = log_[2] (x + 1) + log_[2] 5 - log_[2] (x - 2)"

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
                    startExpr = "-2 * log_[2] (x + 1) + log_[4] (2 x) = log_[2] (x + 1) + log_[2] 5 - log_[2] (x - 2)"

                    step {
                        step {
                            toExpr =
                                "-2 * log_[2] (x + 1) + [log_[2] (2 x) / 2] =" +
                                " log_[2] (x + 1) + log_[2] 5 - log_[2] (x - 2)"
                            explanation {
                                key = LogsExplanation.BringLogsToCommonBase
                            }
                        }

                        step {
                            toExpr =
                                "[log_[2] (2 x) / 2] + log_[2] (x - 2) " +
                                "= 3 * log_[2] (x + 1) + log_[2] 5"
                            explanation {
                                key = LogsExplanation.MoveNegatedLogarithmicTermsToOppositeSideAndSimplify
                            }
                        }

                        step {
                            toExpr = "log_[2] (2 x) + 2 * log_[2] (x - 2) = 6 * log_[2] (x + 1) + 2 * log_[2] 5"
                        }

                        step {
                            toExpr = "log_[2] (2 x * [(x - 2) ^ 2]) = 6 * log_[2] (x + 1) + 2 * log_[2] 5"
                            explanation {
                                key = LogsExplanation.CollectLogarithmsInSum
                            }
                        }

                        step {
                            toExpr = "log_[2] (2 x * [(x - 2) ^ 2]) = log_[2] (25 * [(x + 1) ^ 6])"
                            explanation {
                                key = LogsExplanation.CollectLogarithmsInSum
                            }
                        }
                    }
                }

                task {
                    startExpr =
                        "log_[2] (2 x * [(x - 2) ^ 2]) = log_[2] (25 * [(x + 1) ^ 6]) " +
                        "GIVEN SetSolution[x: (2, /infinity/)]"
                }
            }
        }
    }

    @Test
    fun `solve already balanced equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "3 * log (x + 1) = log ([x ^ 3] + 3 x + 7)"

            check {
                task {
                    step {
                        toExpr = "SetSolution[x: (-1, /infinity/)] AND [x ^ 3] + 3 x + 7 > 0"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    step {
                        fromExpr = "3 * log (x + 1) = log ([x ^ 3] + 3 x + 7)"
                        toExpr = "log [(x + 1) ^ 3] = log ([x ^ 3] + 3 x + 7)"
                        explanation {
                            key = LogsExplanation.RewriteCoefficientsAsExponents
                        }
                    }
                }

                task {
                    startExpr =
                        "log [(x + 1) ^ 3] = log ([x ^ 3] + 3 x + 7) " +
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
            inputExpr = "[1 / 2] * log (9 - x) = log 3 + [1 / 2] * log x"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                    }

                    step {
                        toExpr = "SetSolution[x: (0, 9)]"
                    }
                }

                task {
                    step {
                        toExpr = "log (9 - x) = log (9 x)"
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquations
                        }

                        step {
                            toExpr = "log (9 - x) = 2 * log 3 + log x"
                        }

                        step {
                            toExpr = "log (9 - x) = log (9 x)"
                            explanation {
                                key = LogsExplanation.CollectLogarithmsInSum
                            }
                        }
                    }
                }

                task {
                    startExpr = "log (9 - x) = log (9 x) GIVEN SetSolution[x: (0, 9)]"
                }
            }
        }
    }

    @Test
    fun `does not expand constant logarithms`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "log (x - 16) = log (105) - log (x)"

            check {
                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
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
                            toExpr = "log (x - 16) = log 105 - log x"
                        }

                        step {
                            toExpr = "log (x - 16) + log x = log 105"
                        }

                        step {
                            toExpr = "log (x (x - 16)) = log 105"
                        }
                    }
                }

                task {
                    startExpr = "log (x (x - 16)) = log 105 GIVEN SetSolution[x: (16, /infinity/)]"
                }
            }
        }
    }
}
