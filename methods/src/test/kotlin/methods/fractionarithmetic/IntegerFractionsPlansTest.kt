package methods.fractionarithmetic

import engine.steps.metadata.Skill
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.plans.testPlan
import org.junit.jupiter.api.Test

class TestAddFractions {

    @Test
    fun addLikeFractionsTest() = testPlan {
        plan = evaluateFractionSum
        inputExpr = "[1/5] + [2/5]"

        check {
            toExpr = "[3/5]"

            explanation {
                key = Explanation.EvaluateFractionSum

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
        plan = evaluateFractionSum
        inputExpr = "[1/3] + [2/5]"

        check {
            step { toExpr = "[1 * 5 / 3 * 5] + [2 * 3 / 5 * 3]" }
            step { toExpr = "[5 / 15] + [6 / 15]" }
            step { toExpr = "[5 + 6 / 15]" }
            step { toExpr = "[11 / 15]" }
        }
    }

    @Test
    fun testAddFractionsWithCommonFactor() = testPlan {
        plan = evaluateFractionSum
        inputExpr = "[1 / 4] + [1 / 4]"

        check {
            toExpr = "[1 / 2]"
        }
    }

    @Test
    fun testSumSimplifies() = testPlan {
        plan = evaluateFractionSum
        inputExpr = "[1 / 4] + [1 / 4]"

        check {
            step { toExpr = "[1 + 1 / 4]" }
            step { toExpr = "[2 / 4]" }
            step { toExpr = "[1 / 2]" }
        }
    }

    @Test
    fun testSumIsInteger() = testPlan {
        plan = evaluateFractionSum
        inputExpr = "[3 / 5] + [7 / 5]"

        check {
            step { toExpr = "[3 + 7 / 5]" }
            step { toExpr = "[10 / 5]" }
            step { toExpr = "2" }
        }
    }

    @Test
    fun testSumDoesNotSimplify() = testPlan {
        plan = evaluateFractionSum
        inputExpr = "[2 / 5] + [1 / 3]"

        check {
            step { toExpr = "[2 * 3 / 5 * 3] + [1 * 5 / 3 * 5]" }
            step { toExpr = "[6 / 15] + [5 / 15]" }
            step { toExpr = "[6 + 5 / 15]" }
            step { toExpr = "[11 / 15]" }
        }
    }

    @Test
    fun testSubtract() = testPlan {
        plan = evaluateFractionSum
        inputExpr = "[3 / 5] - [1 / 4]"

        check {
            toExpr = "[7 / 20]"
        }
    }

    @Test
    fun testAddNegatives() = testPlan {
        plan = evaluateFractionSum
        inputExpr = "- [3 / 5] - [1 / 4]"

        check {
            toExpr = "-[17 / 20]"
        }
    }
}

class TestSimplifyFraction {

    @Test
    fun testToInteger() = testPlan {
        plan = simplifyFraction
        inputExpr = "[40 / 8]"

        check {
            toExpr = "5"
        }
    }

    @Test
    fun testWithDenominatorEqualTo1() = testPlan {
        plan = simplifyFraction
        inputExpr = "[7 / 1]"

        check {
            toExpr = "7"
        }
    }

    @Test
    fun testWithGCD() = testPlan {
        plan = simplifyFraction
        inputExpr = "[28 / 42]"

        check {
            step { toExpr = "[14 * 2 / 14 * 3]" }
            step { toExpr = "[2 / 3]" }
        }
    }

    @Test
    fun testAlreadyFactorized() = testPlan {
        plan = simplifyFraction
        inputExpr = "[3 * 4 / 4 * 5]"

        check {
            step { toExpr = "[3 / 5]" }
        }
    }

    @Test
    fun testAlreadyPartiallyFactorized() = testPlan {
        plan = simplifyFraction
        inputExpr = "[14 * 12 / 21 * 6]"

        check {
            step { toExpr = "[7 * 2 * 12 / 7 * 3 * 6]" }
            step { toExpr = "[2 * 12 / 3 * 6]" }
            step { toExpr = "[2 * 12 / 3 * 2 * 3]" }
            step { toExpr = "[12 / 3 * 3]" }
            step { toExpr = "[3 * 4 / 3 * 3]" }
            step { toExpr = "[4 / 3]" }
        }
    }

    @Test
    fun testNoSimplification() = testPlan {
        plan = simplifyFraction
        inputExpr = "[3 / 4]"

        check {
            noTransformation()
        }
    }

    @Test
    fun testEvaluatePowerOfFraction() = testPlan {
        plan = evaluateFractionPower
        inputExpr = "[([3 / 4]) ^ 2]"

        check {
            step { toExpr = "[[3 ^ 2] / [4 ^ 2]]" }
            step { toExpr = "[9 / 16]" }
        }
    }

    @Test
    fun testEvaluatePositiveFractionPower() = testPlan {
        plan = evaluateFractionPower
        inputExpr = "[([2 / 3]) ^ 2]"

        check {
            toExpr = "[4 / 9]"
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

    @Test
    fun testEvaluateSumOfFractionAnInteger() = testPlan {
        plan = evaluateSumOfFractionAndInteger
        inputExpr = "[2/5] + 3"

        check {
            toExpr = "[17/5]"
        }
    }
}
