package engine.patterns

class FractionPattern : KeyedPattern {
    val numerator = AnyPattern()
    val denominator = AnyPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key
}

class IntegerFractionPattern : KeyedPattern {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key
}

class RationalPattern : KeyedPattern {

    private val numerator = UnsignedIntegerPattern()
    private val denominator = UnsignedIntegerPattern()

    private val options = oneOf(
        numerator,
        fractionOf(numerator, denominator),
    )

    private val ptn = optionalNegOf(options)

    override val key = ptn.key
}
