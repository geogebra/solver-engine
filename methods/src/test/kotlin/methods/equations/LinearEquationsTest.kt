package methods.equations

import engine.context.Context
import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class LinearEquationsTest {

    @Test
    fun `test ax = b linear equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "3 x = 1"

        check {
            fromExpr = "3 x = 1"
            toExpr = "Solution[x, {[1 / 3]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "3 x = 1"
                toExpr = "x = [1 / 3]"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }

                step {
                    fromExpr = "3 x = 1"
                    toExpr = "[3 x / 3] = [1 / 3]"
                    explanation {
                        key = EquationsExplanation.DivideByCoefficientOfVariable
                    }
                }

                step {
                    fromExpr = "[3 x / 3] = [1 / 3]"
                    toExpr = "x = [1 / 3]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }

            step {
                fromExpr = "x = [1 / 3]"
                toExpr = "Solution[x, {[1 / 3]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test x div a = b linear equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "[x / 9] = -1"

        check {
            fromExpr = "[x / 9] = -1"
            toExpr = "Solution[x, {-9}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            // should not happen
            step {
                fromExpr = "[x / 9] = -1"
                toExpr = "[1 / 9] x = -1"
                explanation {
                    key = PolynomialsExplanation.NormalizeMonomialAndSimplify
                }
            }

            step {
                fromExpr = "[1 / 9] x = -1"
                toExpr = "x = -9"
                explanation {
                    key = EquationsExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -9"
                toExpr = "Solution[x, {-9}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test x - a = b linear equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "x - 2 = 36"

        check {
            fromExpr = "x - 2 = 36"
            toExpr = "Solution[x, {38}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "x - 2 = 36"
                toExpr = "x = 38"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }

                step {
                    fromExpr = "x - 2 = 36"
                    toExpr = "x - 2 + 2 = 36 + 2"
                    explanation {
                        key = methods.solvable.EquationsExplanation.MoveConstantsToTheRight
                    }
                }

                step {
                    fromExpr = "x - 2 + 2 = 36 + 2"
                    toExpr = "x = 38"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }

            step {
                fromExpr = "x = 38"
                toExpr = "Solution[x, {38}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test a = b - cx linear equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "1 = [3 / 5] - x"

        check {
            fromExpr = "1 = [3 / 5] - x"
            toExpr = "Solution[x, {-[2 / 5]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            // should not happen
            step {
                fromExpr = "1 = [3 / 5] - x"
                toExpr = "1 = -x + [3 / 5]"
                explanation {
                    key = PolynomialsExplanation.NormalizePolynomial
                }
            }

            step {
                fromExpr = "1 = -x + [3 / 5]"
                toExpr = "x + 1 = [3 / 5]"
                explanation {
                    key = EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "x + 1 = [3 / 5]"
                toExpr = "x = -[2 / 5]"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "x = -[2 / 5]"
                toExpr = "Solution[x, {-[2 / 5]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test a = x equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "1 + [2 / 3] = x"

        check {
            fromExpr = "1 + [2 / 3] = x"
            toExpr = "Solution[x, {[5 / 3]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "1 + [2 / 3] = x"
                toExpr = "[5 / 3] = x"
                explanation {
                    key = FractionArithmeticExplanation.EvaluateSumOfFractionAndInteger
                }
            }

            step {
                fromExpr = "[5 / 3] = x"
                toExpr = "x = [5 / 3]"
                explanation {
                    key = EquationsExplanation.FlipEquation
                }
            }

            step {
                fromExpr = "x = [5 / 3]"
                toExpr = "Solution[x, {[5 / 3]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test a - bx = 0 linear equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "1 - 3 x = 0"

        check {
            fromExpr = "1 - 3 x = 0"
            toExpr = "Solution[x, {[1 / 3]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "1 - 3 x = 0"
                toExpr = "-3 x + 1 = 0"
                explanation {
                    key = PolynomialsExplanation.NormalizePolynomial
                }
            }

            step {
                fromExpr = "-3 x + 1 = 0"
                toExpr = "-3 x = -1"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "-3 x = -1"
                toExpr = "x = [1 / 3]"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = [1 / 3]"
                toExpr = "Solution[x, {[1 / 3]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test a = b + cx linear equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "4 = 11 + [x / 3]"

        check {
            fromExpr = "4 = 11 + [x / 3]"
            toExpr = "Solution[x, {-21}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            // should not happen
            step {
                fromExpr = "4 = 11 + [x / 3]"
                toExpr = "4 = [1 / 3] x + 11"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }

            step {
                fromExpr = "4 = [1 / 3] x + 11"
                toExpr = "-7 = [1 / 3] x"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "-7 = [1 / 3] x"
                toExpr = "[1 / 3] x = -7"
                explanation {
                    key = EquationsExplanation.FlipEquation
                }
            }

            step {
                fromExpr = "[1 / 3] x = -7"
                toExpr = "x = -21"
                explanation {
                    key = EquationsExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -21"
                toExpr = "Solution[x, {-21}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test expanding brackets in linear equation`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "3 (x + 1) - 2 (x - 6) = 0"

        check {
            fromExpr = "3 (x + 1) - 2 (x - 6) = 0"
            toExpr = "Solution[x, {-15}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "3 (x + 1) - 2 (x - 6) = 0"
                toExpr = "x + 15 = 0"
                explanation {
                    key = PolynomialsExplanation.ExpandPolynomialExpression
                }
            }

            step {
                fromExpr = "x + 15 = 0"
                toExpr = "x = -15"
                explanation {
                    key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "x = -15"
                toExpr = "Solution[x, {-15}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test multiplying through by the LCD in linear equations`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "[x - 14 / 12] - [2 x - 1 / 18] = [2 / 9] (2 x - 5)"

        check {
            fromExpr = "[x - 14 / 12] - [2 x - 1 / 18] = [2 / 9] (2 x - 5)"
            toExpr = "Solution[x, {0}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "[x - 14 / 12] - [2 x - 1 / 18] = [2 / 9] (2 x - 5)"
                toExpr = "-x - 40 = 16 x - 40"
                explanation {
                    key = EquationsExplanation.MultiplyByLCDAndSimplify
                }
            }

            step {
                fromExpr = "-x - 40 = 16 x - 40"
                toExpr = "-x = 16 x"
                explanation {
                    key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                }
            }

            step {
                fromExpr = "-x = 16 x"
                toExpr = "-17 x = 0"
                explanation {
                    key = EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "-17 x = 0"
                toExpr = "x = 0"
                explanation {
                    key = EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = 0"
                toExpr = "Solution[x, {0}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test linear equations with no solutions`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "6 x + 6 = 6 x - 5"

        check {
            fromExpr = "6 x + 6 = 6 x - 5"
            toExpr = "Solution[x, {}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "6 x + 6 = 6 x - 5"
                toExpr = "6 = -5"
                explanation {
                    key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                }
            }

            step {
                fromExpr = "6 = -5"
                toExpr = "Solution[x, {}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromContradiction
                }
            }
        }
    }

    @Test
    fun `test linear equation with infinitely many solutions`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveLinearEquation
        inputExpr = "6 x + 6 = 6 x + 3 + 3"

        check {
            fromExpr = "6 x + 6 = 6 x + 3 + 3"
            toExpr = "Solution[x, REALS]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "6 x + 6 = 6 x + 3 + 3"
                toExpr = "6 = 3 + 3"
                explanation {
                    key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                }
            }

            step {
                fromExpr = "6 = 3 + 3"
                toExpr = "6 = 6"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }

            step {
                fromExpr = "6 = 6"
                toExpr = "Solution[x, REALS]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromIdentity
                }
            }
        }
    }
}
