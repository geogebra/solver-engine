package engine.patterns

import engine.expressions.Subexpression

class IntegerFractionPattern : Pattern {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return fraction.findMatches(subexpression, match)
    }
}
