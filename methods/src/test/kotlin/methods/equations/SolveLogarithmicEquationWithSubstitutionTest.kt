/*
 * Copyright (c) 2026 GeoGebra GmbH, office@geogebra.org
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
import methods.algebra.AlgebraExplanation
import methods.expand.ExpandExplanation
import methods.logs.LogsExplanation
import kotlin.test.Test

class SolveLogarithmicEquationWithSubstitutionTest {
    @Suppress("LongMethod")
    @Test
    fun `solve quadratic equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "5 * log x * (log x - 1) = 2 (log x + 6)"

            check {
                task {
                    step {
                        toExpr = "SetSolution[x: (0, /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfEquation
                        }
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquationBySubstitution
                        }

                        step {
                            toExpr = "5 * log x * (log x - 1) = 2 * log x + 12"
                            explanation {
                                key = ExpandExplanation.ExpandSingleBracketAndSimplify
                            }
                        }

                        step {
                            toExpr = "5 * [log ^ 2] x - 5 * log x = 2 * log x + 12"
                            explanation {
                                key = ExpandExplanation.ExpandSingleBracketAndSimplify
                            }
                        }

                        step {
                            toExpr = "5 * [log ^ 2] x - 7 * log x - 12 = 0"
                        }

                        step {
                            toExpr = "5 [t ^ 2] - 7 t - 12 = 0 AND t = log x"
                            explanation {
                                key = LogsExplanation.SubstituteLogsInEquation
                            }
                        }

                        step {
                            toExpr = "SetSolution[t: {-1, [12 / 5]}] AND t = log x"
                            explanation {
                                key = EquationsExplanation.SolveEquationByFactoring
                            }
                        }

                        step {
                            explanation {
                                key = LogsExplanation.SubstituteOriginalExpressionIntoLogarithmicEquation
                            }

                            task {
                                explanation {
                                    key = EquationsExplanation.SolveLogarithmicEquations
                                }

                                step {
                                    fromExpr = "log x = -1"
                                    toExpr = "SetSolution[x: {[1 / 10]}]"
                                    explanation {
                                        key = EquationsExplanation.SolveLogarithmicEquations
                                    }
                                }
                            }

                            task {
                                explanation {
                                    key = EquationsExplanation.SolveLogarithmicEquations
                                }

                                step {
                                    fromExpr = "log x = [12 / 5]"
                                    toExpr = "SetSolution[x: {100 * [10 ^ [2 / 5]]}]"
                                    explanation {
                                        key = EquationsExplanation.SolveLogarithmicEquations
                                    }
                                }
                            }

                            task {
                                startExpr = "SetSolution[x: {[1 / 10], 100 * [10 ^ [2 / 5]]}]"
                                explanation {
                                    key = EquationsExplanation.CollectSolutions
                                }
                            }
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: {[1 / 10], 100 * [10 ^ [2 / 5]]}] GIVEN SetSolution[x: (0, /infinity/)]"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }

                    step {
                        toExpr = "SetSolution[x: {[1 / 10], 100 * [10 ^ [2 / 5]]}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `reorder and solve quadratic equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "0 = log_[2] [x ^ 2] + [log_[2] ^ 2] x"

            check {
                task {
                    step {
                        toExpr = "SetSolution[x: (0, /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfEquation
                        }
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquationBySubstitution
                        }

                        step {
                            toExpr = "0 = 2 * log_[2] x + [log_[2] ^ 2] x"
                        }

                        step {
                            toExpr = "2 * log_[2] x + [log_[2] ^ 2] x = 0"
                        }

                        step {
                            toExpr = "[log_[2] ^ 2] x + 2 * log_[2] x + 0 = 0"
                        }

                        step {
                            toExpr = "[t ^ 2] + 2 t + 0 = 0 AND t = log_[2] x"
                        }

                        step {
                            toExpr = "SetSolution[t: {-2, 0}] AND t = log_[2] x"
                            explanation {
                                key = EquationsExplanation.SolveEquationByFactoring
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: {[1 / 4], 1}]"
                            explanation {
                                key = LogsExplanation.SubstituteOriginalExpressionIntoLogarithmicEquation
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: {[1 / 4], 1}] GIVEN SetSolution[x: (0, /infinity/)]"

                    step {
                        toExpr = "SetSolution[x: {[1 / 4], 1}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `solve trinomial with odd n`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 * [log ^ 6] x + 5 * [log ^ 3] x - 3 = 0"

            check {
                task {
                    step {
                        toExpr = "SetSolution[x: (0, /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfEquation
                        }
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquationBySubstitution
                        }

                        step {
                            toExpr = "2 [t ^ 2] + 5 t - 3 = 0 AND t = [log ^ 3] x"
                            explanation {
                                key = LogsExplanation.SubstituteLogsInEquation
                            }
                        }

                        step {
                            toExpr = "SetSolution[t: {-3, [1 / 2]}] AND t = [log ^ 3] x"
                            explanation {
                                key = EquationsExplanation.SolveEquationByFactoring
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: {[10 ^ -root[3, 3]], [10 ^ [root[4, 3] / 2]]}]"
                            explanation {
                                key = LogsExplanation.SubstituteOriginalExpressionIntoLogarithmicEquation
                            }

                            task {
                                explanation {
                                    key = EquationsExplanation.SolveLogarithmicEquations
                                }

                                step {
                                    fromExpr = "[log ^ 3] x = -3"
                                    toExpr = "SetSolution[x: {[10 ^ -root[3, 3]]}]"
                                    explanation {
                                        key = EquationsExplanation.SolveEquationUsingRootsMethod
                                    }
                                }
                            }

                            task {
                                explanation {
                                    key = EquationsExplanation.SolveLogarithmicEquations
                                }

                                step {
                                    fromExpr = "[log ^ 3] x = [1 / 2]"
                                    toExpr = "SetSolution[x: {[10 ^ [root[4, 3] / 2]]}]"
                                    explanation {
                                        key = EquationsExplanation.SolveEquationUsingRootsMethod
                                    }
                                }
                            }

                            task {
                                startExpr = "SetSolution[x: {[10 ^ -root[3, 3]], [10 ^ [root[4, 3] / 2]]}]"
                                explanation {
                                    key = EquationsExplanation.CollectSolutions
                                }
                            }
                        }
                    }
                }

                task {
                    startExpr =
                        "SetSolution[x: {[10 ^ -root[3, 3]], [10 ^ [root[4, 3] / 2]]}] " +
                        "GIVEN SetSolution[x: (0, /infinity/)]"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }

                    step {
                        toExpr = "SetSolution[x: {[10 ^ -root[3, 3]], [10 ^ [root[4, 3] / 2]]}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `solve trinomial with even n`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 * [log ^ 4] x + 5 * [log ^ 2] x - 3 = 0"

            check {
                task {
                    taskId = "#1"

                    step {
                        toExpr = "SetSolution[x: (0, /infinity/)]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfEquation
                        }
                    }
                }

                task {
                    step {
                        explanation {
                            key = EquationsExplanation.SolveLogarithmicEquationBySubstitution
                        }

                        step {
                            toExpr = "2 [t ^ 2] + 5 t - 3 = 0 AND t = [log ^ 2] x"
                            explanation {
                                key = LogsExplanation.SubstituteLogsInEquation
                            }
                        }

                        step {
                            toExpr = "SetSolution[t: {-3, [1 / 2]}] AND t = [log ^ 2] x"
                            explanation {
                                key = EquationsExplanation.SolveEquationByFactoring
                            }
                        }

                        step {
                            toExpr = "SetSolution[x: {[10 ^ -[sqrt[2] / 2]], [10 ^ [sqrt[2] / 2]]}]"
                            explanation {
                                key = LogsExplanation.SubstituteOriginalExpressionIntoLogarithmicEquation
                            }

                            task {
                                explanation {
                                    key = EquationsExplanation.SolveLogarithmicEquations
                                }

                                step {
                                    fromExpr = "[log ^ 2] x = -3"
                                    toExpr = "Contradiction[x: [log ^ 2] x = -3]"
                                }
                            }

                            task {
                                explanation {
                                    key = EquationsExplanation.SolveLogarithmicEquations
                                }

                                step {
                                    fromExpr = "[log ^ 2] x = [1 / 2]"
                                    toExpr = "SetSolution[x: {[10 ^ -[sqrt[2] / 2]], [10 ^ [sqrt[2] / 2]]}]"
                                }
                            }

                            task {
                                startExpr = "SetSolution[x: {[10 ^ -[sqrt[2] / 2]], [10 ^ [sqrt[2] / 2]]}]"
                                explanation {
                                    key = EquationsExplanation.CollectSolutions
                                }
                            }
                        }
                    }
                }

                task {
                    startExpr = "SetSolution[x: {[10 ^ -[sqrt[2] / 2]], [10 ^ [sqrt[2] / 2]]}] " +
                        "GIVEN SetSolution[x: (0, /infinity/)]"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }

                    step {
                        toExpr = "SetSolution[x: {[10 ^ -[sqrt[2] / 2]], [10 ^ [sqrt[2] / 2]]}]"
                        explanation {
                            key = EquationsExplanation.GatherSolutionsAndConstraint
                        }
                    }
                }
            }
        }
    }
}
