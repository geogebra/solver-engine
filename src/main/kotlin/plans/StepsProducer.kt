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

data class Deeply(val plan: TransformationProducer, val deepFirst: Boolean = false) : StepsProducer {

    override val pattern = FindPattern(plan.pattern, deepFirst)

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val step = plan.execute(ctx, match, match.getLastBinding(plan.pattern)!!)
        return if (step == null) emptyList() else listOf(step)
    }
}

data class WhilePossible(val plan: TransformationProducer) : StepsProducer {

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

data class PipelineItem(val plan: TransformationProducer, val optional: Boolean = false)

data class Pipeline(val items: List<PipelineItem>) : StepsProducer {
    init {
        require(!items.all { it.optional })
    }

    val firstNonOptionalItemIndex = items.indexOfFirst { !it.optional }
    override val pattern = _getPattern()

    private fun _getPattern(): Pattern {
        if (firstNonOptionalItemIndex == 0) {
            return items[0].plan.pattern
        }
        return OneOfPattern(items.subList(0, firstNonOptionalItemIndex + 1).map { it.plan.pattern })
    }

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for (item in items) {
            val step = item.plan.tryExecute(ctx, lastSub)
            if (step != null) {
                val substitution = lastSub.substitute(step.fromExpr.path, step.toExpr)
                lastSub = Subexpression(lastSub.path, substitution.expr)
                steps.add(step)
            } else if (!item.optional) {
                return emptyList()
            }
        }
        return steps
    }
}

interface InStep : StepsProducer {

    val pipelineItems: List<PipelineItem>
    fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression>

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val stepSubs = getSubexpressions(match, sub).toMutableList()

        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for ((stepPlan, optional) in pipelineItems) {
            val stepTransformations = stepSubs.map { stepPlan.tryExecute(ctx, it) }
            if (!optional && stepTransformations.any { it == null }) {
                return emptyList()
            }
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
    InStep {

    override val pipelineItems = (plan.stepsProducer as Pipeline).items

    override fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression> {
        return sub.children()
    }
}
