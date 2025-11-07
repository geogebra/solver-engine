/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

package methods.angles

import engine.methods.testRule
import kotlin.test.Test

class AnglesRulesTest {
    @Test
    fun testDegreeConversion() {
        testRule("degree[45]", AnglesRules.UseDegreeConversionFormula, "degree[45] * [/pi/ / degree[180]]")
    }

    @Test
    fun testRadianConversion() {
        testRule("[/pi/ / 4]", AnglesRules.UseRadianConversionFormula, "[/pi/ / 4] * [degree[180] / /pi/]")
    }

    @Test
    fun testEvaluatingTrigonometricFunctionOfMainAngles() {
        testRule("sin[degree[30]]", AnglesRules.EvaluateExactValueOfMainAngle, "[1 / 2]")
        testRule("cos[[/pi/ / 3]]", AnglesRules.EvaluateExactValueOfMainAngle, "[1 / 2]")
        testRule("tan[degree[45]]", AnglesRules.EvaluateExactValueOfMainAngle, "1")
        testRule("cot[[/pi/ / 4]]", AnglesRules.EvaluateExactValueOfMainAngle, "1")
    }

    @Test
    fun testFindReferenceAngleInFirstQuadrantInDegree() {
        testRule(
            "cos[degree[120]]",
            AnglesRules.FindReferenceAngleInFirstQuadrantInDegree,
            "-cos[degree[ 180 ] - degree [ 120 ]]",
        )
        testRule(
            "cos[degree[240]]",
            AnglesRules.FindReferenceAngleInFirstQuadrantInDegree,
            "-cos[degree[ 240 ] - degree[ 180 ]]",
        )
        testRule(
            "cos[degree[300]]",
            AnglesRules.FindReferenceAngleInFirstQuadrantInDegree,
            "cos[degree[ 360 ] - degree[ 300 ]]",
        )
    }

    @Test
    fun testFindReferenceAngleInFirstQuadrantInRadian() {
        testRule(
            "cos[[2 /pi/ / 3]]",
            AnglesRules.FindReferenceAngleInFirstQuadrantInRadian,
            "-cos[/pi/ - [2 /pi/ / 3]]",
        )
        testRule(
            "cos[[4 /pi/ / 3]]",
            AnglesRules.FindReferenceAngleInFirstQuadrantInRadian,
            "-cos[[4 /pi/ / 3] - /pi/]",
        )
        testRule(
            "cos[[5 /pi/ / 3]]",
            AnglesRules.FindReferenceAngleInFirstQuadrantInRadian,
            "cos[2 /pi/ - [5 /pi/ / 3]]",
        )
    }

    @Test
    fun testRewriteAngleInDegrees() {
        testRule(
            "degree[390]",
            AnglesRules.RewriteAngleInDegreesByExtractingMultiplesOf360,
            "degree[360] + degree[30]",
        )
        testRule(
            "degree[720]",
            AnglesRules.RewriteAngleInDegreesByExtractingMultiplesOf360,
            "2 * degree[360] + degree[0]",
        )
        testRule(
            "degree[-420]",
            AnglesRules.RewriteAngleInDegreesByExtractingMultiplesOf360,
            "(-1) degree[360] + degree[-60]",
        )
    }

    @Test
    fun testRewriteAngleInRadians() {
        testRule(
            "[7 * /pi/ / 2]",
            AnglesRules.RewriteAngleInRadiansByExtractingMultiplesOfTwoPi,
            "2 /pi/ + [ 3 /pi/ / 2]",
        )
        testRule(
            "8 * /pi/",
            AnglesRules.RewriteAngleInRadiansByExtractingMultiplesOfTwoPi,
            "4 * 2 /pi/ + 0",
        )
    }

    @Test
    fun testCheckDomainOfTrigFunction() {
        testRule(
            "tan[degree[270]]",
            AnglesRules.CheckDomainOfFunction,
            "/undefined/",
        )
        testRule(
            "cot[degree[270]]",
            AnglesRules.CheckDomainOfFunction,
            null,
        )
        testRule(
            "sec[degree[270]]",
            AnglesRules.CheckDomainOfFunction,
            "/undefined/",
        )
        testRule(
            "csc[degree[270]]",
            AnglesRules.CheckDomainOfFunction,
            null,
        )
        testRule(
            "cot[degree[360]]",
            AnglesRules.CheckDomainOfFunction,
            "/undefined/",
        )
        testRule(
            "csc[degree[360]]",
            AnglesRules.CheckDomainOfFunction,
            "/undefined/",
        )
        testRule(
            "tan[[3 /pi/ / 2]]",
            AnglesRules.CheckDomainOfFunction,
            "/undefined/",
        )
        testRule(
            "cot[[3 /pi/ / 2]]",
            AnglesRules.CheckDomainOfFunction,
            null,
        )
        testRule(
            "sec[[3 /pi/ / 2]]",
            AnglesRules.CheckDomainOfFunction,
            "/undefined/",
        )
        testRule(
            "csc[[3 /pi/ / 2]]",
            AnglesRules.CheckDomainOfFunction,
            null,
        )
        testRule(
            "cot[2 /pi/]",
            AnglesRules.CheckDomainOfFunction,
            "/undefined/",
        )
        testRule(
            "csc[2 /pi/]",
            AnglesRules.CheckDomainOfFunction,
            "/undefined/",
        )
        testRule(
            "tan[degree[45]]",
            AnglesRules.CheckDomainOfFunction,
            null,
        )
        testRule(
            "tan[[/pi/ / 4]]",
            AnglesRules.CheckDomainOfFunction,
            null,
        )
        testRule(
            "cot[0]",
            AnglesRules.CheckDomainOfFunction,
            "/undefined/",
        )
        testRule(
            "sec[0]",
            AnglesRules.CheckDomainOfFunction,
            null,
        )
    }

    @Test
    fun testEvaluatingDerivedTrigonometricFunction() {
        testRule(
            "sec[degree[60]]",
            AnglesRules.DeriveTrigonometricFunctionFromPrimitiveFunctions,
            "[1 / cos [degree[60]]]",
        )
        testRule(
            "csc[[/pi/ / 6]]",
            AnglesRules.DeriveTrigonometricFunctionFromPrimitiveFunctions,
            "[1 / sin[[/pi/ / 6]]]",
        )
    }
}
