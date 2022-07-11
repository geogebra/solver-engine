package methods.mixednumbers

import engine.patterns.mixedNumberOf
import engine.patterns.sumOf
import engine.plans.PlanId
import engine.plans.plan
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

    selectFromContext {
        case(curriculum = "EU", addMixedNumbersByConverting)
        case(curriculum = "US", addMixedNumbersUsingCommutativity)
        default(addMixedNumbersByConverting)
    }
}
