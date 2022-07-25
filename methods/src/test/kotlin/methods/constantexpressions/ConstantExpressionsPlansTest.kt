package methods.constantexpressions

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
                step {
                    step { toExpr = "(-[1 / 6]) * [2 / -5]" }
                    step { toExpr = "(-[1 / 6]) * (-[2 / 5])" }
                    step { toExpr = "[1 / 6] * [2 / 5]" }
                }
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
                step {
                    step { toExpr = "(-[1 / 3]) * (-[1 / 4]) * [3 / -2]" }
                    step { toExpr = "(-[1 / 3]) * (-[1 / 4]) * (-[3 / 2])" }
                    step { toExpr = "[1 / 3] * [1 / 4] * (-[3 / 2])" }
                    step { toExpr = "-[1 / 3] * [1 / 4] * [3 / 2]" }
                }
            }
            step { toExpr = "-[1 / 8]" }
        }
    }

    @Test
    fun testDividingTwice() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "3 : 4 : 5"

        check {
            step { toExpr = "[3 / 4] : 5" }
            step { toExpr = "[[3 / 4] / 5]" }
            step { toExpr = "[3 / 4] * [1 / 5]" }
            step { toExpr = "[3 / 20]" }
        }
    }

    @Test
    fun testDividingFractions() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "[5 / 6] : [3 / 4]"

        check {
            step { toExpr = "[5 / 6] * [4 / 3]" }
            step { toExpr = "[10 / 9]" }
        }
    }

    @Test
    fun testDividingWithNegatives() = testPlan {
        plan = simplifyConstantExpression
        inputExpr = "3 : (-5)"

        check {
            step { toExpr = "-3 : 5" }
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
}
