package methods.constantexpressions

import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.expand.ExpandExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.fractionroots.FractionRootsExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerroots.IntegerRootsExplanation
import org.junit.jupiter.api.Test

class SimplifyingRootsTest {

    @Test
    fun testResultSimplifyPowerOfRoot() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[(root[12, 5]) ^ 4]"

        check {
            fromExpr = "[(root[12, 5]) ^ 4]"
            toExpr = "2 root[648, 5]"
        }
    }

    @Test
    fun testResultSimplifyPowerOfRootWithCoefficients() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[(2 sqrt[5]) ^ 3]"

        check {
            fromExpr = "[(2 sqrt[5]) ^ 3]"
            toExpr = "40 sqrt[5]"
        }
    }

    @Test
    fun testResultPowerOfBinomialContainingRoots() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[(2 sqrt[6] + 3 sqrt[2]) ^ 2]"

        check {
            fromExpr = "[(2 sqrt[6] + 3 sqrt[2]) ^ 2]"
            toExpr = "42 + 24 sqrt[3]"

            // Currently has 10 steps!
        }
    }

    @Test
    fun testSimplifyRootOfRoot() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "sqrt[root[12, 4]]"

        check {
            fromExpr = "sqrt[root[12, 4]]"
            toExpr = "root[12, 8]"

            step {
                fromExpr = "sqrt[root[12, 4]]"
                toExpr = "root[12, 2 * 4]"
            }

            step {
                fromExpr = "root[12, 2 * 4]"
                toExpr = "root[12, 8]"
            }
        }
    }

    @Test
    fun testSimplifyRootOfRootWithCoefficient() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[2 sqrt[6], 3]"

        check {
            step {
                toExpr = "root[sqrt[24], 3]"

                step {
                    fromExpr = "2 sqrt[6]"
                    toExpr = "sqrt[[2 ^ 2] * 6]"
                }

                step {
                    fromExpr = "sqrt[[2 ^ 2] * 6]"
                    toExpr = "sqrt[4 * 6]"
                }

                step {
                    fromExpr = "sqrt[4 * 6]"
                    toExpr = "sqrt[24]"
                }
            }

            step {
                fromExpr = "root[sqrt[24], 3]"
                toExpr = "root[24, 3 * 2]"
            }

            step {
                fromExpr = "root[24, 3 * 2]"
                toExpr = "root[24, 6]"
            }
        }
    }

    @Test
    fun testHigherOrderRootSimplify() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[[5 / 12], 4]"

        check {
            toExpr = "[root[540, 4] / 6]"

            step {
                toExpr = "[root[5, 4] / root[12, 4]]"
                explanation {
                    key = FractionRootsExplanation.DistributeRadicalOverFraction
                }
            }

            step {
                toExpr = "[root[540, 4] / 6]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }

                step {
                    toExpr = "[root[5, 4] / root[[2 ^ 2] * 3, 4]]"
                    explanation {
                        key = FractionRootsExplanation.FactorizeHigherOrderRadicand
                    }
                }

                step {
                    toExpr = "[root[5, 4] / root[[2 ^ 2] * 3, 4]] " +
                        "* [root[[2 ^ 2] * [3 ^ 3], 4] / root[[2 ^ 2] * [3 ^ 3], 4]]"
                }

                step {
                    toExpr =
                        "[root[5, 4] * root[[2 ^ 2] * [3 ^ 3], 4] / root[[2 ^ 2] * 3, 4] * root[[2 ^ 2] * [3 ^ 3], 4]]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    toExpr = "[root[540, 4] / root[[2 ^ 2] * 3, 4] * root[[2 ^ 2] * [3 ^ 3], 4]]"
                }

                step {
                    toExpr = "[root[540, 4] / 6]"
                }
            }
        }
    }
}

class SimplifyIntegerPowerUnderRoot {

    @Test
    fun testCancelPowersAndEvaluate1() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[[7^4], 6]"

        check {
            toExpr = "root[49, 3]"

            step {
                toExpr = "root[[7 ^ 2], 3]"

                step {
                    toExpr = "root[[7 ^ 2 * 2], 3 * 2]"
                }

                step {
                    toExpr = "root[[7 ^ 2], 3]"
                }
            }

            step {
                toExpr = "root[49, 3]"
            }
        }
    }

    @Test
    fun testCancelPowersAndEvaluate2() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[[7^6], 4]"

        check {
            fromExpr = "root[[7 ^ 6], 4]"
            toExpr = "7 sqrt[7]"

            step {
                fromExpr = "root[[7 ^ 6], 4]"
                toExpr = "sqrt[[7 ^ 3]]"
                explanation {
                    key = IntegerRootsExplanation.RewriteAndCancelPowerUnderRoot
                }
            }

            step {
                fromExpr = "sqrt[[7 ^ 3]]"
                toExpr = "7 sqrt[7]"
                explanation {
                    key = IntegerRootsExplanation.SplitRootsAndCancelRootsOfPowers
                }
            }
        }
    }

    @Test
    fun testSamePowerAsRoot() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[[24 ^ 3], 3]"

        check {
            fromExpr = "root[[24 ^ 3], 3]"
            toExpr = "24"
            explanation {
                key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
            }
        }
    }

    @Test
    fun `test nthRoot of (-a)^n, where n is even`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[ [(-2)^4], 4]"

        check {
            fromExpr = "root[[(-2) ^ 4], 4]"
            toExpr = "2"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "root[[(-2) ^ 4], 4]"
                toExpr = "root[[2 ^ 4], 4]"
                explanation {
                    key = GeneralExplanation.SimplifyEvenPowerOfNegative
                }
            }

            step {
                fromExpr = "root[[2 ^ 4], 4]"
                toExpr = "2"
                explanation {
                    key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                }
            }
        }
    }

    @Test
    fun `test nthRoot of (-1)^n, where n is even`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[[(-1) ^ 4], 4]"

        check {
            fromExpr = "root[[(-1) ^ 4], 4]"
            toExpr = "1"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "root[[(-1) ^ 4], 4]"
                toExpr = "root[[1 ^ 4], 4]"
                explanation {
                    key = GeneralExplanation.SimplifyEvenPowerOfNegative
                }
            }

            step {
                fromExpr = "root[[1 ^ 4], 4]"
                toExpr = "1"
                explanation {
                    key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                }
            }
        }
    }

    @Test
    fun `test don't cancel even root index & base negative`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[[(-2) ^ 5 * 2], 3 * 2]"

        check {
            fromExpr = "root[[(-2) ^ 5 * 2], 3 * 2]"
            toExpr = "2 root[4, 3]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "root[[(-2) ^ 5 * 2], 3 * 2]"
                toExpr = "root[[(-2) ^ 5 * 2], 6]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                }
            }

            step {
                fromExpr = "root[[(-2) ^ 5 * 2], 6]"
                toExpr = "root[[(-2) ^ 10], 6]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                }
            }

            step {
                fromExpr = "root[[(-2) ^ 10], 6]"
                toExpr = "root[[2 ^ 5], 3]"
                explanation {
                    key = IntegerRootsExplanation.RewriteAndCancelPowerUnderRoot
                }
            }

            step {
                fromExpr = "root[[2 ^ 5], 3]"
                toExpr = "2 root[[2 ^ 2], 3]"
                explanation {
                    key = IntegerRootsExplanation.SplitRootsAndCancelRootsOfPowers
                }
            }

            step {
                fromExpr = "2 root[[2 ^ 2], 3]"
                toExpr = "2 root[4, 3]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                }
            }
        }
    }

    @Test
    fun testSplitAndSimplifyIntegerPowerUnderRoot() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[[24^5], 3]"

        check {
            fromExpr = "root[[24 ^ 5], 3]"
            toExpr = "96 root[9, 3]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "root[[24 ^ 5], 3]"
                toExpr = "24 root[[24 ^ 2], 3]"
                explanation {
                    key = IntegerRootsExplanation.SplitRootsAndCancelRootsOfPowers
                }
            }

            step {
                fromExpr = "24 root[[24 ^ 2], 3]"
                toExpr = "24 * <. 4 root[9, 3] .>"
                explanation {
                    key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                }
            }

            step {
                fromExpr = "24 * 4 root[9, 3]"
                toExpr = "96 root[9, 3]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                }
            }
        }
    }

    @Test
    fun testRootCancelsWithPowerOfRadicand() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[[24 ^ 6], 3]"

        check {
            fromExpr = "root[[24 ^ 6], 3]"
            toExpr = "576"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "root[[24 ^ 6], 3]"
                toExpr = "[24 ^ 2]"
                explanation {
                    key = IntegerRootsExplanation.RewriteAndCancelPowerUnderRoot
                }
            }

            step {
                fromExpr = "[24 ^ 2]"
                toExpr = "576"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                }
            }
        }
    }

    @Test
    fun testSimplificationOfIntegerPowerUnderHigherOrderRoot() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[[24 ^ 5], 6]"

        check {
            fromExpr = "root[[24 ^ 5], 6]"
            toExpr = "4 root[1944, 6]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "root[[24 ^ 5], 6]"
                toExpr = "sqrt[32] * root[243, 6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                }
            }

            step {
                fromExpr = "sqrt[32] * root[243, 6]"
                toExpr = "4 sqrt[2] * root[243, 6]"
                explanation {
                    // I am not sure about this key
                    key = ConstantExpressionsExplanation.SimplifyRootsInExpression
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

class SmartFactorizationUnderRoot {
    @Test
    fun testSimplifyPerfectSquareUnderEvenOrderHigherRoot() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[9, 4]"

        check {
            toExpr = "sqrt[3]"
            explanation {
                key = IntegerRootsExplanation.SimplifyIntegerRoot
            }

            step {
                fromExpr = "root[9, 4]"
                toExpr = "root[[3 ^ 2], 4]"
                explanation {
                    key = IntegerRootsExplanation.FactorizeIntegerUnderRoot
                }
            }

            step {
                fromExpr = "root[[3 ^ 2], 4]"
                toExpr = "sqrt[3]"
                explanation {
                    key = IntegerRootsExplanation.RewriteAndCancelPowerUnderRoot
                }

                step {
                    fromExpr = "root[[3 ^ 2], 4]"
                    toExpr = "root[[3 ^ 2], 2 * 2]"
                    explanation {
                        key = GeneralExplanation.RewritePowerUnderRoot
                    }
                }

                step {
                    fromExpr = "root[[3 ^ 2], 2 * 2]"
                    toExpr = "sqrt[3]"
                    explanation {
                        key = GeneralExplanation.CancelRootIndexAndExponent
                    }
                }
            }
        }
    }

    @Test
    fun testSimplifyMultipleFactorsSamePowerUnderHigherOrderRoot() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[7776, 10]"

        check {
            toExpr = "sqrt[6]"
            explanation {
                key = IntegerRootsExplanation.SimplifyIntegerRoot
            }

            step {
                fromExpr = "root[7776, 10]"
                toExpr = "root[[2 ^ 5] * [3 ^ 5], 10]"
                explanation {
                    key = IntegerRootsExplanation.FactorizeIntegerUnderRoot
                }
            }

            step {
                fromExpr = "root[[2 ^ 5] * [3 ^ 5], 10]"
                toExpr = "root[[2 ^ 5], 10] * root[[3 ^ 5], 10]"
                explanation {
                    key = IntegerRootsExplanation.SplitRootOfProduct
                }
            }

            step {
                fromExpr = "root[[2 ^ 5], 10] * root[[3 ^ 5], 10]"
                toExpr = "sqrt[2] * sqrt[3]"
                explanation {
                    key = IntegerRootsExplanation.CancelAllRootsOfPowers
                }
            }

            step {
                fromExpr = "sqrt[2] * sqrt[3]"
                toExpr = "sqrt[6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }
        }
    }

    @Test
    fun testSimplifyCancellablePowerWithProductBaseUnderHigherOrderRoot() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[ [(2*3) ^ 5], 10]"

        check {
            toExpr = "sqrt[6]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "root[[(2 * 3) ^ 5], 10]"
                toExpr = "root[[6 ^ 5], 10]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }
            }

            step {
                fromExpr = "root[[6 ^ 5], 10]"
                toExpr = "sqrt[6]"
                explanation {
                    key = IntegerRootsExplanation.RewriteAndCancelPowerUnderRoot
                }
            }
        }
    }

    @Test
    fun testSimplifyRootOfSum() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "sqrt[4 (sqrt[2] + 2) + 5 sqrt[2] + 10]"

        check {
            fromExpr = "sqrt[4 (sqrt[2] + 2) + 5 sqrt[2] + 10]"
            toExpr = "3 sqrt[sqrt[2] + 2]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "sqrt[4 (sqrt[2] + 2) + 5 sqrt[2] + 10]"
                toExpr = "sqrt[<.4 sqrt[2] + 8.> + 5 sqrt[2] + 10]"
                explanation {
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }
            }

            step {
                fromExpr = "sqrt[4 sqrt[2] + 8 + 5 sqrt[2] + 10]"
                toExpr = "sqrt[9 sqrt[2] + 8 + 10]"
                explanation {
                    key = CollectingExplanation.CollectLikeRootsAndSimplify
                }
            }

            step {
                fromExpr = "sqrt[9 sqrt[2] + 8 + 10]"
                toExpr = "sqrt[9 sqrt[2] + 18]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }

            step {
                fromExpr = "sqrt[9 sqrt[2] + 18]"
                toExpr = "3 sqrt[sqrt[2] + 2]"
                explanation {
                    key = IntegerRootsExplanation.SimplifySquareRootWithASquareFactorRadicand
                }

                step {
                    fromExpr = "sqrt[9 sqrt[2] + 18]"
                    toExpr = "sqrt[9 (sqrt[2] + 2)]"
                    explanation {
                        key = IntegerRootsExplanation.FactorGreatestCommonSquareIntegerFactor
                    }
                }

                step {
                    fromExpr = "sqrt[9 (sqrt[2] + 2)]"
                    toExpr = "sqrt[9] * sqrt[sqrt[2] + 2]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "sqrt[9] * sqrt[sqrt[2] + 2]"
                    toExpr = "3 sqrt[sqrt[2] + 2]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRoot
                    }
                }
            }
        }
    }
}
