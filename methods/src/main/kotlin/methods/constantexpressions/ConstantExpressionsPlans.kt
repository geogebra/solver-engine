package methods.constantexpressions

import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
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
import methods.collecting.createCollectLikeRationalPowersAndSimplifyPlan
import methods.collecting.createCollectLikeRootsAndSimplifyPlan
import methods.decimals.DecimalPlans
import methods.decimals.DecimalRules
import methods.expand.ExpandRules
import methods.expand.createExpandAndSimplifySteps
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.FractionArithmeticRules
import methods.fractionarithmetic.createAddFractionsPlan
import methods.fractionarithmetic.createAddIntegerAndFractionPlan
import methods.fractionarithmetic.createAddRootAndFractionPlan
import methods.fractionarithmetic.normalizeFractionsWithinFractions
import methods.fractionarithmetic.normalizeNegativeSignsInFraction
import methods.fractionarithmetic.simplifyIntegerToNegativePower
import methods.fractionroots.FractionRootsPlans
import methods.fractionroots.FractionRootsRules
import methods.general.GeneralPlans
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.createEvaluateAbsoluteValuePlan
import methods.general.inlineSumsAndProducts
import methods.general.reorderProductSteps
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

enum class ConstantExpressionsPlans(override val runner: CompositeMethod) : RunnerMethod {
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
                    }
                }
            }
        },
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
                        option { deeply(GeneralRules.RewriteOddRootOfNegative) }
                    }
                }
            }
        },
    ),

    SimplifyConstantSubexpression(
        plan {
            explanation = Explanation.SimplifyExpressionInBrackets
            pattern = condition(AnyPattern()) { it.hasBracket() }

            steps {
                whilePossible(constantSimplificationSteps)
            }
        },
    ),

    RewriteIntegerOrderRootsAsPowers(
        plan {
            val exponent = IntegerFractionPattern()
            pattern = FindPattern(
                powerOf(
                    AnyPattern(),
                    ConditionPattern(
                        optionalNegOf(exponent),
                        integerCondition(exponent.numerator, exponent.denominator) { n, d -> !d.divides(n) },
                    ),
                ),
            )
            explanation = Explanation.RewriteIntegerOrderRootsAsPowers
            steps {
                whilePossible {
                    deeply(GeneralRules.RewriteIntegerOrderRootAsPower)
                }
            }
        },
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
                IntegerArithmeticPlans.EvaluateArithmeticExpression,
            )

            steps {
                firstOf {
                    option {
                        optionally(NormalizationPlans.NormalizeExpression)
                        whilePossible { deeply(simpleTidyUpSteps) }
                        optionally(RewriteIntegerOrderRootsAsPowers)
                        whilePossible { deeply(SimplifyConstantSubexpression, deepFirst = true) }
                        whilePossible(constantSimplificationSteps)
                    }
                    shortOption {
                        // to avoid rewriting things like 0 * (x)
                        whilePossible { deeply(simpleTidyUpSteps) }
                    }
                }
            }
        },
    ),
}

val simpleTidyUpSteps = steps {
    deeply {
        firstOf {
            // simplify to undefined
            option(GeneralRules.SimplifyZeroDenominatorFractionToUndefined)
            option(GeneralRules.EvaluateZeroToThePowerOfZero)
            option(IntegerRationalExponentsRules.EvaluateNegativeToRationalExponentAsUndefined)

            // tidy up decimals
            option(DecimalRules.StripTrailingZerosAfterDecimal)

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
            option(GeneralRules.SimplifyDoubleMinus)
            option(MixedNumbersRules.SplitMixedNumber)
            option(GeneralRules.CancelAdditiveInverseElements)
        }
    }
}

private val trickySimplificationSteps = steps {
    deeply {
        firstOf {
            option(IntegerArithmeticRules.SimplifyEvenPowerOfNegative)
            option(cancelRootOfPower)
            option(IntegerRootsPlans.SplitRootsAndCancelRootsOfPowers)
            option(IntegerRootsPlans.SimplifyPowerOfIntegerUnderRoot)
            option(addConstantFractions)
        }
    }
}

private val fractionSimplificationSteps = steps {
    deeply {
        firstOf {
            option(normalizeFractionsWithinFractions)
            option(normalizeNegativeSignsInFraction)
            option(FractionArithmeticPlans.SimplifyFraction)
            option(DecimalPlans.NormalizeFractionOfDecimals)
            option(DecimalPlans.ConvertTerminatingDecimalToFractionAndSimplify)
            option(DecimalPlans.ConvertRecurringDecimalToFractionAndSimplify)
        }
    }
}

val constantSimplificationSteps: StepsProducer = steps {
    firstOf {
        option(simpleTidyUpSteps)

        option { deeply(inlineSumsAndProducts) }

        option(trickySimplificationSteps)

        option(FractionArithmeticPlans.RewriteDivisionsAsFractions)

        option { deeply(evaluateConstantAbsoluteValue) }

        option(fractionSimplificationSteps)

        option { deeply(GeneralPlans.NormalizeNegativeSignsInProduct) }

        option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase) }
        option { deeply(ConstantExpressionsPlans.SimplifyPowers) }

        option { deeply(collectLikeRootsAndSimplify) }
        option { deeply(collectLikeRationalPowersAndSimplify) }

        option(ConstantExpressionsPlans.SimplifyRootsInExpression)
        option(simplifyRationalExponentsInProduct)

        option { deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions) }
        option { deeply(IntegerRootsPlans.SimplifyProductWithRoots) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInSum) }
        option { deeply(addIntegerAndFraction) }
        option { deeply(addRootAndFraction) }

        option { deeply(FractionRootsPlans.RationalizeDenominators) }

        option { deeply(ExpandRules.DistributeNegativeOverBracket) }
        option { deeply(expandConstantExpression) }

        option { deeply(reorderProductSteps) }
    }
}

private val evaluateConstantAbsoluteValue =
    createEvaluateAbsoluteValuePlan(constantSimplificationSteps)

private val collectLikeRootsAndSimplify =
    createCollectLikeRootsAndSimplifyPlan(constantSimplificationSteps)
private val collectLikeRationalPowersAndSimplify =
    createCollectLikeRationalPowersAndSimplifyPlan(constantSimplificationSteps)

private val expandConstantExpression = run {
    val constantExpansionSteps = createExpandAndSimplifySteps(ConstantExpressionsPlans.SimplifyConstantExpression)

    steps {
        applyTo(constantExpansionSteps) { if (it.isConstant()) it else null }
    }
}

private val addConstantFractions = run {
    val fractionAdditionSteps = createAddFractionsPlan(steps { whilePossible(constantSimplificationSteps) })

    steps {
        applyTo(fractionAdditionSteps) { if (it.isConstant()) it else null }
    }
}

private val addIntegerAndFraction =
    createAddIntegerAndFractionPlan(steps { whilePossible(constantSimplificationSteps) })

private val addRootAndFraction =
    createAddRootAndFractionPlan(steps { whilePossible(constantSimplificationSteps) })
