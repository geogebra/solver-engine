package methods.constantexpressions

import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.fractionroots.FractionRootsExplanation
import methods.fractionroots.FractionRootsPlans
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerroots.IntegerRootsExplanation
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength", "ktlint:standard:max-line-length")
class RationalizationTest {
    @Test
    fun testCancelTermsBeforeRationalization() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[1 / root[3, 3] - root[3, 3]]"

            check {
                toExpr = "/undefined/"

                step {
                    toExpr = "[1 / 0]"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }

                step {
                    toExpr = "/undefined/"
                    explanation {
                        key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                    }
                }
            }
        }

    @Test
    fun testCancelUnitaryDenominator() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[sqrt[8] / 2]"

            check {
                toExpr = "sqrt[2]"
            }
        }

    @Test
    fun testRationalizationIntegerAndCubeRoot() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[2 / 2 - root[3, 3]]"

            check {
                toExpr = "[8 + 4 root[3, 3] + 2 root[9, 3] / 5]"
            }
        }

    @Test
    fun testSimplifyBeforeRationalizing() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[1 / sqrt[12] - sqrt[3]]"

            check {
                fromExpr = "[1 / sqrt[12] - sqrt[3]]"
                toExpr = "[sqrt[3] / 3]"

                step {
                    fromExpr = "[1 / sqrt[12] - sqrt[3]]"
                    toExpr = "[1 / 2 sqrt[3] - sqrt[3]]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRoot
                    }
                }

                step {
                    fromExpr = "[1 / 2 sqrt[3] - sqrt[3]]"
                    toExpr = "[1 / sqrt[3]]"
                    explanation {
                        key = CollectingExplanation.CollectLikeRootsAndSimplify
                    }
                }

                step {
                    fromExpr = "[1 / sqrt[3]]"
                    toExpr = "[sqrt[3] / 3]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeDenominator
                    }
                }
            }
        }

    @Test
    fun testRationalizeCubeRootDenominator1() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[2 / root[5, 3] + root[3, 3]]"

            check {
                toExpr = "[root[25, 3] - root[15, 3] + root[9, 3] / 4]"

                step {
                    toExpr = "[2 ([(root[5, 3]) ^ 2] - root[15, 3] + [(root[3, 3]) ^ 2]) / 8]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeDenominator
                    }
                }

                step {
                    toExpr = "[[(root[5, 3]) ^ 2] - root[15, 3] + [(root[3, 3]) ^ 2] / 4]"
                    explanation {
                        key = FractionArithmeticExplanation.SimplifyFraction
                    }
                }

                step {
                    toExpr = "[root[[5 ^ 2], 3] - root[15, 3] + root[[3 ^ 2], 3] / 4]"
                }

                step {
                    toExpr = "[root[25, 3] - root[15, 3] + root[[3 ^ 2], 3] / 4]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                    }
                }

                step {
                    toExpr = "[root[25, 3] - root[15, 3] + root[9, 3] / 4]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                    }
                }
            }
        }

    @Test
    fun testRationalizeCubeRootDenominator2() =
        testMethod {
            method = FractionRootsPlans.RationalizeDenominators
            inputExpr = "[2 / -root[5, 3] + root[3, 3]]"

            @Suppress("MaxLineLength")
            check {
                fromExpr = "[2 / -root[5, 3] + root[3, 3]]"
                toExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / -2]"
                explanation {
                    key = FractionRootsExplanation.RationalizeDenominator
                }

                step {
                    fromExpr = "[2 / -root[5, 3] + root[3, 3]]"
                    toExpr = "[2 / root[3, 3] - root[5, 3]]"
                    explanation {
                        key = FractionRootsExplanation.FlipRootsInDenominator
                    }
                }

                step {
                    fromExpr = "[2 / root[3, 3] - root[5, 3]]"
                    toExpr =
                        "[2 / root[3, 3] - root[5, 3]] * [[(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2] / [(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeSumOfIntegerAndCubeRoot
                    }
                }

                step {
                    fromExpr =
                        "[2 / root[3, 3] - root[5, 3]] * [[(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2] / [(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]]"
                    toExpr =
                        "[2 ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    fromExpr =
                        "[2 ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                    toExpr =
                        "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyProductWithRoots
                    }

                    step {
                        fromExpr = "root[3, 3] * root[5, 3]"
                        toExpr = "root[3 * 5, 3]"
                        explanation {
                            key = IntegerRootsExplanation.MultiplyNthRoots
                        }
                    }

                    step {
                        fromExpr = "root[3 * 5, 3]"
                        toExpr = "root[15, 3]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                        }

                        step {
                            fromExpr = "3 * 5"
                            toExpr = "15"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                            }
                        }
                    }
                }

                step {
                    fromExpr =
                        "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                    toExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / -2]"
                    explanation {
                        key = FractionRootsExplanation.SimplifyDenominatorAfterRationalization
                    }

                    step {
                        fromExpr =
                            "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / (root[3, 3] - root[5, 3]) ([(root[3, 3]) ^ 2] + root[3, 3] * root[5, 3] + [(root[5, 3]) ^ 2])]"
                        toExpr =
                            "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / [(root[3, 3]) ^ 3] - [(root[5, 3]) ^ 3]]"
                        explanation {
                            key = FractionRootsExplanation.IdentityCubeSumDifference
                        }
                    }

                    step {
                        fromExpr =
                            "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / [(root[3, 3]) ^ 3] - [(root[5, 3]) ^ 3]]"
                        toExpr =
                            "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / 3 - [(root[5, 3]) ^ 3]]"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyNthRootToThePowerOfN
                        }
                    }

                    step {
                        fromExpr =
                            "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / 3 - [(root[5, 3]) ^ 3]]"
                        toExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / 3 - 5]"
                        explanation {
                            key = IntegerRootsExplanation.SimplifyNthRootToThePowerOfN
                        }
                    }

                    step {
                        fromExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / 3 - 5]"
                        toExpr = "[2 ([(root[3, 3]) ^ 2] + root[15, 3] + [(root[5, 3]) ^ 2]) / -2]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                        }

                        step {
                            fromExpr = "3 - 5"
                            toExpr = "-2"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                            }
                        }
                    }
                }
            }
        }

    @Test
    fun testRationalizeHigherOrderRoot1() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[2 / root[2, 3]]"

            check {
                fromExpr = "[2 / root[2, 3]]"
                toExpr = "root[4, 3]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "[2 / root[2, 3]]"
                    toExpr = "[2 root[4, 3] / 2]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeDenominator
                    }

                    step {
                        fromExpr = "[2 / root[2, 3]]"
                        toExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                        explanation {
                            key = FractionRootsExplanation.RationalizeHigherOrderRoot
                        }

                        step {
                            fromExpr = "[2 / root[2, 3]]"
                            toExpr = "[2 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                            explanation {
                                key = FractionRootsExplanation.HigherOrderRationalizingTerm
                            }
                        }

                        step {
                            fromExpr = "[2 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                            toExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                            explanation {
                                key = FractionRootsExplanation.SimplifyRationalizingTerm
                            }

                            step {
                                fromExpr = "[2 / root[2, 3]] * [root[[2 ^ 3 - 1], 3] / root[[2 ^ 3 - 1], 3]]"
                                toExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 3 - 1], 3]]"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                                }
                            }

                            step {
                                fromExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 3 - 1], 3]]"
                                toExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                                }
                            }
                        }
                    }

                    step {
                        fromExpr = "[2 / root[2, 3]] * [root[[2 ^ 2], 3] / root[[2 ^ 2], 3]]"
                        toExpr = "[2 root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyFractions
                        }
                    }

                    step {
                        fromExpr = "[2 root[[2 ^ 2], 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        toExpr = "[2 root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                        }
                    }

                    step {
                        fromExpr = "[2 root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                        toExpr = "[2 root[4, 3] / 2]"
                        explanation {
                            key = FractionRootsExplanation.SimplifyDenominatorAfterRationalization
                        }

                        step {
                            fromExpr = "[2 root[4, 3] / root[2, 3] * root[[2 ^ 2], 3]]"
                            toExpr = "[2 root[4, 3] / root[[2 ^ 3], 3]]"
                            explanation {
                                key = FractionRootsExplanation.CollectRationalizingRadicals
                            }

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
                                    key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                                }
                            }

                            step {
                                fromExpr = "root[[2 ^ 1 + 2], 3]"
                                toExpr = "root[[2 ^ 3], 3]"
                                explanation {
                                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                                }
                            }
                        }

                        step {
                            fromExpr = "[2 root[4, 3] / root[[2 ^ 3], 3]]"
                            toExpr = "[2 root[4, 3] / 2]"
                            explanation {
                                key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                            }
                        }
                    }
                }

                step {
                    fromExpr = "[2 root[4, 3] / 2]"
                    toExpr = "root[4, 3]"
                    explanation {
                        key = GeneralExplanation.CancelDenominator
                    }
                }
            }
        }
}
