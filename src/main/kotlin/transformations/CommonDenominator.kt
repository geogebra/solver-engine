package transformations

import expressionmakers.*
import patterns.*

object CommonDenominator : Rule {
    private val num1 = IntegerPattern()
    private val num2 = IntegerPattern()
    private val denom1 = IntegerPattern()
    private val denom2 = IntegerPattern()
    private val f1 = fractionOf(num1, denom1)
    private val f2 = fractionOf(num2, denom2)
    private val sum = sumContaining(f1, f2)

    override val pattern = ConditionPattern(sum, numericCondition(denom1, denom2) { n1, n2 -> n1 != n2 })

    private val factor1 = makeNumericOp(denom1, denom2) { n1, n2 -> n2 / n1.gcd(n2) }
    private val factor2 = makeNumericOp(denom1, denom2) { n1, n2 -> n1 / n1.gcd(n2) }

    override val resultMaker = substituteIn(sum,
        makeSumOf(
            makeFractionOf(makeProductOf(move(num1), factor1), makeProductOf(move(denom1), factor1)),
            makeFractionOf(makeProductOf(move(num2), factor2), makeProductOf(move(denom2), factor2))
        )
    )
}