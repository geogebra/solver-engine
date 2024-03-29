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

package methods.equations

import engine.methods.testRule
import engine.methods.testRuleInX
import methods.equations.EquationsRules.ApplyQuadraticFormula
import methods.equations.EquationsRules.EliminateConstantFactorOfLhsWithZeroRhsDirectly
import methods.equations.EquationsRules.MultiplyByInverseOfLeadingCoefficient
import methods.equations.EquationsRules.SeparateEquationInPlusMinusForm
import org.junit.jupiter.api.Test

class EquationRulesTest {
    @Test
    fun testMultiplyByInverseCoefficientOfSquaredMonomial() {
        testRuleInX(
            "[x^2] + x = 3",
            MultiplyByInverseOfLeadingCoefficient,
            null,
        )
        testRuleInX(
            "[1/2][x^2] + 3x = 5",
            MultiplyByInverseOfLeadingCoefficient,
            "([1/2][x^2] + 3x)*2 = 5*2",
        )
        testRuleInX(
            "2x - [2/3][x^2] = 10",
            MultiplyByInverseOfLeadingCoefficient,
            "(2x - [2/3][x^2])(-[3/2]) = 10(-[3/2])",
        )
        testRuleInX(
            "2[x^2] - 3[x^3] = 2",
            MultiplyByInverseOfLeadingCoefficient,
            "(2[x^2] - 3[x^3])(-[1/3]) = 2(-[1/3])",
        )
    }

    @Test
    fun testCompleteTheSquare() {
        testRuleInX(
            "[x^2] + 6x = 1",
            EquationsRules.CompleteTheSquare,
            "[x^2] + 6x + [([6/2])^2] = 1 + [([6/2])^2]",
        )
        testRuleInX(
            "[x^2] + [1/2]x = 10",
            EquationsRules.CompleteTheSquare,
            "[x^2] + [1/2]x + [([[1/2]/2])^2] = 10 + [([[1/2]/2])^2]",
        )
    }

    @Test
    fun testCancelGcfOfCoefficients() {
        testRuleInX(
            "2 ([x^2] + x + 1) = 0",
            EliminateConstantFactorOfLhsWithZeroRhsDirectly,
            "[x^2] + x + 1 = 0",
        )
        testRuleInX(
            "2x = 0",
            EliminateConstantFactorOfLhsWithZeroRhsDirectly,
            "x = 0",
        )
        testRuleInX(
            "-2x = 0",
            EliminateConstantFactorOfLhsWithZeroRhsDirectly,
            "x = 0",
        )
        testRuleInX(
            "-2x(x-1) = 0",
            EliminateConstantFactorOfLhsWithZeroRhsDirectly,
            "x(x-1) = 0",
        )
        testRuleInX(
            "-2x(x-1) != 0",
            EliminateConstantFactorOfLhsWithZeroRhsDirectly,
            "x(x-1) != 0",
        )
        testRuleInX(
            "-2a[b^2] != 0",
            EliminateConstantFactorOfLhsWithZeroRhsDirectly,
            "a[b^2] != 0",
        )
    }

    @Test
    fun testApplyQuadraticFormula() {
        testRuleInX(
            "[x^2] + 2x + 1 = 0",
            ApplyQuadraticFormula,
            "x = [-2 +/- sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1]",
        )
    }

    @Test
    fun testSeparatePlusMinusQuadraticSolutions() {
        testRuleInX(
            "x = [-2 +/- sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1]",
            SeparateEquationInPlusMinusForm,
            "x = [-2 - sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1] OR " +
                "x = [-2 + sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1]",
        )
    }

    @Test
    fun testExtractSolutionFromNegativeUnderSquareRootInRealDomain() {
        testRuleInX(
            "x = [2 +/- sqrt[-28] / 2]",
            EquationsRules.ExtractSolutionFromNegativeUnderSquareRootInRealDomain,
            "Contradiction[x: x = [2 +/- sqrt[-28] / 2]]",
        )
    }

    @Test
    fun testSeparateModulusEqualsPositiveConstant() {
        testRuleInX(
            "abs[x - 2] = 3",
            EquationsRules.SeparateModulusEqualsPositiveConstant,
            "x - 2 = 3 OR x - 2 = -3",
        )
        testRuleInX(
            "3abs[1 - x] = 6",
            EquationsRules.SeparateModulusEqualsPositiveConstant,
            "3(1 - x) = 6 OR 3(1 - x) = -6",
        )
        testRuleInX(
            "[abs[2x + 1] / 5] = 8",
            EquationsRules.SeparateModulusEqualsPositiveConstant,
            "[1 / 5](2x + 1) = 8 OR [1 / 5](2x + 1) = -8",
        )
        testRuleInX(
            "-3abs[x + 1] = 5",
            EquationsRules.SeparateModulusEqualsPositiveConstant,
            null,
        )
        testRuleInX(
            "abs[x + 2] = -10",
            EquationsRules.SeparateModulusEqualsPositiveConstant,
            null,
        )
    }

    @Test
    fun testResolveModulusEqualsZero() {
        testRuleInX(
            "abs[x - 2] = 0",
            EquationsRules.ResolveModulusEqualsZero,
            "x - 2 = 0",
        )
        testRuleInX(
            "3abs[1 - x] = 0",
            EquationsRules.ResolveModulusEqualsZero,
            "3(1 - x) = 0",
        )
        testRuleInX(
            "[abs[2x + 1] / 5] = 0",
            EquationsRules.ResolveModulusEqualsZero,
            "[1 / 5](2x + 1) = 0",
        )
    }

    @Test
    fun testExtractSolutionFromModulusEqualsNegativeConstant() {
        testRuleInX(
            "abs[x - 2] = -3",
            EquationsRules.ExtractSolutionFromModulusEqualsNegativeConstant,
            "Contradiction[x : abs[x - 2] = -3]",
        )
        testRuleInX(
            "3abs[1 - x] = -1",
            EquationsRules.ExtractSolutionFromModulusEqualsNegativeConstant,
            "Contradiction[x : 3abs[1 - x] = -1]",
        )
        testRuleInX(
            "[abs[2x + 1] / 5] = -10",
            EquationsRules.ExtractSolutionFromModulusEqualsNegativeConstant,
            "Contradiction[x : [abs[2x + 1] / 5] = -10]",
        )
        testRuleInX(
            "2abs[x - 2] = 5",
            EquationsRules.ResolveModulusEqualsZero,
            null,
        )
        testRuleInX(
            "-2abs[x - 2] = -5",
            EquationsRules.ResolveModulusEqualsZero,
            null,
        )
    }

    @Test
    fun testMoveSecondModulusToRhs() {
        testRule(
            "abs[x + 1] + abs[x - 2] = 0",
            EquationsRules.MoveSecondModulusToRhs,
            "abs[x + 1] + abs[x - 2] - abs[x - 2] = -abs[x - 2]",
        )
        testRule(
            "-abs[x] + abs[2x] = 0",
            EquationsRules.MoveSecondModulusToRhs,
            "-abs[x] + abs[2x] + abs[x] = abs[x]",
        )
        testRule(
            "abs[2x] - 3abs[x] = 0",
            EquationsRules.MoveSecondModulusToRhs,
            "abs[2x] - 3abs[x] + 3abs[x] = 3abs[x]",
        )
        testRule(
            "-2abs[x + 1] - 3abs[x - 2] = 0",
            EquationsRules.MoveSecondModulusToRhs,
            "-2abs[x + 1] - 3abs[x - 2] + 3abs[x - 2] = 3abs[x - 2]",
        )
    }

    @Test
    fun testMoveSecondModulusToLhs() {
        testRule(
            "0 = abs[x + 1] + abs[x - 2]",
            EquationsRules.MoveSecondModulusToLhs,
            "-abs[x - 2] = abs[x + 1] + abs[x - 2] - abs[x - 2]",
        )
        testRule(
            "0 = -abs[x] + abs[2x]",
            EquationsRules.MoveSecondModulusToLhs,
            "abs[x] = -abs[x] + abs[2x] + abs[x]",
        )
        testRule(
            "0 = abs[2x] - 3abs[x]",
            EquationsRules.MoveSecondModulusToLhs,
            "3abs[x] = abs[2x] - 3abs[x] + 3abs[x]",
        )
        testRule(
            "0 = -2abs[x + 1] - 3abs[x - 2]",
            EquationsRules.MoveSecondModulusToLhs,
            "3abs[x - 2] = -2abs[x + 1] - 3abs[x - 2] + 3abs[x - 2]",
        )
    }

    @Test
    fun testSeparateModulusEqualsModulus() {
        testRule(
            "abs[x + 1] = abs[3x]",
            EquationsRules.SeparateModulusEqualsModulus,
            "x + 1 = 3x OR x + 1 = -3x",
        )
        testRule(
            "3abs[x] = 5abs[x + 1]",
            EquationsRules.SeparateModulusEqualsModulus,
            "3x = 5(x + 1) OR 3x = -5(x + 1)",
        )
        testRule(
            "abs[2x] = -abs[x + 1]",
            EquationsRules.SeparateModulusEqualsModulus,
            null,
        )
    }

    @Test
    fun testResolveModulusEqualsNegativeModulus() {
        testRule(
            "abs[x + 2] = -abs[x - 1]",
            EquationsRules.ResolveModulusEqualsNegativeModulus,
            "x + 2 = 0 AND x - 1 = 0",
        )
        testRule(
            "3abs[x] = -2abs[x + 2]",
            EquationsRules.ResolveModulusEqualsNegativeModulus,
            "3x = 0 AND 2(x + 2) = 0",
        )
        testRule(
            "5abs[x] = -2x",
            EquationsRules.ResolveModulusEqualsNegativeModulus,
            null,
        )
        testRule(
            "abs[x] = abs[2x]",
            EquationsRules.ResolveModulusEqualsNegativeModulus,
            null,
        )
    }

    @Test
    fun testSeparateModulusEqualsExpression() {
        testRule(
            "abs[x + 2] = 3x",
            EquationsRules.SeparateModulusEqualsExpression,
            "x + 2 = 3x AND x + 2 >= 0 OR -(x + 2) = 3x AND x + 2 < 0",
        )
        testRule(
            "5abs[x] = [x ^ 2]",
            EquationsRules.SeparateModulusEqualsExpression,
            "5x = [x ^ 2] AND x >= 0 OR -5x = [x ^ 2] AND x < 0",
        )
    }

    @Test
    fun testSplitEquationWithRationalVariables() {
        testRule(
            "[x^2] + 3[y^2] + 6xy sqrt[3] = 5 - 2sqrt[3]",
            EquationsRules.SplitEquationWithRationalVariables,
            "[x^2] + 3[y^2] = 5 AND 6xy sqrt[3] = -2sqrt[3]",
        )
        testRule(
            "3x + sqrt[5] = y sqrt[5] - 2",
            EquationsRules.SplitEquationWithRationalVariables,
            "3x = -2 AND sqrt[5] = y sqrt[5]",
        )
    }

    @Test
    fun testSolveEquationWithIncompatibleSigns() {
        testRuleInX(
            "x + R = [E / I]",
            EquationsRules.SolveEquationWithIncompatibleSigns,
            null,
        )
        testRuleInX(
            "[x ^ 6] + 1 = [x ^ 5] - 3",
            EquationsRules.SolveEquationWithIncompatibleSigns,
            null,
        )
        testRuleInX(
            "-3 = 2[x^2]",
            EquationsRules.SolveEquationWithIncompatibleSigns,
            "Contradiction[x: -3 = 2[x^2]]",
        )
        testRuleInX(
            "[2^x]=-1",
            EquationsRules.SolveEquationWithIncompatibleSigns,
            "Contradiction[x: [2^x] = -1]",
        )
    }
}
