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

package methods.inequalities

import engine.methods.testMethod
import methods.approximation.ApproximationExplanation
import org.junit.jupiter.api.Test

class ConstantInequalitiesTest {
    @Test
    fun `test trivial inequality 1`() =
        testMethod {
            method = InequalitiesPlans.SolveConstantInequality
            inputExpr = "2 >= 2"
            check {
                fromExpr = "2 >= 2"
                toExpr = "Identity[2 >= 2]"
                explanation {
                    key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                }
            }
        }

    @Test
    fun `test trivial inequality 2`() =
        testMethod {
            method = InequalitiesPlans.SolveConstantInequality
            inputExpr = "3 >= 2"
            check {
                fromExpr = "3 >= 2"
                toExpr = "Identity[3 >= 2]"
                explanation {
                    key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                }
            }
        }

    @Test
    fun `test true inequality with root`() =
        testMethod {
            method = InequalitiesPlans.SolveConstantInequality
            inputExpr = "1 + sqrt[2] < 2 + sqrt[2]"
            check {
                fromExpr = "1 + sqrt[2] < 2 + sqrt[2]"
                toExpr = "Identity[1 < 2]"
                explanation {
                    key = InequalitiesExplanation.SolveConstantInequality
                }
                step {
                    fromExpr = "1 + sqrt[2] < 2 + sqrt[2]"
                    toExpr = "1 < 2"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.CancelCommonTermsOnBothSides
                    }
                }
                step {
                    fromExpr = "1 < 2"
                    toExpr = "Identity[1 < 2]"
                    explanation {
                        key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                    }
                }
            }
        }

    @Test
    fun `test false inequality approximated`() =
        testMethod {
            method = InequalitiesPlans.SolveConstantInequality
            inputExpr = "2 < sqrt[2]"
            check {
                fromExpr = "2 < sqrt[2]"
                toExpr = "Contradiction[0.5857864376 < 0]"
                explanation {
                    key = InequalitiesExplanation.SolveConstantInequality
                }
                step {
                    fromExpr = "2 < sqrt[2]"
                    toExpr = "2 - sqrt[2] < 0"
                    explanation {
                        key = methods.solvable.InequalitiesExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }
                step {
                    fromExpr = "2 - sqrt[2] < 0"
                    toExpr = "0.5857864376 < 0"
                    explanation {
                        key = ApproximationExplanation.EvaluateExpressionNumerically
                    }
                }
                step {
                    fromExpr = "0.5857864376 < 0"
                    toExpr = "Contradiction[0.5857864376 < 0]"
                    explanation {
                        key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                    }
                }
            }
        }
}
