package methods.mixednumbers

import engine.expressions.fractionOf
import engine.expressions.mixedNumberOf
import engine.expressions.sumOf
import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.ConditionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
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

val fractionToMixedNumber = rule {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val isImproperFraction = numericCondition(numerator, denominator) { n, d -> n > d }

    onPattern(ConditionPattern(fractionOf(numerator, denominator), isImproperFraction)) {
        val quotient = numericOp(numerator, denominator) { n, d -> n / d }
        val remainder = numericOp(numerator, denominator) { n, d -> n % d }

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
