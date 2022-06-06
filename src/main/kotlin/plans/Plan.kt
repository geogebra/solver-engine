package plans

import context.Context
import context.Resource
import context.ResourceData
import expressionmakers.ExpressionMaker
import expressions.Subexpression
import patterns.*
import steps.Transformation

interface TransformationProducer {
    val pattern: Pattern
    fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation?

    fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        for (match in pattern.findMatches(sub, RootMatch)) {
            return execute(ctx, match, sub)
        }
        return null
    }
}

data class Plan(
    val ownPattern: Pattern? = null,
    val overridePattern: Pattern? = null,
    val explanationMaker: ExpressionMaker? = null,
    val skillMakers: List<ExpressionMaker> = emptyList(),
    val stepsProducer: StepsProducer
) : TransformationProducer {

    override val pattern = overridePattern ?: allOf(ownPattern, stepsProducer.pattern)

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val steps = stepsProducer.produceSteps(ctx, match, sub)

        if (steps.isEmpty()) {
            return null
        }

        if (steps.size == 1 && steps[0].explanation == null && steps[0].skills.isEmpty()) {
            val onlyStep = steps[0]
            return Transformation(
                fromExpr = sub,
                toExpr = sub.substitute(onlyStep.fromExpr.path, onlyStep.toExpr),
                steps = onlyStep.steps,
                explanation = explanationMaker?.makeMappedExpression(match),
                skills = skillMakers.map { it.makeMappedExpression(match) }
            )
        }

        val lastStep = steps.last()
        return Transformation(
            fromExpr = sub,
            toExpr = sub.substitute(lastStep.fromExpr.path, lastStep.toExpr),
            steps = steps,
            explanation = explanationMaker?.makeMappedExpression(match),
            skills = skillMakers.map { it.makeMappedExpression(match) }
        )
    }
}

data class AnnotatedPlan(val plan: TransformationProducer, override val resourceData: ResourceData) : Resource

data class ContextSensitivePlanSelector(
    val alternatives: List<AnnotatedPlan>,
    val default: TransformationProducer,
    override val pattern: Pattern = AnyPattern()
) : TransformationProducer {

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val alternative = ctx.selectBestResource(alternatives.asSequence())?.plan ?: default
        return alternative.tryExecute(ctx, sub)
    }
}
