package methods.constantexpressions

import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class UndefinedTest {
    @Test
    fun testZeroDenominator1() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[5 - 4 / 1 - 1] + 2"

            check {
                fromExpr = "[5 - 4 / 1 - 1] + 2"
                toExpr = "/undefined/"

                step {
                    fromExpr = "[5 - 4 / 1 - 1] + 2"
                    toExpr = "[5 - 4 / 0] + 2"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }

                step {
                    fromExpr = "[5 - 4 / 0] + 2"
                    toExpr = "/undefined/"
                    explanation {
                        key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                    }
                }
            }
        }

    @Test
    fun testDivisionByZero1() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(5 - 4) : (1 - 1) + 2"

            check {
                fromExpr = "(5 - 4) : (1 - 1) + 2"
                toExpr = "/undefined/"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "(5 - 4) : (1 - 1) + 2"
                    toExpr = "(5 - 4) : 0 + 2"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }

                step {
                    fromExpr = "(5 - 4) : 0 + 2"
                    toExpr = "1 : 0 + 2"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                    }

                    step {
                        fromExpr = "(5 - 4)"
                        toExpr = "1"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }
                }

                step {
                    fromExpr = "1 : 0 + 2"
                    toExpr = "[1 / 0] + 2"
                    explanation {
                        key = FractionArithmeticExplanation.RewriteDivisionAsFraction
                    }
                }

                step {
                    fromExpr = "[1 / 0] + 2"
                    toExpr = "/undefined/"
                    explanation {
                        key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                    }
                }
            }
        }

    @Test
    fun testZeroDenominator2() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[0 / 1 - 1]"

            check {
                toExpr = "/undefined/"
            }
        }

    // step-by-step of this, needs to be improved
    @Test
    fun testDivisionByZero2() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "0:(1 - 1)"

            check {
                toExpr = "/undefined/"
            }
        }

    @Test
    fun testNegativeToRationalExponent() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "1 + 3 * [(-1) ^ [6/5]]"

            check {
                toExpr = "/undefined/"
            }
        }
}
