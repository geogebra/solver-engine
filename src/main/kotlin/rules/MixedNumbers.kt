package rules

import expressionmakers.*
import patterns.*
import steps.SkillType
import steps.makeMetadata

val splitMixedNumber = run {
    val pattern = MixedNumberPattern()

    Rule(
        pattern = pattern,
        resultMaker = makeSumOf(
            move(pattern.integer),
            makeFractionOf(move(pattern.numerator), move(pattern.denominator))
        ),
        explanationMaker = makeMetadata("split mixed number", move(pattern)),
    )
}

val fractionToMixedNumber = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val isImproperFraction = numericCondition(numerator, denominator) { n, d -> n > d }

    val quotient = makeNumericOp(numerator, denominator) { n, d -> n / d }
    val remainder = makeNumericOp(numerator, denominator) { n, d -> n % d }

    Rule(
        pattern = ConditionPattern(fractionOf(numerator, denominator), isImproperFraction),
        explanationMaker = makeMetadata(
            "fraction to mixed number",
            move(numerator), move(denominator), quotient, remainder
        ),
        skillMakers = listOf(makeMetadata(SkillType.DivisionWithRemainder, move(numerator), move(denominator))),
        resultMaker = MixedNumberMaker(quotient, remainder, move(denominator)),
    )
}