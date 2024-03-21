/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
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
import methods.expand.ExpandExplanation
import methods.general.GeneralExplanation
import methods.logs.LogsExplanation
import org.junit.jupiter.api.Test

class ExponentialEquationsTest {
    @Test
    fun `test same base`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[2 ^ x] = [2 ^ 1 - x]"

            check {
                fromExpr = "[2 ^ x] = [2 ^ 1 - x]"
                toExpr = "SetSolution[x: {[1 / 2]}]"
                explanation {
                    key = EquationsExplanation.SolveLinearEquation
                }

                step {
                    fromExpr = "[2 ^ x] = [2 ^ 1 - x]"
                    toExpr = "x = 1 - x"
                    explanation {
                        key = methods.solvable.EquationsExplanation.CancelCommonBase
                    }
                }

                step {
                    fromExpr = "x = 1 - x"
                    toExpr = "2 x = 1"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "2 x = 1"
                    toExpr = "x = [1 / 2]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                    }
                }

                step {
                    fromExpr = "x = [1 / 2]"
                    toExpr = "SetSolution[x: {[1 / 2]}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test take log of RHS`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[3 ^ x + 1] = 5"

            check {
                fromExpr = "[3 ^ x + 1] = 5"
                toExpr = "SetSolution[x: {log_[3] 5 - 1}]"
                explanation {
                    key = EquationsExplanation.SolveExponentialEquation
                }

                step {
                    fromExpr = "[3 ^ x + 1] = 5"
                    toExpr = "x + 1 = log_[3] 5"
                    explanation {
                        key = methods.solvable.EquationsExplanation.TakeLogOfRHS
                    }
                }

                step {
                    fromExpr = "x + 1 = log_[3] 5"
                    toExpr = "x = log_[3] 5 - 1"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                    }
                }

                step {
                    fromExpr = "x = log_[3] 5 - 1"
                    toExpr = "SetSolution[x: {log_[3] 5 - 1}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test log of both sides`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[2 ^ x + 1] = [3 ^ x]"

            check {
                fromExpr = "[2 ^ x + 1] = [3 ^ x]"
                toExpr = "SetSolution[x: {-[ln 2 / ln 2 - ln 3]}]"
                explanation {
                    key = EquationsExplanation.SolveExponentialEquation
                }

                step {
                    fromExpr = "[2 ^ x + 1] = [3 ^ x]"
                    toExpr = "(x + 1) ln 2 = x * ln 3"
                    explanation {
                        key = methods.solvable.EquationsExplanation.TakeLogOfBothSidesAndSimplify
                    }

                    step {
                        fromExpr = "[2 ^ x + 1] = [3 ^ x]"
                        toExpr = "ln [2 ^ x + 1] = ln [3 ^ x]"
                        explanation {
                            key = methods.solvable.EquationsExplanation.TakeLogOfBothSides
                        }
                    }

                    step {
                        fromExpr = "ln [2 ^ x + 1] = ln [3 ^ x]"
                        toExpr = "(x + 1) ln 2 = ln [3 ^ x]"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }

                        step {
                            fromExpr = "ln [2 ^ x + 1]"
                            toExpr = "(x + 1) ln 2"
                            explanation {
                                key = LogsExplanation.TakePowerOutOfLog
                            }
                        }
                    }

                    step {
                        fromExpr = "(x + 1) ln 2 = ln [3 ^ x]"
                        toExpr = "(x + 1) ln 2 = x * ln 3"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }

                        step {
                            fromExpr = "ln [3 ^ x]"
                            toExpr = "x * ln 3"
                            explanation {
                                key = LogsExplanation.TakePowerOutOfLog
                            }
                        }
                    }
                }

                step {
                    fromExpr = "(x + 1) ln 2 = x * ln 3"
                    toExpr = "x * ln 2 + ln 2 = x * ln 3"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "(x + 1) ln 2"
                        toExpr = "x * ln 2 + 1 * ln 2"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "x * ln 2 + 1 * ln 2"
                        toExpr = "x * ln 2 + ln 2"
                        explanation {
                            key = GeneralExplanation.RemoveUnitaryCoefficient
                        }
                    }
                }

                step {
                    fromExpr = "x * ln 2 + ln 2 = x * ln 3"
                    toExpr = "x (ln 2 - ln 3) + ln 2 = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "x (ln 2 - ln 3) + ln 2 = 0"
                    toExpr = "x (ln 2 - ln 3) = -ln 2"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                    }
                }

                step {
                    fromExpr = "x (ln 2 - ln 3) = -ln 2"
                    toExpr = "x = -[ln 2 / ln 2 - ln 3]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                    }
                }

                step {
                    fromExpr = "x = -[ln 2 / ln 2 - ln 3]"
                    toExpr = "SetSolution[x: {-[ln 2 / ln 2 - ln 3]}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test no solution`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[2 ^ x] = -3"

            check {
                fromExpr = "[2 ^ x] = -3"
                toExpr = "Contradiction[x: [2 ^ x] = -3]"
                explanation {
                    key = EquationsExplanation.EquationSidesHaveIncompatibleSigns
                }
            }
        }
}
