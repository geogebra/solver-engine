package methods.general

import engine.context.Curriculum
import engine.context.ResourceData
import engine.expressions.Constants
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.powerOf
import engine.patterns.sumOf

enum class GeneralPlans(override val runner: CompositeMethod) : RunnerMethod {
    RewriteDivisionsAsFractions(
        plan {
            explanation = Explanation.RewriteDivisionsAsFractionInExpression

            steps {
                whilePossible { deeply(GeneralRules.RewriteDivisionAsFraction) }
            }
        }
    ),

    ExpandBinomialSquared(expandBinomialSquared),
    ExpandBinomialCubed(expandBinomialCubed),
    ExpandTrinomialSquared(expandTrinomialSquared)
}

val normalizeNegativeSigns = steps {
    firstOf {
        option(GeneralRules.SimplifyDoubleMinus)
        option(GeneralRules.SimplifyProductWithTwoNegativeFactors)
        option(GeneralRules.MoveSignOfNegativeFactorOutOfProduct)
    }
}

val evaluateOperationContainingZero = steps {
    firstOf {
        option(GeneralRules.EvaluateZeroDividedByAnyValue)
        option(GeneralRules.EvaluateProductContainingZero)
    }
}

private val expandBinomialSquared = plan {
    explanation = Explanation.ExpandBinomialSquared
    pattern = powerOf(sumOf(AnyPattern(), AnyPattern()), FixedPattern(Constants.Two))

    steps(ResourceData(curriculum = Curriculum.EU)) {
        apply(GeneralRules.ExpandBinomialSquaredUsingIdentity)
    }
    alternative(ResourceData(curriculum = Curriculum.US)) {
        apply(GeneralRules.RewritePowerAsProduct)
    }
}

private val expandBinomialCubed = plan {
    explanation = Explanation.ExpandBinomialCubed
    pattern = powerOf(sumOf(AnyPattern(), AnyPattern()), FixedPattern(Constants.Three))

    steps(ResourceData(curriculum = Curriculum.EU)) {
        apply(GeneralRules.ExpandBinomialCubedUsingIdentity)
    }
    alternative(ResourceData(curriculum = Curriculum.US)) {
        apply(GeneralRules.RewritePowerAsProduct)
    }
}

private val expandTrinomialSquared = plan {
    explanation = Explanation.ExpandTrinomialSquared
    pattern = powerOf(
        sumOf(AnyPattern(), AnyPattern(), AnyPattern()),
        FixedPattern(Constants.Two)
    )

    steps(ResourceData(curriculum = Curriculum.EU)) {
        apply(GeneralRules.ExpandTrinomialSquaredUsingIdentity)
    }
    alternative(ResourceData(curriculum = Curriculum.US)) {
        apply(GeneralRules.RewritePowerAsProduct)
    }
}
