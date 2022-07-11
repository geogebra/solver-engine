package engine.methods

import engine.context.Context
import engine.context.Resource
import engine.context.ResourceData
import engine.expressions.Subexpression
import engine.patterns.Match
import engine.steps.Transformation

data class ContextSensitiveMethod(
    val method: Method,
    override val resourceData: ResourceData
) : Resource

data class ContextSensitiveMethodSelector(
    val default: ContextSensitiveMethod,
    val alternatives: List<ContextSensitiveMethod>
) : Method {

    override val pattern = default.method.pattern

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val alternative = ctx.selectBestResource(alternatives.asSequence()) ?: default
        return alternative.method.tryExecute(ctx, sub)
    }
}
