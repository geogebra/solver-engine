package methods.constantexpressions

import engine.expressions.Fraction
import engine.expressions.Minus
import engine.expressions.Power
import engine.expressions.ValueExpression
import engine.expressions.containsPowers
import engine.expressions.containsRoots
import engine.expressions.isSignedFraction
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.applyAfterMaybeExtractingMinus
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FindPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.RationalPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.condition
import engine.patterns.integerCondition
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.utility.divides
import methods.collecting.createCollectLikeRationalPowersAndSimplifyPlan
import methods.collecting.createCollectLikeRootsAndSimplifyPlan
import methods.decimals.DecimalPlans
import methods.decimals.DecimalRules
import methods.expand.ExpandAndSimplifier
import methods.expand.ExpandRules
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.FractionArithmeticRules
import methods.fractionarithmetic.addIntegerAndFraction
import methods.fractionarithmetic.createAddFractionsPlan
import methods.fractionarithmetic.createAddRootAndFractionPlan
import methods.fractionarithmetic.normalizeFractionsWithinFractions
import methods.fractionarithmetic.normalizeNegativeSignsInFraction
import methods.fractionroots.FractionRootsPlans
import methods.fractionroots.FractionRootsRules
import methods.general.GeneralPlans
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.NormalizationRules
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

    SimplifyPowerOfInteger(
        plan {
            pattern = powerOf(SignedIntegerPattern(), AnyPattern())
            explanation = Explanation.SimplifyPowerOfInteger

            steps {
                optionally(GeneralRules.SimplifyEvenPowerOfNegative)
                optionally(GeneralRules.SimplifyOddPowerOfNegative)

                applyAfterMaybeExtractingMinus {
                    firstOf {
                        option(FractionArithmeticRules.TurnIntegerToMinusOneToFraction)

                        option {
                            apply(FractionArithmeticRules.TurnNegativePowerOfIntegerToFraction)
                            applyToKind<Fraction>(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) {
                                it.denominator
                            }
                        }

                        option {
                            // [0 ^ -n] -> [[1 / 0] ^ n]
                            apply(FractionArithmeticRules.TurnNegativePowerOfZeroToPowerOfFraction)
                            applyToKind<Power>(GeneralRules.SimplifyZeroDenominatorFractionToUndefined) {
                                it.base
                            }
                        }

                        option(IntegerArithmeticRules.EvaluateIntegerPowerDirectly)
                    }
                }
            }
        },
    ),

    SimplifyPowerOfProduct(
        plan {
            pattern = powerOf(optionalNegOf(productContaining()), AnyPattern())
            explanation = Explanation.SimplifyPowerOfProduct

            steps {
                optionally(GeneralRules.SimplifyOddPowerOfNegative)
                optionally(GeneralRules.SimplifyEvenPowerOfNegative)

                applyAfterMaybeExtractingMinus {
                    apply(GeneralRules.DistributePowerOfProduct)
                    whilePossible(constantSimplificationSteps)
                }
            }
        },
    ),

    SimplifyPowerOfFraction(
        plan {
            pattern = powerOf(condition { it.isSignedFraction() && it.isConstant() }, RationalPattern())
            explanation = Explanation.SimplifyPowerOfFraction

            steps {
                optionally(GeneralRules.SimplifyOddPowerOfNegative)
                optionally(GeneralRules.SimplifyEvenPowerOfNegative)

                applyAfterMaybeExtractingMinus {
                    shortcut(FractionArithmeticRules.SimplifyFractionToMinusOne)

                    optionally(GeneralRules.FlipFractionUnderNegativePower)
                    firstOf {
                        option {
                            apply(FractionArithmeticPlans.SplitRationalExponent)
                            applyToChildren(SimplifyPowerOfFraction)
                        }
                        option {
                            // separate rule, so it doesn't distribute exponents if neither the numerator
                            // nor the denominator can be further simplified, e.g. (2/3)^(2/5)
                            apply(FractionArithmeticRules.DistributeFractionalPowerOverFraction)
                            applyToChildren { whilePossible(constantSimplificationSteps) }
                        }
                        option {
                            apply(FractionArithmeticRules.DistributePositiveIntegerPowerOverFraction)
                            applyToChildren { whilePossible(constantSimplificationSteps) }
                        }
                    }
                }
            }
        },
    ),

    SimplifyRootsInExpression(
        plan {
            explanation = Explanation.SimplifyRootsInExpression
            pattern = condition { it.containsRoots() }

            steps {
                whilePossible {
                    firstOf {
                        option { deeply(GeneralRules.SimplifyEvenPowerOfNegative) }
                        option { deeply(GeneralRules.SimplifyOddPowerOfNegative) }
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
            pattern = condition { it.hasVisibleBracket() }

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
            pattern = condition { it.isConstant() && it is ValueExpression }
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
        option(GeneralRules.RemoveUnitaryCoefficient)
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

private val trickySimplificationSteps = steps {
    check { it.containsRoots() }
    deeply {
        firstOf {
            option(cancelRootOfPower)
            option(IntegerRootsPlans.SplitRootsAndCancelRootsOfPowers)
            option(IntegerRootsPlans.SimplifyPowerOfIntegerUnderRoot)
        }
    }
}

private val fractionSimplificationSteps = steps {
    deeply {
        firstOf {
            option(normalizeFractionsWithinFractions)
            option(normalizeNegativeSignsInFraction)
            option(FractionArithmeticPlans.SimplifyFraction)

            // These should be done once and for all at the start I guess, no need to try again and again
            option(DecimalPlans.NormalizeFractionOfDecimals)
            option(DecimalPlans.ConvertTerminatingDecimalToFractionAndSimplify)
            option(DecimalPlans.ConvertRecurringDecimalToFractionAndSimplify)
        }
    }
}

val constantSimplificationSteps: StepsProducer = steps {
    firstOf {
        option { deeply(simpleTidyUpSteps) }

        option { deeply(inlineSumsAndProducts) }

        option(trickySimplificationSteps)

        option { deeply(evaluateConstantAbsoluteValue) }

        option(FractionArithmeticPlans.RewriteDivisionsAsFractions)

        // We do this before simplifying fractions for a variety of reasons, a simple one being e.g.
        // 2/10 + 1/10 --> 3/10
        option { deeply(addConstantFractions) }

        option(fractionSimplificationSteps)

        option {
            deeply {
                // We reorganize the product before extracting the signs, but if we don't have signs
                // to handle, we leave the reorganization to the end
                optionally { applyTo(reorderProductSteps) { if (it is Minus) it.argument else it } }
                apply(GeneralPlans.NormalizeNegativeSignsInProduct)
            }
        }

        option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase) }
        option {
            check { it.containsPowers() }
            deeply(deepFirst = true) {
                firstOf {
                    option(ConstantExpressionsPlans.SimplifyPowerOfInteger)
                    option(ConstantExpressionsPlans.SimplifyPowerOfProduct)
                    option(ConstantExpressionsPlans.SimplifyPowerOfFraction)
                }
            }
        }

        option { deeply(collectLikeRootsAndSimplify) }
        option { deeply(collectLikeRationalPowersAndSimplify) }

        option(ConstantExpressionsPlans.SimplifyRootsInExpression)
        option(simplifyRationalExponentsInProduct)

        option { deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions) }
        option { deeply(IntegerRootsPlans.SimplifyProductWithRoots) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInSum) }
        option { deeply(addIntegerAndFraction) }

        option {
            check { it.containsRoots() }
            firstOf {
                // Do this after integers are added we don't apply it to e.g. `sqrt[4 + 8]`
                option { deeply(IntegerRootsPlans.SimplifySquareRootWithASquareFactorRadicand) }
                option { deeply(addRootAndFraction) }
                option { deeply(FractionRootsPlans.RationalizeDenominators) }
            }
        }

        option { deeply(ExpandRules.DistributeNegativeOverBracket) }
        option { deeply(expandConstantExpression) }

        option { deeply(reorderProductSteps) }
        option { deeply(NormalizationRules.NormalizeProductSigns) }
    }
}

private val evaluateConstantAbsoluteValue =
    createEvaluateAbsoluteValuePlan(constantSimplificationSteps)

private val collectLikeRootsAndSimplify =
    createCollectLikeRootsAndSimplifyPlan(constantSimplificationSteps)
private val collectLikeRationalPowersAndSimplify =
    createCollectLikeRationalPowersAndSimplifyPlan(constantSimplificationSteps)

private val expandAndSimplifier = ExpandAndSimplifier(ConstantExpressionsPlans.SimplifyConstantExpression)

private val expandConstantExpression = steps {
    check { it.isConstant() }
    apply(expandAndSimplifier.steps)
}

private val addConstantFractions = run {
    val fractionAdditionSteps = createAddFractionsPlan(steps { whilePossible(constantSimplificationSteps) })

    steps {
        check { it.isConstant() }
        apply(fractionAdditionSteps)
    }
}

private val addRootAndFraction =
    createAddRootAndFractionPlan(steps { whilePossible(constantSimplificationSteps) })
