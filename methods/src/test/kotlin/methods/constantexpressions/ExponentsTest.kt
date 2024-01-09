/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods.constantexpressions

import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerrationalexponents.IntegerRationalExponentsExplanation
import methods.integerroots.IntegerRootsExplanation
import org.junit.jupiter.api.Test

class ExponentsTest {
    @Test
    fun `test simplify -ve constant base to even power`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[(-sqrt[2])^6]"

            check {
                fromExpr = "[(-sqrt[2]) ^ 6]"
                toExpr = "8"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "[(-sqrt[2]) ^ 6]"
                    toExpr = "[2 ^ 3]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyRootsInExpression
                    }

                    step {
                        fromExpr = "[(-sqrt[2]) ^ 6]"
                        toExpr = "[(sqrt[2]) ^ 6]"
                        explanation {
                            key = GeneralExplanation.SimplifyEvenPowerOfNegative
                        }
                    }

                    step {
                        fromExpr = "[(sqrt[2]) ^ 6]"
                        toExpr = "[2 ^ 3]"
                        explanation {
                            key = IntegerRootsExplanation.CancelPowerOfARoot
                        }
                    }
                }

                step {
                    fromExpr = "[2 ^ 3]"
                    toExpr = "8"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerPowerDirectly
                    }
                }
            }
        }

    @Test
    fun `test simplify -ve constant base to odd power`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[(-2 sqrt[2])^3]"

            check {
                fromExpr = "[(-2 sqrt[2]) ^ 3]"
                toExpr = "-16 sqrt[2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "[(-2 sqrt[2]) ^ 3]"
                    toExpr = "-8 * 2 sqrt[2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyPowerOfProduct
                    }

                    step {
                        fromExpr = "[(-2 sqrt[2]) ^ 3]"
                        toExpr = "-[(2 sqrt[2]) ^ 3]"
                        explanation {
                            key = GeneralExplanation.SimplifyOddPowerOfNegative
                        }
                    }

                    step {
                        fromExpr = "-[(2 sqrt[2]) ^ 3]"
                        toExpr = "-[2 ^ 3] [(sqrt[2]) ^ 3]"
                        explanation {
                            key = GeneralExplanation.DistributePowerOfProduct
                        }
                    }

                    step {
                        fromExpr = "-[2 ^ 3] [(sqrt[2]) ^ 3]"
                        toExpr = "-8 [(sqrt[2]) ^ 3]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                        }
                    }

                    step {
                        fromExpr = "-8 [(sqrt[2]) ^ 3]"
                        toExpr = "-8 sqrt[[2 ^ 3]]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyRootsInExpression
                        }
                    }

                    step {
                        fromExpr = "-8 sqrt[[2 ^ 3]]"
                        toExpr = "-8 * <. 2 sqrt[2] .>"
                        explanation {
                            key = IntegerRootsExplanation.SplitAndCancelRootOfPower
                        }
                    }
                }

                step {
                    fromExpr = "-8 * 2 sqrt[2]"
                    toExpr = "-16 sqrt[2]"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
        }

    @Test
    fun testNegativeExponentsOfIntegers() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[3 ^ -1] - [3 ^ -2]"

            check {
                step { toExpr = "[1 / 3] - [3 ^ -2]" }
                step { toExpr = "[1 / 3] - [1 / 9]" }
                step { toExpr = "[2 / 9]" }
            }
        }

    @Test
    fun testFractionToTheMinusOne() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[([1 / 3])^-1] * [2 ^ -2]"

            check {
                toExpr = "[3 / 4]"

                step { toExpr = "[3 / 1] * [2 ^ -2]" }

                step { toExpr = "3 * [2 ^ -2]" }

                step { toExpr = "3 * [1 / 4]" }

                step { toExpr = "[3 / 4]" }
            }
        }

    @Test
    fun testFractionExponent() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[(-[1 / 2]) ^ 3] + [([2 / 3]) ^ -2]"

            check {
                step { toExpr = "- [1 / 8] + [([2/3])^-2]" }
                step { toExpr = "- [1 / 8] + [9 / 4]" }
                step { toExpr = "[17 / 8]" }
            }
        }

    @Test
    fun testEvaluateExpressionToThePowerOfOneComplexExpression() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[ (sqrt[2] + 1) ^ [5 / 5]]  + 1"

            check {
                toExpr = "sqrt[2] + 2"

                step {
                    toExpr = "[(sqrt[2] + 1) ^ 1] + 1"
                }

                step {
                    toExpr = "<. sqrt[2] + 1 .> + 1"
                    explanation {
                        key = GeneralExplanation.SimplifyExpressionToThePowerOfOne
                    }
                }

                step { }
            }
        }

    @Test
    fun testEvaluateExpressionToThePowerOfOneSimpleExpr() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[2 ^ 1] + [5 ^ 1]"

            check {
                toExpr = "7"

                step {
                    fromExpr = "[2 ^ 1] + [5 ^ 1]"
                    toExpr = "2 + [5 ^ 1]"
                    explanation {
                        key = GeneralExplanation.SimplifyExpressionToThePowerOfOne
                    }
                }

                step {
                    fromExpr = "2 + [5 ^ 1]"
                    toExpr = "2 + 5"
                    explanation {
                        key = GeneralExplanation.SimplifyExpressionToThePowerOfOne
                    }
                }

                step {
                    fromExpr = "2 + 5"
                    toExpr = "7"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }
            }
        }

    @Test
    fun testZeroToNegativePower() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[0 ^ -[3 / 2]]"

            check {
                toExpr = "/undefined/"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                }

                step {
                    toExpr = "[([1 / 0]) ^ [3 / 2]]"
                    explanation {
                        key = FractionArithmeticExplanation.TurnNegativePowerOfZeroToPowerOfFraction
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
    fun testProductOfExponentsSameBase1() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[20 ^ 2] * [20 ^ -2]"

            check {
                toExpr = "1"

                step {
                    fromExpr = "[20 ^ 2] * [20 ^ -2]"
                    toExpr = "[20 ^ 0]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                    }

                    step {
                        fromExpr = "[20 ^ 2] * [20 ^ -2]"
                        toExpr = "[20 ^ 2 - 2]"
                        explanation {
                            key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                        }
                    }

                    step {
                        fromExpr = "[20 ^ 2 - 2]"
                        toExpr = "[20 ^ 0]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }
                }

                step {
                    fromExpr = "[20 ^ 0]"
                    toExpr = "1"
                    explanation {
                        key = GeneralExplanation.EvaluateExpressionToThePowerOfZero
                    }
                }
            }
        }

    @Test
    fun testProductOfExponentsSameBase2() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[20 ^ 2] * [20 ^ -3]"

            check {
                toExpr = "[1 / 20]"

                step {
                    fromExpr = "[20 ^ 2] * [20 ^ -3]"
                    toExpr = "[20 ^ -1]"
                    explanation {
                        key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                    }
                }

                step { }
            }
        }
}
