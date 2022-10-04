package methods.decimals

import engine.context.ResourceData
import engine.expressionmakers.move
import engine.expressions.Expression
import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.plan
import engine.methods.steps
import engine.operators.BinaryExpressionOperator
import engine.operators.BracketOperator
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.bracketOf
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.fractionarithmetic.normalizeSignsInFraction
import methods.fractionarithmetic.simplifyFraction
import methods.general.addClarifyingBrackets
import methods.general.eliminateOneInProduct
import methods.general.evaluateExpressionToThePowerOfZero
import methods.general.evaluateProductContainingZero
import methods.general.evaluateProductDividedByZeroAsUndefined
import methods.general.evaluateZeroToAPositivePower
import methods.general.evaluateZeroToThePowerOfZero
import methods.general.removeOuterBracket
import methods.general.removeRedundantBrackets
import methods.general.rewritePowerAsProduct
import methods.general.simplifyDoubleMinus
import methods.general.simplifyExpressionToThePowerOfOne
import methods.general.simplifyZeroDenominatorFractionToUndefined
import methods.integerarithmetic.arithmeticOperators
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

val evaluateDecimalPower = plan {
    val base = SignedNumberPattern()
    val exponent = UnsignedIntegerPattern()
    pattern = powerOf(base, exponent)
    explanation(Explanation.EvaluateDecimalPower, move(base), move(exponent))

    firstOf {
        option(evaluateZeroToThePowerOfZero)
        option(evaluateExpressionToThePowerOfZero) // at this point the base is guaranteed to be nonzero
        option(simplifyExpressionToThePowerOfOne)
        option(evaluateZeroToAPositivePower) // at this point the exponent is guaranteed to be > 0
        option {
            pipeline {
                steps(rewritePowerAsProduct)
                steps(evaluateProductOfDecimals)
            }
        }
        option(evaluateDecimalPowerDirectly)
    }
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

private val evaluationSteps = steps {
    whilePossible {
        firstOf {
            option { deeply(evaluateProductDividedByZeroAsUndefined, deepFirst = true) }
            option { deeply(simplifyZeroDenominatorFractionToUndefined, deepFirst = true) }
            option { deeply(removeRedundantBrackets, deepFirst = true) }

            option(normalizeSignsInFraction)

            option { deeply(normalizeFractionOfDecimals, deepFirst = true) }
            option { deeply(simplifyFraction, deepFirst = true) }
            option { deeply(simplifyDoubleMinus, deepFirst = true) }
            option { deeply(convertNiceFractionToDecimal, deepFirst = true) }
            option { deeply(evaluateDecimalPower, deepFirst = true) }
            option { deeply(evaluateProductOfDecimals, deepFirst = true) }
            option { deeply(evaluateSumOfDecimals, deepFirst = true) }
            option { deeply(multiplyAndSimplifyFractions, deepFirst = true) }
            option { deeply(turnDivisionOfDecimalsIntoFraction, deepFirst = true) }
        }
    }
}

val evaluateSubexpressionAsDecimal = plan {
    explanation(Explanation.EvaluateExpressionInBracketsAsDecimal)
    pattern = bracketOf(AnyPattern())
    whilePossible(evaluationSteps)
}

private fun Expression.isDecimalExpression(): Boolean {
    val validOperator = operator is IntegerOperator || operator is DecimalOperator || operator is BracketOperator ||
        arithmeticOperators.contains(operator) || operator == BinaryExpressionOperator.Fraction

    return validOperator && operands.all { it.isDecimalExpression() }
}

val evaluateExpressionAsDecimal = plan {
    val expression = AnyPattern()
    pattern = condition(expression) { it.isDecimalExpression() }
    resultPattern = SignedNumberPattern()

    explanation(Explanation.EvaluateExpressionAsDecimal, move(expression))

    whilePossible {
        firstOf {
            option(addClarifyingBrackets)
            option(removeOuterBracket)

            option {
                deeply(evaluateSubexpressionAsDecimal, deepFirst = true)
            }

            option {
                plan {
                    whilePossible(evaluationSteps)
                }
            }
        }
    }
}
