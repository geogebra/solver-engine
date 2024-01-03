package methods.simplify

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.LegacyKeyName
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class SimplifyExplanation : CategorisedMetadataKey {
    /**
     * Simplify an expression in brackets
     */
    @LegacyKeyName("Polynomials.SimplifyExpressionInBrackets")
    SimplifyExpressionInBrackets,

    /**
     * Simplify a polynomial expression in one variable
     */
    @LegacyKeyName("Polynomials.SimplifyPolynomialExpressionInOneVariable")
    SimplifyPolynomialExpression,

    /**
     * Simplify an algebraic expression in one variable
     */
    @LegacyKeyName("Algebra.SimplifyAlgebraicExpression")
    SimplifyAlgebraicExpression,

    ;

    override val category = "Simplify"
}

typealias Explanation = SimplifyExplanation
