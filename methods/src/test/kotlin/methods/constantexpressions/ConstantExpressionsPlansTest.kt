package methods.constantexpressions

import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.decimals.DecimalsExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.fractionroots.FractionRootsExplanation
import methods.fractionroots.FractionRootsPlans
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerrationalexponents.IntegerRationalExponentsExplanation
import methods.integerroots.IntegerRootsExplanation
import org.junit.jupiter.api.Test

class ConstantExpressionsPlansTest {

    @Test
    fun simpleTest() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[1 / 3] + [2 / 3] * [1 / 2]"

        check {
            fromExpr = "[1 / 3] + [2 / 3] * [1 / 2]"
            toExpr = "[2 / 3]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "[1 / 3] + [2 / 3] * [1 / 2]"
                toExpr = "[1 / 3] + [1 / 3]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                }
            }

            step {
                fromExpr = "[1 / 3] + [1 / 3]"
                toExpr = "[2 / 3]"
                explanation {
                    key = FractionArithmeticExplanation.AddFractions
                }
            }
        }
    }

    @Test
    fun testMultiplyAndSimplify() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[14 / 4] * [4 / 21]"

        check {
            toExpr = "[2 / 3]"

            step { toExpr = "[7 / 2] * [ 4 / 21]" }

            step {
                toExpr = "[2 / 3]"

                step { toExpr = "[7 * 4 / 2 * 21]" }

                step { toExpr = "[2 / 3]" }
            }
        }
    }

    @Test
    fun testWithNegativesInFractions() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[-1 / 6] * [2 / -5]"

        check {
            step { toExpr = "(-[1 / 6]) * [2 / -5]" }
            step { toExpr = "(-[1 / 6]) (-[2 / 5])" }
            step { toExpr = "[1 / 6] * [2 / 5]" }
            step { toExpr = "[1 / 15]" }
        }
    }

    @Test
    fun testWithMoreNegativesInFractions() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "(-[1 / 3]) * [-1 / 4] * [3 / -2]"

        check {
            step { toExpr = "(-[1 / 3]) (-[1 / 4]) * [3 / -2]" }
            step { toExpr = "(-[1 / 3]) (-[1 / 4]) (-[3 / 2])" }
            step {
                step { toExpr = "[1 / 3] * [1 / 4] (-[3 / 2])" }
                step { toExpr = "-[1 / 3] * [1 / 4] * [3 / 2]" }
            }
            step { toExpr = "-[1 / 8]" }
        }
    }

    @Test
    fun testDividingTwice() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "3 : 4 : 5"

        check {
            step {
                step { toExpr = "[3 / 4] : 5" }
                step { toExpr = "[3 / 4] * [1 / 5]" }
            }

            step { toExpr = "[3 / 20]" }
        }
    }

    @Test
    fun testDividingFractions() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[5 / 6] : [3 / 4]"

        check {
            step { toExpr = "[5 / 6] * [4 / 3]" }
            step { toExpr = "[10 / 9]" }
        }
    }

    @Test
    fun testDividingWithNegatives() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "3 : (-5)"

        check {
            step { toExpr = "[3 / -5]" }
            step { toExpr = "-[3 / 5]" }
        }
    }

    @Test
    fun testFractionExponent() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[(-[1 / 2]) ^ 3] + [([2 / 3]) ^ -2]"

        check {
            step { toExpr = "- [1 / 8] + [([2/3])^-2]" }
            step { toExpr = "- [1 / 8] + [9 / 4]" }
            step { toExpr = "[17 / 8]" }
        }
    }

    @Test
    fun testNegativeExponentsOfIntegers() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[3 ^ -1] - [3 ^ -2]"

        check {
            step { toExpr = "[1 / 3] - [3 ^ -2]" }
            step { toExpr = "[1 / 3] - [1 / 9]" }
            step { toExpr = "[2 / 9]" }
        }
    }

    @Test
    fun testFractionToTheMinusOne() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[([1 / 3])^-1] * [2 ^ -2]"

        check {
            toExpr = "[3 / 4]"

            step { toExpr = "[3 / 1] * [2 ^ -2]" }

            step { toExpr = "3 * [2 ^ -2]" }

            step { toExpr = "3 * [1 / 4]" }

            step { toExpr = "[3 / 4]" }
        }
    }

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
    fun testFractionOverOnePriority() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[sqrt[12] / 1]"

        check {
            step {
                toExpr = "sqrt[12]"
            }
            step {}
        }
    }

    @Test
    fun testSumOfProductOfSameBaseRationalExponent1() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[12^[1/2]] * [12^[1/2]]"

        check {
            fromExpr = "[12 ^ [1 / 2]] * [12 ^ [1 / 2]]"
            toExpr = "12"

            step {
                fromExpr = "[12 ^ [1 / 2]] * [12 ^ [1 / 2]]"
                toExpr = "[12 ^ 1]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }
            }

            step { toExpr = "12" }
        }
    }

    @Test
    fun testSumOfProductOfSameBaseRationalExponent2() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[12^[1/3]] * [12^[1/2]]"

        check {
            fromExpr = "[12 ^ [1 / 3]] * [12 ^ [1 / 2]]"
            toExpr = "[12 ^ [5 / 6]]"

            step {
                fromExpr = "[12 ^ [1 / 3]] * [12 ^ [1 / 2]]"
                toExpr = "[12 ^ [1 / 3] + [1 / 2]]"
                explanation {
                    key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                }
            }

            step { }
        }
    }

    @Test
    fun testCollectLikeRootsAndSimplify() = testMethod {
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
                toExpr = "2 + (-[1 / 3]) sqrt[3] + root[3, 3]"
                explanation {
                    key = CollectingExplanation.SimplifyCoefficient
                }
            }

            step {
                fromExpr = "2 + (-[1 / 3]) sqrt[3] + root[3, 3]"
                toExpr = "2 - [1 / 3] sqrt[3] + root[3, 3]"
                explanation {
                    key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                }
            }

            step {
                fromExpr = "2 - [1 / 3] sqrt[3] + root[3, 3]"
                toExpr = "2 - [sqrt[3] / 3] + root[3, 3]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                }
            }
        }
    }
}

class ConstantExpressionRationalizationTest {

    @Test
    fun testSimplifyBeforeRationalizing() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[1 / sqrt[12] - sqrt[3]]"

        check {
            fromExpr = "[1 / sqrt[12] - sqrt[3]]"
            toExpr = "[sqrt[3] / 3]"

            step {
                fromExpr = "[1 / sqrt[12] - sqrt[3]]"
                toExpr = "[1 / 2 sqrt[3] - sqrt[3]]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }
            }

            step {
                fromExpr = "[1 / 2 sqrt[3] - sqrt[3]]"
                toExpr = "[1 / sqrt[3]]"
                explanation {
                    key = CollectingExplanation.CollectLikeRootsAndSimplify
                }
            }

            step {
                fromExpr = "[1 / sqrt[3]]"
                toExpr = "[sqrt[3] / 3]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }
            }
        }
    }

    @Test
    fun testRationalizeCubeRootDenominator1() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[2 / root[5, 3] + root[3, 3]]"

        check {
            toExpr = "[root[25, 3] - root[15, 3] + root[9, 3] / 4]"

            step {
                toExpr = "[2 ([(root[5, 3]) ^ 2] - root[15, 3] + [(root[3, 3]) ^ 2]) / 8]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }
            }

            step {
                toExpr = "[[(root[5, 3]) ^ 2] - root[15, 3] + [(root[3, 3]) ^ 2] / 4]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }
            }

            step {
                toExpr = "[root[[5 ^ 2], 3] - root[15, 3] + root[[3 ^ 2], 3] / 4]"
            }

            step {
                toExpr = "[root[25, 3] - root[15, 3] + root[[3 ^ 2], 3] / 4]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowers
                }
            }

            step {
                toExpr = "[root[25, 3] - root[15, 3] + root[9, 3] / 4]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowers
                }
            }
        }
    }

    @Test
    fun testRationalizeCubeRootDenominator2() = testMethod {
        method = FractionRootsPlans.RationalizeDenominators
        inputExpr = "[2 / -root[5, 3] + root[3, 3]]"

        @Suppress("MaxLineLength")
        check {
            fromExpr = "[2 / -root[5, 3] + root[3, 3]]"
            toExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / -2]"
            explanation {
                key = FractionRootsExplanation.RationalizeDenominator
            }

            step {
                fromExpr = "[2 / -root[5, 3] + root[3, 3]]"
                toExpr = "[2 / root[3, 3] - root[5, 3]]"
                explanation {
                    key = FractionRootsExplanation.FlipRootsInDenominator
                }
            }

            step {
                fromExpr = "[2 / root[3, 3] - root[5, 3]]"
                toExpr =
                    "[2 / root[3, 3] - root[5, 3]] * [[(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2] / [(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]]"
                explanation {
                    key = FractionRootsExplanation.RationalizeSumOfIntegerAndCubeRoot
                }
            }

            step {
                fromExpr =
                    "[2 / root[3, 3] - root[5, 3]] * [[(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2] / [(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]]"
                toExpr =
                    "[2 ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyFractions
                }
            }

            step {
                fromExpr =
                    "[2 ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                toExpr =
                    "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }

                step {
                    fromExpr = "root[3, 3] * root[5, 3]"
                    toExpr = "root[3 * 5, 3]"
                    explanation {
                        key = IntegerRootsExplanation.MultiplyNthRoots
                    }
                }

                step {
                    fromExpr = "root[3 * 5, 3]"
                    toExpr = "root[15, 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                    }

                    step {
                        fromExpr = "3 * 5"
                        toExpr = "15"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }
            }

            step {
                fromExpr =
                    "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                toExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / -2]"
                explanation {
                    key = FractionRootsExplanation.SimplifyDenominatorAfterRationalization
                }

                step {
                    fromExpr =
                        "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                    toExpr =
                        "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / [(root[3, 3]) ^ 3] - [(root[5, 3]) ^ 3]]"
                    explanation {
                        key = FractionRootsExplanation.IdentityCubeSumDifference
                    }
                }

                step {
                    fromExpr =
                        "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / [(root[3, 3]) ^ 3] - [(root[5, 3]) ^ 3]]"
                    toExpr =
                        "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / 3 - [(root[5, 3]) ^ 3]]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyNthRootToThePowerOfN
                    }
                }

                step {
                    fromExpr =
                        "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / 3 - [(root[5, 3]) ^ 3]]"
                    toExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / 3 - 5]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyNthRootToThePowerOfN
                    }
                }

                step {
                    fromExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / 3 - 5]"
                    toExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / -2]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }

                    step {
                        fromExpr = "3 - 5"
                        toExpr = "-2"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testRationalizeHigherOrderRoot1() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[2 / root[2, 3]]"

        check {
            fromExpr = "[2 / root[2, 3]]"
            toExpr = "root[4, 3]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "[2 / root[2, 3]]"
                toExpr = "[2 root[4, 3] / 2]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }

                step {
                    fromExpr = "[2 / root[2, 3]]"
                    toExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeHigherOrderRoot
                    }

                    step {
                        fromExpr = "[2 / root[2, 3]]"
                        toExpr = "[2 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                        explanation {
                            key = FractionRootsExplanation.HigherOrderRationalizingTerm
                        }
                    }

                    step {
                        fromExpr = "[2 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                        toExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                        explanation {
                            key = FractionRootsExplanation.SimplifyRationalizingTerm
                        }

                        step {
                            fromExpr = "[2 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                            toExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 3 - 1], 3]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }

                        step {
                            fromExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 3 - 1], 3]]"
                            toExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }
                    }
                }

                step {
                    fromExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                    toExpr = "[2 root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    fromExpr = "[2 root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    toExpr = "[2 root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyProductWithRoots
                    }

                    step {
                        fromExpr = "2 root[[2 ^ 2], 3]"
                        toExpr = "2 root[4, 3]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                        }
                    }
                }

                step {
                    fromExpr = "[2 root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    toExpr = "[2 root[4, 3] / 2]"
                    explanation {
                        key = FractionRootsExplanation.SimplifyDenominatorAfterRationalization
                    }

                    step {
                        fromExpr = "[2 root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        toExpr = "[2 root[4, 3] / root[[2 ^ 3], 3]]"
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
                        fromExpr = "[2 root[4, 3] / root[[2 ^ 3], 3]]"
                        toExpr = "[2 root[4, 3] / 2]"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 root[4, 3] / 2]"
                toExpr = "root[4, 3]"
                explanation {
                    key = GeneralExplanation.CancelDenominator
                }
            }
        }
    }
}

class TestNormalization {

    @Test
    fun testSimpleNormalization() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "(+1 + (3))"

        check {
            step {
                toExpr = "1 + 3"
                explanation {
                    key = GeneralExplanation.NormalizeExpression
                }

                step { toExpr = "+1 + (3)" }
                step { toExpr = "+1 + 3" }
                step { toExpr = "1 + 3" }
            }

            step { toExpr = "4" }
        }
    }

    @Test
    fun testNoNormalizationIfNotNeeded() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "0 * (1)"

        check {
            fromExpr = "0 * (1)"
            toExpr = "0"

            explanation {
                key = GeneralExplanation.EvaluateProductContainingZero
            }
        }
    }

    @Test
    fun `test remove brackets and simplify a (bc) (de)`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"

        check {
            fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
            toExpr = "12 sqrt[2]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                explanation {
                    key = GeneralExplanation.RemoveAllBracketProductInProduct
                }

                step {
                    fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                    toExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                    explanation {
                        key = GeneralExplanation.RemoveBracketProductInProduct
                    }
                }

                step {
                    fromExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                    toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                    explanation {
                        key = GeneralExplanation.RemoveBracketProductInProduct
                    }
                }
            }

            step { }
        }
    }

    @Test
    fun `test remove brackets and simplify (a) (bc) (de)`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "(sqrt[2]) (2 sqrt[2]) (3 sqrt[2])"

        check {
            fromExpr = "(sqrt[2]) (2 sqrt[2]) (3 sqrt[2])"
            toExpr = "12 sqrt[2]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "(sqrt[2]) (2 sqrt[2]) (3 sqrt[2])"
                toExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                explanation {
                    key = GeneralExplanation.RemoveRedundantBracket
                }
            }

            step {
                fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                explanation {
                    key = GeneralExplanation.RemoveAllBracketProductInProduct
                }

                step {
                    fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                    toExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                    explanation {
                        key = GeneralExplanation.RemoveBracketProductInProduct
                    }
                }

                step {
                    fromExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                    toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                    explanation {
                        key = GeneralExplanation.RemoveBracketProductInProduct
                    }
                }
            }

            step { }
        }
    }
}

class ConstantExpressionFractionHigherOrderRootTest {
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

    @Test
    fun testSimplifyConstantExpressionWithDivisionOfDecimals() = testMethod {
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
    fun testDivisionOfDecimalsWithTrailingZeros() = testMethod {
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

class ConstantExpressionTest {
    @Test
    fun testCancelUnitaryDenominator() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[sqrt[8] / 2]"

        check {
            toExpr = "sqrt[2]"
        }
    }

    @Test
    fun testRationalizationIntegerAndCubeRoot() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[2 / 2 - root[3, 3]]"

        check {
            toExpr = "[8 + 4 root[3, 3] + 2 root[9, 3] / 5]"
        }
    }

    @Test
    fun `test constant expression containing absolute values`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "abs[3 * 2 - 4] - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"

        check {
            fromExpr = "abs[3 * 2 - 4] - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"
            toExpr = "[9 / 5]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "abs[3 * 2 - 4] - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"
                toExpr = "2 - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"
                explanation {
                    key = GeneralExplanation.EvaluateAbsoluteValue
                }

                step {
                    fromExpr = "abs[3 * 2 - 4]"
                    toExpr = "abs[6 - 4]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyProductWithRoots
                    }
                }

                step {
                    fromExpr = "abs[6 - 4]"
                    toExpr = "abs[2]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }

                step {
                    fromExpr = "abs[2]"
                    toExpr = "2"
                    explanation {
                        key = GeneralExplanation.ResolveAbsoluteValueOfPositiveValue
                    }
                }
            }

            step {
                fromExpr = "2 - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"
                toExpr = "2 - [1 / 5] + abs[2 - 1 - 1]"
                explanation {
                    key = GeneralExplanation.EvaluateAbsoluteValue
                }

                step {
                    fromExpr = "abs[1 - [6 / 5]]"
                    toExpr = "abs[-[1 / 5]]"
                    explanation {
                        key = FractionArithmeticExplanation.AddIntegerAndFraction
                    }
                }

                step {
                    fromExpr = "abs[-[1 / 5]]"
                    toExpr = "[1 / 5]"
                    explanation {
                        key = GeneralExplanation.ResolveAbsoluteValueOfNegativeValue
                    }
                }
            }

            step {
                fromExpr = "2 - [1 / 5] + abs[2 - 1 - 1]"
                toExpr = "2 - [1 / 5] + 0"
                explanation {
                    key = GeneralExplanation.EvaluateAbsoluteValue
                }

                step {
                    fromExpr = "abs[2 - 1 - 1]"
                    toExpr = "abs[0]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }

                step {
                    fromExpr = "abs[0]"
                    toExpr = "0"
                    explanation {
                        key = GeneralExplanation.ResolveAbsoluteValueOfZero
                    }
                }
            }

            step {
                fromExpr = "2 - [1 / 5] + 0"
                toExpr = "2 - [1 / 5]"
                explanation {
                    key = GeneralExplanation.EliminateZeroInSum
                }
            }

            step {
                fromExpr = "2 - [1 / 5]"
                toExpr = "[9 / 5]"
                explanation {
                    key = FractionArithmeticExplanation.AddIntegerAndFraction
                }
            }
        }
    }
}

class SimplifyToZeroTest {

    @Test
    fun testZeroDivideByValue() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "0:(1+1)"

        check {
            fromExpr = "0 : (1 + 1)"
            toExpr = "0"

            step {
                fromExpr = "0 : (1 + 1)"
                toExpr = "0 : 2"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }
                step {
                    fromExpr = "1 + 1"
                    toExpr = "2"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }
            }

            step {
                fromExpr = "0 : 2"
                toExpr = "[0 / 2]"
                explanation {
                    key = FractionArithmeticExplanation.RewriteDivisionAsFraction
                }
            }

            step {
                fromExpr = "[0 / 2]"
                toExpr = "0"
                explanation {
                    key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
                }
            }
        }
    }
}

class SimplifyToUndefinedTest {
    @Test
    fun testZeroDenominator1() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[5 - 4 / 1 - 1] + 2"

        check {
            fromExpr = "[5 - 4 / 1 - 1] + 2"
            toExpr = "/undefined/"

            step {
                fromExpr = "[5 - 4 / 1 - 1] + 2"
                toExpr = "[5 - 4 / 0] + 2"
                explanation {
                    key = GeneralExplanation.CancelAdditiveInverseElements
                }
            }

            step {
                fromExpr = "[5 - 4 / 0] + 2"
                toExpr = "/undefined/"
                explanation {
                    key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                }
            }
        }
    }

    @Test
    fun testDivisionByZero1() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "(5 - 4) : (1 - 1) + 2"

        check {
            fromExpr = "(5 - 4) : (1 - 1) + 2"
            toExpr = "/undefined/"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "(5 - 4) : (1 - 1) + 2"
                toExpr = "(5 - 4) : 0 + 2"
                explanation {
                    key = GeneralExplanation.CancelAdditiveInverseElements
                }
            }

            step {
                fromExpr = "(5 - 4) : 0 + 2"
                toExpr = "1 : 0 + 2"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }

                step {
                    fromExpr = "5 - 4"
                    toExpr = "1"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                    }
                }
            }

            step {
                fromExpr = "1 : 0 + 2"
                toExpr = "[1 / 0] + 2"
                explanation {
                    key = FractionArithmeticExplanation.RewriteDivisionAsFraction
                }
            }

            step {
                fromExpr = "[1 / 0] + 2"
                toExpr = "/undefined/"
                explanation {
                    key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                }
            }
        }
    }

    @Test
    fun testZeroDenominator2() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 / 1 - 1]"

        check {
            toExpr = "/undefined/"
        }
    }

    // step-by-step of this needs to be improved
    @Test
    fun testDivisionByZero2() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "0:(1 - 1)"

        check {
            toExpr = "/undefined/"
        }
    }

    @Test
    fun testNegativeToRationalExponent() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "1 + 3 * [(-1) ^ [6/5]]"

        check {
            toExpr = "/undefined/"
        }
    }
}

class CancelOppositeTermTest {
    @Test
    fun testCancelOppositeTerm() = testMethod {
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
    fun testCommutativeCancelAdditiveInverseElements() = testMethod {
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
    fun testCommutativeAdditiveInverseElementsComplex() = testMethod {
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
    fun testCancelAdditiveInverseElementsAfterSimplifying() = testMethod {
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

    @Test
    fun testCancelTermsBeforeRationalization() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[1 / root[3, 3] - root[3, 3]]"

        check {
            toExpr = "/undefined/"

            step {
                toExpr = "[1 / 0]"
                explanation {
                    key = GeneralExplanation.CancelAdditiveInverseElements
                }
            }

            step {
                toExpr = "/undefined/"
                explanation {
                    key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                }
            }
        }
    }

    @Test
    fun testCollectLikeRationalExponentsBeforeSimplification() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[12 ^ [1 / 2]] + [12 ^ [1 / 2]]"

        check {
            toExpr = "4 * [3 ^ [1 / 2]]"

            step {
                toExpr = "2 * [12 ^ [1 / 2]]"
                explanation {
                    key = CollectingExplanation.CollectLikeRationalPowersAndSimplify
                }
            }

            step {
                toExpr = "2 * 2 * [3 ^ [1 / 2]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                }
            }

            step {
                toExpr = "4 * [3 ^ [1 / 2]]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                }
            }
        }
    }

    @Test
    fun testSimplifyRationalExponentsInProduct() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[2 ^ [1 / 3]] * [3 ^ [1 / 3]] * [6 ^ [1 / 2]]"

        check {
            toExpr = "[6 ^ [5 / 6]]"

            step {
                toExpr = "[6 ^ [1 / 3]] * [6 ^ [1 / 2]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameExponent
                }

                step {
                    toExpr = "[(2 * 3) ^ [1 / 3]] * [6 ^ [1 / 2]]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameExponent
                    }
                }

                step {
                    toExpr = "[6 ^ [1 / 3]] * [6 ^ [1 / 2]]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }

            step {
                toExpr = "[6 ^ [5 / 6]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }

                step {
                    toExpr = "[6 ^ [1 / 3] + [1 / 2]]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step {
                    toExpr = "[6 ^ [5 / 6]]"
                    explanation {
                        key = FractionArithmeticExplanation.AddFractions
                    }
                }
            }
        }
    }
}

class ExponentsTest {
    @Test
    fun testEvaluateExpressionToThePowerOfOneComplexExpression() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[ (sqrt[2] + 1) ^ [5 / 5]]  + 1"

        check {
            toExpr = "sqrt[2] + 2"

            step {
                toExpr = "[(sqrt[2] + 1) ^ 1] + 1"
            }

            step {
                toExpr = "(sqrt[2] + 1) + 1"
                explanation {
                    key = GeneralExplanation.SimplifyExpressionToThePowerOfOne
                }
            }

            step { }

            step { }
        }
    }

    @Test
    fun testEvaluateExpressionToThePowerOfOneSimpleExpr() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[2 ^ 1] + [5 ^ 1]"

        check {
            toExpr = "7"

            step {
                fromExpr = "[2 ^ 1] + [5 ^ 1]"
                toExpr = "2 + [5 ^ 1]"
                explanation {
                    key = GeneralExplanation.SimplifyExpressionToThePowerOfOne
                }
            }

            step {
                fromExpr = "2 + [5 ^ 1]"
                toExpr = "2 + 5"
                explanation {
                    key = GeneralExplanation.SimplifyExpressionToThePowerOfOne
                }
            }

            step {
                fromExpr = "2 + 5"
                toExpr = "7"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                }
            }
        }
    }

    @Test
    fun testZeroToNegativePower() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 ^ -[3 / 2]]"

        check {
            toExpr = "/undefined/"
            explanation {
                key = FractionArithmeticExplanation.EvaluateIntegerToNegativePower
            }

            step {
                toExpr = "[([1 / 0]) ^ [3 / 2]]"
                explanation {
                    key = FractionArithmeticExplanation.TurnNegativePowerOfZeroToPowerOfFraction
                }
            }

            step {
                toExpr = "/undefined/"
                explanation {
                    key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                }
            }
        }
    }

    @Test
    fun testProductOfExponentsSameBase1() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[20 ^ 2] * [20 ^ -2]"

        check {
            toExpr = "1"

            step {
                fromExpr = "[20 ^ 2] * [20 ^ -2]"
                toExpr = "[20 ^ 0]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }

                step {
                    fromExpr = "[20 ^ 2] * [20 ^ -2]"
                    toExpr = "[20 ^ 2 - 2]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "[20 ^ 2 - 2]"
                    toExpr = "[20 ^ 0]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                    }
                }
            }

            step {
                fromExpr = "[20 ^ 0]"
                toExpr = "1"
                explanation {
                    key = GeneralExplanation.EvaluateExpressionToThePowerOfZero
                }
            }
        }
    }

    @Test
    fun testProductOfExponentsSameBase2() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[20 ^ 2] * [20 ^ -3]"

        check {
            toExpr = "[1 / 20]"

            step {
                fromExpr = "[20 ^ 2] * [20 ^ -3]"
                toExpr = "[20 ^ -1]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }
            }

            step { }
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
                    key = IntegerArithmeticExplanation.SimplifyEvenPowerOfNegative
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
        inputExpr = "root[ [(-1)^4], 4]"

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
                    key = IntegerArithmeticExplanation.SimplifyEvenPowerOfNegative
                }
            }

            step { }

            step { }
        }
    }

    @Test
    fun `test don't cancel even root index & base negative`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[ [(-2)^5*2], 3*2]"

        check {
            fromExpr = "root[[(-2) ^ 5 * 2], 3 * 2]"
            toExpr = "2 root[4, 3]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "root[[(-2) ^ 5 * 2], 3 * 2]"
                toExpr = "root[[(-2) ^ 10], 6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }

            step {
                fromExpr = "root[[(-2) ^ 10], 6]"
                toExpr = "root[[2 ^ 10], 6]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyEvenPowerOfNegative
                }
            }

            step {
                fromExpr = "root[[2 ^ 10], 6]"
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
                    key = ConstantExpressionsExplanation.SimplifyPowers
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
                toExpr = "24 * 4 root[9, 3]"
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
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
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

class SimplifyRationalPowerOfFraction {
    @Test
    fun testRationalPowerFractionSplitPowerAndNoBaseSimplify() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[([4 / 9]) ^ [11 / 3]]"

        check {
            toExpr = "[64 / 729] * [([4 / 9]) ^ [2 / 3]]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyPowers
            }

            step {
                fromExpr = "[([4 / 9]) ^ [11 / 3]]"
                toExpr = "[([4 / 9]) ^ 3] * [([4 / 9]) ^ [2 / 3]]"
                explanation {
                    key = FractionArithmeticExplanation.SplitRationalExponent
                }
            }

            step {
                fromExpr = "[([4 / 9]) ^ 3] * [([4 / 9]) ^ [2 / 3]]"
                toExpr = "[[4 ^ 3] / [9 ^ 3]] * [([4 / 9]) ^ [2 / 3]]"
                explanation {
                    key = FractionArithmeticExplanation.DistributeFractionPositivePower
                }
            }

            step {
                fromExpr = "[[4 ^ 3] / [9 ^ 3]] * [([4 / 9]) ^ [2 / 3]]"
                toExpr = "[64 / [9 ^ 3]] * [([4 / 9]) ^ [2 / 3]]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                }
            }

            step {
                fromExpr = "[64 / [9 ^ 3]] * [([4 / 9]) ^ [2 / 3]]"
                toExpr = "[64 / 729] * [([4 / 9]) ^ [2 / 3]]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                }
            }
        }
    }

    @Test
    fun testRationalPowerFractionNoSplitSimplifyToFraction() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[([32 / 243]) ^ [2 / 5]]"

        check {
            toExpr = "[4 / 9]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "[([32 / 243]) ^ [2 / 5]]"
                toExpr = "[[32 ^ [2 / 5]] / [243 ^ [2 / 5]]]"
                explanation {
                    key = FractionArithmeticExplanation.DistributeFractionPositivePower
                }
            }

            step {
                fromExpr = "[[32 ^ [2 / 5]] / [243 ^ [2 / 5]]]"
                toExpr = "[4 / [243 ^ [2 / 5]]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                }
            }

            step {
                fromExpr = "[4 / [243 ^ [2 / 5]]]"
                toExpr = "[4 / 9]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                }
            }
        }
    }

    @Test
    fun testRationalPowerFractionNoTransformation() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[([14 / 45]) ^ [2 / 3]]"

        check {
            noTransformation()
        }
    }

    @Test
    fun testRationalPowerFractionOnlyNumeratorSimplifiedToInt() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[([4 / 3]) ^ [3 / 2]]"

        check {
            toExpr = "[8 / 3 * [3 ^ [1 / 2]]]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "[([4 / 3]) ^ [3 / 2]]"
                toExpr = "[4 / 3] * [[4 ^ [1 / 2]] / [3 ^ [1 / 2]]]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowers
                }
            }

            step {
                fromExpr = "[4 / 3] * [[4 ^ [1 / 2]] / [3 ^ [1 / 2]]]"
                toExpr = "[4 / 3] * [2 / [3 ^ [1 / 2]]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                }
            }

            step {
                fromExpr = "[4 / 3] * [2 / [3 ^ [1 / 2]]]"
                toExpr = "[8 / 3 * [3 ^ [1 / 2]]]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
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
}
