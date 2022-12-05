package methods.constantexpressions

import engine.methods.Plan
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
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
import methods.integerroots.cancelRootOfPower
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
                        option { deeply(IntegerRootsPlans.SimplifyIntegerRootToInteger) }
                        option { deeply(IntegerRootsPlans.CancelPowerOfARoot) }
                        option { deeply(IntegerRootsPlans.SimplifyRootOfRootWithCoefficient) }
                        option { deeply(IntegerRootsPlans.SimplifyIntegerRoot) }
                        option { deeply(IntegerRootsRules.TurnPowerOfRootToRootOfPower) }
                        option { deeply(FractionRootsPlans.SimplifyFractionOfRoots) }
                        option { deeply(FractionRootsRules.DistributeRadicalOverFraction) }
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
            pattern = condition(AnyPattern()) { it.isConstant() }
            explanation = Explanation.SimplifyConstantExpression

            specificPlans(
                MixedNumbersPlans.AddMixedNumbers,
                IntegerArithmeticPlans.EvaluateArithmeticExpression
            )

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible { deeply(SimplifyConstantSubexpression, deepFirst = true) }
                whilePossible(simplificationSteps)
            }
        }
    )
}

val simpleTidyUpSteps = steps {
    firstOf {
        // simplify to undefined
        option { deeply(GeneralRules.SimplifyZeroDenominatorFractionToUndefined) }
        option { deeply(GeneralRules.EvaluateZeroToThePowerOfZero) }
        option { deeply(IntegerRationalExponentsRules.EvaluateNegativeToRationalExponentAsUndefined) }

        // handle zeroes
        option { deeply(GeneralRules.EvaluateProductContainingZero) }
        option { deeply(GeneralRules.EliminateZeroInSum) }
        option { deeply(GeneralRules.SimplifyZeroNumeratorFractionToZero) }
        option { deeply(GeneralRules.EvaluateZeroToAPositivePower) }
        option { deeply(GeneralRules.EvaluateExpressionToThePowerOfZero) }
        option { deeply(IntegerRootsRules.SimplifyRootOfZero) }

        // handle ones
        option { deeply(GeneralRules.SimplifyFractionWithOneDenominator) }
        option { deeply(GeneralRules.EvaluateOneToAnyPower) }
        option { deeply(GeneralRules.SimplifyExpressionToThePowerOfOne) }
        option { deeply(IntegerRootsRules.SimplifyRootOfOne) }

        // miscellaneous
        option { deeply(MixedNumbersRules.SplitMixedNumber) }
        option { deeply(GeneralRules.CancelAdditiveInverseElements) }
    }
}

val trickySimplificationSteps = steps {
    firstOf {
        option { deeply(cancelRootOfPower) }
        option { deeply(IntegerRootsPlans.SplitRootsAndCancelRootsOfPowers) }
        option { deeply(IntegerRootsPlans.SimplifyPowerOfIntegerUnderRoot) }
    }
}

val simplificationSteps = steps {
    firstOf {
        option(simpleTidyUpSteps)
        option(ConstantExpressionsPlans.RewriteIntegerOrderRootsAsPowers)

        option { deeply(removeRedundantBrackets) }

        option(trickySimplificationSteps)

        option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase) }
        option { deeply(ConstantExpressionsPlans.SimplifyPowers) }

        option(FractionArithmeticPlans.NormalizeFractions)
        option(FractionArithmeticPlans.NormalizeSignsInFraction)

        option { deeply(FractionArithmeticRules.AddLikeFractions) }
        option { deeply(FractionArithmeticPlans.SimplifyFraction) }
        option { deeply(DecimalPlans.NormalizeFractionOfDecimals) }
        option { deeply(DecimalPlans.ConvertTerminatingDecimalToFractionAndSimplify) }
        option { deeply(DecimalPlans.ConvertRecurringDecimalToFractionAndSimplify) }

        option { deeply(IntegerRootsPlans.CollectLikeRootsAndSimplify) }
        option { deeply(IntegerRationalExponentsPlans.CollectLikeRationalPowersAndSimplify) }

        option(ConstantExpressionsPlans.SimplifyRootsInExpression)
        option(simplifyRationalExponentsInProduct)

        option { deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions) }
        option { deeply(IntegerRootsPlans.SimplifyProductWithRoots) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInSum) }
        option { deeply(FractionArithmeticPlans.EvaluateFractionSum) }
        option { deeply(FractionArithmeticPlans.EvaluateSumOfFractionAndInteger) }

        option { deeply(FractionRootsPlans.RationalizeDenominators) }
        option {
            deeply {
                // We don't want to expand something like x(1 + sqrt[3]) because the algebra rules would factorise it
                // back again! so restrict expansion to constant expressions. It might be better to have a finer
                // condition depending on the factors that could be expanded.
                applyTo(GeneralRules.DistributeMultiplicationOverSum) { if (it.isConstant()) it else null }
            }
        }
    }
}
