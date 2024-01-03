package engine.patterns

import engine.expressions.Constants
import java.math.BigInteger

class OptionalPowerPattern(val base: Pattern, exponent: Pattern) : KeyedPattern {
    private val ptn = oneOf(powerOf(base, exponent), base)

    /**
     * This is either the exponent or just a path provider that returns 1 if the pattern wasn't a power.
     */
    val exponent = ProviderWithDefault(exponent, Constants.One)

    override val key = ptn
}

class OptionalIntegerPowerPattern(val base: Pattern, exponent: IntegerPattern) : KeyedPattern {
    private val ptn = oneOf(powerOf(base, exponent), base)

    /**
     * This is either the exponent or just a path provider that returns 1 if the pattern wasn't a power.
     */
    val exponent = IntegerProviderWithDefault(exponent, BigInteger.ONE)

    override val key = ptn
}

fun optionalPowerOf(base: Pattern, exponent: Pattern = AnyPattern()) = OptionalPowerPattern(base, exponent)

fun optionalIntegerPowerOf(base: Pattern, exponent: IntegerPattern = UnsignedIntegerPattern()) =
    OptionalIntegerPowerPattern(
        base,
        exponent,
    )
