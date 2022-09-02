package engine.patterns

import engine.expressionmakers.MakerBuilder
import engine.expressions.Constants
import engine.expressions.MappedExpression
import engine.expressions.Subexpression
import engine.expressions.fractionOf

/**
 * A pattern matching `value` multiplied by an integer coefficient.
 * Say `value` matches x, the pattern can then match
 *      x
 *      3 * x
 */
class IntegerCoefficientPattern(value: Pattern) : Pattern {

    private val coefficient = UnsignedIntegerPattern()

    private val options = oneOf(
        value,
        productOf(coefficient, value),
    )

    override val key = options

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return options.findMatches(subexpression, match)
    }

    fun coefficient(match: Match): MappedExpression = with(MakerBuilder(match)) {
        when {
            match.isBound(coefficient) -> move(coefficient)
            else -> introduce(Constants.One)
        }
    }
}

/**
 * A pattern matching `value` multiplied by a rational coefficient.
 * Say `value` matches x, the pattern can then match
 *     2 * x
 *     [2/3] * x
 *     [4*x/5]
 *     [x/10]
 *     any of the above with a negative sign in front.
 */
class RationalCoefficientPattern(value: Pattern) : Pattern {

    private val numerator = UnsignedIntegerPattern()
    private val denominator = UnsignedIntegerPattern()

    private val options = oneOf(
        value,
        productOf(numerator, value),
        fractionOf(oneOf(value, productOf(numerator, value)), denominator),
        productOf(fractionOf(numerator, denominator), value)
    )

    private val ptn = optionalNegOf(options)

    override val key = ptn

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return ptn.findMatches(subexpression, match)
    }

    /**
     * Given a match, returns the coefficient as an integer or fraction
     */
    fun coefficient(match: Match): MappedExpression = with(MakerBuilder(match)) {
        val numeratorCoefficient = when {
            match.isBound(numerator) -> move(numerator)
            else -> introduce(Constants.One)
        }

        val coefficient = when {
            match.isBound(denominator) -> fractionOf(numeratorCoefficient, move(denominator))
            else -> numeratorCoefficient
        }

        copySign(ptn, coefficient)
    }
}

/**
 * Creates a pattern for the given pattern optionally multiplied by an integer
 * coefficient. See IntegerCoefficientPattern for details.
 */
fun withOptionalIntegerCoefficient(pattern: Pattern) = IntegerCoefficientPattern(pattern)

/**
 * Creates a pattern for the given pattern optionally multiplied by a rational
 * coefficient. See RationalCoefficientPattern for details.
 */
fun withOptionalRationalCoefficient(pattern: Pattern) = RationalCoefficientPattern(pattern)
