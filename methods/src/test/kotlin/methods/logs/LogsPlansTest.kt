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
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.constantSimplificationSteps
import org.junit.jupiter.api.Test

class LogsPlansTest {
    @Test
    fun `switch logs in a sum to the smallest base`() =
        testMethod {
            method = createSwitchLogsToSmallestBase(constantSimplificationSteps)
            inputExpr = "log_[8] 15 + log_[4] 14 + log_[2] 13"

            check {
                toExpr = "[log_[2] 3 + log_[2] 5 / 3] + [1 + log_[2] 7 / 2] + log_[2] 13"
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
            inputExpr = "4 log_[8] 15 + 3 log_[4] 14 + 2 log_[2] 13"

            check {
                toExpr = "[4 * log_[2] 3 + 4 * log_[2] 5 / 3] + [3 + 3 * log_[2] 7 / 2] + 2 * log_[2] 13"
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
            inputExpr = "log_[4] 9 + ln 3"

            check {
                toExpr = "[ln 3 / ln 2] + ln 3"
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
            inputExpr = "log_[16] 64 + log_[2] 3"

            check {
                step {
                    toExpr = "log_[4] 8 + log_[2] 3"
                    explanation {
                        key = LogsExplanation.SimplifyLogWithMatchingPowers
                    }
                }

                step {
                    toExpr = "3 * log_[4] 2 + log_[2] 3"
                    explanation {
                        key = LogsExplanation.SimplifyLogOfKnownPower
                    }
                }

                step {
                    toExpr = "[3 / 2] + log_[2] 3"
                    explanation {
                        key = LogsExplanation.BringLogsToCommonBase
                    }
                }
            }
        }
}
