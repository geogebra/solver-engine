package engine.methods

import engine.context.Context
import engine.expressions.Subexpression
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.allOf
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

data class Plan(
    val ownPattern: Pattern? = null,
    val explanationMaker: MetadataMaker? = null,
    val skillMakers: List<MetadataMaker> = emptyList(),
    val stepsProducer: StepsProducer,
    val planId: MethodId? = null,
) : Method {

    override val pattern = allOf(ownPattern, stepsProducer.pattern)

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val steps = stepsProducer.produceSteps(ctx, match, sub)

        if (steps.isEmpty()) {
            return null
        }

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
