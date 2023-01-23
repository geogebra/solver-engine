package engine.steps

import engine.expressions.Expression
import engine.steps.metadata.Metadata

/**
 * Contains the details of the transformation to a mathematical expression effected by a plan or rule.
 */
data class Transformation(
    val type: Type,
    val fromExpr: Expression,
    val toExpr: Expression,
    val steps: List<Transformation>? = null,
    val tasks: List<Task>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList()
) {

    enum class Type {
        /**
         * A transformation made of a chain of steps, each step transforming the result of the previous step.
         */
        Plan,

        /**
         * An "atomic" transformation which cannot be decomposed into smaller parts.
         */
        Rule,

        /**
         * A transformation made of different tasks, each working on their own expression.  The last task gives the
         * result of the overall transformation.
         */
        TaskSet,

        /**
         * A transformation that consists of a rearrangement of the expression tree but is not really a mathematical
         * representation.  Such a transformation can be hidden from the user but its path mappings may be useful.
         */
        Rearrangement;
    }

    /**
     * Clear all labels contained in the [fromExpr] or [toExpr] of the transformation.
     */
    fun clearLabels() = copy(fromExpr = fromExpr.clearLabels(), toExpr = toExpr.clearLabels())
}
