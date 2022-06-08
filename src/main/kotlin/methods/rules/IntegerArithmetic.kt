package methods.rules

import engine.expressionmakers.*
import engine.expressions.xp
import engine.patterns.*
import engine.rules.Rule
import engine.steps.metadata.Explanation
import engine.steps.metadata.makeMetadata
import java.math.BigInteger

val eliminateZeroInSum = run {
    val zero = FixedPattern(xp(0))
    val pattern = sumContaining(zero)

    Rule(
        pattern = pattern,
        resultMaker = cancel(zero, restOf(pattern)),
        explanationMaker = makeMetadata(Explanation.EliminateZeroInSum, move(zero)),
    )
}

val eliminateOneInProduct = run {
    val one = FixedPattern(xp(1))
    val pattern = productContaining(one)

    Rule(
        pattern = pattern,
        resultMaker = cancel(one, restOf(pattern)),
        explanationMaker = makeMetadata(Explanation.EliminateOneInProduct, move(one)),
    )
}

val zeroInProduct = run {
    val zero = FixedPattern(xp(0))
    val pattern = productContaining(zero)

    Rule(
        pattern = pattern,
        resultMaker = transform(zero),
        explanationMaker = makeMetadata(Explanation.ProductContainingZero, move(zero))
    )
}

val evaluateUnsignedIntegerSubtraction = run {
    val term1 = UnsignedIntegerPattern()
    val term2 = UnsignedIntegerPattern()
    val sum = sumContaining(term1, negOf(term2))
    val pattern = ConditionPattern(sum, numericCondition(term1, term2) { n1, n2 -> n1 >= n2 })

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(sum, makeNumericOp(term1, term2) { n1, n2 -> n1 - n2 }),
        explanationMaker = makeMetadata(Explanation.EvaluateIntegerSubtraction, move(term1), move(term2)),
    )
}

val evaluateSignedIntegerAddition = run {
    val term1 = SignedIntegerPattern()
    val term2 = SignedIntegerPattern()
    val sum = sumContaining(term1, term2)

    Rule(
        pattern = sum,
        resultMaker = substituteIn(sum, makeNumericOp(term1, term2) { n1, n2 -> n1 + n2 }),
        explanationMaker = makeMetadata(Explanation.EvaluateIntegerAddition, move(term1), move(term2)),
    )
}

val evaluateIntegerProduct = run {
    val factor1 = UnsignedIntegerPattern()
    val factor2 = UnsignedIntegerPattern()
    val pattern = productContaining(factor1, factor2)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeNumericOp(factor1, factor2) { n1, n2 -> n1 * n2 }),
        explanationMaker = makeMetadata(Explanation.EvaluateIntegerProduct, move(factor1), move(factor2)),
    )
}

val evaluateSignedIntegerProduct = run {
    val base = SignedIntegerPattern()
    val multiplier = SignedIntegerPattern()
    val divisor = SignedIntegerPattern()
    val product = productContaining(
        base, oneOf(
            multiplier,
            ConditionPattern(
                divideBy(divisor),
                numericCondition(base, divisor) { n1, n2 -> n1 % n2 == BigInteger.ZERO }
            )
        )
    )

    Rule(
        pattern = product,
        resultMaker = substituteIn(
            product,
            custom {
                if (isBound(multiplier))
                    makeNumericOp(base, multiplier) { n1, n2 -> n1 * n2 }
                else
                    makeNumericOp(base, divisor) { n1, n2 -> n1 / n2 }
            }
        ),
        explanationMaker = makeMetadata(
            Explanation.EvaluateIntegerProduct, move(base), custom {
                if (isBound(multiplier)) move(multiplier) else move(divisor)
            }
        )
    )
}

val evaluateSignedIntegerPower = run {
    val base = SignedIntegerPattern()
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(
        base,
        ConditionPattern(exponent, numericCondition(exponent) { it < Int.MAX_VALUE.toBigInteger() })
    )

    Rule(
        pattern = power,
        resultMaker = makeNumericOp(base, exponent) { n1, n2 -> n1.pow(n2.toInt()) },
        explanationMaker = makeMetadata(Explanation.EvaluateIntegerPower, move(base), move(power))
    )
}

val simplifyDoubleNeg = run {
    val value = AnyPattern()
    val pattern = negOf(bracketOf(negOf(value)))

    Rule(
        pattern = pattern,
        resultMaker = move(value),
        explanationMaker = makeMetadata(Explanation.SimplifyDoubleMinus, move(value))
    )
}
