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

package methods.fallback

import engine.methods.testMethod
import methods.inequalities.InequalitiesExplanation
import org.junit.jupiter.api.Test

class FallbackPlansTest {
    @Test
    fun testExpressionIsFullySimplifiedSucceedsWithLinearPolynomial() =
        testMethod {
            method = FallbackPlans.ExpressionIsFullySimplified
            inputExpr = "x + 1"

            check {
                fromExpr = "x + 1"
                toExpr = "/void/"
                explanation {
                    key = FallbackExplanation.ExpressionIsFullySimplified
                }
            }
        }

    @Test
    fun testExpressionIsFullySimplifiedSucceedsWithQuadratic() =
        testMethod {
            method = FallbackPlans.ExpressionIsFullySimplified
            inputExpr = "[x^2] + 2x"

            check {
                fromExpr = "[x^2] + 2x"
                toExpr = "/void/"
                explanation {
                    key = FallbackExplanation.ExpressionIsFullySimplified
                }
            }
        }

    @Test
    fun testQuadraticHasNegativeDiscriminantSucceeds() =
        testMethod {
            method = FallbackPlans.QuadraticIsIrreducible
            inputExpr = "[x ^ 2] + 2 x + 2"

            check {
                fromExpr = "[x ^ 2] + 2 x + 2"
                toExpr = "/void/"
                explanation {
                    key = FallbackExplanation.QuadraticIsIrreducible
                }

                task {
                    taskId = "#1"
                    startExpr = "[2 ^ 2] - 4 * 1 * 2 < 0"
                    explanation {
                        key = FallbackExplanation.CheckDiscriminantIsNegative
                    }

                    step {
                        fromExpr = "[2 ^ 2] - 4 * 1 * 2 < 0"
                        toExpr = "-4 < 0"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                    }

                    step {
                        fromExpr = "-4 < 0"
                        toExpr = "Identity[-4 < 0]"
                        explanation {
                            key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "/void/"
                    explanation {
                        key = FallbackExplanation.QuadraticIsIrreducibleBecauseDiscriminantIsNegative
                    }
                }
            }
        }

    @Test
    fun testQuadraticHasNegativeDiscriminantFails() =
        testMethod {
            method = FallbackPlans.QuadraticIsIrreducible
            inputExpr = "[x^2] - 3x + 2"

            check {
                noTransformation()
            }
        }
}
