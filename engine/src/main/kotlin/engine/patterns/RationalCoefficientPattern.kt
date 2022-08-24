package engine.patterns

import engine.expressionmakers.FixedExpressionMaker
import engine.expressionmakers.copySign
import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.move
import engine.expressions.Constants
import engine.expressions.MappedExpression
import engine.expressions.Subexpression

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
    fun coefficient(match: Match): MappedExpression {
        val numeratorCoefficient = when {
            match.isBound(numerator) -> move(numerator)
            else -> FixedExpressionMaker(Constants.One)
        }

        val coefficient = when {
            match.isBound(denominator) -> makeFractionOf(numeratorCoefficient, move(denominator))
            else -> numeratorCoefficient
        }

        return copySign(ptn, coefficient).make(match)
    }
}

/**
 * Creates a pattern for the given pattern multiplied by a rational coefficient.
 * See RationalCoefficientPattern for details.
 */
fun withRationalCoefficient(pattern: Pattern) = RationalCoefficientPattern(pattern)
