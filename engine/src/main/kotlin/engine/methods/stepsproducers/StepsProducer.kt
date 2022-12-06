package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.Root
import engine.operators.UndefinedOperator
import engine.steps.Transformation

/**
 * This is used by plans to provide them with a list of steps.  There are a number of standard `StepsProducer` in this
 * package that can be combined.
 */
interface StepsProducer {
    /**
     * Produces non-empty list of `Transformation` instances or null.
     */
    fun produceSteps(ctx: Context, sub: Expression): List<Transformation>?
}

/**
 * This helps build a list of chained `Transformation` instances, starting from the given Expression`.
 */
class StepsBuilder(sub: Expression) {
    private var sub: Expression

    private val steps = mutableListOf<Transformation>()
    private var aborted = false

    init {
        // Redundant brackets are removed because the outer brackets in the expression serve no
        // useful purpose
        this.sub = when (sub.origin) {
            is Root -> sub
            else -> sub.removeBrackets()
        }
    }

    fun undefined() = sub.operator == UndefinedOperator

    internal fun clearLabels() {
        sub = sub.clearLabels()
        steps.replaceAll { it.clearLabels() }
    }

    fun addStep(step: Transformation) {
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
        val substitution = when (step.toExpr.operator) {
            UndefinedOperator -> sub.substitute(sub, step.toExpr)
            else -> sub.substitute(step.fromExpr, step.toExpr)
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

        sub = substitution.withOrigin(sub.origin)
    }

    /**
     * Adds a list of `Transformation` instances to the `Transformation` chain.
     */
    fun addSteps(newSteps: List<Transformation>?) {
        if (!aborted) {
            newSteps?.forEach { addStep(it) }
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
fun buildSteps(sub: Expression, init: StepsBuilder.() -> Unit): List<Transformation>? {
    val builder = StepsBuilder(sub)
    builder.init()
    return builder.getFinalSteps()
}
