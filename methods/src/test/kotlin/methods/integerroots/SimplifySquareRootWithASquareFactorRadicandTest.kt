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

class SimplifySquareRootWithASquareFactorRadicandTest {
    @Test
    fun testExpressionWithSquareCoefficient() =
        testMethod {
            method = IntegerRootsPlans.SimplifySquareRootWithASquareFactorRadicand
            inputExpr = "sqrt[9 x y]"

            check {
                fromExpr = "sqrt[9 x y]"
                toExpr = "3 sqrt[x y]"
                explanation {
                    key = IntegerRootsExplanation.SimplifySquareRootWithASquareFactorRadicand
                }

                step {
                    fromExpr = "sqrt[9 x y]"
                    toExpr = "sqrt[9] * sqrt[x y]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "sqrt[9] * sqrt[x y]"
                    toExpr = "3 sqrt[x y]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRootToInteger
                    }
                }
            }
        }

    @Test
    fun testExpressionWithACoefficientWithASquareFactor() =
        testMethod {
            method = IntegerRootsPlans.SimplifySquareRootWithASquareFactorRadicand
            inputExpr = "sqrt[50 z]"

            check {
                fromExpr = "sqrt[50 z]"
                toExpr = "5 sqrt[2 z]"
                explanation {
                    key = IntegerRootsExplanation.SimplifySquareRootWithASquareFactorRadicand
                }

                step {
                    fromExpr = "sqrt[50 z]"
                    toExpr = "sqrt[25 * 2 z]"
                    explanation {
                        key = IntegerRootsExplanation.FactorGreatestCommonSquareIntegerFactor
                    }
                }

                step {
                    fromExpr = "sqrt[25 * 2 z]"
                    toExpr = "sqrt[25] * sqrt[2 z]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "sqrt[25] * sqrt[2 z]"
                    toExpr = "5 sqrt[2 z]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRootToInteger
                    }
                }
            }
        }

    @Test
    fun testSumWithASquareFactor() =
        testMethod {
            method = IntegerRootsPlans.SimplifySquareRootWithASquareFactorRadicand
            inputExpr = "sqrt[4 x - 8 y]"

            check {
                fromExpr = "sqrt[4 x - 8 y]"
                toExpr = "2 sqrt[x - 2 y]"
                explanation {
                    key = IntegerRootsExplanation.SimplifySquareRootWithASquareFactorRadicand
                }

                step {
                    fromExpr = "sqrt[4 x - 8 y]"
                    toExpr = "sqrt[4 (x - 2 y)]"
                    explanation {
                        key = IntegerRootsExplanation.FactorGreatestCommonSquareIntegerFactor
                    }
                }

                step {
                    fromExpr = "sqrt[4 (x - 2 y)]"
                    toExpr = "sqrt[4] * sqrt[x - 2 y]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "sqrt[4] * sqrt[x - 2 y]"
                    toExpr = "2 sqrt[x - 2 y]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRootToInteger
                    }
                }
            }
        }
}
