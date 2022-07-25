package engine.methods

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.executors.PlanExecutor
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

data class Plan(
    val pattern: Pattern? = null,
    val explanationMaker: MetadataMaker? = null,
    val skillMakers: List<MetadataMaker> = emptyList(),
    val planExecutor: PlanExecutor,
    val planId: MethodId? = null,
) : Method {

    private fun getMatch(sub: Subexpression): Match? {
        return when {
            pattern != null -> pattern.findMatches(sub, RootMatch).firstOrNull()
            else -> RootMatch
        }
    }

    private fun getSteps(ctx: Context, sub: Subexpression): List<Transformation>? {
        val steps = planExecutor.produceSteps(ctx, sub)
        if (steps.isEmpty()) {
            return null
        }
        return steps
    }

    override fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        return getMatch(sub)?.let { match ->
            getSteps(ctx, sub)?.let { steps ->
                val lastStep = steps.last()
                val singletonStep = steps.size == 1 && steps[0].explanation == null && steps[0].skills.isEmpty()
                return Transformation(
                    planId = planId,
                    fromExpr = sub,
                    toExpr = sub.substitute(lastStep.fromExpr.path, lastStep.toExpr),
                    steps = if (singletonStep) lastStep.steps else steps,
                    explanation = explanationMaker?.make(match),
                    skills = skillMakers.map { it.make(match) }
                )
            }
        }
    }
}
