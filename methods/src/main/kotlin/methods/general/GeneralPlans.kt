package methods.general

import engine.methods.plan

val normalizeNegativeSigns = plan {
    firstOf {
        option(simplifyDoubleMinus)
        option(simplifyProductWithTwoNegativeFactors)
        option(moveSignOfNegativeFactorOutOfProduct)
    }
}
