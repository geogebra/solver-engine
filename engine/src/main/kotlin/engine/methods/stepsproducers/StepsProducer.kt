package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Subexpression
import engine.expressions.UndefinedOperator
import engine.steps.Transformation

/**
 * This is used by plans to provide them with a list of steps.  There are a number of standard `StepsProducer` in this
 * package that can be combined.
 */
interface StepsProducer {
    /**
     * Produces non-empty list of `Transformation` instances or null.
     */
    fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation>?
}

/**
 * This helps build a list of chained `Transformation` instances, starting from the given `Subexpression`.
 */
class StepsBuilder(private var sub: Subexpression) {
    private val steps = mutableListOf<Transformation>()
    private var aborted = false

    fun undefined() = sub.expr.operator == UndefinedOperator

    private fun add(step: Transformation) {
        if (undefined()) {
            return
        }

        /**
         * If `step` results in `undefined` for a subexpression of the current
         * working expression, the execution of the plan is halted and the
         * result of the current plan is also `undefined`.
         * For ex:
         * [1 / 1 - 1] + 2
         * --> [1 / 0] + 2
         * --> undefined ([1/ 0] is undefined)
         */
        val (substitution, newSub) = when (step.toExpr.expr.operator) {
            UndefinedOperator -> sub.substitute(sub.path, step.toExpr)
            else -> sub.substitute(step.fromExpr.path, step.toExpr)
        }

        steps.add(
            Transformation(
                fromExpr = sub,
                toExpr = substitution,
                explanation = step.explanation,
                skills = step.skills,
                steps = step.steps
            )
        )

        sub = newSub
    }

    /**
     * Adds a list of `Transformation` instances to the `Transformation` chain.
     */
    fun addSteps(newSteps: List<Transformation>?) {
        if (!aborted) {
            newSteps?.forEach { add(it) }
        }
    }

    /**
     * Aborts the `Transformation` chain.  Any further call to `getFinalSteps()` will return null and any further call
     * to `addSteps()` has no effect.
     */
    fun abort() {
        aborted = true
    }

    /**
     * This is the value the initial substitution was turned into after applying all the currently added
     * `Transformation` instances.
     */
    val lastSub get() = sub

    /**
     * Returns the list of steps added to the builder, or null if `abort()` was called at least once.
     */
    fun getFinalSteps() = if (aborted || steps.isEmpty()) null else steps.toList()
}

/**
 * Provides a convenient way to use a `StepsBuilder`.
 */
fun buildSteps(sub: Subexpression, init: StepsBuilder.() -> Unit): List<Transformation>? {
    val builder = StepsBuilder(sub)
    builder.init()
    return builder.getFinalSteps()
}
