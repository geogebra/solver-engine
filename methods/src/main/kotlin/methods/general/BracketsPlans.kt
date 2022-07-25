package methods.general

import engine.expressions.BracketOperator
import engine.expressions.Subexpression
import engine.methods.plan

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

val removeRedundantBrackets = plan {
    whilePossible {
        firstOf {
            option {
                applyTo(removeOuterBracket, redundantBracketChecker)
            }
            option(removeBracketAroundSignedIntegerInSum)
        }
    }
}
