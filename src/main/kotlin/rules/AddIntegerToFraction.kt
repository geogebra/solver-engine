package rules

import expressionmakers.*
import expressions.IntegerExpr
import patterns.UnsignedIntegerPattern
import patterns.commutativeSumOf
import patterns.fractionOf
import steps.makeMetadata

val addIntegerToFraction = run {

    val integer = UnsignedIntegerPattern()
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)
    val sum = commutativeSumOf(integer, fraction)

    Rule(
        pattern = sum,
        resultMaker = substituteIn(
            sum,
            makeFractionOf(
                makeProductOf(move(integer), introduce(denominator)),
                makeProductOf(FixedExpressionMaker(IntegerExpr(1)), introduce(denominator))
            ),
            move(fraction),
        ),
        explanationMaker = makeMetadata("add integer to fraction", move(integer), move(fraction)),
    )
}
