package methods.mixednumbers

import engine.expressions.fractionOf
import engine.expressions.mixedNumberOf
import engine.expressions.sumOf
import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.ConditionPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeSumOf
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.mixedNumberOf
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata

val splitMixedNumber = rule {
    val integer = UnsignedIntegerPattern()
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val mixedNumber = mixedNumberOf(integer, numerator, denominator)

    onPattern(mixedNumber) {
        TransformationResult(
            toExpr = sumOf(
                move(integer),
                fractionOf(move(numerator), move(denominator))
            ),
            explanation = metadata(Explanation.ConvertMixedNumberToSum, move(mixedNumber)),
        )
    }
}

val convertSumOfIntegerAndProperFractionToMixedNumber = rule {
    val integer = UnsignedIntegerPattern()
    val fraction = IntegerFractionPattern()
    val sum = commutativeSumOf(integer, fraction)

    onPattern(sum) {
        val numeratorValue = getValue(fraction.numerator)
        val denominatorValue = getValue(fraction.denominator)

        when {
            numeratorValue < denominatorValue -> TransformationResult(
                toExpr = mixedNumberOf(
                    move(integer), move(fraction.numerator), move(fraction.denominator)
                ),
                explanation = metadata(Explanation.ConvertSumOfIntegerAndProperFractionToMixedNumber)
            )
            else -> null
        }
    }
}

val fractionToMixedNumber = rule {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val isImproperFraction = integerCondition(numerator, denominator) { n, d -> n > d }

    onPattern(ConditionPattern(fractionOf(numerator, denominator), isImproperFraction)) {
        val quotient = integerOp(numerator, denominator) { n, d -> n / d }
        val remainder = integerOp(numerator, denominator) { n, d -> n % d }

        TransformationResult(
            toExpr = mixedNumberOf(quotient, remainder, move(denominator)),
            explanation = metadata(
                Explanation.ConvertFractionToMixedNumber,
                move(numerator), move(denominator), quotient, remainder
            ),
            skills = listOf(
                metadata(Skill.DivisionWithRemainder, move(numerator), move(denominator))
            ),
        )
    }
}
