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

package methods.general

import engine.methods.testMethod
import engine.methods.testRule
import methods.general.GeneralRules.CancelAdditiveInverseElements
import methods.general.GeneralRules.DistributePowerOfProduct
import methods.general.GeneralRules.DistributeSumOfPowers
import methods.general.GeneralRules.EliminateZeroInSum
import methods.general.GeneralRules.EvaluateExpressionToThePowerOfZero
import methods.general.GeneralRules.EvaluateOneToAnyPower
import methods.general.GeneralRules.EvaluateProductContainingZero
import methods.general.GeneralRules.EvaluateProductDividedByZeroAsUndefined
import methods.general.GeneralRules.EvaluateZeroToAPositivePower
import methods.general.GeneralRules.FactorizeInteger
import methods.general.GeneralRules.FlipFractionUnderNegativePower
import methods.general.GeneralRules.MoveSignOfNegativeFactorOutOfProduct
import methods.general.GeneralRules.RewriteFractionOfPowersWithSameBase
import methods.general.GeneralRules.RewriteFractionOfPowersWithSameExponent
import methods.general.GeneralRules.RewritePowerAsProduct
import methods.general.GeneralRules.RewriteProductOfPowersWithInverseBase
import methods.general.GeneralRules.RewriteProductOfPowersWithInverseFractionBase
import methods.general.GeneralRules.RewriteProductOfPowersWithNegatedExponent
import methods.general.GeneralRules.RewriteProductOfPowersWithSameBase
import methods.general.GeneralRules.RewriteProductOfPowersWithSameExponent
import methods.general.GeneralRules.SimplifyDoubleMinus
import methods.general.GeneralRules.SimplifyEvenPowerOfNegative
import methods.general.GeneralRules.SimplifyExpressionToThePowerOfOne
import methods.general.GeneralRules.SimplifyNonObviousZeroNumeratorFractionToZero
import methods.general.GeneralRules.SimplifyOddPowerOfNegative
import methods.general.GeneralRules.SimplifyProductWithTwoNegativeFactors
import methods.general.GeneralRules.SimplifyUnitFractionToOne
import methods.general.GeneralRules.SimplifyZeroDenominatorFractionToUndefined
import methods.general.GeneralRules.SimplifyZeroNumeratorFractionToZero
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertTrue
import engine.methods.SerializedGmAction as GmAction

@Suppress("LargeClass")
class GeneralRulesTest {
    @Test
    fun testRemoveUnitaryCoefficient() {
        testMethod {
            method = GeneralRules.RemoveUnitaryCoefficient
            inputExpr = "1x"

            check {
                toExpr = "x"
                shift("./1", ".")
                cancel("./0", "./0:outerOp")
            }
        }

        testRule(
            "3*x*1*y*4",
            GeneralRules.RemoveUnitaryCoefficient,
            "3xy*4",
            GmAction("Tap", "./2"),
        )
        testRule("3*x*1*y*4", GeneralRules.RemoveUnitaryCoefficient, "3xy*4")
        testRule("3*1*4", GeneralRules.RemoveUnitaryCoefficient, "3*4")
        testRule("1*x", GeneralRules.RemoveUnitaryCoefficient, "x")

        testMethod {
            method = GeneralRules.RemoveUnitaryCoefficient
            inputExpr = "3x*1*y"

            check {
                toExpr = "3xy"

                shift("./0", "./0")
                shift("./1", "./1")
                cancel("./2", "./2:outerOp")
                shift("./3", "./2")
            }
        }
    }

    @Test
    fun testSimplifyProductWithTwoNegativeFactors() {
        testRule(
            "(-2) * (-3)",
            SimplifyProductWithTwoNegativeFactors,
            "2 * 3",
            GmAction("Drag", "./1:op", "./0:op"),
        )
        testRule(
            "-3*(-a)",
            SimplifyProductWithTwoNegativeFactors,
            "3 a",
            GmAction("Drag", "./0/1:op", ".:op"),
        )
        testRule("(-x) * y * (-12) * 5", SimplifyProductWithTwoNegativeFactors, "x y * 12 * 5")
        testRule("-x * y * (-12) * 5", SimplifyProductWithTwoNegativeFactors, "x y * 12 * 5")
        testRule("(-2):(-3)", SimplifyProductWithTwoNegativeFactors, "2:3")
        testRule("-2:(-3)", SimplifyProductWithTwoNegativeFactors, "2:3")
        testRule("-2:-3", SimplifyProductWithTwoNegativeFactors, "2:3")
        testRule("-(-42) sqrt[6]", SimplifyProductWithTwoNegativeFactors, "42 sqrt[6]")

        testMethod {
            method = SimplifyProductWithTwoNegativeFactors
            inputExpr = "(-2)(-x)"

            check {
                toExpr = "2x"
                shift("./0/0", "./0")
                shift("./1/0", "./1")
                cancel("./0:op", "./1:op")
            }
        }

        testMethod {
            method = SimplifyProductWithTwoNegativeFactors
            inputExpr = "-2(-x)"

            check {
                toExpr = "2x"
                shift("./0/0", "./0")
                shift("./0/1/0", "./1")
                cancel(".:op", "./0/1:op")
            }
        }
    }

    @Test
    fun testMoveSignOfNegativeFactorOutOfProduct() {
        testRule("x * (-y) * z", MoveSignOfNegativeFactorOutOfProduct, "-xyz")

        testMethod {
            method = MoveSignOfNegativeFactorOutOfProduct
            inputExpr = "3 * (-5)"

            check {
                toExpr = "- 3 * 5"

                move("./1:op", ".:op")
                shift("./0", "./0/0")
                shift("./1/0", "./0/1")
            }
        }
    }

    @Test
    fun testEliminateZeroInSum() {
        testRule("0 + x + y", EliminateZeroInSum, "x + y")
        testRule("0 + 1 + x", EliminateZeroInSum, "1 + x")
        testRule("x + 0 + y", EliminateZeroInSum, "x + y")
        testRule("x + 0", EliminateZeroInSum, "x")
        testRule("1 + 0", EliminateZeroInSum, "1")
        testRule("1 - 0", EliminateZeroInSum, "1")
        testRule("-0 - x + y", EliminateZeroInSum, "-x + y")
        testRule("z +/- 0", EliminateZeroInSum, "z")
        testRule("+/-0 + 1 + 2", EliminateZeroInSum, "1 + 2")

        testMethod {
            method = EliminateZeroInSum
            inputExpr = "x + y + 0"

            check {
                toExpr = "x + y"

                keep("./0", "./1")
                cancel("./2", "./2:outerOp")
            }
        }

        testMethod {
            method = EliminateZeroInSum
            inputExpr = "0 + x + y"

            check {
                toExpr = "x + y"
                shift("./1", "./0")
                shift("./2", "./1")
                cancel("./0", "./0:outerOp")
            }
        }
    }

    @Test
    fun testEvaluateProductContainingZero() {
        testRule("z*x*0", EvaluateProductContainingZero, "0")
        testRule("0*1", EvaluateProductContainingZero, "0")
        testRule("(-2)*0*x", EvaluateProductContainingZero, "0")
        testRule("0:1", EvaluateProductContainingZero, null)
        testRule("0:(1+1)", EvaluateProductContainingZero, null)
        testRule("0 * [1 / 1 + 1] * 3", EvaluateProductContainingZero, "0")
        testRule("-4*sqrt[3]*0", EvaluateProductContainingZero, "0")
        testRule("0 * [1 / 1 - 1] * 3", EvaluateProductContainingZero, null)

        testMethod {
            method = EvaluateProductContainingZero
            inputExpr = "sqrt[2]*0*x*11"

            check {
                toExpr = "0"

                move("./1", ".")
            }
        }
    }

    @Test
    fun testSimplifyZeroNumeratorFractionToZero() {
        testRule("[0 / -1]", SimplifyZeroNumeratorFractionToZero, "0")
        testRule("[0 / root[3, 3] + root[5, 3]]", SimplifyZeroNumeratorFractionToZero, "0")
        testRule("[0 / 3 * (sqrt[2] - 1)]", SimplifyZeroNumeratorFractionToZero, "0")
        testRule("[0 / 1 - 1]", SimplifyZeroNumeratorFractionToZero, null)
        testRule("[0 / ln3 - ln2]", SimplifyZeroNumeratorFractionToZero, null)

        testMethod {
            method = SimplifyZeroNumeratorFractionToZero
            inputExpr = "[0 / 2]"

            check {
                toExpr = "0"

                transform(".", ".")
            }
        }
    }

    @Test
    fun testSimplifyNonObviousZeroNumeratorFractionToZero() {
        testRule("[0 / -1]", SimplifyNonObviousZeroNumeratorFractionToZero, "0")
        testRule("[0 / root[3, 3] + root[5, 3]]", SimplifyNonObviousZeroNumeratorFractionToZero, "0")
        testRule("[0 / 3 * (sqrt[2] - 1)]", SimplifyNonObviousZeroNumeratorFractionToZero, "0")
        testRule("[0 / 1 - 1]", SimplifyNonObviousZeroNumeratorFractionToZero, null)
        testRule("[0 / ln3 - ln2]", SimplifyNonObviousZeroNumeratorFractionToZero, "0")
    }

    @Test
    fun testSimplifyUnitFractionToOne() {
        testRule("[2 - 2 / 2 - 2]", SimplifyUnitFractionToOne, null)

        testMethod {
            method = SimplifyUnitFractionToOne
            inputExpr = "[sqrt[2] + sqrt[3] / sqrt[2] + sqrt[3]]"

            check {
                toExpr = "1"

                transform(".", ".")
            }
        }
    }

    @Test
    fun testSimplifyFractionWithOneDenominator() {
        testMethod {
            method = GeneralRules.SimplifyFractionWithOneDenominator
            inputExpr = "[x + 1 / 1]"

            check {
                toExpr = "x + 1"
                shift("./0", ".")
                cancel("./1")
            }
        }
    }

    @Test
    fun testSimplifyZeroDenominatorFractionToUndefined() {
        testRule("[sqrt[2] / 0]", SimplifyZeroDenominatorFractionToUndefined, "/undefined/")

        testMethod {
            method = SimplifyZeroDenominatorFractionToUndefined
            inputExpr = "[2 / 0]"

            check {
                toExpr = "/undefined/"

                transform(".", ".")
            }
        }
    }

    @Test
    fun testCancelDenominator() {
        testMethod {
            method = GeneralRules.CancelDenominator
            inputExpr = "[5 * 3 / 5]"

            check {
                shift("./0/1", ".")
                cancel("./0/0", "./1")
            }
        }
    }

    @Test
    fun testFactorMinusFromSumWithAllNegativeTerms() {
        testMethod {
            method = GeneralRules.FactorMinusFromSumWithAllNegativeTerms
            inputExpr = "-1 - sqrt[2] - root[3, 3]"

            check {
                toExpr = "-(1 + sqrt[2] + root[3, 3])"

                factor {
                    fromPaths("./0:op", "./1:op", "./2:op")
                    toPaths(".:op")
                }
            }
        }
    }

    @Test
    fun testFactorMinusFromSum() {
        testRule("-x + 1", GeneralRules.FactorMinusFromSum, "-(x - 1)")
        testRule("x + 1", GeneralRules.FactorMinusFromSum, null)
        testRule("-x + 1", GeneralRules.FactorMinusFromSum, "-(x - 1)")
        testRule("x - 1", GeneralRules.FactorMinusFromSum, null)
        testRule("1 - sqrt[2]", GeneralRules.FactorMinusFromSum, "-(-1 + sqrt[2])")
        testRule("-1 + sqrt[2]", GeneralRules.FactorMinusFromSum, null)
        testRule("x - sqrt[2]", GeneralRules.FactorMinusFromSum, null)
        testRule("-x + sqrt[2]", GeneralRules.FactorMinusFromSum, "-(x - sqrt[2])")
        testRule("sqrt[2] - x", GeneralRules.FactorMinusFromSum, "-(-sqrt[2] + x)")
        testRule("-sqrt[2] + x", GeneralRules.FactorMinusFromSum, null)
        testRule("(1 - sqrt[2])x + 1", GeneralRules.FactorMinusFromSum, null)
    }

    @Test
    fun testSimplifyProductOfConjugates() {
        testMethod {
            method = GeneralRules.SimplifyProductOfConjugates
            inputExpr = "(sqrt[2] + 1)(sqrt[2] - 1)"

            check {
                transform(".", ".")
            }
        }
    }

    @Test
    fun testSimplifyDoubleMinus() {
        testRule("-(-5)", SimplifyDoubleMinus, "5")

        testMethod {
            method = SimplifyDoubleMinus
            inputExpr = "-(-x)"

            check {
                toExpr = "x"

                shift("./0/0", ".")
                cancel(".:op", "./0:op")
            }
        }
    }

    @Test
    fun testDistributePowerOfProduct() {
        testMethod {
            method = DistributePowerOfProduct
            inputExpr = "[(2 * x * y) ^ 5]"

            check {
                toExpr = "[2 ^ 5] [x ^ 5] [y ^ 5]"

                distribute {
                    fromPaths("./1")
                    toPaths("./0/1", "./1/1", "./2/1")
                }
                move("./0/0", "./0/0")
                move("./0/1", "./1/0")
                move("./0/2", "./2/0")
            }
        }
        testRule(
            "[(sqrt[3] * root[5, 2]) ^ n]",
            DistributePowerOfProduct,
            "[(sqrt[3]) ^ n] * [(root[5, 2]) ^ n]",
        )
        testRule(
            "[([(sqrt[2]) ^ 3] * 9 [a ^ 2]) ^ [1 / 2]]",
            DistributePowerOfProduct,
            "[([(sqrt[2]) ^ 3]) ^ [1 / 2]] * [9 ^ [1 / 2]] [([a ^ 2]) ^ [1 / 2]]",
        )
    }

    @Test
    fun testMultiplyExponentsUsingPowerRule() {
        testMethod {
            method = GeneralRules.MultiplyExponentsUsingPowerRule
            inputExpr = "[([x^2])^4]"

            check {
                toExpr = "[x^2*4]"
                shift("./0/0", "./0")
                move("./0/1", "./1/0")
                move("./1", "./1/1")
            }
        }
    }

    @Test
    fun testDistributeSumOfPowers() {
        testMethod {
            method = DistributeSumOfPowers
            inputExpr = "[2 ^ a + b + c]"

            check {
                toExpr = "[2^a] * [2^b] * [2^c]"

                distribute {
                    fromPaths("./0")
                    toPaths("./0/0", "./1/0", "./2/0")
                }
                move("./1/0", "./0/1")
                move("./1/1", "./1/1")
                move("./1/2", "./2/1")
            }
        }
    }

    @Test
    fun testRewritePowerAsProduct() {
        testMethod {
            method = RewritePowerAsProduct
            inputExpr = "[2^3]"

            check {
                toExpr = "2 * 2 * 2"
                distribute {
                    fromPaths("./0")
                    toPaths("./0", "./1", "./2")
                }
            }
        }

        testRule("[3^3]", RewritePowerAsProduct, "3 * 3 * 3")
        testRule("[0.3 ^ 2]", RewritePowerAsProduct, "0.3 * 0.3")
        testRule("[(x + 1) ^ 2]", RewritePowerAsProduct, "(x + 1) (x + 1)")
        testRule("[x^5]", RewritePowerAsProduct, "x * x * x * x * x")
        testRule("[x^6]", RewritePowerAsProduct, null)
        testRule("[x^1]", RewritePowerAsProduct, null)
        testRule("[x^0]", RewritePowerAsProduct, null)
        testRule("[([1/2])^4]", RewritePowerAsProduct, "[1/2] * [1/2] * [1/2] * [1/2]")
        testRule(
            "[(4[x^2] - 3) ^ 3]",
            RewritePowerAsProduct,
            "(4[x^2] - 3) (4[x^2] - 3) (4[x^2] - 3)",
        )
    }

    @Test
    fun testEvaluateProductDividedByZeroAsUndefined() {
        testRule("7 : 0.00", EvaluateProductDividedByZeroAsUndefined, "/undefined/")
        testRule("x : 0 * y", EvaluateProductDividedByZeroAsUndefined, "/undefined/")
        testMethod {
            method = EvaluateProductDividedByZeroAsUndefined
            inputExpr = "3 * 5 : 0"

            check {
                toExpr = "/undefined/"
                transform(".", ".")
            }
        }
    }

    @Test
    fun testCancelAdditiveInverseElements() {
        testRule("sqrt[12] - sqrt[12] + 1", CancelAdditiveInverseElements, "1")
        testRule(
            "(sqrt[2] + root[3, 3])  + 1 - (sqrt[2] + root[3, 3]) + 2",
            CancelAdditiveInverseElements,
            "1 + 2",
        )
        testRule("sqrt[12] - sqrt[12]", CancelAdditiveInverseElements, "0")
        testRule("-sqrt[12] + sqrt[12]", CancelAdditiveInverseElements, "0")
        testRule("(x + 1 - y) - (x + 1 - y)", CancelAdditiveInverseElements, "0")

        testMethod {
            method = CancelAdditiveInverseElements
            inputExpr = "2 + 1 - 2"

            check {
                toExpr = "1"

                shift("./1", ".")
                cancel("./0", "./0:outerOp", "./2/0", "./2/0:outerOp")
            }
        }
    }

    @Test
    fun testSimplifyPowerOfNegative() {
        testMethod {
            method = SimplifyEvenPowerOfNegative
            inputExpr = "[(-2)^4]"

            check {
                toExpr = "[2^4]"
                shift("./0/0", "./0")
                shift("./1", "./1")
                cancel("./0:op", "./1")
            }
        }
        testRule("[(-x)^6]", SimplifyEvenPowerOfNegative, "[x^6]")

        testMethod {
            method = SimplifyOddPowerOfNegative
            inputExpr = "[(-2)^5]"

            check {
                toExpr = "-[2^5]"

                transform(".", ".")
            }
        }
        testRule("[(-x)^7]", SimplifyOddPowerOfNegative, "-[x^7]", GmAction("Drag", "./1", "./0/0"))
        testRule("[(-[1 / 2]) ^ 3]", SimplifyOddPowerOfNegative, "-[([1 / 2]) ^ 3]")
    }

    @Test
    fun testRewriteEvenPowerOfBaseAsEvenPowerOfAbsoluteValueOfBase() {
        testRule(
            "[y ^ 6]",
            GeneralRules.RewriteEvenPowerOfBaseAsEvenPowerOfAbsoluteValueOfBase,
            "[abs[y] ^ 6]",
        )
    }

    @Test
    fun testSimplifyExpressionToThePowerOfOne() {
        testRule("[(sqrt[2] + 1) ^ 1]", SimplifyExpressionToThePowerOfOne, "sqrt[2] + 1")
        testRule("[2 ^ 1]", SimplifyExpressionToThePowerOfOne, "2")
        testMethod {
            method = SimplifyExpressionToThePowerOfOne
            inputExpr = "[x ^ 1]"

            check {
                toExpr = "x"

                cancel("./1")
                shift("./0", ".")
            }
        }
    }

    @Test
    fun testEvaluateOneToAnyPower() {
        testRule("[1 ^ [1 / 1 - 1]]", EvaluateOneToAnyPower, null)

        testMethod {
            method = EvaluateOneToAnyPower
            inputExpr = "[1 ^ sqrt[2] + 1]"

            check {
                toExpr = "1"

                move("./0", ".")
            }
        }
    }

    @Test
    fun testEvaluateZeroToThePowerOfZero() {
        testMethod {
            method = GeneralRules.EvaluateZeroToThePowerOfZero
            inputExpr = "[0 ^ 0]"

            check {
                toExpr = "/undefined/"

                transform(".", ".")
            }
        }
    }

    @Test
    fun testEvaluateExpressionToThePowerOfZero() {
        testMethod {
            method = EvaluateExpressionToThePowerOfZero
            inputExpr = "[(sqrt[2] + 1) ^ 0]"

            check {
                toExpr = "1"

                transform(".", ".")
            }
        }
        testRule("[(1 - 1) ^ 0]", EvaluateExpressionToThePowerOfZero, null)
    }

    @Test
    fun testEvaluateZeroToAPositivePower() {
        testMethod {
            method = EvaluateZeroToAPositivePower
            inputExpr = "[0 ^ [3 / 2]]"

            check {
                toExpr = "0"

                move("./0", ".")
            }
        }
        testRule("[0 ^ sqrt[3] - sqrt[5]]", EvaluateZeroToAPositivePower, null)
    }

    @Test
    fun testRewriteProductOfPowersWithSameBase() {
        testMethod {
            method = RewriteProductOfPowersWithSameBase
            inputExpr = "[x^2] * [x^3]"

            check {
                toExpr = "[x^2 + 3]"

                factor {
                    fromPaths("./0/0", "./1/0")
                    toPaths("./0")
                }
                move("./0/1", "./1/0")
                move("./1/1", "./1/1")
            }
        }
        testRule("[x^2]*[y^2]", RewriteProductOfPowersWithSameBase, null)
        testRule("x*[3^4]*[3^-9]", RewriteProductOfPowersWithSameBase, "x*[3 ^ 4 - 9]")
        testRule("y*[3^[1 / 2]]*z*[3^[2 / 3]]", RewriteProductOfPowersWithSameBase, "y*[3 ^ [1 / 2] + [2 / 3]]z")
    }

    @Test
    fun testRewriteProductOfPowersWithSameExponent() {
        testMethod {
            method = RewriteProductOfPowersWithSameExponent
            inputExpr = "[2 ^ [1 / 5]] * [3 ^ [1 / 5]]"

            check {
                toExpr = "[(2*3) ^ [1 / 5]]"

                move("./0/0", "./0/0")
                move("./1/0", "./0/1")
                factor {
                    fromPaths("./0/1", "./1/1")
                    toPaths("./1")
                }
            }
        }
        testRule("[x^2]*[x^3]", RewriteProductOfPowersWithSameExponent, null)
        testRule("[x^2]*[y^2]", RewriteProductOfPowersWithSameExponent, "[(x y) ^ 2]")
        testRule("x*[3^4]*[2^4]", RewriteProductOfPowersWithSameExponent, "x*[(3 * 2) ^ 4]")
        testRule("y*[3^[2 / 3]]*z*[4^[2 / 3]]", RewriteProductOfPowersWithSameExponent, "y*[(3 * 4) ^ [2 / 3]]*z")
    }

    @Test
    fun testRewriteFractionOfPowersWithSameBase() {
        testMethod {
            method = RewriteFractionOfPowersWithSameBase
            inputExpr = "[[2^[1/2]] / [2^[1/3]]]"

            check {
                toExpr = "[2 ^ [1/2] - [1/3]]"

                factor {
                    fromPaths("./0/0", "./1/0")
                    toPaths("./0")
                }
                move("./0/1", "./1/0")
                move("./1/1", "./1/1/0")
            }
        }
        testRule("[[x^2] / [y^2]]", RewriteFractionOfPowersWithSameBase, null)
        testRule("[[x^2] / [x^3]]", RewriteFractionOfPowersWithSameBase, "[x ^ 2 - 3]")
        testRule("[[3^4] / [3^-9]]", RewriteFractionOfPowersWithSameBase, "[3 ^ 4 -(- 9)]")
        testRule("[[3^[1 / 2]] / [3^[2 / 3]]]", RewriteFractionOfPowersWithSameBase, "[3 ^ [1 / 2] - [2 / 3]]")
    }

    @Test
    fun testRewriteFractionOfPowersWithSameExponent() {
        testMethod {
            method = RewriteFractionOfPowersWithSameExponent
            inputExpr = "[[2^[1/3]] / [5^[1/3]]]"

            check {
                toExpr = "[ ([2/5]) ^ [1/3] ]"

                factor {
                    fromPaths("./0/1", "./1/1")
                    toPaths("./1")
                }
                move("./0/0", "./0/0")
                move("./1/0", "./0/1")
            }
        }
        testRule("[[x^2] / [x^3]]", RewriteFractionOfPowersWithSameExponent, null)
        testRule("[[x^2] / [y^2]]", RewriteFractionOfPowersWithSameExponent, "[([x / y]) ^ 2]")
        testRule("[[3^4] / [2^4]]", RewriteFractionOfPowersWithSameExponent, "[([3 / 2]) ^ 4]")
        testRule("[[3^[2 / 3]] / [4^[2 / 3]]]", RewriteFractionOfPowersWithSameExponent, "[([3 / 4]) ^ [2 / 3]]")
    }

    @Test
    fun testSimplifyRootOfPower() {
        testRule("root[ [7^6], 8]", GeneralRules.RewritePowerUnderRoot, "root[ [7^3*2], 4*2]")
        testRule("root[ [7^4], 8]", GeneralRules.RewritePowerUnderRoot, "root[ [7^4], 2*4]")
    }

    @Test
    fun testFlipFractionUnderNegativePower() {
        testMethod {
            method = FlipFractionUnderNegativePower
            inputExpr = "[([2 / 3]) ^ -[4 / 5]]"

            check {
                toExpr = "[([3 / 2]) ^ [4 / 5]]"

                move("./0/1", "./0/0")
                move("./0/0", "./0/1")
                move("./1/0", "./1")
            }
        }
        testRule("[([3 / 5]) ^ -2]", FlipFractionUnderNegativePower, "[([5 / 3]) ^ 2]")
        testRule("[([7 / 10]) ^ (-5)]", FlipFractionUnderNegativePower, "[([10 / 7]) ^ 5]")
        testRule("[([2 / 3]) ^ [4 / 5]]", FlipFractionUnderNegativePower, null)
    }

    @Test
    fun testRewriteProductOfPowersWithNegatedExponent() {
        testMethod {
            method = RewriteProductOfPowersWithNegatedExponent
            inputExpr = "[2 ^ [1 / 2]] * [([4 / 3]) ^ -[1 / 2]]"

            check {
                toExpr = "[2 ^ [1 / 2]] * [([3 / 4]) ^ [1 / 2]]"

                shift("./0", "./0")
                move("./1/0/1", "./1/0/0")
                move("./1/0/0", "./1/0/1")
                factor {
                    fromPaths("./0/1", "./1/1/0")
                    toPaths("./1/1")
                }
            }
        }
        testRule(
            "[2 ^ [1 / 2]] * [3 ^ -[1 / 3]]",
            RewriteProductOfPowersWithNegatedExponent,
            null,
        )
        testRule(
            "[2 ^ [1 / 2]] * [([4 / 3]) ^ -[1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[2 ^ [1 / 2]] * [([3 / 4]) ^ [1 / 2]]",
        )
        testRule(
            "[2 ^ [1 / 2]] * [3 ^ -[1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[2 ^ [1 / 2]] * [([1 / 3]) ^ [1 / 2]]",
        )
        testRule(
            "[3 ^ -[1 / 2]] * [2 ^ [1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[([1 / 3]) ^ [1 / 2]] * [2 ^ [1 / 2]]",
        )
        testRule(
            "[2 ^ [1 / 2]] * [([2 / 3]) ^ -[1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[2 ^ [1 / 2]] * [([3 / 2]) ^ [1 / 2]]",
        )
        testRule(
            "[([2 / 3]) ^ -[1 / 2]] * [2 ^ [1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[([3 / 2]) ^ [1 / 2]] * [2 ^ [1 / 2]]",
        )
    }

    @Test
    fun testRewriteProductOfPowersWithInverseFractionBase() {
        testMethod {
            method = RewriteProductOfPowersWithInverseFractionBase
            inputExpr = "[([2 / 3]) ^ [1 / 2]] * [([3 / 2]) ^ [2 / 5]]"

            check {
                toExpr = "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ -[2 / 5]]"

                shift("./0", "./0")
                factor {
                    fromPaths("./0/0/0", "./1/0/1")
                    toPaths("./1/0/0")
                }
                factor {
                    fromPaths("./0/0/1", "./1/0/0")
                    toPaths("./1/0/1")
                }
                move("./1/1", "./1/1/0")
            }
        }
        testRule(
            "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseFractionBase,
            null,
        )
        testRule(
            "[2 / 3] * [3 / 2]",
            RewriteProductOfPowersWithInverseFractionBase,
            null,
        )
        testRule(
            "[([2 / 3]) ^ [1 / 2]] * [3 / 2]",
            RewriteProductOfPowersWithInverseFractionBase,
            "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ -1]",
        )
        testRule(
            "[([2 / 3]) ^ [1 / 2]] * [([3 / 2]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseFractionBase,
            "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ -[2 / 5]]",
        )
    }

    @Test
    fun testRewriteProductOfPowersWithInverseBase() {
        testMethod {
            method = RewriteProductOfPowersWithInverseBase
            inputExpr = "[2 ^ [1 / 2]] * [([1 / 2]) ^ [2 / 5]]"

            check {
                toExpr = "[2 ^ [1 / 2]] * [2 ^ -[2 / 5]]"

                shift("./0", "./0")
                factor {
                    fromPaths("./0/0", "./1/0/1")
                    toPaths("./1/0")
                }
                move("./1/1", "./1/1/0")
            }
        }
        testRule(
            "[2 ^ [1 / 2]] * [([1 / 3]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseBase,
            null,
        )
        testRule(
            "[([1 / 2]) ^ [2 / 5]] * [2 ^ [1 / 2]]",
            RewriteProductOfPowersWithInverseBase,
            "[2 ^ -[2 / 5]] * [2 ^ [1 / 2]]",
        )
    }

    @Test
    fun testRewriteOddRootOfNegative() {
        testMethod {
            method = GeneralRules.RewriteOddRootOfNegative
            inputExpr = "root[-8, 3]"

            check {
                toExpr = "-root[8, 3]"

                transform(".", ".")
            }
        }
        testRule("root[8, 3]", GeneralRules.RewriteOddRootOfNegative, null)
        testRule("root[-8, 4]", GeneralRules.RewriteOddRootOfNegative, null)
    }

    @Test
    fun testRewriteIntegerOrderRootAsPower() {
        testMethod {
            method = GeneralRules.RewriteIntegerOrderRootAsPower
            inputExpr = "root[5, 3]"

            check {
                toExpr = "[5^[1/3]]"

                move("./0", "./0")
                introduce("./1/0")
                move("./1", "./1/1")
            }
        }
        testRule("sqrt[x + 2]", GeneralRules.RewriteIntegerOrderRootAsPower, "[(x + 2) ^ [1/2]]")
    }

    @Test
    fun testCancelCommonPowers() {
        testRule(
            "root[[7 ^ 2 * 2], 3 * 2]",
            GeneralRules.CancelRootIndexAndExponent,
            "root[[7 ^ 2], 3]",
        )
        testRule(
            "root[[(-7) ^ 5 * 2], 3 * 2]",
            GeneralRules.CancelRootIndexAndExponent,
            null,
        )
        testRule(
            "root[[(-7) ^ 2 * 3], 4 * 3]",
            GeneralRules.CancelRootIndexAndExponent,
            "root[[(-7) ^ 2], 4]",
        )
        testMethod {
            method = GeneralRules.CancelRootIndexAndExponent
            inputExpr = "root[[7 ^ 2], 3 * 2]"

            check {
                toExpr = "root[7, 3]"

                shift("./0/0", "./0")
                shift("./1/0", "./1")
                cancel("./0/1", "./1/1")
            }
        }
        testRule(
            "root[[7 ^ 2 * 2], 2]",
            GeneralRules.CancelRootIndexAndExponent,
            "[7 ^ 2]",
        )
        testRule(
            "root[[7 ^ 2], 2]",
            GeneralRules.CancelRootIndexAndExponent,
            "7",
        )
        testRule(
            "root[[abs[y] ^ 3 * 4], 4]",
            GeneralRules.CancelRootIndexAndExponent,
            "[abs[y] ^ 3]",
        )
        testRule(
            "sqrt[[(7-sqrt[2])^2]]",
            GeneralRules.CancelRootIndexAndExponent,
            "7-sqrt[2]",
        )
    }

    @Test
    fun testReorderProduct() {
        testMethod {
            method = NormalizationRules.ReorderProduct
            inputExpr = "(x + 1)*5x*sqrt[2]"

            check {
                toExpr = "5 sqrt[2] * x (x + 1)"

                move("./1", "./0")
                move("./3", "./1")
                move("./2", "./2")
                move("./0", "./3")
            }
        }

        testRule(
            "(x + 1)*5",
            NormalizationRules.ReorderProduct,
            "5(x+1)",
        )
        testRule(
            "(x + 1)*5x",
            NormalizationRules.ReorderProduct,
            "5x(x+1)",
        )
        testRule(
            "5*(x+1)*sqrt[2]",
            NormalizationRules.ReorderProduct,
            "5 sqrt[2] (x+1)",
        )
        testRule(
            "5x * sqrt[2]",
            NormalizationRules.ReorderProduct,
            "5 sqrt[2] * x",
        )
        testRule(
            "5(1 + sqrt[2])*sqrt[3]",
            NormalizationRules.ReorderProduct,
            "5 sqrt[3] (1 + sqrt[2])",
        )
        testRule(
            "sqrt[3] * (1 + sqrt[2]) * 5",
            NormalizationRules.ReorderProduct,
            "5 sqrt[3] (1 + sqrt[2])",
        )
        testRule(
            "2*sqrt[2]",
            NormalizationRules.ReorderProduct,
            null,
        )
        testRule(
            "[3^-[1/3]]*2",
            NormalizationRules.ReorderProduct,
            "2*[3^-[1/3]]",
        )
        // although the opposite order is preferable, reordering is unnecessary most of the time
        testRule(
            "[(x + 1) ^ 2] (x + 2)",
            NormalizationRules.ReorderProduct,
            null,
        )
    }

    @Test
    fun testReorderProductSingleStep() {
        testMethod {
            method = NormalizationRules.ReorderProductSingleStep
            inputExpr = "(x + 1)*5x*sqrt[2]"

            check {
                toExpr = "5 (x+1) x sqrt[2]"

                move("./1", "./0")
                shift("./0", "./1")
                shift("./2", "./2")
                shift("./3", "./3")
            }
        }
    }

    @Test
    fun testResolveOrSimplifyAbsoluteValue() {
        testMethod {
            method = GeneralRules.ResolveAbsoluteValueOfZero
            inputExpr = "abs[0]"

            check {
                toExpr = "0"
                transform(".", ".")
            }
        }

        testMethod {
            method = GeneralRules.ResolveAbsoluteValueOfNonNegativeValue
            inputExpr = "abs[3]"

            check {
                toExpr = "3"
                transform(".", ".")
            }
        }
        testRule("abs[3]", GeneralRules.ResolveAbsoluteValueOfNonPositiveValue, null)

        testRule("abs[-3]", GeneralRules.ResolveAbsoluteValueOfNonNegativeValue, null)
        testMethod {
            method = GeneralRules.ResolveAbsoluteValueOfNonPositiveValue
            inputExpr = "abs[-3]"

            check {
                toExpr = "3"
                transform(".", ".")
            }
        }

        testRule("abs[[3 / 2]]", GeneralRules.ResolveAbsoluteValueOfNonNegativeValue, "[3 / 2]")
        testRule("abs[-[3 / 2]]", GeneralRules.ResolveAbsoluteValueOfNonPositiveValue, "[3 / 2]")

        testRule("abs[sqrt[2] + sqrt[3]]", GeneralRules.ResolveAbsoluteValueOfNonNegativeValue, "sqrt[2] + sqrt[3]")
        testRule("abs[-(sqrt[2] + sqrt[3])]", GeneralRules.ResolveAbsoluteValueOfNonPositiveValue, "sqrt[2] + sqrt[3]")

        testRule("abs[-x]", GeneralRules.SimplifyAbsoluteValueOfNegatedExpression, "abs[x]")
        testRule("abs[-(x + y)]", GeneralRules.SimplifyAbsoluteValueOfNegatedExpression, "abs[x + y]")

        testRule("abs[x(x - 1)]", GeneralRules.ResolveAbsoluteValueOfNonNegativeValue, null)

        testRule("abs[[x^2]]", GeneralRules.ResolveAbsoluteValueOfNonNegativeValue, "[x^2]")
        testRule(
            "abs[2 - sqrt[2]]",
            GeneralRules.ResolveAbsoluteValueOfNonNegativeValue,
            "2 - sqrt[2]",
        )
        testRule("abs[-3x + 1]", GeneralRules.ResolveAbsoluteValueOfNonNegativeValue, null)
        testRule(
            "abs[-2 + sqrt[2]]",
            GeneralRules.ResolveAbsoluteValueOfNonPositiveValue,
            "2 - sqrt[2]",
        )
        testRule(
            "abs[-(2 - sqrt[2])]",
            GeneralRules.ResolveAbsoluteValueOfNonPositiveValue,
            "2 - sqrt[2]",
        )
    }

    @Test
    fun testFactorizeInteger() {
        testRule("12", FactorizeInteger, "[2^2] * 3")
    }

    @Test
    fun testSimplifyPlusMinusOfAbsoluteValue() {
        testRule("+/- abs[y]", GeneralRules.SimplifyPlusMinusOfAbsoluteValue, "+/- y")
        testRule("+/- abs[1 - y]", GeneralRules.SimplifyPlusMinusOfAbsoluteValue, "+/- (1 - y)")
    }
}

class PseudoDegreeComparatorTest {
    /**
     * Utility function to test the ordering of two expressions.
     * Checks if e1 is smaller than e2 regardless of their input order.
     */
    private fun testOrdering(smallerExpression: String, biggerExpression: String) {
        val e1 = parseExpression(smallerExpression)
        val e2 = parseExpression(biggerExpression)
        val isOrderedCorrectly = pseudoDegreeComparator.compare(e2, e1) > 0
        assertTrue(isOrderedCorrectly, "Unexpected order of $e1 and $e2")
    }

    @Test
    fun testNonConstantBeforeConstant() {
        testOrdering("1", "sqrt[2]")
        testOrdering("1", "-sqrt[2]")
        testOrdering("-1", "-sqrt[2]")
        testOrdering("1", "2 sqrt[2]")
        testOrdering("5", "x")
        testOrdering("3x", "[x^2]")
        testOrdering("1", "sqrt[2]x")
        testOrdering("sqrt[2]x", "[x^2]")
        testOrdering("2 root[5, 8]", "x")
        testOrdering("1 + x", "1 + x + [x^2]")
        testOrdering("[(1 + x)^6]", "[(1 + [x^2])^5]")
        testOrdering("sqrt[2] [x^2]", "[x^[7/3]]")
        // absolute values are compared
        testOrdering("[x^4]", "[x^-5]")
    }
}
