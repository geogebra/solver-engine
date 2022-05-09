package transformations

import expressionmakers.*
import patterns.*

object SplitMixedNumber : Rule {
    private val integer = IntegerPattern()
    private val numerator = IntegerPattern()
    private val denominator = IntegerPattern()

    override val pattern = MixedNumberPattern(integer, numerator, denominator)
    override val resultMaker = makeSumOf(move(integer), makeFractionOf(move(numerator), move(denominator)))
}

object FractionToMixedNumber : Rule {
    private val numerator = IntegerPattern()
    private val denominator = IntegerPattern()

    private val isImproperFraction = numericCondition(numerator, denominator) { n, d -> n > d }

    override val pattern = ConditionPattern(fractionOf(numerator, denominator), isImproperFraction)

    override val resultMaker = MixedNumberMaker(
        makeNumericOp(numerator, denominator) { n, d -> n / d },
        makeNumericOp(numerator, denominator) { n, d -> n % d },
        move(denominator),
    )
}
