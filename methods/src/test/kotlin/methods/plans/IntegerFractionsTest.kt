package methods.plans

import engine.steps.metadata.Explanation
import engine.steps.metadata.PlanExplanation
import engine.steps.metadata.Skill
import org.junit.jupiter.api.Test

class TestAddFractions {

    @Test
    fun addLikeFractionsTest() = testPlan {
        plan = evaluatePositiveFractionSum
        inputExpr = "[1/5] + [2/5]"

        check {
            toExpr = "[3/5]"

            explanation {
                key = PlanExplanation.AddFractions

                param {
                    expr = "[1/5]"
                }
                param {
                    expr = "[2/5]"
                }
            }

            skill {
                key = Skill.AddFractions

                param {
                    expr = "[1/5]"
                }
                param {
                    expr = "[2/5]"
                }
            }

            step {
                toExpr = "[1 + 2/5]"

                explanation {
                    key = Explanation.AddLikeFractions
                }
            }

            step {
                fromExpr = "[1 + 2/5]"
                toExpr = "[3/5]"

                step {
                    fromExpr = "1 + 2"
                    toExpr = "3"
                }
            }
        }
    }

    @Test
    fun addUnlikeFractionsTest() = testPlan {
        plan = evaluatePositiveFractionSum
        inputExpr = "[1/3] + [2/5]"

        check {
            toExpr = "[11 / 15]"

            step {
                toExpr = "[1 * 5 / 3 * 5] + [2 * 3 / 5 * 3]"
            }

            step {
                toExpr = "[5 / 15] + [6 / 15]"
            }

            step {
                toExpr = "[5 + 6 / 15]"
            }

            step {
                toExpr = "[11 / 15]"
            }
        }
    }

    @Test
    fun testAddFractionsWithCommonFactor() = testPlan {
        plan = evaluatePositiveFractionSum
        inputExpr = "[1 / 4] + [1 / 4]"

        check {
            toExpr = "[1 / 2]"
        }
    }

    @Test
    fun testSumSimplifies() = testPlan {
        plan = evaluatePositiveFractionSum
        inputExpr = "[1 / 4] + [1 / 4]"

        check {
            toExpr = "[1 / 2]"

            step {
                toExpr = "[1 + 1 / 4]"
            }

            step {
                toExpr = "[2 / 4]"
            }

            step {
                toExpr = "[1 / 2]"
            }
        }
    }

    @Test
    fun testSumIsInteger() = testPlan {
        plan = evaluatePositiveFractionSum
        inputExpr = "[3 / 5] + [7 / 5]"

        check {
            toExpr = "2"

            step {
                toExpr = "[3 + 7 / 5]"
            }

            step {
                toExpr = "[10 / 5]"
            }

            step {
                toExpr = "2"
            }
        }
    }

    @Test
    fun testSumDoesNotSimplify() = testPlan {
        plan = evaluatePositiveFractionSum
        inputExpr = "[2 / 5] + [1 / 3]"

        check {

            step {
                toExpr = "[2 * 3 / 5 * 3] + [1 * 5 / 3 * 5]"
            }

            step {
                toExpr = "[6 / 15] + [5 / 15]"
            }

            step {
                toExpr = "[6 + 5 / 15]"
            }

            step {
                toExpr = "[11 / 15]"
            }
        }
    }

    @Test
    fun testSubtract() = testPlan {
        plan = evaluatePositiveFractionSum
        inputExpr = "[3 / 5] - [1 / 4]"

        check {
            toExpr = "[7 / 20]"
        }
    }

    @Test
    fun testAddNegatives() = testPlan {
        plan = evaluatePositiveFractionSum
        inputExpr = "- [3 / 5] - [1 / 4]"

        check {
            toExpr = "-[17 / 20]"
        }
    }
}

class TestSimplifyNumericFraction {
    @Test
    fun testToInteger() = testPlan {
        plan = simplifyNumericFraction
        inputExpr = "[40 / 8]"
        check {
            toExpr = "5"
        }
    }

    @Test
    fun testWithDenominatorEqualTo1() = testPlan {
        plan = simplifyNumericFraction
        inputExpr = "[7 / 1]"
        check {
            toExpr = "7"
        }
    }

    @Test
    fun testWithGCD() = testPlan {
        plan = simplifyNumericFraction
        inputExpr = "[28 / 42]"
        check {
            toExpr = "[2 / 3]"

            step {
                toExpr = "[14 * 2 / 14 * 3]"
            }
            step {
                toExpr = "[2 / 3]"
            }
        }
    }

    @Test
    fun testNoSimplification() = testPlan {
        plan = simplifyNumericFraction
        inputExpr = "[3 / 4]"
        check {
            noTransformation()
        }
    }
}

class TestCombineFractionsInExpression {

    @Test
    fun simpleTest() = testPlan {
        plan = combineFractionsInExpression
        inputExpr = "[1 / 3] + [2 / 3] * [1 / 2]"

        check {
            toExpr = "[2 / 3]"

            step {
                toExpr = "[1 / 3] + [1 / 3]"
            }

            step {
                toExpr = "[2 / 3]"
            }
        }
    }

    @Test
    fun testWithNegativesInFractions() = testPlan {
        plan = combineFractionsInExpression
        inputExpr = "[-1 / 6] * [2 / -5]"

        check {
            toExpr = "[1 / 15]"

            step {
                toExpr = "[1 / 6] * [2 / 5]"
            }

            step {
                toExpr = "[1 / 15]"
            }
        }
    }

    @Test
    fun testWithMoreNegativesInFractions() = testPlan {
        plan = combineFractionsInExpression
        inputExpr = "(-[1 / 3]) * [-1 / 4] * [3 / -2]"

        check {
            toExpr = "-[1 / 8]"

            step {
                toExpr = "- [1 / 3] * [1 / 4] * [3 / 2]"
            }

            step {
                toExpr = "- [1 / 12] * [3 / 2]"
            }

            step {
                toExpr = "-[1 / 8]"
            }
        }
    }

    @Test
    fun testDividingTwice() = testPlan {
        plan = combineFractionsInExpression
        inputExpr = "3 : 4 : 5"

        check {
            toExpr = "[3 / 20]"

            step {
                toExpr = "[3/4] * [1/5]"

                step {
                    toExpr = "[3/4] : 5"
                }
                step {
                    toExpr = "[[3/4] / 5]"
                }
                step {
                    toExpr = "[3/4] * [1/5]"
                }
            }
            step {
                toExpr = "[3/20]"
            }
        }
    }

    @Test
    fun testDividingFractions() = testPlan {
        plan = combineFractionsInExpression
        inputExpr = "[5/6] : [3/4]"

        check {
            toExpr = "[10/9]"

            step {
                toExpr = "[5/6] * [4/3]"
            }
            step {
                toExpr = "[10/9]"
            }
        }
    }

    @Test
    fun testDividingWithNegatives() = testPlan {
        plan = combineFractionsInExpression
        inputExpr = "3:(-5)"

        check {
            toExpr = "-[3/5]"

            step {
                toExpr = "- 3:5"
            }

            step {
                toExpr = "-[3/5]"
            }
        }
    }

    @Test
    fun testFractionExponent() = testPlan {
        plan = combineFractionsInExpression
        inputExpr = "[(-[1/2])^3] + [([2/3])^-2]"

        check {
            toExpr = "[17/8]"

            step {
                toExpr = "-[([1/2])^3] + [([2/3])^-2]"
            }

            step {
                toExpr = "-[1/8] + [([2/3])^-2]"
            }

            step {
                toExpr = "-[1/8] + [9/4]"
            }

            step {
                toExpr = "[17/8]"
            }
        }
    }

    @Test
    fun testNegativeExponentsOfIntegers() = testPlan {
        plan = combineFractionsInExpression
        inputExpr = "[3 ^ -1] - [3 ^ -2]"

        check {
            toExpr = "[2/9]"

            step {
                toExpr = "[1/3] - [3 ^ -2]"
            }

            step {
                toExpr = "[1/3] - [1/9]"
            }

            step {
                toExpr = "[2/9]"
            }
        }
    }

    @Test
    fun testFractionToTheMinusOne() = testPlan {
        plan = combineFractionsInExpression
        inputExpr = "[([1 / 3])^-1] * [2 ^ -2]"

        check {
            toExpr = "[3 / 4]"

            step {
                toExpr = "[3 / 1] * [2 ^ -2]"
            }

            step {
                toExpr = "[3 / 1] * [1 / 4]"
            }

            step {
                toExpr = "[3 / 4]"
            }
        }
    }

    @Test
    fun testEvaluatePowerOfFraction() = testPlan {
        plan = evaluatePowerOfFraction
        inputExpr = "[([3/4]) ^ 2]"

        check {
            toExpr = "[9 / 16]"

            step {
                toExpr = "[ [3^2] / [4^2] ]"
            }
            step {
                toExpr = "[9 / 16]"
            }
        }
    }

    @Test
    fun testEvaluateSignedIntegerPower1() = testPlan {
        plan = evaluateSignedIntegerPower
        inputExpr = "[2 ^ 6]"

        check {
            toExpr = "64"

            step {
                toExpr = "64"
            }
        }
    }

    @Test
    fun testEvaluateSignedIntegerPower2() = testPlan {
        plan = evaluateSignedIntegerPower
        inputExpr = "[2^5]"

        check {
            toExpr = "32"

            step {
                toExpr = "2 * 2 * 2 * 2 * 2"
            }

            step {
                toExpr = "32"
            }
        }
    }
}
