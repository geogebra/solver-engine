package rules

import expressionmakers.*
import expressions.xp
import patterns.*
import steps.makeMetadata

val eliminateZeroInSum = run {
    val zero = FixedPattern(xp(0))
    val pattern = sumContaining(zero)

    Rule(
        pattern = pattern,
        resultMaker = cancel(zero, restOf(pattern)),
        explanationMaker = makeMetadata("eliminate 0 in sum", move(zero)),
    )
}

val eliminateOneInProduct = run {
    val one = FixedPattern(xp(1))
    val pattern = productContaining(one)

    Rule(
        pattern = pattern,
        resultMaker = cancel(one, restOf(pattern)),
        explanationMaker = makeMetadata("eliminate 1 in product", move(one)),
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
        explanationMaker = makeMetadata(
            "evaluate integer subtraction", move(term1), move(term2)
        ),
    )
}

val evaluateSignedIntegerAddition = run {
    val term1 = SignedIntegerPattern()
    val term2 = SignedIntegerPattern()
    val sum = sumContaining(term1, term2)

    Rule(
        pattern = sum,
        resultMaker = substituteIn(sum, makeNumericOp(term1, term2) { n1, n2 -> n1 + n2 }),
        explanationMaker = makeMetadata(
            "evaluate integer addition", move(term1), move(term2)
        ),
    )
}

val evaluateIntegerProduct = run {
    val factor1 = UnsignedIntegerPattern()
    val factor2 = UnsignedIntegerPattern()
    val pattern = productContaining(factor1, factor2)

    Rule(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeNumericOp(factor1, factor2) { n1, n2 -> n1 * n2 }),
        explanationMaker = makeMetadata("evaluate integer product", move(factor1), move(factor2)),
    )
}

val evaluateSignedIntegerProduct = run {
    val factor1 = SignedIntegerPattern()
    val factor2 = SignedIntegerPattern()
    val product = productContaining(factor1, factor2)

    Rule(
        pattern = product,
        resultMaker = substituteIn(product, makeNumericOp(factor1, factor2) { n1, n2 -> n1 * n2 }),
        explanationMaker = makeMetadata(
            "evaluate integer product", move(factor1), move(factor2)
        ),
    )
} // x * 2 * y * 7 -> x * 14 * y

val simplifyDoubleNeg = run {
    val value = AnyPattern()
    val pattern = negOf(bracketOf(negOf(value)))

    Rule(
        pattern = pattern,
        resultMaker = move(value),
        explanationMaker = makeMetadata("simplify -(-x)", move(value))
    )
}

// 1 + 2 - 3 +  4 + (-3) * 4 * (-6)


// (-3) * 4 * (-6)
// (-12) * (-6)
// 72

// (-3) * 4 * (-6)
// 3 * 4 * 6
//

// 1 + 4 * (-3) * (-4) * 4 * (-6)
// 1 + 4 * 3 * 4 * 4 * (-6)
// 1 - 4 * 3 * 4 * 4 * 6