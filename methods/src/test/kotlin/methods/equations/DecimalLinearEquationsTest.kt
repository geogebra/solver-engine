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
import methods.decimals.DecimalsExplanation
import org.junit.jupiter.api.Test

class DecimalLinearEquationsTest {
    @Test
    fun `test ax + b = cx + d decimal linear equation`() =
        testMethodInX {
            method = EquationsPlans.SolveDecimalLinearEquation
            inputExpr = "3.1 x + 2.2 = 2.9 x - 9.34"

            check {
                fromExpr = "3.1 x + 2.2 = 2.9 x - 9.34"
                toExpr = "SetSolution[x: {-57.7}]"
                explanation {
                    key = EquationsExplanation.SolveDecimalLinearEquation
                }

                step {
                    fromExpr = "3.1 x + 2.2 = 2.9 x - 9.34"
                    toExpr = "0.2 x + 2.2 = -9.34"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "0.2 x + 2.2 = -9.34"
                    toExpr = "0.2 x = -11.54"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                    }
                }

                step {
                    fromExpr = "0.2 x = -11.54"
                    toExpr = "x = -57.7"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                    }
                }

                step {
                    fromExpr = "x = -57.7"
                    toExpr = "SetSolution[x: {-57.7}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test decimal linear equation with fractions in the initial expression`() =
        testMethodInX {
            method = EquationsPlans.SolveDecimalLinearEquation
            inputExpr = "3.6 x + 2.2 = [2 / 5] x + 1.2"

            check {
                fromExpr = "3.6 x + 2.2 = [2 / 5] x + 1.2"
                toExpr = "SetSolution[x: {-0.3125}]"
                explanation {
                    key = EquationsExplanation.SolveDecimalLinearEquation
                }

                step {
                    fromExpr = "3.6 x + 2.2 = [2 / 5] x + 1.2"
                    toExpr = "3.6 x + 2.2 = 0.4 x + 1.2"
                    explanation {
                        key = DecimalsExplanation.ConvertNiceFractionToDecimal
                    }
                }

                step {
                    fromExpr = "3.6 x + 2.2 = 0.4 x + 1.2"
                    toExpr = "3.2 x + 2.2 = 1.2"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "3.2 x + 2.2 = 1.2"
                    toExpr = "3.2 x = -1"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                    }
                }

                step {
                    fromExpr = "3.2 x = -1"
                    toExpr = "x = -0.3125"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                    }
                }

                step {
                    fromExpr = "x = -0.3125"
                    toExpr = "SetSolution[x: {-0.3125}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }
}
