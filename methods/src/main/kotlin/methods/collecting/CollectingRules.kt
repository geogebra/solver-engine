package methods.collecting

import engine.expressions.Expression
import engine.expressions.Label
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.CoefficientPattern
import engine.patterns.Pattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.rootOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.patterns.withOptionalIntegerCoefficient
import engine.patterns.withOptionalRationalCoefficient
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata

/**
 * Create a rule which collects all the terms matching the common pattern
 * with the given coefficient.
 * E.g. when the common pattern matches roots with rational coefficients:
 *  3 sqrt[2] + sqrt[3] + [1 / 2] sqrt[2] -> (3 + [1 / 2]) sqrt[2] + sqrt[3]
 */
private fun createCollectLikeTermsRule(
    commonPattern: Pattern,
    coefficientWrapper: (Pattern) -> CoefficientPattern,
    explanationKey: MetadataKey,
): Rule {
    return rule {
        val commonTerm1 = coefficientWrapper(commonPattern)
        val commonTerm2 = coefficientWrapper(commonPattern)
        val sum = sumContaining(commonTerm1, commonTerm2)

        onPattern(sum) {
            val commonTerm = coefficientWrapper(commonPattern)

            val coefficients = mutableListOf<Expression>()
            val otherTerms = mutableListOf<Expression>()
            var firstIndex: Int? = null

            for ((index, term) in get(sum).children.withIndex()) {
                val m = matchPattern(commonTerm, term)
                if (m != null) {
                    coefficients.add(commonTerm.coefficient(m))
                    if (firstIndex == null) {
                        firstIndex = index
                    }
                } else {
                    otherTerms.add(term)
                }
            }

            require(firstIndex != null)

            val collectedTerms = productOf(sumOf(coefficients), move(commonTerm.value)).withLabel(Label.A)
            otherTerms.add(firstIndex, collectedTerms)

            ruleResult(
                toExpr = sumOf(otherTerms),
                explanation = metadata(explanationKey),
            )
        }
    }
}

/**
 * Create a rule which combines two occurences of the common pattern
 * with integer coefficients.
 * E.g. when the common pattern matches roots:
 *  3 sqrt[2] + sqrt[3] + 5 sqrt[2] -> 8 sqrt[2] + sqrt[3]
 */
private fun createCombineSimpleLikeTermsRule(
    commonPattern: Pattern,
    explanationKey: MetadataKey,
): Rule {
    return rule {
        val t1 = withOptionalIntegerCoefficient(commonPattern, false)
        val t2 = withOptionalIntegerCoefficient(commonPattern, false)
        val sum = sumContaining(t1, t2)

        onPattern(sum) {
            if (!context.gmFriendly) return@onPattern null
            val newCoef =
                integerOp(t1.integerCoefficient, t2.integerCoefficient) { n1, n2 -> (n1 + n2).abs() }
            val newCoefValue = getValue(t1.integerCoefficient) + getValue(t2.integerCoefficient)
            val newTermAbs = simplifiedProductOf(newCoef, factor(commonPattern))
            val newTerm = if (newCoefValue < java.math.BigInteger.ZERO) negOf(newTermAbs) else newTermAbs

            ruleResult(
                toExpr = sum.substitute(newTerm),
                gmAction = drag(t2, t1),
                explanation = metadata(explanationKey, move(t1), move(t2)),
            )
        }
    }
}

enum class CollectingRules(override val runner: Rule) : RunnerMethod {
    CollectLikeRoots(
        createCollectLikeTermsRule(
            commonPattern = rootOf(UnsignedIntegerPattern()),
            coefficientWrapper = { withOptionalRationalCoefficient(it) },
            explanationKey = Explanation.CollectLikeRoots,
        ),
    ),

    CollectLikeRationalPowers(
        createCollectLikeTermsRule(
            commonPattern = powerOf(UnsignedIntegerPattern(), engine.patterns.IntegerFractionPattern()),
            coefficientWrapper = { withOptionalRationalCoefficient(it) },
            explanationKey = Explanation.CollectLikeRationalPowers,
        ),
    ),

    CollectLikeTerms(
        createCollectLikeTermsRule(
            commonPattern = condition { !it.isConstant() },
            coefficientWrapper = { withOptionalConstantCoefficient(it) },
            explanationKey = Explanation.CollectLikeTerms,
        ),
    ),

    CombineTwoSimpleLikeRoots(
        createCombineSimpleLikeTermsRule(
            commonPattern = rootOf(UnsignedIntegerPattern()),
            explanationKey = Explanation.CombineTwoSimpleLikeRoots,
        ),
    ),

    CombineTwoSimpleLikeRationalPowers(
        createCombineSimpleLikeTermsRule(
            commonPattern = powerOf(UnsignedIntegerPattern(), engine.patterns.IntegerFractionPattern()),
            explanationKey = Explanation.CombineTwoSimpleLikeRationalPowers,
        ),
    ),

    CombineTwoSimpleLikeTerms(
        createCombineSimpleLikeTermsRule(
            commonPattern = oneOf(
                ArbitraryVariablePattern(),
                powerOf(ArbitraryVariablePattern(), UnsignedIntegerPattern()),
            ),
            explanationKey = Explanation.CombineTwoSimpleLikeTerms,
        ),
    ),
}
