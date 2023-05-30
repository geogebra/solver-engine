package methods.constantexpressions

import engine.methods.SolverEngineExplanation
import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class ConstantFractionsTest {

    @Test
    fun testAddLikeFractions() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[5 / 4] + [2 / 4]"

        check {
            fromExpr = "[5 / 4] + [2 / 4]"
            toExpr = "[7 / 4]"
            explanation {
                key = FractionArithmeticExplanation.AddFractions
            }

            step {
                fromExpr = "[5 / 4] + [2 / 4]"
                toExpr = "[5 + 2 / 4]"
                explanation {
                    key = FractionArithmeticExplanation.AddLikeFractions
                }
            }

            step {
                fromExpr = "[5 + 2 / 4]"
                toExpr = "[7 / 4]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }
        }
    }

    @Test
    fun testSubtractLikeFractions() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[5 / 4] - [2 / 4]"

        check {
            fromExpr = "[5 / 4] - [2 / 4]"
            toExpr = "[3 / 4]"
            explanation {
                key = FractionArithmeticExplanation.AddFractions
            }

            step {
                fromExpr = "[5 / 4] - [2 / 4]"
                toExpr = "[5 - 2 / 4]"
                explanation {
                    key = FractionArithmeticExplanation.SubtractLikeFractions
                }
            }

            step {
                fromExpr = "[5 - 2 / 4]"
                toExpr = "[3 / 4]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }
        }
    }

    @Test
    fun testSimplifyFractionsBeforeAdding() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[100/200] + [100/300]"

        check {
            toExpr = "[5 / 6]"

            step { toExpr = "[1 / 2] + [100 / 300]" }
            step { toExpr = "[1 / 2] + [1 / 3]" }
            step { toExpr = "[5 / 6]" }
        }
    }

    @Test
    fun testAddingFractionWithoutUnnecessaryCanceling() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[2 / 2 * 3] + [3 / 2 * 3] + [5 / 3 * 5]"

        check {
            toExpr = "[7 / 6]"

            step { toExpr = "[5 / 6] + [5 / 3 * 5]" }
            step { toExpr = "[5 / 6] + [1 / 3]" }
            step { toExpr = "[7 / 6]" }
        }
    }

    @Test
    fun `test adding integer to integer fraction`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "3 + [1 / 2]"

        check {
            fromExpr = "3 + [1 / 2]"
            toExpr = "[7 / 2]"
            explanation {
                key = FractionArithmeticExplanation.AddIntegerAndFraction
            }

            step {
                fromExpr = "3 + [1 / 2]"
                toExpr = "[3 * 2 / 2] + [1 / 2]"
                explanation {
                    key = FractionArithmeticExplanation.BringToCommonDenominator
                }
            }

            step {
                fromExpr = "[3 * 2 / 2] + [1 / 2]"
                toExpr = "[6 / 2] + [1 / 2]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                }
            }

            step {
                fromExpr = "[6 / 2] + [1 / 2]"
                toExpr = "[6 + 1 / 2]"
                explanation {
                    key = FractionArithmeticExplanation.AddLikeFractions
                }
            }

            step {
                fromExpr = "[6 + 1 / 2]"
                toExpr = "[7 / 2]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }
        }
    }

    @Test
    fun `test subtracting integer from fraction (match in the opposite order)`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "sqrt[2] + [7 / 2] - 1"

        check {
            fromExpr = "sqrt[2] + [7 / 2] - 1"
            toExpr = "sqrt[2] + [5 / 2]"
            explanation {
                key = FractionArithmeticExplanation.AddIntegerAndFraction
            }

            invisibleStep {
                fromExpr = "sqrt[2] + [7 / 2] - 1"
                toExpr = "sqrt[2] + <. [7 / 2] - 1 .>"
                explanation {
                    key = SolverEngineExplanation.ExtractPartialExpression
                }
            }

            step {
                fromExpr = "sqrt[2] + <. [7 / 2] - 1 .>"
                toExpr = "sqrt[2] + <. [7 / 2] - [1 * 2 / 2] .>"
                explanation {
                    key = FractionArithmeticExplanation.BringToCommonDenominator
                }
            }

            step {
                fromExpr = "sqrt[2] + <. [7 / 2] - [1 * 2 / 2] .>"
                toExpr = "sqrt[2] + <. [7 / 2] - [2 / 2] .>"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                }
            }

            step {
                fromExpr = "sqrt[2] + <. [7 / 2] - [2 / 2] .>"
                toExpr = "sqrt[2] + [7 - 2 / 2]"
                explanation {
                    key = FractionArithmeticExplanation.SubtractLikeFractions
                }
            }

            step {
                fromExpr = "sqrt[2] + [7 - 2 / 2]"
                toExpr = "sqrt[2] + [5 / 2]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyNumerator
                }
            }
        }
    }

    @Test
    fun `test adding integer to fraction with no integer in its numerator fails`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "3 + [sqrt[2] / 2]"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test adding integer to fraction with integer and root in its numerator`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "3 + [3 + sqrt[2] / 2]"

        check {
            fromExpr = "3 + [3 + sqrt[2] / 2]"
            toExpr = "[9 + sqrt[2] / 2]"
            explanation {
                key = FractionArithmeticExplanation.AddIntegerAndFraction
            }

            step {
                fromExpr = "3 + [3 + sqrt[2] / 2]"
                toExpr = "[3 * 2 / 2] + [3 + sqrt[2] / 2]"
                explanation {
                    key = FractionArithmeticExplanation.BringToCommonDenominator
                }
            }

            step {
                fromExpr = "[3 * 2 / 2] + [3 + sqrt[2] / 2]"
                toExpr = "[6 / 2] + [3 + sqrt[2] / 2]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                }
            }

            step {
                fromExpr = "[6 / 2] + [3 + sqrt[2] / 2]"
                toExpr = "[6 + 3 + sqrt[2] / 2]"
                explanation {
                    key = FractionArithmeticExplanation.AddLikeFractions
                }
            }

            step {
                fromExpr = "[6 + 3 + sqrt[2] / 2]"
                toExpr = "[9 + sqrt[2] / 2]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }
        }
    }

    @Test
    fun `test adding root to integer fraction fails`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "sqrt[2] + [3 / 2]"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test adding root to fraction with different root in its numerator fails`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "sqrt[2] + [3 + root[2, 3] / 2]"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test adding root to fraction with the same root in its numerator`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "sqrt[5] + [3 + sqrt[5] / 2]"

        check {
            fromExpr = "sqrt[5] + [3 + sqrt[5] / 2]"
            toExpr = "[3 sqrt[5] + 3 / 2]"
            explanation {
                key = FractionArithmeticExplanation.AddRootAndFraction
            }

            step {
                fromExpr = "sqrt[5] + [3 + sqrt[5] / 2]"
                toExpr = "[sqrt[5] * 2 / 2] + [3 + sqrt[5] / 2]"
                explanation {
                    key = FractionArithmeticExplanation.BringToCommonDenominator
                }
            }

            step {
                fromExpr = "[sqrt[5] * 2 / 2] + [3 + sqrt[5] / 2]"
                toExpr = "[sqrt[5] * 2 + 3 + sqrt[5] / 2]"
                explanation {
                    key = FractionArithmeticExplanation.AddLikeFractions
                }
            }

            step {
                fromExpr = "[sqrt[5] * 2 + 3 + sqrt[5] / 2]"
                toExpr = "[3 sqrt[5] + 3 / 2]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyNumerator
                }

                step {
                    fromExpr = "[sqrt[5] * 2 + 3 + sqrt[5] / 2]"
                    toExpr = "[2 sqrt[5] + 3 + sqrt[5] / 2]"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "[2 sqrt[5] + 3 + sqrt[5] / 2]"
                    toExpr = "[3 sqrt[5] + 3 / 2]"
                    explanation {
                        key = CollectingExplanation.CollectLikeRootsAndSimplify
                    }
                }
            }
        }
    }
}
