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
        step(addFractions)
    }
}

val addMixedNumbersByConverting = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    pipeline {

        step {
            applyToChildrenInStep(convertMixedNumberToImproperFraction)
        }
        step(addFractions)
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
        step(addFractions)
        step(convertIntegerToFraction)
        step(addFractions)
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
