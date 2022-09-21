package methods.decimals

import engine.context.Context
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.plans.testMethod
import org.junit.jupiter.api.Test

class DecimalsPlansTest {

    @Test
    fun testDirectConversionOfRecurringDecimalToFraction() = testMethod {
        method = convertRecurringDecimalToFractionAndSimplify
        context = Context(curriculum = "EU")
        inputExpr = "3.14[15]"

        check {
            fromExpr = "3.14[15]"
            toExpr = "[10367 / 3300]"
            explanation {
                key = DecimalsExplanation.ConvertRecurringDecimalToFractionAndSimplify
            }

            step {
                fromExpr = "3.14[15]"
                toExpr = "[31415 - 314 / 9900]"
                explanation {
                    key = DecimalsExplanation.ConvertRecurringDecimalToFractionDirectly
                }
            }

            step {
                fromExpr = "[31415 - 314 / 9900]"
                toExpr = "[31101 / 9900]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                }
            }

            step {
                fromExpr = "[31101 / 9900]"
                toExpr = "[10367 / 3300]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }
            }
        }
    }

    @Test
    fun testAlgorithmForConversionOfRecurringDecimalToFraction() = testMethod {
        method = convertRecurringDecimalToFractionAndSimplify
        context = Context(curriculum = "US")
        inputExpr = "3.14[15]"

        check {
            fromExpr = "3.14[15]"
            toExpr = "[10367 / 3300]"
            explanation {
                key = DecimalsExplanation.ConvertRecurringDecimalToFractionAndSimplify
            }

            step {
                fromExpr = "3.14[15]"
                toExpr = "x = 3.14[15]"
                explanation {
                    key = DecimalsExplanation.ConvertRecurringDecimalToEquation
                }
            }

            step {
                fromExpr = "x = 3.14[15]"
                toExpr = "100 * x = 314.[15], 10000 * x = 31415.[15]"
                explanation {
                    key = DecimalsExplanation.MakeEquationSystemForRecurringDecimal
                }

                step {
                    fromExpr = "x = 3.14[15]"
                    toExpr = "100 * x = 314.[15]"
                    explanation {
                        key = DecimalsExplanation.MultiplyRecurringDecimal
                    }
                }

                step {
                    fromExpr = "x = 3.14[15]"
                    toExpr = "10000 * x = 31415.[15]"
                    explanation {
                        key = DecimalsExplanation.MultiplyRecurringDecimal
                    }
                }
            }

            step {
                fromExpr = "100 * x = 314.[15], 10000 * x = 31415.[15]"
                toExpr = "9900 * x = 31101"
                explanation {
                    key = DecimalsExplanation.SimplifyEquationSystemForRecurringDecimal
                }
            }

            step {
                fromExpr = "9900 * x = 31101"
                toExpr = "[31101 / 9900]"
                explanation {
                    key = DecimalsExplanation.SolveLinearEquation
                }
            }

            step {
                fromExpr = "[31101 / 9900]"
                toExpr = "[10367 / 3300]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }
            }
        }
    }

    @Test
    fun testSimplifyDecimalsInProduct() = testMethod {
        method = simplifyDecimalsInProduct
        inputExpr = "0.2 * 0.3 * 10"

        check {
            toExpr = "0.6"
            explanation {
                key = DecimalsExplanation.SimplifyDecimalsInProduct
            }
        }
    }

    @Test
    fun testNormalizeFractionOfDecimals() = testMethod {
        method = normalizeFractionOfDecimals
        inputExpr = "[0.24 / 0.002]"

        check {
            fromExpr = "[0.24 / 0.002]"
            toExpr = "[240 / 2]"
            explanation {
                key = DecimalsExplanation.NormalizeFractionOfDecimals
            }

            step {
                fromExpr = "[0.24 / 0.002]"
                toExpr = "[0.24 * 1000 / 0.002 * 1000]"
                explanation {
                    key = DecimalsExplanation.MultiplyFractionOfDecimalsByPowerOfTen
                }
            }

            step {
                fromExpr = "[0.24 * 1000 / 0.002 * 1000]"
                toExpr = "[240 / 0.002 * 1000]"
                explanation {
                    key = DecimalsExplanation.SimplifyDecimalsInProduct
                }
            }

            step {
                fromExpr = "[240 / 0.002 * 1000]"
                toExpr = "[240 / 2]"
                explanation {
                    key = DecimalsExplanation.SimplifyDecimalsInProduct
                }
            }
        }
    }

    @Test
    fun testNormalizeFractionOfDecimalsWithIntegerNumerator() = testMethod {
        method = normalizeFractionOfDecimals
        inputExpr = "[5 / 0.35]"

        check {
            toExpr = "[500 / 35]"
        }
    }

    @Test
    fun testNormalizeFractionOfDecimalsWithIntegerDenominator() = testMethod {
        method = normalizeFractionOfDecimals
        inputExpr = "[0.006 / 2]"

        check {
            toExpr = "[6 / 2000]"
        }
    }
}
