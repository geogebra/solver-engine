package methods.integerarithmetic

import engine.context.Context
import engine.methods.testMethod
import methods.general.GeneralExplanation
import kotlin.test.Test

class IntegerArithmeticPlansTest {

    @Test
    fun testSimplifyArithmeticExpressionSimple() = testMethod { // change to gmTestMethod
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "1 + 2 + 3"
        context = context.copy(advancedBalancing = true)

        check {
            toExpr = "6"

            step {
                fromExpr = "1 + 2 + 3"
                toExpr = "3 + 3"
                // gmToExpr = "3 + 3"

                combine {
                    fromPaths("./0", "./1", "./1:outerOp")
                    toPaths("./0")
                }

                shift {
                    fromPaths("./2")
                    toPaths("./1")
                }
            }
            step {
                fromExpr = "3 + 3"
                toExpr = "6"

                combine {
                    fromPaths("./0", "./1", "./1:outerOp")
                    toPaths(".")
                }
            }
        }
    }

    @Test
    fun testSimplifyArithmeticExpressionPowerSmall() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "[2 ^ 4]"

        check {
            toExpr = "16"

            step {
                fromExpr = "[2 ^ 4]"
                toExpr = "2 * 2 * 2 * 2"
            }

            step {
                fromExpr = "2 * 2 * 2 * 2"
                toExpr = "16"

                step {
                    fromExpr = "2 * 2 * 2 * 2"
                    toExpr = "4 * 2 * 2"
                }

                step {
                    fromExpr = "4 * 2 * 2"
                    toExpr = "8 * 2"
                }

                step {
                    fromExpr = "8 * 2"
                    toExpr = "16"
                }
            }
        }
    }

    @Test
    fun testSimplifyArithmeticExpressionPowerLarge() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "[2 ^ 6]"

        check {
            toExpr = "64"
        }
    }

    @Test
    fun testWithDifferentBrackets() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "1 + {.2 + [.3 + (4 + 5).].}"

        check {
            toExpr = "15"

            step {
                toExpr = "1 + {.2 + [.3 + 9.].}"
            }
            step {
                toExpr = "1 + {.2 + 12.}"
            }
            step {
                toExpr = "1 + 14"
            }
            step {
                toExpr = "15"
            }
        }
    }

    @Test
    fun testPlusNegative() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "1+-2"

        check {
            fromExpr = "1+-2"
            toExpr = "-1"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateArithmeticExpression
            }

            step {
                fromExpr = "1 + -2"
                toExpr = "1 - 2"
                explanation {
                    key = GeneralExplanation.NormalizeNegativeSignOfIntegerInSum
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
    fun testPlusNegativeGm() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "1+-2"
        context = Context(gmFriendly = true)

        check {
            fromExpr = "1+-2"
            toExpr = "-1"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateArithmeticExpression
            }

            step {
                fromExpr = "1+-2"
                toExpr = "1 - 2"
                explanation {
                    key = GeneralExplanation.NormalizeNegativeSignOfIntegerInSum
                }
            }
            step { toExpr = "-1" }
        }
    }

    @Test
    fun testSimplifyArithmeticExpressionAddMultiplyDivide() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "3*4*5:6 + 6 + 7"

        check {
            toExpr = "23"

            step {
                toExpr = "10 + 6 + 7"

                step {
                    fromExpr = "3 * 4 * 5 : 6"
                    toExpr = "12 * 5 : 6"
                }
                step {
                    toExpr = "60 : 6"
                    explanation {
                        key = Explanation.EvaluateIntegerProduct
                        param { expr = "12" }
                        param { expr = "5" }
                    }
                }
                step {
                    toExpr = "10"
                    explanation {
                        key = Explanation.EvaluateIntegerDivision
                        param { expr = "60" }
                        param { expr = "6" }
                    }
                }
            }

            step {
                toExpr = "23"

                step { toExpr = "16 + 7" }
                step { toExpr = "23" }
            }
        }
    }

    @Test
    fun testSimplifyArithmeticExpressionBracketsAndNegativeMultiply() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "34 + 60 + 6 - (4 + 10 - 3 * 5 * (-2))"

        check {
            toExpr = "56"

            explanation {
                key = Explanation.EvaluateArithmeticExpression
            }

            step {
                fromExpr = "34 + 60 + 6 - (4 + 10 - 3 * 5 * (-2))"
                toExpr = "34 + 60 + 6 - 44"

                step {
                    fromExpr = "(4 + 10 - 3 * 5 * (-2))"
                    toExpr = "(4 + 10 - (-30))"

                    explanation {
                        key = Explanation.EvaluateProductOfIntegers
                    }

                    step {
                        fromExpr = "3 * 5 * (-2)"
                        toExpr = "15 * (-2)"

                        combine {
                            fromPaths("./0", "./1", "./1:outerOp")
                            toPaths("./0")
                        }

                        shift {
                            fromPaths("./2")
                            toPaths("./1")
                        }
                    }

                    step {
                        fromExpr = "15 * (-2)"
                        toExpr = "(-30)"
                    }
                }

                step {
                    fromExpr = "(4 + 10 - (-30))"
                    toExpr = "(4 + 10 + 30)"

                    explanation {
                        key = methods.general.Explanation.SimplifyDoubleMinus
                    }
                }

                step {
                    fromExpr = "(4 + 10 + 30)"
                    toExpr = "44"

                    explanation {
                        key = Explanation.EvaluateSumOfIntegers
                    }
                }
            }

            step {
                fromExpr = "34 + 60 + 6 - 44"
                toExpr = "56"

                explanation {
                    key = Explanation.EvaluateSumOfIntegers
                }
            }
        }
    }

    @Test
    fun testEvaluateArithmeticExpressionPowerAndBrackets() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "[(1 + 1) ^ [2 ^ 3]]"

        check {
            toExpr = "256"

            step {
                fromExpr = "[(1 + 1) ^ [2 ^ 3]]"
                toExpr = "[2 ^ [2 ^ 3]]"

                step {
                    fromExpr = "(1 + 1)"
                    toExpr = "2"
                }
            }

            step {
                fromExpr = "[2 ^ [2 ^ 3]]"
                toExpr = "[2 ^ 8]"

                step {
                    fromExpr = "[2 ^ 3]"
                    toExpr = "2 * 2 * 2"
                }

                step {
                    fromExpr = "2 * 2 * 2"
                    toExpr = "8"
                }
            }

            step {
                toExpr = "256"
            }
        }
    }

    @Test
    fun testDividingByZero() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "5 : (4 - 4)"

        check {
            toExpr = "/undefined/"

            step {
                toExpr = "5 : 0"
            }

            step {
                toExpr = "/undefined/"
            }
        }
    }

    @Test
    fun testEvaluateSignedIntegerPowerPlan() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "[4^2]"

        check {
            toExpr = "16"

            step {
                fromExpr = "[4^2]"
                toExpr = "4 * 4"
            }
            step {
                fromExpr = "4 * 4"
                toExpr = "16"
            }
        }
    }

    @Test
    fun testEvaluateSignedIntegerPower1() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "[2 ^ 6]"

        check {
            fromExpr = "[2 ^ 6]"
            toExpr = "64"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
            }
        }
    }

    @Test
    fun testEvaluateSignedIntegerPower2() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
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
    fun testZeroToThePowerZero() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "[0 ^ 0]"

        check {
            fromExpr = "[0 ^ 0]"
            toExpr = "/undefined/"
            explanation {
                key = GeneralExplanation.EvaluateZeroToThePowerOfZero
            }
        }
    }

    @Test
    fun testZeroToThePowerOfFour() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "[12 ^ 3] + [0 ^ 4] - 16"

        check {
            fromExpr = "[12 ^ 3] + [0 ^ 4] - 16"
            toExpr = "1712"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateArithmeticExpression
            }

            step {
                fromExpr = "[12 ^ 3] + [0 ^ 4] - 16"
                toExpr = "1728 + [0 ^ 4] - 16"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerPower
                }
                // sub-steps omitted
            }

            step {
                fromExpr = "1728 + [0 ^ 4] - 16"
                toExpr = "1728 + 0 - 16"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerPower
                }

                // this sub-step is necessary for the test-case
                step {
                    fromExpr = "[0 ^ 4]"
                    toExpr = "0"
                    explanation {
                        key = GeneralExplanation.EvaluateZeroToAPositivePower
                    }
                }
            }

            step {
                fromExpr = "1728 + 0 - 16"
                toExpr = "1712"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateSumOfIntegers
                }
                // sub-steps omitted
            }
        }
    }

    @Test
    fun testNoTransformationNonDivisible() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "5 + 6*3:5"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test x - (+y)`() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "1 + 2 - (+2)"

        check {
            fromExpr = "1 + 2 - (+2)"
            toExpr = "1"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateArithmeticExpression
            }

            step {
                fromExpr = "1 + 2 - (+2)"
                toExpr = "1 + 2 - 2"
                explanation {
                    key = GeneralExplanation.RemoveRedundantPlusSign
                }
            }

            step {
                fromExpr = "1 + 2 - 2"
                toExpr = "1"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateSumOfIntegers
                }
            }
        }
    }

    @Test
    fun `test -x times ZERO + y`() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "-4*0 + 1"

        check {
            fromExpr = "-4 * 0 + 1"
            toExpr = "1"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateArithmeticExpression
            }
            step {
                fromExpr = "-4 * 0 + 1"
                toExpr = "0 + 1"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateProductOfIntegers
                }
                step {
                    fromExpr = "-4 * 0"
                    toExpr = "0"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
            step { }
        }
    }

    @Test
    fun `test z - x times ZERO`() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "1 - 4*0"
        check {
            fromExpr = "1 - 4 * 0"
            toExpr = "1"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateArithmeticExpression
            }
            step {
                fromExpr = "1 - 4 * 0"
                toExpr = "1 - 0"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateProductOfIntegers
                }
                step {
                    fromExpr = "4 * 0"
                    toExpr = "0"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
            step { }
        }
    }

    @Test
    fun `test -xy + z`() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "-4*3 + 1"
        check {
            fromExpr = "-4 * 3 + 1"
            toExpr = "-11"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateArithmeticExpression
            }
            step {
                fromExpr = "-4 * 3 + 1"
                toExpr = "-12 + 1"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateProductOfIntegers
                }
                step {
                    fromExpr = "-4 * 3"
                    toExpr = "-12"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
            step { }
        }
    }

    @Test
    fun `test - x times ZERO`() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "-14*0"
        check {
            fromExpr = "-14 * 0"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
            }
            toExpr = "0"
        }
    }

    @Test
    fun `test - x times y`() = testMethod {
        method = IntegerArithmeticPlans.EvaluateArithmeticExpression
        inputExpr = "-2*3"
        check {
            fromExpr = "-2 * 3"
            explanation {
                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
            }
            toExpr = "-6"
        }
    }
}
