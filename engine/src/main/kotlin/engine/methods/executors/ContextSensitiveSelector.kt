package engine.methods.executors

import engine.context.Context
import engine.context.Resource
import engine.context.ResourceData
import engine.expressions.Subexpression
import engine.patterns.Match
import engine.steps.Transformation

/**
 * Data structure that contains a plan executor and ResourceData that allows
 * to decide whether this executor should be selected in a particular context.
 */
data class ContextSensitiveAlternative(
    val planExecutor: PlanExecutor,
    override val resourceData: ResourceData
) : Resource

/**
 * This PlanExecutor has a default implementation and a set of alternative
 * implementations which are context-sensitive.  When producing steps, It
 * selects the most appropriate option from the available ones.
 *
 * The alternative plan executors should successfully match the same
 * expressions as the default one.  If that is not the case produceSteps
 * may result in an IllegalStateException.
 */
data class ContextSensitiveSelector(
    val default: ContextSensitiveAlternative,
    val alternatives: List<ContextSensitiveAlternative>
) : PlanExecutor {

    override val pattern = default.planExecutor.pattern

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val best = ctx.selectBestResource(default, alternatives).planExecutor
        if (best === default.planExecutor) {
            return best.produceSteps(ctx, match, sub)
        } else {
            // Hack to bind the pattern
            for (submatch in best.pattern.findMatches(sub, match)) {
                return best.produceSteps(ctx, submatch, sub)
            }
            throw (IllegalStateException("alternative steps producer does not match"))
        }
    }
}
