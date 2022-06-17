package methods.rules

import engine.expressionmakers.FixedExpressionMaker
import engine.expressionmakers.factor
import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.makeNegOf
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makeOptionalNegOf
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeSumOf
import engine.expressionmakers.move
import engine.expressionmakers.substituteIn
import engine.expressions.xp
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeSumOf
import engine.patterns.divideBy
import engine.patterns.fractionOf
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.sumOf
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
    val nf1 = optionalNegOf(f1)
    val nf2 = optionalNegOf(f2)
    val sum = sumContaining(nf1, nf2)

    Rule(
        pattern = sum,
        resultMaker = substituteIn(
            sum,
            makeFractionOf(
                makeSumOf(
                    makeOptionalNegOf(nf1, move(num1)),
                    makeOptionalNegOf(nf2, move(num2))
                ),
                factor(denom)
            )
        ),
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
    val nf1 = optionalNegOf(f1)
    val nf2 = optionalNegOf(f2)
    val sum = sumContaining(nf1, nf2)

    val factor1 = makeNumericOp(denom1, denom2) { n1, n2 -> n2 / n1.gcd(n2) }
    val factor2 = makeNumericOp(denom1, denom2) { n1, n2 -> n1 / n1.gcd(n2) }

    Rule(
        pattern = ConditionPattern(sum, numericCondition(denom1, denom2) { n1, n2 -> n1 != n2 }),
        resultMaker = substituteIn(
            sum,
            makeSumOf(
                makeOptionalNegOf(
                    nf1,
                    makeFractionOf(makeProductOf(move(num1), factor1), makeProductOf(move(denom1), factor1))
                ),
                makeOptionalNegOf(
                    nf2,
                    makeFractionOf(makeProductOf(move(num2), factor2), makeProductOf(move(denom2), factor2))
                ),
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
        explanationMaker = makeMetadata(Explanation.MultiplyFractions, move(f1), move(f2)),
    )
}

val simplifyDividingByAFraction = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val f = fractionOf(numerator, denominator)
    val product = productContaining(divideBy(f))

    Rule(
        pattern = product,
        resultMaker = substituteIn(
            product,
            makeFractionOf(move(denominator), move(numerator))
        ),
        explanationMaker = makeMetadata(Explanation.DivideByAFraction, move(f)),
    )
}

val simplifyDividingByANumber = run {
    val dividend = AnyPattern()
    val divisor = UnsignedIntegerPattern()
    val product = productContaining(dividend, divideBy(divisor))

    Rule(
        pattern = product,
        resultMaker = substituteIn(
            product,
            makeFractionOf(move(dividend), move(divisor)),
        ),
        explanationMaker = makeMetadata(Explanation.TurnDivisionToFraction, move(dividend), move(divisor)),
    )
}

val simplifyFractionWithFractionNumerator = run {
    val numerator = fractionOf(AnyPattern(), AnyPattern())
    val denominator = UnsignedIntegerPattern()
    val f = fractionOf(numerator, denominator)

    Rule(
        pattern = f,
        resultMaker = makeProductOf(
            move(numerator),
            makeFractionOf(FixedExpressionMaker(xp(1)), move(denominator))
        ),
        explanationMaker = makeMetadata(Explanation.SimplifyFractionWithFractionNumerator, move(f)),
    )
}

val simplifyFractionWithFractionDenominator = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val innerFraction = fractionOf(numerator, denominator)
    val outerNumerator = AnyPattern()
    val outerFraction = fractionOf(outerNumerator, innerFraction)

    Rule(
        pattern = outerFraction,
        resultMaker = makeProductOf(
            move(outerNumerator),
            makeFractionOf(move(denominator), move(numerator))
        ),
        explanationMaker = makeMetadata(Explanation.SimplifyFractionWithFractionDenominator, move(outerFraction)),
    )
}
