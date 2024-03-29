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
import methods.equationsystems.EquationSystemsExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class EquationWithTwoAbsoluteValuesTest {
    @Test
    fun `test negative absolute values`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "-abs[x + 1] = -2 * abs[x - 2]"

            check {
                fromExpr = "-abs[x + 1] = -2 * abs[x - 2]"
                toExpr = "SetSolution[x: {1, 5}]"
                explanation {
                    key = EquationsExplanation.SolveEquationWithTwoAbsoluteValues
                }
                step {
                    fromExpr = "-abs[x + 1] = -2 * abs[x - 2]"
                    toExpr = "abs[x + 1] = 2 * abs[x - 2]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.NegateBothSides
                    }
                }
                step {
                    fromExpr = "abs[x + 1] = 2 * abs[x - 2]"
                    toExpr = "x + 1 = 2 (x - 2) OR x + 1 = -2 (x - 2)"
                    explanation {
                        key = EquationsExplanation.SeparateModulusEqualsPositiveConstant
                    }
                }
                step {
                    fromExpr = "x + 1 = 2 (x - 2) OR x + 1 = -2 (x - 2)"
                    toExpr = "SetSolution[x: {1, 5}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }
                }
            }
        }

    @Test
    fun `test system equations have common solution`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "abs[[x ^ 2] - x] + abs[[x ^ 3] - 1] = 0"

            check {
                fromExpr = "abs[[x ^ 2] - x] + abs[[x ^ 3] - 1] = 0"
                toExpr = "SetSolution[x: {1}]"
                explanation {
                    key = EquationsExplanation.SolveEquationWithTwoAbsoluteValues
                }
                step {
                    fromExpr = "abs[[x ^ 2] - x] + abs[[x ^ 3] - 1] = 0"
                    toExpr = "abs[[x ^ 2] - x] = -abs[[x ^ 3] - 1]"
                    explanation {
                        key = EquationsExplanation.MoveOneModulusToOtherSideAndSimplify
                    }
                    step {
                        fromExpr = "abs[[x ^ 2] - x] + abs[[x ^ 3] - 1] = 0"
                        toExpr = "abs[[x ^ 2] - x] + abs[[x ^ 3] - 1] - abs[[x ^ 3] - 1] = -abs[[x ^ 3] - 1]"
                        explanation {
                            key = EquationsExplanation.MoveSecondModulusToRhs
                        }
                    }
                    step {
                        fromExpr = "abs[[x ^ 2] - x] + abs[[x ^ 3] - 1] - abs[[x ^ 3] - 1] = -abs[[x ^ 3] - 1]"
                        toExpr = "abs[[x ^ 2] - x] = -abs[[x ^ 3] - 1]"
                        explanation {
                            key = GeneralExplanation.CancelAdditiveInverseElements
                        }
                    }
                }
                step {
                    fromExpr = "abs[[x ^ 2] - x] = -abs[[x ^ 3] - 1]"
                    toExpr = "[x ^ 2] - x = 0 AND [x ^ 3] - 1 = 0"
                    explanation {
                        key = EquationsExplanation.ResolveModulusEqualsNegativeModulus
                    }
                }
                step {
                    fromExpr = "[x ^ 2] - x = 0 AND [x ^ 3] - 1 = 0"
                    toExpr = "SetSolution[x: {1}]"
                    explanation {
                        key = EquationSystemsExplanation.SolveEquationSystemInOneVariable
                    }
                }
            }
        }

    @Test
    fun `test system equations have no common solution`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "3 * abs[x + 1] + 1 = 1 - abs[x - 1]"

            check {
                fromExpr = "3 * abs[x + 1] + 1 = 1 - abs[x - 1]"
                toExpr = "Contradiction[x: 3 (x + 1) = 0 AND x - 1 = 0]"
                explanation {
                    key = EquationsExplanation.SolveEquationWithTwoAbsoluteValues
                }
                step {
                    fromExpr = "3 * abs[x + 1] + 1 = 1 - abs[x - 1]"
                    toExpr = "3 * abs[x + 1] = -abs[x - 1]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                    }
                }
                step {
                    fromExpr = "3 * abs[x + 1] = -abs[x - 1]"
                    toExpr = "3 (x + 1) = 0 AND x - 1 = 0"
                    explanation {
                        key = EquationsExplanation.ResolveModulusEqualsNegativeModulus
                    }
                }
                step {
                    fromExpr = "3 (x + 1) = 0 AND x - 1 = 0"
                    toExpr = "Contradiction[x: 3 (x + 1) = 0 AND x - 1 = 0]"
                    explanation {
                        key = EquationSystemsExplanation.SolveEquationSystemInOneVariable
                    }
                }
            }
        }
}
