package engine.steps

import engine.expressions.Expression
import engine.steps.Transformation.Tag
import engine.steps.Transformation.Type
import engine.steps.metadata.GmAction
import engine.steps.metadata.Metadata

/**
 * Contains the details of the transformation to a mathematical expression effected by a plan or rule.
 */
data class Transformation(

    /**
     * The transformation type
     */
    val type: Type,

    /**
     * Optional list of [Tag] values that give the user extra information about the transformation.  It can for example
     * indicate that the transformation doesn't change the rendered expression so it would be redundant to show it.
     */
    val tags: List<Tag>? = null,

    /**
     * Starting expression for the transformation (e.g. 1 + 1)
     */
    val fromExpr: Expression,

    /**
     * Resulting expression for the transformation (e.g. 2)
     */
    val toExpr: Expression,

    /**
     * Optional list of steps that would constitute the "step-by-step" workings for this whole transformation.  Only
     * relevant for transformations of type [Type.Plan].
     */
    val steps: List<Transformation>? = null,

    /**
     * Optional list of sub-tasks this transformation can be decomposed into.  Only relevant for transformations of
     * type [Type.TaskSet].
     */
    val tasks: List<Task>? = null,

    /**
     * Optional list of alternative sub-steps for the transformation.  Only relevant for transformations with non-empty
     * [steps].
     */
    val alternatives: List<Alternative>? = null,

    /**
     * The explanation for the transformation, explaining to the user what the transformation does and possibly
     * highlighting key parts of the [fromExpr] involved in the transformation.
     */
    val explanation: Metadata? = null,

    /**
     * An optional list of skills associated with this transformation.
     */
    val skills: List<Metadata>? = null,

    /**
     * Optional GM action associated with this transformation.
     */
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

        /** A transformation that doesn't make any visible change to the LaTeX. Such
         * transformations are not intended to be shown to the user, under any
         * circumstance. It should always be squashed with the next step. */
        InvisibleChange,
    }

    /**
     * Clear all labels contained in the [fromExpr] or [toExpr] of the transformation.
     */
    fun clearLabels() = copy(fromExpr = fromExpr.clearLabels(), toExpr = toExpr.clearLabels())
}
