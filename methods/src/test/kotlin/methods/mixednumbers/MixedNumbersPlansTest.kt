package methods.mixednumbers

import engine.context.Context
import engine.context.Curriculum
import engine.methods.SolverEngineExplanation
import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import kotlin.test.Test

class MixedNumbersPlansTest {

    @Test
    fun testAddMixedNumbersPlan() = testMethod {
        method = MixedNumbersPlans.AddMixedNumbers
        inputExpr = "[5 1/4] + [2 2/3]"

        check {
            toExpr = "[7 11/12]"
            step {
                fromExpr = "[5 1/4] + [2 2/3]"
                toExpr = "[21 / 4] + [8 / 3]"

                transform {
                    fromPaths(".")
                    toPaths(".")
                }
            }

            step {
                fromExpr = "[21 / 4] + [8 / 3]"
                toExpr = "[95 / 12]"

                transform {
                    fromPaths(".")
                    toPaths(".")
                }

                explanation {
                    key = methods.fractionarithmetic.Explanation.AddFractions
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
        method = MixedNumbersPlans.AddMixedNumbers
        context = Context(curriculum = Curriculum.EU)
        inputExpr = "[5 1/4] + [2 2/3]"

        check {
            toExpr = "[7 11/12]"
            step {
                fromExpr = "[5 1/4] + [2 2/3]"
                toExpr = "[21 / 4] + [8 / 3]"

                transform {
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
        method = MixedNumbersPlans.AddMixedNumbers
        context = Context(curriculum = Curriculum.US)
        inputExpr = "[5 3/4] + [2 2/3]"

        check {
            fromExpr = "[5 3/4] + [2 2/3]"
            toExpr = "[8 5/12]"

            step {
                toExpr = "(5 + [3 / 4]) + (2 + [2 / 3])"
                explanation {
                    key = MixedNumbersExplanation.ConvertMixedNumbersToSums
                }
            }

            step {
                toExpr = "5 + [3 / 4] + 2 + [2 / 3]"
            }

            step {
                toExpr = "7 + [3 / 4] + [2 / 3]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                }
            }

            step {
                toExpr = "7 + [17 / 12]"
                explanation {
                    key = SolverEngineExplanation.SimplifyPartialExpression
                }
            }

            step {
                toExpr = "[7 / 1] + [17 / 12]"
                explanation {
                    key = FractionArithmeticExplanation.ConvertIntegerToFraction
                }
            }

            step {
                toExpr = "[101 / 12]"
                explanation {
                    key = FractionArithmeticExplanation.AddFractions
                }
            }

            step {
                toExpr = "[8 5/12]"
                explanation {
                    key = MixedNumbersExplanation.ConvertFractionToMixedNumber
                }
            }
        }
    }

    @Test
    fun testUSStyleConversionWithShortcut() = testMethod {
        method = MixedNumbersPlans.AddMixedNumbers
        context = Context(curriculum = Curriculum.US)
        inputExpr = "[1 1/2] + [2 1/3]"

        check {
            fromExpr = "[1 1/2] + [2 1/3]"
            toExpr = "[3 5/6]"

            step {
                toExpr = "(1 + [1 / 2]) + (2 + [1 / 3])"
                explanation {
                    key = MixedNumbersExplanation.ConvertMixedNumbersToSums
                }
            }

            step {
                toExpr = "1 + [1 / 2] + 2 + [1 / 3]"
            }

            step {
                toExpr = "3 + [1 / 2] + [1 / 3]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                }
            }

            step {
                toExpr = "3 + [5 / 6]"
                explanation {
                    key = SolverEngineExplanation.SimplifyPartialExpression
                }
            }

            step {
                toExpr = "[3 5/6]"
                explanation {
                    key = MixedNumbersExplanation.ConvertSumOfIntegerAndProperFractionToMixedNumber
                }
            }
        }
    }

    @Test
    fun testUSStyleConversionResultingInInteger() = testMethod {
        method = MixedNumbersPlans.AddMixedNumbers
        context = Context(curriculum = Curriculum.US)
        inputExpr = "[5 2/3] + [3 12/36]"

        check {
            fromExpr = "[5 2/3] + [3 12/36]"
            toExpr = "9"

            step {
                toExpr = "(5 + [2 / 3]) + (3 + [12 / 36])"
                explanation {
                    key = MixedNumbersExplanation.ConvertMixedNumbersToSums
                }
            }

            step {
                toExpr = "5 + [2 / 3] + 3 + [12 / 36]"
            }

            step {
                toExpr = "5 + [2 / 3] + 3 + [1 / 3]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }
            }

            step {
                toExpr = "8 + [2 / 3] + [1 / 3]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                }
            }

            step {
                toExpr = "8 + 1"
                explanation {
                    key = SolverEngineExplanation.SimplifyPartialExpression
                }
            }

            step {
                toExpr = "9"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                }
            }
        }
    }

    // this would need a check as well
    // @Test
    // fun testFunction() = testMethod {
    //     method = convertMixedNumberToImproperFraction
    //     inputExpr = "[3 1/0] + [2 1/0]"
    //
    //     check {
    //         toExpr = "[7 / 2] + [7 / 3]"
    //     }
    // }
}
