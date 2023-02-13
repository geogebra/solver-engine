package methods.general

import engine.conditions.Sign
import engine.conditions.isDefinitelyNotUndefined
import engine.conditions.isDefinitelyNotZero
import engine.conditions.signOf
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.explicitProductOf
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
import engine.methods.ruleResult
import engine.operators.UnaryExpressionOperator
import engine.operators.VariableOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.FractionPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedIntegerPattern
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
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.rootOf
import engine.patterns.stickyOptionalNegOf
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.metadata.metadata
import java.math.BigDecimal
import java.math.BigInteger

private val MAX_POWER_AS_PRODUCT = 5.toBigInteger()

enum class GeneralRules(override val runner: Rule) : RunnerMethod {
    EliminateOneInProduct(eliminateOneInProduct),
    EliminateZeroInSum(eliminateZeroInSum),
    EvaluateProductContainingZero(evaluateProductContainingZero),
    EvaluateZeroDividedByAnyValue(evaluateZeroDividedByAnyValue),
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
    ExpandBinomialSquaredUsingIdentity(expandBinomialSquaredUsingIdentity),
    ExpandBinomialCubedUsingIdentity(expandBinomialCubedUsingIdentity),
    ExpandTrinomialSquaredUsingIdentity(expandTrinomialSquaredUsingIdentity),
    ApplyFoilMethod(applyFoilMethod),
    ExpandDoubleBrackets(expandDoubleBrackets),
    ExpandProductOfSumAndDifference(expandProductOfSumAndDifference),
    RewriteDivisionAsFraction(rewriteDivisionAsFraction),
    MultiplyExponentsUsingPowerRule(multiplyExponentsUsingPowerRule),
    DistributeSumOfPowers(distributeSumOfPowers),
    DistributeMultiplicationOverSum(distributeMultiplicationOverSum),
    DistributeNegativeOverBracket(distributeNegativeOverBracket),
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
    RewriteIntegerOrderRootAsPower(rewriteIntegerOrderRootAsPower),
    RewritePowerUnderRoot(rewritePowerUnderRoot),
    CancelRootIndexAndExponent(cancelRootIndexAndExponent),
}

private val eliminateOneInProduct =
    rule {
        val one = FixedPattern(Constants.One)
        val pattern = productContaining(one)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(one, restOf(pattern)),
                explanation = metadata(Explanation.EliminateOneInProduct, move(one)),
            )
        }
    }

private val eliminateZeroInSum =
    rule {
        val zero = FixedPattern(Constants.Zero)
        val pattern = sumContaining(zero)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(zero, restOf(pattern)),
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
            expression.children().all {
                it.operator != UnaryExpressionOperator.DivideBy && it.isDefinitelyNotUndefined()
            }
        }

        onPattern(pattern) {
            ruleResult(
                toExpr = transform(zero),
                explanation = metadata(Explanation.EvaluateProductContainingZero, move(zero)),
            )
        }
    }

/**
 * 0:anyX && anyX != 0 --> 0
 */
private val evaluateZeroDividedByAnyValue =
    rule {
        val zero = FixedPattern(Constants.Zero)
        val divByPattern = numericCondition(SignedNumberPattern()) { it != BigDecimal.ZERO }
        val pattern = productContaining(zero, divideBy(divByPattern))

        onPattern(pattern) {
            ruleResult(
                toExpr = transform(zero),
                explanation = metadata(Explanation.EvaluateZeroDividedByAnyValue, move(zero)),
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
                explanation = metadata(Explanation.EvaluateProductDividedByZeroAsUndefined, move(zero)),
            )
        }
    }

private val simplifyDoubleMinus =
    rule {
        val value = AnyPattern()
        val pattern = negOf(negOf(value))

        onPattern(pattern) {
            ruleResult(
                toExpr = move(value),
                explanation = metadata(Explanation.SimplifyDoubleMinus, move(value)),
            )
        }
    }

private val simplifyProductWithTwoNegativeFactors =
    rule {
        val f1 = AnyPattern()
        val f2 = AnyPattern()
        val fd1 = optionalDivideBy(negOf(f1))
        val fd2 = optionalDivideBy(negOf(f2))
        val product = productContaining(fd1, fd2)

        onPattern(product) {
            ruleResult(
                toExpr = product.substitute(
                    optionalDivideBy(fd1, move(f1)),
                    optionalDivideBy(fd2, move(f2)),
                ),
                explanation = metadata(Explanation.SimplifyProductWithTwoNegativeFactors),
            )
        }
    }

private val moveSignOfNegativeFactorOutOfProduct =
    rule {
        val f = AnyPattern()
        val fd = optionalDivideBy(negOf(f))
        val product = productContaining(fd)

        onPattern(product) {
            ruleResult(
                toExpr = negOf(product.substitute(optionalDivideBy(fd, move(f)))),
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
        val denominator = condition(AnyPattern()) { it.isDefinitelyNotZero() }
        val pattern = fractionOf(zero, denominator)

        onPattern(pattern) {
            ruleResult(
                toExpr = transform(zero),
                explanation = metadata(Explanation.SimplifyZeroNumeratorFractionToZero, move(zero)),
            )
        }
    }

/**
 * [expr / expr] = 0, given expr ≠ 0
 */
private val simplifyUnitFractionToOne =
    rule {
        val common = condition(AnyPattern()) { it.isDefinitelyNotZero() }
        val pattern = fractionOf(common, common)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(common, introduce(Constants.One)),
                explanation = metadata(Explanation.SimplifyUnitFractionToOne),
            )
        }
    }

private val simplifyFractionWithOneDenominator =
    rule {
        val numerator = AnyPattern()
        val denominator = FixedPattern(Constants.One)
        val pattern = fractionOf(numerator, denominator)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(denominator, move(numerator)),
                explanation = metadata(Explanation.SimplifyFractionWithOneDenominator),
            )
        }
    }

private val cancelDenominator =
    rule {
        val common = AnyPattern()
        val numerator = productContaining(common)
        val pattern = fractionOf(numerator, common)

        onPattern(pattern) {
            ruleResult(
                toExpr = cancel(common, restOf(numerator)),
                explanation = metadata(Explanation.CancelDenominator),
            )
        }
    }

private val cancelCommonTerms =
    rule {
        val common = condition(AnyPattern()) { it != Constants.One }
        val numerator = productContaining(common)
        val denominator = productContaining(common)
        val fraction = fractionOf(numerator, denominator)

        onPattern(fraction) {
            ruleResult(
                toExpr = cancel(common, fractionOf(restOf(numerator), restOf(denominator))),
                explanation = metadata(Explanation.CancelCommonTerms),
            )
        }
    }

private val factorMinusFromSum =
    rule {
        val sum = condition(sumContaining()) { expression ->
            expression.children().all { it.operator == UnaryExpressionOperator.Minus }
        }

        onPattern(sum) {
            ruleResult(
                toExpr = negOf(sumOf(get(sum).children().map { move(it.firstChild) })),
                explanation = metadata(Explanation.FactorMinusFromSum),
            )
        }
    }

private val simplifyProductOfConjugates =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val sum1 = commutativeSumOf(a, b)
        val sum2 = commutativeSumOf(a, negOf(b))
        val product = commutativeProductOf(sum1, sum2)

        onPattern(product) {
            ruleResult(
                toExpr = sum2.substitute(
                    powerOf(move(a), introduce(Constants.Two)),
                    negOf(powerOf(move(b), introduce(Constants.Two))),
                ),
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
                toExpr = productOf(get(product).children().map { powerOf(move(it), distributedExponent) }),
                explanation = metadata(Explanation.DistributePowerOfProduct),
            )
        }
    }

/**
 * [(a + b)^2] --> [a^2] + 2ab + [b^2]
 *
 * NOTE: @Simona explicitly mentioned to not use the formula:
 * [(a - b)^2] --> [a^2] - 2ab + [b^2]
 */
private val expandBinomialSquaredUsingIdentity =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val two = FixedPattern(Constants.Two)
        val pattern = powerOf(sumOf(a, b), two)

        onPattern(pattern) {
            val da = distribute(a)
            val db = distribute(b)
            val dtwo = distribute(two)
            ruleResult(
                toExpr = sumOf(
                    powerOf(da, dtwo),
                    productOf(dtwo, da, db),
                    powerOf(db, dtwo),
                ),
                explanation = metadata(Explanation.ExpandBinomialSquaredUsingIdentity),
            )
        }
    }

/**
 * [(a + b)^3] --> [a^3] + 3 [a^2] b + 3 a [b^2] + [b^3]
 *
 * NOTE: @Simona explicitly mentioned to not use the formula:
 * [(a - b)^3] --> [a^3] - 3 [a^2] b + 3 a [b^2] - [b^3]
 */
private val expandBinomialCubedUsingIdentity =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val pattern = powerOf(
            sumOf(a, b),
            FixedPattern(Constants.Three),
        )

        onPattern(pattern) {
            ruleResult(
                toExpr = sumOf(
                    powerOf(move(a), introduce(Constants.Three)),
                    productOf(
                        introduce(Constants.Three),
                        powerOf(move(a), introduce(Constants.Two)),
                        move(b),
                    ),
                    productOf(
                        introduce(Constants.Three),
                        move(a),
                        powerOf(move(b), introduce(Constants.Two)),
                    ),
                    powerOf(move(b), introduce(Constants.Three)),
                ),
                explanation = metadata(Explanation.ExpandBinomialCubedUsingIdentity),
            )
        }
    }

/**
 * [(a + b + c)^2] --> [a^2] + [b^2] + [c^2] + 2ab + 2bc + 2ca
 */
private val expandTrinomialSquaredUsingIdentity =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val c = AnyPattern()

        val sum = sumOf(a, b, c)
        val trinomialSquared = powerOf(sum, FixedPattern(Constants.Two))

        onPattern(trinomialSquared) {
            ruleResult(
                toExpr = sumOf(
                    powerOf(move(a), introduce(Constants.Two)),
                    powerOf(move(b), introduce(Constants.Two)),
                    powerOf(move(c), introduce(Constants.Two)),
                    productOf(introduce(Constants.Two), move(a), move(b)),
                    productOf(introduce(Constants.Two), move(b), move(c)),
                    productOf(introduce(Constants.Two), move(c), move(a)),
                ),
                explanation = metadata(Explanation.ExpandTrinomialSquaredUsingIdentity),
            )
        }
    }

/**
 * (a +- b) * (a -+ b) --> [a^2] - [b^2]
 */
private val expandProductOfSumAndDifference =
    rule {
        val a = AnyPattern()
        val b = condition(AnyPattern()) { it.operator != UnaryExpressionOperator.Minus }
        val pattern = commutativeProductOf(commutativeSumOf(a, b), commutativeSumOf(a, negOf(b)))

        onPattern(pattern) {
            ruleResult(
                toExpr = sumOf(
                    powerOf(move(a), introduce(Constants.Two)),
                    negOf(
                        powerOf(move(b), introduce(Constants.Two)),
                    ),
                ),
                explanation = metadata(Explanation.ExpandProductOfSumAndDifference),
            )
        }
    }

/**
 * (a + b) * (c + d) --> a*c + a*d + b*c + b*d
 */
private val applyFoilMethod =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val c = AnyPattern()
        val d = AnyPattern()
        val sum1 = sumOf(a, b)
        val sum2 = sumOf(c, d)
        val prod = productOf(sum1, sum2)

        onPattern(prod) {
            val da = distribute(a)
            val db = distribute(b)
            val dc = distribute(c)
            val dd = distribute(d)
            val toExpr = sumOf(
                explicitProductOf(da, dc),
                explicitProductOf(da, dd),
                explicitProductOf(db, dc),
                explicitProductOf(db, dd),
            )

            ruleResult(
                toExpr = toExpr,
                explanation = metadata(Explanation.ApplyFoilMethod),
            )
        }
    }

/**
 * (a1 + a2 + ... + aj) * (b1 + b2 + ... + bk) -->
 * a1*b1 + a1*b2 + ... + a1*bk   +   a2*b1 + a2*b2 + ... + a2*bk   + ... +   aj*bk
 */
private val expandDoubleBrackets =
    rule {
        val sum1 = sumContaining()
        val sum2 = sumContaining()
        val prod = productOf(sum1, sum2)

        onPattern(prod) {
            val terms1 = get(sum1).children().map { distribute(it) }
            val terms2 = get(sum2).children().map { distribute(it) }

            val toExpr = sumOf(
                terms1.map { term1 ->
                    sumOf(terms2.map { term2 -> explicitProductOf(term1, term2) })
                },
            )

            ruleResult(
                toExpr = toExpr,
                explanation = metadata(Explanation.ExpandDoubleBrackets),
            )
        }
    }

private val rewriteDivisionAsFraction =
    rule {
        val product = productContaining(divideBy(AnyPattern()))

        onPattern(product) {
            val factors = get(product).children()
            val division = factors.indexOfFirst { it.operator == UnaryExpressionOperator.DivideBy }

            val result = mutableListOf<Expression>()
            result.addAll(factors.subList(0, division - 1))

            val denominator = factors[division].firstChild

            result.add(
                fractionOf(
                    move(factors[division - 1]),
                    move(denominator),
                ),
            )
            result.addAll(factors.subList(division + 1, factors.size))

            ruleResult(
                toExpr = productOf(result),
                explanation = metadata(Explanation.RewriteDivisionAsFraction),
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
        val exp2 = AnyPattern()

        val pattern = powerOf(powerOf(base, exp1), exp2)

        onPattern(pattern) {
            ruleResult(
                toExpr = powerOf(
                    get(base),
                    productOf(move(exp1), move(exp2)),
                ),
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
                    get(sumOfExponents).children().map {
                        simplifiedPowerOf(distributedBase, move(it))
                    },
                ),
                explanation = metadata(Explanation.DistributeSumOfPowers),
            )
        }
    }

/**
 * a * (b + c) -> a * b + a * c
 * (b + c + d) * a -> b * a + c * a + d * a
 */
private val distributeMultiplicationOverSum =
    rule {
        val sum = sumContaining()
        val product = productContaining(sum)
        val optionalNegProduct = stickyOptionalNegOf(product)

        onPattern(optionalNegProduct) {
            val getSum = get(sum)
            val terms = getSum.children()
            val restOfProd = distribute(restOf(product))

            // variableExpression * (c1 + c2 + ... + cn) --> shouldn't be expanded
            if (getSum.isConstant() && !restOfProd.isConstant()) return@onPattern null
            val distributeTerm = if (optionalNegProduct.isNeg()) {
                negOf(restOfProd)
            } else {
                restOfProd
            }

            val distributedExpr = sumOf(
                terms.map { explicitProductOf(distributeTerm, move(it)) },
            )

            ruleResult(
                toExpr = distributedExpr,
                explanation = metadata(Explanation.DistributeMultiplicationOverSum),
            )
        }
    }

/**
 * -(x + y) --> -x - y
 */
private val distributeNegativeOverBracket =
    rule {
        val sumTerm = sumContaining()
        val negSumTerm = negOf(sumTerm)
        val sumContainingNegTerm = sumContaining(negSumTerm)
        val sum = oneOf(sumContainingNegTerm, negSumTerm)

        onPattern(sum) {
            // Note: we can't have distribute path mappings for this as things are because there is no node for "-"
            // itself
            val terms = get(sumTerm).children()
            val negDistributedTerm = sumOf(
                terms.map {
                    if (it.operator == UnaryExpressionOperator.Minus) move(it.firstChild) else negOf(move(it))
                },
            )

            val toExpr = if (isBound(sumContainingNegTerm)) {
                sumContainingNegTerm.substitute(negDistributedTerm)
            } else {
                negDistributedTerm
            }

            ruleResult(
                toExpr = toExpr,
                explanation = metadata(Explanation.DistributeNegativeOverBracket),
            )
        }
    }

private val rewritePowerAsProduct =
    rule {
        val base = AnyPattern()
        val exponent = integerCondition(UnsignedIntegerPattern()) {
            it <= MAX_POWER_AS_PRODUCT &&
                it >= BigInteger.TWO
        }
        val power = powerOf(base, exponent)

        onPattern(power) {
            ruleResult(
                toExpr = productOf(List(getValue(exponent).toInt()) { move(base) }),
                explanation = metadata(Explanation.RewritePowerAsProduct, move(base), move(exponent)),
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
                toExpr = cancel(one, get(base)),
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
        val exponent = condition(AnyPattern()) { it.isDefinitelyNotUndefined() }
        val power = powerOf(one, exponent)

        onPattern(power) {
            ruleResult(
                toExpr = move(one),
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
                explanation = metadata(Explanation.EvaluateZeroToThePowerOfZero),
            )
        }
    }

/**
 * [a ^ 0] -> 1, for any non-zero `a`
 */
private val evaluateExpressionToThePowerOfZero =
    rule {
        val power = powerOf(condition(AnyPattern()) { it.isDefinitelyNotZero() }, FixedPattern(Constants.Zero))
        onPattern(power) {
            ruleResult(
                toExpr = transformTo(power, Constants.One),
                explanation = metadata(Explanation.EvaluateExpressionToThePowerOfZero),
            )
        }
    }

/**
 * [0 ^ a] -> 0, for any positive a
 */
private val evaluateZeroToAPositivePower =
    rule {
        val power = powerOf(FixedPattern(Constants.Zero), condition(AnyPattern()) { it.signOf() == Sign.POSITIVE })
        onPattern(power) {
            ruleResult(
                toExpr = transformTo(power, Constants.Zero),
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
            val toExpr = when (get(pattern).children().size) {
                2 -> transformTo(pattern, Constants.Zero)
                else -> cancel(term, restOf(pattern))
            }
            ruleResult(
                toExpr = toExpr,
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
            if (get(base).operator is VariableOperator ||
                get(power1.exponent) != Constants.One ||
                get(power2.exponent) != Constants.One
            ) {

                ruleResult(
                    toExpr = product.substitute(
                        powerOf(factor(base), sumOf(move(power1.exponent), move(power2.exponent))),
                    ),
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
                explanation = metadata(Explanation.RewriteProductOfPowersWithInverseBase),
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

        val commonExponent = AnyPattern()

        val productExponent = productContaining(commonExponent)
        val exponent = oneOf(commonExponent, productExponent)
        val power = powerOf(base, exponent)

        val productOrder = productContaining(commonExponent)
        val order = oneOf(commonExponent, productOrder)
        val root = rootOf(power, order)

        onPattern(root) {
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
                explanation = metadata(Explanation.CancelRootIndexAndExponent),
            )
        }
    }
