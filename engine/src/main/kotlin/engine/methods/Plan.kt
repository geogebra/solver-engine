package engine.methods

import engine.context.Context
import engine.expressions.Constants
import engine.expressions.Subexpression
import engine.methods.stepsproducers.StepsProducer
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

/**
 * A `Plan` is a `Method` with a non-empty set of steps which are produced by a `StepsProducer`.
 */
data class Plan(
    val pattern: Pattern,
    val resultPattern: Pattern,
    val explanationMaker: MetadataMaker,
    val skillMakers: List<MetadataMaker> = emptyList(),
    val stepsProducer: StepsProducer,
) : Method {

    private fun getMatch(sub: Subexpression): Match? {
        return pattern.findMatches(sub, RootMatch).firstOrNull()
    }

    override fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        val match = getMatch(sub) ?: return null

        return stepsProducer.produceSteps(ctx, sub)?.let { steps ->
            val toExpr = steps.last().toExpr

            when {
                toExpr.expr == Constants.Undefined || resultPattern.matches(toExpr.expr) -> Transformation(
                    fromExpr = sub,
                    toExpr = toExpr,
                    steps = steps,
                    explanation = explanationMaker.make(match),
                    skills = skillMakers.map { it.make(match) }
                )

                else -> null
            }
        }
    }
}
