package plans

import context.Context
import expressions.Subexpression
import patterns.FindPattern
import patterns.Match
import patterns.OneOfPattern
import patterns.Pattern
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

data class FirstOfSP(val options: List<TransformationProducer>) : StepsProducer {

    override val pattern = OneOfPattern(options.map { it.pattern })

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        return options.asSequence()
            .filter { match.getLastBinding(it.pattern) != null }
            .map { it.execute(ctx, match, sub) }
            .filterNotNull()
            .map { listOf(it) }
            .firstOrNull() ?: emptyList()
    }
}