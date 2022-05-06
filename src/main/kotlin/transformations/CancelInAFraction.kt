package transformations

import expressions.Expression
import patterns.*

object CancelInAFraction : Rule {

    private val common = AnyPattern()
    private val numerator = productContaining(common, minSize = 2)
    private val denominator = productContaining(common, minSize = 2)

    override val pattern = fractionOf(numerator, denominator)

    override fun apply(match: Match): Expression? {
        return cancel(common, makeFractionOf(restOf(numerator), restOf(denominator))).makeExpression(match)
    }
}

