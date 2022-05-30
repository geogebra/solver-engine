package rules

import expressionmakers.cancel
import expressionmakers.makeFractionOf
import expressionmakers.move
import expressionmakers.restOf
import patterns.AnyPattern
import patterns.Pattern
import patterns.fractionOf
import patterns.productContaining
import steps.makeMetadata

val cancelInAFraction = rule<Pattern> {
    val common = AnyPattern()
    val numerator = productContaining(common, minSize = 2)
    val denominator = productContaining(common, minSize = 2)

    pattern = fractionOf(numerator, denominator)
    resultMaker = cancel(common, makeFractionOf(restOf(numerator), restOf(denominator)))
    explanationMaker = makeMetadata("cancel in fraction", move(pattern), move(common))
}
