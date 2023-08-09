package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.LabelSpace
import engine.methods.Strategy
import engine.operators.UndefinedOperator
import engine.steps.Alternative
import engine.steps.Transformation
import java.util.logging.Level

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
class StepsBuilder(val context: Context, private var sub: Expression) {

    private enum class Status {
        InProgress,
        Succeeded,
        Aborted,
    }

    private var steps = mutableListOf<Transformation>()

    private var status = Status.InProgress

    private val alternatives = mutableListOf<Alternative>()

    val inProgress get() = status == Status.InProgress

    fun copy(): StepsBuilder {
        val builder = StepsBuilder(context, sub)
        builder.steps = steps.toMutableList()
        builder.status = status
        return builder
    }

    fun undefined() = sub.operator == UndefinedOperator

    internal fun clearLabels(labelSpace: LabelSpace) {
        sub = sub.clearLabels(labelSpace)
        steps.replaceAll { it.clearLabels(labelSpace) }
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
            UndefinedOperator -> step.toExpr
            else -> sub.substitute(step.fromExpr, step.toExpr)
        }

        steps.add(
            when {
                substitution === step.toExpr && step.toExpr.operator != UndefinedOperator -> {
                    step
                }
                else -> {
                    step.copy(fromExpr = sub, toExpr = substitution)
                }
            },
        )

        val prevIndex = steps.indexOfFirst { it.fromExpr == substitution }
        if (prevIndex != -1) {
            context.log(Level.WARNING, "Circular steps detected (see details below)")
            for (prevStep in steps.subList(prevIndex, steps.size)) {
                context.log(
                    Level.INFO,
                    "${prevStep.explanation?.key?.keyName}: ${prevStep.fromExpr} --> ${prevStep.toExpr}",
                )
            }
            steps = steps.subList(0, prevIndex)
        }

        sub = substitution.withOrigin(sub.origin)
    }

    /**
     * Adds a list of `Transformation` instances to the `Transformation` chain.
     */
    fun addSteps(newSteps: List<Transformation>) {
        if (inProgress) {
            newSteps.forEach { addStep(it) }
        }
    }

    fun addAlternative(strategy: Strategy, steps: List<Transformation>) {
        if (inProgress) {
            val alternativeBuilder = copy()
            alternativeBuilder.addSteps(steps)
            alternatives.add(Alternative(strategy, alternativeBuilder.steps))
        }
    }

    /**
     * Aborts the `Transformation` chain.  Any further call to `getFinalSteps()` will return null and any further call
     * to `addSteps()` has no effect.
     */
    fun abort() {
        status = Status.Aborted
    }

    fun succeed() {
        status = Status.Succeeded
    }

    /**
     * This is the value the initial substitution was turned into after applying all the currently added
     * `Transformation` instances.
     */
    val lastSub get() = sub

    /**
     * Returns the list of steps added to the builder, or null if `abort()` was called at least once.
     */
    fun getFinalSteps(): List<Transformation>? = if (status == Status.Aborted || steps.isEmpty()) null else steps

    fun getAlternatives(): List<Alternative> = alternatives
}

/**
 * Provides a convenient way to use a `StepsBuilder`.
 */
fun buildSteps(ctx: Context, sub: Expression, init: StepsBuilder.() -> Unit): List<Transformation>? {
    val builder = StepsBuilder(ctx, sub)
    builder.init()
    return builder.getFinalSteps()
}
