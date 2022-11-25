package methods.constantexpressions

import engine.expressions.Expression
import engine.methods.Plan
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.operators.VariableOperator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FindPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.condition
import engine.patterns.integerCondition
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.utility.divides
import methods.decimals.DecimalPlans
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.FractionArithmeticRules
import methods.fractionarithmetic.simplifyIntegerToNegativePower
import methods.fractionroots.FractionRootsPlans
import methods.fractionroots.FractionRootsRules
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.removeRedundantBrackets
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerrationalexponents.IntegerRationalExponentsPlans
import methods.integerrationalexponents.IntegerRationalExponentsRules
import methods.integerrationalexponents.simplifyRationalExponentsInProduct
import methods.integerroots.IntegerRootsPlans
import methods.integerroots.IntegerRootsRules
import methods.mixednumbers.MixedNumbersPlans
import methods.mixednumbers.MixedNumbersRules

enum class ConstantExpressionsPlans(override val runner: Plan) : RunnerMethod {
    SimplifyPowers(
        plan {
            pattern = powerOf(AnyPattern(), AnyPattern())
            explanation = Explanation.SimplifyPowers

            steps {
                whilePossible {
                    firstOf {
                        // one as base or as exponent
                        option { deeply(GeneralRules.EvaluateOneToAnyPower) }
                        option { deeply(GeneralRules.SimplifyExpressionToThePowerOfOne) }

                        // zero as base or as exponent
                        option { deeply(GeneralRules.EvaluateZeroToThePowerOfZero) }
                        option { deeply(GeneralRules.EvaluateZeroToAPositivePower) }
                        option { deeply(GeneralRules.EvaluateExpressionToThePowerOfZero) }

                        // minus one and other negative integer powers
                        option { deeply(simplifyIntegerToNegativePower) }
                        option { deeply(FractionArithmeticRules.SimplifyFractionToMinusOne) }
                        option { deeply(FractionArithmeticRules.SimplifyFractionNegativePower) }

                        option { deeply(FractionArithmeticPlans.SplitRationalExponent) }
                        option { deeply(FractionArithmeticRules.DistributeFractionPositivePower) }
                        option { deeply(FractionArithmeticRules.DistributeFractionPositiveFractionPower) }
                        option { deeply(IntegerArithmeticRules.SimplifyEvenPowerOfNegative) }
                        option { deeply(IntegerArithmeticRules.SimplifyOddPowerOfNegative) }
                        option { deeply(GeneralRules.DistributePowerOfProduct) }
                        option { deeply(GeneralRules.ExpandBinomialSquared) }
                        option { deeply(IntegerArithmeticPlans.EvaluateSignedIntegerPower) }
                    }
                }
            }
        }
    ),
    SimplifyRootsInExpression(
        plan {
            explanation = Explanation.SimplifyRootsInExpression

            steps {
                whilePossible {
                    firstOf {
                        option { deeply(IntegerRootsRules.SimplifyRootOfZero, deepFirst = true) }
                        option { deeply(IntegerRootsRules.SimplifyRootOfOne, deepFirst = true) }
                        option { deeply(IntegerRootsPlans.SimplifyIntegerRootToInteger, deepFirst = true) }
                        option { deeply(IntegerRootsPlans.CancelPowerOfARoot, deepFirst = true) }
                        option { deeply(IntegerRootsPlans.SimplifyRootOfRootWithCoefficient, deepFirst = true) }
                        option { deeply(IntegerRootsPlans.SimplifyIntegerRoot, deepFirst = true) }
                        option { deeply(IntegerRootsRules.TurnPowerOfRootToRootOfPower, deepFirst = true) }
                        option { deeply(FractionRootsPlans.SimplifyFractionOfRoots, deepFirst = true) }
                        option { deeply(FractionRootsRules.DistributeRadicalOverFraction, deepFirst = true) }
                    }
                }
            }
        }
    ),
    SimplifyConstantSubexpression(
        plan {
            explanation = Explanation.SimplifyExpressionInBrackets
            pattern = condition(AnyPattern()) { it.hasBracket() }

            steps {
                whilePossible(simplificationSteps)
            }
        }
    ),

    RewriteIntegerOrderRootsAsPowers(
        plan {
            val exponent = IntegerFractionPattern()
            pattern = FindPattern(
                powerOf(
                    AnyPattern(),
                    ConditionPattern(
                        optionalNegOf(exponent),
                        integerCondition(exponent.numerator, exponent.denominator) { n, d -> !d.divides(n) }
                    )
                )
            )
            explanation = Explanation.RewriteIntegerOrderRootsAsPowers
            steps {
                whilePossible {
                    deeply(GeneralRules.RewriteIntegerOrderRootAsPower)
                }
            }
        }
    ),

    /**
     * Simplifies a constant expression, i.e. one containing no variables
     */
    @PublicMethod
    SimplifyConstantExpression(
        plan {
            pattern = condition(AnyPattern()) { it.isConstantExpression() }
            explanation = Explanation.SimplifyConstantExpression

            specificPlans(
                MixedNumbersPlans.AddMixedNumbers,
                IntegerArithmeticPlans.EvaluateArithmeticExpression
            )

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible { deeply(ConstantExpressionsPlans.SimplifyConstantSubexpression, deepFirst = true) }
                whilePossible(simplificationSteps)
            }
        }
    )
}

val simpleTidyUpSteps = steps {
    firstOf {
        option { deeply(MixedNumbersRules.SplitMixedNumber) }
        option { deeply(GeneralRules.SimplifyZeroDenominatorFractionToUndefined) }
        option { deeply(GeneralRules.SimplifyZeroNumeratorFractionToZero) }
        option { deeply(GeneralRules.SimplifyFractionWithOneDenominator) }
        option { deeply(IntegerRationalExponentsRules.EvaluateNegativeToRationalExponentAsUndefined) }
        option { deeply(GeneralRules.EvaluateProductContainingZero) }
        option { deeply(GeneralRules.CancelAdditiveInverseElements) }
        option { deeply(IntegerRootsRules.SimplifyRootOfOne) }
    }
}

val simplificationSteps = steps {
    firstOf {
        option(simpleTidyUpSteps)
        option(ConstantExpressionsPlans.RewriteIntegerOrderRootsAsPowers)

        option { deeply(removeRedundantBrackets, deepFirst = true) }

        option { deeply(IntegerRootsPlans.SimplifyPowerOfIntegerUnderRoot, deepFirst = true) }
        option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase) }
        option { deeply(ConstantExpressionsPlans.SimplifyPowers, deepFirst = true) }

        option(FractionArithmeticPlans.NormalizeFractions)
        option(FractionArithmeticPlans.NormalizeSignsInFraction)

        option { deeply(FractionArithmeticRules.AddLikeFractions, deepFirst = true) }
        option { deeply(FractionArithmeticPlans.SimplifyFraction, deepFirst = true) }
        option { deeply(DecimalPlans.NormalizeFractionOfDecimals, deepFirst = true) }
        option { deeply(DecimalPlans.ConvertTerminatingDecimalToFractionAndSimplify, deepFirst = true) }
        option { deeply(DecimalPlans.ConvertRecurringDecimalToFractionAndSimplify, deepFirst = true) }

        option { deeply(IntegerRootsPlans.CollectLikeRootsAndSimplify, deepFirst = true) }
        option { deeply(IntegerRationalExponentsPlans.CollectLikeRationalPowersAndSimplify, deepFirst = true) }

        option(ConstantExpressionsPlans.SimplifyRootsInExpression)
        option(simplifyRationalExponentsInProduct)

        option { deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions, deepFirst = true) }
        option { deeply(IntegerRootsPlans.SimplifyProductWithRoots, deepFirst = true) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct, deepFirst = true) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInSum, deepFirst = true) }
        option { deeply(FractionArithmeticPlans.EvaluateFractionSum, deepFirst = true) }
        option { deeply(FractionArithmeticPlans.EvaluateSumOfFractionAndInteger, deepFirst = true) }

        option { deeply(FractionRootsPlans.RationalizeDenominators, deepFirst = true) }
        option { deeply(GeneralRules.DistributeMultiplicationOverSum, deepFirst = true) }
    }
}

private fun Expression.isConstantExpression(): Boolean {
    for (operand in operands) {
        if (!operand.isConstantExpression()) return false
    }

    return operator !is VariableOperator
}
