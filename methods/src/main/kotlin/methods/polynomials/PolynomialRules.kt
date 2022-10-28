package methods.polynomials

import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.metadata.metadata

val collectLikeTerms = rule {
    val common = oneOf(ArbitraryVariablePattern(), powerOf(ArbitraryVariablePattern(), UnsignedIntegerPattern()))

    val commonTerm1 = withOptionalConstantCoefficient(common)
    val commonTerm2 = withOptionalConstantCoefficient(common)
    val sum = sumContaining(commonTerm1, commonTerm2)

    onPattern(sum) {
        TransformationResult(
            toExpr = collectLikeTermsInSum(get(sum)!!, withOptionalConstantCoefficient(common)),
            explanation = metadata(Explanation.CollectLikeTerms)
        )
    }
}
