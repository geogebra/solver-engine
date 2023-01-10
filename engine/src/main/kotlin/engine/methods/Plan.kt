package engine.methods

import engine.context.Context
import engine.expressions.Combine
import engine.expressions.Constants
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsProducer
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

abstract class Plan(
    val specificPlans: List<Method> = emptyList()
) : Method, Runner {

    override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
        ctx.requireActive()

        return run(ctx, sub)?.let {
            Transformation(
                fromExpr = sub,
                toExpr = it.toExpr,
                steps = it.steps,
                explanation = it.explanation,
                skills = it.skills
            )
        }
    }
}

/**
 * A `Plan` is a `Method` with a non-empty set of steps which are produced by a `StepsProducer`.
 */
class RegularPlan(
    val pattern: Pattern,
    val resultPattern: Pattern,
    val explanationMaker: MetadataMaker,
    val skillMakers: List<MetadataMaker> = emptyList(),
    specificPlans: List<Method> = emptyList(),
    val stepsProducer: StepsProducer
) : Plan(specificPlans) {

    override fun run(ctx: Context, sub: Expression): TransformationResult? {
        val match = pattern.findMatches(ctx, RootMatch, sub).firstOrNull() ?: return null

        return stepsProducer.produceSteps(ctx, sub)?.let { steps ->
            val toExpr = steps.last().toExpr.withOrigin(Combine(listOf(sub)))

            when {
                toExpr == Constants.Undefined || resultPattern.matches(ctx, toExpr) -> TransformationResult(
                    toExpr = toExpr,
                    steps = steps,
                    explanation = explanationMaker.make(ctx, match),
                    skills = skillMakers.map { it.make(ctx, match) }
                )

                else -> null
            }
        }
    }
}
