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

class SimplifyPowerOfIntegerUnderRootTest {
    @Test
    fun testSimplificationOfIntegerPowerUnderHigherOrderRoot() =
        testMethod {
            method = IntegerRootsPlans.SimplifyPowerOfIntegerUnderRoot
            inputExpr = "root[[24 ^ 5], 6]"

            check {
                fromExpr = "root[[24 ^ 5], 6]"
                toExpr = "4 root[1944, 6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                }

                step {
                    fromExpr = "root[[24 ^ 5], 6]"
                    toExpr = "root[[2 ^ 15] * [3 ^ 5], 6]"
                    explanation {
                        key = IntegerRootsExplanation.FactorizeAndDistributePowerUnderRoot
                    }
                }

                step {
                    fromExpr = "root[[2 ^ 15] * [3 ^ 5], 6]"
                    toExpr = "root[[2 ^ 15], 6] * root[[3 ^ 5], 6]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "root[[2 ^ 15], 6] * root[[3 ^ 5], 6]"
                    toExpr = "<.4 sqrt[2].> * root[[3 ^ 5], 6]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "<.4 sqrt[2].> * root[[3 ^ 5], 6]"
                    toExpr = "<.4 sqrt[2].> * root[243, 6]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "4 sqrt[2] * root[243, 6]"
                    toExpr = "4 root[1944, 6]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyProductWithRoots
                    }
                }
            }
        }
}
