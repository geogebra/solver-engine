package methods.integerarithmetic

import engine.expressions.Expression
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.operators.BinaryExpressionOperator
import engine.operators.IntegerOperator
import engine.operators.NaryOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.condition
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.inlineSumsAndProducts

private enum class PrivateIntegerArithmeticPlans(override val runner: CompositeMethod) : RunnerMethod {
    EvaluateProductOfIntegers(
        plan {
            pattern = productContaining()
            explanation = Explanation.EvaluateProductOfIntegers
            explanationParameters(pattern)

            steps {
                whilePossible {
                    firstOf {
                        option(GeneralRules.EvaluateProductDividedByZeroAsUndefined)
                        option(IntegerArithmeticRules.EvaluateIntegerProductAndDivision)
                    }
                }
            }
        },
    ),

    EvaluateSumOfIntegers(
        plan {
            pattern = sumContaining()
            explanation = Explanation.EvaluateSumOfIntegers
            explanationParameters(pattern)

            steps {
                whilePossible(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
            }
        },
    ),

    /**
     * Evaluates [2 ^ 3] as 2 * 2 * 2 -> 4 * 2 -> 8
     * but evaluates [2 ^ 6] directly to 64
     */
    EvaluateSignedIntegerPower(
        plan {
            val base = SignedIntegerPattern()
            val exponent = SignedIntegerPattern()
            pattern = powerOf(base, exponent)
            explanation = Explanation.EvaluateIntegerPower
            explanationParameters(base, exponent)

            steps {
                firstOf {
                    option(GeneralRules.EvaluateZeroToThePowerOfZero)
                    option(GeneralRules.EvaluateZeroToAPositivePower)
                    option {
                        apply(GeneralRules.RewritePowerAsProduct)
                        apply(EvaluateProductOfIntegers)
                    }
                    option {
                        optionally(IntegerArithmeticRules.SimplifyEvenPowerOfNegative)
                        optionally(IntegerArithmeticRules.SimplifyOddPowerOfNegative)
                        apply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly)
                    }
                }
            }
        },
    ),

    EvaluateArithmeticSubexpression(
        plan {
            explanation = Explanation.SimplifyExpressionInBrackets
            pattern = condition(AnyPattern()) { it.hasBracket() }

            steps {
                whilePossible(evaluationSteps)
            }
        },
    ),
}

enum class IntegerArithmeticPlans(override val runner: CompositeMethod) : RunnerMethod {
    SimplifyIntegersInSum(
        plan {
            pattern = sumContaining()
            explanation = Explanation.SimplifyIntegersInSum
            explanationParameters(pattern)

            steps {
                whilePossible {
                    firstOf {
                        option(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
                        option(GeneralRules.EliminateZeroInSum)
                    }
                }
            }
        },
    ),

    SimplifyIntegersInProduct(
        plan {
            pattern = productContaining()
            explanation = Explanation.SimplifyIntegersInProduct
            explanationParameters(pattern)

            steps {
                whilePossible {
                    firstOf {
                        option(GeneralRules.EvaluateProductDividedByZeroAsUndefined)
                        option(GeneralRules.EvaluateProductContainingZero)
                        option(IntegerArithmeticRules.EvaluateIntegerProductAndDivision)
                        option(GeneralRules.RemoveUnitaryCoefficient)
                        option(GeneralRules.EliminateOneInProduct)
                    }
                }
            }
        },
    ),

    /**
     * Evaluate an arithmetic expression (i.e. an expression containing only
     * integers and the operators +, -, *, :, ^ and brackets)
     */
    @PublicMethod
    EvaluateArithmeticExpression(
        plan {
            val expression = AnyPattern()
            pattern = condition(expression) { it.isArithmeticExpression() }
            explanation = Explanation.EvaluateArithmeticExpression
            resultPattern = SignedIntegerPattern()
            explanationParameters(expression)

            steps {
                whilePossible {
                    firstOf {
                        option(NormalizationPlans.NormalizeExpression)

                        option {
                            deeply(PrivateIntegerArithmeticPlans.EvaluateArithmeticSubexpression, deepFirst = true)
                        }

                        option {
                            whilePossible(evaluationSteps)
                        }
                    }
                }
            }
        },
    ),
}

val arithmeticOperators = listOf(
    UnaryExpressionOperator.Minus,
    UnaryExpressionOperator.Plus,
    UnaryExpressionOperator.DivideBy,
    BinaryExpressionOperator.Power,
    NaryOperator.Sum,
    NaryOperator.Product,
)

private fun Expression.isArithmeticExpression(): Boolean {
    val validOperator = operator is IntegerOperator || arithmeticOperators.contains(operator)

    return validOperator && children.all { it.isArithmeticExpression() }
}

private val evaluationSteps = steps {
    firstOf {
        option { deeply(inlineSumsAndProducts, deepFirst = true) }
        option { deeply(GeneralRules.SimplifyDoubleMinus, deepFirst = true) }
        option { deeply(PrivateIntegerArithmeticPlans.EvaluateSignedIntegerPower, deepFirst = true) }
        option { deeply(PrivateIntegerArithmeticPlans.EvaluateProductOfIntegers, deepFirst = true) }
        option { deeply(PrivateIntegerArithmeticPlans.EvaluateSumOfIntegers, deepFirst = true) }
    }
}

// Auxiliary steps used in several plans
val simplifyIntegersInExpression = steps {
    whilePossible {
        firstOf {
            option { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
            option { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }
            option { deeply(IntegerArithmeticPlans.SimplifyIntegersInSum) }
        }
    }
}
