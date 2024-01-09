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

package methods.general

import engine.context.BooleanSetting
import engine.context.Setting
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.branchOn
import engine.methods.stepsproducers.steps

enum class NormalizationPlans(override val runner: CompositeMethod) : RunnerMethod {
    NormalizeExpression(
        plan {
            explanation = Explanation.NormalizeExpression

            steps {
                // Temporary workaround. Changing the order of the `deeply` and the
                //  `firstOf` does not currently work because of the removal of the outer
                //  brackets in StepsBuilder
                whilePossible {
                    firstOf {
                        option { deeply(NormalizationRules.NormalizeNegativeSignOfIntegerInSum) }
                        option {
                            check { !isSet(Setting.DontAddClarifyingBrackets) }
                            deeply(NormalizationRules.AddClarifyingBracket)
                        }
                        option { deeply(NormalizationRules.RemoveRedundantBracket) }
                        option { deeply(NormalizationRules.RemoveRedundantPlusSign) }
                    }
                }
            }
        },
    ),

    ReorderProductInSteps(
        plan {
            explanation = Explanation.ReorderProduct

            steps {
                whilePossible(NormalizationRules.ReorderProductSingleStep)
            }
        },
    ),

    RemoveAllBracketSumInSum(
        plan {
            explanation = Explanation.RemoveAllBracketSumInSum

            steps {
                whilePossible(NormalizationRules.RemoveBracketSumInSum)
            }
        },
    ),

    RemoveAllBracketProductInProduct(
        plan {
            explanation = Explanation.RemoveAllBracketProductInProduct

            steps {
                whilePossible(NormalizationRules.RemoveBracketProductInProduct)
            }
        },
    ),
}

val reorderProductSteps = branchOn(Setting.CommutativeReorderInSteps) {
    case(BooleanSetting.True, NormalizationPlans.ReorderProductInSteps)
    case(BooleanSetting.False, NormalizationRules.ReorderProduct)
}

val inlineSumsAndProducts = steps {
    firstOf {
        option(NormalizationPlans.RemoveAllBracketSumInSum)
        option(NormalizationPlans.RemoveAllBracketProductInProduct)
    }
}
