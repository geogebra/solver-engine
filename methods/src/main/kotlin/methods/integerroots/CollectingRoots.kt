package methods.integerroots

import engine.expressionmakers.ExpressionMaker
import engine.expressionmakers.makeProductOf
import engine.expressionmakers.makeSumOf
import engine.expressionmakers.move
import engine.expressions.MappedExpression
import engine.methods.Rule
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.custom
import engine.patterns.integerOrderRootOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.makeMetadata

/**
 * Collects a set of like roots (square or higher) in a sum and simplifies the resulting expression
 *  2 + sqrt[3] - [2/3]*sqrt[3] + sqrt[5] + [sqrt[3]/2] -> 2 + (1 - [2/3] + [1/2])*sqrt[3] + sqrt[5]
 */
val collectLikeRoots = run {
    val root = integerOrderRootOf(UnsignedIntegerPattern())
    val rootTerm1 = withOptionalRationalCoefficient(root)
    val rootTerm2 = withOptionalRationalCoefficient(root)
    val pattern = sumContaining(rootTerm1, rootTerm2)

    Rule(
        pattern = pattern,
        resultMaker = custom {
            val rootTerm = withOptionalRationalCoefficient(root)
            val rootCoefficients = mutableListOf<MappedExpression>()

            val otherTerms = mutableListOf<ExpressionMaker>()
            var firstRootIndex: Int? = null

            for (term in get(pattern)!!.children()) {
                val match = matchPattern(rootTerm, term)
                if (match != null) {
                    rootCoefficients.add(rootTerm.coefficient(match))
                    if (firstRootIndex == null) {
                        firstRootIndex = term.index()
                    }
                } else {
                    otherTerms.add(term)
                }
            }

            require(firstRootIndex != null)

            val collectedRoots = makeProductOf(makeSumOf(rootCoefficients), move(root))
            otherTerms.add(firstRootIndex, collectedRoots)

            makeSumOf(otherTerms)
        },
        explanationMaker = makeMetadata(Explanation.CollectLikeRoots)
    )
}
