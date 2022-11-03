package methods.general

import engine.expressions.Subexpression
import engine.methods.plan
import engine.methods.stepsproducers.steps

val addClarifyingBrackets = plan {
    explanation(Explanation.AddClarifyingBrackets)

    steps {
        whilePossible { deeply(NormalizationRules.ReplaceInvisibleBrackets) }
    }
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

val normalizeExpression = plan {
    explanation(Explanation.NormalizeExpression)

    steps {
        whilePossible {
            deeply {
                firstOf {
                    option(addClarifyingBrackets)
                    option(removeRedundantBrackets)
                    option(NormalizationRules.RemoveRedundantPlusSign)
                }
            }
        }
    }
}
