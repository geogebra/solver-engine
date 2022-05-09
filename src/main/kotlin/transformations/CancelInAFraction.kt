package transformations

import expressionmakers.cancel
import expressionmakers.makeFractionOf
import expressionmakers.restOf
import patterns.AnyPattern
import patterns.fractionOf
import patterns.productContaining

object CancelInAFraction : Rule {

    private val common = AnyPattern()
    private val numerator = productContaining(common, minSize = 2)
    private val denominator = productContaining(common, minSize = 2)

    override val pattern = fractionOf(numerator, denominator)
    override val resultMaker = cancel(common, makeFractionOf(restOf(numerator), restOf(denominator)))
}
