package methods.mixednumbers

import engine.context.Context
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.plans.testPlan
import kotlin.test.Test

class MixedNumbersPlansTest {

    @Test
    fun testConvertMixedNumberToImproperFractionsPlan() = testPlan {
        plan = convertMixedNumberToImproperFraction
        inputExpr = "[1 2/3]"

        check {
            toExpr = "[5/3]"
            step {
                fromExpr = "[1 2/3]"
                toExpr = "1 + [2 / 3]"

                explanation {
                    key = Explanation.ConvertMixedNumberToSum
                }

                move {
                    fromPaths("./0")
                    toPaths("./0")
                }

                move {
                    fromPaths("./1")
                    toPaths("./1/0")
                }

                move {
                    fromPaths("./2")
                    toPaths("./1/1")
                }
            }
            step {
                fromExpr = "1 + [2 / 3]"
                toExpr = "[1 / 1] + [2 / 3]"

                explanation {
                    methods.fractionarithmetic.Explanation.ConvertIntegerToFraction
                }

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
            }
            step {
                fromExpr = "[1 / 1] + [2 / 3]"
                toExpr = "[5 / 3]"

                explanation {
                    methods.fractionarithmetic.Explanation.EvaluateFractionSum
                }

                combine {
                    fromPaths("./0/0", "./0/1")
                    toPaths("./0")
                }

                move {
                    fromPaths("./1")
                    toPaths("./1")
                }
            }
        }
    }

    @Test
    fun testAddMixedNumbersPlan() = testPlan {
        plan = addMixedNumbers
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
    fun testContextSensitivePlanEU() = testPlan {
        context = Context("EU")
        plan = addMixedNumbers
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
    fun testContextSensitivePlanUS() = testPlan {
        context = Context("US")
        plan = addMixedNumbers
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
