package methods.plans

import engine.expressionmakers.move
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.sumContaining
import engine.plans.plan
import engine.steps.metadata.PlanExplanation
import engine.steps.metadata.Skill
import methods.rules.*

val addFractions = plan {
    val f1 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    val f2 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    pattern = sumContaining(f1, f2)

    explanation(PlanExplanation.AddFractions, move(f1), move(f2))

    skill(Skill.AddFractions, move(f1), move(f2))

    pipeline {
        optionalStep(commonDenominator)
        optionalStep {
            whilePossible {
                deeply(evaluateIntegerProduct)
            }
        }
        step(addLikeFractions)
        step {
            whilePossible {
                deeply(evaluateSignedIntegerAddition)
            }
        }
    }
}

val simplifyNumericFraction = plan {
    val f = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

    pattern = f

    explanation(PlanExplanation.SimplifyNumericFraction, move(f))

    skill(Skill.SimplifyNumericFraction, move(f))

    firstOf {
        option(simplifyFractionToInteger)
        option {
            pipeline {
                step(findCommonFactorInFraction)
                step(cancelInAFraction)
            }
        }
    }
}

val addFractionsAndSimplify = plan {
    pipeline {
        step(addFractions)
        optionalStep {
            deeply(simplifyNumericFraction)
        }
    }
}
