package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.patterns.Match
import engine.steps.Transformation

data class WhilePossible(val plan: Method) : PlanExecutor {

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
