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

package methods.fractionroots

import engine.methods.testMethod
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.constantexpressions.ConstantExpressionsPlans
import methods.expand.ExpandExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerroots.IntegerRootsExplanation
import org.junit.jupiter.api.Test

class FractionRootsPlansTest {
    @Test
    fun testRootOfFraction() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "sqrt[[3 / 2]]"

            check {
                step {
                    toExpr = "[sqrt[3] / sqrt[2]]"
                    explanation {
                        key = FractionRootsExplanation.DistributeRadicalOverFraction
                    }
                }

                step {
                    toExpr = "[sqrt[6] / 2]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeDenominator
                    }

                    step {
                        toExpr = "[sqrt[3] / sqrt[2]] * [sqrt[2] / sqrt[2]]"
                    }

                    step {
                        toExpr = "[sqrt[3] * sqrt[2] / sqrt[2] * sqrt[2]]"
                    }

                    step {
                        toExpr = "[sqrt[6] / sqrt[2] * sqrt[2]]"
                    }

                    step {
                        toExpr = "[sqrt[6] / 2]"
                    }
                }
            }
        }

    @Test
    fun testRationalizationRadicalWithCoefficient() =
        testMethod {
            method = FractionRootsPlans.RationalizeDenominators
            inputExpr = "[sqrt[3] / 3 sqrt[2]]"

            check {
                toExpr = "[sqrt[6] / 6]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }

                step {
                    toExpr = "[sqrt[3] / 3 sqrt[2]] * [sqrt[2] / sqrt[2]]"
                }

                step {
                    toExpr = "[sqrt[3] * sqrt[2] / 3 sqrt[2] * sqrt[2]]"
                }

                step {
                    toExpr = "[sqrt[6] / 3 sqrt[2] * sqrt[2]]"
                }

                step {
                    toExpr = "[sqrt[6] / 6]"
                }
            }
        }

    @Test
    fun testRationalizationWithSumOfRadicalsInNumerator() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"

            check {
                fromExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"
                toExpr = "[2 + sqrt[6] / 2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"
                    toExpr = "[(sqrt[2] + sqrt[3]) sqrt[2] / 2]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeDenominator
                    }
                }

                step {
                    fromExpr = "[(sqrt[2] + sqrt[3]) sqrt[2] / 2]"
                    toExpr = "[2 + sqrt[6] / 2]"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }
                }
            }
        }

    @Test
    fun testRationalizeHigherOrderRoot() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[9 / 2 root[18, 4]]"

            check {
                fromExpr = "[9 / 2 root[18, 4]]"
                toExpr = "[3 root[72, 4] / 4]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "[9 / 2 root[18, 4]]"
                    toExpr = "[9 root[72, 4] / 12]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeDenominator
                    }

                    step {
                        fromExpr = "[9 / 2 root[18, 4]]"
                        toExpr = "[9 / 2 root[2 * [3 ^ 2], 4]]"
                        explanation {
                            key = FractionRootsExplanation.FactorizeHigherOrderRadicand
                        }
                    }

                    step {
                        fromExpr = "[9 / 2 root[2 * [3 ^ 2], 4]]"
                        toExpr =
                            "[9 / 2 root[2 * [3 ^ 2], 4]] * [root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 2], 4]]"
                        explanation {
                            key = FractionRootsExplanation.RationalizeHigherOrderRoot
                        }

                        step {
                            fromExpr = "[9 / 2 root[2 * [3 ^ 2], 4]]"
                            toExpr =
                                "[9 / 2 root[2 * [3 ^ 2], 4]] * [root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4] " +
                                "/ root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                            explanation {
                                key = FractionRootsExplanation.HigherOrderRationalizingTerm
                            }
                        }

                        step {
                            fromExpr =
                                "[9 / 2 root[2 * [3 ^ 2], 4]] * [root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4] " +
                                "/ root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                            toExpr =
                                "[9 / 2 root[2 * [3 ^ 2], 4]] * [root[[2 ^ 3] * [3 ^ 2], 4] " +
                                "/ root[[2 ^ 3] * [3 ^ 2], 4]]"
                            explanation {
                                key = FractionRootsExplanation.SimplifyRationalizingTerm
                            }
                        }
                    }

                    step {
                        fromExpr =
                            "[9 / 2 root[2 * [3 ^ 2], 4]] * [root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 2], 4]]"
                        toExpr = "[9 root[[2 ^ 3] * [3 ^ 2], 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyFractions
                        }
                    }

                    step {
                        fromExpr =
                            "[9 root[[2 ^ 3] * [3 ^ 2], 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                        toExpr = "[9 root[72, 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                        explanation {
                            key = FractionRootsExplanation.SimplifyNumeratorAfterRationalization
                        }

                        step {
                            fromExpr =
                                "[9 root[[2 ^ 3] * [3 ^ 2], 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                            toExpr = "[9 root[8 * [3 ^ 2], 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                            }
                        }

                        step {
                            fromExpr = "[9 root[8 * [3 ^ 2], 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                            toExpr = "[9 root[8 * 9, 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                            }
                        }

                        step {
                            fromExpr = "[9 root[8 * 9, 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                            toExpr = "[9 root[72, 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                            explanation {
                                key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                            }
                        }
                    }

                    step {
                        fromExpr = "[9 root[72, 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                        toExpr = "[9 root[72, 4] / 12]"
                        explanation {
                            key = FractionRootsExplanation.SimplifyDenominatorAfterRationalization
                        }

                        step {
                            fromExpr = "[9 root[72, 4] / 2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                            toExpr = "[9 root[72, 4] / 2 root[[2 ^ 4] * [3 ^ 4], 4]]"
                            explanation {
                                key = FractionRootsExplanation.CollectRationalizingRadicals
                            }

                            step {
                                fromExpr = "2 root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]"
                                toExpr = "2 root[2 * [3 ^ 2] * [2 ^ 3] * [3 ^ 2], 4]"
                                explanation {
                                    key = IntegerRootsExplanation.MultiplyNthRoots
                                }
                            }

                            step {
                                fromExpr = "2 root[2 * [3 ^ 2] * [2 ^ 3] * [3 ^ 2], 4]"
                                toExpr = "2 root[[2 ^ 1 + 3] * [3 ^ 2] * [3 ^ 2], 4]"
                                explanation {
                                    key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                                }
                            }

                            step {
                                fromExpr = "2 root[[2 ^ 1 + 3] * [3 ^ 2] * [3 ^ 2], 4]"
                                toExpr = "2 root[[2 ^ 4] * [3 ^ 2] * [3 ^ 2], 4]"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                                }
                            }

                            step {
                                fromExpr = "2 root[[2 ^ 4] * [3 ^ 2] * [3 ^ 2], 4]"
                                toExpr = "2 root[[2 ^ 4] * [3 ^ 2 + 2], 4]"
                                explanation {
                                    key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                                }
                            }

                            step {
                                fromExpr = "2 root[[2 ^ 4] * [3 ^ 2 + 2], 4]"
                                toExpr = "2 root[[2 ^ 4] * [3 ^ 4], 4]"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                                }
                            }
                        }

                        step {
                            fromExpr = "[9 root[72, 4] / 2 root[[2 ^ 4] * [3 ^ 4], 4]]"
                            toExpr = "[9 root[72, 4] / 2 root[[(2 * 3) ^ 4], 4]]"
                            explanation {
                                key = IntegerRootsExplanation.CombineProductOfSamePowerUnderHigherRoot
                            }
                        }

                        step {
                            fromExpr = "[9 root[72, 4] / 2 root[[(2 * 3) ^ 4], 4]]"
                            toExpr = "[9 root[72, 4] / 2 * <. 2 * 3 .>]"
                            explanation {
                                key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                            }
                        }

                        step {
                            fromExpr = "[9 root[72, 4] / 2 * 2 * 3]"
                            toExpr = "[9 root[72, 4] / 12]"
                            explanation {
                                key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                            }
                        }
                    }
                }

                step {
                    fromExpr = "[9 root[72, 4] / 12]"
                    toExpr = "[3 root[72, 4] / 4]"
                    explanation {
                        key = FractionArithmeticExplanation.SimplifyFraction
                    }

                    step {
                        fromExpr = "[9 root[72, 4] / 12]"
                        toExpr = "[3 * 3 root[72, 4] / 3 * 4]"
                        explanation {
                            key = FractionArithmeticExplanation.FindCommonFactorInFraction
                        }
                    }

                    step {
                        fromExpr = "[3 * 3 root[72, 4] / 3 * 4]"
                        toExpr = "[3 root[72, 4] / 4]"
                        explanation {
                            key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                        }
                    }
                }
            }
        }

    @Test
    fun testRationalizeHigherOrderRoot2() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[9 / 2 root[11, 4]]"

            check {
                fromExpr = "[9 / 2 root[11, 4]]"
                toExpr = "[9 root[1331, 4] / 22]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }

                step {
                    fromExpr = "[9 / 2 root[11, 4]]"
                    toExpr = "[9 / 2 root[11, 4]] * [root[[11 ^ 3], 4] / root[[11 ^ 3], 4]]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeHigherOrderRoot
                    }

                    step {
                        fromExpr = "[9 / 2 root[11, 4]]"
                        toExpr = "[9 / 2 root[11, 4]] * [root[[11 ^ 4 - 1], 4] / root[[11 ^ 4 - 1], 4]]"
                        explanation {
                            key = FractionRootsExplanation.HigherOrderRationalizingTerm
                        }
                    }

                    step {
                        fromExpr = "[9 / 2 root[11, 4]] * [root[[11 ^ 4 - 1], 4] / root[[11 ^ 4 - 1], 4]]"
                        toExpr = "[9 / 2 root[11, 4]] * [root[[11 ^ 3], 4] / root[[11 ^ 3], 4]]"
                        explanation {
                            key = FractionRootsExplanation.SimplifyRationalizingTerm
                        }
                    }
                }

                step {
                    fromExpr = "[9 / 2 root[11, 4]] * [root[[11 ^ 3], 4] / root[[11 ^ 3], 4]]"
                    toExpr = "[9 root[[11 ^ 3], 4] / 2 root[11, 4] * root[[11 ^ 3], 4]]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    fromExpr = "[9 root[[11 ^ 3], 4] / 2 root[11, 4] * root[[11 ^ 3], 4]]"
                    toExpr = "[9 root[1331, 4] / 2 root[11, 4] * root[[11 ^ 3], 4]]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                    }
                }

                step {
                    fromExpr = "[9 root[1331, 4] / 2 root[11, 4] * root[[11 ^ 3], 4]]"
                    toExpr = "[9 root[1331, 4] / 22]"
                    explanation {
                        key = FractionRootsExplanation.SimplifyDenominatorAfterRationalization
                    }

                    step {
                        fromExpr = "[9 root[1331, 4] / 2 root[11, 4] * root[[11 ^ 3], 4]]"
                        toExpr = "[9 root[1331, 4] / 2 root[[11 ^ 4], 4]]"
                        explanation {
                            key = FractionRootsExplanation.CollectRationalizingRadicals
                        }

                        step {
                            fromExpr = "2 root[11, 4] * root[[11 ^ 3], 4]"
                            toExpr = "2 root[11 * [11 ^ 3], 4]"
                            explanation {
                                key = IntegerRootsExplanation.MultiplyNthRoots
                            }
                        }

                        step {
                            fromExpr = "2 root[11 * [11 ^ 3], 4]"
                            toExpr = "2 root[[11 ^ 1 + 3], 4]"
                            explanation {
                                key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                            }
                        }

                        step {
                            fromExpr = "2 root[[11 ^ 1 + 3], 4]"
                            toExpr = "2 root[[11 ^ 4], 4]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                            }
                        }
                    }

                    step {
                        fromExpr = "[9 root[1331, 4] / 2 root[[11 ^ 4], 4]]"
                        toExpr = "[9 root[1331, 4] / 2 * 11]"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                        }
                    }

                    step {
                        fromExpr = "[9 root[1331, 4] / 2 * 11]"
                        toExpr = "[9 root[1331, 4] / 22]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                        }
                    }
                }
            }
        }
}

class FractionRootsRationalization {
    @Test
    fun testRationalizeHigherOrderRootWithPrimeRadicand() =
        testMethod {
            method = FractionRootsPlans.RationalizeDenominators
            inputExpr = "[1 / root[2, 3]]"

            check {
                fromExpr = "[1 / root[2, 3]]"
                toExpr = "[root[4, 3] / 2]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }

                step {
                    fromExpr = "[1 / root[2, 3]]"
                    toExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeHigherOrderRoot
                    }

                    step {
                        fromExpr = "[1 / root[2, 3]]"
                        toExpr = "[1 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                        explanation {
                            key = FractionRootsExplanation.HigherOrderRationalizingTerm
                        }
                    }

                    step {
                        fromExpr = "[1 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                        toExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                        explanation {
                            key = FractionRootsExplanation.SimplifyRationalizingTerm
                        }

                        step {
                            fromExpr = "[1 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                            toExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 3 - 1], 3]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }

                        step {
                            fromExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 3 - 1], 3]]"
                            toExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }
                    }
                }

                step {
                    fromExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                    toExpr = "[1 root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    fromExpr = "[1 root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    toExpr = "[root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    explanation {
                        key = FractionRootsExplanation.SimplifyNumeratorAfterRationalization
                    }

                    step {
                        fromExpr = "[1 root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        toExpr = "[1 root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                        }
                    }

                    step {
                        fromExpr = "[1 root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        toExpr = "[root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                        }
                    }
                }

                step {
                    fromExpr = "[root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    toExpr = "[root[4, 3] / 2]"
                    explanation {
                        key = FractionRootsExplanation.SimplifyDenominatorAfterRationalization
                    }

                    step {
                        fromExpr = "[root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        toExpr = "[root[4, 3] / root[[2 ^ 3], 3]]"
                        explanation {
                            key = FractionRootsExplanation.CollectRationalizingRadicals
                        }

                        step {
                            fromExpr = "root[2, 3] * root[[2 ^ 2], 3]"
                            toExpr = "root[2 * [2 ^ 2], 3]"
                            explanation {
                                key = IntegerRootsExplanation.MultiplyNthRoots
                            }
                        }

                        step {
                            fromExpr = "root[2 * [2 ^ 2], 3]"
                            toExpr = "root[[2 ^ 1 + 2], 3]"
                            explanation {
                                key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                            }
                        }

                        step {
                            fromExpr = "root[[2 ^ 1 + 2], 3]"
                            toExpr = "root[[2 ^ 3], 3]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                            }
                        }
                    }

                    step {
                        fromExpr = "[root[4, 3] / root[[2 ^ 3], 3]]"
                        toExpr = "[root[4, 3] / 2]"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                        }
                    }
                }
            }
        }

    @Test
    fun testRationalizeHigherOrderRootWithPowerOfPrimeRadicand() =
        testMethod {
            method = FractionRootsPlans.RationalizeDenominators
            inputExpr = "[1 / root[[13^2], 3]]"

            check {
                fromExpr = "[1 / root[[13 ^ 2], 3]]"
                toExpr = "[root[13, 3] / 13]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }

                step {
                    fromExpr = "[1 / root[[13 ^ 2], 3]]"
                    toExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[13, 3]]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeHigherOrderRoot
                    }

                    step {
                        fromExpr = "[1 / root[[13 ^ 2], 3]]"
                        toExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 3 - 2], 3] / root[[13 ^ 3 - 2], 3]]"
                        explanation {
                            key = FractionRootsExplanation.HigherOrderRationalizingTerm
                        }
                    }

                    step {
                        fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 3 - 2], 3] / root[[13 ^ 3 - 2], 3]]"
                        toExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[13, 3]]"
                        explanation {
                            key = FractionRootsExplanation.SimplifyRationalizingTerm
                        }

                        step {
                            fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 3 - 2], 3] / root[[13 ^ 3 - 2], 3]]"
                            toExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 1], 3] / root[[13 ^ 3 - 2], 3]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }

                        step {
                            fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 1], 3] / root[[13 ^ 3 - 2], 3]]"
                            toExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 1], 3] / root[[13 ^ 1], 3]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }

                        step {
                            fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 1], 3] / root[[13 ^ 1], 3]]"
                            toExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[[13 ^ 1], 3]]"
                            explanation {
                                key = GeneralExplanation.SimplifyExpressionToThePowerOfOne
                            }
                        }

                        step {
                            fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[[13 ^ 1], 3]]"
                            toExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[13, 3]]"
                            explanation {
                                key = GeneralExplanation.SimplifyExpressionToThePowerOfOne
                            }
                        }
                    }
                }

                step {
                    fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[13, 3]]"
                    toExpr = "[1 root[13, 3] / root[[13 ^ 2], 3] * root[13, 3]]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    fromExpr = "[1 root[13, 3] / root[[13 ^ 2], 3] * root[13, 3]]"
                    toExpr = "[root[13, 3] / root[[13 ^ 2], 3] * root[13, 3]]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                    }

                    step {
                        fromExpr = "1 root[13, 3]"
                        toExpr = "root[13, 3]"
                        explanation {
                            key = GeneralExplanation.RemoveUnitaryCoefficient
                        }
                    }
                }

                step {
                    fromExpr = "[root[13, 3] / root[[13 ^ 2], 3] * root[13, 3]]"
                    toExpr = "[root[13, 3] / 13]"
                    explanation {
                        key = FractionRootsExplanation.SimplifyDenominatorAfterRationalization
                    }

                    step {
                        fromExpr = "[root[13, 3] / root[[13 ^ 2], 3] * root[13, 3]]"
                        toExpr = "[root[13, 3] / root[[13 ^ 3], 3]]"
                        explanation {
                            key = FractionRootsExplanation.CollectRationalizingRadicals
                        }

                        step {
                            fromExpr = "root[[13 ^ 2], 3] * root[13, 3]"
                            toExpr = "root[[13 ^ 2] * 13, 3]"
                            explanation {
                                key = IntegerRootsExplanation.MultiplyNthRoots
                            }
                        }

                        step {
                            fromExpr = "root[[13 ^ 2] * 13, 3]"
                            toExpr = "root[[13 ^ 2 + 1], 3]"
                            explanation {
                                key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                            }
                        }

                        step {
                            fromExpr = "root[[13 ^ 2 + 1], 3]"
                            toExpr = "root[[13 ^ 3], 3]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                            }
                        }
                    }

                    step {
                        fromExpr = "[root[13, 3] / root[[13 ^ 3], 3]]"
                        toExpr = "[root[13, 3] / 13]"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                        }
                    }
                }
            }
        }
}

class CollectRationalizingRadicalsTest {
    @Test
    fun testCollectRationalizingRadicalsTest1() =
        testMethod {
            method = FractionRootsPlans.CollectRationalizingRadicals
            inputExpr = "root[2 * 19, 3] * root[ [2^2] * [19^2], 3]"

            check {
                fromExpr = "root[2 * 19, 3] * root[ [2^2] * [19^2], 3]"
                toExpr = "root[[2^3] * [19^3], 3]"
            }
        }

    @Test
    fun testCollectRationalizingRadicalsTest2() =
        testMethod {
            method = FractionRootsPlans.CollectRationalizingRadicals
            inputExpr = "root[19, 3] * root[ [19^2], 3]"

            check {
                fromExpr = "root[19, 3] * root[ [19^2], 3]"
                toExpr = "root[[19^3], 3]"
            }
        }
}
