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
