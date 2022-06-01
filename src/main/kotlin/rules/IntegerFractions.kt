package rules

import expressionmakers.*
import expressions.xp
import patterns.*
import steps.metadata.Explanation
import steps.metadata.Skill
import steps.metadata.makeMetadata

val convertIntegerToFraction = run {
    val integer = UnsignedIntegerPattern()
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)
    val sum = commutativeSumOf(integer, fraction)

    Rule(
        pattern = sum,
        resultMaker = substituteIn(
            sum,
            makeFractionOf(
                makeProductOf(move(integer), introduce(denominator)),
                makeProductOf(FixedExpressionMaker(xp(1)), introduce(denominator))
            ),
            move(fraction),
        ),
        explanationMaker = makeMetadata(Explanation.ConvertIntegerToFraction, move(integer), move(fraction)),
    )
}

val addLikeFractions = run {
    val num1 = UnsignedIntegerPattern()
    val num2 = UnsignedIntegerPattern()
    val denom = UnsignedIntegerPattern()
    val f1 = fractionOf(num1, denom)
    val f2 = fractionOf(num2, denom)
    val sum = sumContaining(f1, f2)

    Rule(
        pattern = sum,
        resultMaker = substituteIn(sum, makeFractionOf(makeSumOf(move(num1), move(num2)), factor(denom))),
        explanationMaker = makeMetadata(Explanation.AddLikeFractions, move(f1), move(f2)),
    )
}

val commonDenominator = run {
    val num1 = UnsignedIntegerPattern()
    val num2 = UnsignedIntegerPattern()
    val denom1 = UnsignedIntegerPattern()
    val denom2 = UnsignedIntegerPattern()
    val f1 = fractionOf(num1, denom1)
    val f2 = fractionOf(num2, denom2)
    val sum = sumContaining(f1, f2)

    val factor1 = makeNumericOp(denom1, denom2) { n1, n2 -> n2 / n1.gcd(n2) }
    val factor2 = makeNumericOp(denom1, denom2) { n1, n2 -> n1 / n1.gcd(n2) }

    Rule(
        pattern = ConditionPattern(sum, numericCondition(denom1, denom2) { n1, n2 -> n1 != n2 }),
        resultMaker = substituteIn(
            sum,
            makeSumOf(
                makeFractionOf(makeProductOf(move(num1), factor1), makeProductOf(move(denom1), factor1)),
                makeFractionOf(makeProductOf(move(num2), factor2), makeProductOf(move(denom2), factor2))
            )
        ),
        explanationMaker = makeMetadata(Explanation.BringToCommonDenominator, move(f1), move(f2)),
        skillMakers = listOf(makeMetadata(Skill.NumericLCM, move(denom1), move(denom2))),
    )
}

val cancelInAFraction = run {
    val common = AnyPattern()
    val numerator = productContaining(common, minSize = 2)
    val denominator = productContaining(common, minSize = 2)
    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = cancel(common, makeFractionOf(restOf(numerator), restOf(denominator))),
        explanationMaker = makeMetadata(Explanation.CancelCommonTerms, move(pattern), move(common)),
    )
}