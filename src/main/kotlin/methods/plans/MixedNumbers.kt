package methods.plans

import engine.patterns.mixedNumberOf
import engine.patterns.sumOf
import engine.plans.plan
import methods.rules.*

val convertMixedNumberToImproperFraction = plan {
    pattern = mixedNumberOf()

    pipeline {
        step(splitMixedNumber)
        step(convertIntegerToFraction)
        step(addPositiveFractions)
    }
}

val addMixedNumbersByConverting = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    pipeline {

        step {
            applyToChildrenInStep(convertMixedNumberToImproperFraction)
        }
        step(addPositiveFractions)
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
        step(addPositiveFractions)
        step(convertIntegerToFraction)
        step(addPositiveFractions)
        step(fractionToMixedNumber)
    }
}

val addMixedNumbers = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    selectFromContext {
        case(curriculum = "EU", addMixedNumbersByConverting)
        case(curriculum = "US", addMixedNumbersUsingCommutativity)
        default(addMixedNumbersByConverting)
    }
}
