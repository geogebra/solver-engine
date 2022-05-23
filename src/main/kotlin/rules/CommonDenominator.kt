package rules

import expressionmakers.*
import patterns.*
import steps.SkillType
import steps.makeMetadata

val commonDenominator = run {
    val num1 = IntegerPattern()
    val num2 = IntegerPattern()
    val denom1 = IntegerPattern()
    val denom2 = IntegerPattern()
    val f1 = fractionOf(num1, denom1)
    val f2 = fractionOf(num2, denom2)
    val sum = sumContaining(f1, f2)

    val factor1 = makeNumericOp(denom1, denom2) { n1, n2 -> n2 / n1.gcd(n2) }
    val factor2 = makeNumericOp(denom1, denom2) { n1, n2 -> n1 / n1.gcd(n2) }

    Rule(
        pattern = ConditionPattern(sum, numericCondition(denom1, denom2) { n1, n2 -> n1 != n2 }),
        resultMaker = substituteIn(
            sum,
            makeSumOf(
                makeFractionOf(makeProductOf(move(num1), factor1), makeProductOf(move(denom1), factor1)),
                makeFractionOf(makeProductOf(move(num2), factor2), makeProductOf(move(denom2), factor2))
            )
        ),
        explanationMaker = makeMetadata("common denominator", move(f1), move(f2)),
        skillMakers = listOf(makeMetadata(SkillType.NumericLCM, move(denom1), move(denom2))),
    )
}