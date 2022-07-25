package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.steps.Transformation

private const val MAX_WHILE_POSSIBLE_ITERATIONS = 100

class TooManyIterationsException(msg: String) : RuntimeException(msg)

data class WhilePossible(val plan: Method) : PlanExecutor {

    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation> {
        val steps: MutableList<Transformation> = mutableListOf()

        var lastSub = sub
        repeat(MAX_WHILE_POSSIBLE_ITERATIONS) {
            val lastStep: Transformation = plan.tryExecute(ctx, lastSub) ?: return steps
            steps.add(lastStep)

            val substitution = lastSub.substitute(lastStep.fromExpr.path, lastStep.toExpr)
            lastSub = Subexpression(substitution.expr, sub.parent, lastSub.path)
        }

        throw TooManyIterationsException("WhilePossible max iteration number ($MAX_WHILE_POSSIBLE_ITERATIONS) exceeded")
    }
}
