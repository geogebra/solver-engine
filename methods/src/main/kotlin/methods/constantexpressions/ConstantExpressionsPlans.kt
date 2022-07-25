package methods.constantexpressions

import engine.expressions.Expression
import engine.expressions.VariableOperator
import engine.methods.plan
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.bracketOf
import engine.patterns.condition
import methods.fractionarithmetic.evaluateFractionPower
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.evaluateIntegerToNegativePower
import methods.fractionarithmetic.evaluateSumOfFractionAndInteger
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.fractionarithmetic.normalizeFractionsAndDivisions
import methods.fractionarithmetic.normalizeSignsInFraction
import methods.fractionarithmetic.simplifyFraction
import methods.fractionarithmetic.simplifyFractionToMinusOne
import methods.fractionarithmetic.turnDivisionToFraction
import methods.general.addClarifyingBrackets
import methods.general.evaluateProductContainingZero
import methods.general.removeOuterBracket
import methods.general.removeRedundantBrackets
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.integerarithmetic.simplifyIntegersInProduct
import methods.integerarithmetic.simplifyIntegersInSum
import methods.integerroots.simplifyIntegerRoot
import methods.mixednumbers.splitMixedNumber

val simplifyConstantSubexpression = plan {
    whilePossible {
        firstOf {
            option { deeply(evaluateProductContainingZero) }

            option { deeply(removeRedundantBrackets, deepFirst = true) }

            option { deeply(simplifyIntegerRoot, deepFirst = true) }

            option(normalizeSignsInFraction)
            option { deeply(evaluateSignedIntegerPower, deepFirst = true) }
            option { deeply(simplifyFractionToMinusOne, deepFirst = true) }
            option { deeply(evaluateIntegerToNegativePower, deepFirst = true) }
            option { deeply(evaluateFractionPower, deepFirst = true) }

            option(normalizeFractionsAndDivisions)
            option { deeply(simplifyFraction, deepFirst = true) }
            option { deeply(multiplyAndSimplifyFractions, deepFirst = true) }

            option { deeply(splitMixedNumber, deepFirst = true) }
            option { deeply(simplifyIntegersInProduct, deepFirst = true) }
            option { deeply(turnDivisionToFraction, deepFirst = true) }

            option { deeply(simplifyIntegersInSum, deepFirst = true) }
            option { deeply(evaluateFractionSum, deepFirst = true) }
            option { deeply(evaluateSumOfFractionAndInteger, deepFirst = true) }
        }
    }
}

private fun Expression.isConstantExpression(): Boolean {
    for (operand in operands) {
        if (!operand.isConstantExpression()) return false
    }

    return operator !is VariableOperator
}

val simplifyConstantExpression = plan {
    val inner = AnyPattern()
    pattern = ConditionPattern(inner, condition(inner) { it.isConstantExpression() })

    whilePossible {
        firstOf {
            option(addClarifyingBrackets)
            option(removeOuterBracket)

            option {
                explanation(Explanation.SimplifyExpressionInBrackets)

                deeply(deepFirst = true) {
                    pattern = bracketOf(AnyPattern())
                    applyTo(simplifyConstantSubexpression) { it.nthChild(0) }
                }
            }

            option(simplifyConstantSubexpression)
        }
    }
}
