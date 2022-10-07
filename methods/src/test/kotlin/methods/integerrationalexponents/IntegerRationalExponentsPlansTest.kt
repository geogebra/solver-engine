package methods.integerrationalexponents

import method.integerrationalexponents.IntegerRationalExponentsExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.mixednumbers.MixedNumbersExplanation
import methods.plans.testMethod
import kotlin.test.Test

class SimplifyRationalExponents {

    @Test
    fun testSimplifyRationalExponentOnePrimeFactorToInt() = testMethod {
        method = simplifyRationalExponentOfInteger
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
                            key = FractionArithmeticExplanation.MultiplyFractions
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
        method = simplifyRationalExponentOfInteger
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
                            key = FractionArithmeticExplanation.MultiplyFractions
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
                    toExpr = "[2 ^ [3 1/2]]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertFractionToMixedNumber
                    }
                }

                step {
                    fromExpr = "[2 ^ [3 1/2]]"
                    toExpr = "[2 ^ 3 + [1 / 2]]"
                    explanation {
                        key = MixedNumbersExplanation.ConvertMixedNumberToSum
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
    fun testSimplifyRationalExponentMoreThanOnePrimeFactor() = testMethod {
        method = simplifyRationalExponentOfInteger
        inputExpr = "[18 ^ [2 / 3]]"

        check {
            fromExpr = "[18 ^ [2 / 3]]"
            toExpr = "3 * [2 ^ [2 / 3]] * [3 ^ [1 / 3]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
            }

            step {
                fromExpr = "[18 ^ [2 / 3]]"
                toExpr = "[(2 * [3 ^ 2]) ^ [2 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.FactorizeIntegerUnderRationalExponent
                }
            }

            step {
                fromExpr = "[(2 * [3 ^ 2]) ^ [2 / 3]]"
                toExpr = "[2 ^ [2 / 3]] * [([3 ^ 2]) ^ [2 / 3]]"
                explanation {
                    key = GeneralExplanation.DistributePowerOfProduct
                }
            }

            step {
                fromExpr = "[2 ^ [2 / 3]] * [([3 ^ 2]) ^ [2 / 3]]"
                toExpr = "[2 ^ [2 / 3]] * [3 ^ [4 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                }

                step {
                    fromExpr = "[([3 ^ 2]) ^ [2 / 3]]"
                    toExpr = "[3 ^ 2 * [2 / 3]]"
                    explanation {
                        key = GeneralExplanation.MultiplyExponentsUsingPowerRule
                    }
                }

                step {
                    fromExpr = "[3 ^ 2 * [2 / 3]]"
                    toExpr = "[3 ^ [4 / 3]]"

                    step {
                        fromExpr = "2 * [2 / 3]"
                        toExpr = "[2 / 1] * [2 / 3]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyFractions
                        }
                    }

                    step {
                        fromExpr = "[2 / 1] * [2 / 3]"
                        toExpr = "[2 * 2 / 1 * 3]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyFractions
                        }
                    }

                    step {
                        fromExpr = "[2 * 2 / 1 * 3]"
                        toExpr = "[4 / 1 * 3]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                        }

                        step {
                            fromExpr = "2 * 2"
                            toExpr = "4"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                            }
                        }
                    }

                    step {
                        fromExpr = "[4 / 1 * 3]"
                        toExpr = "[4 / 3]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                        }

                        step {
                            fromExpr = "1 * 3"
                            toExpr = "3"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                            }
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 ^ [2 / 3]] * [3 ^ [4 / 3]]"
                toExpr = "[2 ^ [2 / 3]] * 3 * [3 ^ [1 / 3]]"

                step {
                    fromExpr = "[2 ^ [2 / 3]] * [3 ^ [4 / 3]]"
                    toExpr = "[2 ^ [2 / 3]] * (3 * [3 ^ [1 / 3]])"

                    step {
                        fromExpr = "[3 ^ [4 / 3]]"
                        toExpr = "[3 ^ [1 1/3]]"
                        explanation {
                            key = MixedNumbersExplanation.ConvertFractionToMixedNumber
                        }
                    }

                    step {
                        fromExpr = "[3 ^ [1 1/3]]"
                        toExpr = "[3 ^ 1 + [1 / 3]]"
                        explanation {
                            key = MixedNumbersExplanation.ConvertMixedNumberToSum
                        }
                    }

                    step {
                        fromExpr = "[3 ^ 1 + [1 / 3]]"
                        toExpr = "3 * [3 ^ [1 / 3]]"
                    }
                }

                step {
                    fromExpr = "[2 ^ [2 / 3]] * (3 * [3 ^ [1 / 3]])"
                    toExpr = "[2 ^ [2 / 3]] * 3 * [3 ^ [1 / 3]]"
                    explanation {
                        key = GeneralExplanation.RemoveBracketProductInProduct
                    }
                }
            }

            step {
                fromExpr = "[2 ^ [2 / 3]] * 3 * [3 ^ [1 / 3]]"
                toExpr = "3 * [2 ^ [2 / 3]] * [3 ^ [1 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.NormaliseProductWithRationalExponents
                }
            }
        }
    }
}

class SimplifyRationalExponentDifferentPrimeFactors {

    @Test
    fun testSimplifyRationalExponentDiffKindsOfPrimeFactor() = testMethod {
        method = simplifyRationalExponentOfInteger
        inputExpr = "[93750 ^ [1 / 5]]"

        check {
            fromExpr = "[93750 ^ [1 / 5]]"
            toExpr = "5 * [2 ^ [1 / 5]] * [3 ^ [1 / 5]] * [5 ^ [1 / 5]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
            }

            step {
                fromExpr = "[93750 ^ [1 / 5]]"
                toExpr = "[(2 * 3 * [5 ^ 6]) ^ [1 / 5]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.FactorizeIntegerUnderRationalExponent
                }
            }

            step {
                fromExpr = "[(2 * 3 * [5 ^ 6]) ^ [1 / 5]]"
                toExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * [([5 ^ 6]) ^ [1 / 5]]"
                explanation {
                    key = GeneralExplanation.DistributePowerOfProduct
                }
            }

            step {
                fromExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * [([5 ^ 6]) ^ [1 / 5]]"
                toExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * [5 ^ [6 / 5]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                }

                step {
                    fromExpr = "[([5 ^ 6]) ^ [1 / 5]]"
                    toExpr = "[5 ^ 6 * [1 / 5]]"
                    explanation {
                        key = GeneralExplanation.MultiplyExponentsUsingPowerRule
                    }
                }

                step {
                    fromExpr = "[5 ^ 6 * [1 / 5]]"
                    toExpr = "[5 ^ [6 / 5]]"

                    step {
                        fromExpr = "6 * [1 / 5]"
                        toExpr = "[6 / 1] * [1 / 5]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyFractions
                        }
                    }

                    step {
                        fromExpr = "[6 / 1] * [1 / 5]"
                        toExpr = "[6 * 1 / 1 * 5]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyFractions
                        }
                    }

                    step {
                        fromExpr = "[6 * 1 / 1 * 5]"
                        toExpr = "[6 / 1 * 5]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                        }

                        step {
                            fromExpr = "6 * 1"
                            toExpr = "6"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                            }
                        }
                    }

                    step {
                        fromExpr = "[6 / 1 * 5]"
                        toExpr = "[6 / 5]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                        }

                        step {
                            fromExpr = "1 * 5"
                            toExpr = "5"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                            }
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * [5 ^ [6 / 5]]"
                toExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * 5 * [5 ^ [1 / 5]]"

                step {
                    fromExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * [5 ^ [6 / 5]]"
                    toExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * (5 * [5 ^ [1 / 5]])"

                    step {
                        fromExpr = "[5 ^ [6 / 5]]"
                        toExpr = "[5 ^ [1 1/5]]"
                        explanation {
                            key = MixedNumbersExplanation.ConvertFractionToMixedNumber
                        }
                    }

                    step {
                        fromExpr = "[5 ^ [1 1/5]]"
                        toExpr = "[5 ^ 1 + [1 / 5]]"
                        explanation {
                            key = MixedNumbersExplanation.ConvertMixedNumberToSum
                        }
                    }

                    step {
                        fromExpr = "[5 ^ 1 + [1 / 5]]"
                        toExpr = "5 * [5 ^ [1 / 5]]"
                    }
                }

                step {
                    fromExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * (5 * [5 ^ [1 / 5]])"
                    toExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * 5 * [5 ^ [1 / 5]]"
                    explanation {
                        key = GeneralExplanation.RemoveBracketProductInProduct
                    }
                }
            }

            step {
                fromExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]] * 5 * [5 ^ [1 / 5]]"
                toExpr = "5 * [2 ^ [1 / 5]] * [3 ^ [1 / 5]] * [5 ^ [1 / 5]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.NormaliseProductWithRationalExponents
                }
            }
        }
    }

    @Test
    fun testSimplifyRationalExponentDiffKindsOfPrimeFactor2() = testMethod {
        method = simplifyRationalExponentOfInteger
        inputExpr = "[85050 ^ [2 / 3]]"

        check {
            fromExpr = "[85050 ^ [2 / 3]]"
            toExpr = "135 * [2 ^ [2 / 3]] * [3 ^ [1 / 3]] * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"
            explanation {
                key = IntegerRationalExponentsExplanation.SimplifyRationalExponentOfInteger
            }

            step {
                fromExpr = "[85050 ^ [2 / 3]]"
                toExpr = "[(2 * [3 ^ 5] * [5 ^ 2] * 7) ^ [2 / 3]]"
                explanation {
                    key = IntegerRationalExponentsExplanation.FactorizeIntegerUnderRationalExponent
                }
            }

            step {
                fromExpr = "[(2 * [3 ^ 5] * [5 ^ 2] * 7) ^ [2 / 3]]"
                toExpr = "[2 ^ [2 / 3]] * [([3 ^ 5]) ^ [2 / 3]] * [([5 ^ 2]) ^ [2 / 3]] * [7 ^ [2 / 3]]"
                explanation {
                    key = GeneralExplanation.DistributePowerOfProduct
                }
            }

            step {
                fromExpr = "[2 ^ [2 / 3]] * [([3 ^ 5]) ^ [2 / 3]] * [([5 ^ 2]) ^ [2 / 3]] * [7 ^ [2 / 3]]"
                toExpr = "[2 ^ [2 / 3]] * [3 ^ [10 / 3]] * [5 ^ [4 / 3]] * [7 ^ [2 / 3]]"

                step {
                    fromExpr = "[2 ^ [2 / 3]] * [([3 ^ 5]) ^ [2 / 3]] * [([5 ^ 2]) ^ [2 / 3]] * [7 ^ [2 / 3]]"
                    toExpr = "[2 ^ [2 / 3]] * [3 ^ [10 / 3]] * [([5 ^ 2]) ^ [2 / 3]] * [7 ^ [2 / 3]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                    }

                    step {
                        fromExpr = "[([3 ^ 5]) ^ [2 / 3]]"
                        toExpr = "[3 ^ 5 * [2 / 3]]"
                        explanation {
                            key = GeneralExplanation.MultiplyExponentsUsingPowerRule
                        }
                    }

                    step {
                        fromExpr = "[3 ^ 5 * [2 / 3]]"
                        toExpr = "[3 ^ [10 / 3]]"

                        step {
                            fromExpr = "5 * [2 / 3]"
                            toExpr = "[5 / 1] * [2 / 3]"
                            explanation {
                                key = FractionArithmeticExplanation.MultiplyFractions
                            }
                        }

                        step {
                            fromExpr = "[5 / 1] * [2 / 3]"
                            toExpr = "[5 * 2 / 1 * 3]"
                            explanation {
                                key = FractionArithmeticExplanation.MultiplyFractions
                            }
                        }

                        step {
                            fromExpr = "[5 * 2 / 1 * 3]"
                            toExpr = "[10 / 1 * 3]"
                            explanation {
                                key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                            }

                            step {
                                fromExpr = "5 * 2"
                                toExpr = "10"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                                }
                            }
                        }

                        step {
                            fromExpr = "[10 / 1 * 3]"
                            toExpr = "[10 / 3]"
                            explanation {
                                key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                            }

                            step {
                                fromExpr = "1 * 3"
                                toExpr = "3"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                                }
                            }
                        }
                    }
                }

                step {
                    fromExpr = "[2 ^ [2 / 3]] * [3 ^ [10 / 3]] * [([5 ^ 2]) ^ [2 / 3]] * [7 ^ [2 / 3]]"
                    toExpr = "[2 ^ [2 / 3]] * [3 ^ [10 / 3]] * [5 ^ [4 / 3]] * [7 ^ [2 / 3]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.PowerRuleOfExponents
                    }

                    step {
                        fromExpr = "[([5 ^ 2]) ^ [2 / 3]]"
                        toExpr = "[5 ^ 2 * [2 / 3]]"
                        explanation {
                            key = GeneralExplanation.MultiplyExponentsUsingPowerRule
                        }
                    }

                    step {
                        fromExpr = "[5 ^ 2 * [2 / 3]]"
                        toExpr = "[5 ^ [4 / 3]]"

                        step {
                            fromExpr = "2 * [2 / 3]"
                            toExpr = "[2 / 1] * [2 / 3]"
                            explanation {
                                key = FractionArithmeticExplanation.MultiplyFractions
                            }
                        }

                        step {
                            fromExpr = "[2 / 1] * [2 / 3]"
                            toExpr = "[2 * 2 / 1 * 3]"
                            explanation {
                                key = FractionArithmeticExplanation.MultiplyFractions
                            }
                        }

                        step {
                            fromExpr = "[2 * 2 / 1 * 3]"
                            toExpr = "[4 / 1 * 3]"
                            explanation {
                                key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                            }

                            step {
                                fromExpr = "2 * 2"
                                toExpr = "4"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                                }
                            }
                        }

                        step {
                            fromExpr = "[4 / 1 * 3]"
                            toExpr = "[4 / 3]"
                            explanation {
                                key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                            }

                            step {
                                fromExpr = "1 * 3"
                                toExpr = "3"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                                }
                            }
                        }
                    }
                }
            }

            step {
                fromExpr = "[2 ^ [2 / 3]] * [3 ^ [10 / 3]] * [5 ^ [4 / 3]] * [7 ^ [2 / 3]]"
                toExpr = "[2 ^ [2 / 3]] * [3 ^ 3] * [3 ^ [1 / 3]] * 5 * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"

                step {
                    fromExpr = "[2 ^ [2 / 3]] * [3 ^ [10 / 3]] * [5 ^ [4 / 3]] * [7 ^ [2 / 3]]"
                    toExpr = "[2 ^ [2 / 3]] * ([3 ^ 3] * [3 ^ [1 / 3]]) * [5 ^ [4 / 3]] * [7 ^ [2 / 3]]"

                    step {
                        fromExpr = "[3 ^ [10 / 3]]"
                        toExpr = "[3 ^ [3 1/3]]"
                        explanation {
                            key = MixedNumbersExplanation.ConvertFractionToMixedNumber
                        }
                    }

                    step {
                        fromExpr = "[3 ^ [3 1/3]]"
                        toExpr = "[3 ^ 3 + [1 / 3]]"
                        explanation {
                            key = MixedNumbersExplanation.ConvertMixedNumberToSum
                        }
                    }

                    step {
                        fromExpr = "[3 ^ 3 + [1 / 3]]"
                        toExpr = "[3 ^ 3] * [3 ^ [1 / 3]]"
                    }
                }

                step {
                    fromExpr = "[2 ^ [2 / 3]] * ([3 ^ 3] * [3 ^ [1 / 3]]) * [5 ^ [4 / 3]] * [7 ^ [2 / 3]]"
                    toExpr = "[2 ^ [2 / 3]] * ([3 ^ 3] * [3 ^ [1 / 3]]) * (5 * [5 ^ [1 / 3]]) * [7 ^ [2 / 3]]"

                    step {
                        fromExpr = "[5 ^ [4 / 3]]"
                        toExpr = "[5 ^ [1 1/3]]"
                        explanation {
                            key = MixedNumbersExplanation.ConvertFractionToMixedNumber
                        }
                    }

                    step {
                        fromExpr = "[5 ^ [1 1/3]]"
                        toExpr = "[5 ^ 1 + [1 / 3]]"
                        explanation {
                            key = MixedNumbersExplanation.ConvertMixedNumberToSum
                        }
                    }

                    step {
                        fromExpr = "[5 ^ 1 + [1 / 3]]"
                        toExpr = "5 * [5 ^ [1 / 3]]"
                    }
                }

                step {
                    fromExpr = "[2 ^ [2 / 3]] * ([3 ^ 3] * [3 ^ [1 / 3]]) * (5 * [5 ^ [1 / 3]]) * [7 ^ [2 / 3]]"
                    toExpr = "[2 ^ [2 / 3]] * [3 ^ 3] * [3 ^ [1 / 3]] * (5 * [5 ^ [1 / 3]]) * [7 ^ [2 / 3]]"
                    explanation {
                        key = GeneralExplanation.RemoveBracketProductInProduct
                    }
                }

                step {
                    fromExpr = "[2 ^ [2 / 3]] * [3 ^ 3] * [3 ^ [1 / 3]] * (5 * [5 ^ [1 / 3]]) * [7 ^ [2 / 3]]"
                    toExpr = "[2 ^ [2 / 3]] * [3 ^ 3] * [3 ^ [1 / 3]] * 5 * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"
                    explanation {
                        key = GeneralExplanation.RemoveBracketProductInProduct
                    }
                }
            }

            step {
                fromExpr = "[2 ^ [2 / 3]] * [3 ^ 3] * [3 ^ [1 / 3]] * 5 * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"
                toExpr = "135 * [2 ^ [2 / 3]] * [3 ^ [1 / 3]] * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"

                step {
                    fromExpr = "[2 ^ [2 / 3]] * [3 ^ 3] * [3 ^ [1 / 3]] * 5 * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"
                    toExpr = "[3 ^ 3] * 5 * [2 ^ [2 / 3]] * [3 ^ [1 / 3]] * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.NormaliseProductWithRationalExponents
                    }
                }

                step {
                    fromExpr = "[3 ^ 3] * 5 * [2 ^ [2 / 3]] * [3 ^ [1 / 3]] * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"
                    toExpr = "27 * 5 * [2 ^ [2 / 3]] * [3 ^ [1 / 3]] * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                    }
                }

                step {
                    fromExpr = "27 * 5 * [2 ^ [2 / 3]] * [3 ^ [1 / 3]] * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"
                    toExpr = "135 * [2 ^ [2 / 3]] * [3 ^ [1 / 3]] * [5 ^ [1 / 3]] * [7 ^ [2 / 3]]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
        }
    }
}

class SimplifyRationalExponentsDontApply {
    @Test
    fun testDontSimplifyMultipleFactors() = testMethod {
        method = simplifyRationalExponentOfInteger
        inputExpr = "[480 ^ [1 / 6]]"

        check {
            noTransformation()
        }
    }
}
