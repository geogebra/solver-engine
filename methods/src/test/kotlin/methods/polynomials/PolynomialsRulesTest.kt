package methods.polynomials

import engine.methods.testRule
import methods.polynomials.PolynomialRules.CollectUnitaryMonomialsInProduct
import methods.polynomials.PolynomialRules.DistributeMonomialToIntegerPower
import methods.polynomials.PolynomialRules.DistributeProductToIntegerPower
import methods.polynomials.PolynomialRules.NormalizeMonomial
import methods.polynomials.PolynomialRules.NormalizePolynomial
import org.junit.jupiter.api.Test

class PolynomialsRulesTest {

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

        // Do those simplifications separately so they can have an explanation for the user.
        testRule("0y", NormalizeMonomial, null)
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
}
