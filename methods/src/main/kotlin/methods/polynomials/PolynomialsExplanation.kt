package methods.polynomials

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class PolynomialsExplanation : CategorisedMetadataKey {
    /**
     * Add the first two addends that are in the form of an integer times a variable (that have the same variable)
     *
     * E.g. 2x + 3 + x + 2[x^2] --> 3x + 3 + 2[x^2]
     */
    CombineTwoSimpleLikeTerms,

    /**
     * Collect like terms of the form a[x^n] in a sum
     *
     * E.g. [x^2] + 3 + x + 2[x^2] --> (1 + 2)[x^2] + 3 + x
     */
    CollectLikeTerms,

    /**
     * Simplify the coefficient of a monomial after collecting like terms
     *
     * E.g. ([1 / 2] + 2)x^2 -> [5 / 2]x^2
     */
    SimplifyCoefficient,

    /**
     * Collect like terms in a sum and simplify the collected coefficients
     *
     * E.g. [x^2] + 3 + x + 2[x^2] --> 3[x^2] + 3 + x
     */
    CollectLikeTermsAndSimplify,

    /**
     * Collect powers of the same variable in a product
     *
     * E.g. 2x * 3[x^2] * [x/5] --> (2 * 3 * [1/5])(x * [x^2] * x)
     */
    CollectUnitaryMonomialsInProduct,

    /**
     * Multiply powers of the same variable
     *
     * E.g. [x^2] * [x^3] * x --> [x^6]
     */
    MultiplyUnitaryMonomialsAndSimplify,

    /**
     * Multiply monomials in the same variable and simplify the result
     *
     * E.g. 2x * 3[x^2] * [x/5] --> [6/5][x^4]
     */
    MultiplyMonomialsAndSimplify,

    /**
     * Normalize a monomial so the variable appears at the end (but do not simplify)
     *
     * E.g. x * 2     --> 2x
     *      3x * 5    --> 3 * 5x
     *      [x/2] * 3 --> [1/2] * 3x
     */
    NormalizeMonomial,

    /**
     * Normalize all monomials in an expression
     */
    NormalizeAllMonomials,

    /**
     * Simplify the coefficient of a monomial
     *
     * E.g. x * 2     --> 2x
     *      3x * 5    --> 15x
     *      [x/2] * 3 --> [3/2]x
     */
    SimplifyMonomial,

    /**
     * Distribute a product raised to an integer power
     *
     * E.g. [(2[x^3]) ^ 2] --> [2^2] * [([x^3]) ^ 2]
     */
    DistributeProductToIntegerPower,

    /**
     * Use the power rule to simplify the power of a power of a variable and simplify the result
     *
     * E.g. [([x^3]) ^ 2] --> [x ^ 6]
     */
    SimplifyPowerOfUnitaryMonomial,

    /**
     * Distribute a product raised to an integer power and simplify the result
     *
     * E.g. [(2[x^3]) ^ 2] --> 4[x^6]
     */
    DistributeProductToIntegerPowerAndSimplify,

    /**
     * Normalize the order of terms in a polynomial so that monomials are in descending order.
     * Terms which are not monomial are added to the right and their order is not changed
     *
     * E.g. sqrt[3] + x + 1 + 2[x^2] -> 2[x^2] + x + sqrt[3] + 1
     */
    NormalizePolynomial,

    /**
     * Simplify an algebraic expression in one variable
     */
    SimplifyAlgebraicExpression,

    ExpandPolynomialExpression,

    /**
     * Find the gcd of all the integer coefficients in a sum of monomials
     * and split the integers into a product of gcd and remainder
     *
     * E.g. 4 + 2x + 8x^2 -> 2 * 2 + 2x + 2 * 4x^2
     *   6x + 9x^2 + 12x^3 -> 3 * 2x + 3 * 3x^2 + 3 * 4x^3
     */
    SplitIntegersInMonomialsBeforeFactoring,

    /**
     * Find the gcf of all the variable powers in a sum of monomials
     * and split the powers into a product of gcf and remainder
     *
     * E.g. x^2 + x^3 -> x^2 + x^2 * x
     *   6x + 9x^2 + 12x^3 -> 6x + 9x * x + 12x * x^2
     */
    SplitVariablePowersInMonomialsBeforeFactoring,

    /**
     * Extract the common terms from the sum of monomials. Usually applied
     * after splitting monomials into the gcf and the remainder.
     *
     * E.g. 3x * 2 + 3x * 3x + 3x * 4x^2 -> 3x(2 + 3x + 4x^2)
     *   2 sqrt[2] + 2 * 2sqrt[2] x -> 2 sqrt[2] (1 + 2x)
     */
    ExtractCommonTerms,

    /**
     * Factor out the greatest common factor from a sum, by first splitting
     * the monomials and then extracting the common terms.
     *
     * E.g. 6x + 9x^2 + 12x^3
     *   -> 3x * 2 + 3x * 3x + 3x * 4x^2
     *   -> 3x(2 + 3x + 4x^2)
     */
    FactorGreatestCommonFactor,

    /**
     * Rewrite the difference of squares to make it obvious how it should be
     * factored.
     *
     * E.g. 9x^4 - 16y^2 -> (3x^2)^2 - (4y)^2
     */
    RewriteDifferenceOfSquares,

    /**
     * Apply the difference of squares formula a^2 - b^2 = (a - b)(a + b) to factor.
     *
     * E.g. (3x^2)^2 - (4y)^2 -> (3x^2 - 4y)(3x^2 + 4y)
     */
    ApplyDifferenceOfSquaresFormula,

    /**
     * Factor the difference of squares by first rewriting it to make it clearer then
     * applying the formula.
     *
     * E.g. 9x^4 - 16y^2
     *   -> (3x^2)^2 - (4y)^2
     *   -> (3x^2 - 4y)(3x^2 + 4y)
     */
    FactorDifferenceOfSquares,

    /**
     * Factor a trinomial x^2 + sx + p into (x + a)(x + b) by "guessing"
     * integers a and b such that a + b = s and a * b = p.
     *
     * E.g. x^2 + 5x + 6 = (x + 2)(x + 3)
     */
    FactorTrinomialByGuessing,

    /**
     * Set up the equation system
     * a + b = s
     * a * b = p
     * for the trinomial x^2 + sx + p and solve it.
     *
     * E.g. for x^2 + 5x + 6
     *   {a + b = 5, a * b = 6} -> {a = 2, b = 3}
     */
    SetUpAndSolveEquationSystemForTrinomial,

    /**
     * Solve a diophantine equation system of the form by "guessing".
     * a + b = s
     * a * b = p
     *
     * E.g. {a + b = 5, a * b = 6} -> {a = 2, b = 3}
     */
    SolveSumProductDiophantineEquationSystemByGuessing,

    /**
     * Factor a trinomial x^2 + sx + p into (x + a)(x + b) after
     * solving the equation system
     * a + b = s
     * a * b = p
     *
     * E.g. knowing that 2 + 3 = 5 and 2 * 3 = 6
     *   x^2 + 5x + 6 -> (x + 2)(x + 3)
     */
    FactorTrinomialUsingTheSolutionsOfTheSumAndProductSystem,

    /**
     * Factor a polynomial expression.
     *
     * E.g. 18x^4 - 32x^2
     *   -> 2x^2(9x^2 - 16)
     *   -> 2x^2(3x - 4)(3x + 4)
     */
    FactorPolynomial,

    /**
     * Factor a trinomial of the form x^2 + ax + b to (x + a/2)^2 if possible
     * (i.e. if (a/2)^2 == b).  The constant in the bracket is not simplified
     *
     * E.g. x^2 + 6x + 9 --> (x + 6/2)^2
     */
    FactorTrinomialToSquare,

    /**
     * Factor a trinomial of the form x^2 + ax + b to (x + k)^2 if possible
     * (i.e. if (a/2)^2 == b).  The constant in the bracket is simplified
     *
     * E.g. x^2 + 6x + 9 --> (x + 3)^2
     */
    FactorTrinomialToSquareAndSimplify,

    ;

    override val category = "Polynomials"
}

typealias Explanation = PolynomialsExplanation
