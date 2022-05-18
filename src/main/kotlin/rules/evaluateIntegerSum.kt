package rules

import expressionmakers.*
import expressions.IntegerExpr
import patterns.FixedPattern
import patterns.IntegerPattern
import patterns.sumContaining
import steps.makeMetadata

val evaluateIntegerSum = run {
    val term1 = IntegerPattern()
    val term2 = IntegerPattern()
    val pattern = sumContaining(term1, term2)

    RuleData(
        pattern = pattern,
        resultMaker = substituteIn(pattern, makeNumericOp(term1, term2) { n1, n2 -> n1 + n2 }),
        explanationMaker = makeMetadata("evaluate integer sum", move(term1), move(term2)),
    )
}

val eliminateZeroInSum = run {
    val zero = FixedPattern(IntegerExpr(0))
    val pattern = sumContaining(zero)

    RuleData(
        pattern = pattern,
        resultMaker = cancel(zero, restOf(pattern)),
        explanationMaker = makeMetadata("eliminate 0 in sum", move(zero)),
    )
}
