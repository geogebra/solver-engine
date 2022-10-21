package engine.patterns

import engine.expressions.Subexpression

class FractionPattern : Pattern {
    val numerator = AnyPattern()
    val denominator = AnyPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return fraction.findMatches(subexpression, match)
    }
}

class IntegerFractionPattern : Pattern {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return fraction.findMatches(subexpression, match)
    }
}

class RationalPattern : Pattern {

    private val numerator = UnsignedIntegerPattern()
    private val denominator = UnsignedIntegerPattern()

    private val options = oneOf(
        numerator,
        fractionOf(numerator, denominator)
    )

    private val ptn = optionalNegOf(options)

    override val key = ptn.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return ptn.findMatches(subexpression, match)
    }
}
