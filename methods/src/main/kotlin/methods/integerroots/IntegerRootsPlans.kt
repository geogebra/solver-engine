package methods.integerroots

import engine.methods.plan
import engine.methods.steps
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.squareRootOf
import methods.general.removeRedundantBrackets
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.integerarithmetic.simplifyIntegersInProduct

val rewriteFactorizedSquareRootAsProductOfFactors = plan {
    pipeline {
        steps(separateFactorizedPowersUnderSquareRootAsSquareRoots)
        steps(splitPowerUnderSquareRootOfProduct)
        steps(splitProductOfPowerUnderSquareRootAsProductMultipleRemoveBrackets)
        steps(simplifyEvenIntegerPowerUnderRootProduct)
    }
}

val findIntegerPartOfRoot = steps {
    firstOf {
        option {
            deeply(evaluateSignedIntegerPower, deepFirst = true)
        }
        option {
            deeply(simplifyIntegersInProduct, deepFirst = true)
        }
        option { deeply(removeRedundantBrackets, deepFirst = true) }
    }
}

val simplifyIntegerPartOfRoot = plan {
    whilePossible(findIntegerPartOfRoot)
}

val findValueOfRoot = plan {
    pipeline {
        optionalSteps(simplifyIntegerPartOfRoot)
        optionalSteps(multiplySquareRootFactors)
    }
}

val simplifyIntegerRoot = plan {
    pattern = squareRootOf(UnsignedIntegerPattern())

    pipeline {
        optionalSteps(factorizeIntegerUnderSquareRoot)
        optionalSteps(rewriteFactorizedSquareRootAsProductOfFactors)
        optionalSteps(rewriteWithIntegerFactorsAtFront)
        optionalSteps(findValueOfRoot)
    }
}
