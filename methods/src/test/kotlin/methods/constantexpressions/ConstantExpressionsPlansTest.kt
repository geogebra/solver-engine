package methods.constantexpressions

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
            step { toExpr = "[7 / 2] * [4 / 21]" }
            step {
                step { toExpr = "[7 * 4 / 2 * 21]" }
                step {
                    step { toExpr = "[7 * 4 / 2 * 7 * 3]" }
                    step { toExpr = "[4 / 2 * 3]" }
                    step { toExpr = "[2 * 2 / 2 * 3]" }
                    step { toExpr = "[2 / 3]" }
                }
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
            step { toExpr = "[3 / 1] * [2 ^ -2]" }
            step { toExpr = "[3 / 1] * [1 / 4]" }
            // TODO this is not good
            step { toExpr = "3 * [1 / 4]" }
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

class ConstantExpressionRationalizationTest {
    @Test
    fun testRationalizeCubeRootDenominator1() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[2 / root[5, 3] + root[3, 3]]"

        check {
            toExpr = "[root[25, 3] - root[15, 3] + root[9, 3] / 4]"

            step {
                toExpr = "[2 * ([(root[5, 3]) ^ 2] - (root[5, 3]) * (root[3, 3]) + [(root[3, 3]) ^ 2]) / 8]"
            }

            step {
                toExpr = "[2 * ([(root[5, 3]) ^ 2] - root[5, 3] * (root[3, 3]) + [(root[3, 3]) ^ 2]) / 8]"
            }

            step {
                toExpr = "[2 * ([(root[5, 3]) ^ 2] - root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2]) / 8]"
            }

            step {
                toExpr = "[2 * (root[[5 ^ 2], 3] - root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2]) / 8]"
            }

            step {
                toExpr = "[2 * (root[25, 3] - root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2]) / 8]"
            }

            step {
                toExpr = "[2 * (root[25, 3] - root[5, 3] * root[3, 3] + root[[3 ^ 2], 3]) / 8]"
            }

            step {
                toExpr = "[2 * (root[25, 3] - root[5, 3] * root[3, 3] + root[9, 3]) / 8]"
            }

            step {
                toExpr = "[(root[25, 3] - root[5, 3] * root[3, 3] + root[9, 3]) / 4]"
            }

            step {
                toExpr = "[root[25, 3] - root[5, 3] * root[3, 3] + root[9, 3] / 4]"
            }

            step {
                toExpr = "[root[25, 3] - root[15, 3] + root[9, 3] / 4]"
            }
        }
    }

    @Test
    fun testRationalizeCubeRootDenominator2() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[2 / -root[5, 3] + root[3, 3]]"

        check {
            toExpr = "-(root[9, 3] + root[15, 3] + root[25, 3])"

            step {
                toExpr = "[2 * ([(root[3, 3]) ^ 2] + (root[3, 3]) * (root[5, 3]) + [(root[5, 3]) ^ 2]) / -2]"
            }

            step {
                toExpr = "[2 * ([(root[3, 3]) ^ 2] + root[3, 3] * (root[5, 3]) + [(root[5, 3]) ^ 2]) / -2]"
            }

            step {
                toExpr = "[2 * ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / -2]"
            }

            step {
                toExpr = "[2 * (root[[3 ^ 2], 3] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / -2]"
            }

            step {
                toExpr = "[2 * (root[9, 3] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / -2]"
            }

            step {
                toExpr = "[2 * (root[9, 3] + root[3, 3] * root[5, 3] + root[[5 ^ 2], 3]) / -2]"
            }

            step {
                toExpr = "[2 * (root[9, 3] + root[3, 3] * root[5, 3] + root[25, 3]) / -2]"
            }

            step {
                toExpr = "-[2 * (root[9, 3] + root[3, 3] * root[5, 3] + root[25, 3]) / 2]"
            }

            step {
                toExpr = "-(root[9, 3] + root[3, 3] * root[5, 3] + root[25, 3])"
            }

            step {
                toExpr = "-(root[9, 3] + root[15, 3] + root[25, 3])"
            }
        }
    }

    @Test
    fun testRationalizeHigherOrderRoot1() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[9 / 2 * root[18, 4]]"

        check {
            fromExpr = "[9 / 2 * root[18, 4]]"
            toExpr = "[3 * root[72, 4] / 4]"

            step {
                fromExpr = "[9 / 2 * root[18, 4]]"
                toExpr = "[9 * root[72, 4] / 12]"

                step {
                    fromExpr = "[9 / 2 * root[18, 4]]"
                    toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]]"
                }

                step {
                    fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]]"
                    toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                        "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 2], 4]]"

                    step {
                        fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]]"
                        toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                            "[root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4] / root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                    }

                    step {
                        fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                            "[root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4] / root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                        toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                            "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 2], 4]]"
                    }
                }

                step {
                    fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                        "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 2], 4]]"
                    toExpr = "[9 * root[[2 ^ 3] * [3 ^ 2], 4] / " +
                        "2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                }

                step {
                    fromExpr = "[9 * root[[2 ^ 3] * [3 ^ 2], 4] / " +
                        "2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                    toExpr = "[9 * root[72, 4] / 2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"

                    step {
                        fromExpr = "9 * root[[2 ^ 3] * [3 ^ 2], 4]"
                        toExpr = "9 * root[72, 4]"

                        step {
                            fromExpr = "9 * root[[2 ^ 3] * [3 ^ 2], 4]"
                            toExpr = "9 * root[8 * [3 ^ 2], 4]"
                        }

                        step {
                            fromExpr = "9 * root[8 * [3 ^ 2], 4]"
                            toExpr = "9 * root[8 * 9, 4]"
                        }

                        step {
                            fromExpr = "9 * root[8 * 9, 4]"
                            toExpr = "9 * root[72, 4]"

                            step {
                                fromExpr = "8 * 9"
                                toExpr = "72"
                            }
                        }
                    }
                }

                step {
                    fromExpr = "[9 * root[72, 4] / 2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                    toExpr = "[9 * root[72, 4] / 12]"

                    step {
                        fromExpr = "2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]"
                        toExpr = "2 * root[[2 ^ 1 + 3] * [3 ^ 2 + 2], 4]"
                    }

                    step {
                        fromExpr = "2 * root[[2 ^ 1 + 3] * [3 ^ 2 + 2], 4]"
                        toExpr = "2 * root[[2 ^ 4] * [3 ^ 4], 4]"
                    }

                    step {
                        fromExpr = "2 * root[[2 ^ 4] * [3 ^ 4], 4]"
                        toExpr = "2 * root[[(2 * 3) ^ 4], 4]"
                    }

                    step {
                        fromExpr = "2 * root[[(2 * 3) ^ 4], 4]"
                        toExpr = "2 * root[[(6) ^ 4], 4]"

                        step {
                            fromExpr = "2 * 3"
                            toExpr = "6"
                        }
                    }

                    step {
                        fromExpr = "2 * root[[(6) ^ 4], 4]"
                        toExpr = "2 * 6"
                    }

                    step {
                        fromExpr = "2 * 6"
                        toExpr = "12"
                    }
                }
            }

            step {
                fromExpr = "[9 * root[72, 4] / 12]"
                toExpr = "[3 * root[72, 4] / 4]"

                step {
                    fromExpr = "[9 * root[72, 4] / 12]"
                    toExpr = "[3 * 3 * root[72, 4] / 3 * 4]"
                }

                step {
                    fromExpr = "[3 * 3 * root[72, 4] / 3 * 4]"
                    toExpr = "[3 * root[72, 4] / 4]"
                }
            }
        }
    }

    @Test
    fun testRationalizeHigherOrderRoot2() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[2/root[2, 3]]"

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
                        explanation {
                            key = IntegerRootsExplanation.BringSameIndexSameFactorRootsAsOneRoot
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
    fun testSimplifyArithmeticExpression3() = testMethod {
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
                    explanation {
                        key = IntegerRootsExplanation.BringSameIndexSameFactorRootsAsOneRoot
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
