package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression

/**
 * [WhilePossible] will fail if exceeding this number of iterations.
 */
private const val MAX_WHILE_POSSIBLE_ITERATIONS = 100

/**
 * Exception returned by [WhilePossible]::produceSteps() when it exceeds the maximum number of iterations.  It probably
 * means that there is a buggy plan specification (or that the expression it is applied to is very large */
class TooManyIterationsException(msg: String) : RuntimeException(msg)

/**
 * A [StepsProducer] implementation that repeats the same [stepsProducer] until it fails and chains together
 * the results.
 */
data class WhilePossible(val stepsProducer: StepsProducer) : StepsProducer {

    override fun produceSteps(ctx: Context, sub: Expression) = buildSteps(sub) {
        repeat(MAX_WHILE_POSSIBLE_ITERATIONS) {
            val iterationSteps = stepsProducer.produceSteps(ctx, lastSub) ?: return@buildSteps
            addSteps(iterationSteps)
            if (undefined()) {
                return@buildSteps
            }
        }

        throw TooManyIterationsException(
            "WhilePossible max iteration number ($MAX_WHILE_POSSIBLE_ITERATIONS) exceeded for expression $sub",
        )
    }
}
