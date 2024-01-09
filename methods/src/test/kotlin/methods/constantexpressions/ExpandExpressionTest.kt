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
import methods.expand.ExpandExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class ExpandExpressionTest {
    @Test
    fun `test simplify by expanding constant expression`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "3(sqrt[2] + 1) + root[5, 3] + 1"

            check {
                fromExpr = "3 (sqrt[2] + 1) + root[5, 3] + 1"
                toExpr = "3 sqrt[2] + 4 + root[5, 3]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "3 (sqrt[2] + 1) + root[5, 3] + 1"
                    toExpr = "<.3 sqrt[2] + 3.> + root[5, 3] + 1"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "3 (sqrt[2] + 1)"
                        toExpr = "<.3 * sqrt[2] + 3 * 1.>"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "<.3 * sqrt[2] + 3 * 1.>"
                        toExpr = "<.3 sqrt[2] + 3.>"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }
                }

                step {
                    fromExpr = "3 sqrt[2] + 3 + root[5, 3] + 1"
                    toExpr = "3 sqrt[2] + 4 + root[5, 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }
            }
        }
}
