package methods.constantexpressions

import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.equations.EquationsExplanation
import methods.equationsystems.EquationSystemsExplanation
import methods.expand.ExpandExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.fractionroots.FractionRootsExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerroots.IntegerRootsExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength", "ktlint:standard:max-line-length")
class SimplifyingRootsTest {
    @Test
    fun testResultSimplifyPowerOfRoot() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[(root[12, 5]) ^ 4]"

            check {
                fromExpr = "[(root[12, 5]) ^ 4]"
                toExpr = "2 root[648, 5]"
            }
        }

    @Test
    fun testResultSimplifyPowerOfRootWithCoefficients() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[(2 sqrt[5]) ^ 3]"

            check {
                fromExpr = "[(2 sqrt[5]) ^ 3]"
                toExpr = "40 sqrt[5]"
            }
        }

    @Test
    fun testResultPowerOfBinomialContainingRoots() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[(2 sqrt[6] + 3 sqrt[2]) ^ 2]"

            check {
                fromExpr = "[(2 sqrt[6] + 3 sqrt[2]) ^ 2]"
                toExpr = "42 + 24 sqrt[3]"

                // Currently has 10 steps!
            }
        }

    @Test
    fun testSimplifyRootOfRoot() =
        testMethod {
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
    fun testRootOfRootWithCoefficient() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "root[3 sqrt[4], 3]"

            check {
                toExpr = "root[6, 3]"

                step {
                    toExpr = "root[3 * 2, 3]"

                    step {
                        fromExpr = "sqrt[4]"
                        toExpr = "2"
                    }
                }

                step {
                    toExpr = "root[6, 3]"
                }
            }
        }

    @Test
    fun testSimplifyRootOfRootWithCoefficient() =
        testMethod {
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
    fun testHigherOrderRootSimplify() =
        testMethod {
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
    fun testCancelPowersAndEvaluate1() =
        testMethod {
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
    fun testCancelPowersAndEvaluate2() =
        testMethod {
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
                        key = IntegerRootsExplanation.SplitAndCancelRootOfPower
                    }
                }
            }
        }

    @Test
    fun testSamePowerAsRoot() =
        testMethod {
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
    fun `test nthRoot of (-a)^n, where n is even`() =
        testMethod {
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
    fun `test nthRoot of (-1)^n, where n is even`() =
        testMethod {
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
    fun `test don't cancel even root index & base negative`() =
        testMethod {
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
                        key = IntegerRootsExplanation.SplitAndCancelRootOfPower
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
    fun testSplitAndSimplifyIntegerPowerUnderRoot() =
        testMethod {
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
                        key = IntegerRootsExplanation.SplitAndCancelRootOfPower
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
    fun testRootCancelsWithPowerOfRadicand() =
        testMethod {
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
}

class SmartFactorizationUnderRoot {
    @Test
    fun testSimplifyCancellablePowerWithProductBaseUnderHigherOrderRoot() =
        testMethod {
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
    fun testSimplifyRootOfSum() =
        testMethod {
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
                            key = IntegerRootsExplanation.SimplifyIntegerRootToInteger
                        }
                    }
                }
            }
        }

    @Test
    fun `test simplifying root of integer minus surd`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "sqrt[11 - 6 sqrt[2]]"

            check {
                fromExpr = "sqrt[11 - 6 sqrt[2]]"
                toExpr = "3 - sqrt[2]"
                explanation {
                    key = IntegerRootsExplanation.SimplifySquareRootOfIntegerPlusSurd
                }

                step {
                    fromExpr = "sqrt[11 - 6 sqrt[2]]"
                    toExpr = "sqrt[[(3 - sqrt[2]) ^ 2]]"
                    explanation {
                        key = IntegerRootsExplanation.WriteIntegerPlusSquareRootAsSquare
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[(x + y sqrt[2]) ^ 2] = 11 - 6 sqrt[2]"
                        explanation {
                            key = IntegerRootsExplanation.WriteEquationInXAndYAndSolveItForFactoringIntegerPlusSurd
                        }

                        step {
                            fromExpr = "[(x + y sqrt[2]) ^ 2] = 11 - 6 sqrt[2]"
                            toExpr = "[x ^ 2] + 2 sqrt[2] * x y + 2 [y ^ 2] = 11 - 6 sqrt[2]"
                            explanation {
                                key = PolynomialsExplanation.ExpandPolynomialExpression
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 2 sqrt[2] * x y + 2 [y ^ 2] = 11 - 6 sqrt[2]"
                            toExpr = "[x ^ 2] + 2 [y ^ 2] = 11 AND 2 sqrt[2] * x y = -6 sqrt[2]"
                            explanation {
                                key = EquationsExplanation.SplitEquationWithRationalVariables
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 2 [y ^ 2] = 11 AND 2 sqrt[2] * x y = -6 sqrt[2]"
                            toExpr = "[x ^ 2] + 2 [y ^ 2] = 11 AND 2 x y = -6"
                            explanation {
                                key = methods.solvable.EquationsExplanation.CancelCommonFactorOnBothSides
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 2 [y ^ 2] = 11 AND 2 x y = -6"
                            toExpr = "[x ^ 2] + 2 [y ^ 2] = 11 AND 2 x y = 2 * (-3)"
                            explanation {
                                key = methods.solvable.EquationsExplanation.FindCommonIntegerFactorOnBothSides
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 2 [y ^ 2] = 11 AND 2 x y = 2 * (-3)"
                            toExpr = "[x ^ 2] + 2 [y ^ 2] = 11 AND x y = -3"
                            explanation {
                                key = methods.solvable.EquationsExplanation.CancelCommonFactorOnBothSides
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 2 [y ^ 2] = 11 AND x y = -3"
                            toExpr = "x = 3 AND y = -1"
                            explanation {
                                key = EquationSystemsExplanation.GuessIntegerSolutionsOfSystemContainingXYEqualsInteger
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[(3 - sqrt[2]) ^ 2]"
                        explanation {
                            key = IntegerRootsExplanation.SubstituteXAndYorFactoringIntegerPlusSurd
                        }
                    }
                }

                step {
                    fromExpr = "sqrt[[(3 - sqrt[2]) ^ 2]]"
                    toExpr = "3 - sqrt[2]"
                    explanation {
                        key = GeneralExplanation.CancelRootIndexAndExponent
                    }
                }
            }
        }

    @Test
    fun `test simplifying root of integer plus surd`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "sqrt[28 + 10 sqrt[3]]"

            check {
                fromExpr = "sqrt[28 + 10 sqrt[3]]"
                toExpr = "5 + sqrt[3]"
                explanation {
                    key = IntegerRootsExplanation.SimplifySquareRootOfIntegerPlusSurd
                }

                step {
                    fromExpr = "sqrt[28 + 10 sqrt[3]]"
                    toExpr = "sqrt[[(5 + sqrt[3]) ^ 2]]"
                    explanation {
                        key = IntegerRootsExplanation.WriteIntegerPlusSquareRootAsSquare
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[(x + y sqrt[3]) ^ 2] = 28 + 10 sqrt[3]"
                        explanation {
                            key = IntegerRootsExplanation.WriteEquationInXAndYAndSolveItForFactoringIntegerPlusSurd
                        }

                        step {
                            fromExpr = "[(x + y sqrt[3]) ^ 2] = 28 + 10 sqrt[3]"
                            toExpr = "[x ^ 2] + 2 sqrt[3] * x y + 3 [y ^ 2] = 28 + 10 sqrt[3]"
                            explanation {
                                key = PolynomialsExplanation.ExpandPolynomialExpression
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 2 sqrt[3] * x y + 3 [y ^ 2] = 28 + 10 sqrt[3]"
                            toExpr = "[x ^ 2] + 3 [y ^ 2] = 28 AND 2 sqrt[3] * x y = 10 sqrt[3]"
                            explanation {
                                key = EquationsExplanation.SplitEquationWithRationalVariables
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 3 [y ^ 2] = 28 AND 2 sqrt[3] * x y = 10 sqrt[3]"
                            toExpr = "[x ^ 2] + 3 [y ^ 2] = 28 AND 2 x y = 10"
                            explanation {
                                key = methods.solvable.EquationsExplanation.CancelCommonFactorOnBothSides
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 3 [y ^ 2] = 28 AND 2 x y = 10"
                            toExpr = "[x ^ 2] + 3 [y ^ 2] = 28 AND 2 x y = 2 * 5"
                            explanation {
                                key = methods.solvable.EquationsExplanation.FindCommonIntegerFactorOnBothSides
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 3 [y ^ 2] = 28 AND 2 x y = 2 * 5"
                            toExpr = "[x ^ 2] + 3 [y ^ 2] = 28 AND x y = 5"
                            explanation {
                                key = methods.solvable.EquationsExplanation.CancelCommonFactorOnBothSides
                            }
                        }

                        step {
                            fromExpr = "[x ^ 2] + 3 [y ^ 2] = 28 AND x y = 5"
                            toExpr = "x = 5 AND y = 1"
                            explanation {
                                key = EquationSystemsExplanation.GuessIntegerSolutionsOfSystemContainingXYEqualsInteger
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[(5 + sqrt[3]) ^ 2]"
                        explanation {
                            key = IntegerRootsExplanation.SubstituteXAndYorFactoringIntegerPlusSurd
                        }
                    }
                }

                step {
                    fromExpr = "sqrt[[(5 + sqrt[3]) ^ 2]]"
                    toExpr = "5 + sqrt[3]"
                    explanation {
                        key = GeneralExplanation.CancelRootIndexAndExponent
                    }
                }
            }
        }
}
