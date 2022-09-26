package methods.decimals

import engine.context.ResourceData
import engine.expressionmakers.move
import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.plan
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.fractionOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.fractionarithmetic.simplifyFraction
import methods.general.eliminateOneInProduct
import methods.general.evaluateProductContainingZero
import methods.integerarithmetic.evaluateSignedIntegerAddition

val evaluateSumOfDecimals = plan {
    pattern = sumContaining()
    explanation(Explanation.EvaluateSumOfDecimals, move(pattern))
    whilePossible(evaluateSignedDecimalAddition)
}

val evaluateProductOfDecimals = plan {
    pattern = productContaining()
    explanation(Explanation.EvaluateProductOfDecimals, move(pattern))
    whilePossible(evaluateDecimalProductAndDivision)
}

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
    explanation(Explanation.SimplifyDecimalsInProduct, move(pattern))

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

/**
 * Convert a "nice" fraction to a decimal if possible.  A fraction is called "nice" if its denominator divides a power
 * of 10.
 */
val convertNiceFractionToDecimal = plan {
    explanation(Explanation.ConvertNiceFractionToDecimal)
    pattern = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
    pipeline {
        optionalSteps(expandFractionToPowerOfTenDenominator)
        optionalSteps {
            applyTo(evaluateDecimalProductAndDivision) { it.numerator() }
        }
        optionalSteps {
            applyTo(evaluateDecimalProductAndDivision) { it.denominator() }
        }
        steps(convertFractionWithPowerOfTenDenominatorToDecimal)
    }
}

// Simple version of evaluate expression as decimal to be able to test the convertNiceFractionToDecimal plan

val evaluateExpressionAsDecimal = plan {
    whilePossible {
        firstOf {
            option(evaluateSumOfDecimals)
            option(evaluateProductOfDecimals)
            option(convertNiceFractionToDecimal)
            option(simplifyFraction)
            option(multiplyAndSimplifyFractions)
        }
    }
}
