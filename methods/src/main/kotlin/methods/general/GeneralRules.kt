package methods.general

import engine.conditions.Sign
import engine.conditions.isDefinitelyNotUndefined
import engine.conditions.isDefinitelyNotZero
import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.MappedExpression
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.FractionPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.divideBy
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.oppositeSignPattern
import engine.patterns.optionalDivideBy
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.metadata.metadata
import java.math.BigDecimal
import java.math.BigInteger

val eliminateOneInProduct = rule {
    val one = FixedPattern(Constants.One)
    val pattern = productContaining(one)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(one, restOf(pattern)),
            explanation = metadata(Explanation.EliminateOneInProduct, move(one))
        )
    }
}

val eliminateZeroInSum = rule {
    val zero = FixedPattern(Constants.Zero)
    val pattern = sumContaining(zero)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(zero, restOf(pattern)),
            explanation = metadata(Explanation.EliminateZeroInSum, move(zero))
        )
    }
}

/**
 * 0 * anyX --> 0
 */
val evaluateProductContainingZero = rule {
    val zero = FixedPattern(Constants.Zero)
    val p = productContaining(zero)
    val pattern = condition(p) { expression ->
        expression.operands.all { it.operator != UnaryExpressionOperator.DivideBy }
    }

    onPattern(pattern) {
        TransformationResult(
            toExpr = transform(zero),
            explanation = metadata(Explanation.EvaluateProductContainingZero, move(zero))
        )
    }
}

/**
 * 0:anyX && anyX != 0 --> 0
 */
val evaluateZeroDividedByAnyValue = rule {
    val zero = FixedPattern(Constants.Zero)
    val divByPattern = numericCondition(SignedNumberPattern()) { it != BigDecimal.ZERO }
    val pattern = productContaining(zero, divideBy(divByPattern))

    onPattern(pattern) {
        TransformationResult(
            toExpr = transform(zero),
            explanation = metadata(Explanation.EvaluateZeroDividedByAnyValue, move(zero))
        )
    }
}

/**
 * anyX : 0 --> undefined
 */
val evaluateProductDividedByZeroAsUndefined = rule {
    val zero = SignedNumberPattern()
    val pattern = productContaining(divideBy(numericCondition(zero) { it.signum() == 0 }))

    onPattern(pattern) {
        TransformationResult(
            toExpr = transformTo(pattern, Constants.Undefined),
            explanation = metadata(Explanation.EvaluateProductDividedByZeroAsUndefined, move(zero))
        )
    }
}

val simplifyDoubleMinus = rule {
    val value = AnyPattern()
    val pattern = negOf(negOf(value))

    onPattern(pattern) {
        TransformationResult(
            toExpr = move(value),
            explanation = metadata(Explanation.SimplifyDoubleMinus, move(value))
        )
    }
}

val simplifyProductWithTwoNegativeFactors = rule {
    val f1 = AnyPattern()
    val f2 = AnyPattern()
    val fd1 = optionalDivideBy(negOf(f1))
    val fd2 = optionalDivideBy(negOf(f2))
    val product = productContaining(fd1, fd2)

    onPattern(product) {
        TransformationResult(
            toExpr = product.substitute(
                optionalDivideBy(fd1, move(f1)),
                optionalDivideBy(fd2, move(f2))
            ),
            explanation = metadata(Explanation.SimplifyProductWithTwoNegativeFactors)
        )
    }
}

val moveSignOfNegativeFactorOutOfProduct = rule {
    val f = AnyPattern()
    val fd = optionalDivideBy(negOf(f))
    val product = productContaining(fd)

    onPattern(product) {
        TransformationResult(
            toExpr = negOf(product.substitute(optionalDivideBy(fd, move(f)))),
            explanation = metadata(Explanation.MoveSignOfNegativeFactorOutOfProduct)
        )
    }
}

/**
 * anyX / 0 --> undefined
 */
val simplifyZeroDenominatorFractionToUndefined = rule {
    val zero = FixedPattern(Constants.Zero)
    val numerator = AnyPattern()
    val pattern = fractionOf(numerator, zero)

    onPattern(pattern) {
        TransformationResult(
            toExpr = transformTo(pattern, Constants.Undefined),
            explanation = metadata(Explanation.SimplifyZeroDenominatorFractionToUndefined, move(pattern))
        )
    }
}

/**
 * 0 / anyX = 0 && anyX != 0 --> 0
 */
val simplifyZeroNumeratorFractionToZero = rule {
    val zero = FixedPattern(Constants.Zero)
    val denominator = condition(AnyPattern()) { it.isDefinitelyNotZero() }
    val pattern = fractionOf(zero, denominator)

    onPattern(pattern) {
        TransformationResult(
            toExpr = transform(zero),
            explanation = metadata(Explanation.SimplifyZeroNumeratorFractionToZero, move(zero))
        )
    }
}

val simplifyUnitFractionToOne = rule {
    val common = AnyPattern()
    val pattern = fractionOf(common, common)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(common, introduce(Constants.One)),
            explanation = metadata(Explanation.SimplifyUnitFractionToOne)
        )
    }
}

val simplifyFractionWithOneDenominator = rule {
    val numerator = AnyPattern()
    val denominator = FixedPattern(Constants.One)
    val pattern = fractionOf(numerator, denominator)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(denominator, move(numerator)),
            explanation = metadata(Explanation.SimplifyFractionWithOneDenominator)
        )
    }
}

val cancelDenominator = rule {
    val common = AnyPattern()
    val numerator = productContaining(common, minSize = 2)
    val pattern = fractionOf(numerator, common)

    onPattern(pattern) {
        TransformationResult(
            toExpr = cancel(common, restOf(numerator)),
            explanation = metadata(Explanation.CancelDenominator)
        )
    }
}

val cancelCommonTerms = rule {
    val common = condition(AnyPattern()) { it != Constants.One }
    val numerator = productContaining(common, minSize = 2)
    val denominator = productContaining(common, minSize = 2)
    val fraction = fractionOf(numerator, denominator)

    onPattern(fraction) {
        TransformationResult(
            toExpr = cancel(common, fractionOf(restOf(numerator), restOf(denominator))),
            explanation = metadata(Explanation.CancelCommonTerms)
        )
    }
}

val factorMinusFromSum = rule {
    val sum = condition(sumContaining()) { expression ->
        expression.operands.all { it.operator == UnaryExpressionOperator.Minus }
    }

    onPattern(sum) {
        TransformationResult(
            toExpr = negOf(sumOf(get(sum)!!.children().map { move(it.children()[0]) })),
            explanation = metadata(Explanation.FactorMinusFromSum)
        )
    }
}

val simplifyProductOfConjugates = rule {
    val a = AnyPattern()
    val b = AnyPattern()
    val sum1 = commutativeSumOf(a, b)
    val sum2 = commutativeSumOf(a, negOf(b))
    val product = commutativeProductOf(sum1, sum2)

    onPattern(product) {
        TransformationResult(
            toExpr = sum2.substitute(
                powerOf(move(a), introduce(Constants.Two)),
                negOf(powerOf(move(b), introduce(Constants.Two)))
            ),
            explanation = metadata(Explanation.SimplifyProductOfConjugates)
        )
    }
}

/**
 * [ ( [x1^a1] * ... * [xn^an] ) ^ [ p/q ] ] --> [(x1^a1) ^ [p/q]] * ... * [(xn^an) ^ [p/q]]
 */
val distributePowerOfProduct = rule {
    val exponent = AnyPattern()
    val product = productContaining()
    val pattern = powerOf(product, exponent)

    onPattern(pattern) {
        TransformationResult(
            toExpr = productOf(get(product)!!.children().map { powerOf(move(it), move(exponent)) }),
            explanation = metadata(Explanation.DistributePowerOfProduct)
        )
    }
}

val expandBinomialSquared = rule {
    val a = AnyPattern()
    val b = AnyPattern()
    val pattern = powerOf(sumOf(a, b), FixedPattern(xp(2)))

    onPattern(pattern) {
        TransformationResult(
            toExpr = sumOf(
                powerOf(move(a), introduce(Constants.Two)),
                productOf(introduce(Constants.Two), move(a), move(b)),
                powerOf(move(b), introduce(Constants.Two))
            ),
            explanation = metadata(Explanation.ExpandBinomialSquared)
        )
    }
}

val rewriteDivisionAsFraction = rule {
    val product = productContaining(divideBy(AnyPattern()))

    onPattern(product) {
        val factors = get(product)!!.children()
        val division = factors.indexOfFirst { it.expr.operator == UnaryExpressionOperator.DivideBy }

        val result = mutableListOf<MappedExpression>()
        result.addAll(factors.subList(0, division - 1).map { move(it) })

        val denominator = factors[division].nthChild(0)

        result.add(
            fractionOf(
                move(factors[division - 1]),
                move(denominator)
            )
        )
        result.addAll(factors.subList(division + 1, factors.size).map { move(it) })

        TransformationResult(
            toExpr = productOf(result),
            explanation = metadata(Explanation.RewriteDivisionAsFraction)
        )
    }
}

/**
 * [([a^b]) ^ c] --> [a^b*c]
 */
val multiplyExponentsUsingPowerRule = rule {
    val base = AnyPattern()
    val exp1 = AnyPattern()
    val exp2 = AnyPattern()

    val pattern = powerOf(powerOf(base, exp1), exp2)

    onPattern(pattern) {
        TransformationResult(
            toExpr = powerOf(
                move(base),
                productOf(move(exp1), move(exp2))
            ),
            explanation = metadata(Explanation.MultiplyExponentsUsingPowerRule)
        )
    }
}

/**
 * [base ^ exp1 + ... + expN] --> [base ^ exp1] * ... [base ^ expN]
 */
val distributeSumOfPowers = rule {
    val base = AnyPattern()
    val sumOfExponents = sumContaining()
    val pattern = powerOf(base, sumOfExponents)

    onPattern(pattern) {
        TransformationResult(
            toExpr = productOf(
                get(sumOfExponents)!!.children().map {
                    simplifiedPowerOf(move(base), move(it))
                }
            ),
            explanation = metadata(Explanation.DistributeSumOfPowers)
        )
    }
}

/**
 * a * (b + c) -> a * b + a * c
 * (b + c + d) * a -> b * a + c * a + d * a
 */
val distributeMultiplicationOverSum = rule {
    val singleTerm = AnyPattern()
    val sum = sumContaining()
    val product = commutativeProductOf(singleTerm, sum)

    onPattern(product) {
        val terms = get(sum)!!.children()

        TransformationResult(
            toExpr = sumOf(
                terms.map {
                    when (it.expr.operator) {
                        UnaryExpressionOperator.Minus -> negOf(
                            product.substitute(
                                distribute(singleTerm),
                                move(it.nthChild(0))
                            )
                        )

                        else -> product.substitute(distribute(singleTerm), move(it))
                    }
                }
            ),
            explanation = metadata(Explanation.DistributeMultiplicationOverSum)
        )
    }
}

private val MAX_POWER_AS_PRODUCT = 5.toBigInteger()

val rewritePowerAsProduct = rule {
    val base = AnyPattern()
    val exponent = integerCondition(UnsignedIntegerPattern()) { it <= MAX_POWER_AS_PRODUCT && it >= BigInteger.TWO }
    val power = powerOf(base, exponent)

    onPattern(power) {
        TransformationResult(
            toExpr = productOf(List(getValue(exponent).toInt()) { move(base) }),
            explanation = metadata(Explanation.RewritePowerAsProduct, move(base), move(exponent))
        )
    }
}

/**
 * [a ^ 1] -> a, for any `a`
 */
val simplifyExpressionToThePowerOfOne = rule {
    val base = AnyPattern()
    val one = FixedPattern(Constants.One)
    val power = powerOf(base, one)

    onPattern(power) {
        TransformationResult(
            toExpr = move(base),
            explanation = metadata(Explanation.SimplifyExpressionToThePowerOfOne)
        )
    }
}

/**
 * [1 ^ a] -> 1, for any defined `a`
 */
val evaluateOneToAnyPower = rule {
    val one = FixedPattern(Constants.One)
    val exponent = condition(AnyPattern()) { it.isDefinitelyNotUndefined() }
    val power = powerOf(one, exponent)

    onPattern(power) {
        TransformationResult(
            toExpr = move(one),
            explanation = metadata(Explanation.EvaluateOneToAnyPower)
        )
    }
}

/**
 * [0 ^ 0] -> undefined
 */
val evaluateZeroToThePowerOfZero = rule {
    val power = powerOf(FixedPattern(Constants.Zero), FixedPattern(Constants.Zero))
    onPattern(power) {
        TransformationResult(
            toExpr = transformTo(power, Constants.Undefined),
            explanation = metadata(Explanation.EvaluateZeroToThePowerOfZero)
        )
    }
}

/**
 * [a ^ 0] -> 1, for any non-zero `a`
 */
val evaluateExpressionToThePowerOfZero = rule {
    val power = powerOf(condition(AnyPattern()) { it.isDefinitelyNotZero() }, FixedPattern(Constants.Zero))
    onPattern(power) {
        TransformationResult(
            toExpr = transformTo(power, Constants.One),
            explanation = metadata(Explanation.EvaluateExpressionToThePowerOfZero)
        )
    }
}

/**
 * [0 ^ a] -> 0, for any positive a
 */
val evaluateZeroToAPositivePower = rule {
    val power = powerOf(FixedPattern(Constants.Zero), condition(AnyPattern()) { it.signOf() == Sign.POSITIVE })
    onPattern(power) {
        TransformationResult(
            toExpr = transformTo(power, Constants.Zero),
            explanation = metadata(Explanation.EvaluateZeroToAPositivePower)
        )
    }
}

val cancelAdditiveInverseElements = rule {
    val term = AnyPattern()
    val searchTerm = optionalNegOf(term)
    val additiveInverseSearchTerm = oppositeSignPattern(searchTerm, term)
    val pattern = sumContaining(searchTerm, additiveInverseSearchTerm)

    onPattern(pattern) {
        val toExpr = when (get(pattern)!!.children().size) {
            2 -> transformTo(pattern, Constants.Zero)
            else -> cancel(term, restOf(pattern))
        }
        TransformationResult(
            toExpr = toExpr,
            explanation = metadata(Explanation.CancelAdditiveInverseElements, move(term))
        )
    }
}

val rewriteProductOfPowersWithSameBase = rule {
    val base = AnyPattern()
    val exponent1 = AnyPattern()
    val exponent2 = AnyPattern()

    val power1 = powerOf(base, exponent1)
    val power2 = powerOf(base, exponent2)

    val product = productContaining(power1, power2)

    onPattern(product) {
        TransformationResult(
            toExpr = product.substitute(
                powerOf(factor(base), sumOf(move(exponent1), move(exponent2)))
            ),
            explanation = metadata(Explanation.RewriteProductOfPowersWithSameBase)
        )
    }
}

val rewriteProductOfPowersWithSameExponent = rule {
    val base1 = AnyPattern()
    val base2 = AnyPattern()
    val exponent = AnyPattern()

    val power1 = powerOf(base1, exponent)
    val power2 = powerOf(base2, exponent)

    val product = productContaining(power1, power2)

    onPattern(product) {
        TransformationResult(
            toExpr = product.substitute(
                powerOf(productOf(move(base1), move(base2)), factor(exponent))
            ),
            explanation = metadata(Explanation.RewriteProductOfPowersWithSameExponent)
        )
    }
}

val rewriteFractionOfPowersWithSameBase = rule {
    val base = AnyPattern()
    val exponent1 = AnyPattern()
    val exponent2 = AnyPattern()

    val power1 = powerOf(base, exponent1)
    val power2 = powerOf(base, exponent2)

    val product = fractionOf(power1, power2)

    onPattern(product) {
        TransformationResult(
            toExpr = powerOf(factor(base), sumOf(move(exponent1), negOf(move(exponent2)))),
            explanation = metadata(Explanation.RewriteFractionOfPowersWithSameBase)
        )
    }
}

val rewriteFractionOfPowersWithSameExponent = rule {
    val base1 = AnyPattern()
    val base2 = AnyPattern()
    val exponent = AnyPattern()

    val power1 = powerOf(base1, exponent)
    val power2 = powerOf(base2, exponent)

    val product = fractionOf(power1, power2)

    onPattern(product) {
        TransformationResult(
            toExpr = powerOf(fractionOf(move(base1), move(base2)), factor(exponent)),
            explanation = metadata(Explanation.RewriteFractionOfPowersWithSameExponent)
        )
    }
}

val flipFractionUnderNegativePower = rule {
    val fraction = FractionPattern()
    val exponent = AnyPattern()

    val power = powerOf(fraction, negOf(exponent))

    onPattern(power) {
        TransformationResult(
            toExpr = powerOf(
                fractionOf(move(fraction.denominator), move(fraction.numerator)),
                move(exponent)
            ),
            explanation = metadata(Explanation.FlipFractionUnderNegativePower)
        )
    }
}

val rewriteProductOfPowersWithNegatedExponent = rule {
    val base1 = AnyPattern()

    val fraction = FractionPattern()
    val base2 = oneOf(fraction, AnyPattern())

    val exponent = AnyPattern()

    val power1 = powerOf(base1, exponent)
    val power2 = powerOf(base2, negOf(exponent))

    val product1 = productContaining(power1, power2)
    val product2 = productContaining(power2, power1)

    onPattern(oneOf(product1, product2)) {
        val inverse = when {
            isBound(fraction) -> fractionOf(move(fraction.denominator), move(fraction.numerator))
            else -> fractionOf(introduce(Constants.One), move(base2))
        }

        val newProduct = when {
            isBound(product1) -> product1.substitute(move(power1), powerOf(inverse, move(exponent)))
            else -> product2.substitute(powerOf(inverse, move(exponent)), move(power1))
        }

        TransformationResult(
            toExpr = newProduct,
            explanation = metadata(Explanation.RewriteProductOfPowersWithNegatedExponent)
        )
    }
}

val rewriteProductOfPowersWithInverseFractionBase = rule {
    val value1 = AnyPattern()
    val value2 = AnyPattern()

    val fraction1 = fractionOf(value1, value2)
    val fraction2 = fractionOf(value2, value1)

    val exponent1 = AnyPattern()
    val exponent2 = AnyPattern()

    val power1 = powerOf(fraction1, exponent1)
    val power2 = powerOf(fraction2, exponent2)

    val product = productContaining(power1, power2)

    onPattern(product) {
        TransformationResult(
            toExpr = product.substitute(
                move(power1),
                powerOf(fractionOf(move(value1), move(value2)), negOf(move(exponent2)))
            ),
            explanation = metadata(Explanation.RewriteProductOfPowersWithInverseFractionBase)
        )
    }
}

val rewriteProductOfPowersWithInverseBase = rule {
    val base1 = AnyPattern()
    val base2 = fractionOf(FixedPattern(Constants.One), base1)

    val exponent1 = AnyPattern()
    val exponent2 = AnyPattern()

    val power1 = powerOf(base1, exponent1)
    val power2 = powerOf(base2, exponent2)

    val product1 = productContaining(power1, power2)
    val product2 = productContaining(power2, power1)

    onPattern(oneOf(product1, product2)) {
        val newProduct = when {
            isBound(product1) -> product1.substitute(move(power1), powerOf(move(base1), negOf(move(exponent2))))
            else -> product2.substitute(powerOf(move(base1), negOf(move(exponent2))), move(power1))
        }

        TransformationResult(
            toExpr = newProduct,
            explanation = metadata(Explanation.RewriteProductOfPowersWithInverseBase)
        )
    }
}
