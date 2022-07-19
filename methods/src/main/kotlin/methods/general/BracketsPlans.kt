package methods.general

import engine.methods.plan

val normalizeBrackets = plan {
    explanation(Explanation.NormalizeBracketsInExpression)

    whilePossible {
        deeply {
            firstOf {
                option(replaceInvisibleBrackets)
                option(removeBracketAroundUnsignedInteger)
                option(removeBracketAroundSignedIntegerInSum)
            }
        }
    }
}
