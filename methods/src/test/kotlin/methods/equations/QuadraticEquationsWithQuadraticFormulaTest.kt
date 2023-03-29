package methods.equations

import engine.methods.testMethodInX
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
class QuadraticEquationsWithQuadraticFormulaTest {
    @Test
    fun `test ax^2 + bx + c = d rational solutions`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "2[x^2] - 7x + 4 = 1"

        check {
            fromExpr = "2 [x ^ 2] - 7 x + 4 = 1"
            toExpr = "Solution[x, {[1 / 2], 3}]"
            explanation {
                key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
            }

            step {
                fromExpr = "2 [x ^ 2] - 7 x + 4 = 1"
                toExpr = "2 [x ^ 2] - 7 x + 3 = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheLeftAndSimplify
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
                toExpr = "x = [7 - 5 / 4] OR x = [7 + 5 / 4]]"
                explanation {
                    key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                }
            }

            step {
                fromExpr = "x = [7 - 5 / 4] OR x = [7 + 5 / 4]"
                toExpr = "Solution[x, {[1 / 2], 3}]"
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
                        toExpr = "Solution[x, {[1 / 2]}]"
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
                        toExpr = "Solution[x, {3}]"
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
    fun `test ax^2 + bx + c = 0 rational solutions`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "2[x^2] - 7x + 3 = 0"

        check {
            fromExpr = "2 [x ^ 2] - 7 x + 3 = 0"
            toExpr = "Solution[x, {[1 / 2], 3}]"
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
                toExpr = "Solution[x, {[1 / 2], 3}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
            }
        }
    }

    @Test
    fun `test quadraticEquation with discriminant = 0`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "[x^2] + 4x + 4 = 0"

        check {
            fromExpr = "[x ^ 2] + 4 x + 4 = 0"
            toExpr = "Solution[x, {-2}]"
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
                toExpr = "Solution[x, {-2}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test quadraticEquation with discriminant less than 0`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "[x^2] + 4x + 9 = 0"

        check {
            fromExpr = "[x ^ 2] + 4 x + 9 = 0"
            toExpr = "Solution[x, {}]"
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
                toExpr = "Solution[x, {}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromNegativeUnderSquareRootInRealDomain
                }
            }
        }
    }

    @Test
    fun `test negative unitary quadraticCoefficient`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "-[x^2] + 2x - 8 = 0"

        check {
            fromExpr = "-[x ^ 2] + 2 x - 8 = 0"
            toExpr = "Solution[x, {}]"
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
                toExpr = "Solution[x, {}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromNegativeUnderSquareRootInRealDomain
                }
            }
        }
    }

    @Test
    fun `test gcd(quadraticCoeff, linearCoeff, constantCoeff) != 1`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "2[x^2] + 4x + 2 = 0"

        check {
            fromExpr = "2 [x ^ 2] + 4 x + 2 = 0"
            toExpr = "Solution[x, {-1}]"
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
                        key = PolynomialsExplanation.FactorGreatestCommonFactor
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
                toExpr = "Solution[x, {-1}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test gcd(quadCoeff, linearCoeff, constantCoeff) != 1`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "2[x^2] - 4x + 2 = 0"

        check {
            fromExpr = "2 [x ^ 2] - 4 x + 2 = 0"
            toExpr = "Solution[x, {1}]"
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
                toExpr = "Solution[x, {1}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test expand to ax^2 + c = 0 form`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "(x + 1)(x+2) - 3x - 6 = 0"

        check {
            fromExpr = "(x + 1) (x + 2) - 3 x - 6 = 0"
            toExpr = "Solution[x, {-2, 2}]"
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
                toExpr = "Solution[x, {-2, 2}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
            }
        }
    }

    @Test
    fun `test expand to ax^2 + bx = 0 form`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "(x + 1)(x+2) + 4x - 2 = 0"

        check {
            fromExpr = "(x + 1) (x + 2) + 4 x - 2 = 0"
            toExpr = "Solution[x, {-7, 0}]"
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
                toExpr = "Solution[x, {-7, 0}]"
                explanation {
                    key = EquationsExplanation.SolveEquationUnion
                }
            }
        }
    }

    @Test
    fun `test non simplifiable distinct roots`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "[x^2] - 7x - 1 = 0"

        check {
            fromExpr = "[x ^ 2] - 7 x - 1 = 0"
            toExpr = "Solution[x, {[7 - sqrt[53] / 2], [7 + sqrt[53] / 2]}]"
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
                toExpr = "Solution[x, {[7 - sqrt[53] / 2], [7 + sqrt[53] / 2]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                }
            }
        }
    }

    @Test
    fun `test fractional coefficients`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "[[x^2] / 6] + [x / 3] + [1 / 8] = 0"

        check {
            fromExpr = "[[x ^ 2] / 6] + [x / 3] + [1 / 8] = 0"
            toExpr = "Solution[x, {-[3 / 2], -[1 / 2]}]"
            explanation {
                key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
            }

            step {
                fromExpr = "[[x ^ 2] / 6] + [x / 3] + [1 / 8] = 0"
                toExpr = "4 [x ^ 2] + 8 x + 3 = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify
                }

                step {
                    fromExpr = "[[x ^ 2] / 6] + [x / 3] + [1 / 8] = 0"
                    toExpr = "([[x ^ 2] / 6] + [x / 3] + [1 / 8]) * 24 = 0 * 24"
                }

                step {
                    fromExpr = "([[x ^ 2] / 6] + [x / 3] + [1 / 8]) * 24 = 0 * 24"
                    toExpr = "4 [x ^ 2] + 8 x + 3 = 0"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }
            }

            step { }
            step { }
            step { }
            step { }
        }
    }

    @Test
    fun `test solve (x^2 + a) by b + x by c = (x + d) by e`() = testMethodInX {
        method = EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula
        inputExpr = "[[x^2] + 1 / 2] + [x / 2] = [x + 1 / 3]"

        check {
            fromExpr = "[[x ^ 2] + 1 / 2] + [x / 2] = [x + 1 / 3]"
            toExpr = "Solution[x, {}]"
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
                toExpr = "3 [x ^ 2] + 3 + x = 2"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "3 [x ^ 2] + 3 + x = 2"
                toExpr = "3 [x ^ 2] + 1 + x = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheLeftAndSimplify
                }
            }

            step { }
            step { }
            step { }
        }
    }
}
