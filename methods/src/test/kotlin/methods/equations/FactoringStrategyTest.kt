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

import engine.methods.getPlan
import engine.methods.testMethodInX
import methods.equations.EquationSolvingStrategy.Factoring
import methods.factor.FactorExplanation
import org.junit.jupiter.api.Test

class FactoringStrategyTest {
    private val factoringPlan = Factoring.getPlan()

    @Test
    fun `test solving irreducible polynomial by factoring fails`() =
        testMethodInX {
            method = factoringPlan
            inputExpr = "[x^2] + x + 1 = 0"

            check {
                noTransformation()
            }
        }

    @Test
    fun `test solving quadratic equation by factoring it by guessing`() =
        testMethodInX {
            method = factoringPlan
            inputExpr = "[x ^ 2] + 5 x + 6 = 0"

            check {
                fromExpr = "[x ^ 2] + 5 x + 6 = 0"
                toExpr = "SetSolution[x: {-3, -2}]"
                explanation {
                    key = EquationsExplanation.SolveEquationByFactoring
                }

                step {
                    fromExpr = "[x ^ 2] + 5 x + 6 = 0"
                    toExpr = "(x + 2) (x + 3) = 0"
                    explanation {
                        key = FactorExplanation.FactorPolynomial
                    }
                }

                step {
                    fromExpr = "(x + 2) (x + 3) = 0"
                    toExpr = "x + 2 = 0 OR x + 3 = 0"
                    explanation {
                        key = EquationsExplanation.SeparateFactoredEquation
                    }
                }

                step {
                    toExpr = "SetSolution[x: {-3, -2}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }

                    task {
                        taskId = "#1"
                        startExpr = "x + 2 = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "x + 2 = 0"
                            toExpr = "SetSolution[x: {-2}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x + 3 = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "x + 3 = 0"
                            toExpr = "SetSolution[x: {-3}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }

                    task {
                        taskId = "#3"
                        startExpr = "SetSolution[x: {-3, -2}]"
                        explanation {
                            key = EquationsExplanation.CollectSolutions
                        }
                    }
                }
            }
        }

    @Test
    fun `test solving simple quadratic equation by factoring`() =
        testMethodInX {
            method = factoringPlan
            inputExpr = "[x ^ 2] - 4 = 0"

            check {
                fromExpr = "[x ^ 2] - 4 = 0"
                toExpr = "SetSolution[x: {-2, 2}]"
                explanation {
                    key = EquationsExplanation.SolveEquationByFactoring
                }

                step {
                    fromExpr = "[x ^ 2] - 4 = 0"
                    toExpr = "(x - 2) (x + 2) = 0"
                    explanation {
                        key = FactorExplanation.FactorPolynomial
                    }
                }

                step {
                    fromExpr = "(x - 2) (x + 2) = 0"
                    toExpr = "x - 2 = 0 OR x + 2 = 0"
                    explanation {
                        key = EquationsExplanation.SeparateFactoredEquation
                    }
                }

                step {
                    fromExpr = "x - 2 = 0 OR x + 2 = 0"
                    toExpr = "SetSolution[x: {-2, 2}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }

                    task {
                        taskId = "#1"
                        startExpr = "x - 2 = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "x - 2 = 0"
                            toExpr = "SetSolution[x: {2}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x + 2 = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "x + 2 = 0"
                            toExpr = "SetSolution[x: {-2}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }

                    task {
                        taskId = "#3"
                        startExpr = "SetSolution[x: {-2, 2}]"
                        explanation {
                            key = EquationsExplanation.CollectSolutions
                        }
                    }
                }
            }
        }

    @Test
    fun `test solving tricky higher order equation by factoring`() =
        testMethodInX {
            method = factoringPlan
            inputExpr = "[x ^ 6] = 5 [x ^ 5] - 3 [x ^ 4]"

            check {
                fromExpr = "[x ^ 6] = 5 [x ^ 5] - 3 [x ^ 4]"
                toExpr = "SetSolution[x: {0, [5 - sqrt[13] / 2], [5 + sqrt[13] / 2]}]"
                explanation {
                    key = EquationsExplanation.SolveEquationByFactoring
                }

                step {
                    fromExpr = "[x ^ 6] = 5 [x ^ 5] - 3 [x ^ 4]"
                    toExpr = "[x ^ 6] - 5 [x ^ 5] + 3 [x ^ 4] = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "[x ^ 6] - 5 [x ^ 5] + 3 [x ^ 4] = 0"
                    toExpr = "[x ^ 4] ([x ^ 2] - 5 x + 3) = 0"
                    explanation {
                        key = FactorExplanation.FactorPolynomial
                    }
                }

                step {
                    fromExpr = "[x ^ 4] ([x ^ 2] - 5 x + 3) = 0"
                    toExpr = "[x ^ 4] = 0 OR [x ^ 2] - 5 x + 3 = 0"
                    explanation {
                        key = EquationsExplanation.SeparateFactoredEquation
                    }
                }

                step {
                    toExpr = "SetSolution[x: {0, [5 - sqrt[13] / 2], [5 + sqrt[13] / 2]}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[x ^ 4] = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "[x ^ 4] = 0"
                            toExpr = "SetSolution[x: {0}]"
                            explanation {
                                key = EquationsExplanation.SolveEquationUsingRootsMethod
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[x ^ 2] - 5 x + 3 = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "[x ^ 2] - 5 x + 3 = 0"
                            toExpr = "SetSolution[x: {[5 - sqrt[13] / 2], [5 + sqrt[13] / 2]}]"
                            explanation {
                                key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                            }
                        }
                    }

                    task {
                        taskId = "#3"
                        startExpr = "SetSolution[x: {0, [5 - sqrt[13] / 2], [5 + sqrt[13] / 2]}]"
                        explanation {
                            key = EquationsExplanation.CollectSolutions
                        }
                    }
                }
            }
        }

    @Test
    fun `test solving higher order equation which factors into multiple terms`() =
        testMethodInX {
            method = factoringPlan
            inputExpr = "[x ^ 6] - [x ^ 2] = 0"

            check {
                fromExpr = "[x ^ 6] - [x ^ 2] = 0"
                toExpr = "SetSolution[x: {-1, 0, 1}]"
                explanation {
                    key = EquationsExplanation.SolveEquationByFactoring
                }

                step {
                    fromExpr = "[x ^ 6] - [x ^ 2] = 0"
                    toExpr = "[x ^ 2] (x - 1) (x + 1) ([x ^ 2] + 1) = 0"
                    explanation {
                        key = FactorExplanation.FactorPolynomial
                    }
                }

                step {
                    fromExpr = "[x ^ 2] (x - 1) (x + 1) ([x ^ 2] + 1) = 0"
                    toExpr = "[x ^ 2] = 0 OR x - 1 = 0 OR x + 1 = 0 OR [x ^ 2] + 1 = 0"
                    explanation {
                        key = EquationsExplanation.SeparateFactoredEquation
                    }
                }

                step {
                    toExpr = "SetSolution[x: {-1, 0, 1}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[x ^ 2] = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "[x ^ 2] = 0"
                            toExpr = "SetSolution[x: {0}]"
                            explanation {
                                key = EquationsExplanation.SolveEquationUsingRootsMethod
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x - 1 = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "x - 1 = 0"
                            toExpr = "SetSolution[x: {1}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }

                    task {
                        taskId = "#3"
                        startExpr = "x + 1 = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "x + 1 = 0"
                            toExpr = "SetSolution[x: {-1}]"
                            explanation {
                                key = EquationsExplanation.SolveLinearEquation
                            }
                        }
                    }

                    task {
                        taskId = "#4"
                        startExpr = "[x ^ 2] + 1 = 0"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "[x ^ 2] + 1 = 0"
                            toExpr = "Contradiction[x: [x ^ 2] = -1]"
                            explanation {
                                key = EquationsExplanation.SolveEquationUsingRootsMethod
                            }
                        }
                    }

                    task {
                        taskId = "#5"
                        startExpr = "SetSolution[x: {-1, 0, 1}]"
                        explanation {
                            key = EquationsExplanation.CollectSolutions
                        }
                    }
                }
            }
        }

    @Test
    fun `test constant factor is removed first`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "-11 ([x ^ 2] + x) = 0"

            check {
                fromExpr = "-11 ([x ^ 2] + x) = 0"
                toExpr = "SetSolution[x : {-1, 0}]"
                explanation {
                    key = EquationsExplanation.SolveEquationByFactoring
                }

                step {
                    fromExpr = "-11 ([x ^ 2] + x) = 0"
                    toExpr = "[x ^ 2] + x = 0"
                    explanation {
                        key = EquationsExplanation.EliminateConstantFactorOfLhsWithZeroRhs
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + x = 0"
                    toExpr = "x (x + 1) = 0"
                    explanation {
                        key = FactorExplanation.FactorPolynomial
                    }
                }

                step {
                    fromExpr = "x (x + 1) = 0"
                    toExpr = "x = 0 OR x + 1 = 0"
                    explanation {
                        key = EquationsExplanation.SeparateFactoredEquation
                    }
                }

                step {
                    fromExpr = "x = 0 OR x + 1 = 0"
                    toExpr = "SetSolution[x : {-1, 0}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }
                }
            }
        }
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 [x ^ 2] + 4 x = 0"

            check {
                fromExpr = "2 [x ^ 2] + 4 x = 0"
                toExpr = "SetSolution[x: {-2, 0}]"
                explanation {
                    key = EquationsExplanation.SolveEquationByFactoring
                }

                step {
                    fromExpr = "2 [x ^ 2] + 4 x = 0"
                    toExpr = "2 x (x + 2) = 0"
                    explanation {
                        key = FactorExplanation.FactorPolynomial
                    }
                }

                step {
                    fromExpr = "2 x (x + 2) = 0"
                    toExpr = "x (x + 2) = 0"
                    explanation {
                        key = EquationsExplanation.EliminateConstantFactorOfLhsWithZeroRhs
                    }
                }

                step {
                    fromExpr = "x (x + 2) = 0"
                    toExpr = "x = 0 OR x + 2 = 0"
                    explanation {
                        key = EquationsExplanation.SeparateFactoredEquation
                    }
                }

                step {
                    fromExpr = "x = 0 OR x + 2 = 0"
                    toExpr = "SetSolution[x: {-2, 0}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }
                }
            }
        }
    }
}
