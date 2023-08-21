package methods.integerarithmetic

import engine.expressions.Constants
import engine.expressions.PathScope
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.divideBy
import engine.patterns.integerCondition
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.stickyOptionalNegOf
import engine.patterns.sumContaining
import engine.steps.metadata.metadata
import engine.utility.isEven
import engine.utility.isOdd

private val MAX_POWER = 64.toBigInteger()

enum class IntegerArithmeticRules(override val runner: Rule) : RunnerMethod {
    EvaluateSignedIntegerAddition(
        rule {
            val term1 = SignedIntegerPattern()
            val term2 = SignedIntegerPattern()
            val sum = sumContaining(term1, term2)

            onPattern(sum) {
                val explanation = when {
                    !term1.isNeg() && term2.isNeg() ->
                        metadata(Explanation.EvaluateIntegerSubtraction, move(term1), move(term2.unsignedPattern))
                    else ->
                        metadata(Explanation.EvaluateIntegerAddition, move(term1), move(term2))
                }

                ruleResult(
                    toExpr = sum.substitute(integerOp(term1, term2) { n1, n2 -> n1 + n2 }),
                    gmAction = drag(term2, term1),
                    explanation = explanation,
                )
            }
        },
    ),

    EvaluateIntegerProductAndDivision(
        rule {
            val base = SignedIntegerPattern()
            val multiplier = SignedIntegerPattern()
            val divisor = SignedIntegerPattern()
            val product = productContaining(
                base,
                oneOf(
                    multiplier,
                    ConditionPattern(
                        divideBy(divisor),
                        integerCondition(base, divisor) { n1, n2 -> n2.signum() != 0 && (n1 % n2).signum() == 0 },
                    ),
                ),
            )

            val optionalNegProduct = stickyOptionalNegOf(product, initialPositionOnly = true)

            onPattern(optionalNegProduct) {
                val res = if (isBound(multiplier)) {
                    integerOp(base, multiplier) { n1, n2 -> n1 * n2 }
                } else {
                    integerOp(base, divisor) { n1, n2 -> n1 / n2 }
                }

                // if the result is 0, then no need for the sign
                val toExpr = if (res == Constants.Zero) {
                    product.substitute(res)
                } else {
                    copySign(optionalNegProduct, product.substitute(res))
                }

                val gmAction = if (isBound(multiplier)) {
                    drag(multiplier, base)
                } else {
                    noGmSupport()
                }

                val explanation = if (isBound(multiplier)) {
                    metadata(Explanation.EvaluateIntegerProduct, move(base), move(multiplier))
                } else {
                    metadata(Explanation.EvaluateIntegerDivision, move(base), move(divisor))
                }

                ruleResult(
                    toExpr = toExpr,
                    gmAction = gmAction,
                    explanation = explanation,
                )
            }
        },
    ),

    EvaluateIntegerPowerDirectly(
        rule {
            val base = SignedIntegerPattern()
            val exponent = integerCondition(UnsignedIntegerPattern()) { it <= MAX_POWER }
            val power = powerOf(base, exponent)

            onPattern(power) {
                ruleResult(
                    toExpr = integerOp(base, exponent) { n1, n2 -> n1.pow(n2.toInt()) },
                    gmAction = tap(exponent),
                    explanation = metadata(Explanation.EvaluateIntegerPowerDirectly, move(base), move(exponent)),
                )
            }
        },
    ),

    SimplifyEvenPowerOfNegative(
        rule {
            val positiveBase = AnyPattern()
            val base = negOf(positiveBase)
            val exponent = integerCondition(SignedIntegerPattern()) { it.isEven() }
            val power = powerOf(base, exponent)

            onPattern(power) {
                ruleResult(
                    toExpr = cancel(
                        mapOf(
                            base to listOf(engine.expressions.PathScope.Operator),
                            exponent to listOf(PathScope.Expression),
                        ),
                        powerOf(get(positiveBase), get(exponent)),
                    ),
                    gmAction = tapOp(base),
                    explanation = metadata(Explanation.SimplifyEvenPowerOfNegative),
                )
            }
        },
    ),

    SimplifyOddPowerOfNegative(
        rule {
            val positiveBase = AnyPattern()
            val base = negOf(positiveBase)
            val exponent = integerCondition(SignedIntegerPattern()) { it.isOdd() }
            val power = powerOf(base, exponent)

            onPattern(power) {
                ruleResult(
                    toExpr = transform(power, negOf(powerOf(move(positiveBase), move(exponent)))),
                    gmAction = drag(exponent, positiveBase),
                    explanation = metadata(Explanation.SimplifyOddPowerOfNegative),
                )
            }
        },
    ),
}
