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
import methods.decimals.DecimalsExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class DecimalsTest {
    @Test
    fun testSimplifyConstantExpressionWithDivisionOfDecimals() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "1 + 0.002 : 0.6"

            check {
                fromExpr = "1 + 0.002 : 0.6"
                toExpr = "[301 / 300]"

                step {
                    fromExpr = "1 + 0.002 : 0.6"
                    toExpr = "1 + [0.002 / 0.6]"
                    explanation {
                        key = FractionArithmeticExplanation.RewriteDivisionAsFraction
                    }
                }

                step {
                    fromExpr = "1 + [0.002 / 0.6]"
                    toExpr = "1 + [2 / 600]"
                    explanation {
                        key = DecimalsExplanation.NormalizeFractionOfDecimals
                    }
                }

                step {
                    fromExpr = "1 + [2 / 600]"
                    toExpr = "1 + [1 / 300]"
                    explanation {
                        key = FractionArithmeticExplanation.SimplifyFraction
                    }
                }

                step {
                    fromExpr = "1 + [1 / 300]"
                    toExpr = "[301 / 300]"
                }
            }
        }

    @Test
    fun testDivisionOfDecimalsWithTrailingZeros() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[1.20000 / 1.2]"

            check {
                fromExpr = "[1.20000 / 1.2]"
                toExpr = "1"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "[1.20000 / 1.2]"
                    toExpr = "[1.2 / 1.2]"
                    explanation {
                        key = DecimalsExplanation.StripTrailingZerosAfterDecimal
                    }
                }

                step {
                    fromExpr = "[1.2 / 1.2]"
                    toExpr = "1"
                    explanation {
                        key = GeneralExplanation.SimplifyUnitFractionToOne
                    }
                }
            }
        }
}
