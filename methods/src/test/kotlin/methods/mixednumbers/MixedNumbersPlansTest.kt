package methods.mixednumbers

import engine.context.BooleanSetting
import engine.context.Setting
import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import kotlin.test.Test

class MixedNumbersPlansTest {
    @Test
    fun testAddMixedNumbersPlan() =
        testMethod {
            method = MixedNumbersPlans.AddMixedNumbers
            inputExpr = "[5 1/4] + [2 2/3]"

            check {
                fromExpr = "[5 1/4] + [2 2/3]"
                toExpr = "[7 11/12]"
                explanation {
                    key = MixedNumbersExplanation.AddMixedNumbers
                }

                step {
                    fromExpr = "[5 1/4] + [2 2/3]"
                    toExpr = "[21 / 4] + [2 2/3]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertMixedNumberToImproperFraction
                    }

                    transform {
                        fromPaths("./0")
                        toPaths("./0")
                    }

                    shift {
                        fromPaths("./1")
                        toPaths("./1")
                    }
                }

                step {
                    fromExpr = "[21 / 4] + [2 2/3]"
                    toExpr = "[21 / 4] + [8 / 3]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertMixedNumberToImproperFraction
                    }

                    shift {
                        fromPaths("./0")
                        toPaths("./0")
                    }

                    transform {
                        fromPaths("./1")
                        toPaths("./1")
                    }
                }

                step {
                    fromExpr = "[21 / 4] + [8 / 3]"
                    toExpr = "[95 / 12]"
                    explanation {
                        key = FractionArithmeticExplanation.AddFractions
                    }

                    transform {
                        fromPaths(".")
                        toPaths(".")
                    }
                }

                step {
                    fromExpr = "[95 / 12]"
                    toExpr = "[7 11/12]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertFractionToMixedNumber
                    }

                    combine {
                        fromPaths("./0", "./1", "./1:outerOp")
                        toPaths("./0")
                    }

                    combine {
                        fromPaths("./0", "./1", "./1:outerOp")
                        toPaths("./1")
                    }

                    move {
                        fromPaths("./1")
                        toPaths("./2")
                    }
                }
            }
        }

    @Test
    fun `test add mixed numbers without converting them to fractions first`() =
        testMethod {
            method = MixedNumbersPlans.AddMixedNumbers
            context = context.copy(
                settings = mapOf(Setting.AddMixedNumbersWithoutConvertingToImproperFractions setTo BooleanSetting.True),
            )
            inputExpr = "[5 3/4] + [2 2/3]"

            check {
                fromExpr = "[5 3/4] + [2 2/3]"
                toExpr = "[8 5/12]"
                explanation {
                    key = MixedNumbersExplanation.AddMixedNumbers
                }

                step {
                    fromExpr = "[5 3/4] + [2 2/3]"
                    toExpr = "5 + [3 / 4] + 2 + [2 / 3]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertMixedNumbersToSums
                    }
                }

                step {
                    fromExpr = "5 + [3 / 4] + 2 + [2 / 3]"
                    toExpr = "7 + [3 / 4] + [2 / 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }

                step {
                    fromExpr = "7 + [3 / 4] + [2 / 3]"
                    toExpr = "7 + [17 / 12]"
                    explanation {
                        key = FractionArithmeticExplanation.AddFractions
                    }
                }

                step {
                    fromExpr = "7 + [17 / 12]"
                    toExpr = "[7 / 1] + [17 / 12]"
                    explanation {
                        key = FractionArithmeticExplanation.ConvertIntegerToFraction
                    }
                }

                step {
                    fromExpr = "[7 / 1] + [17 / 12]"
                    toExpr = "[101 / 12]"
                    explanation {
                        key = FractionArithmeticExplanation.AddFractions
                    }
                }

                step {
                    fromExpr = "[101 / 12]"
                    toExpr = "[8 5/12]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertFractionToMixedNumber
                    }
                }
            }
        }

    @Test
    fun `test shortcut in adding mixed numbers without converting to improper fractions`() =
        testMethod {
            method = MixedNumbersPlans.AddMixedNumbers
            context = context.copy(
                settings = mapOf(Setting.AddMixedNumbersWithoutConvertingToImproperFractions setTo BooleanSetting.True),
            )
            inputExpr = "[1 1/2] + [2 1/3]"

            check {
                fromExpr = "[1 1/2] + [2 1/3]"
                toExpr = "[3 5/6]"
                explanation {
                    key = MixedNumbersExplanation.AddMixedNumbers
                }

                step {
                    fromExpr = "[1 1/2] + [2 1/3]"
                    toExpr = "1 + [1 / 2] + 2 + [1 / 3]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertMixedNumbersToSums
                    }
                }

                step {
                    fromExpr = "1 + [1 / 2] + 2 + [1 / 3]"
                    toExpr = "3 + [1 / 2] + [1 / 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }

                step {
                    fromExpr = "3 + [1 / 2] + [1 / 3]"
                    toExpr = "3 + [5 / 6]"
                    explanation {
                        key = FractionArithmeticExplanation.AddFractions
                    }
                }

                step {
                    fromExpr = "3 + [5 / 6]"
                    toExpr = "[3 5 / 6]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertSumOfIntegerAndProperFractionToMixedNumber
                    }
                }
            }
        }

    @Test
    fun `test shortcut when the addition of mixed numbers results in the sum of two integers`() =
        testMethod {
            method = MixedNumbersPlans.AddMixedNumbers
            context = context.copy(
                settings = mapOf(Setting.AddMixedNumbersWithoutConvertingToImproperFractions setTo BooleanSetting.True),
            )
            inputExpr = "[5 2/3] + [3 12/36]"

            check {
                fromExpr = "[5 2/3] + [3 12/36]"
                toExpr = "9"
                explanation {
                    key = MixedNumbersExplanation.AddMixedNumbers
                }

                step {
                    fromExpr = "[5 2/3] + [3 12/36]"
                    toExpr = "5 + [2 / 3] + 3 + [12 / 36]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertMixedNumbersToSums
                    }
                }

                step {
                    fromExpr = "5 + [2 / 3] + 3 + [12 / 36]"
                    toExpr = "5 + [2 / 3] + 3 + [1 / 3]"
                    explanation {
                        key = FractionArithmeticExplanation.SimplifyFraction
                    }
                }

                step {
                    fromExpr = "5 + [2 / 3] + 3 + [1 / 3]"
                    toExpr = "8 + [2 / 3] + [1 / 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }

                step {
                    fromExpr = "8 + [2 / 3] + [1 / 3]"
                    toExpr = "8 + 1"
                    explanation {
                        key = FractionArithmeticExplanation.AddFractions
                    }
                }

                step {
                    fromExpr = "8 + 1"
                    toExpr = "9"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }
            }
        }
}
