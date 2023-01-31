package engine.patterns

import java.math.BigInteger

/**
 * Monomial pattern, i.e. [x ^ n] for n non-negative integer or x where x is the [base],
 * possibly with a constant coefficient.
 */
class MonomialPattern(val base: Pattern, positiveOnly: Boolean = false) : KeyedPattern {
    private val exponentPattern = UnsignedIntegerPattern()

    val powerPattern = oneOf(base, powerOf(base, exponentPattern))

    private val ptn = withOptionalConstantCoefficient(powerPattern, positiveOnly)

    val exponent: IntegerProvider = IntegerProviderWithDefault(exponentPattern, BigInteger.ONE)

    override val key = ptn.key

    fun getPower(match: Match) = powerPattern.getBoundExpr(match)

    fun coefficient(match: Match) = ptn.coefficient(match)
}
