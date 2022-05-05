package transformations

import expressions.Expression
import expressions.NaryOperator
import expressions.fractionOf
import patterns.AnyPattern
import patterns.AssocNaryPattern
import patterns.Match
import patterns.fractionOf
import steps.PathMappingType

object CancelInAFraction : Rule {

    val common = AnyPattern()
    val numerator = AssocNaryPattern(NaryOperator.Product, listOf(common))
    val denominator = AssocNaryPattern(NaryOperator.Product, listOf(common))

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