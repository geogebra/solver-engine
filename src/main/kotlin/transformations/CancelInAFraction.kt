package transformations

import expressions.Expression
import expressions.fractionOf
import patterns.AnyPattern
import patterns.Match
import patterns.fractionOf
import patterns.productContaining
import steps.PathMappingType

object CancelInAFraction : Rule {

    private val common = AnyPattern()
    private val numerator = productContaining(common)
    private val denominator = productContaining(common)

    override val pattern = fractionOf(numerator, denominator)

    override fun apply(match: Match): Expression? {
        val numVal = match.getPathMappingExpr(numerator, PathMappingType.Transform)
        val denomVal = match.getPathMappingExpr(denominator, PathMappingType.Transform)

        if (numVal.expr.children().size == 1 || denomVal.expr.children().size == 1) {
            return null
        }

        val numeratorRest = numerator.getRest(match)
        val denominatorRest = denominator.getRest(match)

        return fractionOf(numeratorRest, denominatorRest)
    }
}