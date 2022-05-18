package rules

import expressionmakers.*
import expressions.IntegerExpr
import patterns.IntegerPattern
import patterns.fractionOf
import patterns.sumOf
import steps.makeMetadata

val addIntegerToFraction = run {

    val integer = IntegerPattern()
    val numerator = IntegerPattern()
    val denominator = IntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    Rule(
        pattern = sumOf(integer, fraction),
        resultMaker = makeSumOf(
            makeFractionOf(
                makeProductOf(move(integer), introduce(denominator)),
                makeProductOf(FixedExpressionMaker(IntegerExpr(1)), introduce(denominator))
            ),
            move(fraction)
        ),
        explanationMaker = makeMetadata("add integer to fraction", move(integer), move(fraction)),
    )
}
