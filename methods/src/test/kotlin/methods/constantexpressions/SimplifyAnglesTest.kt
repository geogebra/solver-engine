/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

package methods.constantexpressions

import engine.methods.testMethod
import methods.angles.AnglesExplanation
import kotlin.test.Test

class SimplifyAnglesTest {
    @Test
    fun testEvaluationOfMainAngles() {
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "sin [10 * /pi/ / 24]"

            check {
                step { toExpr = "sin [5 /pi/ / 12]" }
                step { toExpr = "[sqrt[6] + sqrt[2] / 4]" }
            }
        }

        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "cos degree[ 120 ]"

            check {
                fromExpr = "cos degree[ 120 ]"
                toExpr = "-[1 / 2]"

                step {
                    fromExpr = "cos degree[ 120 ]"
                    toExpr = "-cos (degree[ 180 ] - degree[ 120 ])"
                }

                step {
                    fromExpr = "-cos (degree[ 180 ] - degree[ 120 ])"
                    toExpr = "-cos degree[ 60 ]"
                }

                step {
                    fromExpr = "-cos degree[ 60 ]"
                    toExpr = "-[1 / 2]"
                }
            }
        }

        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "sin degree[ 157.5 ]"

            check {
                fromExpr = "sin degree[ 157.5 ]"
                toExpr = "[sqrt[2 - sqrt[2]] / 2]"

                step {
                    fromExpr = "sin degree[ 157.5 ]"
                    toExpr = "sin [degree[ 315 ] / 2]"
                }

                step {
                    fromExpr = "sin [degree[ 315 ] / 2]"
                    toExpr = "[sqrt[2 - sqrt[2]] / 2]"

                    step {
                        fromExpr = "sin [degree[ 315 ] / 2]"
                        toExpr = "sin (degree[ 180 ] - [degree[ 315 ] / 2])"
                    }

                    step {
                        fromExpr = "sin (degree[ 180 ] - [degree[ 315 ] / 2])"
                        toExpr = "sin [degree[ 45 ] / 2]"
                    }

                    step {
                        fromExpr = "sin [degree[ 45 ] / 2]"
                        toExpr = "[sqrt[2 - sqrt[2]] / 2]"
                    }
                }
            }
        }
    }

    @Test
    fun `test normalize and evaluate multiple main angles`() {
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "sin degree[ 405 ] + sin [/pi/ / 4]"

            check {
                fromExpr = "sin degree[ 405 ] + sin [/pi/ / 4]"
                toExpr = "sqrt[2]"

                step {
                    fromExpr = "sin degree[ 405 ] + sin [/pi/ / 4]"
                    toExpr = "sin [/pi/ / 4] + sin [/pi/ / 4]"
                }

                step {
                    fromExpr = "sin [/pi/ / 4] + sin [/pi/ / 4]"
                    toExpr = "2 * sin [/pi/ / 4]"
                }

                step {
                    fromExpr = "2 * sin [/pi/ / 4]"
                    toExpr = "2 * [sqrt[2] / 2]"
                }

                step {
                    fromExpr = "2 * [sqrt[2] / 2]"
                    toExpr = "sqrt[2]"
                }
            }
        }
    }

    @Test
    fun `test normalize and add angles`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[5 /pi/ / 2] + degree[ 382.5 ]"

            check {
                fromExpr = "[5 /pi/ / 2] + degree[ 382.5 ]"
                toExpr = "[5 /pi/ / 8]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "[5 /pi/ / 2] + degree[ 382.5 ]"
                    toExpr = "[5 /pi/ / 2] + [/pi/ / 8]"
                    explanation {
                        key = AnglesExplanation.ConvertDegreesToRadians
                    }
                }

                step {
                    fromExpr = "[5 /pi/ / 2] + [/pi/ / 8]"
                    toExpr = "[21 /pi/ / 8]"
                }

                step {
                    fromExpr = "[21 /pi/ / 8]"
                    toExpr = "[5 /pi/ / 8]"
                    explanation {
                        key = AnglesExplanation.NormalizeAngles
                    }
                }
            }
        }
}
