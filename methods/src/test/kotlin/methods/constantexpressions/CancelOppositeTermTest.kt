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
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class CancelOppositeTermTest {
    @Test
    fun testCancelOppositeTerm() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(sqrt[2] + root[3, 3]) + 1 - (sqrt[2] + root[3, 3]) - 2"

            check {
                fromExpr = "(sqrt[2] + root[3, 3]) + 1 - (sqrt[2] + root[3, 3]) - 2"
                toExpr = "-1"

                step {
                    fromExpr = "(sqrt[2] + root[3, 3]) + 1 - (sqrt[2] + root[3, 3]) - 2"
                    toExpr = "1 - 2"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }

                step {
                    fromExpr = "1 - 2"
                    toExpr = "-1"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                    }
                }
            }
        }

    @Test
    fun testCommutativeCancelAdditiveInverseElements() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "-(sqrt[2] + root[3, 3]) + 1 + (sqrt[2] + root[3, 3]) - 2"

            check {
                fromExpr = "-(sqrt[2] + root[3, 3]) + 1 + (sqrt[2] + root[3, 3]) - 2"
                toExpr = "-1"

                step {
                    fromExpr = "-(sqrt[2] + root[3, 3]) + 1 + (sqrt[2] + root[3, 3]) - 2"
                    toExpr = "1 - 2"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }

                step {
                    fromExpr = "1 - 2"
                    toExpr = "-1"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                    }
                }
            }
        }

    @Test
    fun testCommutativeAdditiveInverseElementsComplex() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "-1 + root[2, 3] + 2 - 1 - root[2, 3]"

            check {
                step {
                    toExpr = "-1 + 2 - 1"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }

                step { }
            }
        }

    @Test
    fun testCancelAdditiveInverseElementsAfterSimplifying() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "-(root[3, 3] + 2 root[3, 3] - 4 root[3, 3]) - (-root[3, 3] - 3 root[3, 3] + 5 root[3, 3])"

            check {
                toExpr = "0"

                step { }

                step { }

                // earlier it used to apply IntegerRoots.CollectLikeRootsAndSimplify
                step {
                    fromExpr = "-(-root[3, 3]) - root[3, 3]"
                    toExpr = "0"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }
            }
        }
}
