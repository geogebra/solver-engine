package engine.patterns

import engine.context.Context
import engine.expressions.Subexpression

data class FindPattern(val pattern: Pattern, val deepFirst: Boolean = false) : Pattern {

    override val key = pattern

    override fun findMatches(context: Context, match: Match, subexpression: Subexpression): Sequence<Match> {
        val ownMatches = pattern.findMatches(context, match, subexpression)
        val childMatches = subexpression.children().asSequence().flatMap { findMatches(context, match, it) }
        return when {
            deepFirst -> childMatches + ownMatches
            else -> ownMatches + childMatches
        }
    }
}

/**
 * Used to match in a given `Subexpression` object, containing any of
 * the given `Pattern`'s in the given order in the list `options`
 */
data class OneOfPattern(val options: List<Pattern>) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Subexpression): Sequence<Match> {
        return sequence {
            for (option in options) {
                for (m in option.findMatches(context, match, subexpression)) {
                    yield(m.newChild(this@OneOfPattern, m.getLastBinding(option)!!))
                }
            }
        }
    }
}

fun oneOf(vararg options: Pattern) = OneOfPattern(options.asList())
