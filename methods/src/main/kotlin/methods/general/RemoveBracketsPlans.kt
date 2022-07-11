package methods.general

import engine.methods.plan

val replaceAllInvisibleBrackets = plan {
    explanation(Explanation.ReplaceAllInvisibleBrackets)

    whilePossible {
        deeply(replaceInvisibleBrackets)
    }
}
