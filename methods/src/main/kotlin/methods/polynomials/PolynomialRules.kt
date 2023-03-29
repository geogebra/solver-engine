package methods.polynomials

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.Label
import engine.expressions.equationOf
import engine.expressions.implicitProductOf
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
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.RationalPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.equationSystemOf
import engine.patterns.integerCondition
import engine.patterns.monomialPattern
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.rationalMonomialPattern
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.Transformation
import engine.steps.metadata.metadata
import engine.utility.isEven
import engine.utility.isSquare
import engine.utility.times
import java.math.BigInteger

enum class PolynomialRules(override val runner: Rule) : RunnerMethod {
    CollectUnitaryMonomialsInProduct(collectUnitaryMonomialsInProduct),
    NormalizeMonomial(normalizeMonomial),
    DistributeMonomialToIntegerPower(distributeMonomialToIntegerPower),
    DistributeProductToIntegerPower(distributeProductToIntegerPower),
    NormalizePolynomial(normalizePolynomial),

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

    ExtractCommonTerms(
        rule {
            val sum = sumContaining()

            onPattern(sum) {
                val terms = get(sum).children.map {
                    if (it.operator == UnaryExpressionOperator.Minus) {
                        Pair(true, it.firstChild.flattenedProductChildren())
                    } else {
                        Pair(false, it.flattenedProductChildren())
                    }
                }

                var commonTerms = terms[0].second.toSet()
                for (i in 1 until terms.size) {
                    commonTerms = commonTerms.intersect(terms[i].second.toSet())
                }

                if (commonTerms.isEmpty()) return@onPattern null

                val leftovers = terms.map { (isNegative, childTerms) ->
                    val leftoverTerms = childTerms.toMutableList()
                    for (common in commonTerms) {
                        leftoverTerms.remove(common)
                    }
                    val leftover = if (leftoverTerms.isEmpty()) Constants.One else productOf(leftoverTerms)
                    if (isNegative) negOf(leftover) else leftover
                }

                ruleResult(
                    toExpr = productOf(commonTerms.toList() + sumOf(leftovers)),
                    explanation = metadata(Explanation.ExtractCommonTerms),
                )
            }
        },
    ),

    RewriteDifferenceOfSquares(
        rule {
            val firstCoeff = integerCondition(UnsignedIntegerPattern()) { it.isSquare() }
            val secondCoeff = integerCondition(UnsignedIntegerPattern()) { it.isSquare() }

            val firstExponent = integerCondition(UnsignedIntegerPattern()) { it.isEven() }
            val secondExponent = integerCondition(UnsignedIntegerPattern()) { it.isEven() }

            val firstBase = ArbitraryVariablePattern()
            val secondBase = ArbitraryVariablePattern()

            val firstPower = powerOf(firstBase, firstExponent)
            val secondPower = powerOf(secondBase, secondExponent)

            val first = oneOf(firstCoeff, firstPower, productOf(firstCoeff, firstPower))
            val second = oneOf(secondCoeff, secondPower, productOf(secondCoeff, secondPower))

            val sum = commutativeSumOf(first, negOf(second))

            onPattern(sum) {
                val firstSqrt = simplifiedProductOf(
                    if (isBound(firstCoeff)) integerOp(firstCoeff) { it.sqrt() } else Constants.One,
                    if (isBound(firstPower)) {
                        simplifiedPowerOf(
                            get(firstBase),
                            integerOp(firstExponent) { it.shiftRight(1) },
                        )
                    } else {
                        Constants.One
                    },
                )

                val secondSqrt = simplifiedProductOf(
                    if (isBound(secondCoeff)) integerOp(secondCoeff) { it.sqrt() } else Constants.One,
                    if (isBound(secondPower)) {
                        simplifiedPowerOf(
                            get(secondBase),
                            integerOp(secondExponent) { it.shiftRight(1) },
                        )
                    } else {
                        Constants.One
                    },
                )

                ruleResult(
                    toExpr = sum.substitute(
                        powerOf(firstSqrt, Constants.Two),
                        negOf(powerOf(secondSqrt, Constants.Two)),
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
                ruleResult(
                    toExpr = productOf(
                        sum.substitute(move(firstBase), negOf(move(secondBase))),
                        sum.substitute(move(firstBase), move(secondBase)),
                    ),
                    explanation = metadata(Explanation.ApplyDifferenceOfSquaresFormula),
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
                    toExpr = engine.expressions.equationSystemOf(
                        equationOf(move(variable1), combineTo(rhs1, rhs2, xp(solution1))),
                        equationOf(move(variable2), combineTo(rhs1, rhs2, xp(solution2))),
                    ),
                    explanation = metadata(Explanation.SolveSumProductDiophantineEquationSystemByGuessing),
                )
            }
        },
    ),

    FactorTrinomialToSquare(factorTrinomialToSquare),
}

private val collectUnitaryMonomialsInProduct = rule {
    val commonVariable = ArbitraryVariablePattern()
    val product = productContaining(monomialPattern(commonVariable), monomialPattern(commonVariable))
    val negProduct = optionalNegOf(product)

    onPattern(negProduct) {
        val factors = get(product).flattenedProductChildren()
        val monomialFactorPattern = monomialPattern(commonVariable)
        val monomialFactors = mutableListOf<Expression>()
        val constantFactors = mutableListOf<Expression>()
        val otherFactors = mutableListOf<Expression>()
        for (factor in factors) {
            if (factor.isConstant()) {
                constantFactors.add(factor)
                continue
            }
            val match = matchPattern(monomialFactorPattern, factor)
            if (match == null) {
                otherFactors.add(factor)
            } else {
                monomialFactors.add(monomialFactorPattern.getPower(match)!!)
                val coeff = monomialFactorPattern.coefficient(match)

                // It would be better to just know when there is no coefficient
                if (coeff != Constants.One) {
                    constantFactors.add(coeff)
                }
            }
        }

        if (constantFactors.isEmpty() && otherFactors.isEmpty()) {
            return@onPattern null
        }
        var signCopied = false

        val monomialProduct = if (constantFactors.isEmpty()) {
            productOf(monomialFactors).withLabel(Label.B)
        } else {
            val hasNegativeConstantFactor = constantFactors.any { it.operator == UnaryExpressionOperator.Minus }
            if (hasNegativeConstantFactor) {
                constantFactors[0] = copySign(negProduct, constantFactors[0])
                signCopied = true
            }
            implicitProductOf(
                productOf(constantFactors).withLabel(Label.A),
                productOf(monomialFactors).withLabel(Label.B),
            )
        }

        val result = if (otherFactors.isEmpty()) {
            monomialProduct
        } else {
            productOf(monomialProduct, productOf(otherFactors))
        }

        ruleResult(
            toExpr = if (signCopied) result else copySign(negProduct, result),
            explanation = metadata(Explanation.CollectUnitaryMonomialsInProduct, move(commonVariable)),
        )
    }
}

private val normalizeMonomial = rule {
    val monomial = monomialPattern(ArbitraryVariablePattern(), positiveOnly = true)

    onPattern(monomial) {
        val before = get(monomial.key)
        val coeff = get(monomial::coefficient)!!
        val normalized = when {
            coeff == Constants.Zero -> move(coeff)
            coeff == Constants.One -> move(monomial.powerPattern)
            coeff.operator != UnaryExpressionOperator.Minus ->
                productOf(
                    coeff,
                    move(monomial.powerPattern),
                )
            coeff.firstChild == Constants.One -> negOf(move(monomial.powerPattern))
            else -> negOf(productOf(coeff.firstChild, move(monomial.powerPattern)))
        }
        if (normalized == before) {
            null
        } else {
            ruleResult(
                toExpr = normalized,
                explanation = metadata(Explanation.NormalizeMonomial),
            )
        }
    }
}

private val distributeMonomialToIntegerPower = rule {
    val monomial = monomialPattern(ArbitraryVariablePattern())
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(monomial, exponent)

    onPattern(power) {
        val coeff = get(monomial::coefficient)!!
        if (coeff != Constants.One) {
            ruleResult(
                toExpr = productOf(
                    powerOf(coeff, move(exponent)).withLabel(Label.A),
                    powerOf(move(monomial.powerPattern), move(exponent)),
                ),
                gmAction = drag(exponent, monomial),
                explanation = metadata(Explanation.DistributeProductToIntegerPower),
            )
        } else {
            null
        }
    }
}

private val distributeProductToIntegerPower = rule {
    val testMonomialFactor = monomialPattern(ArbitraryVariablePattern())
    val product = productContaining(testMonomialFactor)
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(product, exponent)

    onPattern(power) {
        val factors = get(product).flattenedProductChildren()
        val (constantFactors, otherFactors) = factors.partition { it.isConstant() }
        val factorPowers = mutableListOf<Expression>()
        if (constantFactors.isNotEmpty()) {
            factorPowers.add(powerOf(productOf(constantFactors), move(exponent)).withLabel(Label.A))
        }
        factorPowers.addAll(otherFactors.map { powerOf(it, move(exponent)) })
        ruleResult(
            toExpr = productOf(factorPowers),
            gmAction = drag(exponent, product),
            explanation = metadata(Explanation.DistributeProductToIntegerPower),
        )
    }
}

private val normalizePolynomial = rule {
    val commonVariable = ArbitraryVariablePattern()
    val sum = condition(sumContaining(monomialPattern(commonVariable))) { it.variables.size == 1 }

    onPattern(sum) {
        val terms = get(sum).children
        val monomialPattern = monomialPattern(commonVariable)

        // Find the degree of each term so we can decide whether the sum is normalized already.
        // If we find a non-monomial non-constant term, we don't have a polynomial, so we cannot
        // normalize it
        val termsWithDegree = terms.map { term ->
            val termOrder = when (val match = matchPattern(monomialPattern, term)) {
                null -> if (term.isConstant()) BigInteger.ZERO else return@onPattern null
                else -> monomialPattern.exponent.getBoundInt(match)
            }
            Pair(term, termOrder)
        }

        // It's normalized if the degrees are in non-increasing order
        val isNormalized = termsWithDegree.zipWithNext { t1, t2 -> t1.second >= t2.second }.all { it }

        when {
            isNormalized -> null
            else -> {
                val termsInDescendingOrder = termsWithDegree.sortedByDescending { it.second }.map { it.first }
                ruleResult(
                    toExpr = sumOf(termsInDescendingOrder),
                    explanation = metadata(Explanation.NormalizePolynomial),
                    tags = listOf(Transformation.Tag.Rearrangement),
                )
            }
        }
    }
}

private val factorTrinomialToSquare = rule {
    val variable = ArbitraryVariablePattern()

    val squaredOrder = UnsignedIntegerPattern()
    val squaredTerm = powerOf(variable, squaredOrder)
    val baseTerm = rationalMonomialPattern(variable)
    val constantTerm = RationalPattern() // c

    val trinomial = ConditionPattern(
        commutativeSumOf(squaredTerm, baseTerm, constantTerm),
        integerCondition(squaredOrder, baseTerm.exponent) { a, b -> a == BigInteger.TWO * b },
    )

    onPattern(trinomial) {
        val baseCoefficient = getCoefficientValue(baseTerm.ptn)!!
        val constant = getValue(constantTerm)!!

        @Suppress("MagicNumber")
        val delta = baseCoefficient.squared() - 4 * constant

        if (delta.isZero()) {
            ruleResult(
                toExpr = powerOf(
                    sumOf(
                        simplifiedPowerOf(factor(variable), move(baseTerm.exponent)),
                        productOf(Constants.OneHalf, get(baseTerm::coefficient)!!),
                    ),
                    introduce(Constants.Two),
                ),
                explanation = metadata(Explanation.FactorTrinomialToSquare),
            )
        } else {
            null
        }
    }
}
