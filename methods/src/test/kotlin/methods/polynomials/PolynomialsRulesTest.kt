package methods.polynomials

import engine.methods.testRule
import methods.polynomials.PolynomialRules.ApplyDifferenceOfSquaresFormula
import methods.polynomials.PolynomialRules.CollectLikeTerms
import methods.polynomials.PolynomialRules.CollectUnitaryMonomialsInProduct
import methods.polynomials.PolynomialRules.DistributeMonomialToIntegerPower
import methods.polynomials.PolynomialRules.DistributeProductToIntegerPower
import methods.polynomials.PolynomialRules.ExtractCommonTerms
import methods.polynomials.PolynomialRules.NormalizeMonomial
import methods.polynomials.PolynomialRules.NormalizePolynomial
import methods.polynomials.PolynomialRules.RewriteDifferenceOfSquares
import methods.polynomials.PolynomialRules.SolveSumProductDiophantineEquationSystemByGuessing
import methods.polynomials.PolynomialRules.SplitIntegersInMonomialsBeforeFactoring
import methods.polynomials.PolynomialRules.SplitVariablePowersInMonomialsBeforeFactoring
import org.junit.jupiter.api.Test

class PolynomialsRulesTest {

    @Test
    fun testCollectLikeTerms() {
        testRule("x + x", CollectLikeTerms, "(1+1) x")
        testRule("2*y - 3*y", CollectLikeTerms, "(2 - 3) y")
        testRule("z + [1/2]*z + [z / 2] - z*3", CollectLikeTerms, "(1 + [1/2] + [1/2] - 3) z")
        testRule("t*sqrt[3] + 2*t - [t*sqrt[2]/2]", CollectLikeTerms, "(sqrt[3] + 2 - [sqrt[2]/2]) t")
    }

    @Test
    fun testMultiplyMonomials() {
        testRule("x*x", CollectUnitaryMonomialsInProduct, null)
        testRule("2x*3x", CollectUnitaryMonomialsInProduct, "(2*3)(x*x)")
        testRule("[x/2] * [3x/5] * [x^2]", CollectUnitaryMonomialsInProduct, "([1/2]*[3/5])(x*x*[x^2])")
        testRule("(-2x)*5[x^2]", CollectUnitaryMonomialsInProduct, "((-2)*5)(x*[x^2])")
        testRule("2[x^2]", CollectUnitaryMonomialsInProduct, null)

        // The negation of a product is also handled by this rule.
        testRule("-2x*3x", CollectUnitaryMonomialsInProduct, "-(2 * 3)(x * x)")
        testRule("-2t(-[t^3])", CollectUnitaryMonomialsInProduct, "((-2) * (-1))(t * [t^3])")
    }

    @Test
    fun testNormalizeMonomial() {
        testRule("t*2", NormalizeMonomial, "2t")
        testRule("2x", NormalizeMonomial, null)
        testRule("2*y", NormalizeMonomial, "2y")
        testRule("2y * 3", NormalizeMonomial, "2*3y")
        testRule("[3t/2]", NormalizeMonomial, "[3/2]t")
        testRule("-2x", NormalizeMonomial, null)
        testRule("(-1)[t^3]", NormalizeMonomial, "-[t^3]")
        testRule("1[z^2]", NormalizeMonomial, "[z^2]")
        testRule("0y", NormalizeMonomial, "0")

        // Do those simplifications separately so they can have an explanation for the user.
        testRule("[x^1]", NormalizeMonomial, null)
        testRule("[t^0]", NormalizeMonomial, null)
    }

    @Test
    fun distributeMonomialToIntegerPower() {
        testRule("[([t/2]) ^ 4]", DistributeMonomialToIntegerPower, "[([1/2]) ^ 4][t ^ 4]")
        testRule("[(-2[x ^ 3]) ^ 4]", DistributeMonomialToIntegerPower, "[(-2) ^ 4][([x ^ 3]) ^ 4]")
        testRule("[(-tsqrt[3]) ^ 5]", DistributeMonomialToIntegerPower, "[(-sqrt[3]) ^ 5] * [t ^ 5]")
        testRule("[(2x)^2]", DistributeProductToIntegerPower, "[2^2][x^2]")
        testRule("[([1/2]tsqrt[2]) ^ 4]", DistributeProductToIntegerPower, "[([1/2]sqrt[2])^4]*[t^4]")
    }

    @Test
    fun testDistributeProductToIntegerPower() {
        testRule("[(2x * 3x) ^ 2]", DistributeProductToIntegerPower, "[(2 * 3) ^ 2]*[x^2]*[x^2]")
    }

    @Test
    fun testNormalizePolynomial() {
        testRule("1 + 2", NormalizePolynomial, null)
        testRule("x + 1", NormalizePolynomial, null)
        testRule("1 + x", NormalizePolynomial, "x + 1")
        testRule("1 + 2y + [[y^2]/2]", NormalizePolynomial, "[[y^2]/2] + 2y + 1")
        testRule("[t^10] + 2[t^3] + sqrt[3] + 1", NormalizePolynomial, null)
        testRule("1 + [t^10] + 2[t^3] + sqrt[3]", NormalizePolynomial, "[t^10] + 2[t^3] + 1 + sqrt[3]")
    }

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
    fun testRewriteDifferenceOfSquares() {
        testRule("9[x^4] - 15[y^2]", RewriteDifferenceOfSquares, null)
        testRule("9[x^4] + 16[y^2]", RewriteDifferenceOfSquares, null)
        testRule("9[x^4] - 16", RewriteDifferenceOfSquares, "[(3[x^2]) ^ 2] - [4 ^ 2]")
        testRule("1 - 16[x^4]", RewriteDifferenceOfSquares, "[1 ^ 2] - [(4[x^2]) ^ 2]")
        testRule("9[x^4] - 16[y^2]", RewriteDifferenceOfSquares, "[(3[x^2]) ^ 2] - [(4y) ^ 2]")
    }

    @Test
    fun testApplyDifferenceOfSquaresFormula() {
        testRule("[(3[x^2]) ^ 2] - [4 ^ 4]", ApplyDifferenceOfSquaresFormula, null)
        testRule("[(3[x^2]) ^ 2] - [4 ^ 2]", ApplyDifferenceOfSquaresFormula, "(3[x^2] - 4) (3[x^2] + 4)")
        testRule("[1 ^ 2] - [(4[x^2]) ^ 2]", ApplyDifferenceOfSquaresFormula, "(1 - 4[x^2]) (1 + 4[x^2])")
        testRule("[(3[x^2]) ^ 2] - [(4y) ^ 2]", ApplyDifferenceOfSquaresFormula, "(3[x^2] - 4y) (3[x^2] + 4y)")
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
    fun testFactorTrinomialToSquare() {
        testRule("[x^2] + 2x + 1", PolynomialRules.FactorTrinomialToSquare, "[(x + [1/2]*2) ^ 2]")
        testRule("[x^2] - 4x + 4", PolynomialRules.FactorTrinomialToSquare, "[(x + [1/2]*(-4)) ^ 2]")
        testRule(
            "[x^2] + [1/2]x + [1 / 16]",
            PolynomialRules.FactorTrinomialToSquare,
            "[(x + [1 / 2] * [1 / 2]) ^ 2]",
        )
        testRule(
            "[x ^ 6] + 2[x^3] + 1",
            PolynomialRules.FactorTrinomialToSquare,
            "[([x^3] + [1/2]*2) ^ 2]",
        )
    }
}
