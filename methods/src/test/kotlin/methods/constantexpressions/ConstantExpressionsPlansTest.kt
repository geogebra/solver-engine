package methods.constantexpressions

import methods.decimals.DecimalsExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.fractionroots.FractionRootsExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerroots.IntegerRootsExplanation
import methods.plans.testMethod
import org.junit.jupiter.api.Test

class ConstantExpressionsPlansTest {

    @Test
    fun simpleTest() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[1 / 3] + [2 / 3] * [1 / 2]"

        check {
            step { toExpr = "[1 / 3] + [1 / 3]" }
            step { toExpr = "[2 / 3]" }
        }
    }

    @Test
    fun testMultiplyAndSimplify() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[14 / 4] * [4 / 21]"

        check {
            toExpr = "[2 / 3]"

            step { toExpr = "[14 * 4 / 4 * 21]" }

            step {
                toExpr = "[2 / 3]"

                step { toExpr = "[14 / 21]" }

                step { toExpr = "[7 * 2 / 7 * 3]" }

                step { toExpr = "[2 / 3]" }
            }
        }
    }

    @Test
    fun testWithNegativesInFractions() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[-1 / 6] * [2 / -5]"

        check {
            step {
                step { toExpr = "(-[1 / 6]) * [2 / -5]" }
                step { toExpr = "(-[1 / 6]) * (-[2 / 5])" }
                step { toExpr = "[1 / 6] * [2 / 5]" }
            }
            step { toExpr = "[1 / 15]" }
        }
    }

    @Test
    fun testWithMoreNegativesInFractions() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "(-[1 / 3]) * [-1 / 4] * [3 / -2]"

        check {
            step {
                step { toExpr = "(-[1 / 3]) * (-[1 / 4]) * [3 / -2]" }
                step { toExpr = "(-[1 / 3]) * (-[1 / 4]) * (-[3 / 2])" }
                step { toExpr = "[1 / 3] * [1 / 4] * (-[3 / 2])" }
                step { toExpr = "-[1 / 3] * [1 / 4] * [3 / 2]" }
            }
            step { toExpr = "-[1 / 8]" }
        }
    }

    @Test
    fun testDividingTwice() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "3 : 4 : 5"

        check {
            step {
                step {
                    step { toExpr = "[3 / 4] : 5" }
                    step { toExpr = "[[3 / 4] / 5]" }
                }
                step { toExpr = "[3 / 4] * [1 / 5]" }
            }

            step { toExpr = "[3 / 20]" }
        }
    }

    @Test
    fun testDividingFractions() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[5 / 6] : [3 / 4]"

        check {
            step {
                step { toExpr = "[[5 / 6] / [3 / 4]]" }
                step { toExpr = "[5 / 6] * [4 / 3]" }
            }
            step { toExpr = "[10 / 9]" }
        }
    }

    @Test
    fun testDividingWithNegatives() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "3 : (-5)"

        check {
            step { toExpr = "[3 / -5]" }
            step { toExpr = "-[3 / 5]" }
        }
    }

    @Test
    fun testFractionExponent() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[(-[1 / 2]) ^ 3] + [([2 / 3]) ^ -2]"

        check {
            step { toExpr = "- [1 / 8] + [([2/3])^-2]" }
            step { toExpr = "- [1 / 8] + [9 / 4]" }
            step { toExpr = "[17 / 8]" }
        }
    }

    @Test
    fun testNegativeExponentsOfIntegers() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[3 ^ -1] - [3 ^ -2]"

        check {
            step { toExpr = "[1 / 3] - [3 ^ -2]" }
            step { toExpr = "[1 / 3] - [1 / 9]" }
            step { toExpr = "[2 / 9]" }
        }
    }

    @Test
    fun testFractionToTheMinusOne() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[([1 / 3])^-1] * [2 ^ -2]"

        check {
            toExpr = "[3 / 4]"

            step { toExpr = "[3 / 1] * [2 ^ -2]" }

            step { toExpr = "[3 / 1] * [1 / 4]" }

            step { toExpr = "[3 / 4]" }
        }
    }

    @Test
    fun testResultSimplifyPowerOfRoot() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[(root[12, 5]) ^ 4]"

        check {
            fromExpr = "[(root[12, 5]) ^ 4]"
            toExpr = "2 * root[648, 5]"
        }
    }

    @Test
    fun testResultSimplifyPowerOfRootWithCoefficients() = testMethod {
        testMethod {
            method = simplifyConstantExpression
            inputExpr = "[(2 * sqrt[5]) ^ 3]"

            check {
                fromExpr = "[(2 * sqrt[5]) ^ 3]"
                toExpr = "40 * sqrt[5]"
            }
        }
    }

    @Test
    fun testResultPowerOfBinomialContainingRoots() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[(2 * sqrt[6] + 3 * sqrt[2]) ^ 2]"

        check {
            fromExpr = "[(2 * sqrt[6] + 3 * sqrt[2]) ^ 2]"
            toExpr = "42 + 24 * sqrt[3]"

            // Currently has 10 steps!
        }
    }

    @Test
    fun testSimplifyRootOfRoot() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "sqrt[root[12, 4]]"

        check {
            fromExpr = "sqrt[root[12, 4]]"
            toExpr = "root[12, 8]"

            step {
                fromExpr = "sqrt[root[12, 4]]"
                toExpr = "root[12, 2 * 4]"
            }

            step {
                fromExpr = "root[12, 2* 4]"
                toExpr = "root[12, 8]"
            }
        }
    }

    @Test
    fun testSimplifyRootOfRootWithCoefficient() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "root[2 * sqrt[6], 3]"

        check {
            step {
                toExpr = "root[sqrt[24], 3]"

                step {
                    fromExpr = "2 * sqrt[6]"
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
}

class ConstantExpressionSimpleOperationsTest {
    @Test
    fun testAddLikeFractions() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[5/4]+[2/4]"

        check {
            fromExpr = "[5 / 4] + [2 / 4]"
            toExpr = "[7 / 4]"
            explanation {
                key = FractionArithmeticExplanation.EvaluateFractionSum
            }

            step {
                fromExpr = "[5 / 4] + [2 / 4]"
                toExpr = "[5 + 2 / 4]"
                explanation {
                    key = FractionArithmeticExplanation.AddLikeFractions
                }
            }

            step {
                fromExpr = "[5 + 2 / 4]"
                toExpr = "[7 / 4]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                }
            }
        }
    }

    @Test
    fun testSubtractLikeFractions() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[5/4]-[2/4]"

        check {
            fromExpr = "[5 / 4] - [2 / 4]"
            toExpr = "[3 / 4]"
            explanation {
                key = FractionArithmeticExplanation.EvaluateFractionSum
            }

            step {
                fromExpr = "[5 / 4] - [2 / 4]"
                toExpr = "[5 - 2 / 4]"
                explanation {
                    key = FractionArithmeticExplanation.SubtractLikeFractions
                }
            }

            step {
                fromExpr = "[5 - 2 / 4]"
                toExpr = "[3 / 4]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                }
            }
        }
    }
}

class ConstantExpressionRationalizationTest {
    @Test
    fun testRationalizeCubeRootDenominator1() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[2 / root[5, 3] + root[3, 3]]"

        check {
            toExpr = "[root[25, 3] - root[15, 3] + root[9, 3] / 4]"

            step {
                toExpr = "[2 * ([(root[5, 3]) ^ 2] - (root[5, 3]) * (root[3, 3]) + [(root[3, 3]) ^ 2]) / 8]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }
            }

            step {}

            step {}

            step {
                fromExpr = "[2 * ([(root[5, 3]) ^ 2] - root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2]) / 8]"
                toExpr = "[2 * (root[[5 ^ 2], 3] - root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2]) / 8]"
            }

            step {
                toExpr = "[2 * (root[25, 3] - root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2]) / 8]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowers
                }
            }

            step {
                fromExpr = "[2 * (root[25, 3] - root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2]) / 8]"
                toExpr = "[2 * (root[25, 3] - root[5, 3] * root[3, 3] + root[[3 ^ 2], 3]) / 8]"
            }

            step {
                fromExpr = "[2 * (root[25, 3] - root[5, 3] * root[3, 3] + root[[3 ^ 2], 3]) / 8]"
                toExpr = "[2 * (root[25, 3] - root[5, 3] * root[3, 3] + root[9, 3]) / 8]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowers
                }
            }

            step {
                fromExpr = "[2 * (root[25, 3] - root[5, 3] * root[3, 3] + root[9, 3]) / 8]"
                toExpr = "[2 * (root[25, 3] - root[15, 3] + root[9, 3]) / 8]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }

            step {
                fromExpr = "[2 * (root[25, 3] - root[15, 3] + root[9, 3]) / 8]"
                toExpr = "[(root[25, 3] - root[15, 3] + root[9, 3]) / 4]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }
            }

            step {
                fromExpr = "[(root[25, 3] - root[15, 3] + root[9, 3]) / 4]"
                toExpr = "[root[25, 3] - root[15, 3] + root[9, 3] / 4]"
                explanation {
                    key = GeneralExplanation.RemoveRedundantBracket
                }
            }
        }
    }

    @Test
    fun testRationalizeCubeRootDenominator2() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[2 / -root[5, 3] + root[3, 3]]"

        check {
            fromExpr = "[2 / -root[5, 3] + root[3, 3]]"
            toExpr = "-(root[9, 3] + root[15, 3] + root[25, 3])"

            step {
                fromExpr = "[2 / -root[5, 3] + root[3, 3]]"
                toExpr = "[2 * ([(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2]) / -2]"
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
                    toExpr = "[2 / root[3, 3] - root[5, 3]] * " +
                        "[[(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2] / " +
                        "[(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2]]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeSumOfIntegerAndCubeRoot
                    }
                }

                step {
                    fromExpr = "[2 / root[3, 3] - root[5, 3]] * " +
                        "[[(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2] " +
                        "/ [(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2]]"
                    toExpr = "[2 * ([(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2]) / " +
                        "(root[3, 3] - root[5, 3]) * ([(root[3, 3]) ^ 2] + (root[3, 3]) " +
                        "* (root[5, 3]) + [(root[5, 3]) ^ 2])]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    fromExpr = "[2 * ([(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2]) / " +
                        "(root[3, 3] - root[5, 3]) * ([(root[3, 3]) ^ 2] " +
                        "+ (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2])]"
                    toExpr = "[2 * ([(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2]) / -2]"

                    step {
                        fromExpr = "(root[3, 3] - root[5, 3]) * " +
                            "([(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2])"
                        toExpr = "[(root[3, 3]) ^ 3] - [(root[5, 3]) ^ 3]"
                        explanation {
                            key = FractionRootsExplanation.IdentityCubeSumDifference
                        }
                    }

                    step {
                        fromExpr = "[(root[3, 3]) ^ 3] - [(root[5, 3]) ^ 3]"
                        toExpr = "3 - [(root[5, 3]) ^ 3]"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyNthRootToThePowerOfN
                        }
                    }

                    step {
                        fromExpr = "3 - [(root[5, 3]) ^ 3]"
                        toExpr = "3 - 5"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyNthRootToThePowerOfN
                        }
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

            step {
                fromExpr = "[2 * ([(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2]) / -2]"
                toExpr = "[2 * ([(root[3, 3]) ^ 2] + root[3, 3] * (root[5, 3]) + [(root[5, 3]) ^ 2]) / -2]"
                explanation {
                    key = GeneralExplanation.RemoveRedundantBracket
                }
            }

            step {
                fromExpr = "[2 * ([(root[3, 3]) ^ 2] + root[3, 3] * (root[5, 3]) + [(root[5, 3]) ^ 2]) / -2]"
                toExpr = "[2 * ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / -2]"
                explanation {
                    key = GeneralExplanation.RemoveRedundantBracket
                }
            }

            step {
                fromExpr = "[2 * ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / -2]"
                toExpr = "[2 * (root[[3 ^ 2], 3] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / -2]"

                step {
                    fromExpr = "[(root[3, 3]) ^ 2]"
                    toExpr = "root[[3 ^ 2], 3]"
                    explanation {
                        key = IntegerRootsExplanation.TurnPowerOfRootToRootOfPower
                    }
                }
            }

            step {
                fromExpr = "[2 * (root[[3 ^ 2], 3] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / -2]"
                toExpr = "[2 * (root[9, 3] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / -2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowers
                }

                step {
                    fromExpr = "[3 ^ 2]"
                    toExpr = "9"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerPower
                    }

                    step {
                        fromExpr = "[3 ^ 2]"
                        toExpr = "3 * 3"
                        explanation {
                            key = IntegerArithmeticExplanation.RewriteIntegerPowerAsProduct
                        }
                    }

                    step {
                        fromExpr = "3 * 3"
                        toExpr = "9"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 * (root[9, 3] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / -2]"
                toExpr = "[2 * (root[9, 3] + root[3, 3] * root[5, 3] + root[[5 ^ 2], 3]) / -2]"

                step {
                    fromExpr = "[(root[5, 3]) ^ 2]"
                    toExpr = "root[[5 ^ 2], 3]"
                    explanation {
                        key = IntegerRootsExplanation.TurnPowerOfRootToRootOfPower
                    }
                }
            }

            step {
                fromExpr = "[2 * (root[9, 3] + root[3, 3] * root[5, 3] + root[[5 ^ 2], 3]) / -2]"
                toExpr = "[2 * (root[9, 3] + root[3, 3] * root[5, 3] + root[25, 3]) / -2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowers
                }

                step {
                    fromExpr = "[5 ^ 2]"
                    toExpr = "25"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerPower
                    }

                    step {
                        fromExpr = "[5 ^ 2]"
                        toExpr = "5 * 5"
                        explanation {
                            key = IntegerArithmeticExplanation.RewriteIntegerPowerAsProduct
                        }
                    }

                    step {
                        fromExpr = "5 * 5"
                        toExpr = "25"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 * (root[9, 3] + root[3, 3] * root[5, 3] + root[25, 3]) / -2]"
                toExpr = "-[2 * (root[9, 3] + root[3, 3] * root[5, 3] + root[25, 3]) / 2]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyNegativeInDenominator
                }
            }

            step {
                fromExpr = "-[2 * (root[9, 3] + root[3, 3] * root[5, 3] + root[25, 3]) / 2]"
                toExpr = "-[2 * (root[9, 3] + root[15, 3] + root[25, 3]) / 2]"
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
                fromExpr = "-[2 * (root[9, 3] + root[15, 3] + root[25, 3]) / 2]"
                toExpr = "-(root[9, 3] + root[15, 3] + root[25, 3])"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }

                step {
                    fromExpr = "[2 * (root[9, 3] + root[15, 3] + root[25, 3]) / 2]"
                    toExpr = "(root[9, 3] + root[15, 3] + root[25, 3])"
                    explanation {
                        key = GeneralExplanation.CancelDenominator
                    }
                }
            }
        }
    }

    @Test
    fun testRationalizeHigherOrderRoot1() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[2 / root[2, 3]]"

        check {
            fromExpr = "[2 / root[2, 3]]"
            toExpr = "root[4, 3]"

            step {
                fromExpr = "[2 / root[2, 3]]"
                toExpr = "[2 * root[4, 3] / 2]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }

                step {
                    fromExpr = "[2 / root[2, 3]]"
                    toExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"

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
                    toExpr = "[2 * root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    fromExpr = "[2 * root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    toExpr = "[2 * root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"

                    step {
                        fromExpr = "2 * root[[2 ^ 2], 3]"
                        toExpr = "2 * root[4, 3]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                        }
                    }
                }

                step {
                    fromExpr = "[2 * root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                    toExpr = "[2 * root[4, 3] / 2]"

                    step {
                        fromExpr = "root[2, 3] * root[[2 ^ 2], 3]"
                        toExpr = "root[[2 ^ 1 + 2], 3]"

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
                                key = IntegerRootsExplanation.CollectPowersOfExponentsWithSameBase
                            }
                        }
                    }

                    step {
                        fromExpr = "root[[2 ^ 1 + 2], 3]"
                        toExpr = "root[[2 ^ 3], 3]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                        }
                    }

                    step {
                        fromExpr = "root[[2 ^ 3], 3]"
                        toExpr = "2"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 * root[4, 3] / 2]"
                toExpr = "root[4, 3]"
                explanation {
                    key = GeneralExplanation.CancelDenominator
                }
            }
        }
    }

    @Test
    fun testSimplifyArithmeticExpression2() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[2 / root[9, 3]]"

        check {
            fromExpr = "[2 / root[9, 3]]"
            toExpr = "[2 * root[3, 3] / 3]"
            explanation {
                key = FractionRootsExplanation.RationalizeDenominator
            }

            step {
                fromExpr = "[2 / root[9, 3]]"
                toExpr = "[2 / root[[3 ^ 2], 3]]"
                explanation {
                    key = FractionRootsExplanation.FactorizeHigherOrderRadicand
                }
            }

            step {
                fromExpr = "[2 / root[[3 ^ 2], 3]]"
                toExpr = "[2 / root[[3 ^ 2], 3]] * [root[3, 3] / root[3, 3]]"

                step {
                    fromExpr = "[2 / root[[3 ^ 2], 3]]"
                    toExpr = "[2 / root[[3 ^ 2], 3]] * [root[[3 ^ 3 - 2], 3] / root[[3 ^ 3 - 2], 3]]"
                    explanation {
                        key = FractionRootsExplanation.HigherOrderRationalizingTerm
                    }
                }

                step {
                    fromExpr = "[2 / root[[3 ^ 2], 3]] * [root[[3 ^ 3 - 2], 3] / root[[3 ^ 3 - 2], 3]]"
                    toExpr = "[2 / root[[3 ^ 2], 3]] * [root[3, 3] / root[3, 3]]"

                    step {
                        fromExpr = "[2 / root[[3 ^ 2], 3]] * [root[[3 ^ 3 - 2], 3] / root[[3 ^ 3 - 2], 3]]"
                        toExpr = "[2 / root[[3 ^ 2], 3]] * [root[[3 ^ 1], 3] / root[[3 ^ 3 - 2], 3]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }

                    step {
                        fromExpr = "[2 / root[[3 ^ 2], 3]] * [root[[3 ^ 1], 3] / root[[3 ^ 3 - 2], 3]]"
                        toExpr = "[2 / root[[3 ^ 2], 3]] * [root[[3 ^ 1], 3] / root[[3 ^ 1], 3]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }

                    step {
                        fromExpr = "[2 / root[[3 ^ 2], 3]] * [root[[3 ^ 1], 3] / root[[3 ^ 1], 3]]"
                        toExpr = "[2 / root[[3 ^ 2], 3]] * [root[3, 3] / root[[3 ^ 1], 3]]"
                        explanation {
                            key = GeneralExplanation.EliminateLoneOneInExponent
                        }
                    }

                    step {
                        fromExpr = "[2 / root[[3 ^ 2], 3]] * [root[3, 3] / root[[3 ^ 1], 3]]"
                        toExpr = "[2 / root[[3 ^ 2], 3]] * [root[3, 3] / root[3, 3]]"
                        explanation {
                            key = GeneralExplanation.EliminateLoneOneInExponent
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 / root[[3 ^ 2], 3]] * [root[3, 3] / root[3, 3]]"
                toExpr = "[2 * root[3, 3] / root[[3 ^ 2], 3] * root[3, 3]]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyFractions
                }
            }

            step {
                fromExpr = "[2 * root[3, 3] / root[[3 ^ 2], 3] * root[3, 3]]"
                toExpr = "[2 * root[3, 3] / 3]"

                step {
                    fromExpr = "root[[3 ^ 2], 3] * root[3, 3]"
                    toExpr = "root[[3 ^ 2 + 1], 3]"

                    step {
                        fromExpr = "root[[3 ^ 2], 3] * root[3, 3]"
                        toExpr = "root[[3 ^ 2] * 3, 3]"
                        explanation {
                            key = IntegerRootsExplanation.MultiplyNthRoots
                        }
                    }

                    step {
                        fromExpr = "root[[3 ^ 2] * 3, 3]"
                        toExpr = "root[[3 ^ 2 + 1], 3]"
                        explanation {
                            key = IntegerRootsExplanation.CollectPowersOfExponentsWithSameBase
                        }
                    }
                }

                step {
                    fromExpr = "root[[3 ^ 2 + 1], 3]"
                    toExpr = "root[[3 ^ 3], 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }

                step {
                    fromExpr = "root[[3 ^ 3], 3]"
                    toExpr = "3"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                    }
                }
            }
        }
    }
}

class TestNormalization {

    @Test
    fun testSimpleNormalization() = testMethod {
        method = simplifyConstantExpression
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
        method = simplifyConstantExpression
        inputExpr = "0 * (1)"

        check {
            fromExpr = "0 * (1)"
            toExpr = "0"

            explanation {
                key = GeneralExplanation.EvaluateProductContainingZero
            }
        }
    }
}

class ConstantExpressionFractionHigherOrderRootTest {
    @Test
    fun testHigherOrderRootSimplify() = testMethod {
        method = simplifyConstantExpression
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
        method = simplifyConstantExpression
        inputExpr = "1 + 0.002 : 0.6"

        check {
            fromExpr = "1 + 0.002 : 0.6"
            toExpr = "[301 / 300]"

            step {
                fromExpr = "1 + 0.002 : 0.6"
                toExpr = "1 + [0.002 / 0.6]"
                explanation {
                    key = GeneralExplanation.RewriteDivisionAsFraction
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
                toExpr = "[301 / 300]"
            }
        }
    }
}

class ConstantExpressionTests {
    @Test
    fun testCancelUnitaryDenominator() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[sqrt[8] / 2]"

        check {
            toExpr = "sqrt[2]"
        }
    }

    @Test
    fun testRationalizationIntegerAndCubeRoot() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[2 / 2 - root[3, 3]]"

        check {
            toExpr = "[8 + 4 * root[3, 3] + 2 * root[9, 3] / 5]"
        }
    }

    // this test case might need to be changed
    @Test
    fun testZeroNumerator1() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[0 / root[3, 3] + root[4, 3]]"
        check {
            fromExpr = "[0 / root[3, 3] + root[4, 3]]"
            toExpr = "0"

            step {
                fromExpr = "[0 / root[3, 3] + root[4, 3]]"
                toExpr = "[0 / 7]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }
            }

            step {
                fromExpr = "[0 / 7]"
                toExpr = "0"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFractionToInteger
                }
            }
        }
    }

    @Test
    fun testZeroNumerator2() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[15 / 5] * [0 / 2]"
        check {
            toExpr = "0"

            step {
                toExpr = "[15 / 5] * 0"
                explanation {
                    key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
                }
            }

            step {
                toExpr = "0"
                explanation {
                    key = GeneralExplanation.EvaluateProductContainingZero
                }
            }
        }
    }
}

class SimplifyToZero {
    @Test
    fun testZeroNumerator1() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[0 / -1]"

        check {
            fromExpr = "[0 / -1]"
            toExpr = "0"
            explanation {
                key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
            }
        }
    }

    @Test
    fun testZeroDivideByValue() = testMethod {
        method = simplifyConstantExpression
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
                    fromExpr = "(1 + 1)"
                    toExpr = "(2)"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }

                    step {
                        fromExpr = "1 + 1"
                        toExpr = "2"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                        }
                    }
                }

                step {
                    fromExpr = "(2)"
                    toExpr = "2"
                    explanation {
                        key = GeneralExplanation.RemoveRedundantBracket
                    }
                }
            }

            step {
                fromExpr = "0 : 2"
                toExpr = "0"

                step {
                    fromExpr = "0 : 2"
                    toExpr = "[0 / 2]"
                    explanation {
                        key = GeneralExplanation.RewriteDivisionAsFraction
                    }
                }

                step {
                    fromExpr = "[0 / 2]"
                    toExpr = "0"
                    explanation {
                        key = FractionArithmeticExplanation.SimplifyFractionToInteger
                    }
                }
            }
        }
    }
}

class SimplifyToUndefined {
    @Test
    fun testZeroDenominator1() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[5 - 4 / 1 - 1] + 2"

        check {
            fromExpr = "[5 - 4 / 1 - 1] + 2"
            toExpr = "UNDEFINED"

            step {
                fromExpr = "[5 - 4 / 1 - 1] + 2"
                toExpr = "[1 / 1 - 1] + 2"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
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
                fromExpr = "[1 / 1 - 1] + 2"
                toExpr = "[1 / 0] + 2"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }

                step {
                    fromExpr = "1 - 1"
                    toExpr = "0"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                    }
                }
            }

            step {
                fromExpr = "[1 / 0] + 2"
                toExpr = "UNDEFINED"
                explanation {
                    key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                }
            }
        }
    }

    @Test
    fun testDivisionByZero1() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "(5 - 4) : (1 - 1) + 2"

        check {
            fromExpr = "(5 - 4) : (1 - 1) + 2"
            toExpr = "UNDEFINED"

            step {
                fromExpr = "(5 - 4) : (1 - 1) + 2"
                toExpr = "1 : (1 - 1) + 2"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }

                step {
                    fromExpr = "(5 - 4)"
                    toExpr = "(1)"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
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
                    fromExpr = "(1)"
                    toExpr = "1"
                    explanation {
                        key = GeneralExplanation.RemoveRedundantBracket
                    }
                }
            }

            step {
                fromExpr = "1 : (1 - 1) + 2"
                toExpr = "1 : 0 + 2"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }

                step {
                    fromExpr = "(1 - 1)"
                    toExpr = "(0)"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }

                    step {
                        fromExpr = "1 - 1"
                        toExpr = "0"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }
                }

                step {
                    fromExpr = "(0)"
                    toExpr = "0"
                    explanation {
                        key = GeneralExplanation.RemoveRedundantBracket
                    }
                }
            }

            step {
                fromExpr = "1 : 0 + 2"
                toExpr = "UNDEFINED"

                step {
                    fromExpr = "1 : 0 + 2"
                    toExpr = "[1 / 0] + 2"
                    explanation {
                        key = GeneralExplanation.RewriteDivisionAsFraction
                    }
                }

                step {
                    fromExpr = "[1 / 0] + 2"
                    toExpr = "UNDEFINED"
                    explanation {
                        key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                    }
                }
            }
        }
    }

    @Test
    fun testZeroDenominator2() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[0 / 1 - 1]"

        check {
            toExpr = "UNDEFINED"
        }
    }

    // step-by-step of this needs to be improved
    @Test
    fun testDivisionByZero2() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "0:(1 - 1)"

        check {
            toExpr = "UNDEFINED"
        }
    }
}
