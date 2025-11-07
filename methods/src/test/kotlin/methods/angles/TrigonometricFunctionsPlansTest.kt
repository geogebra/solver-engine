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
import methods.simplify.SimplifyPlans
import org.junit.jupiter.api.Test

class TrigonometricFunctionsPlansTest {
    @Test
    fun usePythagoreanIdentityTest() {
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "3 [sin ^ 2 ][x] + 3 [cos ^ 2][x]"

            check {
                step {
                    toExpr = "3 ([sin ^ 2][x] + [cos ^ 2][x])"
                }

                step {
                    toExpr = "3 * 1"
                }

                step {
                    toExpr = "3"
                }
            }
        }

        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "[sin ^ 2][x] - 1 + [cos ^ 2][x]"

            check {
                step {
                    toExpr = "1 - 1"
                }

                step {
                    toExpr = "0"
                }
            }
        }
    }
}
