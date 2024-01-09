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

package methods.integerroots

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class SimplifyProductOfRootsTest {
    @Test
    fun testProductOfEqualSquareRoots() =
        testMethod {
            method = IntegerRootsPlans.SimplifyProductWithRoots
            inputExpr = "sqrt[6] * sqrt[6]"

            check {
                fromExpr = "sqrt[6] * sqrt[6]"
                toExpr = "6"
                explanation {
                    key = IntegerRootsExplanation.SimplifyMultiplicationOfSquareRoots
                }
            }
        }

    @Test
    fun testProductOfDifferentSquareRoots() =
        testMethod {
            method = IntegerRootsPlans.SimplifyProductWithRoots
            inputExpr = "sqrt[6] * sqrt[3]"

            check {
                toExpr = "sqrt[18]"

                step {
                    toExpr = "sqrt[6 * 3]"
                }

                step {
                    toExpr = "sqrt[18]"
                }
            }
        }
}
