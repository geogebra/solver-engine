package engine.steps

import engine.expressions.Expression
import engine.steps.metadata.GmAction
import engine.steps.metadata.Metadata

/**
 * Contains the details of the transformation to a mathematical expression effected by a plan or rule.
 */
data class Transformation(
    val type: Type,
    val tags: List<Tag>? = null,
    val fromExpr: Expression,
    val toExpr: Expression,
    val steps: List<Transformation>? = null,
    val tasks: List<Task>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata>? = null,
    val gmAction: GmAction? = null,
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
         * A transformation made of different tasks, each working on their own expression.
         * The last task gives the result of the overall transformation.
         */
        TaskSet,
    }

    enum class Tag {
        /**
         * A transformation that consists only of the rearrangement of the operands of some
         * commutative operator.
         */
        Rearrangement,

        /**
         * A transformation that changes only the appearance of the expression, e.g. by adding
         * clarifying brackets or explicit product signs.
         */
        Cosmetic,

        /**
         * A transformation with the only purpose to make the solver output more consistent,
         * e.g. by transforming the equation x = 3 into a solution operator.
         */
        Pedantic,
    }

    /**
     * Clear all labels contained in the [fromExpr] or [toExpr] of the transformation.
     */
    fun clearLabels() = copy(fromExpr = fromExpr.clearLabels(), toExpr = toExpr.clearLabels())
}
