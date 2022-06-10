package methods.rules

import engine.expressionmakers.*
import engine.expressions.xp
import engine.patterns.*
import engine.rules.Rule
import engine.steps.metadata.Explanation
import engine.steps.metadata.Skill
import engine.steps.metadata.makeMetadata
import java.math.BigInteger

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
            makeFractionOf(move(integer), FixedExpressionMaker(xp(1))),
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

val subtractLikeFractions = run {
    val num1 = UnsignedIntegerPattern()
    val num2 = UnsignedIntegerPattern()
    val denom = UnsignedIntegerPattern()
    val f1 = fractionOf(num1, denom)
    val f2 = fractionOf(num2, denom)

    val pattern = sumOf(f1, negOf(f2))

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(makeSumOf(move(num1), makeNegOf(move(num2))), factor(denom)),
        explanationMaker = makeMetadata(Explanation.SubtractLikeFractions, move(f1), move(f2))
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

val negativeDenominator = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val pattern = fractionOf(numerator, negOf(denominator))

    Rule(
        pattern = pattern,
        resultMaker = makeNegOf(makeFractionOf(move(numerator), move(denominator))),
        explanationMaker = makeMetadata(Explanation.SimplifyNegativeInDenominator, move(pattern)),
    )
}

val simplifyFractionToInteger = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val frac = fractionOf(numerator, denominator)

    Rule(
        pattern = ConditionPattern(frac, numericCondition(numerator, denominator) { n, d -> n.mod(d).signum() == 0 }),
        resultMaker = makeNumericOp(numerator, denominator) { n, d -> n / d },
        explanationMaker = makeMetadata(Explanation.SimplifyFractionToInteger),
    )
}

val findCommonFactorInFraction = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val frac = fractionOf(numerator, denominator)

    val gcd = makeNumericOp(numerator, denominator) { n, d -> n.gcd(d) }
    val numeratorOverGcd = makeNumericOp(numerator, denominator) { n, d -> n / n.gcd(d) }
    val denominatorOverGcd = makeNumericOp(numerator, denominator) { n, d -> d / n.gcd(d) }

    Rule(
        pattern = ConditionPattern(
            frac,
            numericCondition(numerator, denominator) { n, d -> n.gcd(d) != BigInteger.ONE }
        ),
        resultMaker = makeFractionOf(
            makeProductOf(gcd, numeratorOverGcd),
            makeProductOf(gcd, denominatorOverGcd),
        ),
        explanationMaker = makeMetadata(Explanation.FindCommonFactorInFraction),
    )
}

val negativeNumerator = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val pattern = fractionOf(negOf(numerator), denominator)

    Rule(
        pattern = pattern,
        resultMaker = makeNegOf(makeFractionOf(move(numerator), move(denominator))),
        explanationMaker = makeMetadata(Explanation.SimplifyNegativeInNumerator, move(pattern)),
    )
}


val negativeNumeratorAndDenominator = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val pattern = fractionOf(negOf(numerator), negOf(denominator))

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(move(numerator), move(denominator)),
        explanationMaker = makeMetadata(Explanation.SimplifyNegativeInNumeratorAndDenominator, move(pattern)),
    )
}

val multiplyPositiveFractions = run {
    val num1 = UnsignedIntegerPattern()
    val num2 = UnsignedIntegerPattern()
    val denom1 = UnsignedIntegerPattern()
    val denom2 = UnsignedIntegerPattern()
    val f1 = fractionOf(num1, denom1)
    val f2 = fractionOf(num2, denom2)
    val product = productContaining(f1, f2)

    Rule(
        pattern = product,
        resultMaker = substituteIn(
            product,
            makeFractionOf(
                makeProductOf(move(num1), move(num2)),
                makeProductOf(move(denom1), move(denom2))
            ),
        ),
        explanationMaker = makeMetadata(Explanation.MultiplyFractions),
    )
}
