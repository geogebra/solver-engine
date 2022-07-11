package methods.mixednumbers

import engine.context.ResourceData
import engine.methods.ContextSensitiveMethod
import engine.methods.ContextSensitiveMethodSelector
import engine.methods.PlanId
import engine.methods.plan
import engine.patterns.mixedNumberOf
import engine.patterns.sumOf
import methods.fractionarithmetic.convertIntegerToFraction
import methods.fractionarithmetic.evaluatePositiveFractionSum
import methods.general.removeBracketsSum
import methods.integerarithmetic.evaluateSignedIntegerAddition

val convertMixedNumberToImproperFraction = plan {
    pattern = mixedNumberOf()

    pipeline {
        step(splitMixedNumber)
        step(convertIntegerToFraction)
        step(evaluatePositiveFractionSum)
    }
}

val addMixedNumbersByConverting = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    pipeline {

        step {
            applyToChildrenInStep(convertMixedNumberToImproperFraction)
        }
        step(evaluatePositiveFractionSum)
        step(fractionToMixedNumber)
    }
}

val addMixedNumbersUsingCommutativity = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    pipeline {
        step {
            whilePossible {
                deeply(splitMixedNumber)
            }
        }
        step {
            whilePossible(removeBracketsSum)
        }
        step(evaluateSignedIntegerAddition)
        step(evaluatePositiveFractionSum)
        step(convertIntegerToFraction)
        step(evaluatePositiveFractionSum)
        step(fractionToMixedNumber)
    }
}

val addMixedNumbers = plan {
    planId = PlanId.AddMixedNumbers

    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    firstOf {
        option(
            ContextSensitiveMethodSelector(
                default = ContextSensitiveMethod(addMixedNumbersByConverting, ResourceData(curriculum = "EU")),
                alternatives = listOf(
                    ContextSensitiveMethod(addMixedNumbersByConverting, ResourceData(curriculum = "EU")),
                    ContextSensitiveMethod(addMixedNumbersUsingCommutativity, ResourceData(curriculum = "US")),
                )
            )
        )
    }
}
