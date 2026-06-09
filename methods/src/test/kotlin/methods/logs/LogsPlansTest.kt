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

package methods.logs

import engine.methods.testMethod
import engine.methods.testMethodInX
import methods.algebra.AlgebraExplanation
import methods.algebra.AlgebraPlans
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.constantSimplificationSteps
import org.junit.jupiter.api.Test

class LogsPlansTest {
    @Test
    fun `switch logs in a sum to the smallest base`() =
        testMethod {
            method = createSwitchLogsToSmallestBase(constantSimplificationSteps)
            inputExpr = "log_[8][15] + log_[4][14] + log_[2][13]"

            check {
                toExpr = "[log_[2][3] + log_[2][5] / 3] + [1 + log_[2][7] / 2] + log_[2][13]"
                explanation {
                    key = Explanation.BringLogsToCommonBase
                    param { expr = "2" }
                }
            }
        }

    @Test
    fun `switch logs with coefficients in a sum to the smallest base`() =
        testMethod {
            method = createSwitchLogsToSmallestBase(constantSimplificationSteps)
            inputExpr = "4 log_[8][15] + 3 log_[4][14] + 2 log_[2][13]"

            check {
                toExpr = "[4 * log_[2][3] + 4 * log_[2][5] / 3] + [3 + 3 * log_[2][7] / 2] + 2 * log_[2][13]"
                explanation {
                    key = Explanation.BringLogsToCommonBase
                    param { expr = "2" }
                }
            }
        }

    @Test
    fun `test switching with real base`() =
        testMethod {
            method = createSwitchLogsToSmallestBase(constantSimplificationSteps)
            inputExpr = "log_[4][9] + ln[3]"

            check {
                toExpr = "[ln[3] / ln[2]] + ln[3]"
                explanation {
                    key = Explanation.BringLogsToCommonBase
                    param { expr = "/e/" }
                }
            }
        }

    @Test
    fun `test constant logarithmic expression simplification`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "log_[16][64] + log_[2][3]"

            check {
                step {
                    toExpr = "log_[4][8] + log_[2][3]"
                    explanation {
                        key = LogsExplanation.SimplifyLogWithMatchingPowers
                    }
                }

                step {
                    toExpr = "3 * log_[4][2] + log_[2][3]"
                    explanation {
                        key = LogsExplanation.SimplifyLogOfKnownPower
                    }
                }

                step {
                    toExpr = "[3 / 2] + log_[2][3]"
                    explanation {
                        key = LogsExplanation.BringLogsToCommonBase
                    }
                }
            }
        }

    @Test
    fun `constant logarithmic simplification does not loop`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "log_[2][3] + ln[3]"

            check {
                toExpr = "(1 + [1 / log_[2][/e/]]) log_[2][3]"
            }
        }

    @Test
    fun `convert logs with rational base to known base`() =
        testMethodInX {
            method = AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression
            inputExpr = "log_[[1 / 2]][[x ^ 2] - 4 x] + log_[2][2 x] - 1"

            check {
                task {
                    step {
                        toExpr = "SetSolution[x: (4, /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    step {
                        toExpr = "-log_[2][[x ^ 2] - 4 x] + log_[2][2 x] - 1"
                        explanation {
                            key = LogsExplanation.BringLogsToCommonBase
                        }
                    }
                }

                task {
                    startExpr = "-log_[2][[x ^ 2] - 4 x] + log_[2][2 x] - 1 GIVEN SetSolution[x: (4, /infinity/)]"
                    explanation {
                        key = AlgebraExplanation.CombineSimplifiedExpressionWithConstraint
                    }
                }
            }
        }

    @Test
    fun `convert logs with rational bases`() =
        testMethodInX {
            method = AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression
            inputExpr = "log_[[1 / 2]][x] + log_[[2 / 3]][x]"

            check {
                explanation {
                    key = AlgebraExplanation.ComputeDomainAndSimplifyAlgebraicExpression
                }

                task {
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                    }

                    step {
                        toExpr = "SetSolution[x: (0, /infinity/)]"
                    }
                }

                task {
                    step {
                        toExpr = "log_[[1 / 2]][x] + [log_[[1 / 2]][x] / log_[[1 / 2]][[2 / 3]]]"
                        explanation {
                            key = LogsExplanation.BringLogsToCommonBase
                        }
                    }

                    step {
                        toExpr = "(1 + [1 / log_[[1 / 2]][[2 / 3]]]) log_[[1 / 2]][x]"
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "(1 + [1 / log_[[1 / 2]][[2 / 3]]]) log_[[1 / 2]][x]" +
                        " GIVEN SetSolution[x: (0, /infinity/)]"
                    explanation {
                        key = AlgebraExplanation.CombineSimplifiedExpressionWithConstraint
                    }
                }
            }
        }
}
