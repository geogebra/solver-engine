package methods.approximation

import engine.expressions.xp
import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedDecimalPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.divideBy
import engine.patterns.integerCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.steps.metadata.metadata

val roundTerminatingDecimal = rule {
    val decimal = UnsignedDecimalPattern()

    onPattern(decimal) {
        val value = getValue(decimal)
        when {
            value.scale() <= context.effectivePrecision -> null
            else -> TransformationResult(
                toExpr = numericOp(decimal) { round(it) },
                explanation = metadata(
                    Explanation.RoundTerminatingDecimal,
                    move(decimal),
                    introduce(xp(context.effectivePrecision))
                )
            )
        }
    }
}

val expandRecurringDecimal = rule {
    val recurringDecimal = RecurringDecimalPattern()

    onPattern(recurringDecimal) {
        val value = getValue(recurringDecimal)
        when {
            // we need at least one more decimal digit than what the precision requires
            value.decimalDigits > context.effectivePrecision -> null
            else -> TransformationResult(
                toExpr = transformTo(recurringDecimal, xp(value.expand(context.effectivePrecision + 1))),
                explanation = metadata(
                    Explanation.ExpandRecurringDecimal,
                    move(recurringDecimal),
                    introduce(xp(context.effectivePrecision))
                )
            )
        }
    }
}

val roundRecurringDecimal = rule {
    val recurringDecimal = RecurringDecimalPattern()

    onPattern(recurringDecimal) {
        val value = getValue(recurringDecimal)
        when {
            // we need at least one more decimal digit than what the precision requires
            value.decimalDigits <= context.effectivePrecision -> null
            else -> TransformationResult(
                toExpr = transformTo(
                    recurringDecimal,
                    xp(round(value.nonRepeatingValue))
                ),
                explanation = metadata(
                    Explanation.RoundRecurringDecimal,
                    move(recurringDecimal),
                    introduce(xp(context.effectivePrecision))
                )
            )
        }
    }
}

val approximateDecimalProductAndDivision = rule {
    val base = SignedNumberPattern()
    val multiplier = SignedNumberPattern()
    val divisor = SignedNumberPattern()
    val product = productContaining(
        base,
        oneOf(
            multiplier,
            divideBy(divisor),
        )
    )

    onPattern(product) {
        when {
            isBound(multiplier) -> TransformationResult(
                toExpr = product.substitute(
                    numericOp(base, multiplier) { n1, n2 -> round(n1 * n2) }
                ),
                explanation = metadata(
                    Explanation.ApproximateDecimalProduct,
                    move(base), move(multiplier), introduce(xp(context.effectivePrecision))
                )
            )

            else -> TransformationResult(
                toExpr = product.substitute(
                    numericOp(base, divisor) { n1, n2 -> round(n1 / n2) }
                ),
                explanation = metadata(
                    Explanation.ApproximateDecimalDivision,
                    move(base), move(divisor), introduce(xp(context.effectivePrecision))
                )
            )
        }
    }
}

private val MAX_POWER = 64.toBigInteger()

val approximateDecimalPower = rule {
    val base = SignedNumberPattern()
    val exponent = integerCondition(UnsignedIntegerPattern()) { it <= MAX_POWER }
    val power = powerOf(base, exponent)

    onPattern(power) {
        TransformationResult(
            toExpr = numericOp(base, exponent) { n1, n2 -> round(n1.pow(n2.toInt())) },
            explanation = metadata(
                Explanation.ApproximateDecimalPower,
                move(base), move(exponent), introduce(xp(context.effectivePrecision))
            )
        )
    }
}
