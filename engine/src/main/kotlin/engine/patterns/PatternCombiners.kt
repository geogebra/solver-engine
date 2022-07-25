package engine.patterns

import engine.expressions.Subexpression

data class FindPattern(val pattern: Pattern, val deepFirst: Boolean = false) : Pattern {

    override val key = pattern

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        val ownMatches = pattern.findMatches(subexpression, match)
        val childMatches = subexpression.children().asSequence().flatMap { findMatches(it, match) }
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
data class OneOfPattern(val options: List<Pattern>) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return sequence {
            for (option in options) {
                for (m in option.findMatches(subexpression, match)) {
                    yield(m.newChild(this@OneOfPattern, m.getLastBinding(option)!!))
                }
            }
        }
    }
}

/**
 * Used to find matches in a given `Subexpression` object, containing all
 * the `Pattern`'s in the given order in the list `patterns`.
 */
data class AllOfPattern(val patterns: List<Pattern>) : Pattern {
    init {
        require(patterns.isNotEmpty())
    }

    override val key = patterns[0]

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        fun rec(i: Int, m: Match): Sequence<Match> {
            if (i == patterns.size - 1) {
                return patterns[i].findMatches(subexpression, m)
            }
            return patterns[i].findMatches(subexpression, m).flatMap { rec(i + 1, it) }
        }

        return rec(0, match)
    }
}

fun oneOf(vararg options: Pattern) = OneOfPattern(options.asList())

fun allOf(vararg patterns: Pattern?): Pattern {
    val nonNullPatterns = patterns.filterNotNull()
    return when (nonNullPatterns.size) {
        0 -> throw java.lang.IllegalArgumentException("At least one non-null pattern should be specified in allOf")
        1 -> nonNullPatterns[0]
        else -> AllOfPattern(nonNullPatterns)
    }
}
