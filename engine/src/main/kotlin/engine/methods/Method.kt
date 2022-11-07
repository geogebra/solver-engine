package engine.methods

import engine.context.Context
import engine.expressions.MappedExpression
import engine.expressions.Subexpression
import engine.methods.stepsproducers.StepsProducer
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import java.util.logging.Level

interface Method : StepsProducer {
    fun tryExecute(ctx: Context, sub: Subexpression): Transformation?

    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation>? =
        tryExecute(ctx, sub)?.let { listOf(it) }
}

fun interface Runner {
    fun run(ctx: Context, sub: Subexpression): TransformationResult?
}

data class TransformationResult(
    val toExpr: MappedExpression,
    val steps: List<Transformation>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList(),
)

interface RunnerMethod : Method {
    val name: String
    val runner: Runner

    override fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        ctx.log(Level.FINER) { "Entering $name with input ${sub.expr}" }

        return runner.run(ctx, sub)?.let {
            ctx.log(Level.FINE) { "Changed at $name from ${sub.expr} to ${it.toExpr.expr}" }
            Transformation(
                fromExpr = sub,
                toExpr = sub.wrapInBracketsForParent(it.toExpr),
                steps = it.steps,
                explanation = it.explanation,
                skills = it.skills,
            )
        }
    }
}

@Target(AnnotationTarget.FIELD)
annotation class PublicMethod
