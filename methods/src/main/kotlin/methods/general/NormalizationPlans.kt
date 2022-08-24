package methods.general

import engine.expressions.BracketOperator
import engine.expressions.Subexpression
import engine.methods.plan
import engine.methods.steps

val addClarifyingBrackets = plan {
    explanation(Explanation.AddClarifyingBrackets)

    whilePossible {
        deeply(replaceInvisibleBrackets)
    }
}

val redundantBracketChecker = { sub: Subexpression ->
    if (sub.expr.operator is BracketOperator && (
        sub.parent == null ||
            sub.parent!!.expr.operator.nthChildAllowed(sub.index(), sub.expr.operands[0].operator)
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
            applyTo(removeOuterBracket, redundantBracketChecker)
        }
        option(removeBracketsSum)
        option(removeBracketsProduct)
        option(removeBracketAroundSignedIntegerInSum)
    }
}

val normalizeExpression = plan {
    explanation(Explanation.NormalizeExpression)

    whilePossible {
        deeply {
            firstOf {
                option(addClarifyingBrackets)
                option(removeRedundantBrackets)
                option(removeRedundantPlusSign)
            }
        }
    }
}
