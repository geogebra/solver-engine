package rules

import expressionmakers.cancel
import expressionmakers.makeFractionOf
import expressionmakers.move
import expressionmakers.restOf
import patterns.AnyPattern
import patterns.fractionOf
import patterns.productContaining
import steps.metadata.Explanation
import steps.metadata.makeMetadata

val cancelInAFraction = run {
    val common = AnyPattern()
    val numerator = productContaining(common, minSize = 2)
    val denominator = productContaining(common, minSize = 2)
    val pattern = fractionOf(numerator, denominator)

    Rule(
        pattern = pattern,
        resultMaker = cancel(common, makeFractionOf(restOf(numerator), restOf(denominator))),
        explanationMaker = makeMetadata(Explanation.CancelCommonTerms, move(pattern), move(common)),
    )
}
