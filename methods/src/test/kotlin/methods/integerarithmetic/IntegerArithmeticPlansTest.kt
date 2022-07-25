package methods.integerarithmetic

import methods.plans.testPlan
import kotlin.test.Test

class IntegerArithmeticPlansTest {

    @Test
    fun testSimplifyArithmeticExpressionSimple() = testPlan {
        plan = evaluateArithmeticExpression
        inputExpr = "1 + 2 + 3"

        check {
            toExpr = "6"

            step {
                step {
                    fromExpr = "1 + 2 + 3"
                    toExpr = "3 + 3"

                    combine {
                        fromPaths("./0", "./1")
                        toPaths("./0")
                    }

                    move {
                        fromPaths("./2")
                        toPaths("./1")
                    }
                }
                step {
                    fromExpr = "3 + 3"
                    toExpr = "6"

                    combine {
                        fromPaths("./0", "./1")
                        toPaths(".")
                    }
                }
            }
        }
    }

    @Test
    fun testSimplifyArithmeticExpressionPowerSmall() = testPlan {
        plan = evaluateArithmeticExpression
        inputExpr = "[2 ^ 4]"

        check {
            toExpr = "16"

            step {
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
    }

    @Test
    fun testSimplifyArithmeticExpressionPowerLarge() = testPlan {
        plan = evaluateArithmeticExpression
        inputExpr = "[2 ^ 6]"

        check {
            toExpr = "64"
        }
    }

    @Test
    fun testWithDifferentBrackets() = testPlan {
        plan = evaluateArithmeticExpression
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
    fun testSimplifyArithmeticExpressionAddMultiplyDivide() = testPlan {
        plan = evaluateArithmeticExpression
        inputExpr = "3*4*5:6 + 6 + 7"

        check {
            toExpr = "23"

            step {
                toExpr = "10 + 6 + 7"

                step {
                    fromExpr = "3*4*5:6"
                    toExpr = "10"

                    step { toExpr = "12*5:6" }
                    step {
                        toExpr = "60:6"
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
            }

            step {
                toExpr = "23"

                step {
                    step { toExpr = "16 + 7" }
                    step { toExpr = "23" }
                }
            }
        }
    }

    @Test
    fun testSimplifyArithmeticExpressionBracketsAndNegativeMultiply() = testPlan {
        plan = evaluateArithmeticExpression
        inputExpr = "34 + 60 + 6 - (4 + 10 - 3 * 5 * (-2))"

        check {
            toExpr = "56"

            explanation {
                key = Explanation.EvaluateArithmeticExpression
            }

            step {
                step {
                    fromExpr = "34 + 60 + 6 - (4 + 10 - 3 * 5 * (-2))"
                    toExpr = "34 + 60 + 6 - 44"

                    step {
                        fromExpr = "(4 + 10 - 3 * 5 * (-2))"
                        toExpr = "(4 + 10 - (-30))"

                        step {
                            explanation {
                                key = Explanation.EvaluateProductOfIntegers
                            }

                            step {
                                fromExpr = "3 * 5 * (-2)"
                                toExpr = "15 * (-2)"

                                combine {
                                    fromPaths("./0", "./1")
                                    toPaths("./0")
                                }

                                move {
                                    fromPaths("./2")
                                    toPaths("./1")
                                }
                            }
                            step {
                                fromExpr = "15 * (-2)"
                                toExpr = "-30"
                            }
                        }
                    }

                    step {
                        fromExpr = "(4 + 10 - (-30))"
                        toExpr = "(4 + 10 + 30)"

                        step {
                            fromExpr = "-(-30)"
                            toExpr = "30"

                            explanation {
                                key = methods.general.Explanation.SimplifyDoubleMinus
                            }
                        }
                    }

                    step {
                        toExpr = "(44)"

                        step {
                            fromExpr = "4 + 10 + 30"
                            toExpr = "44"

                            explanation {
                                key = Explanation.EvaluateSumOfIntegers
                            }
                        }
                    }

                    step {
                        toExpr = "44"

                        step {
                            explanation {
                                key = methods.general.Explanation.RemoveRedundantBracket
                            }
                        }
                    }
                }
            }

            step {
                step {
                    fromExpr = "34 + 60 + 6 - 44"
                    toExpr = "56"

                    explanation {
                        key = Explanation.EvaluateSumOfIntegers
                    }
                }
            }
        }
    }

    @Test
    fun testEvaluateArithmeticExpressionPowerAndBrackets() = testPlan {
        plan = evaluateArithmeticExpression
        inputExpr = "[(1 + 1) ^ [2 ^ 3]]"

        check {
            toExpr = "256"

            step {
                fromExpr = "[(1 + 1) ^ [2 ^ 3]]"
                toExpr = "[2 ^ [2 ^ 3]]"

                step {
                    step {
                        fromExpr = "(1 + 1)"
                        toExpr = "(2)"
                    }
                    step {
                        fromExpr = "(2)"
                        toExpr = "2"
                    }
                }
            }

            step {
                toExpr = "256"
                step {
                    toExpr = "[2 ^ 8]"
                    step {
                        fromExpr = "[2 ^3]"
                        toExpr = "8"
                        step {
                            fromExpr = "[2 ^ 3]"
                            toExpr = "2 * 2 * 2"
                        }

                        step {
                            fromExpr = "2 * 2 * 2"
                            toExpr = "8"
                        }
                    }
                }
                step {
                    toExpr = "256"
                }
            }
        }
    }

    // @Test
    fun testSimplifyRoots() = testPlan {
        plan = evaluateArithmeticExpression
        inputExpr = "sqrt[63]"

        check {
            toExpr = "3 * sqrt[7]"

            step {
                toExpr = "sqrt[[3 ^ 2] * 7]"
            }

            step {
                toExpr = "sqrt[[3 ^ 2]] * sqrt[7]"
            }

            step {
                toExpr = "3 * sqrt[7]"
            }
        }
    }

    @Test
    fun testEvaluateSignedIntegerPowerPlan() = testPlan {
        plan = evaluateSignedIntegerPower
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
