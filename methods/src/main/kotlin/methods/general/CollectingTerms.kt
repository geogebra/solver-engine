package methods.general

import engine.expressions.MappedExpression
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.TransformationResult
import engine.methods.plan
import engine.methods.rule
import engine.patterns.Pattern
import engine.patterns.sumContaining
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.fractionarithmetic.simplifyFractionsInExpression

/**
 * Creates a rule for collecting a set of like terms in a sum
 * What you mean by "like term" can be customized using the "common" pattern
 * E.g. for `integerOrderRootOf(UnsignedIntegerPattern())` we get a rule that does:
 *  2 + sqrt[3] - [2/3]*sqrt[3] + sqrt[5] + [sqrt[3]/2] -> 2 + (1 - [2/3] + [1/2])*sqrt[3] + sqrt[5]
 */
val collectLikeTerms = { common: Pattern, explanation: MetadataKey ->
    rule {
        val commonTerm1 = withOptionalRationalCoefficient(common)
        val commonTerm2 = withOptionalRationalCoefficient(common)
        val pattern = sumContaining(commonTerm1, commonTerm2)

        onPattern(pattern) {
            val commonTerm = withOptionalRationalCoefficient(common)
            val coefficients = mutableListOf<MappedExpression>()

            val otherTerms = mutableListOf<MappedExpression>()
            var firstIndex: Int? = null

            for (term in get(pattern)!!.children()) {
                val match = matchPattern(commonTerm, term)
                if (match != null) {
                    coefficients.add(commonTerm.coefficient(match))
                    if (firstIndex == null) {
                        firstIndex = term.index()
                    }
                } else {
                    otherTerms.add(move(term))
                }
            }

            require(firstIndex != null)

            val collectedRoots = productOf(sumOf(coefficients), move(common))
            otherTerms.add(firstIndex, collectedRoots)

            TransformationResult(
                toExpr = sumOf(otherTerms),
                explanation = metadata(explanation)
            )
        }
    }
}

/**
 * Creates a plan which collects a set of like terms in a sum and simplifies the resulting expression
 * E.g. for `integerOrderRootOf(UnsignedIntegerPattern())` we get a plan that does:
 *   2 + sqrt[3] - [2/3]*sqrt[3]] + sqrt[5] + [sqrt[3]/2]
 * sqrt[3] is collected first
 *   2 + (1 - [2/3] + [1/2])*sqrt[3] + sqrt[5]
 * then the bracket is simplified
 *   2 + [5/6]*sqrt[3] + sqrt[5]
 * And the expression is written in a normalized way
 *   2 + [5*sqrt[3]/6] + sqrt[5]
 */
val collectLikeTermsAndSimplify = { common: Pattern, planExplanation: MetadataKey, ruleExplanation: MetadataKey ->
    plan {
        explanation(planExplanation)

        apply(collectLikeTerms(common, ruleExplanation))
        apply(simplifyFractionsInExpression)
        optionally { deeply(moveSignOfNegativeFactorOutOfProduct) }
        optionally { deeply(removeRedundantBrackets) }
        optionally { deeply(multiplyAndSimplifyFractions) }
    }
}
