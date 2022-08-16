package methods.fractionarithmetic

import engine.expressionmakers.FixedExpressionMaker
import engine.expressionmakers.factor
import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.makeNegOf
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makeOptionalNegOf
import engine.expressionmakers.makePowerOf
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeSimplifiedProductOf
import engine.expressionmakers.makeSumOf
import engine.expressionmakers.move
import engine.expressionmakers.substituteIn
import engine.expressions.BinaryOperator
import engine.expressions.Constants
import engine.expressions.Subexpression
import engine.expressions.xp
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.custom
import engine.patterns.fractionOf
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.metadata.Skill
import engine.steps.metadata.makeMetadata
import java.math.BigInteger

private class FractionPattern : Pattern {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    override val key = fraction.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return fraction.findMatches(subexpression, match)
    }
}

val convertIntegerToFraction = run {
    val integer = UnsignedIntegerPattern()
    val fraction = FractionPattern()
    val sum = commutativeSumOf(integer, fraction)

    Rule(
        pattern = sum,
        resultMaker = substituteIn(
            sum,
            makeFractionOf(move(integer), FixedExpressionMaker(xp(1))),
            move(fraction),
        ),
        explanationMaker = makeMetadata(Explanation.ConvertIntegerToFraction, move(integer)),
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

val bringToCommonDenominator = run {
    val f1 = FractionPattern()
    val f2 = FractionPattern()
    val nf1 = optionalNegOf(f1)
    val nf2 = optionalNegOf(f2)
    val sum = sumContaining(nf1, nf2)

    val factor1 = makeNumericOp(f1.denominator, f2.denominator) { n1, n2 -> n2 / n1.gcd(n2) }
    val factor2 = makeNumericOp(f1.denominator, f2.denominator) { n1, n2 -> n1 / n1.gcd(n2) }

    Rule(
        pattern = ConditionPattern(sum, numericCondition(f1.denominator, f2.denominator) { n1, n2 -> n1 != n2 }),
        resultMaker = substituteIn(
            sum,
            makeSumOf(
                makeOptionalNegOf(
                    nf1,
                    makeFractionOf(
                        makeProductOf(move(f1.numerator), factor1),
                        makeProductOf(move(f1.denominator), factor1)
                    )
                ),
                makeOptionalNegOf(
                    nf2,
                    makeFractionOf(
                        makeProductOf(move(f2.numerator), factor2),
                        makeProductOf(move(f2.denominator), factor2)
                    )
                ),
            )
        ),
        explanationMaker = makeMetadata(Explanation.BringToCommonDenominator, move(f1), move(f2)),
        skillMakers = listOf(makeMetadata(Skill.NumericLCM, move(f1.denominator), move(f2.denominator))),
    )
}

val simplifyNegativeInDenominator = run {
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
    val factorNumerator = UnsignedIntegerPattern()
    val factorDenominator = UnsignedIntegerPattern()

    val productNumerator = productContaining(factorNumerator)
    val productDenominator = productContaining(factorDenominator)

    val numerator = oneOf(factorNumerator, productNumerator)
    val denominator = oneOf(factorDenominator, productDenominator)

    val frac = fractionOf(numerator, denominator)

    val gcd = makeNumericOp(factorNumerator, factorDenominator) { n, d -> n.gcd(d) }
    val numeratorOverGcd = makeNumericOp(factorNumerator, factorDenominator) { n, d -> n / n.gcd(d) }
    val denominatorOverGcd = makeNumericOp(factorNumerator, factorDenominator) { n, d -> d / n.gcd(d) }

    Rule(
        pattern = ConditionPattern(
            frac,
            numericCondition(factorNumerator, factorDenominator) { n, d -> n.gcd(d) != BigInteger.ONE }
        ),
        resultMaker = makeFractionOf(
            custom {
                if (isBound(productNumerator)) {
                    substituteIn(productNumerator, makeSimplifiedProductOf(gcd, numeratorOverGcd))
                } else {
                    makeProductOf(gcd, numeratorOverGcd)
                }
            },
            custom {
                if (isBound(productDenominator)) {
                    substituteIn(productDenominator, makeSimplifiedProductOf(gcd, denominatorOverGcd))
                } else {
                    makeProductOf(gcd, denominatorOverGcd)
                }
            }
        ),
        explanationMaker = makeMetadata(Explanation.FindCommonFactorInFraction),
    )
}

val simplifyNegativeInNumerator = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val pattern = fractionOf(negOf(numerator), denominator)

    Rule(
        pattern = pattern,
        resultMaker = makeNegOf(makeFractionOf(move(numerator), move(denominator))),
        explanationMaker = makeMetadata(Explanation.SimplifyNegativeInNumerator, move(pattern)),
    )
}

val simplifyNegativeNumeratorAndDenominator = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val pattern = fractionOf(negOf(numerator), negOf(denominator))

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(move(numerator), move(denominator)),
        explanationMaker = makeMetadata(Explanation.SimplifyNegativeInNumeratorAndDenominator, move(pattern)),
    )
}

val turnProductOfFractionByIntegerToFractionProduct = run {
    val nonFractionTerm = condition(AnyPattern()) { it.operator != BinaryOperator.Fraction }
    val product = productContaining(nonFractionTerm)

    Rule(
        pattern = condition(product) { expression ->
            expression.operands.any { it.operator == BinaryOperator.Fraction }
        },
        resultMaker = substituteIn(
            product,
            makeFractionOf(move(nonFractionTerm), FixedExpressionMaker(Constants.One))
        ),
        explanationMaker = makeMetadata(Explanation.MultiplyFractions),
    )
}

val turnSumOfFractionAndIntegerToFractionSum = run {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val f = fractionOf(numerator, denominator)
    val nf = optionalNegOf(f)
    val integerTerm = UnsignedIntegerPattern()
    val ni = optionalNegOf(integerTerm)

    val sum = commutativeSumOf(nf, ni)

    Rule(
        pattern = sum,
        resultMaker = substituteIn(
            sum,
            move(nf),
            makeOptionalNegOf(
                ni,
                makeFractionOf(
                    makeProductOf(move(integerTerm), move(denominator)),
                    move(denominator)
                )
            )
        ),
        explanationMaker = makeMetadata(Explanation.BringToCommonDenominator, move(f), move(integerTerm)),
    )
}

val multiplyFractions = run {
    val num1 = AnyPattern()
    val num2 = AnyPattern()
    val denom1 = AnyPattern()
    val denom2 = AnyPattern()
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

val distributeFractionPositivePower = run {
    val fraction = FractionPattern()
    val exponent = numericCondition(UnsignedIntegerPattern()) { it > BigInteger.ONE }
    val pattern = powerOf(bracketOf(fraction), exponent)

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(
            makePowerOf(move(fraction.numerator), move(exponent)),
            makePowerOf(move(fraction.denominator), move(exponent))
        ),
        explanationMaker = makeMetadata(Explanation.DistributeFractionPositivePower, move(fraction), move(exponent))
    )
}

val simplifyFractionNegativePower = run {
    val fraction = FractionPattern()
    val exponent = SignedIntegerPattern()
    val pattern = powerOf(bracketOf(fraction), numericCondition(exponent) { it < -BigInteger.ONE })

    Rule(
        pattern = pattern,
        resultMaker = makePowerOf(
            makeFractionOf(move(fraction.denominator), move(fraction.numerator)),
            move(exponent.unsignedPattern)
        ),
        explanationMaker = makeMetadata(Explanation.SimplifyFractionNegativePower, move(fraction), move(exponent))
    )
}

val simplifyFractionToMinusOne = run {
    val fraction = FractionPattern()
    val pattern = powerOf(bracketOf(fraction), FixedPattern(xp(-1)))

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(move(fraction.denominator), move(fraction.numerator)),
        explanationMaker = makeMetadata(Explanation.SimplifyFractionToMinusOne, move(fraction)),
    )
}

val turnIntegerToMinusOneToFraction = run {
    val base = UnsignedIntegerPattern()
    val pattern = powerOf(base, FixedPattern(xp(-1)))

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(FixedExpressionMaker(xp(1)), move(base)),
        explanationMaker = makeMetadata(Explanation.TurnIntegerToMinusOneToFraction, move(base))
    )
}

val turnNegativePowerOfIntegerToFraction = run {
    val base = UnsignedIntegerPattern()
    val exponent = SignedIntegerPattern()
    val pattern = powerOf(base, numericCondition(exponent) { it < -BigInteger.ONE })

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(
            FixedExpressionMaker(xp(1)),
            makePowerOf(move(base), move(exponent.unsignedPattern)),
        ),
        explanationMaker = makeMetadata(Explanation.TurnNegativePowerOfIntegerToFraction, move(base), move(exponent))
    )
}

/*
[a / b] * [c / d] --> [a * c / b * d]
 */
val writeMultiplicationOfFractionsAsFraction = run {
    val num1 = AnyPattern()
    val den1 = AnyPattern()
    val num2 = AnyPattern()
    val den2 = AnyPattern()
    val pattern = productOf(fractionOf(num1, den1), fractionOf(num2, den2))

    Rule(
        pattern = pattern,
        resultMaker = makeFractionOf(
            makeProductOf(move(num1), move(num2)),
            makeProductOf(move(den1), move(den2)),
        ),
        explanationMaker = makeMetadata(Explanation.ConvertMultiplicationOfFractionsToFraction)
    )
}
