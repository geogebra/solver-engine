package methods.equations

import engine.methods.testMethodInX
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class LinearEquationsTest {

    @Test
    fun `test ax = b linear equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "3 x = 1"

        check {
            fromExpr = "3 x = 1"
            toExpr = "SetSolution[x: {[1 / 3]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "3 x = 1"
                toExpr = "x = [1 / 3]"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }

                step {
                    fromExpr = "3 x = 1"
                    toExpr = "[3 x / 3] = [1 / 3]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariable
                    }
                }

                step {
                    fromExpr = "[3 x / 3] = [1 / 3]"
                    toExpr = "x = [1 / 3]"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }
            }

            step {
                fromExpr = "x = [1 / 3]"
                toExpr = "SetSolution[x: {[1 / 3]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test x div a = b linear equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "[x / 9] = -1"

        check {
            fromExpr = "[x / 9] = -1"
            toExpr = "SetSolution[x: {-9}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            // should not happen
            step {
                fromExpr = "[x / 9] = -1"
                toExpr = "x = -9"
                explanation {
                    key = methods.solvable.EquationsExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -9"
                toExpr = "SetSolution[x: {-9}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test x - a = b linear equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "x - 2 = 36"

        check {
            fromExpr = "x - 2 = 36"
            toExpr = "SetSolution[x: {38}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "x - 2 = 36"
                toExpr = "x = 38"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
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
                        key = EquationsExplanation.SimplifyEquation
                    }
                }
            }

            step {
                fromExpr = "x = 38"
                toExpr = "SetSolution[x: {38}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test a = b - cx linear equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "1 = [3 / 5] - x"

        check {
            fromExpr = "1 = [3 / 5] - x"
            toExpr = "SetSolution[x: {-[2 / 5]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "1 = [3 / 5] - x"
                toExpr = "1 + x = [3 / 5]"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "1 + x = [3 / 5]"
                toExpr = "x = -[2 / 5]"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "x = -[2 / 5]"
                toExpr = "SetSolution[x: {-[2 / 5]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test a = x equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "1 + [2 / 3] = x"

        check {
            fromExpr = "1 + [2 / 3] = x"
            toExpr = "SetSolution[x: {[5 / 3]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "1 + [2 / 3] = x"
                toExpr = "[5 / 3] = x"
                explanation {
                    key = FractionArithmeticExplanation.AddIntegerAndFraction
                }
            }

            step {
                fromExpr = "[5 / 3] = x"
                toExpr = "x = [5 / 3]"
                explanation {
                    key = methods.solvable.EquationsExplanation.FlipEquation
                }
            }

            step {
                fromExpr = "x = [5 / 3]"
                toExpr = "SetSolution[x: {[5 / 3]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test a - bx = 0 linear equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "1 - 3 x = 0"

        check {
            fromExpr = "1 - 3 x = 0"
            toExpr = "SetSolution[x: {[1 / 3]}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "1 - 3 x = 0"
                toExpr = "-3 x = -1"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "-3 x = -1"
                toExpr = "x = [1 / 3]"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = [1 / 3]"
                toExpr = "SetSolution[x: {[1 / 3]}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test a = b + cx linear equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "4 = 11 + [x / 3]"

        check {
            fromExpr = "4 = 11 + [x / 3]"
            toExpr = "SetSolution[x: {-21}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "4 = 11 + [x / 3]"
                toExpr = "-7 = [x / 3]"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "-7 = [x / 3]"
                toExpr = "[x / 3] = -7"
                explanation {
                    key = methods.solvable.EquationsExplanation.FlipEquation
                }
            }

            step {
                fromExpr = "[x / 3] = -7"
                toExpr = "x = -21"
                explanation {
                    key = methods.solvable.EquationsExplanation.MultiplyByInverseCoefficientOfVariableAndSimplify
                }
            }

            step {
                fromExpr = "x = -21"
                toExpr = "SetSolution[x: {-21}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test ax + b = cx + d linear equation with c larger than a`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "x + 1 = 2 x + 3"

        check {
            fromExpr = "x + 1 = 2 x + 3"
            toExpr = "SetSolution[x: {-2}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "x + 1 = 2 x + 3"
                toExpr = "1 = x + 3"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveVariablesToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "1 = x + 3"
                toExpr = "-2 = x"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "-2 = x"
                toExpr = "x = -2"
                explanation {
                    key = methods.solvable.EquationsExplanation.FlipEquation
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
    fun `test expanding brackets in linear equation`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "3 (x + 1) - 2 (x - 6) = 0"

        check {
            fromExpr = "3 (x + 1) - 2 (x - 6) = 0"
            toExpr = "SetSolution[x: {-15}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "3 (x + 1) - 2 (x - 6) = 0"
                toExpr = "x + 15 = 0"
                explanation {
                    key = PolynomialsExplanation.ExpandSingleBracketWithIntegerCoefficient
                }
            }

            step {
                fromExpr = "x + 15 = 0"
                toExpr = "x = -15"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                }
            }

            step {
                fromExpr = "x = -15"
                toExpr = "SetSolution[x: {-15}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test cancellation of opposites happens on one side before it happens on both sides`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "2 x + x + 5 - 5 = x + 9 - 5"

        check {
            fromExpr = "2 x + x + 5 - 5 = x + 9 - 5"
            toExpr = "SetSolution[x: {2}]"
            explanation {
                key = EquationsExplanation.SolveLinearEquation
            }

            step {
                fromExpr = "2 x + x + 5 - 5 = x + 9 - 5"
                toExpr = "2 x = 4"
                explanation {
                    key = EquationsExplanation.SimplifyEquation
                }

                step {
                    fromExpr = "2 x + x + 5 - 5 = x + 9 - 5"
                    toExpr = "2 x + x = x + 9 - 5"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }

                step {
                    fromExpr = "2 x + x = x + 9 - 5"
                    toExpr = "2 x = 9 - 5"
                    explanation {
                        key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                    }
                }

                step {
                    fromExpr = "2 x = 9 - 5"
                    toExpr = "2 x = 4"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }

                    step {
                        fromExpr = "9 - 5"
                        toExpr = "4"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                        }
                    }
                }
            }

            step {
                fromExpr = "2 x = 4"
                toExpr = "x = 2"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }

                step {
                    fromExpr = "2 x = 4"
                    toExpr = "[2 x / 2] = [4 / 2]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariable
                    }
                }

                step {
                    fromExpr = "[2 x / 2] = [4 / 2]"
                    toExpr = "x = 2"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }
            }

            step {
                fromExpr = "x = 2"
                toExpr = "SetSolution[x: {2}]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                }
            }
        }
    }

    @Test
    fun `test linear equations with no solutions`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "6 x + 6 = 6 x - 5"

        check {
            fromExpr = "6 x + 6 = 6 x - 5"
            toExpr = "Contradiction[x: 6 = -5]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
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
                toExpr = "Contradiction[x: 6 = -5]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromContradiction
                }
            }
        }
    }

    @Test
    fun `test linear equation with infinitely many solutions`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "6 x + 6 = 6 x + 3 + 3"

        check {
            fromExpr = "6 x + 6 = 6 x + 3 + 3"
            toExpr = "Identity[x: 6 = 6]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            step {
                fromExpr = "6 x + 6 = 6 x + 3 + 3"
                toExpr = "6 = 6"

                explanation {
                    key = EquationsExplanation.SimplifyEquation
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
            }

            step {
                fromExpr = "6 = 6"
                toExpr = "Identity[x: 6 = 6]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromIdentity
                }
            }
        }
    }

    @Test
    fun `test linear equation with no variable`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "1 = 2"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test linear equation with two variables`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "x + 1 = 2x + y - 2"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test linear equation without solution variable`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "y + 1 = 2y"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test abs(-x) - abs(x) = 0`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "abs[-x] - abs[x] = 0"

        check {
            fromExpr = "abs[-x] - abs[x] = 0"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }
            toExpr = "Identity[x: 0 = 0]"

            step {
                fromExpr = "abs[-x] - abs[x] = 0"
                toExpr = "0 = 0"
                explanation {
                    key = EquationsExplanation.SimplifyEquation
                }

                step {
                    fromExpr = "abs[-x] - abs[x] = 0"
                    toExpr = "abs[x] - abs[x] = 0"
                    explanation {
                        key = GeneralExplanation.EvaluateAbsoluteValue
                    }

                    step {
                        fromExpr = "abs[-x]"
                        toExpr = "abs[x]"
                        explanation {
                            key = GeneralExplanation.SimplifyAbsoluteValueOfNegatedExpression
                        }
                    }
                }

                step {
                    fromExpr = "abs[x] - abs[x] = 0"
                    toExpr = "0 = 0"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }
            }

            step {
                fromExpr = "0 = 0"
                toExpr = "Identity[x: 0 = 0]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromIdentity
                }
            }
        }
    }

    @Test
    fun `test abs(-x - sqrt(2)) - abs(x + sqrt(2)) = 0`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "abs[-x - sqrt[2]] - abs[x + sqrt[2]] = 0"

        check {
            fromExpr = "abs[-x - sqrt[2]] - abs[x + sqrt[2]] = 0"
            toExpr = "Identity[x: 0 = 0]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            step {
                fromExpr = "abs[-x - sqrt[2]] - abs[x + sqrt[2]] = 0"
                toExpr = "0 = 0"
                explanation {
                    key = EquationsExplanation.SimplifyEquation
                }
                step {
                    fromExpr = "abs[-x - sqrt[2]] - abs[x + sqrt[2]] = 0"
                    toExpr = "abs[x + sqrt[2]] - abs[x + sqrt[2]] = 0"
                    explanation {
                        key = GeneralExplanation.EvaluateAbsoluteValue
                    }
                    step {
                        fromExpr = "abs[-x - sqrt[2]]"
                        toExpr = "abs[-(x + sqrt[2])]"
                        explanation {
                            key = GeneralExplanation.FactorMinusFromSum
                        }
                    }
                    step {
                        fromExpr = "abs[-(x + sqrt[2])]"
                        toExpr = "abs[x + sqrt[2]]"
                        explanation {
                            key = GeneralExplanation.SimplifyAbsoluteValueOfNegatedExpression
                        }
                    }
                }
                step {
                    fromExpr = "abs[x + sqrt[2]] - abs[x + sqrt[2]] = 0"
                    toExpr = "0 = 0"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }
            }

            step { }
        }
    }
}
