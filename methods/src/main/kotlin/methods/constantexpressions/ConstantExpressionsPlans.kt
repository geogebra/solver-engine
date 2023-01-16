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
import methods.general.GeneralPlans
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.NormalizationRules
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
                        option { deeply(GeneralRules.EvaluateZeroToAPositivePower) }

                        // minus one and other negative integer powers
                        option { deeply(simplifyIntegerToNegativePower) }
                        option { deeply(FractionArithmeticRules.SimplifyFractionToMinusOne) }
                        option { deeply(FractionArithmeticRules.SimplifyFractionNegativePower) }

                        option { deeply(FractionArithmeticPlans.SplitRationalExponent) }
                        option { deeply(FractionArithmeticRules.DistributeFractionPositivePower) }
                        option { deeply(FractionArithmeticRules.DistributeFractionPositiveFractionPower) }
                        option { deeply(IntegerArithmeticRules.SimplifyEvenPowerOfNegative) }
                        option { deeply(IntegerArithmeticRules.SimplifyOddPowerOfNegative) }
                        option { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
                        option { deeply(GeneralRules.DistributePowerOfProduct) }

                        // the "expand" plans should eventually be moved to a
                        // new public plan for "expanding" constant expression
                        option {
                            deeply {
                                applyTo(GeneralPlans.ExpandBinomialSquared) { if (it.isConstant()) it else null }
                            }
                        }
                        option {
                            deeply {
                                applyTo(GeneralPlans.ExpandBinomialCubed) { if (it.isConstant()) it else null }
                            }
                        }
                        option {
                            deeply {
                                applyTo(GeneralPlans.ExpandTrinomialSquared) { if (it.isConstant()) it else null }
                            }
                        }
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
                optionally(RewriteIntegerOrderRootsAsPowers)
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible { deeply(SimplifyConstantSubexpression, deepFirst = true) }
                whilePossible(simplificationSteps)
            }
        }
    )
}

val simpleTidyUpSteps = steps {
    deeply {
        firstOf {
            // simplify to undefined
            option(GeneralRules.SimplifyZeroDenominatorFractionToUndefined)
            option(GeneralRules.EvaluateZeroToThePowerOfZero)
            option(IntegerRationalExponentsRules.EvaluateNegativeToRationalExponentAsUndefined)

            // handle zeroes
            option(GeneralRules.EvaluateProductContainingZero)
            option(GeneralRules.EliminateZeroInSum)
            option(GeneralRules.SimplifyZeroNumeratorFractionToZero)
            option(GeneralRules.EvaluateZeroToAPositivePower)
            option(GeneralRules.EvaluateExpressionToThePowerOfZero)
            option(IntegerRootsRules.SimplifyRootOfZero)

            // handle ones
            option(GeneralRules.SimplifyFractionWithOneDenominator)
            option(GeneralRules.EvaluateOneToAnyPower)
            option(GeneralRules.SimplifyExpressionToThePowerOfOne)
            option(IntegerRootsRules.SimplifyRootOfOne)

            // miscellaneous
            option(MixedNumbersRules.SplitMixedNumber)
            option(GeneralRules.CancelAdditiveInverseElements)
        }
    }
}

val trickySimplificationSteps = steps {
    deeply {
        firstOf {
            option(cancelRootOfPower)
            option(IntegerRootsPlans.SplitRootsAndCancelRootsOfPowers)
            option(IntegerRootsPlans.SimplifyPowerOfIntegerUnderRoot)
            option(FractionArithmeticPlans.EvaluateFractionSum)
        }
    }
}

val fractionSimplificationSteps = steps {
    firstOf {
        option(FractionArithmeticPlans.NormalizeFractions)
        option(FractionArithmeticPlans.NormalizeSignsInFraction)
        option {
            deeply {
                firstOf {
                    option(FractionArithmeticPlans.SimplifyFraction)
                    option(DecimalPlans.NormalizeFractionOfDecimals)
                    option(DecimalPlans.ConvertTerminatingDecimalToFractionAndSimplify)
                    option(DecimalPlans.ConvertRecurringDecimalToFractionAndSimplify)
                }
            }
        }
    }
}

val simplificationSteps = steps {
    firstOf {
        option(simpleTidyUpSteps)

        option { deeply(removeRedundantBrackets) }

        option(trickySimplificationSteps)

        option(fractionSimplificationSteps)

        option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase) }
        option { deeply(ConstantExpressionsPlans.SimplifyPowers) }

        option { deeply(IntegerRootsPlans.CollectLikeRootsAndSimplify) }
        option { deeply(IntegerRationalExponentsPlans.CollectLikeRationalPowersAndSimplify) }

        option(ConstantExpressionsPlans.SimplifyRootsInExpression)
        option(simplifyRationalExponentsInProduct)

        option { deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions) }
        option { deeply(IntegerRootsPlans.SimplifyProductWithRoots) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInSum) }
        option { deeply(FractionArithmeticPlans.EvaluateSumOfFractionAndInteger) }

        option { deeply(FractionRootsPlans.RationalizeDenominators) }

        // the plan should eventually be moved to "expanding" constant expression
        option {
            deeply {
                applyTo(GeneralRules.ExpandProductOfSumAndDifference) { if (it.isConstant()) it else null }
            }
        }

        option {
            deeply(GeneralRules.DistributeNegativeOverBracket)
        }

        // this rule eventually needs to be moved to "expand constant expression"
        option {
            deeply {
                // We don't want to expand something like x(1 + sqrt[3]) because the algebra rules would factorise
                // it back again! so restrict expansion to constant expressions. It might be better to have a finer
                // condition depending on the factors that could be expanded.
                applyTo(GeneralRules.DistributeMultiplicationOverSum) { if (it.isConstant()) it else null }
            }
        }
        option { deeply(NormalizationRules.NormaliseSimplifiedProduct) }
    }
}
