package methods.mixednumbers

import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.makeMixedNumberOf
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makeSumOf
import engine.expressionmakers.move
import engine.patterns.ConditionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.mixedNumberOf
import engine.patterns.numericCondition
import engine.rules.Rule
import engine.steps.metadata.Skill
import engine.steps.metadata.makeMetadata

val splitMixedNumber = run {
    val integer = UnsignedIntegerPattern()
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val pattern = mixedNumberOf(integer, numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = makeSumOf(
            move(integer),
            makeFractionOf(move(numerator), move(denominator))
        ),
        explanationMaker = makeMetadata(Explanation.ConvertMixedNumberToSum, move(pattern)),
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
            Explanation.ConvertFractionToMixedNumber,
            move(numerator), move(denominator), quotient, remainder
        ),
        skillMakers = listOf(makeMetadata(Skill.DivisionWithRemainder, move(numerator), move(denominator))),
        resultMaker = makeMixedNumberOf(quotient, remainder, move(denominator)),
    )
}
