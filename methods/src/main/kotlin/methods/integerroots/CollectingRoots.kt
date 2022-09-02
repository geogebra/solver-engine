package methods.integerroots

import engine.expressions.MappedExpression
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.TransformationResult
import engine.methods.rule
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.integerOrderRootOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.metadata

/**
 * Collects a set of like roots (square or higher) in a sum and simplifies the resulting expression
 *  2 + sqrt[3] - [2/3]*sqrt[3] + sqrt[5] + [sqrt[3]/2] -> 2 + (1 - [2/3] + [1/2])*sqrt[3] + sqrt[5]
 */
val collectLikeRoots = rule {
    val root = integerOrderRootOf(UnsignedIntegerPattern())
    val rootTerm1 = withOptionalRationalCoefficient(root)
    val rootTerm2 = withOptionalRationalCoefficient(root)
    val pattern = sumContaining(rootTerm1, rootTerm2)

    onPattern(pattern) {
        val rootTerm = withOptionalRationalCoefficient(root)
        val rootCoefficients = mutableListOf<MappedExpression>()

        val otherTerms = mutableListOf<MappedExpression>()
        var firstRootIndex: Int? = null

        for (term in get(pattern)!!.children()) {
            val match = matchPattern(rootTerm, term)
            if (match != null) {
                rootCoefficients.add(rootTerm.coefficient(match))
                if (firstRootIndex == null) {
                    firstRootIndex = term.index()
                }
            } else {
                otherTerms.add(move(term))
            }
        }

        require(firstRootIndex != null)

        val collectedRoots = productOf(sumOf(rootCoefficients), move(root))
        otherTerms.add(firstRootIndex, collectedRoots)

        TransformationResult(
            toExpr = sumOf(otherTerms),
            explanation = metadata(Explanation.CollectLikeRoots)
        )
    }
}
