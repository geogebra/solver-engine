package methods.fractionarithmetic

import engine.conditions.isDefinitelyNotZero
import engine.expressions.Constants
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.Power
import engine.expressions.areEquivalentSums
import engine.expressions.fractionOf
import engine.expressions.inverse
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.FractionPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.divideBy
import engine.patterns.expressionWithFactor
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.optionalIntegerPowerOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import engine.utility.divides
import engine.utility.isFactorizableUnderRationalExponent
import engine.utility.isZero
import java.math.BigInteger
import engine.steps.metadata.GmPathModifier as PM

enum class FractionArithmeticRules(override val runner: Rule) : RunnerMethod {

    RewriteDivisionAsMultiplicationByReciprocal(
        rule {
            val product = productContaining()

            onPattern(product) {
                val factors = get(product).children

                for ((index, factor) in factors.withIndex()) {
                    val divideByTerm = factor as? DivideBy ?: continue

                    val previousTerm = factors[index - 1]
                    val argument = divideByTerm.divisor

                    if (argument is Fraction || previousTerm is Fraction) {
                        val result = mutableListOf<Expression>()
                        result.addAll(factors.subList(0, index))
                        result.add(argument.inverse())
                        result.addAll(factors.subList(index + 1, factors.size))

                        return@onPattern ruleResult(
                            toExpr = productOf(result),
                            gmAction = noGmSupport(),
                            explanation = metadata(Explanation.RewriteDivisionAsMultiplicationByReciprocal),
                        )
                    }
                }

                null
            }
        },
    ),

    RewriteDivisionAsFraction(
        rule {
            val product = productContaining(divideBy(AnyPattern()))

            onPattern(product) {
                val factors = get(product).children
                val index = factors.indexOfFirst { it is DivideBy }

                val numerator = productOf(factors.subList(0, index))
                val denominator = (factors[index] as DivideBy).divisor
                val fraction = fractionOf(numerator, denominator)

                ruleResult(
                    toExpr = productOf(listOf(fraction) + factors.subList(index + 1, factors.size)),
                    gmAction = noGmSupport(),
                    explanation = metadata(Explanation.RewriteDivisionAsFraction),
                )
            }
        },
    ),

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
                    gmAction = edit(integer),
                    explanation = metadata(Explanation.ConvertIntegerToFraction, move(integer)),
                )
            }
        },
    ),

    AddLikeFractions(
        rule {
            val num1 = AnyPattern()
            val num2 = AnyPattern()
            val denom = AnyPattern()
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
                    gmAction = drag(nf2, nf1),
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
            val numerator1 = AnyPattern()
            val numerator2 = AnyPattern()
            val denominator1 = UnsignedIntegerPattern()
            val denominator2 = UnsignedIntegerPattern()

            val f1 = fractionOf(numerator1, denominator1)
            val f2 = fractionOf(numerator2, denominator2)
            val nf1 = optionalNegOf(f1)
            val nf2 = optionalNegOf(f2)
            val sum = sumOf(nf1, nf2)

            onPattern(ConditionPattern(sum, integerCondition(denominator1, denominator2) { n1, n2 -> n1 != n2 })) {
                val factor1 = integerOp(denominator1, denominator2) { n1, n2 -> n2 / n1.gcd(n2) }
                val factor2 = integerOp(denominator1, denominator2) { n1, n2 -> n1 / n1.gcd(n2) }

                val expandedFraction1 = when (factor1) {
                    Constants.One -> get(nf1)
                    else -> copySign(
                        nf1,
                        fractionOf(
                            productOf(get(numerator1), factor1),
                            productOf(get(denominator1), factor1),
                        ),
                    )
                }

                val expandedFraction2 = when (factor2) {
                    Constants.One -> get(nf2)
                    else -> copySign(
                        nf2,
                        fractionOf(
                            productOf(get(numerator2), factor2),
                            productOf(get(denominator2), factor2),
                        ),
                    )
                }

                ruleResult(
                    toExpr = sumOf(expandedFraction1, expandedFraction2),
                    // REVISIT: no clear actor / not aligned with GM's approach
                    gmAction = if (factor1 == Constants.One) {
                        drag(denominator2, f1)
                    } else {
                        drag(denominator1, f2)
                    },
                    explanation = metadata(Explanation.BringToCommonDenominator, move(f1), move(f2)),
                    skills = listOf(metadata(Skill.NumericLCM, move(denominator1), move(denominator2))),
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
                    gmAction = tap(frac, PM.FractionBar),
                    explanation = metadata(Explanation.SimplifyFractionToInteger),
                )
            }
        },
    ),

    FindCommonIntegerFactorInFraction(
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
                    integerCondition(factorNumerator, factorDenominator) { n, d ->
                        d != n && n.gcd(d) != BigInteger.ONE
                    },
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

    CancelCommonFactorInFraction(
        rule {
            val commonFactor = AnyPattern()

            val numeratorFactor = optionalIntegerPowerOf(commonFactor)
            val numerator = expressionWithFactor(numeratorFactor)

            val denominatorFactor = optionalIntegerPowerOf(commonFactor)
            val denominator = expressionWithFactor(denominatorFactor)

            val frac = fractionOf(numerator, denominator)

            onPattern(frac) {
                val factor = get(commonFactor)
                if (!factor.isDefinitelyNotUndefined() || (factor.isConstant() && !factor.isDefinitelyNotZero())) {
                    return@onPattern null
                }

                val num: Expression
                val den: Expression

                when ((getValue(numeratorFactor.exponent) - getValue(denominatorFactor.exponent)).signum()) {
                    1 -> {
                        val simplifiedPower = simplifiedPowerOf(
                            get(commonFactor),
                            integerOp(numeratorFactor.exponent, denominatorFactor.exponent) { n, m -> n - m },
                        )
                        num = numerator.substitute(simplifiedPower)
                        den = restOf(denominator)
                    }
                    0 -> {
                        num = restOf(numerator)
                        den = restOf(denominator)
                    }
                    else -> {
                        val simplifiedPower = simplifiedPowerOf(
                            get(commonFactor),
                            integerOp(numeratorFactor.exponent, denominatorFactor.exponent) { n, m -> m - n },
                        )
                        num = restOf(numerator)
                        den = denominator.substitute(simplifiedPower)
                    }
                }

                ruleResult(
                    toExpr = cancel(commonFactor, fractionOf(num, den)),
                    explanation = metadata(Explanation.CancelCommonFactorInFraction),
                )
            }
        },
    ),

    ReorganizeCommonSumFactorInFraction(
        rule {
            val numeratorBase = sumContaining()
            val numeratorFactor = optionalIntegerPowerOf(numeratorBase)
            val numerator = expressionWithFactor(numeratorFactor)

            val denominatorBase = sumContaining()
            val denominatorFactor = optionalIntegerPowerOf(denominatorBase)
            val denominator = expressionWithFactor(denominatorFactor)

            val frac = fractionOf(numerator, denominator)

            onPattern(frac) {
                val numeratorBaseValue = get(numeratorBase)
                val denominatorBaseValue = get(denominatorBase)

                if (!numeratorBaseValue.equiv(denominatorBaseValue) &&
                    areEquivalentSums(numeratorBaseValue, denominatorBaseValue)
                ) {
                    val rewrittenDenominator = denominator.substitute(
                        simplifiedPowerOf(
                            transformTo(denominatorBaseValue, numeratorBaseValue),
                            get(denominatorFactor.exponent),
                        ),
                    )

                    ruleResult(
                        toExpr = fractionOf(get(numerator), rewrittenDenominator),
                        explanation = metadata(Explanation.ReorganizeCommonSumFactorInFraction),
                    )
                } else {
                    null
                }
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
                condition { it.isConstant() && it.canBeTurnedToFraction() }

            // TODO @Simona doesn't want:  `1 + [([2/5])^[2/3]]` to be a valid `nonFractionFactor`
            val product = productContaining(nonFractionFactor)

            onPattern(
                condition(product) { expression -> expression.children.any { it is Fraction } },
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

    BringToCommonDenominatorWithNonFractionalTerm(
        rule {
            val denominator = AnyPattern()
            val fraction = optionalNegOf(fractionOf(AnyPattern(), denominator))
            val nonFractionalTerm = optionalNegOf(condition { it !is Fraction })

            val sum = commutativeSumOf(fraction, nonFractionalTerm)

            onPattern(sum) {
                val expandedDenominator = distribute(denominator)

                val nonFractionalTermWithDenominator = copySign(
                    nonFractionalTerm,
                    fractionOf(
                        productOf(move(nonFractionalTerm.unsignedPattern), expandedDenominator),
                        expandedDenominator,
                    ),
                )
                val newSum = sum.substitute(get(fraction), nonFractionalTermWithDenominator)
                ruleResult(
                    toExpr = newSum,
                    explanation = metadata(
                        Explanation.BringToCommonDenominatorWithNonFractionalTerm,
                        move(fraction.unsignedPattern),
                        move(nonFractionalTerm.unsignedPattern),
                    ),
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
            val numerator = AnyPattern()
            val denominator = AnyPattern()
            val innerFraction = fractionOf(numerator, denominator)
            val outerNumerator = AnyPattern()
            val outerFraction = fractionOf(outerNumerator, innerFraction)

            onPattern(outerFraction) {
                ruleResult(
                    toExpr = productOf(move(outerNumerator), get(innerFraction).inverse()),
                    explanation = metadata(Explanation.SimplifyFractionWithFractionDenominator, move(outerFraction)),
                )
            }
        },
    ),

    DistributeFractionalPowerOverFraction(
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

    DistributePositiveIntegerPowerOverFraction(
        rule {
            val fraction = FractionPattern()
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

private fun Expression.canBeTurnedToFraction(): Boolean = when (this) {
    is Fraction -> false
    is Power -> firstChild.canBeTurnedToFraction()
    // is NullaryOperator -> true
    else -> children.all { it.canBeTurnedToFraction() }
}
