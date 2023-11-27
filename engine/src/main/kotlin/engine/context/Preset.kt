package engine.context

enum class Preset(val description: String, val settings: Map<Setting, SettingValue>) {

    Default(
        "All settings at default value",
        mapOf(),
    ),

    USCurriculum(
        "A set of settings corresponding to the usual way the concepts are taught in the USA.",
        mapOf(
            Setting.DontUseIdentitiesForExpanding to BooleanSetting.True,
            Setting.SolveEquationsWithoutComputingTheDomain to BooleanSetting.True,
            Setting.ConvertRecurringDecimalsToFractionsUsingAlgorithm to BooleanSetting.True,
            Setting.AddMixedNumbersWithoutConvertingToImproperFractions to BooleanSetting.True,
        ),
    ),

    EUCurriculum(
        "A set of settings corresponding to the usual way the concepts are taught in Europe.",
        mapOf(),
    ),

    GMFriendly(
        "A set of settings which make the solutions compatible with Graspable Math.",
        mapOf(
            Setting.DontAddClarifyingBrackets to BooleanSetting.True,
            Setting.AdvancedBalancing to BooleanSetting.True,
            Setting.QuickAddLikeFraction to BooleanSetting.True,
            Setting.QuickAddLikeTerms to BooleanSetting.True,
            Setting.ReorderProductsInSteps to BooleanSetting.True,
            Setting.CopySumSignsWhenDistributing to BooleanSetting.True,
        ),
    ),
}
