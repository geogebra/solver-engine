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

    /**
     * After executing a series of tasks on some terms of a sum or some factors of a product, substitute the result
     * into the original expression
     * E.g. for 1/(x + 1) + 1/(x + 2) + 1/(x + 3)
     *      first compute 1/(x + 1) + 1/(x + 2) -> (2x + 3)/(x + 1)(x + 2)
     *      then substitute back 1/(x + 1) + 1/(x + 2) + 1/(x + 3) -> (2x + 3)/(x + 1)(x + 2) + 1/(x + 3)
     */
    SubstituteResultOfTaskSet,
    InlinePartialSum,
    InlinePartialProduct,
    ;

    override val category = "SolverEngine"
}
