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
import engine.patterns.mixedNumberOf
import engine.patterns.numericCondition
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
    val fraction = IntegerFractionPattern()
    val improperFractionCondition = numericCondition(fraction.numerator, fraction.denominator) { n1, n2 -> n1 > n2 }
    val improperFraction = ConditionPattern(fraction, improperFractionCondition)

    onPattern(improperFraction) {
        val quotient = integerOp(fraction.numerator, fraction.denominator) { n, d -> n / d }
        val remainder = integerOp(fraction.numerator, fraction.denominator) { n, d -> n % d }

        TransformationResult(
            toExpr = mixedNumberOf(quotient, remainder, move(fraction.denominator)),
            explanation = metadata(Explanation.ConvertFractionToMixedNumber),
            skills = listOf(
                metadata(Skill.DivisionWithRemainder, move(fraction.numerator), move(fraction.denominator))
            ),
        )
    }
}
