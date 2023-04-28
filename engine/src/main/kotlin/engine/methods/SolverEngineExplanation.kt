package engine.methods

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class SolverEngineExplanation : CategorisedMetadataKey {
    /**
     * Simplify part of an expression, e.g. add the integers together in a sum
     * E.g. x + 1 + 2 + y -> x + 3 + y
     *
     * This doesn't have to apply to sums, it could be a product as well
     * E.g. 3*7*x -> 21*x
     */
    SimplifyPartialExpression,

    /**
     * As the last step of simplifying a partial expression, substitute the result back into the parent expression.
     * E.g. x + 1 + 2 + y.
     * - In a previous task the partial expression 1 + 2 is simplified to 3;
     * - now 3 is substituted back into the expression, resulting in x + 3 + y.
     */
    SubstitutePartialExpression,

    /** This should never be shown to the user, because it should only be on hidden steps.
     * It is used when we extract part of a sum to focus on just one part of it. */
    ExtractPartialSum,

    /** Commute one of the fractions so that it is close to the other fraction that we will
     * soon add it to.
     * E.g. [1/3] + 3 + [1/2] -> [1/3] + [1/2] + 3
     */
    CommuteFractionNextToTheOtherFraction,
    ;

    override val category = "SolverEngine"
}
