package transformations

import expressionmakers.*
import expressions.IntegerExpr
import patterns.IntegerPattern
import patterns.fractionOf
import patterns.sumOf

object AddIntegerToFraction : Rule {

    private val integer = IntegerPattern()
    private val numerator = IntegerPattern()
    private val denominator = IntegerPattern()
    private val fraction = fractionOf(numerator, denominator)

    override val pattern = sumOf(integer, fraction)

    override val resultMaker = makeSumOf(
        makeFractionOf(
            makeProductOf(move(integer), introduce(denominator)),
            makeProductOf(FixedExpressionMaker(IntegerExpr(1)), introduce(denominator))
        ),
        move(fraction)
    )
}