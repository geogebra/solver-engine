package engine.methods

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class SolverEngineExplanation : CategorisedMetadataKey {
    /**
     * Internal step for extracting neighbouring terms or factors into a partial sum or product
     * for further simplifications focusing on them.
     */
    ExtractPartialExpression,

    /**
     * Rearrange a product so that certain factors (on which the system is going to focus) are next to each other.
     * E.g. 3^(1/2) * 2 * 3^(3/2) -> 3^(1/2) * 3^(3/2) * 2
     */
    RearrangeProduct,

    /**
     * Rearrange a sum so that certain terms (on which the system is going to focus) are next to each other.
     * E.g. 1/4 + 2 + 3/2 -> 1/4 + 3/2 + 2
     */
    RearrangeSum,
    ;

    override val category = "SolverEngine"
}
