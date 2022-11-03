package methods.decimals

import engine.context.ResourceData
import engine.expressionmakers.move
import engine.expressions.Expression
import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.operators.BinaryExpressionOperator
import engine.operators.BracketOperator
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.fractionarithmetic.normalizeSignsInFraction
import methods.fractionarithmetic.simplifyFraction
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.general.addClarifyingBrackets
import methods.general.removeRedundantBrackets
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.arithmeticOperators

val evaluateSumOfDecimals = plan {
    pattern = sumContaining()
    explanation(Explanation.EvaluateSumOfDecimals, move(pattern))

    steps {
        whilePossible(DecimalRules.EvaluateSignedDecimalAddition)
    }
}

val evaluateProductOfDecimals = plan {
    pattern = productContaining()
    explanation(Explanation.EvaluateProductOfDecimals, move(pattern))

    steps {
        whilePossible(DecimalRules.EvaluateDecimalProductAndDivision)
    }
}

val evaluateDecimalPower = plan {
    val base = SignedNumberPattern()
    val exponent = UnsignedIntegerPattern()
    pattern = powerOf(base, exponent)
    explanation(Explanation.EvaluateDecimalPower, move(base), move(exponent))

    steps {
        firstOf {
            option(GeneralRules.EvaluateZeroToThePowerOfZero)
            // at this point the base is guaranteed to be nonzero
            option(GeneralRules.EvaluateExpressionToThePowerOfZero)
            option(GeneralRules.SimplifyExpressionToThePowerOfOne)
            // at this point the exponent is guaranteed to be > 0
            option(GeneralRules.EvaluateZeroToAPositivePower)
            option {
                apply(GeneralRules.RewritePowerAsProduct)
                apply(evaluateProductOfDecimals)
            }
            option(DecimalRules.EvaluateDecimalPowerDirectly)
        }
    }
}

val convertTerminatingDecimalToFractionAndSimplify = plan {
    explanation(Explanation.ConvertTerminatingDecimalToFractionAndSimplify)

    steps {
        apply(DecimalRules.ConvertTerminatingDecimalToFraction)
        optionally(simplifyFraction)
    }
}

val convertRecurringDecimalToFractionAndSimplify = plan {
    explanation(Explanation.ConvertRecurringDecimalToFractionAndSimplify)

    steps(ResourceData(curriculum = "EU")) {
        apply(DecimalRules.ConvertRecurringDecimalToFractionDirectly)
        deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
        optionally(simplifyFraction)
    }

    alternative(ResourceData(curriculum = "US")) {
        apply(DecimalRules.ConvertRecurringDecimalToEquation)
        apply(DecimalRules.MakeEquationSystemForRecurringDecimal)
        apply(DecimalRules.SimplifyEquationSystemForRecurringDecimal)
        apply(DecimalRules.SolveLinearEquation)
        optionally(simplifyFraction)
    }
}

val simplifyDecimalsInProduct = plan {
    pattern = productContaining()
    explanation(Explanation.SimplifyDecimalsInProduct, move(pattern))

    steps {
        whilePossible {
            firstOf {
                option(GeneralRules.EvaluateProductContainingZero)
                option(DecimalRules.EvaluateDecimalProductAndDivision)
                option(GeneralRules.EliminateOneInProduct)
            }
        }
    }
}

val normalizeFractionOfDecimals = plan {
    explanation(Explanation.NormalizeFractionOfDecimals)

    steps {
        apply(DecimalRules.MultiplyFractionOfDecimalsByPowerOfTen)
        whilePossible {
            deeply(simplifyDecimalsInProduct)
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

    steps {
        optionally(DecimalRules.ExpandFractionToPowerOfTenDenominator)
        optionally {
            applyTo(DecimalRules.EvaluateDecimalProductAndDivision) { it.numerator() }
        }
        optionally {
            applyTo(DecimalRules.EvaluateDecimalProductAndDivision) { it.denominator() }
        }
        apply(DecimalRules.ConvertFractionWithPowerOfTenDenominatorToDecimal)
    }
}

private val evaluationSteps = steps {
    whilePossible {
        firstOf {
            option { deeply(GeneralRules.EvaluateProductDividedByZeroAsUndefined, deepFirst = true) }
            option { deeply(GeneralRules.SimplifyZeroDenominatorFractionToUndefined, deepFirst = true) }
            option { deeply(removeRedundantBrackets, deepFirst = true) }

            option(normalizeSignsInFraction)

            option { deeply(normalizeFractionOfDecimals, deepFirst = true) }
            option { deeply(simplifyFraction, deepFirst = true) }
            option { deeply(GeneralRules.SimplifyDoubleMinus, deepFirst = true) }
            option { deeply(convertNiceFractionToDecimal, deepFirst = true) }
            option { deeply(evaluateDecimalPower, deepFirst = true) }
            option { deeply(evaluateProductOfDecimals, deepFirst = true) }
            option { deeply(evaluateSumOfDecimals, deepFirst = true) }
            option { deeply(multiplyAndSimplifyFractions, deepFirst = true) }
            option { deeply(DecimalRules.TurnDivisionOfDecimalsIntoFraction, deepFirst = true) }
        }
    }
}

val evaluateSubexpressionAsDecimal = plan {
    explanation(Explanation.EvaluateExpressionInBracketsAsDecimal)
    pattern = condition(AnyPattern()) { it.hasBracket() }

    steps {
        whilePossible(evaluationSteps)
    }
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

    explanation(Explanation.EvaluateExpressionAsDecimal)

    steps {
        whilePossible {
            firstOf {
                option(addClarifyingBrackets)
                option(NormalizationRules.RemoveOuterBracket)

                option {
                    deeply(evaluateSubexpressionAsDecimal, deepFirst = true)
                }

                option {
                    whilePossible(evaluationSteps)
                }
            }
        }
    }
}
