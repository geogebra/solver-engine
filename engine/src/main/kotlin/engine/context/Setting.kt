package engine.context

data class SettingValue(val name: String)

interface SettingKind {
    val default: SettingValue
    val settingValues: List<SettingValue>
    fun fromName(name: String) = settingValues.firstOrNull { it.name == name }
    fun get(m: Map<SettingKind, SettingValue>) = m[this] ?: default
}

object BooleanSetting : SettingKind {
    val True = SettingValue("true")
    val False = SettingValue("false")

    override val default = False
    override val settingValues = listOf(True, False)
}

object BalancingModeSetting : SettingKind {
    val Basic = SettingValue("basic")
    val Advanced = SettingValue("advanced")
    val NextTo = SettingValue("nextTo")

    override val default = Basic
    override val settingValues = listOf(Basic, Advanced, NextTo)
}

enum class Setting(val kind: SettingKind, val description: String) {
    PreferDecimals(
        BooleanSetting,
        "Use decimals instead of fractions whenever possible",
    ),

    DontAddClarifyingBrackets(
        BooleanSetting,
        "Do not add clarifying brackets to ambiguous expressions",
    ),

    MoveTermsOneByOne(
        BooleanSetting,
        "Move terms (such as constants or variable) in an equation one by one instead of all at once",
    ),

    BalancingMode(
        BalancingModeSetting,
        "How to balance an equation: " +
            "'basic' means explicit inverse operation to both sides, " +
            "'advanced' means direct canceling of the term(s) to be canceled, " +
            "'nextTo' means keep inverse operation next to original term when possible",
    ),

    QuickAddLikeFraction(
        BooleanSetting,
        "Add like integer fractions, such as 1/5 + 2/5 in a single step",
    ),

    QuickAddLikeTerms(
        BooleanSetting,
        "Add like terms with integer coefficients, such as 2a + 3a in a single step",
    ),

    DontUseIdentitiesForExpanding(
        BooleanSetting,
        "Do not use identities such as (a + b) = a^2 + 2ab + b^2 when expanding",
    ),

    CommutativeReorderInSteps(
        BooleanSetting,
        "Reorder a product or polynomial by moving one term at a time instead of all of them in a single step",
    ),

    SolveEquationsWithoutComputingTheDomain(
        BooleanSetting,
        "Solve an equation without computing the domain first, i.e. by finding all the solutions and then plugging" +
            "them in to check if they are valid. May not work for all equations.",
    ),

    ConvertRecurringDecimalsToFractionsUsingAlgorithm(
        BooleanSetting,
        "Instead of using the formula for converting recurring decimals to fractions, use the algorithm where you" +
            "set x equal to the number, multiply the equation by a power of ten and solve the equation system formed" +
            "by these two equations.",
    ),

    AddMixedNumbersWithoutConvertingToImproperFractions(
        BooleanSetting,
        "When adding several mixed numbers, instead of converting them individually to fractions, split them, then" +
            "add the integers, the fractions and finally add the two together",
    ),

    CopySumSignsWhenDistributing(
        BooleanSetting,
        "When distributing 5(x - 2), do 5*x - 5*2 rather than 5*x + 5*(-2)",
    ),

    MultiplyFractionsAndNotFractionsDirectly(
        BooleanSetting,
        "when multplying e.g. 3*[x / 2], do not turn 3 to a fraction but write it as [3x / 2] directly",
    ),
    ;

    infix fun setTo(value: SettingValue): Pair<Setting, SettingValue> {
        if (value !in kind.settingValues) {
            throw InvalidSettingValueException(this, value)
        }
        return Pair(this, value)
    }
}

class InvalidSettingValueException(val setting: Setting, val value: SettingValue) :
    Exception("Invalid value $value for setting $setting")

typealias SettingsMap = Map<Setting, SettingValue>
