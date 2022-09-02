package methods.integerarithmetic

import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.divideBy
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.metadata
import engine.utility.isEven
import engine.utility.isOdd
import java.math.BigInteger

val evaluateUnsignedIntegerSubtraction = rule {
    val term1 = UnsignedIntegerPattern()
    val term2 = UnsignedIntegerPattern()
    val sum = sumContaining(term1, negOf(term2))
    val pattern = ConditionPattern(sum, numericCondition(term1, term2) { n1, n2 -> n1 >= n2 })

    onPattern(pattern) {
        TransformationResult(
            toExpr = sum.substitute(numericOp(term1, term2) { n1, n2 -> n1 - n2 }),
            explanation = metadata(Explanation.EvaluateIntegerSubtraction, move(term1), move(term2))
        )
    }
}

val evaluateSignedIntegerAddition = rule {
    val term1 = SignedIntegerPattern()
    val term2 = SignedIntegerPattern()
    val sum = sumContaining(term1, term2)

    onPattern(sum) {
        TransformationResult(
            toExpr = sum.substitute(numericOp(term1, term2) { n1, n2 -> n1 + n2 }),
            explanation = metadata(Explanation.EvaluateIntegerAddition, move(term1), move(term2))
        )
    }
}

val evaluateIntegerProductAndDivision = rule {
    val base = SignedIntegerPattern()
    val multiplier = SignedIntegerPattern()
    val divisor = SignedIntegerPattern()
    val product = productContaining(
        base,
        oneOf(
            multiplier,
            ConditionPattern(
                divideBy(divisor),
                numericCondition(base, divisor) { n1, n2 -> n1 % n2 == BigInteger.ZERO }
            )
        )
    )

    onPattern(product) {
        when {
            isBound(multiplier) -> TransformationResult(
                toExpr = product.substitute(numericOp(base, multiplier) { n1, n2 -> n1 * n2 }),
                explanation = metadata(Explanation.EvaluateIntegerProduct, move(base), move(multiplier))
            )
            else -> TransformationResult(
                toExpr = product.substitute(numericOp(base, divisor) { n1, n2 -> n1 / n2 }),
                explanation = metadata(Explanation.EvaluateIntegerDivision, move(base), move(divisor))
            )
        }
    }
}

private val MAX_POWER = 64.toBigInteger()
private val MAX_POWER_AS_PRODUCT = 5.toBigInteger()

val evaluateIntegerPowerDirectly = rule {
    val base = SignedIntegerPattern()
    val exponent = numericCondition(UnsignedIntegerPattern()) { it <= MAX_POWER }
    val power = powerOf(base, exponent)

    onPattern(power) {
        TransformationResult(
            toExpr = numericOp(base, exponent) { n1, n2 -> n1.pow(n2.toInt()) },
            explanation = metadata(Explanation.EvaluateIntegerPowerDirectly, move(base), move(exponent))
        )
    }
}

val rewriteIntegerPowerAsProduct = rule {
    val base = SignedIntegerPattern()
    val exponent = numericCondition(UnsignedIntegerPattern()) { it <= MAX_POWER_AS_PRODUCT && it >= BigInteger.TWO }
    val power = powerOf(base, exponent)

    onPattern(power) {
        TransformationResult(
            toExpr = productOf(List(getValue(exponent).toInt()) { move(base) }),
            explanation = metadata(Explanation.RewriteIntegerPowerAsProduct, move(base), move(exponent))
        )
    }
}

val simplifyEvenPowerOfNegative = rule {
    val positiveBase = AnyPattern()
    val base = bracketOf(negOf(positiveBase))
    val exponent = numericCondition(SignedIntegerPattern()) { it.isEven() }
    val power = powerOf(base, exponent)

    onPattern(power) {
        TransformationResult(
            toExpr = powerOf(move(positiveBase), move(exponent)),
            explanation = metadata(Explanation.SimplifyEvenPowerOfNegative),
        )
    }
}

val simplifyOddPowerOfNegative = rule {
    val positiveBase = AnyPattern()
    val base = bracketOf(negOf(positiveBase))
    val exponent = numericCondition(SignedIntegerPattern()) { it.isOdd() }
    val power = powerOf(base, exponent)

    onPattern(power) {
        TransformationResult(
            toExpr = negOf(powerOf(move(positiveBase), move(exponent))),
            explanation = metadata(Explanation.SimplifyOddPowerOfNegative),
        )
    }
}
