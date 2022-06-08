package methods.plans

import engine.plans.plan
import engine.steps.metadata.PlanExplanation
import methods.rules.replaceInvisibleBrackets

val replaceAllInvisibleBrackets = plan {
    explanation(PlanExplanation.ReplaceAllInvisibleBrackets)

    whilePossible {
        deeply(replaceInvisibleBrackets)
    }
}
