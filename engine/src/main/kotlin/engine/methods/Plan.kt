package engine.methods

import engine.context.Context
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
    val pattern: Pattern? = null,
    val explanationMaker: MetadataMaker? = null,
    val skillMakers: List<MetadataMaker> = emptyList(),
    val stepsProducer: StepsProducer,
) : Method {

    private fun getMatch(sub: Subexpression): Match? {
        return when {
            pattern != null -> pattern.findMatches(sub, RootMatch).firstOrNull()
            else -> RootMatch
        }
    }

    override fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        return getMatch(sub)?.let { match ->
            stepsProducer.produceSteps(ctx, sub)?.let { steps ->
                val lastStep = steps.last()
                return Transformation(
                    fromExpr = sub,
                    toExpr = lastStep.toExpr,
                    steps = steps,
                    explanation = explanationMaker?.make(match),
                    skills = skillMakers.map { it.make(match) }
                )
            }
        }
    }
}
