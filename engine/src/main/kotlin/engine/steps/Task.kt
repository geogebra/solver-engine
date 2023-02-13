package engine.steps

import engine.expressions.Expression
import engine.expressions.Root
import engine.expressions.RootPath
import engine.steps.metadata.Metadata

/**
 * Contains the information for a task, which is part of a task set.
 */
data class Task(
    /**
     * Identifies the task within the task set.
     */
    val taskId: String,
    /**
     * Expression that will be the fromExpr of the transformation.
     */
    val startExpr: Expression,
    /**
     * An explanation for what the task does in the context of its task set.
     */
    val explanation: Metadata? = null,
    /**
     * Sequence of steps that operate on [startExpr].  It can be empty.
     */
    val steps: List<Transformation> = emptyList(),
    /**
     * Possibly empty list of ids of other tasks in the same task set this task depends on
     */
    val dependsOn: List<String> = emptyList(),
) {
    /**
     * Thinking of the task as a transformation, this would be its toExpr.  Only for internal use.
     */
    internal val toExpr get() = steps.lastOrNull()?.toExpr ?: startExpr

    /**
     * This makes the outcome of the task accessible to implementors of methods, with correct origin.
     */
    val result get() = toExpr.withOrigin(Root(rootPath))

    val rootPath get() = RootPath(taskId)
}
