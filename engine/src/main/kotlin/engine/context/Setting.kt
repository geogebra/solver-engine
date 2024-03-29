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
        "Reorder a product or polynomial by moving one term at a time instead of " +
            "all of them in a single step",
    ),

    SolveEquationsWithoutComputingTheDomain(
        BooleanSetting,
        "Solve an equation without computing the domain first, i.e. by finding all the solutions and then plugging " +
            "them in to check if they are valid. May not work for all equations.",
    ),

    ConvertRecurringDecimalsToFractionsUsingAlgorithm(
        BooleanSetting,
        "Instead of using the formula for converting recurring decimals to fractions, use the algorithm where you " +
            "set x equal to the number, multiply the equation by a power of ten and solve the equation system formed " +
            "by these two equations.",
    ),

    AddMixedNumbersWithoutConvertingToImproperFractions(
        BooleanSetting,
        "When adding several mixed numbers, instead of converting them individually to fractions, split them, then " +
            "add the integers, the fractions and finally add the two together",
    ),

    CopySumSignsWhenDistributing(
        BooleanSetting,
        "When distributing 5(x - 2), do 5*x - 5*2 rather than 5*x + 5*(-2)",
    ),

    MultiplyFractionsAndNotFractionsDirectly(
        BooleanSetting,
        "When multplying e.g. 3*[x / 2], do not turn 3 to a fraction but write it as [3x / 2] directly",
    ),

    RestrictAddingFractionsWithConstantDenominator(
        BooleanSetting,
        "Add restrictions to adding fractions with constant denominator, for example " +
            "non-constant fractions are only added if at least one of them is a sum. Similar restrictions " +
            "apply to adding an integer and a fraction.",
    ),

    EliminateNonZeroFactorByDividing(
        BooleanSetting,
        "When simplifying ab = 0 to b = 0 because a != 0, do it by dividing both sides by a instead " +
            "of just cancelling the a, which is the default",
    ),

    /*
     * This setting is added because I don't know how to make the gmAction tests pass after changing SimplifyEquation
     * plan to cancel common factor on both sides
     */
    DontCancelCommonFactorsWhenSimplifyingEquation(
        BooleanSetting,
        "When simplifying an equation, if lhs and rhs have a common non-zero factor, don't cancel it",
    ),

    SolveInequalitiesUsingTestPoints(
        BooleanSetting,
        "Use test points to check if intervals satisfy the inequality to solve",
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
