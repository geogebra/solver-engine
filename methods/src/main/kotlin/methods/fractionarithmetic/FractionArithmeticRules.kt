package methods.fractionarithmetic

import engine.expressions.BinaryOperator
import engine.expressions.Constants
import engine.expressions.Subexpression
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
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
import engine.patterns.fractionOf
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
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

val convertIntegerToFraction = rule {
    val integer = UnsignedIntegerPattern()
    val fraction = FractionPattern()
    val sum = commutativeSumOf(integer, fraction)

    onPattern(sum) {
        TransformationResult(
            toExpr = sum.substitute(
                fractionOf(move(integer), introduce(Constants.One)),
                move(fraction),
            ),
            explanation = metadata(Explanation.ConvertIntegerToFraction, move(integer)),
        )
    }
}

val addLikeFractions = rule {
    val num1 = UnsignedIntegerPattern()
    val num2 = UnsignedIntegerPattern()
    val denom = UnsignedIntegerPattern()
    val f1 = fractionOf(num1, denom)
    val f2 = fractionOf(num2, denom)
    val nf1 = optionalNegOf(f1)
    val nf2 = optionalNegOf(f2)
    val sum = sumContaining(nf1, nf2)

    onPattern(sum) {
        TransformationResult(
            toExpr = sum.substitute(
                fractionOf(
                    sumOf(
                        copySign(nf1, move(num1)),
                        copySign(nf2, move(num2))
                    ),
                    factor(denom)
                )
            ),
            explanation = metadata(Explanation.AddLikeFractions, move(f1), move(f2)),
        )
    }
}

val bringToCommonDenominator = rule {
    val f1 = FractionPattern()
    val f2 = FractionPattern()
    val nf1 = optionalNegOf(f1)
    val nf2 = optionalNegOf(f2)
    val sum = sumContaining(nf1, nf2)

    onPattern(ConditionPattern(sum, numericCondition(f1.denominator, f2.denominator) { n1, n2 -> n1 != n2 })) {
        val factor1 = numericOp(f1.denominator, f2.denominator) { n1, n2 -> n2 / n1.gcd(n2) }
        val factor2 = numericOp(f1.denominator, f2.denominator) { n1, n2 -> n1 / n1.gcd(n2) }

        TransformationResult(
            toExpr = sum.substitute(
                sumOf(
                    copySign(
                        nf1,
                        fractionOf(
                            productOf(move(f1.numerator), factor1),
                            productOf(move(f1.denominator), factor1)
                        )
                    ),
                    copySign(
                        nf2,
                        fractionOf(
                            productOf(move(f2.numerator), factor2),
                            productOf(move(f2.denominator), factor2)
                        )
                    ),
                )
            ),
            explanation = metadata(Explanation.BringToCommonDenominator, move(f1), move(f2)),
            skills = listOf(metadata(Skill.NumericLCM, move(f1.denominator), move(f2.denominator))),
        )
    }
}

val simplifyNegativeInDenominator = rule {
    val numerator = AnyPattern()
    val denominator = AnyPattern()

    val pattern = fractionOf(
        numerator,
        negOf(oneOf(bracketOf(denominator), denominator)),
    )

    onPattern(pattern) {
        TransformationResult(
            toExpr = negOf(fractionOf(move(numerator), move(denominator))),
            explanation = metadata(Explanation.SimplifyNegativeInDenominator, move(pattern)),
        )
    }
}

val simplifyFractionToInteger = rule {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()

    val frac = fractionOf(numerator, denominator)

    onPattern(ConditionPattern(frac, numericCondition(numerator, denominator) { n, d -> n.mod(d).signum() == 0 })) {
        TransformationResult(
            toExpr = numericOp(numerator, denominator) { n, d -> n / d },
            explanation = metadata(Explanation.SimplifyFractionToInteger),
        )
    }
}

val findCommonFactorInFraction = rule {
    val factorNumerator = UnsignedIntegerPattern()
    val factorDenominator = UnsignedIntegerPattern()

    val productNumerator = productContaining(factorNumerator)
    val productDenominator = productContaining(factorDenominator)

    val numerator = oneOf(factorNumerator, productNumerator)
    val denominator = oneOf(factorDenominator, productDenominator)

    val frac = fractionOf(numerator, denominator)

    onPattern(
        ConditionPattern(
            frac,
            numericCondition(factorNumerator, factorDenominator) { n, d -> n.gcd(d) != BigInteger.ONE }
        )
    ) {
        val gcd = numericOp(factorNumerator, factorDenominator) { n, d -> n.gcd(d) }
        val numeratorOverGcd = numericOp(factorNumerator, factorDenominator) { n, d -> n / n.gcd(d) }
        val denominatorOverGcd = numericOp(factorNumerator, factorDenominator) { n, d -> d / n.gcd(d) }

        TransformationResult(
            toExpr = fractionOf(
                if (isBound(productNumerator)) {
                    productNumerator.substitute(simplifiedProductOf(gcd, numeratorOverGcd))
                } else {
                    productOf(gcd, numeratorOverGcd)
                },
                if (isBound(productDenominator)) {
                    productDenominator.substitute(simplifiedProductOf(gcd, denominatorOverGcd))
                } else {
                    productOf(gcd, denominatorOverGcd)
                }
            ),
            explanation = metadata(Explanation.FindCommonFactorInFraction),
        )
    }
}

val simplifyNegativeInNumerator = rule {
    val numerator = AnyPattern()
    val denominator = AnyPattern()

    val pattern = fractionOf(
        negOf(oneOf(bracketOf(numerator), numerator)),
        denominator,
    )

    onPattern(pattern) {
        TransformationResult(
            negOf(fractionOf(move(numerator), move(denominator))),
            explanation = metadata(Explanation.SimplifyNegativeInNumerator, move(pattern)),
        )
    }
}

val simplifyNegativeNumeratorAndDenominator = rule {
    val numerator = AnyPattern()
    val denominator = AnyPattern()

    val pattern = fractionOf(
        negOf(oneOf(bracketOf(numerator), numerator)),
        negOf(oneOf(bracketOf(denominator), denominator)),
    )

    onPattern(pattern) {
        TransformationResult(
            fractionOf(move(numerator), move(denominator)),
            explanation = metadata(Explanation.SimplifyNegativeInNumeratorAndDenominator, move(pattern)),
        )
    }
}

val turnProductOfFractionByIntegerToFractionProduct = rule {
    val nonFractionTerm = condition(AnyPattern()) { it.operator != BinaryOperator.Fraction }
    val product = productContaining(nonFractionTerm)

    onPattern(
        condition(product) { expression ->
            expression.operands.any { it.operator == BinaryOperator.Fraction }
        }
    ) {
        TransformationResult(
            toExpr = product.substitute(
                fractionOf(move(nonFractionTerm), introduce(Constants.One))
            ),
            explanation = metadata(Explanation.MultiplyFractions),
        )
    }
}

val turnSumOfFractionAndIntegerToFractionSum = rule {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val f = fractionOf(numerator, denominator)
    val nf = optionalNegOf(f)
    val integerTerm = UnsignedIntegerPattern()
    val ni = optionalNegOf(integerTerm)

    val sum = commutativeSumOf(nf, ni)

    onPattern(sum) {
        TransformationResult(
            sum.substitute(
                move(nf),
                copySign(
                    ni,
                    fractionOf(
                        productOf(move(integerTerm), move(denominator)),
                        move(denominator)
                    )
                )
            ),
            explanation = metadata(Explanation.BringToCommonDenominator, move(f), move(integerTerm)),
        )
    }
}

val multiplyFractions = rule {
    val num1 = AnyPattern()
    val num2 = AnyPattern()
    val denom1 = AnyPattern()
    val denom2 = AnyPattern()
    val f1 = fractionOf(num1, denom1)
    val f2 = fractionOf(num2, denom2)
    val product = productContaining(f1, f2)

    onPattern(product) {
        TransformationResult(
            product.substitute(
                fractionOf(
                    productOf(move(num1), move(num2)),
                    productOf(move(denom1), move(denom2))
                ),
            ),
            explanation = metadata(Explanation.MultiplyFractions, move(f1), move(f2)),
        )
    }
}

val simplifyFractionWithFractionNumerator = rule {
    val numerator = fractionOf(AnyPattern(), AnyPattern())
    val denominator = UnsignedIntegerPattern()
    val f = fractionOf(numerator, denominator)

    onPattern(f) {
        TransformationResult(
            productOf(
                move(numerator),
                fractionOf(introduce(Constants.One), move(denominator))
            ),
            explanation = metadata(Explanation.SimplifyFractionWithFractionNumerator, move(f)),
        )
    }
}

val simplifyFractionWithFractionDenominator = rule {
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val innerFraction = fractionOf(numerator, denominator)
    val outerNumerator = AnyPattern()
    val outerFraction = fractionOf(outerNumerator, innerFraction)

    onPattern(outerFraction) {
        TransformationResult(
            productOf(
                move(outerNumerator),
                fractionOf(move(denominator), move(numerator))
            ),
            explanation = metadata(Explanation.SimplifyFractionWithFractionDenominator, move(outerFraction)),
        )
    }
}

val distributeFractionPositivePower = rule {
    val fraction = FractionPattern()
    val exponent = numericCondition(UnsignedIntegerPattern()) { it > BigInteger.ONE }
    val pattern = powerOf(bracketOf(fraction), exponent)

    onPattern(pattern) {
        TransformationResult(
            fractionOf(
                powerOf(move(fraction.numerator), move(exponent)),
                powerOf(move(fraction.denominator), move(exponent))
            ),
            explanation = metadata(Explanation.DistributeFractionPositivePower, move(fraction), move(exponent))
        )
    }
}

val simplifyFractionNegativePower = rule {
    val fraction = FractionPattern()
    val exponent = SignedIntegerPattern()
    val pattern = powerOf(bracketOf(fraction), numericCondition(exponent) { it < -BigInteger.ONE })

    onPattern(pattern) {
        TransformationResult(
            powerOf(
                fractionOf(move(fraction.denominator), move(fraction.numerator)),
                move(exponent.unsignedPattern)
            ),
            explanation = metadata(Explanation.SimplifyFractionNegativePower, move(fraction), move(exponent))
        )
    }
}

val simplifyFractionToMinusOne = rule {
    val fraction = FractionPattern()
    val pattern = powerOf(bracketOf(fraction), FixedPattern(xp(-1)))

    onPattern(pattern) {
        TransformationResult(
            fractionOf(move(fraction.denominator), move(fraction.numerator)),
            explanation = metadata(Explanation.SimplifyFractionToMinusOne, move(fraction)),
        )
    }
}

val turnIntegerToMinusOneToFraction = rule {
    val base = UnsignedIntegerPattern()
    val pattern = powerOf(base, FixedPattern(xp(-1)))

    onPattern(pattern) {
        TransformationResult(
            fractionOf(introduce(Constants.One), move(base)),
            explanation = metadata(Explanation.TurnIntegerToMinusOneToFraction, move(base))
        )
    }
}

val turnNegativePowerOfIntegerToFraction = rule {
    val base = UnsignedIntegerPattern()
    val exponent = SignedIntegerPattern()
    val pattern = powerOf(base, numericCondition(exponent) { it < -BigInteger.ONE })

    onPattern(pattern) {
        TransformationResult(
            fractionOf(
                introduce(Constants.One),
                powerOf(move(base), move(exponent.unsignedPattern)),
            ),
            explanation = metadata(Explanation.TurnNegativePowerOfIntegerToFraction, move(base), move(exponent))
        )
    }
}
