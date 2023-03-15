package methods.integerrationalexponents

import engine.methods.testMethod
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.constantexpressions.ConstantExpressionsPlans
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import kotlin.test.Test

class SimplifyRationalExponents {

    @Test
    fun testSimplifyRationalExponentOnePrimeFactorToInt() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyRationalExponentOfInteger
        inputExpr = "[32 ^ [2 / 5]]"

        check {
            fromExpr = "[32 ^ [2 / 5]]"
            toExpr = "4"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
            }

            step {
                fromExpr = "[32 ^ [2 / 5]]"
                toExpr = "[([2 ^ 5]) ^ [2 / 5]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.FactorizeIntegerUnderRationalExponent
                }
            }

            step {
                fromExpr = "[([2 ^ 5]) ^ [2 / 5]]"
                toExpr = "[2 ^ 2]"
                explanation {
                    key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                }

                step {
                    fromExpr = "[([2 ^ 5]) ^ [2 / 5]]"
                    toExpr = "[2 ^ 5 * [2 / 5]]"
                    explanation {
                        key = GeneralExplanation.MultiplyExponentsUsingPowerRule
                    }
                }

                step {
                    fromExpr = "[2 ^ 5 * [2 / 5]]"
                    toExpr = "[2 ^ 2]"

                    step {
                        fromExpr = "5 * [2 / 5]"
                        toExpr = "[5 / 1] * [2 / 5]"
                        explanation {
                            key = FractionArithmeticExplanation.TurnFactorIntoFractionInProduct
                        }
                    }

                    step {
                        fromExpr = "[5 / 1] * [2 / 5]"
                        toExpr = "[5 * 2 / 1 * 5]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyFractions
                        }
                    }

                    step {
                        fromExpr = "[5 * 2 / 1 * 5]"
                        toExpr = "2"
                        explanation {
                            key = FractionArithmeticExplanation.SimplifyFraction
                        }

                        step {
                            fromExpr = "[5 * 2 / 1 * 5]"
                            toExpr = "[2 / 1]"
                            explanation {
                                key = GeneralExplanation.CancelCommonTerms
                            }
                        }

                        step {
                            fromExpr = "[2 / 1]"
                            toExpr = "2"
                            explanation {
                                key = GeneralExplanation.SimplifyFractionWithOneDenominator
                            }
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 ^ 2]"
                toExpr = "4"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                }
            }
        }
    }

    @Test
    fun testSimplifyRationalExponentOnePrimeFactorToProductWithInt() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyRationalExponentOfInteger
        inputExpr = "[8 ^ [7 / 6] ]"

        check {
            fromExpr = "[8 ^ [7 / 6]]"
            toExpr = "8 * [2 ^ [1 / 2]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
            }

            step {
                fromExpr = "[8 ^ [7 / 6]]"
                toExpr = "[([2 ^ 3]) ^ [7 / 6]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.FactorizeIntegerUnderRationalExponent
                }
            }

            step {
                fromExpr = "[([2 ^ 3]) ^ [7 / 6]]"
                toExpr = "[2 ^ [7 / 2]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                }

                step {
                    fromExpr = "[([2 ^ 3]) ^ [7 / 6]]"
                    toExpr = "[2 ^ 3 * [7 / 6]]"
                    explanation {
                        key = GeneralExplanation.MultiplyExponentsUsingPowerRule
                    }
                }

                step {
                    fromExpr = "[2 ^ 3 * [7 / 6]]"
                    toExpr = "[2 ^ [7 / 2]]"

                    step {
                        fromExpr = "3 * [7 / 6]"
                        toExpr = "[3 / 1] * [7 / 6]"
                        explanation {
                            key = FractionArithmeticExplanation.TurnFactorIntoFractionInProduct
                        }
                    }

                    step {
                        fromExpr = "[3 / 1] * [7 / 6]"
                        toExpr = "[3 * 7 / 1 * 6]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyFractions
                        }
                    }

                    step {
                        fromExpr = "[3 * 7 / 1 * 6]"
                        toExpr = "[7 / 1 * 2]"
                        explanation {
                            key = FractionArithmeticExplanation.SimplifyFraction
                        }

                        step {
                            fromExpr = "[3 * 7 / 1 * 6]"
                            toExpr = "[3 * 7 / 1 * 3 * 2]"
                            explanation {
                                key = FractionArithmeticExplanation.FindCommonFactorInFraction
                            }
                        }

                        step {
                            fromExpr = "[3 * 7 / 1 * 3 * 2]"
                            toExpr = "[7 / 1 * 2]"
                            explanation {
                                key = GeneralExplanation.CancelCommonTerms
                            }
                        }
                    }

                    step {
                        fromExpr = "[7 / 1 * 2]"
                        toExpr = "[7 / 2]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                        }

                        step {
                            fromExpr = "1 * 2"
                            toExpr = "2"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                            }
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 ^ [7 / 2]]"
                toExpr = "[2 ^ 3] * [2 ^ [1 / 2]]"

                step {
                    fromExpr = "[2 ^ [7 / 2]]"
                    toExpr = "[2 ^ 3 + [1 / 2]]"
                    explanation {
                        key = FractionArithmeticExplanation.ConvertImproperFractionToSumOfIntegerAndFraction
                    }
                }

                step {
                    fromExpr = "[2 ^ 3 + [1 / 2]]"
                    toExpr = "[2 ^ 3] * [2 ^ [1 / 2]]"
                }
            }

            step {
                fromExpr = "[2 ^ 3] * [2 ^ [1 / 2]]"
                toExpr = "8 * [2 ^ [1 / 2]]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                }
            }
        }
    }

    @Test
    fun testSimplifyUsingFactoringBeforeComputingProduct() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[8 ^ [2 / 3]] * [3 ^ [2 / 3]]"

        check {
            fromExpr = "[8 ^ [2 / 3]] * [3 ^ [2 / 3]]"
            toExpr = "4 * [3 ^ [2 / 3]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
            }

            step {
                fromExpr = "[8 ^ [2 / 3]]"
                toExpr = "[([2 ^ 3]) ^ [2 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.FactorizeIntegerUnderRationalExponent
                }
            }

            step {
                fromExpr = "[([2 ^ 3]) ^ [2 / 3]]"
                toExpr = "[2 ^ 2]"
                explanation {
                    key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                }

                step {
                    fromExpr = "[([2 ^ 3]) ^ [2 / 3]]"
                    toExpr = "[2 ^ 3 * [2 / 3]]"
                    explanation {
                        key = GeneralExplanation.MultiplyExponentsUsingPowerRule
                    }
                }

                step {
                    fromExpr = "[2 ^ 3 * [2 / 3]]"
                    toExpr = "[2 ^ 2]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                    }
                }
            }

            step {
                fromExpr = "[2 ^ 2]"
                toExpr = "4"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                }
            }
        }
    }

    @Test
    fun testMultiplySameExponentPowersBeforeSimplification() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[4 ^ [2 / 3]] * [6 ^ [2 / 3]]"

        check {
            fromExpr = "[4 ^ [2 / 3]] * [6 ^ [2 / 3]]"
            toExpr = "4 * [3 ^ [2 / 3]]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "[4 ^ [2 / 3]] * [6 ^ [2 / 3]]"
                toExpr = "[24 ^ [2 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameExponent
                }
            }

            step {
                fromExpr = "[24 ^ [2 / 3]]"
                toExpr = "4 * [3 ^ [2 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
                }

                step {
                    fromExpr = "[24 ^ [2 / 3]]"
                    toExpr = "[([2 ^ 3] * 3) ^ [2 / 3]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.FactorizeIntegerUnderRationalExponent
                    }
                }

                step {
                    fromExpr = "[([2 ^ 3] * 3) ^ [2 / 3]]"
                    toExpr = "[([2 ^ 3]) ^ [2 / 3]] * [3 ^ [2 / 3]]"
                    explanation {
                        key = GeneralExplanation.DistributePowerOfProduct
                    }
                }

                step {
                    fromExpr = "[([2 ^ 3]) ^ [2 / 3]] * [3 ^ [2 / 3]]"
                    toExpr = "[2 ^ 2] * [3 ^ [2 / 3]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                    }
                }

                step {
                    fromExpr = "[2 ^ 2] * [3 ^ [2 / 3]]"
                    toExpr = "4 * [3 ^ [2 / 3]]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                    }
                }
            }
        }
    }

    @Test
    fun testSimplifyProductOfPowersWithNegatedExponent() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyProductOfPowersWithNegatedExponent
        inputExpr = "[2 ^ [1 / 2]] * [4 ^ -[1 / 2]]"

        check {
            fromExpr = "[2 ^ [1 / 2]] * [4 ^ -[1 / 2]]"
            toExpr = "[([1 / 2]) ^ [1 / 2]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithNegatedExponent
            }

            step {
                fromExpr = "[2 ^ [1 / 2]] * [4 ^ -[1 / 2]]"
                toExpr = "[2 ^ [1 / 2]] * [([1 / 4]) ^ [1 / 2]]"
                explanation {
                    key = GeneralExplanation.RewriteProductOfPowersWithNegatedExponent
                }
            }

            step {
                fromExpr = "[2 ^ [1 / 2]] * [([1 / 4]) ^ [1 / 2]]"
                toExpr = "[([1 / 2]) ^ [1 / 2]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameExponent
                }

                step {
                    fromExpr = "[2 ^ [1 / 2]] * [([1 / 4]) ^ [1 / 2]]"
                    toExpr = "[(2 * [1 / 4]) ^ [1 / 2]]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameExponent
                    }
                }

                step {
                    fromExpr = "[(2 * [1 / 4]) ^ [1 / 2]]"
                    toExpr = "[([1 / 2]) ^ [1 / 2]]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                    }
                }
            }
        }
    }

    @Test
    fun testSimplifyProductOfPowersWithInverseFractionBase() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyProductOfPowersWithInverseFractionBase
        inputExpr = "[([2 / 3]) ^ [1 / 2]] * [([3 / 2]) ^ [2 / 5]]"

        check {
            fromExpr = "[([2 / 3]) ^ [1 / 2]] * [([3 / 2]) ^ [2 / 5]]"
            toExpr = "[([2 / 3]) ^ [1 / 10]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithInverseFractionBase
            }

            step {
                fromExpr = "[([2 / 3]) ^ [1 / 2]] * [([3 / 2]) ^ [2 / 5]]"
                toExpr = "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ -[2 / 5]]"
                explanation {
                    key = GeneralExplanation.RewriteProductOfPowersWithInverseFractionBase
                }
            }

            step {
                fromExpr = "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ -[2 / 5]]"
                toExpr = "[([2 / 3]) ^ [1 / 10]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }

                step {
                    fromExpr = "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ -[2 / 5]]"
                    toExpr = "[([2 / 3]) ^ [1 / 2] - [2 / 5]]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "[([2 / 3]) ^ [1 / 2] - [2 / 5]]"
                    toExpr = "[([2 / 3]) ^ [1 / 10]]"
                    explanation {
                        key = FractionArithmeticExplanation.AddFractions
                    }
                }
            }
        }
    }

    @Test
    fun testSimplifyProductOfPowersWithInverseBase() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyProductOfPowersWithInverseBase
        inputExpr = "[3 ^ [1 / 2]] * [([1 / 3]) ^ [2 / 5]]"

        check {
            fromExpr = "[3 ^ [1 / 2]] * [([1 / 3]) ^ [2 / 5]]"
            toExpr = "[3 ^ [1 / 10]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithInverseBase
            }

            step {
                fromExpr = "[3 ^ [1 / 2]] * [([1 / 3]) ^ [2 / 5]]"
                toExpr = "[3 ^ [1 / 2]] * [3 ^ -[2 / 5]]"
                explanation {
                    key = GeneralExplanation.RewriteProductOfPowersWithInverseBase
                }
            }

            step {
                fromExpr = "[3 ^ [1 / 2]] * [3 ^ -[2 / 5]]"
                toExpr = "[3 ^ [1 / 10]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }

                step {
                    fromExpr = "[3 ^ [1 / 2]] * [3 ^ -[2 / 5]]"
                    toExpr = "[3 ^ [1 / 2] - [2 / 5]]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "[3 ^ [1 / 2] - [2 / 5]]"
                    toExpr = "[3 ^ [1 / 10]]"
                    explanation {
                        key = FractionArithmeticExplanation.AddFractions
                    }
                }
            }
        }
    }

    @Test
    fun testPowerRuleOfExponents() = testMethod {
        method = IntegerRationalExponentsPlans.ApplyPowerRuleOfExponents
        inputExpr = "[([(sqrt[5]) ^ [1 / 2]]) ^ [2 / 3]]"

        check {
            fromExpr = "[([(sqrt[5]) ^ [1 / 2]]) ^ [2 / 3]]"
            toExpr = "[(sqrt[5]) ^ [1 / 3]]"
            explanation {
                key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
            }

            step {
                fromExpr = "[([(sqrt[5]) ^ [1 / 2]]) ^ [2 / 3]]"
                toExpr = "[(sqrt[5]) ^ [1 / 2] * [2 / 3]]"
                explanation {
                    key = GeneralExplanation.MultiplyExponentsUsingPowerRule
                }
            }

            step {
                fromExpr = "[(sqrt[5]) ^ [1 / 2] * [2 / 3]]"
                toExpr = "[(sqrt[5]) ^ [1 / 3]]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                }
            }
        }
    }

    @Test
    fun testSimplifyFractionOfPowersWithSameBase() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyFractionOfPowersWithSameBase
        inputExpr = "[[2 ^ [1 / 2]] / [2 ^ [1 / 3]]]"

        check {
            fromExpr = "[[2 ^ [1 / 2]] / [2 ^ [1 / 3]]]"
            toExpr = "[2 ^ [1 / 6]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyFractionOfPowersWithSameBase
            }

            step {
                fromExpr = "[[2 ^ [1 / 2]] / [2 ^ [1 / 3]]]"
                toExpr = "[2 ^ [1 / 2] - [1 / 3]]"
                explanation {
                    key = GeneralExplanation.RewriteFractionOfPowersWithSameBase
                }
            }

            step {
                fromExpr = "[2 ^ [1 / 2] - [1 / 3]]"
                toExpr = "[2 ^ [1 / 6]]"
                explanation {
                    key = FractionArithmeticExplanation.AddFractions
                }
            }
        }
    }

    @Test
    fun testSimplifyFractionOfPowersWithSameExponent() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyFractionOfPowersWithSameExponent
        inputExpr = "[[2 ^ [1 / 2]] / [6 ^ [1 / 2]]]"

        check {
            fromExpr = "[[2 ^ [1 / 2]] / [6 ^ [1 / 2]]]"
            toExpr = "[([1 / 3]) ^ [1 / 2]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyFractionOfPowersWithSameExponent
            }

            step {
                fromExpr = "[[2 ^ [1 / 2]] / [6 ^ [1 / 2]]]"
                toExpr = "[([2 / 6]) ^ [1 / 2]]"
                explanation {
                    key = GeneralExplanation.RewriteFractionOfPowersWithSameExponent
                }
            }

            step {
                fromExpr = "[([2 / 6]) ^ [1 / 2]]"
                toExpr = "[([1 / 3]) ^ [1 / 2]]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }
            }
        }
    }

    @Test
    fun testSimplifyProductOfPowersWithSameBase() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase
        inputExpr = "[20 ^ 2] * [20 ^ -3]"

        check {
            toExpr = "[20 ^ -1]"

            step { toExpr = "[20 ^ 2 - 3]" }
            step { toExpr = "[20 ^ -1]" }
        }
    }

    @Test
    fun testRationalExponentCoPrimeWithPowerOfPrimeFactorOfBase() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyRationalExponentOfInteger
        inputExpr = "[125 ^ [1 / 2]]"

        check {
            toExpr = "5 * [5 ^ [1 / 2]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
            }

            step {
                fromExpr = "[125 ^ [1 / 2]]"
                toExpr = "[([5 ^ 3]) ^ [1 / 2]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.FactorizeIntegerUnderRationalExponent
                }
            }

            step {
                fromExpr = "[([5 ^ 3]) ^ [1 / 2]]"
                toExpr = "[5 ^ [3 / 2]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                }
            }

            step {
                fromExpr = "[5 ^ [3 / 2]]"
                toExpr = "5 * [5 ^ [1 / 2]]"
                explanation {
                    key = FractionArithmeticExplanation.SplitRationalExponent
                }
            }
        }
    }
}

class SimplifyRationalExponentsDontApply {
    @Test
    fun testDontSimplifyMultipleFactors() = testMethod {
        method = IntegerRationalExponentsPlans.SimplifyRationalExponentOfInteger
        inputExpr = "[480 ^ [1 / 6]]"

        check {
            noTransformation()
        }
    }
}
