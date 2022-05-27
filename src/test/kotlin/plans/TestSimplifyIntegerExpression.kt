package plans

import context.Context
import context.emptyContext
import expressions.RootPath
import expressions.Subexpression
import parser.parseExpression
import steps.Transformation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TransformationCheck {
    var fromExpr: String? = null
    var toExpr: String? = null
    var steps: MutableList<TransformationCheck>? = null

    fun step(init: TransformationCheck.() -> Unit) {
        val stepCheck = TransformationCheck()
        stepCheck.init()
        if (steps == null) {
            steps = mutableListOf(stepCheck)
        } else {
            steps!!.add(stepCheck)
        }
    }

    fun checkTransformation(trans: Transformation?) {
        assertNotNull(trans)
        if (fromExpr != null) {
            assertEquals(parseExpression(fromExpr!!), trans.fromExpr.expr)
        }
        if (toExpr != null) {
            assertEquals(parseExpression(toExpr!!), trans.toExpr.expr)
        }
        if (steps != null) {
            val transSteps = trans.steps
            assertNotNull(transSteps)
            assertEquals(steps!!.size, transSteps.size)
            for ((s, t) in steps!!.zip(transSteps)) {
                s.checkTransformation(t)
            }
        }
    }
}

fun makeCheck(init: TransformationCheck.() -> Unit): TransformationCheck {
    val check = TransformationCheck()
    check.init()
    return check
}

fun testPlan(init: PlanTestCase.() -> Unit) {
    val testCase = PlanTestCase()
    testCase.init()
    testCase.assert()
}

class PlanTestCase {
    lateinit var plan: Plan
    var context: Context = emptyContext
    lateinit var inputExpr: String
    lateinit var checkTrans: TransformationCheck

    fun check(init: TransformationCheck.() -> Unit) {
        checkTrans = makeCheck(init)
    }

    fun assert() {
        val expr = parseExpression(inputExpr)
        val trans = plan.tryExecute(context, Subexpression(RootPath, expr))
        checkTrans.checkTransformation(trans)
    }
}

class TestSimplifyIntegerExpression {

    @Test
    fun simpleTest() = testPlan {
        plan = simplifyIntegerExpression
        inputExpr = "1 + 2 + 3"

        check {
            toExpr = "6"
            step {
                step {
                    step { toExpr = "3 + 3" }
                    step { toExpr = "6" }
                }
            }
        }
    }

    @Test
    fun testAddMultiplyDivide() = testPlan {
        plan = simplifyIntegerExpression
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
        plan = simplifyIntegerExpression
        inputExpr = "34 + 60 + 6 - (4 + 10 - 3 * 5 * (-2))"

        check {
            toExpr = "56"
            step {

                step {
                    fromExpr = "3 * 5 * (-2)"
                    toExpr = "(-30)"
                }
            }

            step {

                step {
                    fromExpr = "-(-30)"
                    toExpr = "30"
                }
            }

            step {

                step {
                    fromExpr = "4 + 10 + 30"
                    toExpr = "44"
                }
            }

            step {
                step {
                    fromExpr = "(44)"
                    toExpr = "44"
                }
            }

            step {

                step {
                    fromExpr = "34 + 60 + 6 - 44"
                    toExpr = "56"
                }
            }
        }
    }

    @Test
    fun testPowerAndBrackets() = testPlan {
        plan = simplifyIntegerExpression
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
