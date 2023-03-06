package methods.general

import engine.context.ResourceData
import engine.expressions.Child
import engine.expressions.Expression
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps

enum class NormalizationPlans(override val runner: CompositeMethod) : RunnerMethod {
    AddClarifyingBrackets(
        plan {
            explanation = Explanation.AddClarifyingBrackets

            steps {
                whilePossible { deeply(NormalizationRules.ReplaceInvisibleBrackets) }
            }
        },
    ),
    NormalizeExpression(
        plan {
            explanation = Explanation.NormalizeExpression

            steps {
                whilePossible {
                    deeply {
                        firstOf {
                            option(AddClarifyingBrackets)
                            option(removeRedundantBrackets)
                            option(NormalizationRules.RemoveRedundantPlusSign)
                        }
                    }
                }
            }
        },
    ),
    NormaliseSimplifiedProduct(normaliseSimplifiedProduct),
}

private val normaliseSimplifiedProduct = plan {
    explanation = Explanation.NormaliseSimplifiedProduct

    steps { optionally(NormalizationRules.NormaliseSimplifiedProductRule) }
    alternative(ResourceData(gmFriendly = true)) {
        whilePossible(NormalizationRules.NormaliseSimplifiedProductSingleStep)
        optionally(NormalizationRules.NormalizeTheImplicitnessAndExplicitnessOfMultiplication)
    }
}

fun redundantBracketChecker(sub: Expression): Expression? = when {
    !sub.hasBracket() -> null
    sub.parent == null -> sub
    sub.origin is Child -> {
        val origin = sub.origin as Child
        if (origin.parent.operator.nthChildAllowed(origin.index, sub.operator)) sub else null
    }
    else -> null
}

val removeRedundantBrackets = steps {
    firstOf {
        option {
            applyTo(NormalizationRules.RemoveOuterBracket, ::redundantBracketChecker)
        }
        option(NormalizationRules.RemoveBracketSumInSum)
        option(NormalizationRules.RemoveBracketProductInProduct)
        option(NormalizationRules.RemoveBracketAroundSignedIntegerInSum)
    }
}
