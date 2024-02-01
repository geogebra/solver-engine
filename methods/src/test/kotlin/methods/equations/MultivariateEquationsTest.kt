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
import engine.methods.SolverEngineExplanation
import engine.methods.testMethod
import engine.methods.testMethodInX
import methods.algebra.AlgebraExplanation
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.general.GeneralExplanation
import methods.inequalities.InequalitiesExplanation
import methods.inequations.InequationsExplanation
import methods.polynomials.PolynomialsExplanation
import kotlin.test.Test

@Suppress("LargeClass", "MaxLineLength", "ktlint:standard:max-line-length")
class MultivariateEquationsTest {
    @Test
    fun `test single step linear equation without constraint`() =
        testMethod {
            method = EquationsPlans.SolveEquation
            context = Context(solutionVariables = listOf("A"))
            inputExpr = "A - b h = 0"

            check {
                fromExpr = "A - b h = 0"
                toExpr = "SetSolution[A: {b h}]"
                explanation {
                    key = EquationsExplanation.SolveLinearEquation
                }

                step {
                    fromExpr = "A - b h = 0"
                    toExpr = "A = b h"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRightAndSimplify
                    }

                    step {
                        fromExpr = "A - b h = 0"
                        toExpr = "A - b h + b h = b h"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRight
                        }
                    }

                    step {
                        fromExpr = "A - b h + b h = b h"
                        toExpr = "A = b h"
                        explanation {
                            key = GeneralExplanation.CancelAdditiveInverseElements
                        }
                    }
                }

                step {
                    fromExpr = "A = b h"
                    toExpr = "SetSolution[A: {b h}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test two step linear equation without constraint`() =
        testMethod {
            method = EquationsPlans.SolveEquation
            context = Context(solutionVariables = listOf("C"))
            inputExpr = "F = [9 / 5] C + 32"

            check {
                fromExpr = "F = [9 / 5] C + 32"
                toExpr = "SetSolution[C: {[5 / 9] (F - 32)}]"
                explanation {
                    key = EquationsExplanation.SolveLinearEquation
                }

                step {
                    fromExpr = "F = [9 / 5] C + 32"
                    toExpr = "F - 32 = [9 / 5] C"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheLeftAndSimplify
                    }

                    step {
                        fromExpr = "F = [9 / 5] C + 32"
                        toExpr = "F - 32 = [9 / 5] C + 32 - 32"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheLeft
                        }
                    }

                    step {
                        fromExpr = "F - 32 = [9 / 5] C + 32 - 32"
                        toExpr = "F - 32 = [9 / 5] C"
                        explanation {
                            key = GeneralExplanation.CancelAdditiveInverseElements
                        }
                    }
                }

                step {
                    fromExpr = "F - 32 = [9 / 5] C"
                    toExpr = "[9 / 5] C = F - 32"
                    explanation {
                        key = methods.solvable.EquationsExplanation.FlipEquation
                    }
                }

                step {
                    fromExpr = "[9 / 5] C = F - 32"
                    toExpr = "C = [5 / 9] (F - 32)"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                    }

                    step {
                        fromExpr = "[9 / 5] C = F - 32"
                        toExpr = "[5 / 9] * [9 / 5] C  = [5 / 9] (F - 32)"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MultiplyByInverseCoefficientOfVariable
                        }
                    }

                    step {
                        fromExpr = "[5 / 9] * [9 / 5] C  = [5 / 9] (F - 32)"
                        toExpr = "C = [5 / 9] (F - 32)"
                        explanation {
                            key = PolynomialsExplanation.SimplifyMonomial
                        }
                    }
                }

                step {
                    fromExpr = "C = [5 / 9] (F - 32)"
                    toExpr = "SetSolution[C: {[5 / 9] (F - 32)}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test two step linear equation with constraint`() =
        testMethod {
            method = EquationsPlans.SolveEquation
            context = Context(solutionVariables = listOf("r"))
            inputExpr = "I (r + R) = E"

            check {
                fromExpr = "I (r + R) = E"
                toExpr = "SetSolution[r: {[E / I] - R}] GIVEN SetSolution[I: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.SolveLinearEquation
                }

                step {
                    fromExpr = "I (r + R) = E"
                    toExpr = "r + R = [E / I] GIVEN SetSolution[I: /reals/ \\ {0}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                    }

                    step {
                        fromExpr = "I (r + R) = E"
                        toExpr = "[I (r + R) / I] = [E / I] GIVEN I != 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableMultivariate
                        }
                    }

                    step {
                        fromExpr = "[I (r + R) / I] = [E / I] GIVEN I != 0"
                        toExpr = "r + R = [E / I] GIVEN I != 0"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "r + R = [E / I] GIVEN I != 0"
                        toExpr = "r + R = [E / I] GIVEN SetSolution[I: /reals/ \\ {0}]"
                        explanation {
                            key = EquationsExplanation.SimplifyConstraint
                        }

                        step {
                            fromExpr = "I != 0"
                            toExpr = "SetSolution[I: /reals/ \\ {0}]"
                            explanation {
                                key = InequationsExplanation.ExtractSolutionFromInequationInSolvedForm
                            }
                        }
                    }
                }

                step {
                    fromExpr = "r + R = [E / I] GIVEN SetSolution[I: /reals/ \\ {0}]"
                    toExpr = "r = [E / I] - R GIVEN SetSolution[I: /reals/ \\ {0}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRightAndSimplify
                    }

                    step {
                        fromExpr = "r + R = [E / I]"
                        toExpr = "r + R - R = [E / I] - R"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRight
                        }
                    }

                    step {
                        fromExpr = "r + R - R = [E / I] - R"
                        toExpr = "r = [E / I] - R"
                        explanation {
                            key = GeneralExplanation.CancelAdditiveInverseElements
                        }
                    }
                }

                step {
                    fromExpr = "r = [E / I] - R GIVEN SetSolution[I: /reals/ \\ {0}]"
                    toExpr = "SetSolution[r: {[E / I] - R}] GIVEN SetSolution[I: /reals/ \\ {0}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test linear equation with two constraints`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "(x z + 1) (y + 2) = 1"

            check {
                fromExpr = "(x z + 1) (y + 2) = 1"
                toExpr = "SetSolution[x: {[[1 / y + 2] - 1 / z]}] " +
                    "GIVEN SetSolution[y: /reals/ \\ {-2}] AND SetSolution[z: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.SolveLinearEquation
                }

                step {
                    fromExpr = "(x z + 1) (y + 2) = 1"
                    toExpr = "x z + 1 = [1 / y + 2] GIVEN SetSolution[y: /reals/ \\ {-2}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                    }
                }

                step {
                    fromExpr = "x z + 1 = [1 / y + 2] GIVEN SetSolution[y: /reals/ \\ {-2}]"
                    toExpr = "x z = [1 / y + 2] - 1 GIVEN SetSolution[y: /reals/ \\ {-2}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRightAndSimplify
                    }
                }

                step {
                    fromExpr = "x z = [1 / y + 2] - 1 GIVEN SetSolution[y: /reals/ \\ {-2}]"
                    toExpr = "x = [[1 / y + 2] - 1 / z] " +
                        "GIVEN SetSolution[z: /reals/ \\ {0}] GIVEN SetSolution[y: /reals/ \\ {-2}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                    }
                }

                step {
                    fromExpr = "x = [[1 / y + 2] - 1 / z] " +
                        "GIVEN SetSolution[z: /reals/ \\ {0}] GIVEN SetSolution[y: /reals/ \\ {-2}]"
                    toExpr = "x = [[1 / y + 2] - 1 / z] " +
                        "GIVEN SetSolution[y: /reals/ \\ {-2}] AND SetSolution[z: /reals/ \\ {0}]"
                    explanation {
                        key = SolverEngineExplanation.MergeConstraints
                    }
                }

                step {
                    fromExpr = "x = [[1 / y + 2] - 1 / z] " +
                        "GIVEN SetSolution[y: /reals/ \\ {-2}] AND SetSolution[z: /reals/ \\ {0}]"
                    toExpr = "SetSolution[x: {[[1 / y + 2] - 1 / z]}] " +
                        "GIVEN SetSolution[y: /reals/ \\ {-2}] AND SetSolution[z: /reals/ \\ {0}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test roots method with odd exponent`() =
        testMethod {
            method = EquationsPlans.SolveEquation
            context = context.copy(solutionVariables = listOf("a"))
            inputExpr = "[a ^ 3] b + c = 0"

            check {
                fromExpr = "[a ^ 3] b + c = 0"
                toExpr = "SetSolution[a: {-root[[c / b], 3]}] GIVEN SetSolution[b: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUsingRootsMethod
                }

                step {
                    fromExpr = "[a ^ 3] b + c = 0"
                    toExpr = "[a ^ 3] b = -c"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRightAndSimplify
                    }
                }

                step {
                    fromExpr = "[a ^ 3] b = -c"
                    toExpr = "[a ^ 3] = -[c / b] GIVEN SetSolution[b: /reals/ \\ {0}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                    }

                    step {
                        fromExpr = "[a ^ 3] b = -c"
                        toExpr = "[[a ^ 3] b / b] = [-c / b] GIVEN b != 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableMultivariate
                        }
                    }

                    step {
                        fromExpr = "[[a ^ 3] b / b] = [-c / b] GIVEN b != 0"
                        toExpr = "[a ^ 3] = -[c / b] GIVEN b != 0"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "[a ^ 3] = -[c / b] GIVEN b != 0"
                        toExpr = "[a ^ 3] = -[c / b] GIVEN SetSolution[b: /reals/ \\ {0}]"
                        explanation {
                            key = EquationsExplanation.SimplifyConstraint
                        }
                    }
                }

                step {
                    fromExpr = "[a ^ 3] = -[c / b] GIVEN SetSolution[b: /reals/ \\ {0}]"
                    toExpr = "a = -root[[c / b], 3] GIVEN SetSolution[b: /reals/ \\ {0}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.TakeRootOfBothSidesAndSimplify
                    }

                    step {
                        fromExpr = "[a ^ 3] = -[c / b]"
                        toExpr = "a = root[-[c / b], 3]"
                        explanation {
                            key = methods.solvable.EquationsExplanation.TakeRootOfBothSides
                        }
                    }

                    step {
                        fromExpr = "a = root[-[c / b], 3]"
                        toExpr = "a = -root[[c / b], 3]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyRootsInExpression
                        }
                    }
                }

                step {
                    fromExpr = "a = -root[[c / b], 3] GIVEN SetSolution[b: /reals/ \\ {0}]"
                    toExpr = "SetSolution[a: {-root[[c / b], 3]}] GIVEN SetSolution[b: /reals/ \\ {0}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test roots method with constraints that simplify`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "m [x ^ 2] = 2 [m ^ 2]"

            check {
                fromExpr = "m [x ^ 2] = 2 [m ^ 2]"
                toExpr = "SetSolution[x: {-sqrt[2 m], sqrt[2 m]}] GIVEN SetSolution[m: (0, /infinity/)]"
                explanation {
                    key = EquationsExplanation.SolveEquationUsingRootsMethod
                }

                step {
                    fromExpr = "m [x ^ 2] = 2 [m ^ 2]"
                    toExpr = "[x ^ 2] = 2 m GIVEN SetSolution[m: /reals/ \\ {0}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                    }

                    step {
                        fromExpr = "m [x ^ 2] = 2 [m ^ 2]"
                        toExpr = "[m [x ^ 2] / m] = [2 [m ^ 2] / m] GIVEN m != 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableMultivariate
                        }
                    }

                    step {
                        fromExpr = "[m [x ^ 2] / m] = [2 [m ^ 2] / m] GIVEN m != 0"
                        toExpr = "[x ^ 2] = 2 m GIVEN m != 0"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "[x ^ 2] = 2 m GIVEN m != 0"
                        toExpr = "[x ^ 2] = 2 m GIVEN SetSolution[m: /reals/ \\ {0}]"
                        explanation {
                            key = EquationsExplanation.SimplifyConstraint
                        }
                    }
                }

                step {
                    fromExpr = "[x ^ 2] = 2 m GIVEN SetSolution[m: /reals/ \\ {0}]"
                    toExpr = "x = +/-sqrt[2 m] GIVEN SetSolution[m: [0, /infinity/)] GIVEN SetSolution[m: /reals/ \\ {0}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.TakeRootOfBothSidesAndSimplify
                    }

                    step {
                        fromExpr = "[x ^ 2] = 2 m"
                        toExpr = "x = +/-sqrt[2 m] GIVEN 2 m >= 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.TakeRootOfBothSides
                        }
                    }

                    step {
                        fromExpr = "x = +/-sqrt[2 m] GIVEN 2 m >= 0"
                        toExpr = "x = +/-sqrt[2 m] GIVEN SetSolution[m: [0, /infinity/)]"
                        explanation {
                            key = EquationsExplanation.SimplifyConstraint
                        }

                        step {
                            fromExpr = "2 m >= 0"
                            toExpr = "SetSolution[m: [0, /infinity/)]"
                            explanation {
                                key = InequalitiesExplanation.SolveLinearInequality
                            }
                        }
                    }
                }

                step {
                    fromExpr = "x = +/-sqrt[2 m] GIVEN SetSolution[m: [0, /infinity/)] GIVEN SetSolution[m: /reals/ \\ {0}]"
                    toExpr = "x = +/-sqrt[2 m] GIVEN SetSolution[m: (0, /infinity/)]"
                    explanation {
                        key = SolverEngineExplanation.MergeConstraints
                    }
                }

                step {
                    fromExpr = "x = +/-sqrt[2 m] GIVEN SetSolution[m: (0, /infinity/)]"
                    toExpr = "SetSolution[x: {-sqrt[2 m], sqrt[2 m]}] GIVEN SetSolution[m: (0, /infinity/)]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                    }
                }
            }
        }

    @Test
    fun `test roots method with no solution`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[x ^ 6] + [y ^ 2] + 1 = 0"

            check {
                fromExpr = "[x ^ 6] + [y ^ 2] + 1 = 0"
                toExpr = "Contradiction[x: [x ^ 6] = -[y ^ 2] - 1]"
                explanation {
                    key = EquationsExplanation.SolveEquationUsingRootsMethod
                }

                step {
                    fromExpr = "[x ^ 6] + [y ^ 2] + 1 = 0"
                    toExpr = "[x ^ 6] = -[y ^ 2] - 1"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsInVariablesToTheRightAndSimplify
                    }
                }

                step {
                    fromExpr = "[x ^ 6] = -[y ^ 2] - 1"
                    toExpr = "Contradiction[x: [x ^ 6] = -[y ^ 2] - 1]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEvenPowerEqualsNegative
                    }
                }
            }
        }

    @Test
    fun `test multiply and then divide by coefficient`() =
        testMethod {
            method = EquationsPlans.SolveEquation
            context = Context(solutionVariables = listOf("h"))
            inputExpr = "[1/2] h (B + b) = S"

            check {
                fromExpr = "[1 / 2] h (B + b) = S"
                toExpr = "SetSolution[h: {[2 S / B + b]}] GIVEN B + b != 0"
                explanation {
                    key = EquationsExplanation.SolveLinearEquation
                }

                step {
                    fromExpr = "[1 / 2] h (B + b) = S"
                    toExpr = "h (B + b) = 2 S"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                    }

                    step {
                        fromExpr = "[1 / 2] h (B + b) = S"
                        toExpr = "2 * [1 / 2] h (B + b) = 2 S"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MultiplyByInverseCoefficientOfVariable
                        }
                    }

                    step {
                        fromExpr = "2 * [1 / 2] h (B + b) = 2 S"
                        toExpr = "h (B + b) = 2 S"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }
                }

                step {
                    fromExpr = "h (B + b) = 2 S"
                    toExpr = "h = [2 S / B + b] GIVEN B + b != 0"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                    }

                    step {
                        fromExpr = "h (B + b) = 2 S"
                        toExpr = "[h (B + b) / B + b] = [2 S / B + b] GIVEN B + b != 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableMultivariate
                        }
                    }

                    step {
                        fromExpr = "[h (B + b) / B + b] = [2 S / B + b] GIVEN B + b != 0"
                        toExpr = "h = [2 S / B + b] GIVEN B + b != 0"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }
                }

                step {
                    fromExpr = "h = [2 S / B + b] GIVEN B + b != 0"
                    toExpr = "SetSolution[h: {[2 S / B + b]}] GIVEN B + b != 0"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test multiply and then divide by coefficient example2`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "x * [y / 2] = 5"

            check {
                fromExpr = "x * [y / 2] = 5"
                toExpr = "SetSolution[x: {[10 / y]}] GIVEN SetSolution[y: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.SolveLinearEquation
                }

                step {
                    fromExpr = "x * [y / 2] = 5"
                    toExpr = "x y = 10"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MultiplyByDenominatorOfVariableLHSAndSimplify
                    }

                    step {
                        fromExpr = "x * [y / 2] = 5"
                        toExpr = "2 x * [y / 2] = 2 * 5"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MultiplyByDenominatorOfVariableLHS
                        }
                    }

                    step {
                        fromExpr = "2 x * [y / 2] = 2 * 5"
                        toExpr = "x y = 10"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }
                }

                step {
                    fromExpr = "x y = 10"
                    toExpr = "x = [10 / y] GIVEN SetSolution[y: /reals/ \\ {0}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                    }

                    step {
                        fromExpr = "x y = 10"
                        toExpr = "[x y / y] = [10 / y] GIVEN y != 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableMultivariate
                        }
                    }

                    step {
                        fromExpr = "[x y / y] = [10 / y] GIVEN y != 0"
                        toExpr = "x = [10 / y] GIVEN y != 0"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "x = [10 / y] GIVEN y != 0"
                        toExpr = "x = [10 / y] GIVEN SetSolution[y: /reals/ \\ {0}]"
                        explanation {
                            key = EquationsExplanation.SimplifyConstraint
                        }
                    }
                }

                step {
                    fromExpr = "x = [10 / y] GIVEN SetSolution[y: /reals/ \\ {0}]"
                    toExpr = "SetSolution[x: {[10 / y]}] GIVEN SetSolution[y: /reals/ \\ {0}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test multiply and then divide by coefficient example3`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[2hx / 3] = 1"

            check {
                fromExpr = "[2 h x / 3] = 1"
                toExpr = "SetSolution[x: {[3 / 2 h]}] GIVEN SetSolution[h: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.SolveLinearEquation
                }

                step {
                    fromExpr = "[2 h x / 3] = 1"
                    toExpr = "2 h x = 3"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MultiplyByDenominatorOfVariableLHSAndSimplify
                    }
                }

                step {
                    fromExpr = "2 h x = 3"
                    toExpr = "x = [3 / 2 h] GIVEN SetSolution[h: /reals/ \\ {0}]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplifyMultivariate
                    }
                }

                step {
                    fromExpr = "x = [3 / 2 h] GIVEN SetSolution[h: /reals/ \\ {0}]"
                    toExpr = "SetSolution[x: {[3 / 2 h]}] GIVEN SetSolution[h: /reals/ \\ {0}]"
                    explanation {
                        key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                    }
                }
            }
        }

    @Test
    fun `test multivariate equation with constraint`() =
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[x / y] = 2"

            check {
                fromExpr = "[x / y] = 2"
                toExpr = "SetSolution[x: {2 y}] GIVEN SetSolution[y: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                task {
                    taskId = "#1"
                    startExpr = "[x / y] = 2"
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                    }

                    step {
                        fromExpr = "[x / y] = 2"
                        toExpr = "SetSolution[y: /reals/ \\ {0}]"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[x / y] = 2"
                    explanation {
                        key = EquationsExplanation.SolveEquation
                    }

                    step {
                        fromExpr = "[x / y] = 2"
                        toExpr = "SetSolution[x: {2 y}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x: {2 y}] GIVEN SetSolution[y: /reals/ \\ {0}]"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }
                }
            }
        }

    @Test
    fun `test multivariate equation with domain constraint and solution constraint`() =
        testMethod {
            method = EquationsPlans.SolveEquation
            inputExpr = "[2 h x / B + b] = S"
            context = context.copy(solutionVariables = listOf("h"))

            check {
                fromExpr = "[2 h x / B + b] = S"
                toExpr = "SetSolution[h: {[S (B + b) / 2 x]}] GIVEN B + b != 0 AND SetSolution[x: /reals/ \\ {0}]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                task {
                    taskId = "#1"
                    startExpr = "[2 h x / B + b] = S"
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                    }

                    step {
                        fromExpr = "[2 h x / B + b] = S"
                        toExpr = "B + b != 0"
                        explanation {
                            key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[2 h x / B + b] = S"
                    explanation {
                        key = EquationsExplanation.SolveEquation
                    }

                    step {
                        fromExpr = "[2 h x / B + b] = S"
                        toExpr = "SetSolution[h: {[S (B + b) / 2 x]}] GIVEN SetSolution[x: /reals/ \\ {0}]"
                        explanation {
                            key = EquationsExplanation.SolveLinearEquation
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[h: {[S (B + b) / 2 x]}] GIVEN SetSolution[x: /reals/ \\ {0}] GIVEN B + b != 0"
                    explanation {
                        key = EquationsExplanation.AddDomainConstraintToSolution
                    }

                    step {
                        fromExpr = "SetSolution[h: {[S (B + b) / 2 x]}] GIVEN SetSolution[x: /reals/ \\ {0}] GIVEN B + b != 0"
                        toExpr = "SetSolution[h: {[S (B + b) / 2 x]}] GIVEN B + b != 0 AND SetSolution[x: /reals/ \\ {0}]"
                        explanation {
                            key = SolverEngineExplanation.MergeConstraints
                        }
                    }
                }
            }
        }
}
