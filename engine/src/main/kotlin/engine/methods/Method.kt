package engine.methods

import engine.context.Context
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsProducer
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import java.util.logging.Level

fun interface Method : StepsProducer {
    fun tryExecute(ctx: Context, sub: Expression): Transformation?

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? =
        tryExecute(ctx, sub)?.let { listOf(it) }
}

fun interface Runner {
    fun run(ctx: Context, sub: Expression): TransformationResult?
}

data class TransformationResult(
    val toExpr: Expression,
    val steps: List<Transformation>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList(),
)

interface RunnerMethod : Method {
    val name: String
    val runner: Runner

    override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
        ctx.log(Level.FINER) { "Entering $name with input $sub" }

        return runner.run(ctx, sub)?.let {
            ctx.log(Level.FINE) { "Changed at $name from $sub to ${it.toExpr}" }
            Transformation(
                fromExpr = sub,
                toExpr = it.toExpr,
                steps = it.steps,
                explanation = it.explanation,
                skills = it.skills,
            )
        }
    }
}

@Target(AnnotationTarget.FIELD)
annotation class PublicMethod
