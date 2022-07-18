package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.expressions.toMappedExpr
import engine.methods.Plan
import engine.patterns.AnyPattern
import engine.patterns.Match
import engine.patterns.Pattern
import engine.steps.Transformation

interface InStep : PlanExecutor {

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
            if (nonNullTransformations.isEmpty()) {
                continue
            }

            val prevSub = lastSub
            for (tr in nonNullTransformations) {
                val substitution = lastSub.substitute(tr.fromExpr.path, tr.toExpr)
                lastSub = Subexpression(lastSub.path, substitution.expr)
            }
            steps.add(
                Transformation(
                    fromExpr = prevSub,
                    toExpr = lastSub.toMappedExpr(),
                    steps = nonNullTransformations
                )
            )
            for ((i, tr) in stepTransformations.withIndex()) {
                if (tr != null) {
                    stepSubs[i] = Subexpression(tr.fromExpr.path, tr.toExpr.expr)
                }
            }
        }
        return steps
    }
}

data class ApplyToChildrenInStep(val plan: Plan, override val pattern: Pattern = AnyPattern()) :
    InStep {

    override val pipelineItems = (plan.planExecutor as Pipeline).items

    override fun getSubexpressions(match: Match, sub: Subexpression): List<Subexpression> {
        return sub.children()
    }
}
