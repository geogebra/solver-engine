package methods.constantexpressions

import engine.expressions.Expression
import engine.methods.plan
import engine.methods.steps
import engine.operators.VariableOperator
import engine.patterns.AnyPattern
import engine.patterns.bracketOf
import engine.patterns.condition
import engine.patterns.powerOf
import methods.decimals.convertRecurringDecimalToFractionAndSimplify
import methods.decimals.convertTerminatingDecimalToFractionAndSimplify
import methods.decimals.normalizeFractionOfDecimals
import methods.fractionarithmetic.addLikeFractions
import methods.fractionarithmetic.distributeFractionPositivePower
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.evaluateIntegerToNegativePower
import methods.fractionarithmetic.evaluateSumOfFractionAndInteger
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.fractionarithmetic.normalizeFractions
import methods.fractionarithmetic.normalizeSignsInFraction
import methods.fractionarithmetic.simplifyFraction
import methods.fractionarithmetic.simplifyFractionNegativePower
import methods.fractionarithmetic.simplifyFractionToMinusOne
import methods.fractionroots.distributeRadicalOverFraction
import methods.fractionroots.rationalizeDenominators
import methods.fractionroots.simplifyFractionOfRoots
import methods.general.distributeMultiplicationOverSum
import methods.general.distributePowerOfProduct
import methods.general.evaluateProductContainingZero
import methods.general.evaluateZeroDividedByAnyValue
import methods.general.expandBinomialSquared
import methods.general.normalizeExpression
import methods.general.removeRedundantBrackets
import methods.general.simplifyZeroDenominatorFractionToUndefined
import methods.general.simplifyZeroNumeratorFractionToZero
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.integerarithmetic.simplifyEvenPowerOfNegative
import methods.integerarithmetic.simplifyIntegersInProduct
import methods.integerarithmetic.simplifyIntegersInSum
import methods.integerarithmetic.simplifyOddPowerOfNegative
import methods.integerroots.cancelPowerOfARoot
import methods.integerroots.collectLikeRootsAndSimplify
import methods.integerroots.simplifyIntegerRoot
import methods.integerroots.simplifyIntegerRootToInteger
import methods.integerroots.simplifyProductWithRoots
import methods.integerroots.simplifyRootOfOne
import methods.integerroots.simplifyRootOfRootWithCoefficient
import methods.integerroots.simplifyRootOfZero
import methods.integerroots.turnPowerOfRootToRootOfPower
import methods.mixednumbers.splitMixedNumber

val simplifyPowers = plan {
    pattern = powerOf(AnyPattern(), AnyPattern())
    explanation(Explanation.SimplifyPowers)

    whilePossible {
        firstOf {
            option { deeply(simplifyEvenPowerOfNegative) }
            option { deeply(simplifyOddPowerOfNegative) }
            option { deeply(simplifyFractionToMinusOne) }
            option { deeply(evaluateIntegerToNegativePower) }
            option { deeply(simplifyFractionNegativePower) }
            option { deeply(distributeFractionPositivePower) }
            option { deeply(distributePowerOfProduct) }
            option { deeply(expandBinomialSquared) }
            option { deeply(evaluateSignedIntegerPower) }
        }
    }
}

val simplifyRootsInExpression = plan {
    explanation(Explanation.SimplifyRootsInExpression)
    whilePossible {
        firstOf {
            option { deeply(simplifyRootOfZero, deepFirst = true) }
            option { deeply(simplifyRootOfOne, deepFirst = true) }
            option { deeply(simplifyIntegerRootToInteger, deepFirst = true) }
            option { deeply(cancelPowerOfARoot, deepFirst = true) }
            option { deeply(simplifyRootOfRootWithCoefficient, deepFirst = true) }
            option { deeply(simplifyIntegerRoot, deepFirst = true) }
            option { deeply(turnPowerOfRootToRootOfPower, deepFirst = true) }
            option { deeply(simplifyFractionOfRoots, deepFirst = true) }
            option { deeply(distributeRadicalOverFraction, deepFirst = true) }
        }
    }
}

val simplificationSteps = steps {
    firstOf {
        option { deeply(simplifyZeroDenominatorFractionToUndefined) }

        option { deeply(evaluateProductContainingZero) }

        option { deeply(removeRedundantBrackets, deepFirst = true) }

        option { deeply(simplifyPowers, deepFirst = true) }
        option(simplifyRootsInExpression)

        option(normalizeFractions)
        option(normalizeSignsInFraction)

        option { deeply(addLikeFractions, deepFirst = true) }
        option { deeply(simplifyFraction, deepFirst = true) }
        option { deeply(normalizeFractionOfDecimals, deepFirst = true) }
        option { deeply(convertTerminatingDecimalToFractionAndSimplify, deepFirst = true) }
        option { deeply(convertRecurringDecimalToFractionAndSimplify, deepFirst = true) }

        option { deeply(multiplyAndSimplifyFractions, deepFirst = true) }
        option { deeply(simplifyProductWithRoots, deepFirst = true) }

        option { deeply(splitMixedNumber, deepFirst = true) }
        option { deeply(simplifyIntegersInProduct, deepFirst = true) }

        option { deeply(simplifyIntegersInSum, deepFirst = true) }
        option { deeply(evaluateFractionSum, deepFirst = true) }
        option { deeply(evaluateSumOfFractionAndInteger, deepFirst = true) }

        option { deeply(rationalizeDenominators, deepFirst = true) }
        option { deeply(collectLikeRootsAndSimplify, deepFirst = true) }
        option { deeply(distributeMultiplicationOverSum, deepFirst = true) }
    }
}

val simplifyConstantSubexpression = plan {
    explanation(Explanation.SimplifyExpressionInBrackets)
    pattern = bracketOf(AnyPattern())

    whilePossible(simplificationSteps)
}

private fun Expression.isConstantExpression(): Boolean {
    for (operand in operands) {
        if (!operand.isConstantExpression()) return false
    }

    return operator !is VariableOperator
}

val simplifyConstantExpression = plan {
    pattern = condition(AnyPattern()) { it.isConstantExpression() }
    explanation(Explanation.SimplifyConstantExpression)

    pipeline {
        optionalSteps {
            whilePossible {
                deeply(simplifyZeroDenominatorFractionToUndefined)
            }
        }

        optionalSteps {
            whilePossible {
                deeply(simplifyZeroNumeratorFractionToZero)
            }
        }

        optionalSteps {
            whilePossible {
                deeply(evaluateZeroDividedByAnyValue)
            }
        }

        // even before normalization, clean up products containing zero
        optionalSteps {
            whilePossible {
                deeply(evaluateProductContainingZero)
            }
        }

        optionalSteps(normalizeExpression)

        optionalSteps {
            whilePossible {
                deeply(simplifyConstantSubexpression, deepFirst = true)
            }
        }

        optionalSteps {
            whilePossible(simplificationSteps)
        }
    }
}
