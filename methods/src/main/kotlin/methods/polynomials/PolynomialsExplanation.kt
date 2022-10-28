package methods.polynomials

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class PolynomialsExplanation : CategorisedMetadataKey {

    CollectLikeTerms,

    CollectLikeTermsAndSimplify,

    SimplifyAlgebraicExpression;

    override val category = "Polynomials"
}

typealias Explanation = PolynomialsExplanation
