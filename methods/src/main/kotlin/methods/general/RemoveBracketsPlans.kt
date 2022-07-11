package methods.general

import engine.plans.plan

val replaceAllInvisibleBrackets = plan {
    explanation(Explanation.ReplaceAllInvisibleBrackets)

    whilePossible {
        deeply(replaceInvisibleBrackets)
    }
}
