package methods.general

import engine.conditions.isDefinitelyNotNegative
import engine.conditions.isDefinitelyNotPositive
import engine.conditions.isDefinitelyNotZero
import engine.expressions.Constants
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.Minus
import engine.expressions.PathScope
import engine.expressions.Power
import engine.expressions.Product
import engine.expressions.Root
import engine.expressions.SquareRoot
import engine.expressions.Sum
import engine.expressions.Variable
import engine.expressions.absoluteValueOf
import engine.expressions.asInteger
import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.simplifiedNegOf
import engine.expressions.simplifiedNegOfSum
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
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
import engine.patterns.rootOf
import engine.patterns.stickyOptionalNegOf
import engine.patterns.sumContaining
import engine.sign.Sign
import engine.steps.metadata.metadata
import engine.utility.isEven
import engine.utility.isOdd
import java.math.BigInteger
import kotlin.math.abs
import engine.expressions.PathScope as Scope
import engine.steps.metadata.DragTargetPosition as Position
import engine.steps.metadata.GmPathModifier as PM

private val maxPowerAsProduct = 5.toBigInteger()

enum class GeneralRules(override val runner: Rule) : RunnerMethod {

    // Tidy up rules
    RemoveUnitaryCoefficient(removeUnitaryCoefficient),
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
    FactorMinusFromSum(factorMinusFromSum),
    FactorMinusFromSumWithAllNegativeTerms(factorMinusFromSumWithAllNegativeTerms),
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

    // Powers
    SimplifyEvenPowerOfNegative(simplifyEvenPowerOfNegative),
    SimplifyOddPowerOfNegative(simplifyOddPowerOfNegative),
    RewriteEvenPowerOfBaseAsEvenPowerOfAbsoluteValueOfBase(rewriteEvenPowerOfBaseAsEvenPowerOfAbsoluteValueOfBase),

    // Products of powers
    RewriteProductOfPowersWithSameBase(rewriteProductOfPowersWithSameBase),
    RewriteProductOfPowersWithSameExponent(rewriteProductOfPowersWithSameExponent),
    RewriteProductOfPowersWithNegatedExponent(rewriteProductOfPowersWithNegatedExponent),
    RewriteProductOfPowersWithInverseFractionBase(rewriteProductOfPowersWithInverseFractionBase),
    RewriteProductOfPowersWithInverseBase(rewriteProductOfPowersWithInverseBase),

    // Fractions of powers
    RewriteFractionOfPowersWithSameBase(rewriteFractionOfPowersWithSameBase),
    RewriteFractionOfPowersWithSameExponent(rewriteFractionOfPowersWithSameExponent),
    FlipFractionUnderNegativePower(flipFractionUnderNegativePower),

    RewriteOddRootOfNegative(rewriteOddRootOfNegative),
    RewriteIntegerOrderRootAsPower(rewriteIntegerOrderRootAsPower),
    RewritePowerUnderRoot(rewritePowerUnderRoot),
    CancelRootIndexAndExponent(cancelRootIndexAndExponent),

    // Absolute values
    ResolveAbsoluteValueOfZero(resolveAbsoluteValueOfZero),
    ResolveAbsoluteValueOfNonNegativeValue(resolveAbsoluteValueOfNonNegativeValue),
    ResolveAbsoluteValueOfNonPositiveValue(resolveAbsoluteValueOfNonPositiveValue),
    SimplifyAbsoluteValueOfNegatedExpression(simplifyAbsoluteValueOfNegatedExpression),
    SimplifyEvenPowerOfAbsoluteValue(simplifyEvenPowerOfAbsoluteValue),

    FactorizeInteger(factorizeInteger),
    SimplifyPlusMinusOfAbsoluteValue(simplifyPlusMinusOfAbsoluteValue),
}

private val removeUnitaryCoefficient = rule {
    val one = FixedPattern(Constants.One)
    val pattern = productContaining(one)

    onPattern(pattern) {
        // We need to check the result doesn't start by divide-by
        val productWithoutOne = restOf(pattern)
        if (productWithoutOne is DivideBy ||
            (productWithoutOne is Product && productWithoutOne.firstChild is DivideBy)
        ) {
            null
        } else {
            ruleResult(
                toExpr = cancel(
                    mapOf(one to listOf(Scope.Expression, Scope.OuterOperator)),
                    productWithoutOne,
                ),
                gmAction = tap(one),
                explanation = metadata(Explanation.RemoveUnitaryCoefficient, move(one)),
            )
        }
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
 * Simplify any product containing zero to zero.
 * Only do the simplification if we are sure all the terms are defined, to avoid simplifying
 * 0 * [1 / 1 - 1] to 0 (it should be undefined).
 * If the product is at the initial position in a sum then remove also the leading minus,
 * i.e. turn -3*0 + 4 into 0 + 4 instead of -0 + 4.
 */
private val evaluateProductContainingZero =
    rule {
        val zero = FixedPattern(Constants.Zero)
        val p = productContaining(zero)
        val pattern = condition(p) { expression ->
            expression.children.all {
                it !is DivideBy && it.isDefinitelyNotUndefined()
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
        val productInNeg = productContaining(fd2)
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
                        productInNeg.substitute(optionalDivideBy(fd2, get(f2))),
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
                    drag(f2Negative, PM.Operator, negProduct, PM.Operator, Position.Onto)
                } else {
                    drag(f2Negative, PM.Operator, f1Negative, PM.Operator, Position.Onto)
                },
                explanation = metadata(Explanation.SimplifyProductWithTwoNegativeFactors),
            )
        }
    }

/**
 * 2 * (-x) -> - 2x
 * x * (-2) -> rule shouldn't apply (we rearrange first)
 * sqrt[2] * (-2) * x -> rule shouldn't apply (we rearrange first)
 * negative sign has a "move" path mapping, rest have a "shift"
 */
private val moveSignOfNegativeFactorOutOfProduct =
    rule {
        val f = AnyPattern()
        val negf = negOf(f)
        val product = productContaining(negf)

        onPattern(product) {
            ruleResult(
                toExpr = moveUnaryOperator(
                    negf, // -x
                    negOf(product.substitute(get(f))), // -2x
                ),
                gmAction = drag(negf, PM.Operator, product.childPatterns[0], PM.Parens, Position.LeftOf),
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
        val denominator = condition {
            it.isDefinitelyNotZero() ||
                !it.isConstant() // if it is not constant we have already made sure the fraction is defined
        }
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
        val common = condition {
            it.isDefinitelyNotZero() ||
                !it.isConstant() // if it is not constant we have already made sure the fraction is defined
        }
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
                gmAction = drag(common.within(denominator), PM.Group, common.within(numerator), PM.Group),
                explanation = metadata(Explanation.CancelDenominator),
            )
        }
    }

/** -2-3-4 --> -(2+3+4) */
private val factorMinusFromSumWithAllNegativeTerms =
    rule {
        val sum = condition(sumContaining()) { expression ->
            expression.children.all { it is Minus }
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
                explanation = metadata(Explanation.FactorMinusFromSumWithAllNegativeTerms),
            )
        }
    }

/**
 * A comparator for [Expression] objects that orders them based on a non-negative "pseudo degree."
 * The "pseudo degree" extends the concept of the polynomial degree to non-polynomial expressions,
 * following certain rules.
 *
 * Comparator priority:
 * - Non-constant expressions take precedence over constant expressions.
 * - Expressions are subsequently ordered by their non-negative pseudo degree.
 * - Ties are resolved by re-evaluating expressions' pseudo degrees, considering constants to have a non-zero degree.
 */
val pseudoDegreeComparator = compareBy<Expression>(
    { !it.isConstant() },
    { it.pseudoDegree() },
    { it.pseudoDegree(constantIsZeroDegree = false) },
)

/**
 * Calculates the non-negative pseudo degree of an [Expression] instance.
 *
 * The pseudo degree is determined by the type of the expression and its components:
 * - For constants, when [constantIsZeroDegree] is true, the degree is 0.0.
 * - For [Minus] expressions, it is the pseudo degree of the argument.
 * - [SquareRoot] expressions are fixed at 0.5.
 * - [Root] expressions are the reciprocal of the radicand's absolute double value.
 * - Variables have a degree of 1.0.
 * - [Power] expressions combine the base's pseudo degree with the exponent's absolute double value.
 * - [Product] expressions sum the pseudo degrees of their children.
 * - [Sum] expressions use the maximum pseudo degree among their children.
 * - Other expression types default to 0.0.
 *
 * @param constantIsZeroDegree Boolean flag indicating whether to consider the degree of constants as zero(default true)
 * @return The non-negative pseudo degree as a [Double].
 */
@Suppress("MagicNumber")
private fun Expression.pseudoDegree(constantIsZeroDegree: Boolean = true): Double = when {
    this.isConstant() && constantIsZeroDegree -> 0.0
    this is Minus -> this.argument.pseudoDegree(constantIsZeroDegree)
    this is SquareRoot -> 0.5
    this is Root -> 1.0 / abs(this.radicand.doubleValue)
    this is Variable -> 1.0
    this is Power -> abs(this.base.pseudoDegree(constantIsZeroDegree)) * abs(this.exponent.doubleValue)
    this is Product -> this.children.sumOf { it.pseudoDegree(constantIsZeroDegree) }
    this is Sum -> this.children.maxOf { it.pseudoDegree(constantIsZeroDegree) }
    else -> 0.0
}

private val factorMinusFromSum =
    rule {
        val sum = condition(sumContaining()) { expression ->
            expression.children.maxWith(pseudoDegreeComparator) is Minus
        }

        onPattern(sum) {
            val toExpr = negOf(simplifiedNegOfSum(get(sum)))
            ruleResult(
                toExpr = transform(sum, toExpr),
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
                gmAction = drag(exponent, PM.Group, product),
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
                gmAction = drag(exp2, PM.Group, innerPower),
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
                gmAction = drag(additiveInverseSearchTerm, PM.Group, searchTerm, PM.Group),
                explanation = metadata(Explanation.CancelAdditiveInverseElements, move(term)),
            )
        }
    }

private val simplifyEvenPowerOfNegative = rule {
    val positiveBase = AnyPattern()
    val base = negOf(positiveBase)
    val exponent = integerCondition(SignedIntegerPattern()) { it.isEven() }
    val power = powerOf(base, exponent)

    onPattern(power) {
        ruleResult(
            toExpr = cancel(
                mapOf(
                    base to listOf(PathScope.Operator),
                    exponent to listOf(PathScope.Expression),
                ),
                powerOf(get(positiveBase), get(exponent)),
            ),
            gmAction = tapOp(base),
            explanation = metadata(Explanation.SimplifyEvenPowerOfNegative),
        )
    }
}

private val simplifyOddPowerOfNegative = rule {
    val positiveBase = AnyPattern()
    val base = negOf(positiveBase)
    val exponent = integerCondition(SignedIntegerPattern()) { it.isOdd() }
    val power = powerOf(base, exponent)

    onPattern(power) {
        ruleResult(
            // transform is a better path mapping that using "moveUnaryOperator" on "neg"
            // and "move" on "exponent", since that would be two separate "move"'s (instead of single one)
            toExpr = transform(power, negOf(powerOf(get(positiveBase), get(exponent)))),
            gmAction = drag(exponent, positiveBase),
            explanation = metadata(Explanation.SimplifyOddPowerOfNegative),
        )
    }
}

/**
 * `[y^k]` --> `[abs[y] ^ k]`
 */
private val rewriteEvenPowerOfBaseAsEvenPowerOfAbsoluteValueOfBase = rule {
    val base = condition { it.signOf() == Sign.UNKNOWN }
    val exponent = integerCondition(SignedIntegerPattern()) { it.isEven() }
    val power = powerOf(base, exponent)

    onPattern(power) {
        val fa = substitute(base, "a")
        val fExponent = substitute(exponent, "2k")
        ruleResult(
            toExpr = transform(power, powerOf(absoluteValueOf(get(base)), get(exponent))),
            formula = equationOf(
                powerOf(fa, fExponent),
                powerOf(absoluteValueOf(fa), fExponent),
            ),
            explanation = metadata(Explanation.RewriteEvenPowerOfBaseAsEvenPowerOfAbsoluteValueOfBase),
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
            val exp1Value = get(power1.exponent)
            val exp2Value = get(power2.exponent)

            val addExponents = (!get(base).isConstant()) ||
                (
                    (exp1Value != Constants.One || exp2Value.doubleValue !in 0.0..1.0) &&
                        (exp2Value != Constants.One || exp1Value.doubleValue !in 0.0..1.0)
                    )

            if (addExponents) {
                ruleResult(
                    toExpr = product.substitute(
                        powerOf(factor(base), sumOf(move(power1.exponent), move(power2.exponent))),
                    ),
                    gmAction = drag(power2, PM.Group, power1, PM.Group),
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
                gmAction = drag(power2, PM.Group, power1, PM.Group),
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
                gmAction = when (get(root)) {
                    is Root -> {
                        drag(root, PM.RootIndex, root.radicand)
                    }
                    is SquareRoot -> {
                        drag(root, PM.RootIndex, root.radicand)
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
                get(base).signOf().signum != 1
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

private val resolveAbsoluteValueOfZero = rule {
    val argument = FixedPattern(Constants.Zero)
    val absoluteValue = absoluteValueOf(argument)

    onPattern(absoluteValue) {
        ruleResult(
            toExpr = transformTo(absoluteValue, get(argument)),
            gmAction = tap(argument),
            explanation = metadata(Explanation.ResolveAbsoluteValueOfZero),
        )
    }
}

/**
 * abs(x) --> x, for any x where x is known to be not-negative
 */
private val resolveAbsoluteValueOfNonNegativeValue = rule {
    val argument = condition { it.isDefinitelyNotNegative() }
    val absoluteValue = absoluteValueOf(argument)
    onPattern(absoluteValue) {
        ruleResult(
            toExpr = transformTo(absoluteValue, get(argument)),
            gmAction = tap(absoluteValue, PM.OpenParens),
            explanation = metadata(Explanation.ResolveAbsoluteValueOfNonNegativeValue),
        )
    }
}

/**
 * abs(x) --> x, for any x where x is known to be not-negative
 */
private val resolveAbsoluteValueOfNonPositiveValue = rule {
    val argument = condition { it.isDefinitelyNotPositive() }
    val absoluteValue = absoluteValueOf(argument)
    onPattern(absoluteValue) {
        val sumTerms = when (val argumentValue = get(argument)) {
            is Sum -> argumentValue.children
            else -> listOf(argumentValue)
        }
        val positiveArgumentValue = sumOf(sumTerms.map { simplifiedNegOf(it) })

        ruleResult(
            toExpr = transformTo(absoluteValue, positiveArgumentValue),
            gmAction = tap(absoluteValue, PM.OpenParens),
            explanation = metadata(Explanation.ResolveAbsoluteValueOfNonPositiveValue),
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
            gmAction = tap(negExpr, PM.Operator),
            explanation = metadata(Explanation.SimplifyAbsoluteValueOfNegatedExpression),
        )
    }
}

private val simplifyEvenPowerOfAbsoluteValue = rule {
    val expr = AnyPattern()
    val absoluteValue = absoluteValueOf(expr)
    val exponent = integerCondition(SignedIntegerPattern()) { it.isEven() }
    val power = powerOf(absoluteValue, exponent)

    onPattern(power) {
        ruleResult(
            toExpr = transform(power, powerOf(get(expr), get(exponent))),
            explanation = metadata(Explanation.SimplifyEvenPowerOfAbsoluteValue),
        )
    }
}

private val factorizeInteger = rule {
    val integer = UnsignedIntegerPattern()
    onPattern(integer) {
        val primeFactorization = productOf(productOfPrimeFactors(integer))
        ruleResult(
            toExpr = transform(integer, primeFactorization),
            gmAction = edit(integer),
            explanation = metadata(Explanation.FactorizeInteger),
        )
    }
}

private val simplifyPlusMinusOfAbsoluteValue = rule {
    val expr = AnyPattern()
    val absoluteValue = absoluteValueOf(expr)
    val plusMinusTerm = plusMinusOf(absoluteValue)

    onPattern(plusMinusTerm) {
        ruleResult(
            toExpr = transform(plusMinusTerm, engine.expressions.plusMinusOf(get(expr))),
            explanation = metadata(Explanation.SimplifyPlusMinusOfAbsoluteValue),
        )
    }
}
