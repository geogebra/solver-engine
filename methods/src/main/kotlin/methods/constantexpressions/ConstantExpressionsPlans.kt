/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods.constantexpressions

import engine.expressions.Fraction
import engine.expressions.Minus
import engine.expressions.Power
import engine.expressions.ValueExpression
import engine.expressions.containsDecimals
import engine.expressions.containsFractions
import engine.expressions.containsLogs
import engine.expressions.containsPowers
import engine.expressions.containsRoots
import engine.expressions.isSigned
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.applyAfterMaybeExtractingMinus
import engine.methods.stepsproducers.steps
import engine.methods.stepsproducers.stepsWithMinDepth
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FindPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.RationalPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.absoluteValueOf
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
import methods.logs.LogsPlans
import methods.logs.LogsRules
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

    SimplifyPowerOfAbsoluteValue(
        plan {
            pattern = powerOf(optionalNegOf(absoluteValueOf(AnyPattern())), AnyPattern())
            explanation = Explanation.SimplifyPowerOfAbsoluteValue

            steps {
                optionally(GeneralRules.SimplifyOddPowerOfNegative)
                optionally(GeneralRules.SimplifyEvenPowerOfNegative)
                optionally(GeneralRules.SimplifyEvenPowerOfAbsoluteValue)
            }
        },
    ),

    SimplifyPowerOfFraction(
        plan {
            pattern = powerOf(condition { it.isSigned<Fraction>() && it.isConstant() }, RationalPattern())
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
                        option { deeply(IntegerRootsPlans.SimplifyIntegerRoot) }
                        option { deeply(IntegerRootsPlans.CancelPowerOfARoot) }
                        option { deeply(IntegerRootsPlans.SimplifyRootOfRootWithCoefficient) }
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
        option(LogsRules.EvaluateLogOfNonPositiveAsUndefined)

        // tidy up decimals
        // It's bad to have this here as it has depth = 0.  We should do this at the start and then forget about it.
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

private val rootOfPowerSimplificationSteps = steps {
    check { it.containsRoots() }
    deeply {
        firstOf {
            option(cancelRootOfPower)
            option(IntegerRootsPlans.SplitAndCancelRootOfPower)
            option(IntegerRootsPlans.SimplifyPowerOfIntegerUnderRoot)
        }
    }
}

/**
 * Steps to tidy up fractions (e.g. fractions of fractions, fractions with negative arguments, unsimplified fractions)
 */
private val fractionSimplificationSteps = steps {
    check { it.containsFractions() }
    deeply {
        firstOf {
            option(normalizeFractionsWithinFractions)
            option(normalizeNegativeSignsInFraction)
            option(FractionArithmeticPlans.SimplifyFraction)
        }
    }
}

/**
 * Steps to turn decimals to fractions
 */
val decimalToFractionConversionSteps = steps {
    check { it.containsDecimals() }
    deeply {
        firstOf {
            option(DecimalPlans.NormalizeFractionOfDecimals)
            option(DecimalPlans.ConvertTerminatingDecimalToFractionAndSimplify)
            option(DecimalPlans.ConvertRecurringDecimalToFractionAndSimplify)
        }
    }
}

// Give it a minDepth of 1 to break cycles.
val constantSimplificationSteps: StepsProducer = stepsWithMinDepth(1) {
    firstOf {
        option { deeply(simpleTidyUpSteps) }

        option { deeply(inlineSumsAndProducts) }

        option(rootOfPowerSimplificationSteps)

        option { deeply(evaluateConstantAbsoluteValue) }

        option(FractionArithmeticPlans.RewriteDivisionsAsFractions)

        // We do this before simplifying fractions for a variety of reasons, a simple one being e.g.
        // 2/10 + 1/10 --> 3/10
        option { deeply(addConstantFractions) }

        option(fractionSimplificationSteps)

        // It would be better to move this out of constantSimplificationSteps altogether and do it first but the
        // required behaviour depends on the previous steps being tried first.
        option(decimalToFractionConversionSteps)
        // can't think of a better place for this rule for now
        option(GeneralRules.SimplifyPlusMinusOfAbsoluteValue)

        option {
            deeply {
                // We reorganize the product before extracting the signs, but if we don't have signs
                // to handle, we leave the reorganization to the end
                optionally { applyTo(reorderProductSteps) { if (it is Minus) it.argument else it } }
                apply(GeneralPlans.NormalizeNegativeSignsInProduct)
            }
        }

        option {
            check { it.containsLogs() }
            deeply {
                firstOf {
                    option(LogsRules.TakePowerOutOfLog)
                    option(LogsRules.EvaluateLogOfBase)
                    option(LogsRules.EvaluateLogOfOne)
                    option(LogsRules.SimplifyLogOfReciprocal)
                    option(LogsPlans.SimplifyLogOfKnownPower)
                }
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
                    option(ConstantExpressionsPlans.SimplifyPowerOfAbsoluteValue)
                }
            }
        }

        option {
            check { it.containsRoots() }
            deeply(collectLikeRootsAndSimplify)
        }
        option { deeply(collectLikeRationalPowersAndSimplify) }

        option(ConstantExpressionsPlans.SimplifyRootsInExpression)
        option(simplifyRationalExponentsInProduct)

        option { deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions) }
        option {
            check { it.containsRoots() }
            deeply(IntegerRootsPlans.SimplifyProductWithRoots)
        }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }

        option { deeply(IntegerArithmeticPlans.SimplifyIntegersInSum) }
        option { deeply(addIntegerAndFraction) }

        option {
            check { it.containsRoots() }
            firstOf {
                // Do this after integers are added, we don't apply it to e.g. `sqrt[4 + 8]`
                option { deeply(IntegerRootsPlans.SimplifySquareRootWithASquareFactorRadicand) }
                // Do this after SimplifySquareRootWithASquareFactorRadicand to make it easier
                option { deeply(IntegerRootsPlans.SimplifySquareRootOfIntegerPlusSurd) }
                option { deeply(addRootAndFraction) }
                option { deeply(FractionRootsPlans.RationalizeDenominators) }
            }
        }

        // This is not a tidy-up rule, we do it only now because at this point the denominator of a fraction has been
        // simplified as much as possible, and the last resort for finding if a fraction of the for [0 / x] can
        // be cancelled is to check the denominator numerically.  We only want to do that on a simplified denominator.
        option {
            check { it.containsFractions() }
            deeply(GeneralRules.SimplifyNonObviousZeroNumeratorFractionToZero)
        }
        // Now that numerator and denominator have been simplified enough, we can find a common factor in the
        // numerator and denominator of fractions.  Do this after factoring squares out of roots so that e.g.
        // [4 + 2sqrt[8] / 8] is transformed to [4 + 4sqrt[2] / 8] first.
        option(FractionArithmeticPlans.SimplifyCommonIntegerFactorInFraction)

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
