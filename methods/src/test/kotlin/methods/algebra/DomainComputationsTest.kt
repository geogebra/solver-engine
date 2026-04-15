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

package methods.algebra

import engine.methods.testMethod
import methods.inequations.InequationsExplanation
import methods.rationalexpressions.RationalExpressionsExplanation
import methods.simplify.SimplifyExplanation
import org.junit.jupiter.api.Test

class DomainComputationsTest {
    @Test
    fun `test computing the domain of an expression with a rational subexpression`() =
        testMethod {
            method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
            inputExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x]"

            check {
                fromExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x]"
                toExpr = "SetSolution[x : /reals/ \\ {-3, 0}]"
                explanation {
                    key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                }

                task {
                    taskId = "#1"
                    startExpr = "4 [x ^ 2] + 12 x != 0"
                    explanation {
                        key = AlgebraExplanation.ExpressionMustNotBeZero
                    }

                    step {
                        fromExpr = "4 [x ^ 2] + 12 x != 0"
                        toExpr = "SetSolution[x : /reals/ \\ {-3, 0}]"
                        explanation {
                            key = InequationsExplanation.SolveInequationInOneVariable
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "SetSolution[x : /reals/ \\ {-3, 0}]"
                    explanation {
                        key = AlgebraExplanation.CollectDomainRestrictions
                    }
                }
            }
        }

    @Test
    fun `test computing the domain of an expression with a non-constant divisor`() =
        testMethod {
            method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
            inputExpr = "[x ^ 2] : ([x ^ 2] - x + 1)"

            check {
                fromExpr = "[x ^ 2] : ([x ^ 2] - x + 1)"
                toExpr = "Identity[x : x = [1 +/- sqrt[-3] / 2]]"
                explanation {
                    key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                }

                task {
                    taskId = "#1"
                    startExpr = "[x ^ 2] - x + 1 != 0"
                    explanation {
                        key = AlgebraExplanation.ExpressionMustNotBeZero
                    }

                    step {
                        fromExpr = "[x ^ 2] - x + 1 != 0"
                        toExpr = "Identity[x : x = [1 +/- sqrt[-3] / 2]]"
                        explanation {
                            key = InequationsExplanation.SolveInequationInOneVariable
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "Identity[x : x = [1 +/- sqrt[-3] / 2]]"
                    explanation {
                        key = AlgebraExplanation.ExpressionIsDefinedEverywhere
                    }
                }
            }
        }

    @Test
    fun `test computing the domain of a multivariate expression`() =
        testMethod {
            method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
            inputExpr = "a b c : c f + c : (2 f + 1) + f : (3 f + 1)"

            check {
                fromExpr = "a b c : c f + c : (2 f + 1) + f : (3 f + 1)"
                toExpr = "c f != 0 AND SetSolution[f : /reals/ \\ {-[1 / 2], -[1 / 3]}]"
                explanation {
                    key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                }

                task {
                    taskId = "#1"
                    startExpr = "c f != 0"
                    explanation {
                        key = AlgebraExplanation.ExpressionMustNotBeZero
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "2 f + 1 != 0"
                    explanation {
                        key = AlgebraExplanation.ExpressionMustNotBeZero
                    }

                    step {
                        fromExpr = "2 f + 1 != 0"
                        toExpr = "SetSolution[f : /reals/ \\ {-[1 / 2]}]"
                        explanation {
                            key = InequationsExplanation.SolveInequationInOneVariable
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "3 f + 1 != 0"
                    explanation {
                        key = AlgebraExplanation.ExpressionMustNotBeZero
                    }

                    step {
                        fromExpr = "3 f + 1 != 0"
                        toExpr = "SetSolution[f : /reals/ \\ {-[1 / 3]}]"
                        explanation {
                            key = InequationsExplanation.SolveInequationInOneVariable
                        }
                    }
                }

                task {
                    taskId = "#4"
                    startExpr = "c f != 0 AND SetSolution[f : /reals/ \\ {-[1 / 2], -[1 / 3]}]"
                    explanation {
                        key = AlgebraExplanation.CollectDomainRestrictions
                    }
                }
            }
        }

    @Test
    fun `test computing the domain for equal denominators is done in the same task`() =
        testMethod {
            method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
            inputExpr = "[6 / x + 1] + [6 x / x + 1] + [3 / 2 x + 3]"

            check {
                fromExpr = "[6 / x + 1] + [6 x / x + 1] + [3 / 2 x + 3]"
                toExpr = "SetSolution[x: /reals/ \\ {-[3 / 2], -1}]"
                explanation {
                    key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                }

                task {
                    taskId = "#1"
                    startExpr = "x + 1 != 0"
                    explanation {
                        key = AlgebraExplanation.ExpressionMustNotBeZeroPlural
                        param { expr = "x + 1" }
                        param { expr = "[6 / x + 1], [6 x / x + 1]" }
                    }

                    step {
                        fromExpr = "x + 1 != 0"
                        toExpr = "SetSolution[x: /reals/ \\ {-1}]"
                        explanation {
                            key = InequationsExplanation.SolveInequationInOneVariable
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "2 x + 3 != 0"
                    explanation {
                        key = AlgebraExplanation.ExpressionMustNotBeZero
                    }

                    step {
                        fromExpr = "2 x + 3 != 0"
                        toExpr = "SetSolution[x: /reals/ \\ {-[3 / 2]}]"
                        explanation {
                            key = InequationsExplanation.SolveInequationInOneVariable
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: /reals/ \\ {-[3 / 2], -1}]"
                    explanation {
                        key = AlgebraExplanation.CollectDomainRestrictions
                    }
                }
            }
        }

    @Test
    fun `test computing the domain of a logarithmic expression`() =
        testMethod {
            method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
            inputExpr = "ln x"

            check {
                fromExpr = "ln x"
                explanation {
                    key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                }

                task {
                    taskId = "#1"
                    startExpr = "x > 0"
                    explanation {
                        key = AlgebraExplanation.LogArgumentMustBePositive
                    }
                }

                task {
                    taskId = "#2"
                    explanation {
                        key = AlgebraExplanation.CollectDomainRestrictions
                    }
                }
            }
        }

    @Test
    fun `test computing the domain of a logarithmic expression with a quadratic argument`() =
        testMethod {
            method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
            inputExpr = "log_[2] ([x ^ 2] - 3)"

            check {
                explanation {
                    key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                }

                task {
                    startExpr = "[x ^ 2] - 3 > 0"
                    explanation {
                        key = AlgebraExplanation.LogArgumentMustBePositive
                    }
                }

                task {
                    startExpr = "SetSolution[x: SetUnion[(-/infinity/, -sqrt[3]), (sqrt[3], /infinity/)]]"
                    explanation {
                        key = AlgebraExplanation.CollectDomainRestrictions
                    }
                }
            }
        }

    @Test
    fun `test computing the domain of multiple logarithms keeps checks grouped by expression`() =
        testMethod {
            method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
            inputExpr = "log_[a] x + log_[b] y"

            check {
                explanation {
                    key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                }

                task {
                    startExpr = "x > 0"
                    explanation {
                        key = AlgebraExplanation.LogArgumentMustBePositive
                    }
                }

                task {
                    startExpr = "a > 0"
                    explanation {
                        key = AlgebraExplanation.LogBaseMustBePositive
                    }
                }

                task {
                    startExpr = "a != 1"
                    explanation {
                        key = AlgebraExplanation.LogBaseMustNotEqualOne
                    }
                }

                task {
                    startExpr = "y > 0"
                    explanation {
                        key = AlgebraExplanation.LogArgumentMustBePositive
                    }
                }

                task {
                    startExpr = "b > 0"
                    explanation {
                        key = AlgebraExplanation.LogBaseMustBePositive
                    }
                }

                task {
                    startExpr = "b != 1"
                    explanation {
                        key = AlgebraExplanation.LogBaseMustNotEqualOne
                    }
                }

                task {
                    explanation {
                        key = AlgebraExplanation.CollectDomainRestrictions
                    }
                }
            }
        }

    @Test
    fun `test simplifying a logarithmic expression with an empty domain`() =
        testMethod {
            method = AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression
            inputExpr = "log_[2](-[x ^ 2] - 3)"

            check {
                fromExpr = "log_[2](-[x ^ 2] - 3)"
                toExpr = "Contradiction[x : [x ^ 2] + 3 < 0]"
                explanation {
                    key = AlgebraExplanation.ComputeDomainAndSimplifyAlgebraicExpression
                }

                task {
                    startExpr = "log_[2](-[x ^ 2] - 3)"
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                    }
                }
            }
        }

    @Test
    fun `test simplifying an algebraic expression after computing its domain`() =
        testMethod {
            method = AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression
            inputExpr = "[x / x + 1] + [x / x + 2]"

            check {
                fromExpr = "[x / x + 1] + [x / x + 2]"
                toExpr = "[2 [x ^ 2] + 3 x / (x + 1) (x + 2)] GIVEN SetSolution[x : /reals/ \\ {-2, -1}]"
                explanation {
                    key = AlgebraExplanation.ComputeDomainAndSimplifyAlgebraicExpression
                }

                task {
                    taskId = "#1"
                    startExpr = "[x / x + 1] + [x / x + 2]"
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                    }

                    step {
                        fromExpr = "[x / x + 1] + [x / x + 2]"
                        toExpr = "SetSolution[x : /reals/ \\ {-2, -1}]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[x / x + 1] + [x / x + 2]"
                    explanation {
                        key = SimplifyExplanation.SimplifyAlgebraicExpression
                    }

                    step {
                        fromExpr = "[x / x + 1] + [x / x + 2]"
                        toExpr = "[2 [x ^ 2] + 3 x / (x + 1) (x + 2)]"
                        explanation {
                            key = RationalExpressionsExplanation.AddRationalExpressions
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "[2 [x ^ 2] + 3 x / (x + 1) (x + 2)] GIVEN SetSolution[x : /reals/ \\ {-2, -1}]"
                    explanation {
                        key = AlgebraExplanation.CombineSimplifiedExpressionWithConstraint
                    }
                }
            }
        }
}
