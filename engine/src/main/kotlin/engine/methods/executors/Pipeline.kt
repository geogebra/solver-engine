package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.patterns.Match
import engine.patterns.OneOfPattern
import engine.patterns.Pattern
import engine.steps.Transformation

data class PipelineItem(val plan: Method, val optional: Boolean = false)

data class Pipeline(val items: List<PipelineItem>) : PlanExecutor {
    init {
        require(!items.all { it.optional })
    }

    override val pattern = calcPattern()

    private fun calcPattern(): Pattern {
        val firstNonOptionalItemIndex = items.indexOfFirst { !it.optional }
        if (firstNonOptionalItemIndex == 0) {
            return items[0].plan.pattern
        }
        return OneOfPattern(items.subList(0, firstNonOptionalItemIndex + 1).map { it.plan.pattern })
    }

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        var started = false
        for (item in items) {
            val step = when {
                !started && match.getLastBinding(item.plan.pattern) == null -> null
                !started -> item.plan.execute(ctx, match, sub)
                else -> item.plan.tryExecute(ctx, lastSub)
            }
            if (step != null) {
                started = true
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
