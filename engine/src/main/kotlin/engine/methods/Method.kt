package engine.methods

import engine.context.Context
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsProducer
import engine.steps.Task
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
    val type: Transformation.Type,
    val toExpr: Expression,
    val steps: List<Transformation>? = null,
    val tasks: List<Task>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList()
)

@Suppress("LongParameterList")
fun ruleResult(
    toExpr: Expression,
    steps: List<Transformation>? = null,
    tasks: List<Task>? = null,
    explanation: Metadata? = null,
    skills: List<Metadata> = emptyList(),
    type: Transformation.Type = Transformation.Type.Rule
) = TransformationResult(type, toExpr, steps, tasks, explanation, skills)

interface RunnerMethod : Method {
    val name: String
    val runner: Runner

    override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
        ctx.log(Level.FINER) { "Entering $name with input $sub" }

        ctx.requireActive()

        return runner.run(ctx, sub)?.let {
            ctx.log(Level.FINE) { "Changed at $name from $sub to ${it.toExpr}" }
            Transformation(
                type = it.type,
                fromExpr = sub,
                toExpr = it.toExpr,
                steps = it.steps,
                tasks = it.tasks,
                explanation = it.explanation,
                skills = it.skills
            )
        }
    }
}

@Target(AnnotationTarget.FIELD)
annotation class PublicMethod
