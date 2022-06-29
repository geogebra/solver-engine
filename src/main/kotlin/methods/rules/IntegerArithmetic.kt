package methods.rules

import engine.expressionmakers.FixedExpressionMaker
import engine.expressionmakers.cancel
import engine.expressionmakers.makeNumericOp
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.move
import engine.expressionmakers.restOf
import engine.expressionmakers.substituteIn
import engine.expressions.xp
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.custom
import engine.patterns.divideBy
import engine.patterns.negOf
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.rules.Rule
import engine.steps.metadata.Explanation
import engine.steps.metadata.makeMetadata
import java.math.BigInteger

val eliminateOneInProduct = run {
    val one = FixedPattern(xp(1))
    val pattern = productContaining(one)

    Rule(
        pattern = pattern,
        resultMaker = cancel(one, restOf(pattern)),
        explanationMaker = makeMetadata(Explanation.EliminateOneInProduct, move(one))
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

val evaluateIntegerProduct = run {
    val factor1 = UnsignedIntegerPattern()
    val factor2 = UnsignedIntegerPattern()
    val pattern = productContaining(factor1, factor2)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeNumericOp(factor1, factor2) { n1, n2 -> n1 * n2 }),
        explanationMaker = makeMetadata(Explanation.EvaluateIntegerProduct, move(factor1), move(factor2))
    )
}

val evaluateSignedIntegerProduct = run {
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

/*
  re-writes [4^2] as 1 * 4 * 4
 */
val writeIntegerSquareAsMulWithOneAtStart = run {
    val base = SignedIntegerPattern()
    val exponent = FixedPattern(xp(2))

    val power = powerOf(base, exponent)

    Rule(
        pattern = power,
        resultMaker = makeProductOf(FixedExpressionMaker(xp(1)), move(base), move(base)),
        explanationMaker = makeMetadata(Explanation.WriteIntegerSquareAsMulWithOneAtStart, move(base), move(power))
    )
}

/*
  re-writes [4^2] as 4 * 4
 */
val writeIntegerSquareAsMulWithoutOneAtStart = run {
    val base = SignedIntegerPattern()
    val exponent = FixedPattern(xp(2))

    val power = powerOf(base, exponent)

    Rule(
        pattern = power,
        resultMaker = makeProductOf(move(base), move(base)),
        explanationMaker = makeMetadata(Explanation.WriteIntegerSquareAsMulWithoutOneAtStart, move(base), move(power))
    )
}
