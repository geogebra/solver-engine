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

package engine.context

enum class Preset(val description: String, val settings: Map<Setting, SettingValue>) {
    Default(
        "All settings at default value",
        mapOf(),
    ),

    USCurriculum(
        "A set of settings corresponding to the usual way the concepts are taught in the USA.",
        mapOf(
            Setting.DontUseIdentitiesForExpanding setTo BooleanSetting.True,
            Setting.SolveEquationsWithoutComputingTheDomain setTo BooleanSetting.True,
            Setting.ConvertRecurringDecimalsToFractionsUsingAlgorithm setTo BooleanSetting.True,
            Setting.AddMixedNumbersWithoutConvertingToImproperFractions setTo BooleanSetting.True,
        ),
    ),

    EUCurriculum(
        "A set of settings corresponding to the usual way the concepts are taught in Europe.",
        mapOf(),
    ),

    GMFriendly(
        "A set of settings which make the solutions compatible with Graspable Math.",
        mapOf(
            Setting.DontAddClarifyingBrackets setTo BooleanSetting.True,
            Setting.BalancingMode setTo BalancingModeSetting.NextTo,
            Setting.MoveTermsOneByOne setTo BooleanSetting.True,
            Setting.QuickAddLikeFraction setTo BooleanSetting.True,
            Setting.QuickAddLikeTerms setTo BooleanSetting.True,
            Setting.CommutativeReorderInSteps setTo BooleanSetting.True,
            Setting.CopySumSignsWhenDistributing setTo BooleanSetting.True,
            Setting.MultiplyFractionsAndNotFractionsDirectly setTo BooleanSetting.True,
            Setting.EliminateNonZeroFactorByDividing setTo BooleanSetting.True,
        ),
    ),

    GMFriendlyAdvanced(
        "A set of settings which make the solutions compatible with Graspable Math.",
        mapOf(
            Setting.DontAddClarifyingBrackets setTo BooleanSetting.True,
            Setting.BalancingMode setTo BalancingModeSetting.Advanced,
            Setting.MoveTermsOneByOne setTo BooleanSetting.True,
            Setting.QuickAddLikeFraction setTo BooleanSetting.True,
            Setting.QuickAddLikeTerms setTo BooleanSetting.True,
            Setting.CommutativeReorderInSteps setTo BooleanSetting.True,
            Setting.CopySumSignsWhenDistributing setTo BooleanSetting.True,
            Setting.MultiplyFractionsAndNotFractionsDirectly setTo BooleanSetting.True,
            Setting.EliminateNonZeroFactorByDividing setTo BooleanSetting.True,
        ),
    ),
}
