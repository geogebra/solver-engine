package methods.decimals

import engine.context.ResourceData
import engine.expressionmakers.move
import engine.methods.plan
import engine.patterns.productContaining
import methods.fractionarithmetic.simplifyFraction
import methods.general.eliminateOneInProduct
import methods.general.evaluateProductContainingZero
import methods.integerarithmetic.evaluateSignedIntegerAddition

val convertTerminatingDecimalToFractionAndSimplify = plan {
    explanation(Explanation.ConvertTerminatingDecimalToFractionAndSimplify)

    pipeline {
        steps(convertTerminatingDecimalToFraction)
        optionalSteps(simplifyFraction)
    }
}

val convertRecurringDecimalToFractionAndSimplify = plan {
    explanation(Explanation.ConvertRecurringDecimalToFractionAndSimplify)

    resourceData = ResourceData(curriculum = "EU")
    pipeline {
        steps(convertRecurringDecimalToFractionDirectly)
        steps {
            deeply(evaluateSignedIntegerAddition)
        }
        optionalSteps(simplifyFraction)
    }

    alternative {
        resourceData = ResourceData(curriculum = "US")

        pipeline {
            steps(convertRecurringDecimalToEquation)
            steps(makeEquationSystemForRecurringDecimal)
            steps(simplifyEquationSystemForRecurringDecimal)
            steps(solveLinearEquation)
            optionalSteps(simplifyFraction)
        }
    }
}

val simplifyDecimalsInProduct = plan {
    pattern = productContaining()
    explanation(Explanation.SimplifyDecimalsInProduct, move(pattern!!))

    whilePossible {
        firstOf {
            option(evaluateProductContainingZero)
            option(evaluateDecimalProductAndDivision)
            option(eliminateOneInProduct)
        }
    }
}

val normalizeFractionOfDecimals = plan {
    explanation(Explanation.NormalizeFractionOfDecimals)

    pipeline {
        steps(multiplyFractionOfDecimalsByPowerOfTen)
        steps {
            whilePossible {
                deeply(simplifyDecimalsInProduct)
            }
        }
    }
}
