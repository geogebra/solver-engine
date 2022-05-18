package rules

import expressionmakers.*
import patterns.*
import steps.SkillType
import steps.makeMetadata

val splitMixedNumber = run {
    val integer = IntegerPattern()
    val numerator = IntegerPattern()
    val denominator = IntegerPattern()
    val pattern = MixedNumberPattern(integer, numerator, denominator)

    RuleData(
        pattern = pattern,
        resultMaker = makeSumOf(move(integer), makeFractionOf(move(numerator), move(denominator))),
        explanationMaker = makeMetadata("split mixed number", move(pattern)),
    )
}

val fractionToMixedNumber = run {
    val numerator = IntegerPattern()
    val denominator = IntegerPattern()

    val isImproperFraction = numericCondition(numerator, denominator) { n, d -> n > d }

    val quotient = makeNumericOp(numerator, denominator) { n, d -> n / d }
    val remainder = makeNumericOp(numerator, denominator) { n, d -> n % d }

    RuleData(
        pattern = ConditionPattern(fractionOf(numerator, denominator), isImproperFraction),
        explanationMaker = makeMetadata(
            "fraction to mixed number",
            move(numerator), move(denominator), quotient, remainder
        ),
        skillMakers = listOf(makeMetadata(SkillType.DivisionWithRemainder, move(numerator), move(denominator))),
        resultMaker = MixedNumberMaker(quotient, remainder, move(denominator)),
    )
}