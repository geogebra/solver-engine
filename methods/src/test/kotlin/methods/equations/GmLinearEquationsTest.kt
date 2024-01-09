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

import engine.context.Preset
import engine.methods.testMethodInX
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

// An example of how the tests can be written, to be moved somewhere better.
// The tag can be on the class as here, in which case all tests in the class will be tagged, or
// on individual tests.
@Tag("GmAction")
class GmOneStepLinearEquationMultiplicationTests {
    @Test
    fun `ax=c`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "3 x = 4"
            check { toExpr = "SetSolution[x : {[4 / 3]}]" }
        }

    @Test
    fun `ax=-c`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "3 x = -4"
            check { toExpr = "SetSolution[x : {-[4 / 3]}]" }
        }

    @Test
    fun `-ax=c`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "-3 x = 4"
            check { toExpr = "SetSolution[x : {-[4 / 3]}]" }
        }

    @Test
    fun `-ax=-c`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "-3 x = -4"
            check { toExpr = "SetSolution[x : {[4 / 3]}]" }
        }

    @Test
    fun `ax=a`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "3 x = 3"
            check { toExpr = "SetSolution[x : {1}]" }
        }

    // to-do: Need to have [10/10]x ==> x
    // @Test
    // fun `frac{a}{b}x=c`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[3/2]x = 5"
    //     check { toExpr = "SetSolution[x : {[10/3]}]" }
    // }

    // to-do: Need to have [6x/3] ==> 2x and [10/10]x ==> x
    // @Test
    // fun `frac{a}{b}x=Na`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[3/2]x = 6"
    //     check { toExpr = "SetSolution[x : {4}]" }
    // }
    //
    // @Test
    // fun `frac{a}{b}x=frac{c}{d}`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[3/2]x = [5/7]"
    //     check { toExpr = "SetSolution[x : {[10/21]}]" }
    // }
    //
    // @Test
    // fun `frac{a}{b}x=frac{Ma}{Nb}`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[3/2]x = [9/10]"
    //     check { toExpr = "SetSolution[x : {[3/5]}]" }
    // }
}

@Tag("GmAction")
class GmOneStepLinearEquationDivisionTests {
    @Test
    fun `equation flipping`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "4 = 2x"
            check { toExpr = "SetSolution[x : {2}]" }
        }

    @Test
    fun `frac{x}{a}=b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "[x/3] = 4"
            check { toExpr = "SetSolution[x : {12}]" }
        }

    // TO-DO: Introduce a map of toExpr -> gmToExpr instead so we don't have to go into all the sub-steps
    // TO-DO: Adjust GM to put brackets around the *-4
    @Test
    fun `frac{x}{-a}=b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "[x/-3] = 4"
            check {
                toExpr = "SetSolution[x : {-12}]"
                step { toExpr = "-[x / 3] = 4" }
                step { toExpr = "[x / 3] = -4" }
                step {
                    step {
                        toExpr = "[x / 3] * 3 = 3 * (-4)"
                        gmToExpr = "[x / 3] * 3 = 3 * -4"
                    }
                    step {
                        step {
                            toExpr = "x = 3 * (-4)"
                            gmToExpr = "x = 3 * -4"
                        }
                        step { }
                        step { }
                    }
                }
                step { }
            }
        }
    // @Test
    // fun `frac{x}{a}=-b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[x/3] = -4"
    //     check { toExpr = "SetSolution[x : {-12}]" }
    // }
    // @Test
    // fun `-frac{x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "-[x/3] = 4"
    //     check { toExpr = "SetSolution[x : {-12}]" }
    // }
    // @Test
    // fun `frac{-x}{a}=b`() = testMethodInX(Preset.GMFriendly) {
    //     method = EquationsPlans.SolveEquation
    //     inputExpr = "[-x/3] = 4"
    //     check { toExpr = "SetSolution[x : {-12}]" }
    // }

    @Test
    fun `-frac{-x}{a}=b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "-[-x/3] = 4"
            check { toExpr = "SetSolution[x : {12}]" }
        }

    @Test
    fun `-frac{x}{a}=-b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "-[x/3] = -4"
            check { toExpr = "SetSolution[x : {12}]" }
        }

    @Test
    fun `-frac{-x}{-a}=-b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "-[-x/-3] = -4"
            check { toExpr = "SetSolution[x : {12}]" }
        }

    @Test
    fun `frac{x}{-a}=-b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "[x/-3] = -4"
            check { toExpr = "SetSolution[x : {12}]" }
        }
}

@Tag("GmAction")
class GmOneStepLinearEquationAdditionTests {
    @Test
    fun `x+a=b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "x+1 = 4"
            check { toExpr = "SetSolution[x : {3}]" }
        }

    @Test
    fun `x-a=b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "x-1 = 4"
            check { toExpr = "SetSolution[x : {5}]" }
        }

    @Test
    fun `a-x=b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "5-x = 7"
            check { toExpr = "SetSolution[x : {-2}]" }
        }

    @Test
    fun `-x+a=-b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "-x+1 = -10"
            check { toExpr = "SetSolution[x : {11}]" }
        }

    @Test
    fun `-x-a=b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "-x-1 = 1"
            check { toExpr = "SetSolution[x : {-2}]" }
        }

    @Test
    fun `-x-a=-b`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "-x-3 = -5"
            check { toExpr = "SetSolution[x : {2}]" }
        }

    @Test
    fun `x+a=a`() =
        testMethodInX(Preset.GMFriendly) {
            method = EquationsPlans.SolveEquation
            inputExpr = "x+3 = 3"
            check { toExpr = "SetSolution[x : {0}]" }
        }
}
