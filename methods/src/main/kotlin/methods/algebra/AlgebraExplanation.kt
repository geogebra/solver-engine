package methods.algebra

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class AlgebraExplanation : CategorisedMetadataKey {

    /**
     * Simplify an algebraic expression in one variable
     */
    SimplifyAlgebraicExpression,

    ;

    override val category = "Algebra"
}

typealias Explanation = AlgebraExplanation
