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

package methods.factor

import engine.methods.testMethod
import engine.methods.testRule
import methods.factor.FactorRules.ApplyDifferenceOfSquaresFormula
import methods.factor.FactorRules.ApplySquareOfBinomialFormula
import methods.factor.FactorRules.FactorCommonFactor
import methods.factor.FactorRules.FactorGreatestCommonIntegerFactor
import methods.factor.FactorRules.FactorNegativeSignOfLeadingCoefficient
import methods.factor.FactorRules.RearrangeEquivalentSums
import methods.factor.FactorRules.RewriteCubeOfBinomial
import methods.factor.FactorRules.RewriteDifferenceOfSquares
import methods.factor.FactorRules.RewriteSquareOfBinomial
import methods.factor.FactorRules.SolveSumProductDiophantineEquationSystemByGuessing
import org.junit.jupiter.api.Test

class FactorRulesTest {
    @Test
    fun testFactorGreatestCommonIntegerFactor() {
        testRule("[x^2] + x[y^3]", FactorGreatestCommonIntegerFactor, null)
        testRule("[x^2] + 5[y^3]", FactorGreatestCommonIntegerFactor, null)
        testRule("3[x^2] + 5[y^3]", FactorGreatestCommonIntegerFactor, null)
        testRule("15[x^2] + 21[y^3]", FactorGreatestCommonIntegerFactor, "3(5[x^2] + 7[y^3])")
        testRule("4[x^2] + 8[y^3]", FactorGreatestCommonIntegerFactor, "4([x^2] + 2[y^3])")
        testRule(
            "15[x^2] + 20[y^3] + 25[z^4]",
            FactorGreatestCommonIntegerFactor,
            "5(3[x^2] + 4[y^3] + 5[z^4])",
        )
    }

    @Test
    fun testFactorCommonFactor() {
        testRule(
            "[(x + 1)^2] + [(y + 1)^3]",
            FactorCommonFactor,
            null,
        )
        testRule(
            "[(x + 1)^2] sqrt[2] + [(y + 1)^3] sqrt[2]",
            FactorCommonFactor,
            "sqrt[2] ([(x + 1)^2] + [(y + 1)^3])",
        )
        testRule(
            "3[(x + 1)^3] + 6(x + 1)",
            FactorCommonFactor,
            "(x + 1)(3[(x + 1)^2] + 6)",
        )
        testRule(
            "3[(x + 1)^3] + 6[(x + 1)^2]",
            FactorCommonFactor,
            "[(x + 1)^2](3(x + 1) + 6)",
        )
    }

    @Test
    fun testRearrangeEquivalentSums() {
        testRule(
            "3[(x + 1)^3] + 6[(x + 1)^2]",
            RearrangeEquivalentSums,
            null,
        )
        testRule(
            "3[(x + 1)^3] + 6[(1 + x)^2]",
            RearrangeEquivalentSums,
            "3[(x + 1)^3] + 6[(x + 1)^2]",
        )
        testRule(
            "3[(x + 1)^3] + 6[(1 + x)^2] + 9(1 + x)",
            RearrangeEquivalentSums,
            "3[(x + 1)^3] + 6[(x + 1)^2] + 9(x + 1)",
        )
    }

    @Test
    fun testFactorNegativeSign() {
        testRule("-[x^2] - 2x + 1", FactorNegativeSignOfLeadingCoefficient, "-([x^2] + 2x - 1)")
        testRule("[x^2] + 2x - 1", FactorNegativeSignOfLeadingCoefficient, null)
        testRule("-[x^2] + 2x + 2", FactorNegativeSignOfLeadingCoefficient, "-([x^2] - 2x - 2)")
    }

    @Test
    fun testRewriteDifferenceOfSquares() {
        testRule("[4 / 9] - [25 / 49]", RewriteDifferenceOfSquares, null)
        testRule("9[x^4] - 15[y^2]", RewriteDifferenceOfSquares, null)
        testRule("9[x^4] + 16[y^2]", RewriteDifferenceOfSquares, null)
        testRule("9[x^4] - 16", RewriteDifferenceOfSquares, "[(3[x^2]) ^ 2] - [4 ^ 2]")
        testRule("1 - 16[x^4]", RewriteDifferenceOfSquares, "[1 ^ 2] - [(4[x^2]) ^ 2]")
        testRule("9[x^4] - 16[y^2]", RewriteDifferenceOfSquares, "[(3[x^2]) ^ 2] - [(4y) ^ 2]")

        testMethod {
            method = RewriteDifferenceOfSquares
            inputExpr = "[x ^ 4] - [1 / 16]"

            check {
                toExpr = "[([x ^ 2]) ^ 2] - [([1 / 4]) ^ 2]"
                explanation {
                    key = Explanation.RewriteDifferenceOfSquares
                }
                transform("./0", "./0")
                transform("./1/0", "./1/0")
            }
        }
    }

    @Test
    fun testApplyDifferenceOfSquaresFormula() {
        testRule("[(3[x^2]) ^ 2] - [4 ^ 4]", ApplyDifferenceOfSquaresFormula, null)
        testRule("[(3[x^2]) ^ 2] - [4 ^ 2]", ApplyDifferenceOfSquaresFormula, "(3[x^2] - 4) (3[x^2] + 4)")
        testRule("[1 ^ 2] - [(4[x^2]) ^ 2]", ApplyDifferenceOfSquaresFormula, "(1 - 4[x^2]) (1 + 4[x^2])")

        testMethod {
            method = ApplyDifferenceOfSquaresFormula
            inputExpr = "[(3[x^2]) ^ 2] - [(4y) ^ 2]"

            check {
                toExpr = "(3[x^2] - 4y) (3[x^2] + 4y)"
                explanation {
                    key = Explanation.ApplyDifferenceOfSquaresFormula
                }
                distribute {
                    fromPaths("./0/0")
                    toPaths("./0/0", "./1/0")
                }
                distribute {
                    fromPaths("./1/0/0")
                    toPaths("./0/1/0", "./1/1")
                }
            }
        }
    }

    @Test
    fun testSolveSumProductDiophantineEquationSystemByGuessing() {
        testRule(
            "a + b = 7 AND a * b = 9",
            SolveSumProductDiophantineEquationSystemByGuessing,
            null,
        )
        testRule(
            "a + b = 5 AND a * b = 6",
            SolveSumProductDiophantineEquationSystemByGuessing,
            "a = 2 AND b = 3",
        )
        testRule(
            "a + b = 6 AND a * b = 5",
            SolveSumProductDiophantineEquationSystemByGuessing,
            "a = 1 AND b = 5",
        )
        testRule(
            "a + b = -2 AND a * b = -15",
            SolveSumProductDiophantineEquationSystemByGuessing,
            "a = -5 AND b = 3",
        )
    }

    @Test
    fun testRewriteSquareOfBinomial() {
        testRule(
            "[x ^ 2] + 2x + 1",
            RewriteSquareOfBinomial,
            "[x ^ 2] + 2 * 1 * x + [1 ^ 2]",
        )
        testRule(
            "[x ^ 2] + 4x + 4",
            RewriteSquareOfBinomial,
            "[x ^ 2] + 2 * 2 * x + [2 ^ 2]",
        )
        testRule(
            "4[x ^ 2] + 12x + 9",
            RewriteSquareOfBinomial,
            "[(2x) ^ 2] + 2 * 3 * <. 2x .> + [3 ^ 2]",
        )
        testRule(
            "4[x ^ 2] - 12x + 9",
            RewriteSquareOfBinomial,
            "[(2x) ^ 2] + 2 * (-3) * <. 2x .> + [(-3) ^ 2]",
        )

        testMethod {
            method = RewriteSquareOfBinomial
            inputExpr = "[x ^ 8] - [1 / 8][x ^ 4] + [1 / 256]"

            check {
                toExpr = "[([x ^ 4]) ^ 2] + 2 * (-[1 / 16]) * [x ^ 4] + [(-[1 / 16]) ^ 2]"
                explanation {
                    key = Explanation.RewriteSquareOfBinomial
                }
                transform("./0", "./0")
                transform("./1", "./1")
                transform("./2", "./2")
            }
        }
    }

    @Test
    fun testApplySquareOfBinomialFormula() {
        testRule(
            "[x^2] + 2 * 1 * x + [1 ^ 2]",
            ApplySquareOfBinomialFormula,
            "[(x + 1) ^ 2]",
        )
        testRule(
            "[x^2] + 2 * sqrt[2] * x + [sqrt[2] ^ 2]",
            ApplySquareOfBinomialFormula,
            "[(x + sqrt[2]) ^ 2]",
        )
        testRule(
            "[(2x) ^ 2] + 2 * 3 * <. 2x .> + [3 ^ 2]",
            ApplySquareOfBinomialFormula,
            "[(2x + 3) ^ 2]",
        )
        testRule(
            "[(2x) ^ 2] + 2 * (-3) * <. 2x .> + [(-3) ^ 2]",
            ApplySquareOfBinomialFormula,
            "[(2x - 3) ^ 2]",
        )

        testMethod {
            method = ApplySquareOfBinomialFormula
            inputExpr = "[(x + 2) ^ 2] + [(x + 3) ^ 2] - 2(x + 2)(x + 3)"

            check {
                toExpr = "[((x + 2) - (x + 3)) ^ 2]"
                explanation {
                    key = Explanation.ApplySquareOfBinomialFormula
                }
                factor {
                    fromPaths("./0/0", "./2/0/1")
                    toPaths("./0/0")
                }
                factor {
                    fromPaths("./2/0/2", "./1/0")
                    toPaths("./0/1/0")
                }
                factor {
                    fromPaths("./0/1", "./2/0/0", "./1/1")
                    toPaths("./1")
                }
            }
        }
    }

    @Test
    fun testRewriteCubeOfBinomial() {
        testRule(
            "[x ^ 4] + 3[x ^ 2] + 3x + 1",
            RewriteCubeOfBinomial,
            null,
        )
        testRule(
            "[x ^ 3] + 3[x ^ 2] + 3x + 1",
            RewriteCubeOfBinomial,
            "[x ^ 3] + 3 * [x ^ 2] * 1 + 3 * x * [1 ^ 2] + [1 ^ 3]",
        )
        testRule(
            "[x ^ 3] + 1 + 3[x ^ 2] + 3x",
            RewriteCubeOfBinomial,
            "[x ^ 3] + 3 * [x ^ 2] * 1 + 3 * x * [1 ^ 2] + [1 ^ 3]",
        )

        testMethod {
            method = RewriteCubeOfBinomial
            inputExpr = "[x ^ 6] + 3[x ^ 4] + 3[x ^ 2] + 1"

            check {
                toExpr = "[([x ^ 2]) ^ 3] + 3 * [([x ^ 2]) ^ 2] * 1 + 3 * [x ^ 2] * [1 ^ 2] + [1 ^ 3]"
                explanation {
                    key = Explanation.RewriteCubeOfBinomial
                }
                transform("./0", "./0")
                transform("./1", "./1")
                transform("./2", "./2")
                transform("./3", "./3")
            }
        }
    }
}
