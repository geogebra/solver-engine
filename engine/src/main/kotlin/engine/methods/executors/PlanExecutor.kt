package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.patterns.Match
import engine.patterns.Pattern
import engine.steps.Transformation

interface PlanExecutor {
    val pattern: Pattern
    fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation>
}
