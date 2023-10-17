package methods.factor

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.LegacyKeyName
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class FactorExplanation : CategorisedMetadataKey {

    /**
     * Factor a single common, non-integer factor (with the greatest possible exponent) from a sum.
     *
     * E.g. 3 (x + 1)^3 + 6 (x + 1)^2 -> (x + 1)^2 (3(x+1) + 6)
     */
    FactorCommonFactor,

    /**
     * Rearrange a sum before factoring common terms, to make the common term obvious.
     *
     * E.g. 3 (x + 1)^3 + 6 (1 + x)^2 -> 3 (x + 1)^3 + 6 (x + 1)^2
     */
    RearrangeEquivalentSums,

    FactorIntegerInSquareRoot,

    /**
     * Factor out the greatest common factor from a sum, by first splitting
     * the monomials and then extracting the common terms.
     *
     * E.g. 6x + 9x^2 + 12x^3
     *   -> 3x * 2 + 3x * 3x + 3x * 4x^2
     *   -> 3x(2 + 3x + 4x^2)
     */
    @LegacyKeyName("Polynomials.FactorGreatestCommonFactor")
    FactorGreatestCommonFactor,

    FactorGreatestCommonSquareIntegerFactor,

    /**
     * Factor out the greatest common integer factor for a sum by splitting the
     * terms so the gcd is apparent and then factoring it out.
     *
     * E.g. 4x^2 - 6x
     *   --> 2 * 2x^2 - 2 * 3x
     *   --> 2(2x^2 - 3x)
     */
    FactorGreatestCommonIntegerFactor,

    /**
     * Factor out the negative sign from the leading coefficient term
     * of the polynomial
     *
     * E.g. -x^2 + 2x - 1 -> -(x^2 - 2x + 1)
     */
    @LegacyKeyName("Equations.FactorNegativeSignOfLeadingCoefficient")
    FactorNegativeSignOfLeadingCoefficient,

    /**
     * Rewrite the square of a binomial to make it obvious how it should be
     * factored.
     *
     * E.g. x^2 + 6x + 9 -> x^2 + 2 * 3 * x + 3^2
     */
    RewriteSquareOfBinomial,

    /**
     * Apply the square of a binomial formula a^2 + 2ab + b^2 = (a + b)^2 to factor.
     *
     * E.g. (3x^2)^2 + 2 * 3x * 4y + (4y)^2 -> (3x^2 + 4y)^2
     */
    ApplySquareOfBinomialFormula,

    /**
     * Factor the square of a binomial by first rewriting it to make it clearer then
     * applying the formula.
     *
     * E.g. x^2 + 6x + 9
     *   -> x^2 + 2 * 3 * x + 3^2
     *   -> (x + 3)^2
     */
    FactorSquareOfBinomial,

    /**
     * Rewrite the cube of a binomial to make it obvious how it should be
     * factored.
     *
     * E.g. 8x^3 - 12x^2 + 6x - 1 -> (2x)^3 + 3*(2x)^2*(-1) + 3*2x*(-1)^22 + (-1)^3
     */
    RewriteCubeOfBinomial,

    /**
     * Apply the cube of a binomial formula a^3 + 3a^b + 3ab^2 + b^3 = (a + b)^3 to factor.
     *
     * E.g. (2x)^3 + 3*(2x)^2*(-1) + 3*2x*(-1)^22 + (-1)^3 -> (2x - 1)^3
     */
    ApplyCubeOfBinomialFormula,

    /**
     * Factor the cube of a binomial by first rewriting it to make it clearer then
     * applying the formula.
     *
     * E.g. 8x^3 - 12x^2 + 6x - 1
     *   -> (2x)^3 + 3*(2x)^2*(-1) + 3*2x*(-1)^22 + (-1)^3
     *   -> (2x - 1)^3
     */
    FactorCubeOfBinomial,

    /**
     * Rewrite the difference of squares to make it obvious how it should be
     * factored.
     *
     * E.g. 9x^4 - 16y^2 -> (3x^2)^2 - (4y)^2
     */
    @LegacyKeyName("Polynomials.RewriteDifferenceOfSquares")
    RewriteDifferenceOfSquares,

    /**
     * Apply the difference of squares formula a^2 - b^2 = (a - b)(a + b) to factor.
     *
     * E.g. (3x^2)^2 - (4y)^2 -> (3x^2 - 4y)(3x^2 + 4y)
     */
    @LegacyKeyName("Polynomials.ApplyDifferenceOfSquaresFormula")
    ApplyDifferenceOfSquaresFormula,

    /**
     * Factor the difference of squares by first rewriting it to make it clearer then
     * applying the formula.
     *
     * E.g. 9x^4 - 16y^2
     *   -> (3x^2)^2 - (4y)^2
     *   -> (3x^2 - 4y)(3x^2 + 4y)
     */
    @LegacyKeyName("Polynomials.FactorDifferenceOfSquares")
    FactorDifferenceOfSquares,

    /**
     * Rewrite the difference of cubes to make it obvious how it should be
     * factored.
     *
     * E.g. 64 - 27x^3 -> 4^3 - (3x)^3
     */
    RewriteDifferenceOfCubes,

    /**
     * Apply the difference of cubes formula a^3 - b^3 = (a - b)(a^2 - ab + b^2) to factor.
     *
     * E.g. 4^3 - (3x)^3 -> (4 - 3x)(4^2 + 4 * 3x + (3x)^2)
     */
    ApplyDifferenceOfCubesFormula,

    /**
     * Factor the difference of cubes by first rewriting it to make it clearer then
     * applying the formula.
     *
     * E.g. 64 - 27x^3
     *   -> 4^3 - (3x)^3
     *   -> (4 - 3x)(4^2 + 4 * 3x + (3x)^2)
     *   -> (4 - 3x)(16 + 12x + 9x^2)
     */
    FactorDifferenceOfCubes,

    /**
     * Rewrite the difference of cubes to make it obvious how it should be
     * factored.
     *
     * E.g. 64 + 27x^3 -> 4^3 + (3x)^3
     */
    RewriteSumOfCubes,

    /**
     * Apply the difference of cubes formula a^3 - b^3 = (a - b)(a^2 - ab + b^2) to factor.
     *
     * E.g. 4^3 + (3x)^3 -> (4 + 3x)(4^2 - 4 * 3x + (3x)^2)
     */
    ApplySumOfCubesFormula,

    /**
     * Factor the difference of cubes by first rewriting it to make it clearer then
     * applying the formula.
     *
     * E.g. 64 + 27x^3
     *   -> 4^3 + (3x)^3
     *   -> (4 + 3x)(4^2 - 4 * 3x + (3x)^2)
     *   -> (4 + 3x)(16 - 12x + 9x^2)
     */
    FactorSumOfCubes,

    /**
     * Group a polynomial into two parts using brackets
     *
     * E.g. 6x^3 + 8x^2 + 9x + 12 -> (6x^3 + 8x^2) + (9x + 12)
     */
    GroupPolynomial,

    /**
     * Factor a polynomial by first grouping into two parts, factoring
     * them individually, then factoring the resulting expression.
     *
     * E.g. 6x^3 + 8x^2 + 9x + 12
     *   -> (6x^3 + 8x^2) + (9x + 12)
     *   -> 2x^2(3x + 4) + (9x + 12)
     *   -> 2x^2(3x + 4) + 3(3x + 4)
     *   -> (3x + 4)(2x^2 + 3)
     */
    FactorByGrouping,

    /**
     * Solve a diophantine equation system of the form by "guessing".
     * a + b = s
     * a * b = p
     *
     * E.g. {a + b = 5, a * b = 6} -> {a = 2, b = 3}
     */
    @LegacyKeyName("Polynomials.SolveSumProductDiophantineEquationSystemByGuessing")
    SolveSumProductDiophantineEquationSystemByGuessing,

    /**
     * Set up the equation system
     * a + b = s
     * a * b = p
     * for the trinomial x^2 + sx + p and solve it.
     *
     * E.g. for x^2 + 5x + 6
     *   {a + b = 5, a * b = 6} -> {a = 2, b = 3}
     */
    @LegacyKeyName("Polynomials.SetUpAndSolveEquationSystemForMonicTrinomial")
    SetUpAndSolveEquationSystemForMonicTrinomial,

    /**
     * Factor a trinomial x^2 + sx + p into (x + a)(x + b) after
     * solving the equation system
     * a + b = s
     * a * b = p
     *
     * E.g. knowing that 2 + 3 = 5 and 2 * 3 = 6
     *   x^2 + 5x + 6 -> (x + 2)(x + 3)
     */
    @LegacyKeyName("Polynomials.FactorTrinomialUsingTheSolutionsOfTheSumAndProductSystem")
    FactorTrinomialUsingTheSolutionsOfTheSumAndProductSystem,

    /**
     * Factor a trinomial x^2 + sx + p into (x + a)(x + b) by "guessing"
     * integers a and b such that a + b = s and a * b = p.
     *
     * E.g. x^2 + 5x + 6 = (x + 2)(x + 3)
     */
    @LegacyKeyName("Polynomials.FactorTrinomialByGuessing")
    FactorTrinomialByGuessing,

    /**
     * Set up the equation system
     * t + v = b
     * t * v = a * c
     * for the trinomial ax^2 + bx + c and solve it.
     *
     * E.g. for 6x^2 + 11x + 4
     *   {t + v = 11, t * v = 24} -> {t = 3, v = 8}
     */
    SetUpAndSolveEquationSystemForNonMonicTrinomial,

    /**
     * Split a trinomial ax^2 + bx + c into ax^2 + sx + tx + c
     * after solving the equation system
     * s + t = b
     * s * t = a * c
     *
     * E.g. knowing that 8 + 3 = 11 and 8 * 3 = 6 * 4
     *   6x^2 + 11x + 4 -> 6x^2 + 8x + 3x + 4
     */
    SplitTrinomialUsingTheSolutionsOfTheSumAndProductSystem,

    /**
     * Split a trinomial ax^2 + bx + c into ax^2 + sx + tx + c
     * in such a way that it can be factored by grouping.
     *
     * E.g. 6x^2 + 11x + 4 -> 6x^2 + 8x + 3x + 4
     */
    SplitNonMonicTrinomial,

    /**
     * Factor a non-monic trinomial ax^2 + bx + c by first splitting
     * it into ax^2 + sx + tx + c, where s + t = b and s * t = a * c
     * then grouping into (ax^2 + sx) + (tx + c).
     *
     * E.g. 6x^2 + 11x + 4
     *   -> 6x^2 + 3x + 8x + 4
     *   -> (6x^2 + 3x) + (8x + 4)
     *   -> 3x(2x + 1) + (8x + 4)
     *   -> 3x(2x + 1) + 4(2x + 1)
     *   -> (2x + 1)(3x + 4)
     */
    FactorNonMonicTrinomial,

    /**
     * Factor a polynomial expression.
     *
     * E.g. 18x^4 - 32x^2
     *   -> 2x^2(9x^2 - 16)
     *   -> 2x^2(3x - 4)(3x + 4)
     */
    @LegacyKeyName("Polynomials.FactorPolynomial")
    FactorPolynomial,
    ;

    override val category = "Factor"
}

typealias Explanation = FactorExplanation
