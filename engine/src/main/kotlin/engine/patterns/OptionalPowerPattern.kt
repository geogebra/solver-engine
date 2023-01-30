package engine.patterns

import engine.expressions.Constants

class OptionalPowerPattern(val base: Pattern) : KeyedPattern {

    private val _exponent = AnyPattern()
    private val ptn = oneOf(powerOf(base, _exponent), base)

    /**
     * This is either the exponent or just a path provider that returns 1 if the pattern wasn't a power.
     */
    val exponent = ProviderWithDefault(_exponent, Constants.One)

    override val key = ptn
}

fun optionalPowerOf(base: Pattern) = OptionalPowerPattern(base)
