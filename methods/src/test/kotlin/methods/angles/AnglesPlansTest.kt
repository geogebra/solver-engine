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

package methods.angles

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class AnglesPlansTest {
    @Test
    fun testDegreeConversion() {
        testMethod {
            method = AnglesPlans.ConvertDegreesToRadians
            inputExpr = "degree[ 45 ]"

            check {
                step { toExpr = "degree[ 45 ] * [/pi/ / degree[ 180 ]]" }
                step {
                    step { toExpr = "[degree[ 45 ] * /pi/ / degree[ 180 ]]" }
                    step { toExpr = "[45 /pi/ / 180]" }
                    step { toExpr = "[/pi/ / 4]" }
                }
            }
        }
    }

    @Test
    fun testRadianConversion() {
        testMethod {
            method = AnglesPlans.ConvertRadiansToDegrees
            inputExpr = "[3 * /pi/ / 4]"

            check {
                step { toExpr = "[3 * /pi/ / 4] * [degree[ 180 ] / /pi/]" }
                step {
                    step { toExpr = "[3 * degree[ 180 ] / 4]" }

                    step {
                        toExpr = "[3 * 4 * degree[ 45 ] / 4 * 1]"
                    }

                    step { toExpr = "[3 * 4 * degree[ 45 ] / 4]" }

                    step {
                        toExpr = "3 * degree[ 45 ]"
                    }

                    step { toExpr = "degree[ 135 ]" }
                }
            }
        }
    }

    @Test
    fun testEvaluationOfMainAngles() {
        testMethod {
            method = AnglesPlans.EvaluateTrigonometricExpression
            inputExpr = "sin [10 * /pi/ / 24]"

            check {
                step { toExpr = "sin [5 /pi/ / 12]" }
                step { toExpr = "[1 / 4] (sqrt[6] + sqrt[2])" }
            }
        }

        testMethod {
            method = AnglesPlans.EvaluateTrigonometricExpression
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
            method = AnglesPlans.EvaluateTrigonometricExpression
            inputExpr = "sin degree[ 157.5 ]"

            check {
                fromExpr = "sin degree[ 157.5 ]"
                toExpr = "[1 / 2] sqrt[2 - sqrt[2]]"

                step {
                    fromExpr = "sin degree[ 157.5 ]"
                    toExpr = "sin [degree[ 315 ] / 2]"
                }

                step {
                    fromExpr = "sin [degree[ 315 ] / 2]"
                    toExpr = "[1 / 2] sqrt[2 - sqrt[2]]"

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
                        toExpr = "[1 / 2] sqrt[2 - sqrt[2]]"
                    }
                }
            }
        }
    }
}
