package plans

import steps.metadata.Explanation
import steps.metadata.PlanExplanation
import kotlin.test.Test

class TestSimplifyIntegerExpression {

    @Test
    fun simpleTest() = testPlan {
        plan = simplifyArithmeticExpression
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
                step { toExpr = "6" }
            }
        }
    }

    @Test
    fun testAddMultiplyDivide() = testPlan {
        plan = simplifyArithmeticExpression
        inputExpr = "3*4*5:6 + 6 + 7"

        check {
            toExpr = "23"

            step {
                toExpr = "10 + 6 + 7"

                step {
                    fromExpr = "3*4*5:6"
                    toExpr = "10"

                    step { toExpr = "12*5:6" }
                    step { toExpr = "60:6" }
                    step { toExpr = "10" }
                }
            }

            step {
                toExpr = "23"

                step {
                    toExpr = "23"

                    step { toExpr = "16 + 7" }
                    step { toExpr = "23" }
                }
            }
        }
    }

    @Test
    fun testBracketsAndNegativeMultiply() = testPlan {
        plan = simplifyArithmeticExpression
        inputExpr = "34 + 60 + 6 - (4 + 10 - 3 * 5 * (-2))"


        check {
            toExpr = "56"

            explanation {
                key = PlanExplanation.SimplifyArithmeticExpression
            }

            step {
                fromExpr = "34 + 60 + 6 - (4 + 10 - 3 * 5 * (-2))"
                toExpr = "34 + 60 + 6 - (4 + 10 - (-30))"

                step {
                    fromExpr = "3 * 5 * (-2)"
                    toExpr = "(-30)"

                    explanation {
                        key = PlanExplanation.SimplifyIntegerProduct
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
                        toExpr = "(-30)"
                    }
                }
            }

            step {
                fromExpr = "34 + 60 + 6 - (4 + 10 - (-30))"
                toExpr = "34 + 60 + 6 - (4 + 10 + 30)"

                step {
                    fromExpr = "-(-30)"
                    toExpr = "30"

                    explanation {
                        key = Explanation.SimplifyDoubleMinus
                    }
                }
            }

            step {
                fromExpr = "34 + 60 + 6 - (4 + 10 + 30)"
                toExpr = "34 + 60 + 6 - (44)"

                step {
                    fromExpr = "4 + 10 + 30"
                    toExpr = "44"

                    explanation {
                        key = PlanExplanation.SimplifyIntegerSum
                    }
                }
            }

            step {
                step {
                    fromExpr = "(44)"
                    toExpr = "44"

                    explanation {
                        key = Explanation.RemoveBracketUnsignedInteger
                    }
                }
            }

            step {

                step {
                    fromExpr = "34 + 60 + 6 - 44"
                    toExpr = "56"

                    explanation {
                        key = PlanExplanation.SimplifyIntegerSum
                    }
                }
            }
        }
    }

    @Test
    fun testPowerAndBrackets() = testPlan {
        plan = simplifyArithmeticExpression
        inputExpr = "[(1 + 1) ^ [2 ^ 3]]"

        check {
            toExpr = "256"

            step {
                fromExpr = "[(1 + 1) ^ [2 ^ 3]]"
                toExpr = "[(2) ^ [2 ^ 3]]"

                step {
                    fromExpr = "1 + 1"
                    toExpr = "2"
                }
            }

            step {
                fromExpr = "[(2) ^ [2 ^ 3]]"
                toExpr = "[2 ^ [2 ^ 3]]"

                step {
                    fromExpr = "(2)"
                    toExpr = "2"
                }
            }

            step {
                fromExpr = "[2 ^ [2 ^ 3]]"
                toExpr = "[2 ^ 8]"

                step {
                    fromExpr = "[2 ^ 3]"
                    toExpr = "8"
                }
            }

            step {
                fromExpr = "[2 ^ 8]"
                toExpr = "256"

                step {
                    fromExpr = "[2 ^ 8]"
                    toExpr = "256"
                }
            }
        }
    }
}
