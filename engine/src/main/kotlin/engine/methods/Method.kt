package engine.methods

import engine.context.Context
import engine.expressions.Subexpression
import engine.steps.Transformation

interface Method {
    fun tryExecute(ctx: Context, sub: Subexpression): Transformation?
}
