package engine.patterns

import engine.context.Context
import engine.expressions.Expression

data class FindPattern(
    val pattern: Pattern,
    val deepFirst: Boolean = false,
    val stopWhenFound: Boolean = false,
) : Pattern {

    override val key = pattern

    override fun findMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (subexpression.depth < minDepth) {
            return emptySequence()
        }
        val ownMatches = pattern.findMatches(context, match, subexpression)
        val childMatches = subexpression.children.asSequence().flatMap { findMatches(context, match, it) }
        return when {
            deepFirst -> childMatches + ownMatches
            stopWhenFound -> ownMatches.ifEmpty { childMatches }
            else -> ownMatches + childMatches
        }
    }

    override val minDepth = pattern.minDepth
}

/**
 * Used to match in a given `Subexpression` object, containing any of
 * the given `Pattern`'s in the given order in the list `options`
 */
data class OneOfPattern(val options: List<Pattern>) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        val m = match.newChild(this, subexpression)
        return options.asSequence().flatMap { option -> option.findMatches(context, m, subexpression) }
    }

    override val minDepth = options.minOf { it.minDepth }
}

fun oneOf(vararg options: Pattern) = OneOfPattern(options.asList())
