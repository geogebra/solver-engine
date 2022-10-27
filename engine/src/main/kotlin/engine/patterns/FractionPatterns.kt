package engine.patterns

import engine.context.Context
import engine.expressions.Subexpression

class FractionPattern : Pattern {
    val numerator = AnyPattern()
    val denominator = AnyPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key

    override fun findMatches(context: Context, match: Match, subexpression: Subexpression) =
        fraction.findMatches(context, match, subexpression)
}

class IntegerFractionPattern : Pattern {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key

    override fun findMatches(context: Context, match: Match, subexpression: Subexpression) =
        fraction.findMatches(context, match, subexpression)
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

    override fun findMatches(context: Context, match: Match, subexpression: Subexpression) =
        ptn.findMatches(context, match, subexpression)
}
