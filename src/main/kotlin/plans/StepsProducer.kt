package plans

import context.Context
import expressions.Subexpression
import expressions.toMappedExpr
import patterns.*
import steps.Transformation

interface StepsProducer {
    val pattern: Pattern
    fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation>
}

data class DeeplySP(val plan: TransformationProducer, val deepFirst: Boolean = false) : StepsProducer {

    override val pattern = FindPattern(plan.pattern, deepFirst)

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val step = plan.execute(ctx, match, match.getLastBinding(plan.pattern)!!)
        return if (step == null) emptyList() else listOf(step)
    }
}

data class WhilePossibleSP(val plan: TransformationProducer) : StepsProducer {

    override val pattern = plan.pattern

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        var lastStep: Transformation? = plan.execute(ctx, match, sub)
        var lastSub = sub
        val steps: MutableList<Transformation> = mutableListOf()
        while (lastStep != null) {
            steps.add(lastStep)
            val substitution = lastSub.substitute(lastStep.fromExpr.path, lastStep.toExpr)
            lastSub = Subexpression(lastSub.path, substitution.expr)
            lastStep = plan.tryExecute(ctx, lastSub)
        }
        return steps
    }
}

data class PipelineSP(val plans: List<TransformationProducer>) : StepsProducer {
    init {
        require(plans.isNotEmpty())
    }

    override val pattern = plans[0].pattern

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for (plan in plans) {
            val step = plan.tryExecute(ctx, lastSub)
            if (step != null) {
                val substitution = lastSub.substitute(step.fromExpr.path, step.toExpr)
                lastSub = Subexpression(lastSub.path, substitution.expr)
                steps.add(step)
            }
        }
        return steps
    }
}

data class FirstOfSP(val options: List<TransformationProducer>) : StepsProducer, TransformationProducer {

    override val pattern = OneOfPattern(options.map { it.pattern })

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        for (option in options) {
            if (match.getLastBinding(option.pattern) != null) {
                val result = option.execute(ctx, match, sub)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val trans = execute(ctx, match, sub)
        return if (trans == null) emptyList() else listOf(trans)
    }
}

interface InStepSP : StepsProducer {

    val pipeline: List<TransformationProducer>
    fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression>

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
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
        return steps
    }
}

data class ApplyToChildrenInStep(val plan: Plan, override val pattern: Pattern = AnyPattern()) :
    InStepSP {

    override val pipeline = (plan.stepsProducer as PipelineSP).plans

    override fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression> {
        return sub.children()
    }
}
