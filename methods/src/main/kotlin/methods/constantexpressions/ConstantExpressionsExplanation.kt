package methods.constantexpressions

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class ConstantExpressionsExplanation : CategorisedMetadataKey {
    /**
     * Simplify an expression in brackets
     */
    SimplifyExpressionInBrackets,
    SimplifyPowers;

    override val category = "ConstantExpressions"
}

typealias Explanation = ConstantExpressionsExplanation
