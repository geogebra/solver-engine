package methods.integerrationalexponents

import method.integerrationalexponents.IntegerRationalExponentsExplanation
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.constantexpressions.simplifyConstantExpression
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
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
        method = simplifyConstantExpression
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
        method = simplifyConstantExpression
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
