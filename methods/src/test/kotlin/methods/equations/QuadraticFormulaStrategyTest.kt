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

import engine.context.Context
import engine.context.strategyChoice
import engine.methods.MethodTestCase
import methods.collecting.CollectingExplanation
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.equations.EquationSolvingStrategy.QuadraticFormula
import methods.factor.FactorExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.polynomials.PolynomialsExplanation
import methods.simplify.SimplifyExplanation
import org.junit.jupiter.api.Test

@Suppress("LargeClass", "ktlint:standard:max-line-length")
class QuadraticFormulaStrategyTest {
    private fun testQuadraticFormula(variable: String = "x", init: MethodTestCase.() -> Unit) {
        val testCase = MethodTestCase()
        testCase.method = EquationsPlans.SolveEquation
        testCase.context = Context(
            solutionVariables = listOf(variable),
            preferredStrategies = mapOf(strategyChoice(QuadraticFormula)),
        )
        testCase.init()
    }

    @Test
    fun `test quadratic equation is normalized before applying the formula`() =
        testQuadraticFormula {
            inputExpr = "[x^2] + 5 + 1 + 5x = 0"

            check {
                fromExpr = "[x ^ 2] + 5 + 1 + 5 x = 0"
                toExpr = "SetSolution[x: {-3, -2}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "[x ^ 2] + 5 + 1 + 5 x = 0"
                    toExpr = "[x ^ 2] + 6 + 5 x = 0"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + 6 + 5 x = 0"
                    toExpr = "[x ^ 2] + 5 x + 6 = 0"
                    explanation {
                        key = PolynomialsExplanation.NormalizePolynomial
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + 5 x + 6 = 0"
                    toExpr = "x = [-5 +/- sqrt[[5 ^ 2] - 4 * 1 * 6] / 2 * 1]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-5 +/- sqrt[[5 ^ 2] - 4 * 1 * 6] / 2 * 1]"
                    toExpr = "x = [-5 +/- 1 / 2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = [-5 +/- 1 / 2]"
                    toExpr = "x = [-5 - 1 / 2] OR x = [-5 + 1 / 2]"
                    explanation {
                        key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                    }
                }

                step {
                    fromExpr = "x = [-5 - 1 / 2] OR x = [-5 + 1 / 2]"
                    toExpr = "SetSolution[x: {-3, -2}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }
                }
            }
        }

    @Test
    fun `test quadratic equation with rational solutions`() =
        testQuadraticFormula {
            inputExpr = "2[x^2] - 7x + 4 = 1"

            check {
                fromExpr = "2 [x ^ 2] - 7 x + 4 = 1"
                toExpr = "SetSolution[x: {[1 / 2], 3}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "2 [x ^ 2] - 7 x + 4 = 1"
                    toExpr = "2 [x ^ 2] - 7 x + 3 = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "2 [x ^ 2] - 7 x + 3 = 0"
                    toExpr = "x = [-(-7) +/- sqrt[[(-7) ^ 2] - 4 * 2 * 3] / 2 * 2]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-(-7) +/- sqrt[[(-7) ^ 2] - 4 * 2 * 3] / 2 * 2]"
                    toExpr = "x = [7 +/- 5 / 4]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = [7 +/- 5 / 4]"
                    toExpr = "x = [7 - 5 / 4] OR x = [7 + 5 / 4]"
                    explanation {
                        key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                    }
                }

                step {
                    fromExpr = "x = [7 - 5 / 4] OR x = [7 + 5 / 4]"
                    toExpr = "SetSolution[x: {[1 / 2], 3}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }

                    task {
                        taskId = "#1"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "x = [7 - 5 / 4]"
                            toExpr = "x = [1 / 2]"
                        }

                        step {
                            toExpr = "SetSolution[x: {[1 / 2]}]"
                            explanation {
                                key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        explanation {
                            key = EquationsExplanation.SolveEquationInEquationUnion
                        }

                        step {
                            fromExpr = "x = [7 + 5 / 4]"
                            toExpr = "x = 3"
                        }

                        step {
                            toExpr = "SetSolution[x: {3}]"
                            explanation {
                                key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                            }
                        }
                    }

                    task {
                        taskId = "#3"
                        explanation {
                            key = EquationsExplanation.CollectSolutions
                        }
                    }
                }
            }
        }

    @Test
    fun `test quadratic equation with rational solutions 2`() =
        testQuadraticFormula {
            inputExpr = "2[x^2] - 7x + 3 = 0"

            check {
                fromExpr = "2 [x ^ 2] - 7 x + 3 = 0"
                toExpr = "SetSolution[x: {[1 / 2], 3}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "2 [x ^ 2] - 7 x + 3 = 0"
                    toExpr = "x = [-(-7) +/- sqrt[[(-7) ^ 2] - 4 * 2 * 3] / 2 * 2]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-(-7) +/- sqrt[[(-7) ^ 2] - 4 * 2 * 3] / 2 * 2]"
                    toExpr = "x = [7 +/- 5 / 4]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = [7 +/- 5 / 4]"
                    toExpr = "x = [7 - 5 / 4] OR x = [7 + 5 / 4]"
                    explanation {
                        key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                    }
                }

                step {
                    fromExpr = "x = [7 - 5 / 4] OR x = [7 + 5 / 4]"
                    toExpr = "SetSolution[x: {[1 / 2], 3}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }
                }
            }
        }

    @Test
    fun `test quadratic equation with discriminant = 0`() =
        testQuadraticFormula {
            inputExpr = "[x^2] + 4x + 4 = 0"

            check {
                fromExpr = "[x ^ 2] + 4 x + 4 = 0"
                toExpr = "SetSolution[x: {-2}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "[x ^ 2] + 4 x + 4 = 0"
                    toExpr = "x = [-4 +/- sqrt[[4 ^ 2] - 4 * 1 * 4] / 2 * 1]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-4 +/- sqrt[[4 ^ 2] - 4 * 1 * 4] / 2 * 1]"
                    toExpr = "x = -2"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = -2"
                    toExpr = "SetSolution[x: {-2}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test quadratic equation with discriminant less than 0`() =
        testQuadraticFormula {
            inputExpr = "[x^2] + 4x + 9 = 0"

            check {
                fromExpr = "[x ^ 2] + 4 x + 9 = 0"
                toExpr = "Contradiction[x: x = [-4 +/- sqrt[-20] / 2]]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "[x ^ 2] + 4 x + 9 = 0"
                    toExpr = "x = [-4 +/- sqrt[[4 ^ 2] - 4 * 1 * 9] / 2 * 1]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-4 +/- sqrt[[4 ^ 2] - 4 * 1 * 9] / 2 * 1]"
                    toExpr = "x = [-4 +/- sqrt[-20] / 2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = [-4 +/- sqrt[-20] / 2]"
                    toExpr = "Contradiction[x: x = [-4 +/- sqrt[-20] / 2]]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromNegativeUnderSquareRootInRealDomain
                    }
                }
            }
        }

    @Test
    fun `test quadratic equation with leading coefficient -1`() =
        testQuadraticFormula {
            inputExpr = "-[x^2] + 2x - 8 = 0"

            check {
                fromExpr = "-[x ^ 2] + 2 x - 8 = 0"
                toExpr = "Contradiction[x: x = [2 +/- sqrt[-28] / 2]]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "-[x ^ 2] + 2 x - 8 = 0"
                    toExpr = "[x ^ 2] - 2 x + 8 = 0"
                    explanation {
                        key = EquationsExplanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient
                    }
                }

                step {
                    fromExpr = "[x ^ 2] - 2 x + 8 = 0"
                    toExpr = "x = [-(-2) +/- sqrt[[(-2) ^ 2] - 4 * 1 * 8] / 2 * 1]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-(-2) +/- sqrt[[(-2) ^ 2] - 4 * 1 * 8] / 2 * 1]"
                    toExpr = "x = [2 +/- sqrt[-28] / 2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = [2 +/- sqrt[-28] / 2]"
                    toExpr = "Contradiction[x: x = [2 +/- sqrt[-28] / 2]]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromNegativeUnderSquareRootInRealDomain
                    }
                }
            }
        }

    @Test
    fun `test ax^2 + bx + c = 0 with gcd(a, b, c) != 1`() =
        testQuadraticFormula {
            inputExpr = "2[x^2] + 4x + 2 = 0"

            check {
                fromExpr = "2 [x ^ 2] + 4 x + 2 = 0"
                toExpr = "SetSolution[x: {-1}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "2 [x ^ 2] + 4 x + 2 = 0"
                    toExpr = "[x ^ 2] + 2 x + 1 = 0"
                    explanation {
                        key = EquationsExplanation.SimplifyByDividingByGcfOfCoefficients
                    }

                    step {
                        fromExpr = "2 [x ^ 2] + 4 x + 2 = 0"
                        toExpr = "2 ([x ^ 2] + 2 x + 1) = 0"
                        explanation {
                            key = FactorExplanation.FactorGreatestCommonIntegerFactor
                        }
                    }

                    step {
                        fromExpr = "2 ([x ^ 2] + 2 x + 1) = 0"
                        toExpr = "[x ^ 2] + 2 x + 1 = 0"
                        explanation {
                            key = EquationsExplanation.EliminateConstantFactorOfLhsWithZeroRhs
                        }
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + 2 x + 1 = 0"
                    toExpr = "x = [-2 +/- sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-2 +/- sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1]"
                    toExpr = "x = -1"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = -1"
                    toExpr = "SetSolution[x: {-1}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test ax^2 + bx + c = 0 with gcd(a, b, c) != 1 with some negative coefficients`() =
        testQuadraticFormula {
            inputExpr = "2[x^2] - 4x + 2 = 0"

            check {
                fromExpr = "2 [x ^ 2] - 4 x + 2 = 0"
                toExpr = "SetSolution[x: {1}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "2 [x ^ 2] - 4 x + 2 = 0"
                    toExpr = "[x ^ 2] - 2 x + 1 = 0"
                    explanation {
                        key = EquationsExplanation.SimplifyByDividingByGcfOfCoefficients
                    }
                }

                step {
                    fromExpr = "[x ^ 2] - 2 x + 1 = 0"
                    toExpr = "x = [-(-2) +/- sqrt[[(-2) ^ 2] - 4 * 1 * 1] / 2 * 1]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-(-2) +/- sqrt[[(-2) ^ 2] - 4 * 1 * 1] / 2 * 1]"
                    toExpr = "x = 1"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = 1"
                    toExpr = "SetSolution[x: {1}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test expand to ax^2 + c = 0 form`() =
        testQuadraticFormula {
            inputExpr = "(x + 1)(x+2) - 3x - 6 = 0"

            check {
                fromExpr = "(x + 1) (x + 2) - 3 x - 6 = 0"
                toExpr = "SetSolution[x: {-2, 2}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "(x + 1) (x + 2) - 3 x - 6 = 0"
                    toExpr = "[x ^ 2] - 4 = 0"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }

                step {
                    fromExpr = "[x ^ 2] - 4 = 0"
                    toExpr = "x = [-0 +/- sqrt[[0 ^ 2] - 4 * 1 * (-4)] / 2 * 1]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-0 +/- sqrt[[0 ^ 2] - 4 * 1 * (-4)] / 2 * 1]"
                    toExpr = "x = [+/- 4 / 2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = [+/- 4 / 2]"
                    toExpr = "x = [- 4 / 2] OR x = [4 / 2]"
                    explanation {
                        key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                    }
                }

                step {
                    fromExpr = "x = [- 4 / 2] OR x = [4 / 2]"
                    toExpr = "SetSolution[x: {-2, 2}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }
                }
            }
        }

    @Test
    fun `test expand to ax^2 + bx = 0 form`() =
        testQuadraticFormula {
            inputExpr = "(x + 1)(x+2) + 4x - 2 = 0"

            check {
                fromExpr = "(x + 1) (x + 2) + 4 x - 2 = 0"
                toExpr = "SetSolution[x: {-7, 0}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "(x + 1) (x + 2) + 4 x - 2 = 0"
                    toExpr = "[x ^ 2] + 7 x = 0"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + 7 x = 0"
                    toExpr = "x = [-7 +/- sqrt[[7 ^ 2] - 4 * 1 * 0] / 2 * 1]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-7 +/- sqrt[[7 ^ 2] - 4 * 1 * 0] / 2 * 1]"
                    toExpr = "x = [-7 +/- 7 / 2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = [-7 +/- 7 / 2]"
                    toExpr = "x = [-7 - 7 / 2] OR x = [-7 + 7 / 2]"
                    explanation {
                        key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                    }
                }

                step {
                    fromExpr = "x = [-7 - 7 / 2] OR x = [-7 + 7 / 2]"
                    toExpr = "SetSolution[x: {-7, 0}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }
                }
            }
        }

    @Test
    fun `test non simplifiable distinct roots`() =
        testQuadraticFormula {
            inputExpr = "[x^2] - 7x - 1 = 0"

            check {
                fromExpr = "[x ^ 2] - 7 x - 1 = 0"
                toExpr = "SetSolution[x: {[7 - sqrt[53] / 2], [7 + sqrt[53] / 2]}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "[x ^ 2] - 7 x - 1 = 0"
                    toExpr = "x = [-(-7) +/- sqrt[[(-7) ^ 2] - 4 * 1 * (-1)] / 2 * 1]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-(-7) +/- sqrt[[(-7) ^ 2] - 4 * 1 * (-1)] / 2 * 1]"
                    toExpr = "x = [7 +/- sqrt[53] / 2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = [7 +/- sqrt[53] / 2]"
                    toExpr = "SetSolution[x: {[7 - sqrt[53] / 2], [7 + sqrt[53] / 2]}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                    }
                }
            }
        }

    @Test
    fun `test multiplying through by the LCD 1`() =
        testQuadraticFormula {
            inputExpr = "[x ^ 2] + [x / 6] + 1 = 0"

            check {
                fromExpr = "[x ^ 2] + [x / 6] + 1 = 0"
                toExpr = "Contradiction[x: x = [-1 +/- sqrt[-143] / 12]]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "[x ^ 2] + [x / 6] + 1 = 0"
                    toExpr = "6 [x ^ 2] + x + 6 = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify
                    }
                }

                step { }
                step { }
                step { }
            }
        }

    @Test
    fun `test multiplying through by the LCD 2`() =
        testQuadraticFormula {
            inputExpr = "[[x^2] / 6] + [x / 3] + [1 / 8] = 0"

            check {
                fromExpr = "[[x ^ 2] / 6] + [x / 3] + [1 / 8] = 0"
                toExpr = "SetSolution[x: {-[3 / 2], -[1 / 2]}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "[[x ^ 2] / 6] + [x / 3] + [1 / 8] = 0"
                    toExpr = "4 [x ^ 2] + 8 x + 3 = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify
                    }
                }

                step { }
                step { }
                step { }
                step { }
            }
        }

    @Test
    fun `test multiplying through by the LCD 3`() =
        testQuadraticFormula {
            inputExpr = "[[x^2] + 1 / 2] + [x / 2] = [x + 1 / 3]"

            check {
                fromExpr = "[[x ^ 2] + 1 / 2] + [x / 2] = [x + 1 / 3]"
                toExpr = "Contradiction[x: x = [-1 +/- sqrt[-11] / 6]]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "[[x ^ 2] + 1 / 2] + [x / 2] = [x + 1 / 3]"
                    toExpr = "3 [x ^ 2] + 3 + 3 x = 2 x + 2"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify
                    }
                }

                step {
                    fromExpr = "3 [x ^ 2] + 3 + 3 x = 2 x + 2"
                    toExpr = "3 [x ^ 2] + 1 + x = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "3 [x ^ 2] + 1 + x = 0"
                    toExpr = "3 [x^2] + x + 1 = 0"
                    explanation {
                        key = PolynomialsExplanation.NormalizePolynomial
                    }
                }

                step { }
                step { }
                step { }
            }
        }

    @Test
    fun `test multiplying through by the LCD 4`() =
        testQuadraticFormula {
            inputExpr = "[1 / 5] [x ^ 2] - [2 / 3] x - [1 / 6] x - [5 / 6] = 0"

            check {
                fromExpr = "[1 / 5] [x ^ 2] - [2 / 3] x - [1 / 6] x - [5 / 6] = 0"
                toExpr = "SetSolution[x: {-[5 / 6], 5}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "[1 / 5] [x ^ 2] - [2 / 3] x - [1 / 6] x - [5 / 6] = 0"
                    toExpr = "[1 / 5] [x ^ 2] - [5 / 6] x - [5 / 6] = 0"
                    explanation {
                        key = CollectingExplanation.CollectLikeTermsAndSimplify
                    }
                }

                step {
                    fromExpr = "[1 / 5] [x ^ 2] - [5 / 6] x - [5 / 6] = 0"
                    toExpr = "6 [x ^ 2] - 25 x - 25 = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify
                    }
                }

                step { }
                step { }
                step { }
                step { }
            }
        }

    @Test
    fun `test quadratic equation with the constant term being a sum of an integer and a root`() =
        testQuadraticFormula {
            inputExpr = "3 [x ^ 2] + 6 x + 3 - sqrt[2] = 0"

            check {
                fromExpr = "3 [x ^ 2] + 6 x + 3 - sqrt[2] = 0"
                toExpr = "SetSolution[x: {-[3 + root[18, 4] / 3], [-3 + root[18, 4] / 3]}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "3 [x ^ 2] + 6 x + 3 - sqrt[2] = 0"
                    toExpr = "x = [-6 +/- sqrt[[6 ^ 2] - 4 * 3 (3 - sqrt[2])] / 2 * 3]"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "x = [-6 +/- sqrt[[6 ^ 2] - 4 * 3 (3 - sqrt[2])] / 2 * 3]"
                    toExpr = "x = [-3 +/- root[18, 4] / 3]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }

                step {
                    fromExpr = "x = [-3 +/- root[18, 4] / 3]"
                    toExpr = "x = [-3 - root[18, 4] / 3] OR x = [-3 + root[18, 4] / 3]"
                    explanation {
                        key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                    }
                }

                step {
                    fromExpr = "x = [-3 - root[18, 4] / 3] OR x = [-3 + root[18, 4] / 3]"
                    toExpr = "SetSolution[x: {-[3 + root[18, 4] / 3], [-3 + root[18, 4] / 3]}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationUnion
                    }
                }
            }
        }

    @Suppress("MaxLineLength")
    @Test
    fun `test parametric equation with constraint`() =
        testQuadraticFormula("c") {
            inputExpr = "-[a ^ 2] = [b ^ 2] + [c ^ 2] - 3 b c"

            check {
                fromExpr = "-[a ^ 2] = [b ^ 2] + [c ^ 2] - 3 b c"
                toExpr = "c = [3 b +/- sqrt[5 [b ^ 2] - 4 [a ^ 2]] / 2] GIVEN 9 [b ^ 2] - 4 ([a ^ 2] + [b ^ 2]) >= 0"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "-[a ^ 2] = [b ^ 2] + [c ^ 2] - 3 b c"
                    toExpr = "-[a ^ 2] - [b ^ 2] - [c ^ 2] + 3 b c = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "-[a ^ 2] - [b ^ 2] - [c ^ 2] + 3 b c = 0"
                    toExpr = "-[c ^ 2] + 3 b c - [a ^ 2] - [b ^ 2] = 0"
                    explanation {
                        key = PolynomialsExplanation.NormalizePolynomial
                    }
                }

                step {
                    fromExpr = "-[c ^ 2] + 3 b c - [a ^ 2] - [b ^ 2] = 0"
                    toExpr = "[c ^ 2] - 3 b c + [a ^ 2] + [b ^ 2] = 0"
                    explanation {
                        key = EquationsExplanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient
                    }
                }

                step {
                    fromExpr = "[c ^ 2] - 3 b c + [a ^ 2] + [b ^ 2] = 0"
                    toExpr = "c = [-(-3 b) +/- sqrt[[(-3 b) ^ 2] - 4 * 1 ([a ^ 2] + [b ^ 2])] / 2 * 1] GIVEN [(-3 b) ^ 2] - 4 * 1 ([a ^ 2] + [b ^ 2]) >= 0"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                // This constraint should be simplified further
                step {
                    fromExpr = "c = [-(-3 b) +/- sqrt[[(-3 b) ^ 2] - 4 * 1 ([a ^ 2] + [b ^ 2])] / 2 * 1] GIVEN [(-3 b) ^ 2] - 4 * 1 ([a ^ 2] + [b ^ 2]) >= 0"
                    toExpr = "c = [-(-3 b) +/- sqrt[[(-3 b) ^ 2] - 4 * 1 ([a ^ 2] + [b ^ 2])] / 2 * 1] GIVEN 9 [b ^ 2] - 4 ([a ^ 2] + [b ^ 2]) >= 0"
                    explanation {
                        key = EquationsExplanation.SimplifyConstraint
                    }
                }

                step {
                    fromExpr = "c = [-(-3 b) +/- sqrt[[(-3 b) ^ 2] - 4 * 1 ([a ^ 2] + [b ^ 2])] / 2 * 1] GIVEN 9 [b ^ 2] - 4 ([a ^ 2] + [b ^ 2]) >= 0"
                    toExpr = "c = [3 b +/- sqrt[5 [b ^ 2] - 4 [a ^ 2]] / 2] GIVEN 9 [b ^ 2] - 4 ([a ^ 2] + [b ^ 2]) >= 0"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }
            }
        }

    @Suppress("MaxLineLength")
    @Test
    fun `test parametric quadratic equation with constraint always satisfied`() =
        testQuadraticFormula("c") {
            inputExpr = "[a ^ 2] = [b ^ 2] + [c ^ 2] - 4 b c"

            check {
                fromExpr = "[a ^ 2] = [b ^ 2] + [c ^ 2] - 4 b c"
                toExpr = "c = 2 b +/- sqrt[3 [b ^ 2] + [a ^ 2]] GIVEN 16 [b ^ 2] - 4 (-[a ^ 2] + [b ^ 2]) >= 0"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "[a ^ 2] = [b ^ 2] + [c ^ 2] - 4 b c"
                    toExpr = "[a ^ 2] - [b ^ 2] - [c ^ 2] + 4 b c = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "[a ^ 2] - [b ^ 2] - [c ^ 2] + 4 b c = 0"
                    toExpr = "-[c ^ 2] + 4 b c + [a ^ 2] - [b ^ 2] = 0"
                    explanation {
                        key = PolynomialsExplanation.NormalizePolynomial
                    }
                }

                step {
                    fromExpr = "-[c ^ 2] + 4 b c + [a ^ 2] - [b ^ 2] = 0"
                    toExpr = "[c ^ 2] - 4 b c - [a ^ 2] + [b ^ 2] = 0"
                    explanation {
                        key = EquationsExplanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient
                    }
                }

                step {
                    fromExpr = "[c ^ 2] - 4 b c - [a ^ 2] + [b ^ 2] = 0"
                    toExpr = "c = [-(-4 b) +/- sqrt[[(-4 b) ^ 2] - 4 * 1 (-[a ^ 2] + [b ^ 2])] / 2 * 1] GIVEN [(-4 b) ^ 2] - 4 * 1 (-[a ^ 2] + [b ^ 2]) >= 0"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                // The steps below should be improved later
                step {
                    fromExpr = "c = [-(-4 b) +/- sqrt[[(-4 b) ^ 2] - 4 * 1 (-[a ^ 2] + [b ^ 2])] / 2 * 1] GIVEN [(-4 b) ^ 2] - 4 * 1 (-[a ^ 2] + [b ^ 2]) >= 0"
                    toExpr = "c = [-(-4 b) +/- sqrt[[(-4 b) ^ 2] - 4 * 1 (-[a ^ 2] + [b ^ 2])] / 2 * 1] GIVEN 16 [b ^ 2] - 4 (-[a ^ 2] + [b ^ 2]) >= 0"
                    explanation {
                        key = EquationsExplanation.SimplifyConstraint
                    }
                }

                step {
                    fromExpr = "c = [-(-4 b) +/- sqrt[[(-4 b) ^ 2] - 4 * 1 (-[a ^ 2] + [b ^ 2])] / 2 * 1] GIVEN 16 [b ^ 2] - 4 (-[a ^ 2] + [b ^ 2]) >= 0"
                    toExpr = "c = 2 b +/- sqrt[3 [b ^ 2] + [a ^ 2]] GIVEN 16 [b ^ 2] - 4 (-[a ^ 2] + [b ^ 2]) >= 0"
                    explanation {
                        key = SimplifyExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }

    @Suppress("MaxLineLength")
    @Test
    fun `test parametric equation with no linear term`() =
        testQuadraticFormula("c") {
            inputExpr = "E = m [c ^ 2]"

            check {
                fromExpr = "E = m [c ^ 2]"
                toExpr = "c = [+/-2 sqrt[E m] / 2 m] GIVEN 4 E m >= 0 AND SetSolution[m: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                }

                step {
                    fromExpr = "E = m [c ^ 2]"
                    toExpr = "E = [c ^ 2] m"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "E = [c ^ 2] m"
                    toExpr = "E - [c ^ 2] m = 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                    }
                }

                step {
                    fromExpr = "E - [c ^ 2] m = 0"
                    toExpr = "-[c ^ 2] m + E = 0"
                    explanation {
                        key = PolynomialsExplanation.NormalizePolynomial
                    }
                }

                step {
                    fromExpr = "-[c ^ 2] m + E = 0"
                    toExpr = "[c ^ 2] m - E = 0"
                    explanation {
                        key = EquationsExplanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient
                    }
                }

                step {
                    fromExpr = "[c ^ 2] m - E = 0"
                    toExpr = "c = [-0 +/- sqrt[[0 ^ 2] - 4 m (-E)] / 2 m] GIVEN [0 ^ 2] - 4 m (-E) >= 0 AND 2 m != 0"
                    explanation {
                        key = EquationsExplanation.ApplyQuadraticFormula
                    }
                }

                step {
                    fromExpr = "c = [-0 +/- sqrt[[0 ^ 2] - 4 m (-E)] / 2 m] GIVEN [0 ^ 2] - 4 m (-E) >= 0 AND 2 m != 0"
                    toExpr = "c = [-0 +/- sqrt[[0 ^ 2] - 4 m (-E)] / 2 m] GIVEN 4 E m >= 0 AND SetSolution[m: /reals/ \\ {0}]"
                    explanation {
                        key = EquationsExplanation.SimplifyConstraint
                    }
                }

                step {
                    fromExpr = "c = [-0 +/- sqrt[[0 ^ 2] - 4 m (-E)] / 2 m] GIVEN 4 E m >= 0 AND SetSolution[m: /reals/ \\ {0}]"
                    toExpr = "c = [+/-2 sqrt[E m] / 2 m] GIVEN 4 E m >= 0 AND SetSolution[m: /reals/ \\ {0}]"
                    explanation {
                        key = SimplifyExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }
}
