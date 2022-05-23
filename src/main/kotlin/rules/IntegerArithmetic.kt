package rules

import expressionmakers.*
import expressions.IntegerExpr
import patterns.*
import steps.makeMetadata

val eliminateZeroInSum = run {
    val zero = FixedPattern(IntegerExpr(0))
    val pattern = sumContaining(zero)

    Rule(
        pattern = pattern,
        resultMaker = cancel(zero, restOf(pattern)),
        explanationMaker = makeMetadata("eliminate 0 in sum", move(zero)),
    )
}

val eliminateOneInProduct = run {
    val one = FixedPattern(IntegerExpr(1))
    val pattern = productContaining(one)

    Rule(
        pattern = pattern,
        resultMaker = cancel(one, restOf(pattern)),
        explanationMaker = makeMetadata("eliminate 1 in product", move(one)),
    )
}

val evaluateIntegerSum = run {
    val term1 = IntegerPattern()
    val term2 = IntegerPattern()
    val pattern = sumContaining(term1, term2)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeNumericOp(term1, term2) { n1, n2 -> n1 + n2 }),
        explanationMaker = makeMetadata("evaluate integer sum", move(term1), move(term2)),
    )
}

val evaluateIntegerSubtraction = run {
    val term1 = IntegerPattern()
    val term2 = IntegerPattern()
    val sum = sumContaining(term1, negOf(term2))
    val pattern = ConditionPattern(sum, numericCondition(term1, term2) { n1, n2 -> n1 >= n2 })

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(sum, makeNumericOp(term1, term2) { n1, n2 -> n1 - n2 }),
        explanationMaker = makeMetadata("evaluate integer subtraction", move(term1), move(term2)),
    )
}

val evaluateSumOfPositiveAndNegativeIntegers = run {
    val term1 = IntegerPattern()
    val term2 = IntegerPattern()
    val pattern = sumContaining(term1, negOf(term2))

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeNumericOp(term1, term2) { n1, n2 -> n1 - n2 }),
        explanationMaker = makeMetadata("add positive and negative integers", move(term1), move(term2)),
    )
}


val evaluateSumOfNegativeAndPositiveIntegers = run {
    val term1 = IntegerPattern()
    val term2 = IntegerPattern()
    val pattern = sumContaining(negOf(term1), term2)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeNumericOp(term1, term2) { n1, n2 -> -n1 + n2 }),
        explanationMaker = makeMetadata("add negative and positive integers", move(term1), move(term2)),
    )
}

val evaluateSumOfNegativeIntegers = run {
    val term1 = IntegerPattern()
    val term2 = IntegerPattern()
    val pattern = sumContaining(negOf(term1), negOf(term2))

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeNumericOp(term1, term2) { n1, n2 -> -n1 - n2 }),
        explanationMaker = makeMetadata("add negative integers", move(term1), move(term2)),
    )
}

val evaluateIntegerProduct = run {
    val factor1 = IntegerPattern()
    val factor2 = IntegerPattern()
    val pattern = productContaining(factor1, factor2)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeNumericOp(factor1, factor2) { n1, n2 -> n1 * n2 }),
        explanationMaker = makeMetadata("evaluate integer product", move(factor1), move(factor2)),
    )
}

