package methods.general

import engine.conditions.isDefinitelyNotUndefined
import engine.conditions.isDefinitelyNotZero
import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.IntegerExpression
import engine.expressions.PathScope
import engine.expressions.Variable
import engine.expressions.absoluteValueOf
import engine.expressions.asInteger
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.BinaryExpressionOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.FractionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.absoluteValueOf
import engine.patterns.commutativeProductContaining
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.divideBy
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.integerOrderRootOf
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.oppositeSignPattern
import engine.patterns.optionalDivideBy
import engine.patterns.optionalNegOf
import engine.patterns.optionalPowerOf
import engine.patterns.plusMinusOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.rootOf
import engine.patterns.stickyOptionalNegOf
import engine.patterns.sumContaining
import engine.sign.Sign
import engine.steps.metadata.metadata
import engine.utility.isEven
import engine.utility.isOdd
import java.math.BigInteger
import engine.expressions.PathScope as Scope
import engine.steps.metadata.DragTargetPosition as Position
import engine.steps.metadata.GmPathModifier as PM

private val maxPowerAsProduct = 5.toBigInteger()

enum class GeneralRules(override val runner: Rule) : RunnerMethod {
    RemoveUnitaryCoefficient(removeUnitaryCoefficient),
    EliminateOneInProduct(eliminateOneInProduct),
    EliminateZeroInSum(eliminateZeroInSum),
    EvaluateProductContainingZero(evaluateProductContainingZero),
    EvaluateProductDividedByZeroAsUndefined(evaluateProductDividedByZeroAsUndefined),
    SimplifyDoubleMinus(simplifyDoubleMinus),
    SimplifyProductWithTwoNegativeFactors(simplifyProductWithTwoNegativeFactors),
    MoveSignOfNegativeFactorOutOfProduct(moveSignOfNegativeFactorOutOfProduct),
    SimplifyZeroDenominatorFractionToUndefined(simplifyZeroDenominatorFractionToUndefined),
    SimplifyZeroNumeratorFractionToZero(simplifyZeroNumeratorFractionToZero),
    SimplifyUnitFractionToOne(simplifyUnitFractionToOne),
    SimplifyFractionWithOneDenominator(simplifyFractionWithOneDenominator),
    CancelDenominator(cancelDenominator),
    CancelCommonTerms(cancelCommonTerms),
    FactorMinusFromSum(factorMinusFromSum),
    SimplifyProductOfConjugates(simplifyProductOfConjugates),
    DistributePowerOfProduct(distributePowerOfProduct),
    MultiplyExponentsUsingPowerRule(multiplyExponentsUsingPowerRule),
    DistributeSumOfPowers(distributeSumOfPowers),
    RewritePowerAsProduct(rewritePowerAsProduct),
    SimplifyExpressionToThePowerOfOne(simplifyExpressionToThePowerOfOne),
    EvaluateOneToAnyPower(evaluateOneToAnyPower),
    EvaluateZeroToThePowerOfZero(evaluateZeroToThePowerOfZero),
    EvaluateExpressionToThePowerOfZero(evaluateExpressionToThePowerOfZero),
    EvaluateZeroToAPositivePower(evaluateZeroToAPositivePower),
    CancelAdditiveInverseElements(cancelAdditiveInverseElements),
    RewriteProductOfPowersWithSameBase(rewriteProductOfPowersWithSameBase),
    RewriteProductOfPowersWithSameExponent(rewriteProductOfPowersWithSameExponent),
    RewriteFractionOfPowersWithSameBase(rewriteFractionOfPowersWithSameBase),
    RewriteFractionOfPowersWithSameExponent(rewriteFractionOfPowersWithSameExponent),
    FlipFractionUnderNegativePower(flipFractionUnderNegativePower),
    RewriteProductOfPowersWithNegatedExponent(rewriteProductOfPowersWithNegatedExponent),
    RewriteProductOfPowersWithInverseFractionBase(rewriteProductOfPowersWithInverseFractionBase),
    RewriteProductOfPowersWithInverseBase(rewriteProductOfPowersWithInverseBase),
    RewriteOddRootOfNegative(rewriteOddRootOfNegative),
    RewriteIntegerOrderRootAsPower(rewriteIntegerOrderRootAsPower),
    RewritePowerUnderRoot(rewritePowerUnderRoot),
    CancelRootIndexAndExponent(cancelRootIndexAndExponent),
    ResolveAbsoluteValueOfPositiveValue(resolveAbsoluteValueOfPositiveValue),
    ResolveAbsoluteValueOfNegativeValue(resolveAbsoluteValueOfNegativeValue),
    ResolveAbsoluteValueOfZero(resolveAbsoluteValueOfZero),
    SimplifyAbsoluteValueOfNegatedExpression(simplifyAbsoluteValueOfNegatedExpression),
}

private val removeUnitaryCoefficient =
    rule {
        val one = FixedPattern(Constants.One)
        val otherTerm = condition { it !is IntegerExpression }
        val pattern = productOf(one, otherTerm)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(one, get(otherTerm)),
                gmAction = tap(one),
                explanation = metadata(Explanation.RemoveUnitaryCoefficient),
            )
        }
    }

private val eliminateOneInProduct =
    rule {
        val one = FixedPattern(Constants.One)
        val pattern = productContaining(one)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(
                    mapOf(one to listOf(Scope.Expression, Scope.OuterOperator)),
                    restOf(pattern),
                ),
                gmAction = tap(one),
                explanation = metadata(Explanation.EliminateOneInProduct, move(one)),
            )
        }
    }

private val eliminateZeroInSum =
    rule {
        val zero = FixedPattern(Constants.Zero)
        val plusMinusZero = oneOf(zero, negOf(zero), plusMinusOf(zero))
        val pattern = sumContaining(plusMinusZero)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(
                    mapOf(zero to listOf(Scope.Expression, Scope.OuterOperator)),
                    restOf(pattern),
                ),
                gmAction = tap(zero),
                explanation = metadata(Explanation.EliminateZeroInSum, move(zero)),
            )
        }
    }

/**
 * 0 * anyX --> 0
 */
private val evaluateProductContainingZero =
    rule {
        val zero = FixedPattern(Constants.Zero)
        val p = productContaining(zero)
        val pattern = condition(p) { expression ->
            expression.children.all {
                it.operator != UnaryExpressionOperator.DivideBy && it.isDefinitelyNotUndefined()
            }
        }

        val optionalNegProduct = stickyOptionalNegOf(pattern, initialPositionOnly = true)

        onPattern(optionalNegProduct) {
            ruleResult(
                toExpr = move(zero),
                gmAction = tap(zero),
                explanation = metadata(Explanation.EvaluateProductContainingZero, move(zero)),
            )
        }
    }

/**
 * anyX : 0 --> undefined
 */
private val evaluateProductDividedByZeroAsUndefined =
    rule {
        val zero = SignedNumberPattern()
        val pattern = productContaining(divideBy(numericCondition(zero) { it.signum() == 0 }))

        onPattern(pattern) {
            ruleResult(
                toExpr = transformTo(pattern, Constants.Undefined),
                gmAction = noGmSupport(),
                explanation = metadata(Explanation.EvaluateProductDividedByZeroAsUndefined, move(zero)),
            )
        }
    }

private val simplifyDoubleMinus =
    rule {
        val value = AnyPattern()
        val innerNeg = negOf(value)
        val pattern = negOf(innerNeg)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(
                    mapOf(
                        pattern to listOf(Scope.Operator),
                        innerNeg to listOf(Scope.Operator),
                    ),
                    get(value),
                ),
                gmAction = drag(innerNeg, PM.Operator, pattern, PM.Operator, Position.Onto),
                explanation = metadata(Explanation.SimplifyDoubleMinus, move(innerNeg)),
            )
        }
    }

private val simplifyProductWithTwoNegativeFactors =
    rule {
        val f1 = AnyPattern()
        val f2 = AnyPattern()
        val f1Negative = negOf(f1)
        val f2Negative = negOf(f2)
        val fd1 = optionalDivideBy(f1Negative)
        val fd2 = optionalDivideBy(f2Negative)
        val product = productContaining(fd1, fd2)

        // Alternative pattern, to cover the situation where the negative is in front of
        // the product

        val fd1Positive = optionalDivideBy(f1)
        val productInNeg = productContaining(fd1Positive, fd2)
        val negProduct = negOf(productInNeg)

        onPattern(oneOf(product, negProduct)) {
            val negIsOnProduct = isBound(negProduct)
            ruleResult(
                toExpr = if (negIsOnProduct) {
                    cancel(
                        mapOf(
                            negProduct to listOf(Scope.Operator),
                            f2Negative to listOf(Scope.Operator),
                        ),
                        productInNeg.substitute(
                            optionalDivideBy(fd1Positive, get(fd1Positive)),
                            optionalDivideBy(fd2, get(f2)),
                        ),
                    )
                } else {
                    cancel(
                        mapOf(
                            f1Negative to listOf(Scope.Operator),
                            f2Negative to listOf(Scope.Operator),
                        ),
                        product.substitute(
                            optionalDivideBy(fd1, get(f1)),
                            optionalDivideBy(fd2, get(f2)),
                        ),
                    )
                },
                gmAction = if (negIsOnProduct) {
                    drag(f2, PM.Operator, negProduct, PM.Operator, Position.Onto)
                } else {
                    drag(f2, PM.Operator, f1, PM.Operator, Position.Onto)
                },
                explanation = metadata(Explanation.SimplifyProductWithTwoNegativeFactors),
            )
        }
    }

/**
 * 2*(-x) -> - 2x
 * negative sign has a "move" path mapping, rest have a "shift"
 */
private val moveSignOfNegativeFactorOutOfProduct =
    rule {
        val f = AnyPattern()
        val negf = negOf(f)
        val fd = optionalDivideBy(negf)
        val product = productContaining(fd)

        onPattern(product) {
            if (context.gmFriendly) return@onPattern null
            ruleResult(
                toExpr = moveUnaryOperator(
                    negf, // -x
                    negOf(product.substitute(optionalDivideBy(fd, get(f)))), // -2x
                ),
                gmAction = drag(negf, PM.Operator, product, null, Position.LeftOf),
                explanation = metadata(Explanation.MoveSignOfNegativeFactorOutOfProduct),
            )
        }
    }

/**
 * anyX / 0 --> undefined
 */
private val simplifyZeroDenominatorFractionToUndefined =
    rule {
        val zero = FixedPattern(Constants.Zero)
        val numerator = AnyPattern()
        val pattern = fractionOf(numerator, zero)

        onPattern(pattern) {
            ruleResult(
                toExpr = transformTo(pattern, Constants.Undefined),
                gmAction = noGmSupport(),
                explanation = metadata(Explanation.SimplifyZeroDenominatorFractionToUndefined, move(pattern)),
            )
        }
    }

/**
 * 0 / anyX = 0 && anyX != 0 --> 0
 */
private val simplifyZeroNumeratorFractionToZero =
    rule {
        val zero = FixedPattern(Constants.Zero)
        val denominator = condition { it.isDefinitelyNotZero() }
        val pattern = fractionOf(zero, denominator)

        onPattern(pattern) {
            ruleResult(
                toExpr = transformTo(pattern, Constants.Zero),
                gmAction = tap(zero),
                explanation = metadata(Explanation.SimplifyZeroNumeratorFractionToZero, move(zero)),
            )
        }
    }

/**
 * [expr / expr] = 1, given expr ≠ 0
 */
private val simplifyUnitFractionToOne =
    rule {
        val common = condition { it.isDefinitelyNotZero() }
        val pattern = fractionOf(common, common)

        onPattern(pattern) {
            ruleResult(
                toExpr = transform(pattern, introduce(Constants.One)),
                gmAction = tap(pattern, PM.FractionBar),
                explanation = metadata(Explanation.SimplifyUnitFractionToOne),
            )
        }
    }

/** any / 1 --> any */
private val simplifyFractionWithOneDenominator =
    rule {
        val numerator = AnyPattern()
        val denominator = FixedPattern(Constants.One)
        val pattern = fractionOf(numerator, denominator)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(denominator, get(numerator)),
                gmAction = tap(denominator),
                explanation = metadata(Explanation.SimplifyFractionWithOneDenominator),
            )
        }
    }

/** [any1 * any2 / any1] --> any2 */
private val cancelDenominator =
    rule {
        val common = AnyPattern()
        val numerator = productContaining(common)
        val pattern = fractionOf(numerator, common)

        onPattern(pattern) {
            val denominator = pattern.childPatterns[1]
            ruleResult(
                toExpr = cancel(common, restOf(numerator)),
                gmAction = drag(common.within(denominator), common.within(numerator)),
                explanation = metadata(Explanation.CancelDenominator),
            )
        }
    }

/** [any1 * any2 / any1 * any3] --> [any2 / any3] */
private val cancelCommonTerms =
    rule {
        val common = condition { it != Constants.One }
        val numerator = productContaining(common)
        val denominator = productContaining(common)
        val fraction = fractionOf(numerator, denominator)

        onPattern(fraction) {
            ruleResult(
                toExpr = cancel(common, fractionOf(restOf(numerator), restOf(denominator))),
                gmAction = drag(common.within(denominator), common.within(numerator)),
                explanation = metadata(Explanation.CancelCommonTerms),
            )
        }
    }

/** -2-3-4 --> -(2+3+4) */
private val factorMinusFromSum =
    rule {
        val sum = condition(sumContaining()) { expression ->
            expression.children.all { it.operator == UnaryExpressionOperator.Minus }
        }

        onPattern(sum) {
            val firstAddend = get(sum).children[0]
            val toExpr = negOf(sumOf(get(sum).children.map { it.firstChild }))
            ruleResult(
                toExpr = factorOp(get(sum).children, toExpr),
                // NOTE: this will only work if there are brackets around the sum
                gmAction = drag(
                    firstAddend,
                    PM.Operator,
                    sum,
                    PM.Operator,
                    Position.LeftOf,
                ),
                explanation = metadata(Explanation.FactorMinusFromSum),
            )
        }
    }

/** [1 / (1+sqrt2)(1-sqrt2)] --> [1 / (1)^2 - (sqrt2)^2]*/
private val simplifyProductOfConjugates =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val sum1 = commutativeSumOf(a, b)
        val sum2 = commutativeSumOf(a, negOf(b))
        val product = commutativeProductOf(sum1, sum2)

        onPattern(product) {
            ruleResult(
                toExpr = transform(
                    product,
                    sum2.substitute(
                        powerOf(get(a), Constants.Two),
                        negOf(powerOf(get(b), Constants.Two)),
                    ),
                ),
                gmAction = applyFormula(product, "Difference of Squares"),
                explanation = metadata(Explanation.SimplifyProductOfConjugates),
            )
        }
    }

/**
 * [(x1 * ... * xn) ^ a] --> [x1 ^ a] * ... * [xn ^ a]
 */
private val distributePowerOfProduct =
    rule {
        val exponent = AnyPattern()
        val product = productContaining()
        val pattern = powerOf(product, exponent)

        onPattern(pattern) {
            val distributedExponent = distribute(exponent)

            ruleResult(
                toExpr = productOf(
                    get(product).children.map { powerOf(move(it), distributedExponent) },
                ),
                gmAction = drag(exponent, product),
                explanation = metadata(Explanation.DistributePowerOfProduct),
            )
        }
    }

/**
 * [([a^b]) ^ c] --> [a^b*c]
 */
private val multiplyExponentsUsingPowerRule =
    rule {
        val base = AnyPattern()
        val exp1 = AnyPattern()
        val innerPower = powerOf(base, exp1)
        val exp2 = AnyPattern()

        val pattern = powerOf(innerPower, exp2)

        onPattern(pattern) {
            ruleResult(
                toExpr = powerOf(
                    get(base),
                    productOf(move(exp1), move(exp2)),
                ),
                gmAction = drag(exp2, innerPower),
                explanation = metadata(Explanation.MultiplyExponentsUsingPowerRule),
            )
        }
    }

/**
 * [base ^ exp1 + ... + expN] --> [base ^ exp1] * ... [base ^ expN]
 */
private val distributeSumOfPowers =
    rule {
        val base = AnyPattern()
        val sumOfExponents = sumContaining()
        val pattern = powerOf(base, sumOfExponents)

        onPattern(pattern) {
            val distributedBase = distribute(base)

            ruleResult(
                toExpr = productOf(
                    get(sumOfExponents).children.map {
                        simplifiedPowerOf(distributedBase, move(it))
                    },
                ),
                gmAction = drag(get(sumOfExponents).children.last(), pattern, Position.RightOf),
                explanation = metadata(Explanation.DistributeSumOfPowers),
            )
        }
    }

private val rewritePowerAsProduct =
    rule {
        val base = AnyPattern()
        val exponent = integerCondition(UnsignedIntegerPattern()) {
            it <= maxPowerAsProduct &&
                it >= BigInteger.TWO
        }
        val power = powerOf(base, exponent)

        onPattern(power) {
            // We want to prefer direct evaluation over 2^3 ==> 2*2*2
            if (context.gmFriendly) return@onPattern null
            ruleResult(
                toExpr = productOf(List(getValue(exponent).toInt()) { distribute(base) }),
                gmAction = edit(power),
                explanation = metadata(Explanation.RewritePowerAsProduct, get(power)),
            )
        }
    }

/**
 * [a ^ 1] -> a, for any `a`
 */
private val simplifyExpressionToThePowerOfOne =
    rule {
        val base = AnyPattern()
        val one = FixedPattern(Constants.One)
        val power = powerOf(base, one)

        onPattern(power) {
            ruleResult(
                toExpr = cancel(
                    mapOf(one to listOf(PathScope.Expression)),
                    get(base),
                ),
                gmAction = tap(one),
                explanation = metadata(Explanation.SimplifyExpressionToThePowerOfOne),
            )
        }
    }

/**
 * [1 ^ a] -> 1, for any defined `a`
 */
private val evaluateOneToAnyPower =
    rule {
        val one = FixedPattern(Constants.One)
        val exponent = condition { it.isDefinitelyNotUndefined() }
        val power = powerOf(one, exponent)

        onPattern(power) {
            ruleResult(
                toExpr = move(one),
                gmAction = tap(one),
                explanation = metadata(Explanation.EvaluateOneToAnyPower),
            )
        }
    }

/**
 * [0 ^ 0] -> undefined
 */
private val evaluateZeroToThePowerOfZero =
    rule {
        val power = powerOf(FixedPattern(Constants.Zero), FixedPattern(Constants.Zero))
        onPattern(power) {
            ruleResult(
                toExpr = transformTo(power, Constants.Undefined),
                gmAction = noGmSupport(),
                explanation = metadata(Explanation.EvaluateZeroToThePowerOfZero),
            )
        }
    }

/**
 * [a ^ 0] -> 1, for any non-zero `a`
 */
private val evaluateExpressionToThePowerOfZero =
    rule {
        val zero = FixedPattern(Constants.Zero)
        val power = powerOf(condition { it.isDefinitelyNotZero() }, zero)
        onPattern(power) {
            ruleResult(
                toExpr = transformTo(power, Constants.One),
                gmAction = tap(zero),
                explanation = metadata(Explanation.EvaluateExpressionToThePowerOfZero),
            )
        }
    }

/**
 * [0 ^ a] -> 0, for any positive a
 */
private val evaluateZeroToAPositivePower =
    rule {
        val zero = FixedPattern(Constants.Zero)
        val power = powerOf(zero, condition { it.signOf() == Sign.POSITIVE })
        onPattern(power) {
            ruleResult(
                toExpr = move(zero),
                gmAction = tap(zero),
                explanation = metadata(Explanation.EvaluateZeroToAPositivePower),
            )
        }
    }

private val cancelAdditiveInverseElements =
    rule {
        val term = AnyPattern()
        val searchTerm = optionalNegOf(term)
        val additiveInverseSearchTerm = oppositeSignPattern(searchTerm, term)
        val pattern = sumContaining(searchTerm, additiveInverseSearchTerm)

        onPattern(pattern) {
            val toExpr = when (get(pattern).children.size) {
                2 -> transformTo(pattern, Constants.Zero)
                else -> cancel(
                    mapOf(term to listOf(Scope.Expression, Scope.OuterOperator)),
                    restOf(pattern),
                )
            }
            ruleResult(
                toExpr = toExpr,
                gmAction = drag(additiveInverseSearchTerm, searchTerm),
                explanation = metadata(Explanation.CancelAdditiveInverseElements, move(term)),
            )
        }
    }

private val rewriteProductOfPowersWithSameBase =
    rule {
        val base = AnyPattern()

        val power1 = optionalPowerOf(base)
        val power2 = optionalPowerOf(base)

        val product = productContaining(power1, power2)

        onPattern(product) {
            if (get(base) is Variable ||
                get(power1.exponent) != Constants.One ||
                get(power2.exponent) != Constants.One
            ) {

                ruleResult(
                    toExpr = product.substitute(
                        powerOf(factor(base), sumOf(move(power1.exponent), move(power2.exponent))),
                    ),
                    gmAction = drag(power2, power1),
                    explanation = metadata(Explanation.RewriteProductOfPowersWithSameBase),
                )
            } else {
                null
            }
        }
    }

private val rewriteProductOfPowersWithSameExponent =
    rule {
        val base1 = AnyPattern()
        val base2 = AnyPattern()
        val exponent = AnyPattern()

        val power1 = powerOf(base1, exponent)
        val power2 = powerOf(base2, exponent)

        val product = productContaining(power1, power2)

        onPattern(product) {
            ruleResult(
                toExpr = product.substitute(
                    powerOf(productOf(move(base1), move(base2)), factor(exponent)),
                ),
                gmAction = dragCollect(
                    listOf(exponent.within(power1), exponent.within(power2)),
                    product,
                    null,
                    Position.OutsideOf,
                ),
                explanation = metadata(Explanation.RewriteProductOfPowersWithSameExponent),
            )
        }
    }

private val rewriteFractionOfPowersWithSameBase =
    rule {
        val base = AnyPattern()

        val power1 = optionalPowerOf(base)
        val power2 = optionalPowerOf(base)

        val product = fractionOf(power1, power2)

        onPattern(product) {
            ruleResult(
                toExpr = powerOf(
                    factor(base),
                    sumOf(
                        move(power1.exponent),
                        negOf(move(power2.exponent)),
                    ),
                ),
                gmAction = drag(power2, power1),
                explanation = metadata(Explanation.RewriteFractionOfPowersWithSameBase),
            )
        }
    }

private val rewriteFractionOfPowersWithSameExponent =
    rule {
        val base1 = AnyPattern()
        val base2 = AnyPattern()
        val exponent = AnyPattern()

        val power1 = powerOf(base1, exponent)
        val power2 = powerOf(base2, exponent)

        val product = fractionOf(power1, power2)

        onPattern(product) {
            ruleResult(
                toExpr = powerOf(fractionOf(move(base1), move(base2)), factor(exponent)),
                gmAction = dragCollect(
                    listOf(exponent.within(power1), exponent.within(power2)),
                    product,
                    null,
                    Position.OutsideOf,
                ),
                explanation = metadata(Explanation.RewriteFractionOfPowersWithSameExponent),
            )
        }
    }

private val flipFractionUnderNegativePower =
    rule {
        val fraction = FractionPattern()
        val exponent = AnyPattern()

        val power = powerOf(fraction, negOf(exponent))

        onPattern(power) {
            ruleResult(
                toExpr = powerOf(
                    fractionOf(move(fraction.denominator), move(fraction.numerator)),
                    move(exponent),
                ),
                gmAction = edit(power),
                explanation = metadata(Explanation.FlipFractionUnderNegativePower),
            )
        }
    }

private val rewriteProductOfPowersWithNegatedExponent =
    rule {
        val base1 = AnyPattern()

        val fraction = FractionPattern()
        val base2 = oneOf(fraction, AnyPattern())

        val exponent = AnyPattern()

        val power1 = powerOf(base1, exponent)
        val power2 = powerOf(base2, negOf(exponent))

        val product = commutativeProductContaining(power1, power2)

        onPattern(product) {
            val inverse = when {
                isBound(fraction) -> fractionOf(move(fraction.denominator), move(fraction.numerator))
                else -> fractionOf(introduce(Constants.One), move(base2))
            }

            ruleResult(
                toExpr = product.substitute(get(power1), powerOf(inverse, move(exponent))),
                gmAction = edit(power2),
                explanation = metadata(Explanation.RewriteProductOfPowersWithNegatedExponent),
            )
        }
    }

private val rewriteProductOfPowersWithInverseFractionBase =
    rule {
        val value1 = AnyPattern()
        val value2 = AnyPattern()

        val fraction1 = fractionOf(value1, value2)
        val fraction2 = fractionOf(value2, value1)

        val power1 = optionalPowerOf(fraction1)
        val power2 = optionalPowerOf(fraction2)

        val product = productContaining(power1, power2)

        onPattern(product) {
            ruleResult(
                toExpr = product.substitute(
                    get(power1),
                    powerOf(
                        fractionOf(move(value1), move(value2)),
                        negOf(move(power2.exponent)),
                    ),
                ),
                gmAction = edit(power2),
                explanation = metadata(Explanation.RewriteProductOfPowersWithInverseFractionBase),
            )
        }
    }

private val rewriteProductOfPowersWithInverseBase =
    rule {
        val base1 = AnyPattern()
        val base2 = fractionOf(FixedPattern(Constants.One), base1)

        val exponent1 = AnyPattern()
        val exponent2 = AnyPattern()

        val power1 = powerOf(base1, exponent1)
        val power2 = powerOf(base2, exponent2)

        val product = commutativeProductContaining(power1, power2)

        onPattern(product) {
            ruleResult(
                toExpr = product.substitute(get(power1), powerOf(move(base1), negOf(move(exponent2)))),
                gmAction = edit(power2),
                explanation = metadata(Explanation.RewriteProductOfPowersWithInverseBase),
            )
        }
    }

private val rewriteOddRootOfNegative = rule {
    val radicand = AnyPattern()
    val order = integerCondition(UnsignedIntegerPattern()) { it.isOdd() }
    val pattern = rootOf(negOf(radicand), order)

    onPattern(pattern) {
        ruleResult(
            toExpr = transformTo(
                pattern,
                negOf(rootOf(get(radicand), get(order))),
            ),
            explanation = metadata(Explanation.RewriteOddRootOfNegative),
        )
    }
}

private val rewriteIntegerOrderRootAsPower =
    rule {
        val root = integerOrderRootOf(AnyPattern())

        onPattern(root) {
            ruleResult(
                toExpr = powerOf(
                    move(root.radicand),
                    fractionOf(introduce(Constants.One), move(root.order)),
                ),
                gmAction = when (get(root).operator) {
                    BinaryExpressionOperator.Root -> {
                        drag(root.order, root, Position.RightOf)
                    }
                    UnaryExpressionOperator.SquareRoot -> {
                        drag(root, PM.RootIndex, root, null, Position.RightOf)
                    }
                    else -> noGmSupport()
                },
                explanation = metadata(Explanation.RewriteIntegerOrderRootAsPower),
            )
        }
    }

/**
 * Rewrites the exponent of a power under a root and the index
 * of a root as a product to cancel out the gcd between root
 * index and exponent
 * E.g. root[[7 ^ 6], 8] --> root[[7 ^ 3 * 2], 4 * 2]
 */
private val rewritePowerUnderRoot =
    rule {
        val base = AnyPattern()
        val exponent = UnsignedIntegerPattern()
        val pow = powerOf(base, exponent)
        val root = integerOrderRootOf(pow)

        onPattern(
            ConditionPattern(
                root,
                integerCondition(root.order, exponent) { p, q -> p != q && p.gcd(q) != BigInteger.ONE },
            ),
        ) {
            val gcdExpRootOrder = integerOp(root.order, exponent) { p, q -> p.gcd(q) }
            val newExp = integerOp(root.order, exponent) { p, q -> q.divide(p.gcd(q)) }
            val newRootOrder = integerOp(root.order, exponent) { p, q -> p.divide(p.gcd(q)) }

            ruleResult(
                toExpr = rootOf(
                    simplifiedPowerOf(get(base), simplifiedProductOf(newExp, gcdExpRootOrder)),
                    simplifiedProductOf(newRootOrder, gcdExpRootOrder),
                ),
                gmAction = edit(root),
                explanation = metadata(Explanation.RewritePowerUnderRoot),
            )
        }
    }

/**
 * root[[7 ^ 3 * 2], 5 * 2] -> root[[7 ^ 3], 5]
 */
private val cancelRootIndexAndExponent =
    rule {
        val base = AnyPattern()

        val commonExponent = SignedIntegerPattern()

        val productExponent = productContaining(commonExponent)
        val exponent = oneOf(commonExponent, productExponent)
        val power = powerOf(base, exponent)

        val productOrder = productContaining(commonExponent)
        val order = oneOf(commonExponent, productOrder)
        val root = rootOf(power, order)

        onPattern(root) {
            if (get(commonExponent).asInteger()!!.isEven() &&
                get(base).signOf() != Sign.POSITIVE
            ) {
                return@onPattern null
            }

            val newPower = when {
                isBound(productExponent) -> powerOf(get(base), restOf(productExponent))
                else -> get(base)
            }

            val newRoot = when {
                isBound(productOrder) -> rootOf(newPower, restOf(productOrder))
                else -> newPower
            }

            ruleResult(
                toExpr = cancel(commonExponent, newRoot),
                gmAction = edit(root),
                explanation = metadata(Explanation.CancelRootIndexAndExponent),
            )
        }
    }

private val resolveAbsoluteValueOfPositiveValue = rule {
    val argument = condition { it.isConstant() && it.signOf() == Sign.POSITIVE }
    val absoluteValue = absoluteValueOf(argument)

    onPattern(absoluteValue) {
        ruleResult(
            toExpr = transformTo(absoluteValue, get(argument)),
            explanation = metadata(Explanation.ResolveAbsoluteValueOfPositiveValue),
        )
    }
}

private val resolveAbsoluteValueOfNegativeValue = rule {
    val argument = condition { it.isConstant() && it.signOf() == Sign.POSITIVE }
    val negatedArgument = negOf(argument)
    val absoluteValue = absoluteValueOf(negatedArgument)

    onPattern(absoluteValue) {
        ruleResult(
            toExpr = transformTo(absoluteValue, get(argument)),
            explanation = metadata(Explanation.ResolveAbsoluteValueOfNegativeValue),
        )
    }
}

private val resolveAbsoluteValueOfZero = rule {
    val argument = FixedPattern(Constants.Zero)
    val absoluteValue = absoluteValueOf(argument)

    onPattern(absoluteValue) {
        ruleResult(
            toExpr = transformTo(absoluteValue, get(argument)),
            explanation = metadata(Explanation.ResolveAbsoluteValueOfZero),
        )
    }
}

/**
 * abs(-x) --> abs(x) for any x
 */
private val simplifyAbsoluteValueOfNegatedExpression = rule {
    val expr = AnyPattern()
    val negExpr = negOf(expr)
    val absoluteValue = absoluteValueOf(negExpr)

    onPattern(absoluteValue) {
        ruleResult(
            toExpr = cancel(
                mapOf(negExpr to listOf(PathScope.Operator)),
                absoluteValueOf(get(expr)),
            ),
            explanation = metadata(Explanation.SimplifyAbsoluteValueOfNegatedExpression),
        )
    }
}
