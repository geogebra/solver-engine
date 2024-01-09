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
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class SimplifyFractionTest {
    @Test
    fun `test simplify fraction with sum numerator & denominator additive inverse`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[1 - 2 sqrt[2] / 2 sqrt[2] - 1]"

            check {
                fromExpr = "[1 - 2 sqrt[2] / 2 sqrt[2] - 1]"
                toExpr = "-1"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }

                step {
                    fromExpr = "[1 - 2 sqrt[2] / 2 sqrt[2] - 1]"
                    toExpr = "[-(-1 + 2 sqrt[2]) / 2 sqrt[2] - 1]"
                    explanation {
                        key = GeneralExplanation.FactorMinusFromSum
                    }
                }

                step {
                    fromExpr = "[-(-1 + 2 sqrt[2]) / 2 sqrt[2] - 1]"
                    toExpr = "[-(-1 + 2 sqrt[2]) / -1 + 2 sqrt[2]]"
                    explanation {
                        key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                    }
                }

                step {
                    fromExpr = "[-(-1 + 2 sqrt[2]) / -1 + 2 sqrt[2]]"
                    toExpr = "[-1 / 1]"
                    explanation {
                        key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                    }
                }

                step {
                    fromExpr = "[-1 / 1]"
                    toExpr = "-1"
                    explanation {
                        key = GeneralExplanation.SimplifyFractionWithOneDenominator
                    }
                }
            }
        }

    @Test
    fun `test simplify fraction with power numerator & denominator additive inverse base even power`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[[(sqrt[2] - 1)^2] / [(1 - sqrt[2])^2]]"

            check {
                fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(1 - sqrt[2]) ^ 2]]"
                toExpr = "1"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }

                step {
                    fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(1 - sqrt[2]) ^ 2]]"
                    toExpr = "[[(sqrt[2] - 1) ^ 2] / [(-(-1 + sqrt[2])) ^ 2]]"
                    explanation {
                        key = GeneralExplanation.FactorMinusFromSum
                    }
                }

                step {
                    fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(-(-1 + sqrt[2])) ^ 2]]"
                    toExpr = "[[(sqrt[2] - 1) ^ 2] / [(-1 + sqrt[2]) ^ 2]]"
                    explanation {
                        key = GeneralExplanation.SimplifyEvenPowerOfNegative
                    }
                }

                step {
                    fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(-1 + sqrt[2]) ^ 2]]"
                    toExpr = "[[(sqrt[2] - 1) ^ 2] / [(sqrt[2] - 1) ^ 2]]"
                    explanation {
                        key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                    }
                }

                step {
                    fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(sqrt[2] - 1) ^ 2]]"
                    toExpr = "[1 / 1]"
                    explanation {
                        key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                    }
                }

                step {
                    fromExpr = "[1 / 1]"
                    toExpr = "1"
                    explanation {
                        key = GeneralExplanation.SimplifyUnitFractionToOne
                    }
                }
            }
        }
}
