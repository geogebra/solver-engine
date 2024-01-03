package methods.constantexpressions

import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerrationalexponents.IntegerRationalExponentsExplanation
import org.junit.jupiter.api.Test

class RationalPowersTest {
    @Test
    fun testSumOfProductOfSameBaseRationalExponent1() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[12^[1/2]] * [12^[1/2]]"

            check {
                fromExpr = "[12 ^ [1 / 2]] * [12 ^ [1 / 2]]"
                toExpr = "12"

                step {
                    fromExpr = "[12 ^ [1 / 2]] * [12 ^ [1 / 2]]"
                    toExpr = "[12 ^ 1]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                    }
                }

                step { toExpr = "12" }
            }
        }

    @Test
    fun testSumOfProductOfSameBaseRationalExponent2() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[12^[1/3]] * [12^[1/2]]"

            check {
                fromExpr = "[12 ^ [1 / 3]] * [12 ^ [1 / 2]]"
                toExpr = "[12 ^ [5 / 6]]"

                step {
                    fromExpr = "[12 ^ [1 / 3]] * [12 ^ [1 / 2]]"
                    toExpr = "[12 ^ [1 / 3] + [1 / 2]]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step { }
            }
        }

    @Test
    fun testCollectLikeRationalExponentsBeforeSimplification() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[12 ^ [1 / 2]] + [12 ^ [1 / 2]]"

            check {
                toExpr = "4 * [3 ^ [1 / 2]]"

                step {
                    toExpr = "2 * [12 ^ [1 / 2]]"
                    explanation {
                        key = CollectingExplanation.CollectLikeRationalPowersAndSimplify
                    }
                }

                step {
                    toExpr = "2 * <. 2 * [3 ^ [1 / 2]] .>"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                    }
                }

                step {
                    toExpr = "4 * [3 ^ [1 / 2]]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
        }

    @Test
    fun testSimplifyRationalExponentsInProduct() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[2 ^ [1 / 3]] * [3 ^ [1 / 3]] * [6 ^ [1 / 2]]"

            check {
                toExpr = "[6 ^ [5 / 6]]"

                step {
                    toExpr = "[6 ^ [1 / 3]] * [6 ^ [1 / 2]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameExponent
                    }

                    step {
                        toExpr = "[(2 * 3) ^ [1 / 3]] * [6 ^ [1 / 2]]"
                        explanation {
                            key = GeneralExplanation.RewriteProductOfPowersWithSameExponent
                        }
                    }

                    step {
                        toExpr = "[6 ^ [1 / 3]] * [6 ^ [1 / 2]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }

                step {
                    toExpr = "[6 ^ [5 / 6]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                    }

                    step {
                        toExpr = "[6 ^ [1 / 3] + [1 / 2]]"
                        explanation {
                            key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                        }
                    }

                    step {
                        toExpr = "[6 ^ [5 / 6]]"
                        explanation {
                            key = FractionArithmeticExplanation.AddFractions
                        }
                    }
                }
            }
        }

    @Test
    fun `test collecting powers with negative rational exponents`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[3^-[1/3]] * 4 - [3^-[1/3]] * 2"

            check {
                fromExpr = "[3 ^ -[1 / 3]] * 4 - [3 ^ -[1 / 3]] * 2"
                toExpr = "2 * [3 ^ -[1 / 3]]"
                explanation {
                    key = CollectingExplanation.CollectLikeRationalPowersAndSimplify
                }
            }
        }

    @Test
    fun `test simplifying division of powers with the same base`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[[3 ^ [5 / 3]] / [3 ^ [7 / 5]]]"

            check {
                fromExpr = "[[3 ^ [5 / 3]] / [3 ^ [7 / 5]]]"
                toExpr = "[3 ^ [4 / 15]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyFractionOfPowersWithSameBase
                }

                step {
                    fromExpr = "[[3 ^ [5 / 3]] / [3 ^ [7 / 5]]]"
                    toExpr = "[3 ^ [5 / 3] - [7 / 5]]"
                    explanation {
                        key = GeneralExplanation.RewriteFractionOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "[3 ^ [5 / 3] - [7 / 5]]"
                    toExpr = "[3 ^ [4 / 15]]"
                    explanation {
                        key = FractionArithmeticExplanation.AddFractions
                    }
                }
            }
        }
}

class SimplifyRationalPowerOfFraction {
    @Test
    fun testRationalPowerFractionSplitPowerAndNoBaseSimplify() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[([4 / 9]) ^ [11 / 3]]"

            check {
                toExpr = "[64 / 729] * [([4 / 9]) ^ [2 / 3]]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowerOfFraction
                }

                step {
                    fromExpr = "[([4 / 9]) ^ [11 / 3]]"
                    toExpr = "[([4 / 9]) ^ 3] * [([4 / 9]) ^ [2 / 3]]"
                    explanation {
                        key = FractionArithmeticExplanation.SplitRationalExponent
                    }
                }

                step {
                    fromExpr = "[([4 / 9]) ^ 3] * [([4 / 9]) ^ [2 / 3]]"
                    toExpr = "[64 / 729] * [([4 / 9]) ^ [2 / 3]]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyPowerOfFraction
                    }
                }
            }
        }

    @Test
    fun testRationalPowerFractionNoSplitSimplifyToFraction() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[([32 / 243]) ^ [2 / 5]]"

            check {
                toExpr = "[4 / 9]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowerOfFraction
                }

                step {
                    fromExpr = "[([32 / 243]) ^ [2 / 5]]"
                    toExpr = "[[32 ^ [2 / 5]] / [243 ^ [2 / 5]]]"
                    explanation {
                        key = FractionArithmeticExplanation.DistributeFractionPositivePower
                    }
                }

                step {
                    fromExpr = "[[32 ^ [2 / 5]] / [243 ^ [2 / 5]]]"
                    toExpr = "[4 / [243 ^ [2 / 5]]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                    }
                }

                step {
                    fromExpr = "[4 / [243 ^ [2 / 5]]]"
                    toExpr = "[4 / 9]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                    }
                }
            }
        }

    @Test
    fun testRationalPowerFractionNoTransformation() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[([14 / 45]) ^ [2 / 3]]"

            check {
                noTransformation()
            }
        }

    @Test
    fun testRationalPowerFractionOnlyNumeratorSimplifiedToInt() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[([4 / 3]) ^ [3 / 2]]"

            check {
                fromExpr = "[([4 / 3]) ^ [3 / 2]]"
                toExpr = "[8 * [3 ^ -[1 / 2]] / 3]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
                step {
                    fromExpr = "[([4 / 3]) ^ [3 / 2]]"
                    toExpr = "[4 / 3] * [2 / [3 ^ [1 / 2]]]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyPowerOfFraction
                    }
                }
                step {
                    fromExpr = "[4 / 3] * [2 / [3 ^ [1 / 2]]]"
                    toExpr = "[4 / 3] * <. 2 * [3 ^ -[1 / 2]] .>"
                    explanation {
                        key = IntegerRationalExponentsExplanation.ApplyReciprocalPowerRule
                    }
                }
                step {
                    fromExpr = "[4 / 3] * 2 * [3 ^ -[1 / 2]]"
                    toExpr = "[8 * [3 ^ -[1 / 2]] / 3]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                    }
                }
            }
        }

    @Test
    fun `test negative rational power in denominator with unit numerator`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[1 / [3^-[1/3]]]"

            check {
                fromExpr = "[1 / [3 ^ -[1 / 3]]]"
                toExpr = "[3 ^ [1 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.ApplyReciprocalPowerRule
                }
            }
        }

    @Test
    fun `test positive rational power in denominator with unit numerator`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[1 / [3^[1/3]]]"

            check {
                fromExpr = "[1 / [3 ^ [1 / 3]]]"
                toExpr = "[3 ^ -[1 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.ApplyReciprocalPowerRule
                }
            }
        }

    @Test
    fun `test positive rational power in denominator with unit numerator as division`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "1 : [3 ^ [1 / 2]]"
            check {
                fromExpr = "1 : [3 ^ [1 / 2]]"
                toExpr = "[3 ^ -[1 / 2]]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
                step {
                    fromExpr = "1 : [3 ^ [1 / 2]]"
                    toExpr = "[1 / [3 ^ [1 / 2]]]"
                    explanation {
                        key = FractionArithmeticExplanation.RewriteDivisionAsFraction
                    }
                }
                step {
                    fromExpr = "[1 / [3 ^ [1 / 2]]]"
                    toExpr = "[3 ^ -[1 / 2]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.ApplyReciprocalPowerRule
                    }
                }
            }
        }

    @Test
    fun `test positive rational power in denominator`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[3 / [3^[1/3]]]"

            check {
                fromExpr = "[3 / [3 ^ [1 / 3]]]"
                toExpr = "[3 ^ [2 / 3]]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
                step {
                    fromExpr = "[3 / [3 ^ [1 / 3]]]"
                    toExpr = "3 * [3 ^ -[1 / 3]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.ApplyReciprocalPowerRule
                    }
                }
                step {
                    fromExpr = "3 * [3 ^ -[1 / 3]]"
                    toExpr = "[3 ^ [2 / 3]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                    }
                }
            }
        }

    @Test
    fun `test negative exponent and positive exponent with same base to negative exponent`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[3^-1] * [3^[1/2]]"

            check {
                fromExpr = "[3 ^ -1] * [3 ^ [1 / 2]]"
                toExpr = "[3 ^ -[1 / 2]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }
                step {
                    fromExpr = "[3 ^ -1] * [3 ^ [1 / 2]]"
                    toExpr = "[3 ^ -1 + [1 / 2]]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }
                step {
                    fromExpr = "[3 ^ -1 + [1 / 2]]"
                    toExpr = "[3 ^ -[1 / 2]]"
                    explanation {
                        key = FractionArithmeticExplanation.AddIntegerAndFraction
                    }
                }
            }
        }

    @Test
    fun `negative exponent and positive exponent with same base to positive exponent`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[3^-3] * [3^[11/3]]"

            check {
                fromExpr = "[3 ^ -3] * [3 ^ [11 / 3]]"
                toExpr = "[3 ^ [2 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }
                step {
                    fromExpr = "[3 ^ -3] * [3 ^ [11 / 3]]"
                    toExpr = "[3 ^ -3 + [11 / 3]]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }
                step {
                    fromExpr = "[3 ^ -3 + [11 / 3]]"
                    toExpr = "[3 ^ [2 / 3]]"
                    explanation {
                        key = FractionArithmeticExplanation.AddIntegerAndFraction
                    }
                }
            }
        }

    @Test
    fun `test product of base and base with positive less than one exponent`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "3 * [3^[1/3]]"

            // this used to go into an infinite loop earlier
            check {
                noTransformation()
            }
        }

    @Test
    fun `test cancellable negative rational exponent of integer`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[25 ^ -[1/2]]"

            check {
                fromExpr = "[25 ^ -[1 / 2]]"
                toExpr = "[1 / 5]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
                step {
                    fromExpr = "[25 ^ -[1 / 2]]"
                    toExpr = "[5 ^ -1]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                    }
                    step {
                        fromExpr = "[25 ^ -[1 / 2]]"
                        toExpr = "[([5 ^ 2]) ^ -[1 / 2]]"
                        explanation {
                            key = IntegerRationalExponentsExplanation.FactorizeIntegerUnderRationalExponent
                        }
                    }
                    step {
                        fromExpr = "[([5 ^ 2]) ^ -[1 / 2]]"
                        toExpr = "[5 ^ -1]"
                        explanation {
                            key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                        }
                    }
                }
                step {
                    fromExpr = "[5 ^ -1]"
                    toExpr = "[1 / 5]"
                    explanation {
                        key = FractionArithmeticExplanation.TurnIntegerToMinusOneToFraction
                    }
                }
            }
        }

    @Test
    fun `test product and division of rational exponents`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[3 ^ 2] * [3 ^ -[1 / 3]] : [3 ^ [1 / 2]]"

            check {
                fromExpr = "[3 ^ 2] * [3 ^ -[1 / 3]] : [3 ^ [1 / 2]]"
                toExpr = "3 * [3 ^ [1 / 6]]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "[3 ^ 2] * [3 ^ -[1 / 3]] : [3 ^ [1 / 2]]"
                    toExpr = "[[3 ^ 2] * [3 ^ -[1 / 3]] / [3 ^ [1 / 2]]]"
                    explanation {
                        key = FractionArithmeticExplanation.RewriteDivisionAsFraction
                    }
                }

                step {
                    fromExpr = "[[3 ^ 2] * [3 ^ -[1 / 3]] / [3 ^ [1 / 2]]]"
                    toExpr = "[[3 ^ [5 / 3]] / [3 ^ [1 / 2]]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "[[3 ^ [5 / 3]] / [3 ^ [1 / 2]]]"
                    toExpr = "[3 ^ [7 / 6]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyFractionOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "[3 ^ [7 / 6]]"
                    toExpr = "3 * [3 ^ [1 / 6]]"
                    explanation {
                        key = FractionArithmeticExplanation.SplitRationalExponent
                    }
                }
            }
        }

    @Test
    fun `test product of integer and negative rational exponent`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "27 * [6 ^ -[1 / 3]]"

            check {
                fromExpr = "27 * [6 ^ -[1 / 3]]"
                toExpr = "9 * [3 ^ [2 / 3]] * [2 ^ -[1 / 3]]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "27 * [6 ^ -[1 / 3]]"
                    toExpr = "[3 ^ [8 / 3]] * [2 ^ -[1 / 3]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfIntegerAndRationalExponentOfInteger
                    }

                    step {
                        fromExpr = "27 * [6 ^ -[1 / 3]]"
                        toExpr = "[3 ^ 3] * [6 ^ -[1 / 3]]"
                        explanation {
                            key = GeneralExplanation.FactorizeInteger
                        }
                    }

                    step {
                        fromExpr = "[3 ^ 3] * [6 ^ -[1 / 3]]"
                        toExpr = "[3 ^ 3] * <.[2 ^ -[1 / 3]] * [3 ^ -[1 / 3]].>"
                        explanation {
                            key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                        }
                    }

                    step {
                        fromExpr = "[3 ^ 3] * [2 ^ -[1 / 3]] * [3 ^ -[1 / 3]]"
                        toExpr = "[3 ^ [8 / 3]] * [2 ^ -[1 / 3]]"
                        explanation {
                            key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                        }
                    }
                }

                step {
                    fromExpr = "[3 ^ [8 / 3]] * [2 ^ -[1 / 3]]"
                    toExpr = "<.9 * [3 ^ [2 / 3]].> * [2 ^ -[1 / 3]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                    }
                }
            }
        }

    @Test
    fun `test product of integer, rational exponent of integers with positive and negated exponent`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "25 * [5 ^ -[1 / 2]] * [2 ^ [1 / 2]]"

            check {
                fromExpr = "25 * [5 ^ -[1 / 2]] * [2 ^ [1 / 2]]"
                toExpr = "5 * [10 ^ [1 / 2]]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
                step {
                    fromExpr = "25 * [5 ^ -[1 / 2]] * [2 ^ [1 / 2]]"
                    toExpr = "[5 ^ [3 / 2]] * [2 ^ [1 / 2]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfIntegerAndRationalExponentOfInteger
                    }
                    step {
                        fromExpr = "<. 25 * [5 ^ -[1 / 2]] .> * [2 ^ [1 / 2]]"
                        toExpr = "<. [5 ^ 2] * [5 ^ -[1 / 2]] .> * [2 ^ [1 / 2]]"
                        explanation {
                            key = GeneralExplanation.FactorizeInteger
                        }
                    }
                    step {
                        fromExpr = "<. [5 ^ 2] * [5 ^ -[1 / 2]] .> * [2 ^ [1 / 2]]"
                        toExpr = "[5 ^ [3 / 2]] * [2 ^ [1 / 2]]"
                        explanation {
                            key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                        }
                        step {
                            fromExpr = "<. [5 ^ 2] * [5 ^ -[1 / 2]] .>"
                            toExpr = "[5 ^ 2 - [1 / 2]]"
                            explanation {
                                key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                            }
                        }
                        step {
                            fromExpr = "[5 ^ 2 - [1 / 2]]"
                            toExpr = "[5 ^ [3 / 2]]"
                            explanation {
                                key = FractionArithmeticExplanation.AddIntegerAndFraction
                            }
                        }
                    }
                }
                step {
                    fromExpr = "[5 ^ [3 / 2]] * [2 ^ [1 / 2]]"
                    toExpr = "<. 5 * [5 ^ [1 / 2]] .> * [2 ^ [1 / 2]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                    }
                    step {
                        fromExpr = "[5 ^ [3 / 2]]"
                        toExpr = "<. 5 * [5 ^ [1 / 2]] .>"
                        explanation {
                            key = FractionArithmeticExplanation.SplitRationalExponent
                        }
                    }
                }
                step {
                    fromExpr = "5 * [5 ^ [1 / 2]] * [2 ^ [1 / 2]]"
                    toExpr = "5 * [10 ^ [1 / 2]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameExponent
                    }
                    step {
                        fromExpr = "5 * [5 ^ [1 / 2]] * [2 ^ [1 / 2]]"
                        toExpr = "5 * [(5 * 2) ^ [1 / 2]]"
                        explanation {
                            key = GeneralExplanation.RewriteProductOfPowersWithSameExponent
                        }
                    }
                    step {
                        fromExpr = "5 * [(5 * 2) ^ [1 / 2]]"
                        toExpr = "5 * [10 ^ [1 / 2]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }
            }
        }

    @Test
    fun `test product of integer, rational exponent of integer with same negative exponent`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "26 * [2 ^ -[1 / 2]] * [3 ^ -[1 / 2]]"

            check {
                fromExpr = "26 * [2 ^ -[1 / 2]] * [3 ^ -[1 / 2]]"
                toExpr = "13 * [([2 / 3]) ^ [1 / 2]]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
                step {
                    fromExpr = "26 * [2 ^ -[1 / 2]] * [3 ^ -[1 / 2]]"
                    toExpr = "<. [2 ^ [1 / 2]] * 13 .> * [3 ^ -[1 / 2]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfIntegerAndRationalExponentOfInteger
                    }
                    step {
                        fromExpr = "<. 26 * [2 ^ -[1 / 2]] .> * [3 ^ -[1 / 2]]"
                        toExpr = "<. <. 2 * 13 .> * [2 ^ -[1 / 2]] .> * [3 ^ -[1 / 2]]"
                        explanation {
                            key = GeneralExplanation.FactorizeInteger
                        }
                    }
                    step {
                        fromExpr = "<. 2 * 13 * [2 ^ -[1 / 2]] .> * [3 ^ -[1 / 2]]"
                        toExpr = "<. [2 ^ [1 / 2]] * 13 .> * [3 ^ -[1 / 2]]"
                        explanation {
                            key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                        }
                    }
                }
                step {
                    fromExpr = "[2 ^ [1 / 2]] * 13 * [3 ^ -[1 / 2]]"
                    toExpr = "[([2 / 3]) ^ [1 / 2]] * 13"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithNegatedExponent
                    }
                    step {
                        fromExpr = "[2 ^ [1 / 2]] * 13 * [3 ^ -[1 / 2]]"
                        toExpr = "[2 ^ [1 / 2]] * 13 * [([1 / 3]) ^ [1 / 2]]"
                        explanation {
                            key = GeneralExplanation.RewriteProductOfPowersWithNegatedExponent
                        }
                    }
                    step {
                        fromExpr = "[2 ^ [1 / 2]] * 13 * [([1 / 3]) ^ [1 / 2]]"
                        toExpr = "[([2 / 3]) ^ [1 / 2]] * 13"
                        explanation {
                            key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameExponent
                        }
                    }
                }
                step {
                    fromExpr = "[([2 / 3]) ^ [1 / 2]] * 13"
                    toExpr = "13 * [([2 / 3]) ^ [1 / 2]]"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }
            }
        }
}
