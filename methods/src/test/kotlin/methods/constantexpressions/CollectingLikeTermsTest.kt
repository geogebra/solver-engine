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
import methods.collecting.CollectingExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class CollectingLikeTermsTest {
    @Test
    fun testCollectLikeRootsAndSimplify() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "2 - 3 sqrt[3] + root[3, 3] + [2 sqrt[3] / 3] + 2 sqrt[3]"

            check {
                fromExpr = "2 - 3 sqrt[3] + root[3, 3] + [2 sqrt[3] / 3] + 2 sqrt[3]"
                toExpr = "2 - [sqrt[3] / 3] + root[3, 3]"
                explanation {
                    key = CollectingExplanation.CollectLikeRootsAndSimplify
                }

                step {
                    fromExpr = "2 - 3 sqrt[3] + root[3, 3] + [2 sqrt[3] / 3] + 2 sqrt[3]"
                    toExpr = "2 + (-3 + [2 / 3] + 2) sqrt[3] + root[3, 3]"
                    explanation {
                        key = CollectingExplanation.CollectLikeRoots
                    }
                }

                step {
                    fromExpr = "2 + (-3 + [2 / 3] + 2) sqrt[3] + root[3, 3]"
                    toExpr = "2 - [sqrt[3] / 3] + root[3, 3]"
                    explanation {
                        key = CollectingExplanation.SimplifyCoefficient
                    }

                    step {
                        fromExpr = "(-3 + [2 / 3] + 2) sqrt[3]"
                        toExpr = "(-1 + [2 / 3]) sqrt[3]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                        }
                    }

                    step {
                        fromExpr = "(-1 + [2 / 3]) sqrt[3]"
                        toExpr = "(-[1 / 3]) sqrt[3]"
                        explanation {
                            key = FractionArithmeticExplanation.AddIntegerAndFraction
                        }
                    }

                    step {
                        fromExpr = "(-[1 / 3]) sqrt[3]"
                        toExpr = "-[1 / 3] sqrt[3]"
                        explanation {
                            key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                        }
                    }

                    step {
                        fromExpr = "-[1 / 3] sqrt[3]"
                        toExpr = "-[sqrt[3] / 3]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                        }
                    }
                }
            }
        }
}
