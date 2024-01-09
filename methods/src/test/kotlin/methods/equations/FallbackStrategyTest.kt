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
import methods.collecting.CollectingExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class FallbackStrategyTest {
    @Test
    fun `test fallback strategy applies on unsolvable unsimplified equation`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[x ^ 6] + 2 x + 1 = [x ^ 5] + 2 x - 3"

            check {
                fromExpr = "[x ^ 6] + 2 x + 1 = [x ^ 5] + 2 x - 3"
                toExpr = "[x ^ 6] - [x ^ 5] + 4 = 0"
                explanation {
                    key = EquationsExplanation.ReduceEquation
                }

                step {
                    fromExpr = "[x ^ 6] + 2 x + 1 = [x ^ 5] + 2 x - 3"
                    toExpr = "[x ^ 6] + 1 = [x ^ 5] - 3"
                    explanation {
                        key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                    }
                }
                step {
                    fromExpr = "[x ^ 6] + 1 = [x ^ 5] - 3"
                    toExpr = "[x ^ 6] + 4 - [x ^ 5] = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }
                step {
                    fromExpr = "[x ^ 6] + 4 - [x ^ 5] = 0"
                    toExpr = "[x ^ 6] - [x ^ 5] + 4 = 0"
                    explanation {
                        key = PolynomialsExplanation.NormalizePolynomial
                    }
                }
            }
        }

    @Test
    fun `test fallback strategy applies for unsimplified equation = 0`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[x ^ 6] + [x ^ 6] + [x ^ 5] + 1 = 0"
            check {
                fromExpr = "[x ^ 6] + [x ^ 6] + [x ^ 5] + 1 = 0"
                toExpr = "2 [x ^ 6] + [x ^ 5] + 1 = 0"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }
            }
        }

    @Test
    fun `test fallback strategy does not apply on simplified equation`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[x^6] + [x^5] + 1 = 0"

            check {
                noTransformation()
            }
        }
}
