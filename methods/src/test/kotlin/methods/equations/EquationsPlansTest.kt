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

import engine.methods.testMethodInX
import methods.factor.FactorExplanation
import org.junit.jupiter.api.Test

class EquationsPlansTest {
    @Test
    fun testSimplifyByFactoringNegativeSignOfLeadingCoefficient() =
        testMethodInX {
            method = EquationsPlans.SimplifyByFactoringNegativeSignOfLeadingCoefficient
            inputExpr = "-2[x^2] + 4x - 2 = 0"

            check {
                fromExpr = "-2 [x ^ 2] + 4 x - 2 = 0"
                toExpr = "2 [x ^ 2] - 4 x + 2 = 0"
                explanation {
                    key = EquationsExplanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient
                }

                step {
                    fromExpr = "-2 [x ^ 2] + 4 x - 2 = 0"
                    toExpr = "-(2 [x ^ 2] - 4 x + 2) = 0"
                    explanation {
                        key = FactorExplanation.FactorNegativeSignOfLeadingCoefficient
                    }
                }

                step {
                    fromExpr = "-(2 [x ^ 2] - 4 x + 2) = 0"
                    toExpr = "2 [x ^ 2] - 4 x + 2 = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.NegateBothSides
                    }
                }
            }
        }

    @Test
    fun testSimplifyByDividingByGcfOfCoefficients() =
        testMethodInX {
            method = EquationsPlans.SimplifyByDividingByGcfOfCoefficients
            inputExpr = "2 [x ^ 2] - 4 x + 2 = 0"

            check {
                fromExpr = "2 [x ^ 2] - 4 x + 2 = 0"
                toExpr = "[x ^ 2] - 2 x + 1 = 0"
                explanation {
                    key = EquationsExplanation.SimplifyByDividingByGcfOfCoefficients
                }

                step {
                    fromExpr = "2 [x ^ 2] - 4 x + 2 = 0"
                    toExpr = "2 ([x ^ 2] - 2 x + 1) = 0"
                    explanation {
                        key = FactorExplanation.FactorGreatestCommonIntegerFactor
                    }
                }

                step {
                    fromExpr = "2 ([x ^ 2] - 2 x + 1) = 0"
                    toExpr = "[x ^ 2] - 2 x + 1 = 0"
                    explanation {
                        key = EquationsExplanation.EliminateConstantFactorOfLhsWithZeroRhs
                    }
                }
            }
        }

    @Test
    fun testSimplifyByDividingByGcfOfCoefficientsWithNoConstantTerm() =
        testMethodInX {
            method = EquationsPlans.SimplifyByDividingByGcfOfCoefficients
            inputExpr = "3[x^2] - 3x = 0"

            check {
                fromExpr = "3[x^2] - 3x = 0 "
                toExpr = "[x^2] - x = 0"
            }
        }

    @Test
    fun `test equation reducible to quadratic equation with integerRationalExponent`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[3^[1/3]]([x^2]-[3^[1/3]])=2x"

            check {
                toExpr = "SetSolution[x : {-[3 ^ -[1 / 3]], [3 ^ [2 / 3]]}]"
            }
        }

    @Test
    fun `test undefined equation cannot be solved`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "x = [1 / 1 - 1]"

            check {
                fromExpr = "x = [1 / 1 - 1]"
                toExpr = "/void/"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "x = [1 / 1 - 1]"
                    toExpr = "/undefined/"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }

                step {
                    fromExpr = "/undefined/"
                    toExpr = "/void/"
                    explanation {
                        key = EquationsExplanation.UndefinedEquationCannotBeSolved
                    }
                }
            }
        }
}
