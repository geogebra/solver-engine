package engine.patterns

import java.math.BigInteger

/**
 * Monomial pattern, i.e. [x ^ n] for n non-negative integer or x where x is the [base],
 * possibly with a constant coefficient.
 */
class MonomialPattern<T : CoefficientPattern> internal constructor(
    val base: Pattern,
    withCoefficient: (Pattern) -> T,
) : KeyedPattern {
    private val exponentPattern = UnsignedIntegerPattern()

    val powerPattern = oneOf(base, powerOf(base, exponentPattern))

    val ptn = withCoefficient(powerPattern)

    val exponent: IntegerProvider = IntegerProviderWithDefault(exponentPattern, BigInteger.ONE)

    override val key = ptn.key

    fun getPower(match: Match) = powerPattern.getBoundExpr(match)

    fun coefficient(match: Match) = ptn.coefficient(match)
}

fun monomialPattern(base: Pattern, positiveOnly: Boolean = false) =
    MonomialPattern(base) { withOptionalConstantCoefficient(it, positiveOnly = positiveOnly) }

fun rationalMonomialPattern(base: Pattern, positiveOnly: Boolean = false) =
    MonomialPattern(base) { withOptionalRationalCoefficient(it, positiveOnly) }

fun integerMonomialPattern(base: Pattern) = MonomialPattern(base) { withOptionalIntegerCoefficient(it) }

fun monomialPattern(spec: PolynomialSpecification, positiveOnly: Boolean = false) =
    MonomialPattern(spec.variable) { withOptionalConstantCoefficient(it, spec.constantChecker, positiveOnly) }
