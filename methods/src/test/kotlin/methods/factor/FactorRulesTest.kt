package methods.factor

import engine.methods.testMethod
import engine.methods.testRule
import methods.factor.FactorRules.ApplyDifferenceOfSquaresFormula
import methods.factor.FactorRules.ApplySquareOfBinomialFormula
import methods.factor.FactorRules.ExtractCommonTerms
import methods.factor.FactorRules.FactorNegativeSignOfLeadingCoefficient
import methods.factor.FactorRules.RewriteCubeOfBinomial
import methods.factor.FactorRules.RewriteDifferenceOfSquares
import methods.factor.FactorRules.RewriteSquareOfBinomial
import methods.factor.FactorRules.SolveSumProductDiophantineEquationSystemByGuessing
import methods.factor.FactorRules.SplitIntegersInMonomialsBeforeFactoring
import methods.factor.FactorRules.SplitVariablePowersInMonomialsBeforeFactoring
import org.junit.jupiter.api.Test

class FactorRulesTest {

    @Test
    fun testSplitIntegersInMonomialsBeforeFactoring() {
        testRule("13 + 4[x^2]", SplitIntegersInMonomialsBeforeFactoring, null)
        testRule("3x + 9", SplitIntegersInMonomialsBeforeFactoring, "3x + 3*3")
        testRule("3[x^2] + 12x", SplitIntegersInMonomialsBeforeFactoring, "3 [x^2] + 3 * 4x")
        testRule("3[y^2] + 12x", SplitIntegersInMonomialsBeforeFactoring, "3 [y^2] + 3 * 4x")
    }

    @Test
    fun testSplitVariablePowersInMonomialsBeforeFactoring() {
        testRule("3[y^2] + 12x", SplitVariablePowersInMonomialsBeforeFactoring, null)
        testRule("3[x^2] + 12x", SplitVariablePowersInMonomialsBeforeFactoring, "3 x * x + 12 x")
        testRule(
            "3 sqrt[2] [x^2] + 12 [x^3] + 9 [x^5]",
            SplitVariablePowersInMonomialsBeforeFactoring,
            "3 sqrt[2] * [x^2] + 12 [x^2] * x + 9 [x^2] * [x^3]",
        )
    }

    @Test
    fun testExtractCommonTerms() {
        testRule("3x * 2 + 3x * 3x + 4[x^2]", ExtractCommonTerms, null)
        testRule("3x * 2 + 3x * 3x + 3x * 4[x^2]", ExtractCommonTerms, "3x (2 + 3x + 4[x ^ 2])")
        testRule("2 sqrt[2] + 2 * 2sqrt[2] x", ExtractCommonTerms, "2 sqrt[2](1 + 2x)")
        testRule("2 sqrt[2] - 2 * 2sqrt[2] x", ExtractCommonTerms, "2 sqrt[2](1 - 2x)")
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
            "a + b = 7, a * b = 9",
            SolveSumProductDiophantineEquationSystemByGuessing,
            null,
        )
        testRule(
            "a + b = 5, a * b = 6",
            SolveSumProductDiophantineEquationSystemByGuessing,
            "a = 2, b = 3",
        )
        testRule(
            "a + b = 6, a * b = 5",
            SolveSumProductDiophantineEquationSystemByGuessing,
            "a = 1, b = 5",
        )
        testRule(
            "a + b = -2, a * b = -15",
            SolveSumProductDiophantineEquationSystemByGuessing,
            "a = -5, b = 3",
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
