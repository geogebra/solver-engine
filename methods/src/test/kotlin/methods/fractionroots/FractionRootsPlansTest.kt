package methods.fractionroots

import methods.constantexpressions.simplifyConstantExpression
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerroots.IntegerRootsExplanation
import methods.plans.testMethod
import org.junit.jupiter.api.Test

class FractionRootsPlansTest {

    @Test
    fun testRationalizationOfSimpleDenominators() = testMethod {
        method = rationalizeDenominators
        inputExpr = "sqrt[[3 / 2]]"

        check {
            toExpr = "[sqrt[6] / 2]"
            explanation {
                key = FractionRootsExplanation.RationalizeDenominator
            }

            step {
                toExpr = "[sqrt[3] / sqrt[2]]"
                explanation {
                    key = FractionRootsExplanation.DistributeRadicalOverFraction
                }
            }

            step {
                toExpr = "[sqrt[3] / sqrt[2]] * [sqrt[2] / sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[3] * sqrt[2] / sqrt[2] * sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[6] / sqrt[2] * sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[6] / 2]"
            }
        }
    }

    @Test
    fun testRationalizationRadicalWithCoefficient() = testMethod {
        method = rationalizeDenominators
        inputExpr = "[sqrt[3] / 3 * sqrt[2]]"

        check {
            toExpr = "[sqrt[6] / 6]"
            explanation {
                key = FractionRootsExplanation.RationalizeDenominator
            }

            step {
                toExpr = "[sqrt[3] / 3 * sqrt[2]] * [sqrt[2] / sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[3] * sqrt[2] / 3 * sqrt[2] * sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[6] / 3 * sqrt[2] * sqrt[2]]"
            }

            step {
                toExpr = "[sqrt[6] / 6]"
            }
        }
    }

    @Test
    fun testRationalizationWithSumOfRadicalsInNumerator() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"

        check {
            fromExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"
            toExpr = "[2 + sqrt[6] / 2]"

            step {
                fromExpr = "[sqrt[2] + sqrt[3] / sqrt[2]]"
                toExpr = "[(sqrt[2] + sqrt[3]) * sqrt[2] / 2]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }
            }

            step {
                fromExpr = "[(sqrt[2] + sqrt[3]) * sqrt[2] / 2]"
                toExpr = "[sqrt[2] * sqrt[2] + sqrt[3] * sqrt[2] / 2]"
                explanation {
                    key = GeneralExplanation.DistributeMultiplicationOverSum
                }
            }

            step {
                fromExpr = "[sqrt[2] * sqrt[2] + sqrt[3] * sqrt[2] / 2]"
                toExpr = "[2 + sqrt[3] * sqrt[2] / 2]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }

                step {
                    fromExpr = "sqrt[2] * sqrt[2]"
                    toExpr = "2"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyMultiplicationOfSquareRoots
                    }
                }
            }

            step {
                fromExpr = "[2 + sqrt[3] * sqrt[2] / 2]"
                toExpr = "[2 + sqrt[6] / 2]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }
        }
    }

    @Test
    fun testRationalizeHigherOrderRoot() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "[9 / 2 * root[18, 4]]"

        check {
            fromExpr = "[9 / 2 * root[18, 4]]"
            toExpr = "[3 * root[72, 4] / 4]"

            step {
                fromExpr = "[9 / 2 * root[18, 4]]"
                toExpr = "[9 * root[72, 4] / 2 * root[1296, 4]]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }

                step {
                    fromExpr = "[9 / 2 * root[18, 4]]"
                    toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]]"
                    explanation {
                        key = FractionRootsExplanation.FactorizeHigherOrderRadicand
                    }
                }

                step {
                    fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]]"
                    toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                        "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 2], 4]]"

                    step {
                        fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]]"
                        toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                            "[root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4] / root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                        explanation {
                            key = FractionRootsExplanation.HigherOrderRationalizingTerm
                        }
                    }

                    step {
                        fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                            "[root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4] / root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                        toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                            "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 2], 4]]"

                        step {
                            fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                                "[root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4] / root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                            toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                                "[root[[2 ^ 3] * [3 ^ 4 - 2], 4] / root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }

                        step {
                            fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                                "[root[[2 ^ 3] * [3 ^ 4 - 2], 4] / root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                            toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                                "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }

                        step {
                            fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                                "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 4 - 1] * [3 ^ 4 - 2], 4]]"
                            toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                                "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 4 - 2], 4]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }

                        step {
                            fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                                "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 4 - 2], 4]]"
                            toExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                                "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 2], 4]]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }
                    }
                }

                step {
                    fromExpr = "[9 / 2 * root[2 * [3 ^ 2], 4]] * " +
                        "[root[[2 ^ 3] * [3 ^ 2], 4] / root[[2 ^ 3] * [3 ^ 2], 4]]"
                    toExpr = "[9 * root[[2 ^ 3] * [3 ^ 2], 4] / " +
                        "2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    fromExpr = "[9 * root[[2 ^ 3] * [3 ^ 2], 4] / " +
                        "2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                    toExpr = "[9 * root[72, 4] / 2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"

                    step {
                        fromExpr = "9 * root[[2 ^ 3] * [3 ^ 2], 4]"
                        toExpr = "9 * root[72, 4]"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyProductWithRoots
                        }

                        step {
                            fromExpr = "9 * root[[2 ^ 3] * [3 ^ 2], 4]"
                            toExpr = "9 * root[8 * [3 ^ 2], 4]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                            }
                        }

                        step {
                            fromExpr = "9 * root[8 * [3 ^ 2], 4]"
                            toExpr = "9 * root[8 * 9, 4]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                            }
                        }

                        step {
                            fromExpr = "9 * root[8 * 9, 4]"
                            toExpr = "9 * root[72, 4]"
                            explanation {
                                key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                            }

                            step {
                                fromExpr = "8 * 9"
                                toExpr = "72"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                                }
                            }
                        }
                    }
                }

                step {
                    fromExpr = "[9 * root[72, 4] / 2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]]"
                    toExpr = "[9 * root[72, 4] / 2 * root[1296, 4]]"

                    step {
                        fromExpr = "2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]"
                        toExpr = "2 * root[1296, 4]"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyProductWithRoots
                        }

                        step {
                            fromExpr = "2 * root[2 * [3 ^ 2], 4] * root[[2 ^ 3] * [3 ^ 2], 4]"
                            toExpr = "2 * root[2 * 9, 4] * root[[2 ^ 3] * [3 ^ 2], 4]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                            }
                        }

                        step {
                            fromExpr = "2 * root[2 * 9, 4] * root[[2 ^ 3] * [3 ^ 2], 4]"
                            toExpr = "2 * root[2 * 9, 4] * root[8 * [3 ^ 2], 4]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                            }
                        }

                        step {
                            fromExpr = "2 * root[2 * 9, 4] * root[8 * [3 ^ 2], 4]"
                            toExpr = "2 * root[2 * 9, 4] * root[8 * 9, 4]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                            }
                        }

                        step {
                            fromExpr = "2 * root[2 * 9, 4] * root[8 * 9, 4]"
                            toExpr = "2 * root[2 * 9 * 8 * 9, 4]"
                            explanation {
                                key = IntegerRootsExplanation.MultiplyNthRoots
                            }
                        }

                        step {
                            fromExpr = "2 * root[2 * 9 * 8 * 9, 4]"
                            toExpr = "2 * root[1296, 4]"
                            explanation {
                                key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                            }

                            step {
                                fromExpr = "2 * 9 * 8 * 9"
                                toExpr = "18 * 8 * 9"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                                }
                            }

                            step {
                                fromExpr = "18 * 8 * 9"
                                toExpr = "144 * 9"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                                }
                            }

                            step {
                                fromExpr = "144 * 9"
                                toExpr = "1296"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                                }
                            }
                        }
                    }
                }
            }

            step {
                fromExpr = "[9 * root[72, 4] / 2 * root[1296, 4]]"
                toExpr = "[9 * root[72, 4] / 2 * 6]"

                step {
                    fromExpr = "root[1296, 4]"
                    toExpr = "6"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRoot
                    }

                    step {
                        fromExpr = "root[1296, 4]"
                        toExpr = "root[[2 ^ 4] * [3 ^ 4], 4]"
                        explanation {
                            key = IntegerRootsExplanation.FactorizeNumberUnderSquareRoot
                        }
                    }

                    step {
                        fromExpr = "root[[2 ^ 4] * [3 ^ 4], 4]"
                        toExpr = "root[[2 ^ 4], 4] * root[[3 ^ 4], 4]"
                        explanation {
                            key = IntegerRootsExplanation.SplitRootOfProduct
                        }
                    }

                    step {
                        fromExpr = "root[[2 ^ 4], 4] * root[[3 ^ 4], 4]"
                        toExpr = "6"

                        step {
                            fromExpr = "root[[2 ^ 4], 4] * root[[3 ^ 4], 4]"
                            toExpr = "2 * 3"

                            step {
                                fromExpr = "root[[2 ^ 4], 4] * root[[3 ^ 4], 4]"
                                toExpr = "2 * root[[3 ^ 4], 4]"
                                explanation {
                                    key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                                }
                            }

                            step {
                                fromExpr = "2 * root[[3 ^ 4], 4]"
                                toExpr = "2 * 3"
                                explanation {
                                    key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                                }
                            }
                        }

                        step {
                            fromExpr = "2 * 3"
                            toExpr = "6"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                            }
                        }
                    }
                }
            }

            step {
                fromExpr = "[9 * root[72, 4] / 2 * 6]"
                toExpr = "[3 * root[72, 4] / 2 * 2]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }

                step {
                    fromExpr = "[9 * root[72, 4] / 2 * 6]"
                    toExpr = "[3 * 3 * root[72, 4] / 2 * 3 * 2]"
                    explanation {
                        key = FractionArithmeticExplanation.FindCommonFactorInFraction
                    }
                }

                step {
                    fromExpr = "[3 * 3 * root[72, 4] / 2 * 3 * 2]"
                    toExpr = "[3 * root[72, 4] / 2 * 2]"
                    explanation {
                        key = GeneralExplanation.CancelCommonTerms
                    }
                }
            }

            step {
                fromExpr = "[3 * root[72, 4] / 2 * 2]"
                toExpr = "[3 * root[72, 4] / 4]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }

                step {
                    fromExpr = "2 * 2"
                    toExpr = "4"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
        }
    }

    @Test
    fun testRationalizeHigherOrderRootWithPrimeRadicand() = testMethod {
        method = rationalizeDenominators
        inputExpr = "[1 / root[2, 3]]"
        check {
            fromExpr = "[1 / root[2, 3]]"
            toExpr = "[root[4, 3] / 2]"
            explanation {
                key = FractionRootsExplanation.RationalizeDenominator
            }

            step {
                fromExpr = "[1 / root[2, 3]]"
                toExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"

                step {
                    fromExpr = "[1 / root[2, 3]]"
                    toExpr = "[1 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                    explanation {
                        key = FractionRootsExplanation.HigherOrderRationalizingTerm
                    }
                }

                step {
                    fromExpr = "[1 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                    toExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"

                    step {
                        fromExpr = "[1 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                        toExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 3 - 1], 3]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }

                    step {
                        fromExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 3 - 1], 3]]"
                        toExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }
                }
            }

            step {
                fromExpr = "[1 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                toExpr = "[1 * root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyFractions
                }
            }

            step {
                fromExpr = "[1 * root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                toExpr = "[root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"

                step {
                    fromExpr = "1 * root[[2 ^ 2], 3]"
                    toExpr = "root[4, 3]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyProductWithRoots
                    }

                    step {
                        fromExpr = "1 * root[[2 ^ 2], 3]"
                        toExpr = "1 * root[4, 3]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                        }
                    }

                    step {
                        fromExpr = "1 * root[4, 3]"
                        toExpr = "root[4, 3]"
                        explanation {
                            key = GeneralExplanation.EliminateOneInProduct
                        }
                    }
                }
            }

            step {
                fromExpr = "[root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                toExpr = "[root[4, 3] / 2]"

                step {
                    fromExpr = "root[2, 3] * root[[2 ^ 2], 3]"
                    toExpr = "root[[2 ^ 1 + 2], 3]"

                    step {
                        fromExpr = "root[2, 3] * root[[2 ^ 2], 3]"
                        toExpr = "root[2 * [2 ^ 2], 3]"
                        explanation {
                            key = IntegerRootsExplanation.MultiplyNthRoots
                        }
                    }

                    step {
                        fromExpr = "root[2 * [2 ^ 2], 3]"
                        toExpr = "root[[2 ^ 1 + 2], 3]"
                        explanation {
                            key = IntegerRootsExplanation.CollectPowersOfExponentsWithSameBase
                        }
                    }
                }

                step {
                    fromExpr = "root[[2 ^ 1 + 2], 3]"
                    toExpr = "root[[2 ^ 3], 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }

                step {
                    fromExpr = "root[[2 ^ 3], 3]"
                    toExpr = "2"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                    }
                }
            }
        }
    }

    @Test
    fun testRationalizeHigherOrderRootWithPowerOfPrimeRadicand() = testMethod {
        method = rationalizeDenominators
        inputExpr = "[1 / root[[13^2], 3]]"

        check {
            fromExpr = "[1 / root[[13 ^ 2], 3]]"
            toExpr = "[root[13, 3] / 13]"
            explanation {
                key = FractionRootsExplanation.RationalizeDenominator
            }

            step {
                fromExpr = "[1 / root[[13 ^ 2], 3]]"
                toExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[13, 3]]"

                step {
                    fromExpr = "[1 / root[[13 ^ 2], 3]]"
                    toExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 3 - 2], 3] / root[[13 ^ 3 - 2], 3]]"
                    explanation {
                        key = FractionRootsExplanation.HigherOrderRationalizingTerm
                    }
                }

                step {
                    fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 3 - 2], 3] / root[[13 ^ 3 - 2], 3]]"
                    toExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[13, 3]]"

                    step {
                        fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 3 - 2], 3] / root[[13 ^ 3 - 2], 3]]"
                        toExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 1], 3] / root[[13 ^ 3 - 2], 3]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }

                    step {
                        fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 1], 3] / root[[13 ^ 3 - 2], 3]]"
                        toExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 1], 3] / root[[13 ^ 1], 3]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }

                    step {
                        fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[[13 ^ 1], 3] / root[[13 ^ 1], 3]]"
                        toExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[[13 ^ 1], 3]]"
                        explanation {
                            key = GeneralExplanation.EliminateLoneOneInExponent
                        }
                    }

                    step {
                        fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[[13 ^ 1], 3]]"
                        toExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[13, 3]]"
                        explanation {
                            key = GeneralExplanation.EliminateLoneOneInExponent
                        }
                    }
                }
            }

            step {
                fromExpr = "[1 / root[[13 ^ 2], 3]] * [root[13, 3] / root[13, 3]]"
                toExpr = "[1 * root[13, 3] / root[[13 ^ 2], 3] * root[13, 3]]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyFractions
                }
            }

            step {
                fromExpr = "[1 * root[13, 3] / root[[13 ^ 2], 3] * root[13, 3]]"
                toExpr = "[root[13, 3] / root[[13 ^ 2], 3] * root[13, 3]]"

                step {
                    fromExpr = "1 * root[13, 3]"
                    toExpr = "root[13, 3]"
                    explanation {
                        key = GeneralExplanation.EliminateOneInProduct
                    }
                }
            }

            step {
                fromExpr = "[root[13, 3] / root[[13 ^ 2], 3] * root[13, 3]]"
                toExpr = "[root[13, 3] / 13]"

                step {
                    fromExpr = "root[[13 ^ 2], 3] * root[13, 3]"
                    toExpr = "root[[13 ^ 2 + 1], 3]"

                    step {
                        fromExpr = "root[[13 ^ 2], 3] * root[13, 3]"
                        toExpr = "root[[13 ^ 2] * 13, 3]"
                        explanation {
                            key = IntegerRootsExplanation.MultiplyNthRoots
                        }
                    }

                    step {
                        fromExpr = "root[[13 ^ 2] * 13, 3]"
                        toExpr = "root[[13 ^ 2 + 1], 3]"
                        explanation {
                            key = IntegerRootsExplanation.CollectPowersOfExponentsWithSameBase
                        }
                    }
                }

                step {
                    fromExpr = "root[[13 ^ 2 + 1], 3]"
                    toExpr = "root[[13 ^ 3], 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }

                step {
                    fromExpr = "root[[13 ^ 3], 3]"
                    toExpr = "13"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                    }
                }
            }
        }
    }
}

class CollectRationalizingRadicalsTest {

    @Test
    fun testCollectRationalizingRadicalsTest1() = testMethod {
        method = collectRationalizingRadicals
        inputExpr = "root[2 * 19, 3] * root[ [2^2] * [19^2], 3]"

        check {
            fromExpr = "root[2 * 19, 3] * root[ [2^2] * [19^2], 3]"
            toExpr = "root[[2^1+2] * [19^1+2], 3]"
        }
    }

    @Test
    fun testCollectRationalizingRadicalsTest2() = testMethod {
        method = collectRationalizingRadicals
        inputExpr = "root[19, 3] * root[ [19^2], 3]"

        check {
            fromExpr = "root[19, 3] * root[ [19^2], 3]"
            toExpr = "root[[19^1 + 2], 3]"
        }
    }
}
