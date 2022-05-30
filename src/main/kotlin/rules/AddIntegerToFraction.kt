package rules

import expressionmakers.*
import expressions.xp
import patterns.CommutativeNaryPattern
import patterns.UnsignedIntegerPattern
import patterns.commutativeSumOf
import patterns.fractionOf
import steps.makeMetadata

val addIntegerToFraction = rule<CommutativeNaryPattern> {
    val integer = UnsignedIntegerPattern()
    val numerator = UnsignedIntegerPattern()
    val denominator = UnsignedIntegerPattern()
    val fraction = fractionOf(numerator, denominator)

    pattern = commutativeSumOf(integer, fraction)
    resultMaker = substituteIn(
        pattern,
        makeFractionOf(
            makeProductOf(move(integer), introduce(denominator)),
            makeProductOf(FixedExpressionMaker(xp(1)), introduce(denominator))
        ),
        move(fraction),
    )
    explanationMaker = makeMetadata("add integer to fraction", move(integer), move(fraction))
}
