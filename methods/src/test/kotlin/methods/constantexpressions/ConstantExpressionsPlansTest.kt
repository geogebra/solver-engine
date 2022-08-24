package methods.constantexpressions

import methods.general.GeneralExplanation
import methods.plans.testPlan
import org.junit.jupiter.api.Test

class ConstantExpressionsPlansTest {

    @Test
    fun simpleTest() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "[1 / 3] + [2 / 3] * [1 / 2]"

        check {
            step { toExpr = "[1 / 3] + [1 / 3]" }
            step { toExpr = "[2 / 3]" }
        }
    }

    @Test
    fun testMultiplyAndSimplify() = testPlan {
        plan = simplifyConstantExpression
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
    fun testWithNegativesInFractions() = testPlan {
        plan = simplifyConstantExpression
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
    fun testWithMoreNegativesInFractions() = testPlan {
        plan = simplifyConstantExpression
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
    fun testDividingTwice() = testPlan {
        plan = simplifyConstantExpression
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
    fun testDividingFractions() = testPlan {
        plan = simplifyConstantExpression
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
    fun testDividingWithNegatives() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "3 : (-5)"

        check {
            step { toExpr = "[3 / -5]" }
            step { toExpr = "-[3 / 5]" }
        }
    }

    @Test
    fun testFractionExponent() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "[(-[1 / 2]) ^ 3] + [([2 / 3]) ^ -2]"

        check {
            step { toExpr = "- [1 / 8] + [([2/3])^-2]" }
            step { toExpr = "- [1 / 8] + [9 / 4]" }
            step { toExpr = "[17 / 8]" }
        }
    }

    @Test
    fun testNegativeExponentsOfIntegers() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "[3 ^ -1] - [3 ^ -2]"

        check {
            step { toExpr = "[1 / 3] - [3 ^ -2]" }
            step { toExpr = "[1 / 3] - [1 / 9]" }
            step { toExpr = "[2 / 9]" }
        }
    }

    @Test
    fun testFractionToTheMinusOne() = testPlan {
        plan = simplifyConstantExpression
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
    fun testResultSimplifyPowerOfRoot() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "[(root[12, 5]) ^ 4]"

        check {
            fromExpr = "[(root[12, 5]) ^ 4]"
            toExpr = "2 * root[648, 5]"
        }
    }

    @Test
    fun testResultSimplifyPowerOfRootWithCoefficients() = testPlan {
        testPlan {
            plan = simplifyConstantExpression
            inputExpr = "[(2 * sqrt[5]) ^ 3]"

            check {
                fromExpr = "[(2 * sqrt[5]) ^ 3]"
                toExpr = "40 * sqrt[5]"
            }
        }
    }

    @Test
    fun testResultPowerOfBinomialContainingRoots() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "[(2 * sqrt[6] + 3 * sqrt[2]) ^ 2]"

        check {
            fromExpr = "[(2 * sqrt[6] + 3 * sqrt[2]) ^ 2]"
            toExpr = "42 + 24 * sqrt[3]"

            // Currently has 10 steps!
        }
    }

    @Test
    fun testSimplifyRootOfRoot() = testPlan {
        plan = simplifyConstantExpression
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
    fun testSimplifyRootOfRootWithCoefficient() = testPlan {
        plan = simplifyConstantExpression
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

class TestNormalization {

    @Test
    fun testSimpleNormalization() = testPlan {
        plan = simplifyConstantExpression
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
    fun testNoNormalizationIfNotNeeded() = testPlan {
        plan = simplifyConstantExpression
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
