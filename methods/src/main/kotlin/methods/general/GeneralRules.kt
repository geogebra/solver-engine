package methods.general

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
import engine.patterns.optionalDivideBy
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

val eliminateLoneOneInExponent = rule {
    val one = FixedPattern(xp(1))
    val base = AnyPattern()
    val pattern = powerOf(base, one)

    onPattern(pattern) {
        TransformationResult(

            toExpr = move(base),
            explanation = metadata(Explanation.EliminateLoneOneInExponent)
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
    val zero = FixedPattern(Constants.Zero)
    val pattern = productContaining(divideBy(zero))

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
    val denominator = numericCondition(SignedNumberPattern()) { it != BigDecimal.ZERO }
    val pattern = fractionOf(zero, denominator)

    onPattern(pattern) {
        TransformationResult(
            toExpr = transform(zero),
            explanation = metadata(Explanation.SimplifyZeroNumeratorFractionToZero)
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

val simplifyExpressionToThePowerOfOne = rule {
    val base = AnyPattern()
    val power = powerOf(base, FixedPattern(Constants.One))

    onPattern(power) {
        TransformationResult(
            toExpr = move(base),
            explanation = metadata(Explanation.SimplifyExpressionToThePowerOfOne)
        )
    }
}

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
 * This rule should only be used when it has been asserted that the base is not 0.
 */
val evaluateExpressionToThePowerOfZero = rule {
    val power = powerOf(AnyPattern(), FixedPattern(Constants.Zero))
    onPattern(power) {
        TransformationResult(
            toExpr = transformTo(power, Constants.One),
            explanation = metadata(Explanation.EvaluateExpressionToThePowerOfZero)
        )
    }
}

/**
 * This rule should only be used when it has been asserted that the exponent is > 0.
 */
val evaluateZeroToAPositivePower = rule {
    val power = powerOf(FixedPattern(Constants.Zero), AnyPattern())
    onPattern(power) {
        TransformationResult(
            toExpr = transformTo(power, Constants.Zero),
            explanation = metadata(Explanation.EvaluateZeroToAPositivePower)
        )
    }
}
