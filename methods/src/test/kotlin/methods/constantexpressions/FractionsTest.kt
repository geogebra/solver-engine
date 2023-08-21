package methods.constantexpressions

import engine.methods.SolverEngineExplanation
import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class SimpleFractionsTest {

    @Test
    fun simpleTest() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[1 / 3] + [2 / 3] * [1 / 2]"

        check {
            fromExpr = "[1 / 3] + [2 / 3] * [1 / 2]"
            toExpr = "[2 / 3]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "[1 / 3] + [2 / 3] * [1 / 2]"
                toExpr = "[1 / 3] + [1 / 3]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                }
            }

            step {
                fromExpr = "[1 / 3] + [1 / 3]"
                toExpr = "[2 / 3]"
                explanation {
                    key = FractionArithmeticExplanation.AddFractions
                }
            }
        }
    }

    @Test
    fun testMultiplyAndSimplify() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[14 / 4] * [4 / 21]"

        check {
            toExpr = "[2 / 3]"

            step { toExpr = "[7 / 2] * [ 4 / 21]" }

            step {
                toExpr = "[2 / 3]"

                step { toExpr = "[7 * 4 / 2 * 21]" }

                step { toExpr = "[2 / 3]" }
            }
        }
    }

    @Test
    fun testWithNegativesInFractions() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[-1 / 6] * [2 / -5]"

        check {
            step { toExpr = "(-[1 / 6]) * [2 / -5]" }
            step { toExpr = "(-[1 / 6]) (-[2 / 5])" }
            step { toExpr = "[1 / 6] * [2 / 5]" }
            step { toExpr = "[1 / 15]" }
        }
    }

    @Test
    fun testWithMoreNegativesInFractions() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "(-[1 / 3]) * [-1 / 4] * [3 / -2]"

        check {
            step { toExpr = "(-[1 / 3]) (-[1 / 4]) * [3 / -2]" }
            step { toExpr = "(-[1 / 3]) (-[1 / 4]) (-[3 / 2])" }
            step {
                step { toExpr = "[1 / 3] * [1 / 4] (-[3 / 2])" }
                step { toExpr = "-[1 / 3] * [1 / 4] * [3 / 2]" }
            }
            step { toExpr = "-[1 / 8]" }
        }
    }

    @Test
    fun testDividingTwice() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "3 : 4 : 5"

        check {
            step {
                step { toExpr = "[3 / 4] : 5" }
                step { toExpr = "[3 / 4] * [1 / 5]" }
            }

            step { toExpr = "[3 / 20]" }
        }
    }

    @Test
    fun testDividingFractions() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[5 / 6] : [3 / 4]"

        check {
            step { toExpr = "[5 / 6] * [4 / 3]" }
            step { toExpr = "[10 / 9]" }
        }
    }

    @Test
    fun testDividingWithNegatives() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "3 : (-5)"

        check {
            step { toExpr = "[3 / -5]" }
            step { toExpr = "-[3 / 5]" }
        }
    }

    @Test
    fun testZeroDivideByValue() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "0:(1+1)"

        check {
            fromExpr = "0 : (1 + 1)"
            toExpr = "0"

            step {
                fromExpr = "0 : (1 + 1)"
                toExpr = "0 : 2"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }
                step {
                    fromExpr = "(1 + 1)"
                    toExpr = "2"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }
            }

            step {
                fromExpr = "0 : 2"
                toExpr = "[0 / 2]"
                explanation {
                    key = FractionArithmeticExplanation.RewriteDivisionAsFraction
                }
            }

            step {
                fromExpr = "[0 / 2]"
                toExpr = "0"
                explanation {
                    key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
                }
            }
        }
    }
}

class AddingFractionsTest {

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
                    key = FractionArithmeticExplanation.BringToCommonDenominatorWithNonFractionalTerm
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
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
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
                    key = FractionArithmeticExplanation.BringToCommonDenominatorWithNonFractionalTerm
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
                    key = FractionArithmeticExplanation.BringToCommonDenominatorWithNonFractionalTerm
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
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
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
                    key = FractionArithmeticExplanation.BringToCommonDenominatorWithNonFractionalTerm
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
                    key = CollectingExplanation.CollectLikeRootsAndSimplify
                }
            }
        }
    }
}

class ZeroNumeratorTest {

    @Test
    fun testSimpleRootDenominator() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 / sqrt[12]]"

        check {
            fromExpr = "[0 / sqrt[12]]"
            toExpr = "0"
            explanation {
                key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
            }
        }
    }

    @Test
    fun testPositiveDenominator() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 / root[3, 3] + root[4, 3]]"
        check {
            fromExpr = "[0 / root[3, 3] + root[4, 3]]"
            toExpr = "0"

            explanation {
                key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
            }
        }
    }

    @Test
    fun testDenominatorNotZeroBecauseOfIncommensurableTerms() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 / [sqrt[3]/3] - [sqrt[2]/2]]"
        check {
            toExpr = "0"

            explanation {
                key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
            }
        }
    }

    @Test
    fun testDenominatorEventuallyNotZero() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 / 3 - 5]"

        check {
            fromExpr = "[0 / 3 - 5]"
            toExpr = "0"

            step {
                fromExpr = "[0 / 3 - 5]"
                toExpr = "[0 / -2]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }
            step {
                fromExpr = "[0 / -2]"
                toExpr = "0"
                explanation {
                    key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
                }
            }
        }
    }
}
