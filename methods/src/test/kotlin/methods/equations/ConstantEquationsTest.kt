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

import engine.methods.testMethod
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class ConstantEquationsTest {
    @Test
    fun `test simple constant equation resulting in falsehood`() =
        testMethod {
            method = EquationsPlans.SolveConstantEquation
            inputExpr = "abs[1 + sqrt[2]] = 0"

            check {
                fromExpr = "abs[1 + sqrt[2]] = 0"
                toExpr = "Contradiction[1 + sqrt[2] = 0]"
                explanation {
                    key = EquationsExplanation.SolveConstantEquation
                }

                step {
                    fromExpr = "abs[1 + sqrt[2]] = 0"
                    toExpr = "1 + sqrt[2] = 0"
                    explanation {
                        key = GeneralExplanation.EvaluateAbsoluteValue
                    }
                }

                step {
                    fromExpr = "1 + sqrt[2] = 0"
                    toExpr = "Contradiction[1 + sqrt[2] = 0]"
                    explanation {
                        key = EquationsExplanation.ExtractFalsehoodFromFalseEquality
                    }
                }
            }
        }

    @Test
    fun `test simple constant equation resulting in truth`() =
        testMethod {
            method = EquationsPlans.SolveConstantEquation
            inputExpr = "1 + sqrt[2] = 1 + sqrt[2]"

            check {
                fromExpr = "1 + sqrt[2] = 1 + sqrt[2]"
                toExpr = "Identity[1 + sqrt[2] = 1 + sqrt[2]]"
                explanation {
                    key = EquationsExplanation.ExtractTruthFromTrueEquality
                }
            }
        }

    @Test
    fun `test constant equation requiring simplification and resulting in falsehood`() =
        testMethod {
            method = EquationsPlans.SolveConstantEquation
            inputExpr = "5 + [(1 + sqrt[2]) ^ 2] = [(2 + sqrt[2]) ^ 2] + 1"

            check {
                fromExpr = "5 + [(1 + sqrt[2]) ^ 2] = [(2 + sqrt[2]) ^ 2] + 1"
                toExpr = "Contradiction[1 - 2 sqrt[2] = 0]"
                explanation {
                    key = EquationsExplanation.SolveConstantEquation
                }

                step {
                    fromExpr = "5 + [(1 + sqrt[2]) ^ 2] = [(2 + sqrt[2]) ^ 2] + 1"
                    toExpr = "8 + 2 sqrt[2] = 7 + 4 sqrt[2]"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }

                step {
                    fromExpr = "8 + 2 sqrt[2] = 7 + 4 sqrt[2]"
                    toExpr = "1 - 2 sqrt[2] = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "1 - 2 sqrt[2] = 0"
                    toExpr = "Contradiction[1 - 2 sqrt[2] = 0]"
                    explanation {
                        key = EquationsExplanation.ExtractFalsehoodFromFalseEquality
                    }
                }
            }
        }

    @Test
    fun `test constant equation requiring simplification resulting in truth`() =
        testMethod {
            method = EquationsPlans.SolveConstantEquation
            inputExpr = "[(2 + sqrt[2]) ^ 2] + 1 - 3 = 4 (1 + sqrt[2])"

            check {
                fromExpr = "[(2 + sqrt[2]) ^ 2] + 1 - 3 = 4 (1 + sqrt[2])"
                toExpr = "Identity[4 + 4 sqrt[2] = 4 + 4 sqrt[2]]"
                explanation {
                    key = EquationsExplanation.SolveConstantEquation
                }

                step {
                    fromExpr = "[(2 + sqrt[2]) ^ 2] + 1 - 3 = 4 (1 + sqrt[2])"
                    toExpr = "4 + 4 sqrt[2] = 4 + 4 sqrt[2]"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }

                step {
                    fromExpr = "4 + 4 sqrt[2] = 4 + 4 sqrt[2]"
                    toExpr = "Identity[4 + 4 sqrt[2] = 4 + 4 sqrt[2]]"
                    explanation {
                        key = EquationsExplanation.ExtractTruthFromTrueEquality
                    }
                }
            }
        }

    @Test
    fun `test undefined constant equation is false`() =
        testMethod {
            method = EquationsPlans.SolveConstantEquation
            inputExpr = "2 = [1 / 1 - 1]"

            check {
                fromExpr = "2 = [1 / 1 - 1]"
                toExpr = "Contradiction[/undefined/]"
                explanation {
                    key = EquationsExplanation.SolveConstantEquation
                }

                step {
                    fromExpr = "2 = [1 / 1 - 1]"
                    toExpr = "/undefined/"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }

                step {
                    fromExpr = "/undefined/"
                    toExpr = "Contradiction[/undefined/]"
                    explanation {
                        key = EquationsExplanation.UndefinedConstantEquationIsFalse
                    }
                }
            }
        }
}
