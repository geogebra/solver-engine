package methods.constantexpressions

import engine.expressions.Expression
import engine.expressions.VariableOperator
import engine.methods.plan
import engine.methods.steps
import engine.patterns.AnyPattern
import engine.patterns.bracketOf
import engine.patterns.condition
import engine.patterns.powerOf
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
import methods.fractionroots.simplifyFractionOfRoots
import methods.general.addClarifyingBrackets
import methods.general.distributePowerOfProduct
import methods.general.evaluateProductContainingZero
import methods.general.expandBinomialSquared
import methods.general.removeBracketsProduct
import methods.general.removeBracketsSum
import methods.general.removeOuterBracket
import methods.general.removeRedundantBrackets
import methods.general.rewriteDivisionsAsFractions
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.integerarithmetic.simplifyEvenPowerOfNegative
import methods.integerarithmetic.simplifyIntegersInProduct
import methods.integerarithmetic.simplifyIntegersInSum
import methods.integerarithmetic.simplifyOddPowerOfNegative
import methods.integerroots.cancelPowerOfARoot
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

val simplifyRoots = plan {
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
        }
    }
}

val simplificationSteps = steps {
    firstOf {
        option { deeply(evaluateProductContainingZero) }

        option { deeply(removeRedundantBrackets, deepFirst = true) }
        option { deeply(removeBracketsProduct, deepFirst = true) }
        option { deeply(removeBracketsSum, deepFirst = true) }

        option { deeply(simplifyPowers, deepFirst = true) }
        option { deeply(simplifyRoots, deepFirst = true) }

        option(normalizeFractions)
        option(normalizeSignsInFraction)
        option { deeply(simplifyFraction, deepFirst = true) }
        option { deeply(multiplyAndSimplifyFractions, deepFirst = true) }
        option { deeply(simplifyProductWithRoots, deepFirst = true) }

        option { deeply(splitMixedNumber, deepFirst = true) }
        option { deeply(simplifyIntegersInProduct, deepFirst = true) }

        option { deeply(simplifyIntegersInSum, deepFirst = true) }
        option { deeply(evaluateFractionSum, deepFirst = true) }
        option { deeply(evaluateSumOfFractionAndInteger, deepFirst = true) }
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

    whilePossible {
        firstOf {
            option(addClarifyingBrackets)
            option(removeOuterBracket)
            option(rewriteDivisionsAsFractions)

            option {
                deeply(simplifyConstantSubexpression, deepFirst = true)
            }

            option {
                plan {
                    whilePossible(simplificationSteps)
                }
            }
        }
    }
}
