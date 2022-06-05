package plans

import context.Context
import context.Resource
import context.ResourceData
import expressionmakers.ExpressionMaker
import expressions.*
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

interface InStep : TransformationProducer {

    val pipeline: List<TransformationProducer>
    fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression>

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val stepSubs = getSubexpressions(match, sub).toMutableList()

        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for (stepPlan in pipeline) {
            val stepTransformations = stepSubs.map { stepPlan.tryExecute(ctx, it) }
            val nonNullTransformations = stepTransformations.filterNotNull()
            if (nonNullTransformations.isNotEmpty()) {
                val prevSub = lastSub
                for (tr in nonNullTransformations) {
                    val substitution = lastSub.substitute(tr.fromExpr.path, tr.toExpr)
                    lastSub = Subexpression(lastSub.path, substitution.expr)
                }
                steps.add(
                    Transformation(prevSub, lastSub.toMappedExpr(), nonNullTransformations)
                )
                for ((i, tr) in stepTransformations.withIndex()) {
                    if (tr != null) {
                        stepSubs[i] = Subexpression(tr.fromExpr.path, tr.toExpr.expr)
                    }
                }
            }
        }

        return Transformation(
            sub,
            MappedExpression(lastSub.expr, PathMappingLeaf(listOf(lastSub.path), PathMappingType.Move)),
            steps
        )
    }
}

data class ApplyToChildrenInStep(val plan: Plan, override val pattern: Pattern = AnyPattern()) :
    InStep {

    override val pipeline = (plan.stepsProducer as PipelineSP).plans

    override fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression> {
        return sub.children()
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
