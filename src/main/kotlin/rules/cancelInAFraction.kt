package rules

import expressionmakers.cancel
import expressionmakers.makeFractionOf
import expressionmakers.move
import expressionmakers.restOf
import patterns.AnyPattern
import patterns.fractionOf
import patterns.productContaining
import steps.makeMetadata

val cancelInAFraction = run {
    val common = AnyPattern()
    val numerator = productContaining(common, minSize = 2)
    val denominator = productContaining(common, minSize = 2)
    val pattern = fractionOf(numerator, denominator)

    RuleData(
        pattern = pattern,
        resultMaker = cancel(common, makeFractionOf(restOf(numerator), restOf(denominator))),
        explanationMaker = makeMetadata("cancel in fraction", move(pattern), move(common)),
    )
}
