package methods.factor

import engine.expressions.Combine
import engine.expressions.Constants
import engine.expressions.DefaultView
import engine.expressions.Expression
import engine.expressions.Factor
import engine.expressions.IntegerExpression
import engine.expressions.IntegerView
import engine.expressions.Label
import engine.expressions.Minus
import engine.expressions.Power
import engine.expressions.Sum
import engine.expressions.SumView
import engine.expressions.View
import engine.expressions.areEquivalentSums
import engine.expressions.bracketOf
import engine.expressions.equationOf
import engine.expressions.explicitProductOf
import engine.expressions.leadingCoefficientOfPolynomial
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.simplifiedFractionOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.statementSystemOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.RationalPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.VariableExpressionPattern
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.integerCondition
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productOf
import engine.patterns.rationalMonomialPattern
import engine.patterns.statementSystemOf
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.metadata
import engine.utility.Rational
import engine.utility.cbrt
import engine.utility.divisibleBy
import engine.utility.isCube
import engine.utility.isEven
import engine.utility.isSquare
import engine.utility.times
import methods.solvable.simplifiedNegOfSum
import java.math.BigInteger

enum class FactorRules(override val runner: Rule) : RunnerMethod {

    FactorGreatestCommonIntegerFactor(
        rule {
            onPattern(sumContaining()) {
                val sumView = SumView(expression as Sum) {
                    if (it is IntegerExpression) IntegerFactorView(it) else DefaultView(it)
                }

                val integerFactors = sumView.termViews.map {
                    it.findSingleFactor<IntegerFactorView>()
                        ?: return@onPattern null
                }

                val gcd = integerFactors.fold(BigInteger.ZERO) { acc, n -> acc.gcd(n.value) }
                if (gcd == BigInteger.ONE) return@onPattern null

                val commonIntegerFactor = xp(gcd).withOrigin(Factor(integerFactors.map { it.original }))

                for (integerFactor in integerFactors) {
                    integerFactor.changeValue(integerFactor.value / gcd)
                }

                ruleResult(
                    toExpr = productOf(commonIntegerFactor, sumView.recombine()),
                    explanation = metadata(Explanation.FactorGreatestCommonIntegerFactor),
                )
            }
        },
    ),

    FactorCommonFactor(
        rule {
            onPattern(sumContaining()) {
                val sumView = SumView(expression as Sum) { CommonFactorView(it) }

                for (factor in sumView.termViews[0].factors) {
                    if (factor.base == Constants.One) continue

                    val (minExponent, sameBaseFactors) = sumView.findSameBaseFactors(factor.base)
                    if (minExponent > BigInteger.ZERO) {
                        val commonFactor = simplifiedPowerOf(
                            factor.base.withOrigin(Factor(sameBaseFactors.map { it.base })),
                            xp(minExponent)
                                .withOrigin(Factor(sameBaseFactors.mapNotNull { it.exponent })),
                        )

                        for (sameBaseFactor in sameBaseFactors) {
                            sameBaseFactor.changeExponent(sameBaseFactor.exponentValue - minExponent)
                        }

                        return@onPattern ruleResult(
                            toExpr = productOf(commonFactor, sumView.recombine()),
                            explanation = metadata(Explanation.FactorCommonFactor),
                        )
                    }
                }

                null
            }
        },
    ),

    RearrangeEquivalentSums(
        rule {
            onPattern(sumContaining()) {
                val sumView = SumView(expression as Sum) { CommonFactorView(it) }

                for (factor in sumView.termViews[0].factors) {
                    val equivalentBaseFactors = sumView.findEquivalentBaseFactors(factor.base)

                    if (equivalentBaseFactors.isNotEmpty()) {
                        equivalentBaseFactors.forEach { it.changeBase(factor.base) }
                        return@onPattern ruleResult(
                            toExpr = sumView.recombine(),
                            explanation = metadata(Explanation.RearrangeEquivalentSums),
                        )
                    }
                }
                null
            }
        },
    ),

    FactorNegativeSignOfLeadingCoefficient(
        rule {
            val polynomial = sumContaining()

            onPattern(polynomial) {
                val polynomialVal = get(polynomial) as Sum
                val leadingCoefficient = leadingCoefficientOfPolynomial(context, polynomialVal)
                if ((leadingCoefficient == null) || (leadingCoefficient !is Minus)) {
                    return@onPattern null
                }

                val negatedPolynomial = simplifiedNegOfSum(polynomialVal)

                ruleResult(
                    toExpr = negOf(negatedPolynomial),
                    explanation = metadata(Explanation.FactorNegativeSignOfLeadingCoefficient),
                )
            }
        },
    ),

    RewriteSquareOfBinomial(rewriteSquareOfBinomial),

    ApplySquareOfBinomialFormula(
        rule {
            val two = FixedPattern(Constants.Two)

            val firstBase = AnyPattern()
            val secondBase = AnyPattern()

            val firstPower = powerOf(firstBase, two)
            val mixedTerm = optionalNegOf(commutativeProductOf(two, firstBase, secondBase))
            val secondPower = powerOf(secondBase, two)

            val sum = commutativeSumOf(firstPower, mixedTerm, secondPower)

            onPattern(sum) {
                ruleResult(
                    toExpr = powerOf(sumOf(factor(firstBase), copySign(mixedTerm, factor(secondBase))), factor(two)),
                    explanation = metadata(Explanation.ApplySquareOfBinomialFormula),
                )
            }
        },
    ),

    RewriteCubeOfBinomial(rewriteCubeOfBinomial),

    ApplyCubeOfBinomialFormula(
        rule {
            val three = FixedPattern(Constants.Three)

            val firstBase = AnyPattern()
            val secondBase = AnyPattern()

            val firstPower = powerOf(firstBase, three)
            val firstMixedTerm = commutativeProductOf(
                three,
                powerOf(firstBase, FixedPattern(Constants.Two)),
                secondBase,
            )
            val secondMixedTerm = commutativeProductOf(
                three,
                firstBase,
                powerOf(secondBase, FixedPattern(Constants.Two)),
            )
            val secondPower = powerOf(secondBase, three)

            val sum = commutativeSumOf(firstPower, firstMixedTerm, secondMixedTerm, secondPower)

            onPattern(sum) {
                ruleResult(
                    toExpr = powerOf(sumOf(factor(firstBase), factor(secondBase)), factor(three)),
                    explanation = metadata(Explanation.ApplyCubeOfBinomialFormula),
                )
            }
        },
    ),

    RewriteDifferenceOfSquares(
        rule {
            val firstConstant = RationalPattern()
            val secondConstant = RationalPattern()

            val firstExponent = integerCondition(UnsignedIntegerPattern()) { it.isEven() }
            val secondExponent = integerCondition(UnsignedIntegerPattern()) { it.isEven() }

            val firstBase = AnyPattern()
            val secondBase = AnyPattern()

            val firstPower = withOptionalRationalCoefficient(powerOf(firstBase, firstExponent))
            val secondPower = withOptionalRationalCoefficient(powerOf(secondBase, secondExponent))

            val firstTerm = oneOf(firstConstant, firstPower)
            val secondTerm = oneOf(secondConstant, secondPower)

            val sum = commutativeSumOf(firstTerm, negOf(secondTerm))

            onPattern(sum) {
                // we require at least one non-trivial term to factor
                if (!isBound(firstPower) && !isBound(secondPower)) return@onPattern null

                val firstCoeffSqrt = if (isBound(firstConstant)) {
                    fractionSqrt(getValue(firstConstant))
                } else {
                    fractionSqrt(getCoefficientValue(firstPower))
                }

                val secondCoeffSqrt = if (isBound(secondConstant)) {
                    fractionSqrt(getValue(secondConstant))
                } else {
                    fractionSqrt(getCoefficientValue(secondPower))
                }

                // disallow creating roots when factoring for now
                if (firstCoeffSqrt == null || secondCoeffSqrt == null) return@onPattern null

                val firstSqrt = simplifiedProductOf(
                    firstCoeffSqrt,
                    if (isBound(firstPower)) {
                        simplifiedPowerOf(
                            get(firstBase),
                            integerOp(firstExponent) { it / 2.toBigInteger() },
                        )
                    } else {
                        Constants.One
                    },
                )

                val secondSqrt = simplifiedProductOf(
                    secondCoeffSqrt,
                    if (isBound(secondPower)) {
                        simplifiedPowerOf(
                            get(secondBase),
                            integerOp(secondExponent) { it / 2.toBigInteger() },
                        )
                    } else {
                        Constants.One
                    },
                )

                ruleResult(
                    toExpr = sum.substitute(
                        transformTo(firstTerm, powerOf(firstSqrt, Constants.Two)),
                        negOf(transformTo(secondTerm, powerOf(secondSqrt, Constants.Two))),
                    ),
                    explanation = metadata(Explanation.RewriteDifferenceOfSquares),
                )
            }
        },
    ),

    ApplyDifferenceOfSquaresFormula(
        rule {
            val firstBase = AnyPattern()
            val secondBase = AnyPattern()

            val firstPower = powerOf(firstBase, FixedPattern(Constants.Two))
            val secondPower = powerOf(secondBase, FixedPattern(Constants.Two))

            val sum = commutativeSumOf(firstPower, negOf(secondPower))

            onPattern(sum) {
                val (dFirstBase, dSecondBase) = distribute(firstBase, secondBase)

                ruleResult(
                    toExpr = productOf(
                        sum.substitute(dFirstBase, negOf(dSecondBase)),
                        sum.substitute(dFirstBase, dSecondBase),
                    ),
                    explanation = metadata(Explanation.ApplyDifferenceOfSquaresFormula),
                )
            }
        },
    ),

    RewriteSumAndDifferenceOfCubes(
        rule {
            val firstConstant = RationalPattern()
            val secondConstant = RationalPattern()

            val firstExponent = integerCondition(UnsignedIntegerPattern()) { it.divisibleBy(3) }
            val secondExponent = integerCondition(UnsignedIntegerPattern()) { it.divisibleBy(3) }

            val firstBase = AnyPattern()
            val secondBase = AnyPattern()

            val firstPower = withOptionalRationalCoefficient(powerOf(firstBase, firstExponent))
            val secondPower = withOptionalRationalCoefficient(powerOf(secondBase, secondExponent))

            val first = oneOf(firstConstant, firstPower)
            val second = oneOf(secondConstant, secondPower)

            val signedSecond = optionalNegOf(second)

            val sum = commutativeSumOf(first, signedSecond)

            onPattern(sum) {
                // we require at least one non-trivial term to factor
                if (!isBound(firstPower) && !isBound(secondPower)) return@onPattern null

                val firstCoeffCbrt = if (isBound(firstConstant)) {
                    fractionCbrt(getValue(firstConstant))
                } else {
                    fractionCbrt(getCoefficientValue(firstPower))
                }

                val secondCoeffCbrt = if (isBound(secondConstant)) {
                    fractionCbrt(getValue(secondConstant))
                } else {
                    fractionCbrt(getCoefficientValue(secondPower))
                }

                // disallow creating roots when factoring for now
                if (firstCoeffCbrt == null || secondCoeffCbrt == null) return@onPattern null

                val firstCbrt = simplifiedProductOf(
                    firstCoeffCbrt,
                    if (isBound(firstPower)) {
                        simplifiedPowerOf(
                            get(firstBase),
                            integerOp(firstExponent) { it / 3.toBigInteger() },
                        )
                    } else {
                        Constants.One
                    },
                )

                val secondCbrt = simplifiedProductOf(
                    secondCoeffCbrt,
                    if (isBound(secondPower)) {
                        simplifiedPowerOf(
                            get(secondBase),
                            integerOp(secondExponent) { it / 3.toBigInteger() },
                        )
                    } else {
                        Constants.One
                    },
                )

                ruleResult(
                    toExpr = sum.substitute(
                        transformTo(firstPower, powerOf(firstCbrt, Constants.Three)),
                        transformTo(secondPower, copySign(signedSecond, powerOf(secondCbrt, Constants.Three))),
                    ),
                    explanation = if (signedSecond.isNeg()) {
                        metadata(Explanation.RewriteDifferenceOfCubes)
                    } else {
                        metadata(Explanation.RewriteSumOfCubes)
                    },
                )
            }
        },
    ),

    ApplyDifferenceOfCubesFormula(
        rule {
            val firstBase = AnyPattern()
            val secondBase = AnyPattern()

            val firstPower = powerOf(firstBase, FixedPattern(Constants.Three))
            val secondPower = powerOf(secondBase, FixedPattern(Constants.Three))

            val sum = commutativeSumOf(firstPower, negOf(secondPower))

            onPattern(sum) {
                val (dFirstBase, dSecondBase) = distribute(firstBase, secondBase)

                ruleResult(
                    toExpr = productOf(
                        sumOf(dFirstBase, negOf(dSecondBase)),
                        sumOf(
                            powerOf(dFirstBase, Constants.Two),
                            productOf(dFirstBase, dSecondBase),
                            powerOf(dSecondBase, Constants.Two),
                        ),
                    ),
                    explanation = metadata(Explanation.ApplyDifferenceOfCubesFormula),
                )
            }
        },
    ),

    ApplySumOfCubesFormula(
        rule {
            val firstBase = AnyPattern()
            val secondBase = AnyPattern()

            val firstPower = powerOf(firstBase, FixedPattern(Constants.Three))
            val secondPower = powerOf(secondBase, FixedPattern(Constants.Three))

            val sum = sumOf(firstPower, secondPower)

            onPattern(sum) {
                val (dFirstBase, dSecondBase) = distribute(firstBase, secondBase)

                ruleResult(
                    toExpr = productOf(
                        sumOf(dFirstBase, dSecondBase),
                        sumOf(
                            powerOf(dFirstBase, Constants.Two),
                            negOf(productOf(dFirstBase, dSecondBase)),
                            powerOf(dSecondBase, Constants.Two),
                        ),
                    ),
                    explanation = metadata(Explanation.ApplySumOfCubesFormula),
                )
            }
        },
    ),

    SolveSumProductDiophantineEquationSystemByGuessing(
        rule {
            val variable1 = ArbitraryVariablePattern()
            val variable2 = ArbitraryVariablePattern()

            val rhs1 = SignedIntegerPattern()
            val rhs2 = SignedIntegerPattern()

            val equation1 = equationOf(sumOf(variable1, variable2), rhs1)
            val equation2 = equationOf(productOf(variable1, variable2), rhs2)

            val equationSystem = statementSystemOf(equation1, equation2)

            onPattern(equationSystem) {
                val rhs1Val = getValue(rhs1)
                val rhs2Val = getValue(rhs2)

                val discriminant = rhs1Val * rhs1Val - rhs2Val.shiftLeft(2)
                if (discriminant.signum() < 0) return@onPattern null

                val discriminantSqrt = discriminant.sqrt()
                if (discriminantSqrt * discriminantSqrt != discriminant) return@onPattern null

                // the solutions are the negations of the solutions of the quadratic equation
                val solution1 = (rhs1Val - discriminantSqrt).shiftRight(1)
                val solution2 = (rhs1Val + discriminantSqrt).shiftRight(1)

                ruleResult(
                    toExpr = statementSystemOf(
                        equationOf(move(variable1), combineTo(rhs1, rhs2, xp(solution1))),
                        equationOf(move(variable2), combineTo(rhs1, rhs2, xp(solution2))),
                    ),
                    explanation = metadata(Explanation.SolveSumProductDiophantineEquationSystemByGuessing),
                )
            }
        },
    ),
}

class GroupPolynomial(val i: Int) : RunnerMethod {
    override val name = "GroupPolynomial_$i"

    override val runner = rule {
        val pattern = condition(sumContaining()) { it.childCount > 2 }

        onPattern(pattern) {
            val terms = expression.children
            val firstSum = bracketOf(sumOf(terms.subList(0, i)))
            val secondSum = bracketOf(sumOf(terms.subList(i, terms.size)))

            ruleResult(
                toExpr = sumOf(firstSum, secondSum),
                explanation = metadata(Explanation.GroupPolynomial),
            )
        }
    }
}

private fun fractionSqrt(f: Rational?) = f?.let {
    if (it.numerator.isSquare() && it.denominator.isSquare()) {
        simplifiedFractionOf(xp(it.numerator.sqrt()), xp(it.denominator.sqrt()))
    } else {
        null
    }
}
private fun fractionCbrt(f: Rational?) = f?.let {
    if (it.numerator.isCube() && it.denominator.isCube()) {
        simplifiedFractionOf(xp(it.numerator.cbrt()), xp(it.denominator.cbrt()))
    } else {
        null
    }
}

private class IntegerFactorView(override val original: IntegerExpression) : IntegerView {
    private var newValue: BigInteger? = null

    override val value get() = newValue ?: original.value

    override fun changeValue(newValue: BigInteger) {
        this.newValue = newValue
    }

    override fun recombine(): Expression? {
        return when {
            newValue == null -> original
            newValue == BigInteger.ONE -> null
            else -> xp(newValue!!).withOrigin(Combine(listOf(original)))
        }
    }
}

private class CommonFactorView(override val original: Expression) : View {

    private var newBase: Expression? = null
    private var newExponent: BigInteger? = null

    val base get() = if (original is Power) original.base else original
    val exponent get() = if (original is Power) original.exponent as? IntegerExpression else null

    val exponentValue: BigInteger get() = exponent?.value ?: BigInteger.ONE

    fun changeBase(newBase: Expression) {
        this.newBase = newBase
    }

    fun changeExponent(newExponent: BigInteger) {
        this.newExponent = newExponent
    }

    private val baseWithOrigin: Expression get() = newBase?.withOrigin(Combine(listOf(base))) ?: base
    private val exponentWithOrigin: Expression? get() {
        val exponent = this.exponent
        val newExponent = this.newExponent

        return if (newExponent != null) {
            if (exponent != null) {
                xp(newExponent).withOrigin(Combine(listOf(exponent)))
            } else {
                xp(newExponent)
            }
        } else {
            exponent
        }
    }

    override fun recombine(): Expression? {
        return when {
            newBase == null && newExponent == null -> original
            newExponent == BigInteger.ZERO -> null
            newExponent == BigInteger.ONE -> baseWithOrigin
            exponent == null && newExponent == null -> baseWithOrigin
            // the previous branch makes sure exponentWithOrigin is not null
            else -> powerOf(baseWithOrigin, exponentWithOrigin!!)
        }
    }
}

private fun SumView<CommonFactorView>.findEquivalentBaseFactors(base: Expression): List<CommonFactorView> {
    if (base !is Sum) {
        return emptyList()
    }
    val equivalentBaseFactors = ArrayList<CommonFactorView>(termViews.size)
    for (term in termViews) {
        val equivalentFactor = term.findSingleFactor { areEquivalentSums(it.base, base) } ?: return emptyList()
        if (equivalentFactor.base != base) equivalentBaseFactors.add(equivalentFactor)
    }
    return equivalentBaseFactors
}

private fun SumView<CommonFactorView>.findSameBaseFactors(base: Expression): Pair<BigInteger, List<CommonFactorView>> {
    val sameBaseFactors = ArrayList<CommonFactorView>(termViews.size)
    for (term in termViews) {
        val factor = term.findSingleFactor { it.base == base && it.exponentValue != BigInteger.ZERO }
            ?: return Pair(BigInteger.ZERO, emptyList())
        sameBaseFactors.add(factor)
    }
    return Pair(sameBaseFactors.minOfOrNull { it.exponentValue } ?: BigInteger.ZERO, sameBaseFactors)
}

val rewriteSquareOfBinomial = rule {
    val variable = VariableExpressionPattern()

    val squaredTerm = rationalMonomialPattern(variable, positiveOnly = true)
    val baseTerm = rationalMonomialPattern(variable)
    val constantTerm = RationalPattern()

    val pattern = ConditionPattern(
        commutativeSumOf(squaredTerm, baseTerm, constantTerm),
        integerCondition(squaredTerm.exponent, baseTerm.exponent) { a, b -> a == BigInteger.TWO * b },
    )

    onPattern(pattern) {
        val squaredCoefficient = getCoefficientValue(squaredTerm.ptn)!!
        val baseCoefficient = getCoefficientValue(baseTerm.ptn)!!
        val constant = getValue(constantTerm)!!

        @Suppress("MagicNumber")
        val delta = baseCoefficient.squared() - 4 * squaredCoefficient * constant

        if (delta.isZero()) {
            val squaredCoefficientSqrt = fractionSqrt(squaredCoefficient) ?: return@onPattern null
            val squaredTermSqrt = simplifiedProductOf(squaredCoefficientSqrt, get(baseTerm.powerPattern))
                .withLabel(Label.A)

            val constantSqrt = fractionSqrt(constant)
                ?.let { if (baseCoefficient.isNeg()) negOf(it) else it }
                ?.withLabel(Label.A)
                ?: return@onPattern null

            ruleResult(
                toExpr = sumOf(
                    transformTo(squaredTerm, powerOf(squaredTermSqrt, Constants.Two)),
                    transformTo(baseTerm, explicitProductOf(Constants.Two, constantSqrt, squaredTermSqrt)),
                    transformTo(constantTerm, powerOf(constantSqrt, Constants.Two)),
                ),
                explanation = metadata(Explanation.RewriteSquareOfBinomial),
            )
        } else {
            null
        }
    }
}

val rewriteCubeOfBinomial = rule {
    val variable = VariableExpressionPattern()

    val cubedTerm = rationalMonomialPattern(variable, positiveOnly = true)
    val squaredTerm = rationalMonomialPattern(variable)
    val baseTerm = rationalMonomialPattern(variable, positiveOnly = true)
    val constantTerm = RationalPattern()

    val sum = ConditionPattern(
        commutativeSumOf(cubedTerm, squaredTerm, baseTerm, constantTerm),
        integerCondition(cubedTerm.exponent, squaredTerm.exponent, baseTerm.exponent) { n1, n2, n3 ->
            n1 == 3.toBigInteger() * n3 && n2 == 2.toBigInteger() * n3
        },
    )

    onPattern(sum) {
        val cubedCoefficient = getCoefficientValue(cubedTerm.ptn)!!
        val squaredCoefficient = getCoefficientValue(squaredTerm.ptn)!!
        val baseCoefficient = getCoefficientValue(baseTerm.ptn)!!
        val constant = getValue(constantTerm)!!

        // x + y + z + w can be written as a^3 + 3 a^2 b + 3 a b^2 + b^3 iff
        // y^3 - 27 x^2 w = 0 and z^3 - 27 x w^2 = 0
        @Suppress("MagicNumber")
        val delta1 = squaredCoefficient.cubed() - 27 * cubedCoefficient.squared() * constant

        @Suppress("MagicNumber")
        val delta2 = baseCoefficient.cubed() - 27 * cubedCoefficient * constant.squared()

        if (delta1.isZero() && delta2.isZero()) {
            val cubedCoefficientCbrt = fractionCbrt(cubedCoefficient) ?: return@onPattern null
            val cubedTermCbrt = simplifiedProductOf(cubedCoefficientCbrt, get(baseTerm.powerPattern)).withLabel(Label.A)

            val constantCbrt = fractionCbrt(constant)?.withLabel(Label.A) ?: return@onPattern null

            ruleResult(
                toExpr = sumOf(
                    transformTo(cubedTerm, powerOf(cubedTermCbrt, Constants.Three)),
                    transformTo(
                        squaredTerm,
                        explicitProductOf(
                            Constants.Three,
                            powerOf(cubedTermCbrt, Constants.Two),
                            constantCbrt,
                        ),
                    ),
                    transformTo(
                        baseTerm,
                        explicitProductOf(
                            Constants.Three,
                            cubedTermCbrt,
                            powerOf(constantCbrt, Constants.Two),
                        ),
                    ),
                    transformTo(
                        constantTerm,
                        powerOf(constantCbrt, Constants.Three),
                    ),
                ),
                explanation = metadata(Explanation.RewriteCubeOfBinomial),
            )
        } else {
            null
        }
    }
}
