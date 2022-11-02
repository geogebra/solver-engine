package methods.constantexpressions

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class ConstantExpressionsExplanation : CategorisedMetadataKey {
    /**
     * Simplify an expression in brackets
     */
    SimplifyExpressionInBrackets,

    /**
     * Simplify roots in the expression, even if they are deep inside it
     *
     * E.g. 5 + 7 * root[12]] -> 5 + 7 * (2 * root[2])
     *
     * Note: it can be roots of any order, not just square roots.
     */
    SimplifyRootsInExpression,
    SimplifyPowers,

    /**
     * Rewrite integer order roots as powers in an expression that also contains rational exponents.
     * This is so that further simplifications of rational exponents can be performed
     *
     * E.g. 5 + sqrt[3] * [4 ^ [1/2]] ->  5 + [3 ^ [1/2]] * [4 ^ [1/2]]
     */
    RewriteIntegerOrderRootsAsPowers,

    /**
     * Simplify an expression containing only constant terms (no variable)
     *
     * This is a public top-level transformation.
     */
    SimplifyConstantExpression;

    override val category = "ConstantExpressions"
}

typealias Explanation = ConstantExpressionsExplanation
