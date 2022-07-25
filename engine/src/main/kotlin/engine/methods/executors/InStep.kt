package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.expressions.toMappedExpr
import engine.methods.Method
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

data class InStepItem(
    val method: Method,
    val explanation: MetadataMaker,
    val optional: Boolean,
)

interface InStep : PlanExecutor {

    val inStepItems: List<InStepItem>
    fun getSubexpressions(sub: Subexpression): List<Subexpression>

    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation> {
        val stepSubs = getSubexpressions(sub).toMutableList()

        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for ((stepPlan, explanation, optional) in inStepItems) {
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
                lastSub = Subexpression(substitution.expr, sub.parent, lastSub.path)
            }
            steps.add(
                Transformation(
                    explanation = explanation.make(RootMatch),
                    fromExpr = prevSub,
                    toExpr = lastSub.toMappedExpr(),
                    steps = nonNullTransformations
                )
            )
            for ((i, tr) in stepTransformations.withIndex()) {
                if (tr != null) {
                    stepSubs[i] = Subexpression(tr.toExpr.expr, tr.fromExpr.parent, tr.fromExpr.path)
                }
            }
        }
        return steps
    }
}

data class ApplyToChildrenInStep(
    override val inStepItems: List<InStepItem>
) : InStep {
    override fun getSubexpressions(sub: Subexpression): List<Subexpression> {
        return sub.children()
    }
}
