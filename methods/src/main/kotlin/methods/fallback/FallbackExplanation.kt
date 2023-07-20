package methods.fallback

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class FallbackExplanation : CategorisedMetadataKey {

    /**
     * This explanation is given when the input is already simplified or factored as much as possible.
     * It means the solver thinks there is nothing useful to do, as opposed to not knowing what to do.
     */
    ExpressionIsFullySimplified,

    ;

    override val category = "Fallback"
}

typealias Explanation = FallbackExplanation
