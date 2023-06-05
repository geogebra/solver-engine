package methods.factor

import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.Factor
import engine.expressions.bracketOf
import engine.expressions.equationOf
import engine.expressions.equationSystemOf
import engine.expressions.explicitProductOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.simplifiedFractionOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.RationalPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.equationSystemOf
import engine.patterns.integerCondition
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productOf
import engine.patterns.rationalMonomialPattern
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.patterns.withOptionalRationalCoefficient
import engine.sign.Sign
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

    SplitIntegersInMonomialsBeforeFactoring(
        rule {
            val sum = condition(sumContaining()) { !it.isConstant() }

            onPattern(sum) {
                val splitMonomials = MonomialGCF.splitIntegersInMonomials(get(sum).children)
                    ?: return@onPattern null

                ruleResult(
                    toExpr = sumOf(splitMonomials),
                    explanation = metadata(Explanation.SplitIntegersInMonomialsBeforeFactoring),
                )
            }
        },
    ),

    SplitVariablePowersInMonomialsBeforeFactoring(
        rule {
            val sum = condition(sumContaining()) { !it.isConstant() }

            onPattern(sum) {
                val sumValue = get(sum)

                if (sumValue.variables.size != 1) return@onPattern null

                val splitMonomials = MonomialGCF.splitVariablePowersInMonomials(
                    sumValue.children,
                    sumValue.variables.first(),
                ) ?: return@onPattern null

                ruleResult(
                    toExpr = sumOf(splitMonomials),
                    explanation = metadata(Explanation.SplitVariablePowersInMonomialsBeforeFactoring),
                )
            }
        },
    ),

    // -6x^2 + 18x -> -6 * x * x + 6 * 3 * x -> -6x(x - 3)
    ExtractCommonTerms(
        rule {
            val sum = sumContaining()

            onPattern(sum) {
                val terms = get(sum).children.map {
                    if (it.operator == UnaryExpressionOperator.Minus) {
                        Pair(true, it.firstChild.factors())
                    } else {
                        Pair(false, it.factors())
                    }
                }

                val commonTerms = mutableMapOf<Expression, MutableList<Expression>>()
                commonTerms.putAll(terms[0].second.map { it to mutableListOf(it) })

                for (i in 1 until terms.size) {
                    val currentTerms = terms[i].second
                    val keys = commonTerms.keys.toList() // done to avoid concurrent modification issue
                    for (commonTerm in keys) {
                        commonTerms.computeIfPresent(commonTerm) { _, occurrences ->
                            val index = currentTerms.indexOf(commonTerm)
                            if (index != -1) {
                                occurrences.add(currentTerms[index])
                                occurrences
                            } else {
                                null
                            }
                        }
                    }
                }

                if (commonTerms.isEmpty()) return@onPattern null

                val leftovers = sumOf(
                    terms.map { (isNegative, childTerms) ->
                        val leftoverTerms = childTerms.toMutableList()
                        for (common in commonTerms.keys) {
                            leftoverTerms.remove(common)
                        }
                        val leftover = if (leftoverTerms.isEmpty()) {
                            Constants.One
                        } else {
                            productOf(leftoverTerms.map { move(it) })
                        }
                        if (isNegative) negOf(leftover) else leftover
                    },
                )

                val leadingSign = leadingCoefficientOfPolynomial(leftovers)?.signOf()

                val factoredCommonTerms = commonTerms.entries
                    .map { (expression, origins) -> expression.withOrigin(Factor(origins)) }

                val toExpr = if (leadingSign == Sign.NEGATIVE) {
                    negOf(productOf(factoredCommonTerms + simplifiedNegOfSum(leftovers)))
                } else {
                    productOf(factoredCommonTerms + leftovers)
                }

                ruleResult(
                    toExpr = toExpr,
                    explanation = metadata(Explanation.ExtractCommonTerms),
                )
            }
        },
    ),

    FactorNegativeSignOfLeadingCoefficient(
        rule {
            val polynomial = sumContaining()

            onPattern(polynomial) {
                val polynomialVal = get(polynomial)
                val leadingCoefficient = leadingCoefficientOfPolynomial(polynomialVal)
                if ((leadingCoefficient == null) || (leadingCoefficient.signOf() == Sign.POSITIVE)) {
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

    RewriteSquareOfBinomial(
        rule {
            val variable = ArbitraryVariablePattern()

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

                    val constantSqrt = fractionSqrt(constant)
                        ?.let { if (baseCoefficient.isNeg()) negOf(it) else it }
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
        },
    ),

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

    RewriteCubeOfBinomial(
        rule {
            val variable = ArbitraryVariablePattern()

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
                    val cubedTermCbrt = simplifiedProductOf(cubedCoefficientCbrt, get(baseTerm.powerPattern))

                    val constantCbrt = fractionCbrt(constant) ?: return@onPattern null

                    ruleResult(
                        toExpr = sumOf(
                            transformTo(cubedTerm, powerOf(cubedTermCbrt, Constants.Three)),
                            transformTo(
                                squaredTerm,
                                explicitProductOf(Constants.Three, powerOf(cubedTermCbrt, Constants.Two), constantCbrt),
                            ),
                            transformTo(
                                baseTerm,
                                explicitProductOf(Constants.Three, cubedTermCbrt, powerOf(constantCbrt, Constants.Two)),
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
        },
    ),

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

            val equationSystem = equationSystemOf(equation1, equation2)

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
                    toExpr = equationSystemOf(
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
