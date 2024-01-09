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

package methods.simplify

import engine.context.Preset
import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("GmAction")
class GmSimplificationTest {
    // @erik you might want to make this a gmaction test
    @Test
    fun `test fraction and value multiplication`() =
        testMethod(Preset.GMFriendly) {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "2 * [x / 2]"

            check {
                fromExpr = "2 * [x / 2]"
                toExpr = "x"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                }

                step {
                    fromExpr = "2 * [x / 2]"
                    toExpr = "[2 x / 2]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractionAndValue
                    }
                }

                step {
                    fromExpr = "[2 x / 2]"
                    toExpr = "x"
                    explanation {
                        key = GeneralExplanation.CancelDenominator
                    }
                }
            }
        }
}
