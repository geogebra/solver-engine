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

package methods.fractionarithmetic

import engine.methods.SolverEngineExplanation
import engine.methods.testMethod
import engine.steps.metadata.Skill
import methods.constantexpressions.ConstantExpressionsPlans
import org.junit.jupiter.api.Test

class TestAddFractions {
    @Test
    fun addLikeFractionsTest() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[1/5] + [2/5]"

            check {
                toExpr = "[3/5]"

                explanation {
                    key = Explanation.AddFractions

                    param {
                        expr = "[1/5]"
                    }
                    param {
                        expr = "[2/5]"
                    }
                }

                skill {
                    key = Skill.AddFractions

                    param {
                        expr = "[1/5]"
                    }
                    param {
                        expr = "[2/5]"
                    }
                }

                step {
                    toExpr = "[1 + 2/5]"

                    explanation {
                        key = Explanation.AddLikeFractions
                    }
                }

                step {
                    fromExpr = "[1 + 2/5]"
                    toExpr = "[3/5]"
                }
            }
        }

    @Test
    fun addUnlikeFractionsTest() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[1/3] + [2/5]"

            check {
                step { toExpr = "[1 * 5 / 3 * 5] + [2 * 3 / 5 * 3]" }
                step { toExpr = "[5 / 15] + [6 / 15]" }
                step { toExpr = "[5 + 6 / 15]" }
                step { toExpr = "[11 / 15]" }
            }
        }

    @Test
    fun testAddFractionsWithCommonFactor() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[1 / 4] + [1 / 4]"

            check {
                toExpr = "[1 / 2]"
            }
        }

    @Test
    fun testSumSimplifies() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[1 / 4] + [1 / 4]"

            check {
                step { toExpr = "[1 + 1 / 4]" }
                step { toExpr = "[2 / 4]" }
                step { toExpr = "[1 / 2]" }
            }
        }

    @Test
    fun testSumIsInteger() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[3 / 5] + [7 / 5]"

            check {
                step { toExpr = "[3 + 7 / 5]" }
                step { toExpr = "[10 / 5]" }
                step { toExpr = "2" }
            }
        }

    @Test
    fun testSumDoesNotSimplify() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[2 / 5] + [1 / 3]"

            check {
                step { toExpr = "[2 * 3 / 5 * 3] + [1 * 5 / 3 * 5]" }
                step { toExpr = "[6 / 15] + [5 / 15]" }
                step { toExpr = "[6 + 5 / 15]" }
                step { toExpr = "[11 / 15]" }
            }
        }

    @Test
    fun testSubtract() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[3 / 5] - [1 / 4]"

            check {
                toExpr = "[7 / 20]"
            }
        }

    @Test
    fun testAddNegatives() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "- [3 / 5] - [1 / 4]"

            check {
                toExpr = "-[17 / 20]"
            }
        }

    @Test
    fun testSumWithMoreTerms() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[1/2] + 3 - [1/3]"

            check {
                step {
                    explanation { key = SolverEngineExplanation.RearrangeSum }
                    toExpr = "<. [1/2] - [1/3] .> + 3"
                }
                step { toExpr = "<. [1*3/2*3] - [1*2/3*2] .> + 3" }
                step { toExpr = "<. [3/6] - [2/6] .> + 3" }
                step { toExpr = "[3 - 2 / 6] + 3" }
                step { toExpr = "[1/6] + 3" }
            }
        }

    @Test
    fun `test fraction sum with more terms at end`() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[1/2] + [1/3] + 3"

            check {
                invisibleStep {
                    explanation { key = SolverEngineExplanation.ExtractPartialExpression }
                    toExpr = "<. [1/2] + [1/3] .> + 3"
                }
                step { toExpr = "<. [1*3/2*3] + [1*2/3*2] .> + 3" }
                step { toExpr = "<. [3/6] + [2/6] .> + 3" }
                step { toExpr = "[3 + 2 / 6] + 3" }
                step { toExpr = "[5/6] + 3" }
            }
        }

    @Test
    fun testSumWithMoreTermsAtStart() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "5 + x + [1/2] + 3 - [1/3]"

            check {
                step {
                    explanation { key = SolverEngineExplanation.RearrangeSum }
                    toExpr = "5 + x + <. [1/2] - [1/3] .> + 3"
                }
                step { toExpr = "5 + x + <. [1*3/2*3] - [1*2/3*2] .> + 3" }
                step { toExpr = "5 + x + <. [3/6] - [2/6] .> + 3" }
                step { toExpr = "5 + x + [3 - 2 / 6] + 3" }
                step { toExpr = "5 + x + [1/6] + 3" }
            }
        }

    @Test
    fun testFractionNeedsCancelingFirst() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[1/3] + [4/8]"

            check {
                noTransformation()
            }
        }

    @Test
    fun testAvoidCancelingThatWouldBeUndone() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[2 / 2 * 3] + [1 / 6]"

            check {
                toExpr = "[1 / 2]"

                step { toExpr = "[2 / 6] + [1 / 6]" }
                step { toExpr = "[2 + 1 / 6]" }
                step { toExpr = "[3 / 6]" }
                step { toExpr = "[1 / 2]" }
            }
        }

    @Test
    fun testExplanationPointsAtCorrectTerms() =
        testMethod {
            method = addIntegerFractions
            inputExpr = "[4/8] + [1/2] + [1/3]"

            check {
                toExpr = "[4/8] + [5/6]"

                explanation {
                    key = FractionArithmeticExplanation.AddFractions
                    param { expr = "[1/2]" }
                    param { expr = "[1/3]" }
                }
            }
        }
}

class TestSimplifyFraction {
    @Test
    fun testToInteger() =
        testMethod {
            method = FractionArithmeticPlans.SimplifyFraction
            inputExpr = "[40 / 8]"

            check {
                toExpr = "5"
            }
        }

    @Test
    fun testWithDenominatorEqualTo1() =
        testMethod {
            method = FractionArithmeticPlans.SimplifyFraction
            inputExpr = "[7 / 1]"

            check {
                toExpr = "7"
            }
        }

    @Test
    fun testWithGCD() =
        testMethod {
            method = FractionArithmeticPlans.SimplifyFraction
            inputExpr = "[28 / 42]"

            check {
                step { toExpr = "[14 * 2 / 14 * 3]" }
                step { toExpr = "[2 / 3]" }
            }
        }

    @Test
    fun testAlreadyFactorized() =
        testMethod {
            method = FractionArithmeticPlans.SimplifyFraction
            inputExpr = "[3 * 4 / 4 * 5]"

            check {
                fromExpr = "[3 * 4 / 4 * 5]"
                toExpr = "[3 / 5]"
                explanation {
                    key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                }
            }
        }

    @Test
    fun testAlreadyPartiallyFactorized() =
        testMethod {
            method = FractionArithmeticPlans.SimplifyFraction
            inputExpr = "[14 * 12 / 21 * 6]"

            check {
                step { toExpr = "[7 * 2 * 12 / 7 * 3 * 6]" }
                step { toExpr = "[2 * 12 / 3 * 6]" }
                step { toExpr = "[2 * 12 / 3 * 2 * 3]" }
                step { toExpr = "[12 / 3 * 3]" }
                step { toExpr = "[3 * 4 / 3 * 3]" }
                step { toExpr = "[4 / 3]" }
            }
        }

    @Test
    fun testNoSimplification() =
        testMethod {
            method = FractionArithmeticPlans.SimplifyFraction
            inputExpr = "[3 / 4]"

            check {
                noTransformation()
            }
        }

    @Test
    fun testEvaluatePowerOfFraction() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[([3 / 4]) ^ 2]"

            check {
                step { toExpr = "[[3 ^ 2] / [4 ^ 2]]" }
                step { toExpr = "[9 / [4 ^ 2]]" }
                step { toExpr = "[9 / 16]" }
            }
        }

    @Test
    fun testEvaluatePositiveFractionPower() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[([2 / 3]) ^ 2]"

            check {
                toExpr = "[4 / 9]"
            }
        }
}
