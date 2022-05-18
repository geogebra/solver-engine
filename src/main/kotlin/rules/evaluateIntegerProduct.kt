package rules

import expressionmakers.*
import expressions.IntegerExpr
import patterns.FixedPattern
import patterns.IntegerPattern
import patterns.productContaining
import steps.makeMetadata

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

val eliminateOneInProduct = run {
    val one = FixedPattern(IntegerExpr(1))
    val pattern = productContaining(one)

    Rule(
        pattern = pattern,
        resultMaker = cancel(one, restOf(pattern)),
        explanationMaker = makeMetadata("eliminate 1 in product", move(one)),
    )
}
