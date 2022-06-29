package methods.rules

import engine.expressionmakers.cancel
import engine.expressionmakers.makeFractionOf
import engine.expressionmakers.move
import engine.expressionmakers.restOf
import engine.patterns.AnyPattern
import engine.patterns.fractionOf
import engine.patterns.productContaining
import engine.rules.Rule
import engine.steps.metadata.Explanation
import engine.steps.metadata.makeMetadata

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
