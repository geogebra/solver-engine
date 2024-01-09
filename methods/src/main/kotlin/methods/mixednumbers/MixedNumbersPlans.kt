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

package methods.mixednumbers

import engine.context.BooleanSetting
import engine.context.Setting
import engine.expressions.MixedNumberExpression
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.patterns.sumContaining
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.FractionArithmeticRules
import methods.fractionarithmetic.addIntegerFractions
import methods.general.GeneralRules
import methods.integerarithmetic.IntegerArithmeticRules

enum class MixedNumbersPlans(override val runner: CompositeMethod) : RunnerMethod {
    ConvertMixedNumbersToSums(
        plan {
            explanation = Explanation.ConvertMixedNumbersToSums

            steps {
                whilePossible { deeply(MixedNumbersRules.SplitMixedNumber) }
            }
        },
    ),

    ConvertMixedNumberToImproperFraction(
        plan {
            explanation = Explanation.ConvertMixedNumberToImproperFraction

            steps {
                apply(MixedNumbersRules.SplitMixedNumber)
                optionally {
                    applyTo(FractionArithmeticPlans.SimplifyFraction) { it.secondChild }
                }
                apply(FractionArithmeticRules.ConvertIntegerToFraction)
                apply(addIntegerFractions)
            }
        },
    ),

    /**
     * Adds two mixed numbers together
     */
    @PublicMethod
    AddMixedNumbers(
        plan {
            pattern = sumContaining { it is MixedNumberExpression }

            explanation = Explanation.AddMixedNumbers

            steps {
                branchOn(Setting.AddMixedNumbersWithoutConvertingToImproperFractions) {
                    case(BooleanSetting.True) {
                        apply(ConvertMixedNumbersToSums)

                        shortcut {
                            deeply(GeneralRules.SimplifyZeroDenominatorFractionToUndefined)
                        }

                        optionally {
                            applyToChildren(FractionArithmeticPlans.SimplifyFraction)
                        }

                        whilePossible(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
                        whilePossible(addIntegerFractions)

                        firstOf {
                            option(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
                            option(MixedNumbersRules.ConvertSumOfIntegerAndProperFractionToMixedNumber)
                            option {
                                apply(FractionArithmeticRules.ConvertIntegerToFraction)
                                apply(addIntegerFractions)
                                apply(MixedNumbersRules.FractionToMixedNumber)
                            }
                        }
                    }

                    case(BooleanSetting.False) {
                        applyToChildren(ConvertMixedNumberToImproperFraction)
                        whilePossible(addIntegerFractions)
                        // result might be integer or proper fraction after
                        // simplification, so this step is optional
                        optionally(MixedNumbersRules.FractionToMixedNumber)
                    }
                }
            }
        },
    ),
}
