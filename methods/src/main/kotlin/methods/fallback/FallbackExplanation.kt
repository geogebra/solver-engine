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

    /**
     * This explanation is given when the input is a simplified quadratic with a negative discriminant
     */
    QuadraticIsIrreducible,

    /**
     * Form an inequality of the form b^2 - 4ac < 0 and solve it to find whether it is true
     */
    CheckDiscriminantIsNegative,

    /**
     * Conclude that the quadratic is negative because its discriminant was shown to be negative.
     */
    QuadraticIsIrreducibleBecauseDiscriminantIsNegative,

    ;

    override val category = "Fallback"
}

typealias Explanation = FallbackExplanation
