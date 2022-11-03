package methods.integerarithmetic

import engine.expressions.negOf
import engine.expressions.powerOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.TransformationResult
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
import engine.patterns.sumContaining
import engine.steps.metadata.metadata
import engine.utility.isEven
import engine.utility.isOdd
import java.math.BigInteger

private val MAX_POWER = 64.toBigInteger()

enum class IntegerArithmeticRules(override val runner: Rule) : RunnerMethod {
    EvaluateSignedIntegerAddition(
        rule {
            val term1 = SignedIntegerPattern()
            val term2 = SignedIntegerPattern()
            val sum = sumContaining(term1, term2)

            onPattern(sum) {
                val explanation = when {
                    getValue(term1) > BigInteger.ZERO && getValue(term2) < BigInteger.ZERO ->
                        metadata(Explanation.EvaluateIntegerSubtraction, move(term1), move(term2.unsignedPattern))

                    else ->
                        metadata(Explanation.EvaluateIntegerAddition, move(term1), move(term2))
                }

                TransformationResult(
                    toExpr = sum.substitute(integerOp(term1, term2) { n1, n2 -> n1 + n2 }),
                    explanation = explanation
                )
            }
        }
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
                        integerCondition(base, divisor) { n1, n2 -> n2.signum() != 0 && (n1 % n2).signum() == 0 }
                    )
                )
            )

            onPattern(product) {
                when {
                    isBound(multiplier) -> TransformationResult(
                        toExpr = product.substitute(integerOp(base, multiplier) { n1, n2 -> n1 * n2 }),
                        explanation = metadata(Explanation.EvaluateIntegerProduct, move(base), move(multiplier))
                    )

                    else -> TransformationResult(
                        toExpr = product.substitute(integerOp(base, divisor) { n1, n2 -> n1 / n2 }),
                        explanation = metadata(Explanation.EvaluateIntegerDivision, move(base), move(divisor))
                    )
                }
            }
        }
    ),

    EvaluateIntegerPowerDirectly(
        rule {
            val base = SignedIntegerPattern()
            val exponent = integerCondition(UnsignedIntegerPattern()) { it <= MAX_POWER }
            val power = powerOf(base, exponent)

            onPattern(power) {
                TransformationResult(
                    toExpr = integerOp(base, exponent) { n1, n2 -> n1.pow(n2.toInt()) },
                    explanation = metadata(Explanation.EvaluateIntegerPowerDirectly, move(base), move(exponent))
                )
            }
        }
    ),

    SimplifyEvenPowerOfNegative(
        rule {
            val positiveBase = AnyPattern()
            val base = negOf(positiveBase)
            val exponent = integerCondition(SignedIntegerPattern()) { it.isEven() }
            val power = powerOf(base, exponent)

            onPattern(power) {
                TransformationResult(
                    toExpr = powerOf(move(positiveBase), move(exponent)),
                    explanation = metadata(Explanation.SimplifyEvenPowerOfNegative),
                )
            }
        }
    ),

    SimplifyOddPowerOfNegative(
        rule {
            val positiveBase = AnyPattern()
            val base = negOf(positiveBase)
            val exponent = integerCondition(SignedIntegerPattern()) { it.isOdd() }
            val power = powerOf(base, exponent)

            onPattern(power) {
                TransformationResult(
                    toExpr = negOf(powerOf(move(positiveBase), move(exponent))),
                    explanation = metadata(Explanation.SimplifyOddPowerOfNegative),
                )
            }
        }
    )
}
