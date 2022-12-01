package engine.patterns

import engine.context.emptyContext
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.fractionOf
import java.math.BigInteger

abstract class CoefficientPattern(val value: Pattern) : KeyedPattern() {
    abstract fun coefficient(match: Match): Expression
}

/**
 * A pattern matching `value` multiplied by an integer coefficient.
 * Say `value` matches x, the pattern can then match
 *      x
 *      3 * x
 */
class IntegerCoefficientPattern(value: Pattern) : CoefficientPattern(value) {

    private val coefficientPattern = UnsignedIntegerPattern()

    private val options = oneOf(
        value,
        productOf(coefficientPattern, value),
    )

    override val key = options

    val integerCoefficient = IntegerProviderWithDefault(coefficientPattern, BigInteger.ONE)

    override fun coefficient(match: Match): Expression =
        with(MappedExpressionBuilder(emptyContext, match)) { move(integerCoefficient) }
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
class RationalCoefficientPattern(value: Pattern) : CoefficientPattern(value) {

    private val numerator = UnsignedIntegerPattern()
    private val denominator = UnsignedIntegerPattern()

    private val options = oneOf(
        value,
        productOf(numerator, value),
        fractionOf(oneOf(value, productOf(numerator, value)), denominator),
        productOf(fractionOf(numerator, denominator), value)
    )

    private val ptn = optionalNegOf(options)

    override val key = ptn.key

    /**
     * Given a match, returns the coefficient as an integer or fraction
     */
    override fun coefficient(match: Match): Expression =
        with(MappedExpressionBuilder(emptyContext /* TODO */, match)) {
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
 * A pattern matching [value] multiplied by a constant (possibly rational) coefficient.
 * Say [value] matches x, the pattern can then match:
 *     sqrt[3] * x
 *     [2/3 * root[5, 7]] * x
 *     [4*sqrt[3]*x/5*root[3, 3] + 1]
 *     [x/10 - sqrt[7]]
 *     any of the above with a negative sign in front.
 * But it will not match expression where the coefficient is not constant, such as:
 *     y * x
 *     [2/3 * y] * x
 *
 *     It is possible to restrict matching to expressions not starting with a negative sign by setting [positiveOnly] to
 *     true.
 */
class ConstantCoefficientPattern(
    value: Pattern,
    private val positiveOnly: Boolean = false
) : CoefficientPattern(value) {

    private val product = productContaining(value)

    private val numerator = oneOf(
        value,
        ConditionPattern(product) { match ->
            product.getRestSubexpressions(match).all { it.isConstant() }
        }
    )
    private val denominator = condition(AnyPattern()) { it.isConstant() }

    private val options = oneOf(
        numerator,
        fractionOf(numerator, denominator)
    )

    private val optionalNegPtn = optionalNegOf(options)
    private val ptn = if (positiveOnly) options else optionalNegPtn

    override val key = ptn.key

    /**
     * Given a match, returns the coefficient
     */
    override fun coefficient(match: Match): Expression =
        with(MappedExpressionBuilder(emptyContext /* TODO */, match)) {
            val numeratorCoefficient = when {
                match.isBound(product) -> restOf(product)
                else -> introduce(Constants.One)
            }

            val coefficient = when {
                match.isBound(denominator) -> fractionOf(numeratorCoefficient, move(denominator))
                else -> numeratorCoefficient
            }

            if (!positiveOnly) copySign(optionalNegPtn, coefficient) else coefficient
        }
}

/**
 * Creates a pattern for the given pattern optionally multiplied by an integer
 * coefficient. See [IntegerCoefficientPattern] for details.
 */
fun withOptionalIntegerCoefficient(pattern: Pattern) = IntegerCoefficientPattern(pattern)

/**
 * Creates a pattern for the given pattern optionally multiplied by a rational
 * coefficient. See [RationalCoefficientPattern] for details.
 */
fun withOptionalRationalCoefficient(pattern: Pattern) = RationalCoefficientPattern(pattern)

/**
 * Creates a pattern which matches the given variable optionally multiplied
 * by a constant coefficient. See [ConstantCoefficientPattern] for details.
 */
fun withOptionalConstantCoefficient(variable: Pattern, positiveOnly: Boolean = false) =
    ConstantCoefficientPattern(variable, positiveOnly)
