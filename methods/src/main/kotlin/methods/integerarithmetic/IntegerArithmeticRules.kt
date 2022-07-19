package methods.integerarithmetic

import engine.expressionmakers.ExpressionMaker
import engine.expressionmakers.OperatorExpressionMaker
import engine.expressionmakers.makeNegOf
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makePowerOf
import engine.expressionmakers.move
import engine.expressionmakers.substituteIn
import engine.expressions.NaryOperator
import engine.methods.Rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.custom
import engine.patterns.divideBy
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.metadata.makeMetadata
import java.math.BigInteger

val evaluateUnsignedIntegerSubtraction = run {
    val term1 = UnsignedIntegerPattern()
    val term2 = UnsignedIntegerPattern()
    val sum = sumContaining(term1, negOf(term2))
    val pattern = ConditionPattern(sum, numericCondition(term1, term2) { n1, n2 -> n1 >= n2 })

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(sum, makeNumericOp(term1, term2) { n1, n2 -> n1 - n2 }),
        explanationMaker = makeMetadata(Explanation.EvaluateIntegerSubtraction, move(term1), move(term2))
    )
}

val evaluateSignedIntegerAddition = run {
    val term1 = SignedIntegerPattern()
    val term2 = SignedIntegerPattern()
    val sum = sumContaining(term1, term2)

    Rule(
        pattern = sum,
        resultMaker = substituteIn(sum, makeNumericOp(term1, term2) { n1, n2 -> n1 + n2 }),
        explanationMaker = makeMetadata(Explanation.EvaluateIntegerAddition, move(term1), move(term2))
    )
}

val evaluateIntegerProductAndDivision = run {
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
        explanationMaker = custom {
            if (isBound(multiplier))
                makeMetadata(Explanation.EvaluateIntegerProduct, move(base), move(multiplier))
            else
                makeMetadata(Explanation.EvaluateIntegerDivision, move(base), move(divisor))
        }
    )
}

val evaluateIntegerPower = run {
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

val rewriteIntegerPowerAsProduct = run {
    val base = SignedIntegerPattern()
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(
        base,
        ConditionPattern(exponent, numericCondition(exponent) { it <= 5.toBigInteger() && it >= BigInteger.TWO })
    )

    Rule(
        pattern = power,
        resultMaker = custom {
            val argsList = mutableListOf<ExpressionMaker>()
            repeat(getValue(exponent).toInt()) {
                argsList += move(base)
            }
            OperatorExpressionMaker(NaryOperator.Product, argsList)
        },
        explanationMaker = makeMetadata(Explanation.RewriteIntegerPowerAsProduct, move(base), move(exponent))
    )
}

val simplifyEvenPowerOfNegative = run {
    val positiveBase = AnyPattern()
    val base = bracketOf(negOf(positiveBase))
    val exponent = SignedIntegerPattern()
    val power =
        powerOf(base, ConditionPattern(exponent, numericCondition(exponent) { it.mod(BigInteger.TWO).signum() == 0 }))

    Rule(
        pattern = power,
        resultMaker = makePowerOf(move(positiveBase), move(exponent)),
        explanationMaker = makeMetadata(Explanation.SimplifyEvenPowerOfNegative),
    )
}

val simplifyOddPowerOfNegative = run {
    val positiveBase = AnyPattern()
    val base = bracketOf(negOf(positiveBase))
    val exponent = SignedIntegerPattern()
    val power =
        powerOf(base, ConditionPattern(exponent, numericCondition(exponent) { it.mod(BigInteger.TWO).signum() != 0 }))

    Rule(
        pattern = power,
        resultMaker = makeNegOf(makePowerOf(move(positiveBase), move(exponent))),
        explanationMaker = makeMetadata(Explanation.SimplifyOddPowerOfNegative),
    )
}
