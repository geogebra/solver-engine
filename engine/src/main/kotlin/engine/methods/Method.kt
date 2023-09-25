package engine.methods

import engine.context.Context
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsProducer
import engine.steps.Transformation
import java.util.logging.Level
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun interface Method : StepsProducer {
    fun tryExecute(ctx: Context, sub: Expression): Transformation?

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? =
        tryExecute(ctx, sub)?.let { listOf(it) }
}

fun interface Runner {
    fun run(ctx: Context, sub: Expression): Transformation?
}

interface RunnerMethod : Method {
    val name: String
    val runner: Runner

    @OptIn(ExperimentalTime::class)
    override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
        ctx.requireActive()

        if (timeRuns) {
            ctx.log(Level.FINER) { "-> $name: $sub" }
            val (result, duration) = try {
                ctx.nest()
                measureTimedValue { runner.run(ctx, sub) }
            } finally {
                ctx.unnest()
            }
            if (result == null) {
                ctx.log(Level.FINER) { "<- ${duration.inWholeNanoseconds} $name: FAIL" }
            } else {
                ctx.log(Level.FINE) { "<- ${duration.inWholeNanoseconds} $name: ${result.toExpr}" }
            }
            return result
        } else {
            return runner.run(ctx, sub)
        }
    }

    companion object {
        // This is not a very sound way to express the intent that we should not time runs in productions and we should
        // only time runs in development when set the log level to at least DEBUG.
        val timeRuns = System.getenv("LOG_LEVEL")?.let { it == "TRACE" || it == "DEBUG" } ?: false
    }
}

@Target(AnnotationTarget.FIELD)
annotation class PublicMethod(val hiddenFromList: Boolean = false)
