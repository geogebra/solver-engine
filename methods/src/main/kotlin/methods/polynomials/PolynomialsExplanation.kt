package methods.polynomials

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class PolynomialsExplanation : CategorisedMetadataKey {

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

    ExpandSingleBracketWithIntegerCoefficient,

    ;

    override val category = "Polynomials"
}

typealias Explanation = PolynomialsExplanation
