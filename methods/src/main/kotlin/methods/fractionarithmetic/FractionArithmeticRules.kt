package methods.fractionarithmetic

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.methods.ruleResult
import engine.operators.BinaryExpressionOperator
import engine.operators.NullaryOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeSumContaining
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumOf
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import engine.utility.divides
import engine.utility.isFactorizableUnderRationalExponent
import engine.utility.isZero
import java.math.BigInteger

enum class FractionArithmeticRules(override val runner: Rule) : RunnerMethod {
    ConvertIntegerToFraction(
        rule {
            val integer = UnsignedIntegerPattern()
            val fraction = IntegerFractionPattern()
            val sum = commutativeSumOf(integer, fraction)

            onPattern(sum) {
                ruleResult(
                    toExpr = sum.substitute(
                        fractionOf(move(integer), introduce(Constants.One)),
                        get(fraction),
                    ),
                    explanation = metadata(Explanation.ConvertIntegerToFraction, move(integer)),
                )
            }
        },
    ),

    AddLikeFractions(
        rule {
            val num1 = UnsignedIntegerPattern()
            val num2 = UnsignedIntegerPattern()
            val denom = UnsignedIntegerPattern()
            val f1 = fractionOf(num1, denom)
            val f2 = fractionOf(num2, denom)
            val nf1 = optionalNegOf(f1)
            val nf2 = optionalNegOf(f2)
            val sum = sumOf(nf1, nf2)

            onPattern(sum) {
                ruleResult(
                    toExpr = fractionOf(
                        sumOf(
                            copySign(nf1, move(num1)),
                            copySign(nf2, move(num2)),
                        ),
                        factor(denom),
                    ),
                    explanation = when {
                        !nf1.isNeg() && nf2.isNeg() -> metadata(Explanation.SubtractLikeFractions, move(f1), move(f2))
                        else -> metadata(Explanation.AddLikeFractions, move(nf1), move(nf2))
                    },
                )
            }
        },
    ),

    BringToCommonDenominator(
        rule {
            val f1 = IntegerFractionPattern()
            val f2 = IntegerFractionPattern()
            val nf1 = optionalNegOf(f1)
            val nf2 = optionalNegOf(f2)
            val sum = sumOf(nf1, nf2)

            onPattern(ConditionPattern(sum, integerCondition(f1.denominator, f2.denominator) { n1, n2 -> n1 != n2 })) {
                val factor1 = integerOp(f1.denominator, f2.denominator) { n1, n2 -> n2 / n1.gcd(n2) }
                val factor2 = integerOp(f1.denominator, f2.denominator) { n1, n2 -> n1 / n1.gcd(n2) }

                ruleResult(
                    toExpr = sumOf(
                        if (factor1 == Constants.One) {
                            get(nf1)
                        } else {
                            copySign(
                                nf1,
                                fractionOf(
                                    productOf(get(f1.numerator), factor1),
                                    productOf(get(f1.denominator), factor1),
                                ),
                            )
                        },
                        if (factor2 == Constants.One) {
                            get(nf2)
                        } else {
                            copySign(
                                nf2,
                                fractionOf(
                                    productOf(get(f2.numerator), factor2),
                                    productOf(get(f2.denominator), factor2),
                                ),
                            )
                        },
                    ),
                    explanation = metadata(Explanation.BringToCommonDenominator, move(f1), move(f2)),
                    skills = listOf(metadata(Skill.NumericLCM, move(f1.denominator), move(f2.denominator))),
                )
            }
        },
    ),

    SimplifyNegativeInDenominator(
        rule {
            val numerator = AnyPattern()
            val denominator = AnyPattern()

            val pattern = fractionOf(numerator, negOf(denominator))

            onPattern(pattern) {
                ruleResult(
                    toExpr = negOf(fractionOf(get(numerator), move(denominator))),
                    explanation = metadata(Explanation.SimplifyNegativeInDenominator, move(pattern)),
                )
            }
        },
    ),

    SimplifyFractionToInteger(
        rule {
            val numerator = UnsignedIntegerPattern()
            val denominator = UnsignedIntegerPattern()

            val frac = fractionOf(numerator, denominator)

            onPattern(
                ConditionPattern(
                    frac,
                    integerCondition(numerator, denominator) { n, d -> d.divides(n) },
                ),
            ) {
                ruleResult(
                    toExpr = integerOp(numerator, denominator) { n, d -> n / d },
                    explanation = metadata(Explanation.SimplifyFractionToInteger),
                )
            }
        },
    ),

    FindCommonFactorInFraction(
        rule {
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
                    integerCondition(factorNumerator, factorDenominator) { n, d -> n.gcd(d) != BigInteger.ONE },
                ),
            ) {
                val gcd = integerOp(factorNumerator, factorDenominator) { n, d -> n.gcd(d) }
                val numeratorOverGcd = integerOp(factorNumerator, factorDenominator) { n, d -> n / n.gcd(d) }
                val denominatorOverGcd = integerOp(factorNumerator, factorDenominator) { n, d -> d / n.gcd(d) }

                ruleResult(
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
                        },
                    ),
                    explanation = metadata(Explanation.FindCommonFactorInFraction),
                )
            }
        },
    ),

    SimplifyNegativeInNumerator(
        rule {
            val numerator = AnyPattern()
            val denominator = AnyPattern()

            val pattern = fractionOf(negOf(numerator), denominator)

            onPattern(pattern) {
                ruleResult(
                    negOf(fractionOf(move(numerator), move(denominator))),
                    explanation = metadata(Explanation.SimplifyNegativeInNumerator, move(pattern)),
                )
            }
        },
    ),

    SimplifyNegativeNumeratorAndDenominator(
        rule {
            val numerator = AnyPattern()
            val denominator = AnyPattern()

            val pattern = fractionOf(negOf(numerator), negOf(denominator))

            onPattern(pattern) {
                ruleResult(
                    fractionOf(move(numerator), move(denominator)),
                    explanation = metadata(Explanation.SimplifyNegativeInNumeratorAndDenominator, move(pattern)),
                )
            }
        },
    ),

    TurnFactorIntoFractionInProduct(
        rule {
            val nonFractionFactor =
                condition(AnyPattern()) { it.isConstant() && it.canBeTurnedToFraction() }

            // TODO @Simona doesn't want:  `1 + [([2/5])^[2/3]]` to be a valid `nonFractionFactor`
            val product = productContaining(nonFractionFactor)

            onPattern(
                condition(product) { expression ->
                    expression.flattenedProductChildren().any { it.operator == BinaryExpressionOperator.Fraction }
                },
            ) {
                ruleResult(
                    toExpr = product.substitute(
                        fractionOf(move(nonFractionFactor), introduce(Constants.One)),
                    ),
                    explanation = metadata(Explanation.TurnFactorIntoFractionInProduct, move(nonFractionFactor)),
                )
            }
        },
    ),

    TurnSumOfFractionAndIntegerToFractionSum(
        rule {
            val f = IntegerFractionPattern()
            val nf = optionalNegOf(f)
            val integerTerm = SignedIntegerPattern()

            val sum = commutativeSumContaining(nf, integerTerm)

            onPattern(sum) {
                ruleResult(
                    sum.substitute(
                        get(nf),
                        copySign(
                            integerTerm,
                            fractionOf(
                                productOf(move(integerTerm.unsignedPattern), move(f.denominator)),
                                move(f.denominator),
                            ),
                        ),
                    ),
                    explanation = metadata(Explanation.BringToCommonDenominator, move(f), move(integerTerm)),
                )
            }
        },
    ),

    MultiplyFractions(
        rule {
            val num1 = AnyPattern()
            val num2 = AnyPattern()
            val denom1 = AnyPattern()
            val denom2 = AnyPattern()
            val f1 = fractionOf(num1, denom1)
            val f2 = fractionOf(num2, denom2)
            val product = productContaining(f1, f2)

            onPattern(product) {
                ruleResult(
                    product.substitute(
                        fractionOf(
                            productOf(move(num1), move(num2)),
                            productOf(move(denom1), move(denom2)),
                        ),
                    ),
                    explanation = metadata(Explanation.MultiplyFractions, move(f1), move(f2)),
                )
            }
        },
    ),

    SimplifyFractionWithFractionNumerator(
        rule {
            val numerator = fractionOf(AnyPattern(), AnyPattern())
            val denominator = AnyPattern()
            val f = fractionOf(numerator, denominator)

            onPattern(f) {
                ruleResult(
                    productOf(
                        move(numerator),
                        fractionOf(introduce(Constants.One), move(denominator)),
                    ),
                    explanation = metadata(Explanation.SimplifyFractionWithFractionNumerator, move(f)),
                )
            }
        },
    ),

    SimplifyFractionWithFractionDenominator(
        rule {
            val numerator = UnsignedIntegerPattern()
            val denominator = UnsignedIntegerPattern()
            val innerFraction = fractionOf(numerator, denominator)
            val outerNumerator = AnyPattern()
            val outerFraction = fractionOf(outerNumerator, innerFraction)

            onPattern(outerFraction) {
                ruleResult(
                    productOf(
                        move(outerNumerator),
                        fractionOf(move(denominator), move(numerator)),
                    ),
                    explanation = metadata(Explanation.SimplifyFractionWithFractionDenominator, move(outerFraction)),
                )
            }
        },
    ),

    DistributeFractionPositiveFractionPower(
        rule {
            val fraction = IntegerFractionPattern()
            val exp = IntegerFractionPattern()
            // we "split" an improper fraction power instead of distributing it
            val expIsProperFraction = numericCondition(
                exp.numerator,
                exp.denominator,
            ) { n1, n2 -> n1 < n2 }
            val properFractionExponent = ConditionPattern(exp, expIsProperFraction)
            val pattern = powerOf(fraction, properFractionExponent)

            onPattern(pattern) {
                val fracNum = getValue(fraction.numerator)
                val fracDen = getValue(fraction.denominator)
                val expNum = getValue(exp.numerator)
                val expDen = getValue(exp.denominator)
                if (fracNum.isFactorizableUnderRationalExponent(expNum, expDen) ||
                    fracDen.isFactorizableUnderRationalExponent(expNum, expDen)
                ) {
                    ruleResult(
                        fractionOf(
                            powerOf(move(fraction.numerator), move(exp)),
                            powerOf(move(fraction.denominator), move(exp)),
                        ),
                        explanation = metadata(
                            Explanation.DistributeFractionPositivePower,
                            move(fraction),
                            move(exp),
                        ),
                    )
                } else {
                    null
                }
            }
        },
    ),

    DistributeFractionPositivePower(
        rule {
            val fraction = IntegerFractionPattern()
            val exponent = integerCondition(UnsignedIntegerPattern()) { it > BigInteger.ONE }
            val pattern = powerOf(fraction, exponent)

            onPattern(pattern) {
                ruleResult(
                    fractionOf(
                        powerOf(move(fraction.numerator), move(exponent)),
                        powerOf(move(fraction.denominator), move(exponent)),
                    ),
                    explanation = metadata(
                        Explanation.DistributeFractionPositivePower,
                        move(fraction),
                        move(exponent),
                    ),
                )
            }
        },
    ),

    SimplifyFractionNegativePower(
        rule {
            val fraction = IntegerFractionPattern()
            val exponent = SignedIntegerPattern()
            val pattern = powerOf(fraction, integerCondition(exponent) { it < -BigInteger.ONE })

            onPattern(pattern) {
                ruleResult(
                    powerOf(
                        fractionOf(move(fraction.denominator), move(fraction.numerator)),
                        move(exponent.unsignedPattern),
                    ),
                    explanation = metadata(Explanation.SimplifyFractionNegativePower, move(fraction), move(exponent)),
                )
            }
        },
    ),

    SimplifyFractionToMinusOne(
        rule {
            val fraction = IntegerFractionPattern()
            val pattern = powerOf(fraction, FixedPattern(xp(-1)))

            onPattern(pattern) {
                ruleResult(
                    fractionOf(move(fraction.denominator), move(fraction.numerator)),
                    explanation = metadata(Explanation.SimplifyFractionToMinusOne, move(fraction)),
                )
            }
        },
    ),

    TurnIntegerToMinusOneToFraction(
        rule {
            val base = UnsignedIntegerPattern()
            val pattern = powerOf(base, FixedPattern(xp(-1)))

            onPattern(pattern) {
                ruleResult(
                    fractionOf(introduce(Constants.One), move(base)),
                    explanation = metadata(Explanation.TurnIntegerToMinusOneToFraction, move(base)),
                )
            }
        },
    ),

    TurnNegativePowerOfIntegerToFraction(
        rule {
            val base = integerCondition(UnsignedIntegerPattern()) { !it.isZero() }
            val exponent = SignedIntegerPattern()
            val pattern = powerOf(base, integerCondition(exponent) { it < -BigInteger.ONE })

            onPattern(pattern) {
                ruleResult(
                    toExpr = fractionOf(
                        introduce(Constants.One),
                        powerOf(move(base), move(exponent.unsignedPattern)),
                    ),
                    explanation = metadata(
                        Explanation.TurnNegativePowerOfIntegerToFraction,
                        move(exponent.unsignedPattern),
                    ),
                )
            }
        },
    ),

    TurnNegativePowerOfZeroToPowerOfFraction(
        rule {
            val zero = FixedPattern(Constants.Zero)
            val unsignedExponent = AnyPattern()
            val power = powerOf(zero, negOf(unsignedExponent))

            onPattern(power) {
                ruleResult(
                    toExpr = powerOf(
                        fractionOf(introduce(Constants.One), move(zero)),
                        move(unsignedExponent),
                    ),
                    explanation = metadata(Explanation.TurnNegativePowerOfZeroToPowerOfFraction),
                )
            }
        },
    ),

    ConvertImproperFractionToSumOfIntegerAndFraction(
        rule {
            val fraction = IntegerFractionPattern()
            val improperFractionCondition = numericCondition(
                fraction.numerator,
                fraction.denominator,
            ) { n1, n2 -> n1 > n2 }
            val improperFraction = ConditionPattern(fraction, improperFractionCondition)

            onPattern(improperFraction) {
                val quotient = integerOp(fraction.numerator, fraction.denominator) { n, d -> n / d }
                val remainder = integerOp(fraction.numerator, fraction.denominator) { n, d -> n % d }

                ruleResult(
                    toExpr = sumOf(quotient, fractionOf(remainder, move(fraction.denominator))),
                    explanation = metadata(Explanation.ConvertImproperFractionToSumOfIntegerAndFraction),
                    skills = listOf(
                        metadata(Skill.DivisionWithRemainder, move(fraction.numerator), move(fraction.denominator)),
                    ),
                )
            }
        },
    ),
}

private fun Expression.canBeTurnedToFraction(): Boolean =
    when (operator) {
        BinaryExpressionOperator.Fraction -> false
        BinaryExpressionOperator.Power -> firstChild.canBeTurnedToFraction()
        is NullaryOperator -> true
        else -> children().all { it.canBeTurnedToFraction() }
    }
