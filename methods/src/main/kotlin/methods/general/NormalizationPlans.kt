package methods.general

import engine.expressions.Subexpression
import engine.methods.Plan
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps

enum class NormalizationPlans(override val runner: Plan) : RunnerMethod {
    AddClarifyingBrackets(
        plan {
            explanation(Explanation.AddClarifyingBrackets)

            steps {
                whilePossible { deeply(NormalizationRules.ReplaceInvisibleBrackets) }
            }
        }
    ),
    NormalizeExpression(
        plan {
            explanation(Explanation.NormalizeExpression)

            steps {
                whilePossible {
                    deeply {
                        firstOf {
                            option(NormalizationPlans.AddClarifyingBrackets)
                            option(removeRedundantBrackets)
                            option(NormalizationRules.RemoveRedundantPlusSign)
                        }
                    }
                }
            }
        }
    )
}

val redundantBracketChecker = { sub: Subexpression ->
    if (sub.expr.hasBracket() && (
        sub.parent == null ||
            sub.parent!!.expr.operator.nthChildAllowed(sub.index(), sub.expr.operator)
        )
    ) {
        sub
    } else {
        null
    }
}

val removeRedundantBrackets = steps {
    firstOf {
        option {
            applyTo(NormalizationRules.RemoveOuterBracket, redundantBracketChecker)
        }
        option(NormalizationRules.RemoveBracketSumInSum)
        option(NormalizationRules.RemoveBracketProductInProduct)
        option(NormalizationRules.RemoveBracketAroundSignedIntegerInSum)
    }
}
