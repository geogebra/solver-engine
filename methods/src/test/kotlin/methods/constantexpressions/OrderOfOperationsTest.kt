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

package methods.constantexpressions

import engine.methods.testMethod
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class OrderOfOperationsTest {
    @Test
    fun testFactorOfOneIsEliminated() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "1 sqrt[2] * 1 sqrt[3]"

            check {
                fromExpr = "1 sqrt[2] * 1 sqrt[3]"
                toExpr = "sqrt[6]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "1 sqrt[2] * 1 sqrt[3]"
                    toExpr = "sqrt[2] * 1 sqrt[3]"
                    explanation {
                        key = GeneralExplanation.RemoveUnitaryCoefficient
                    }
                }

                step {
                    fromExpr = "sqrt[2] * 1 sqrt[3]"
                    toExpr = "sqrt[2] * sqrt[3]"
                    explanation {
                        key = GeneralExplanation.RemoveUnitaryCoefficient
                    }
                }

                step {}
            }
        }

    @Test
    fun testFractionOverOnePriority() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[sqrt[12] / 1]"

            check {
                step {
                    toExpr = "sqrt[12]"
                }
                step {}
            }
        }
}
