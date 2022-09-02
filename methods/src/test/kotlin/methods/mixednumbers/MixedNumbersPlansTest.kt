package methods.mixednumbers

import engine.context.Context
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.plans.testMethod
import kotlin.test.Test

class MixedNumbersPlansTest {

    @Test
    fun testAddMixedNumbersPlan() = testMethod {
        method = addMixedNumbers
        inputExpr = "[5 1/4] + [2 2/3]"

        check {
            toExpr = "[7 11/12]"
            step {
                fromExpr = "[5 1/4] + [2 2/3]"
                toExpr = "[21 / 4] + [8 / 3]"

                move {
                    fromPaths(".")
                    toPaths(".")
                }
            }

            step {
                fromExpr = "[21 / 4] + [8 / 3]"
                toExpr = "[95 / 12]"

                combine {
                    fromPaths("./0/0", "./0/1")
                    toPaths("./0")
                }

                move {
                    fromPaths("./1")
                    toPaths("./1")
                }

                explanation {
                    key = methods.fractionarithmetic.Explanation.EvaluateFractionSum
                }
            }

            step {
                fromExpr = "[95 / 12]"
                toExpr = "[7 11/12]"

                combine {
                    fromPaths("./0", "./1")
                    toPaths("./0")
                }

                combine {
                    fromPaths("./0", "./1")
                    toPaths("./1")
                }

                move {
                    fromPaths("./1")
                    toPaths("./2")
                }

                explanation {
                    key = Explanation.ConvertFractionToMixedNumber
                }
            }
        }
    }

    @Test
    fun testContextSensitivePlanEU() = testMethod {
        context = Context("EU")
        method = addMixedNumbers
        inputExpr = "[5 1/4] + [2 2/3]"

        check {
            toExpr = "[7 11/12]"
            step {
                fromExpr = "[5 1/4] + [2 2/3]"
                toExpr = "[21 / 4] + [8 / 3]"

                move {
                    fromPaths(".")
                    toPaths(".")
                }
            }

            step { }
            step { }
        }
    }

    @Test
    fun testContextSensitivePlanUS() = testMethod {
        context = Context("US")
        method = addMixedNumbers
        inputExpr = "[5 1/4] + [2 2/3]"

        check {
            toExpr = "[7 11/12]"

            step {
                fromExpr = "[5 1/4] + [2 2/3]"
                toExpr = "(5 + [1 / 4]) + (2 + [2 / 3])"

                move {
                    fromPaths("./0")
                    toPaths("./0")
                }

                move {
                    fromPaths("./1/0")
                    toPaths("./1/0/0")
                }

                move {
                    fromPaths("./1/1")
                    toPaths("./1/0/1/0")
                }

                move {
                    fromPaths("./1/2")
                    toPaths("./1/0/1/1")
                }
            }

            step {
                fromExpr = "(5 + [1 / 4]) + (2 + [2 / 3])"
                toExpr = "5 + [1 / 4] + 2 + [2 / 3]"

                move {
                    fromPaths("./0")
                    toPaths("./0")
                }

                move {
                    fromPaths("./1")
                    toPaths("./1")
                }

                move {
                    fromPaths("./2/0/0")
                    toPaths("./2")
                }

                move {
                    fromPaths("./2/0/1")
                    toPaths("./3")
                }
            }

            step {
                fromExpr = "5 + [1 / 4] + 2 + [2 / 3]"
                toExpr = "7 + [1 / 4] + [2 / 3]"

                combine {
                    fromPaths("./0", "./2")
                    toPaths("./0")
                }

                move {
                    fromPaths("./1")
                    toPaths("./1")
                }

                move {
                    fromPaths("./3")
                    toPaths("./2")
                }

                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                }
            }

            step {
                fromExpr = "7 + [1 / 4] + [2 / 3]"
                toExpr = "7 + [11 / 12]"

                move {
                    fromPaths("./0")
                    toPaths("./0")
                }

                combine {
                    fromPaths("./1/0/0", "./1/0/1")
                    toPaths("./1/0")
                }

                move {
                    fromPaths("./1/1")
                    toPaths("./1/1")
                }

                explanation {
                    key = methods.fractionarithmetic.Explanation.EvaluateFractionSum
                }
            }

            step {
                fromExpr = "7 + [11 / 12]"
                toExpr = "[7 / 1] + [11 / 12]"

                move {
                    fromPaths("./0")
                    toPaths("./0/0")
                }

                introduce {
                    fromPaths()
                    toPaths("./0/1")
                }

                move {
                    fromPaths("./1")
                    toPaths("./1")
                }

                explanation {
                    key = methods.fractionarithmetic.Explanation.ConvertIntegerToFraction
                }
            }

            step {
                fromExpr = "[7 / 1] + [11 / 12]"
                toExpr = "[95 / 12]"

                combine {
                    fromPaths("./0/0", "./0/1")
                    toPaths("./0")
                }

                move {
                    fromPaths("./1")
                    toPaths("./1")
                }

                explanation {
                    key = methods.fractionarithmetic.Explanation.EvaluateFractionSum
                }
            }

            step {
                fromExpr = "[95 / 12]"
                toExpr = "[7 11/12]"

                combine {
                    fromPaths("./0", "./1")
                    toPaths("./0")
                }

                combine {
                    fromPaths("./0", "./1")
                    toPaths("./1")
                }

                move {
                    fromPaths("./1")
                    toPaths("./2")
                }

                explanation {
                    key = Explanation.ConvertFractionToMixedNumber
                }
            }
        }
    }
}
