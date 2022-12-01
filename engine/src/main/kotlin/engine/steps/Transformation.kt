package engine.steps

import engine.expressions.Expression
import engine.steps.metadata.Metadata

/**
 * Contains the details of the transformation to a mathematical expression effected by a plan or rule.
 */
data class Transformation(
    val fromExpr: Expression,
    val toExpr: Expression,
    val steps: List<Transformation>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList(),
) {

    /**
     * Clear all labels contained in the [fromExpr] or [toExpr] of the transformation.
     */
    fun clearLabels() = Transformation(
        fromExpr.clearLabels(),
        toExpr.clearLabels(),
        steps,
        explanation,
        skills,
    )
}
