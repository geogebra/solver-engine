package methods.equations

import engine.context.Context
import engine.context.Curriculum
import engine.methods.testMethod
import engine.methods.testMethodInX
import methods.algebra.AlgebraExplanation
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.expand.ExpandExplanation
import methods.factor.FactorExplanation
import methods.polynomials.PolynomialsExplanation
import methods.rationalexpressions.RationalExpressionsExplanation
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength", "LargeClass")
class SolveRationalEquationTest {

    @Test
    fun `test rational equation with one valid solution`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"

        check {
            fromExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
            toExpr = "SetSolution[x : {[1 / 4]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            // not necessary to test domain
            task { }

            task {
                taskId = "#2"
                startExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
                    toExpr = "SetSolution[x: {-3, [1 / 4]}]"
                    explanation {
                        key = EquationsExplanation.SolveRationalEquation
                    }

                    step {
                        fromExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
                        toExpr = "[12 / (x - 3) (x + 3)] = [8 x / x - 3] - [2 / x + 3]"
                        explanation {
                            key = RationalExpressionsExplanation.FactorDenominatorOfFraction
                        }
                    }

                    step {
                        fromExpr = "[12 / (x - 3) (x + 3)] = [8 x / x - 3] - [2 / x + 3]"
                        toExpr = "[12 / (x - 3) (x + 3)] * <.(x - 3) (x + 3).> = [8 x / x - 3] * <.(x - 3) (x + 3).> - [2 / x + 3] * <.(x - 3) (x + 3).>"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesByDenominator
                        }
                    }

                    step {
                        fromExpr = "[12 / (x - 3) (x + 3)] * (x - 3) (x + 3) = [8 x / x - 3] * (x - 3) (x + 3) - [2 / x + 3] * (x - 3) (x + 3)"
                        toExpr = "12 = 8 x (x + 3) - 2 (x - 3)"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "12 = 8 x (x + 3) - 2 (x - 3)"
                        toExpr = "12 = 8 x (x + 3) - 2 x + 6"
                        explanation {
                            key = PolynomialsExplanation.ExpandSingleBracketWithIntegerCoefficient
                        }
                    }

                    step {
                        fromExpr = "12 = 8 x (x + 3) - 2 x + 6"
                        toExpr = "12 = 8 [x ^ 2] + 22 x + 6"
                        explanation {
                            key = PolynomialsExplanation.ExpandPolynomialExpression
                        }
                    }

                    step {
                        fromExpr = "12 = 8 [x ^ 2] + 22 x + 6"
                        toExpr = "6 - 8 [x ^ 2] - 22 x = 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                        }
                    }

                    step {
                        fromExpr = "6 - 8 [x ^ 2] - 22 x = 0"
                        toExpr = "-8 [x ^ 2] - 22 x + 6 = 0"
                        explanation {
                            key = PolynomialsExplanation.NormalizePolynomial
                        }
                    }

                    step {
                        fromExpr = "-8 [x ^ 2] - 22 x + 6 = 0"
                        toExpr = "8 [x ^ 2] + 22 x - 6 = 0"
                        explanation {
                            key = EquationsExplanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient
                        }
                    }

                    step {
                        fromExpr = "8 [x ^ 2] + 22 x - 6 = 0"
                        toExpr = "4 [x ^ 2] + 11 x - 3 = 0"
                        explanation {
                            key = EquationsExplanation.SimplifyByDividingByGcfOfCoefficients
                        }
                    }

                    step {
                        fromExpr = "4 [x ^ 2] + 11 x - 3 = 0"
                        toExpr = "x = [-11 +/- sqrt[[11 ^ 2] - 4 * 4 * (-3)] / 2 * 4]"
                        explanation {
                            key = EquationsExplanation.ApplyQuadraticFormula
                        }
                    }

                    step {
                        fromExpr = "x = [-11 +/- sqrt[[11 ^ 2] - 4 * 4 * (-3)] / 2 * 4]"
                        toExpr = "x = [-11 +/- 13 / 8]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }

                    step {
                        fromExpr = "x = [-11 +/- 13 / 8]"
                        toExpr = "x = [-11 - 13 / 8] OR x = [-11 + 13 / 8]"
                        explanation {
                            key = EquationsExplanation.SeparatePlusMinusQuadraticSolutions
                        }
                    }

                    step {
                        fromExpr = "x = [-11 - 13 / 8] OR x = [-11 + 13 / 8]"
                        toExpr = "SetSolution[x: {-3, [1 / 4]}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUnion
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "SetSolution[x : {[1 / 4]}]"
                explanation {
                    key = EquationsExplanation.SomeSolutionsDoNotSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test rational equation with one valid solution by US method`() = testMethod {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
        context = Context(solutionVariables = listOf("x"), curriculum = Curriculum.US)

        check {
            fromExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
            toExpr = "SetSolution[x: {[1 / 4]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            task {
                taskId = "#1"
                startExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                // same as the equation before
            }

            task {
                taskId = "#2"
                startExpr = "[12 / [(-3) ^ 2] - 9] = [8 * (-3) / -3 - 3] - [2 / -3 + 3]"
                explanation {
                    key = EquationsExplanation.CheckIfSolutionSatisfiesConstraint
                }

                step {
                    fromExpr = "[12 / [(-3) ^ 2] - 9] = [8 * (-3) / -3 - 3] - [2 / -3 + 3]"
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

            task {
                taskId = "#3"
                startExpr = "[12 / [([1 / 4]) ^ 2] - 9] = [8 * [1 / 4] / [1 / 4] - 3] - [2 / [1 / 4] + 3]"
                explanation {
                    key = EquationsExplanation.CheckIfSolutionSatisfiesConstraint
                }

                step {
                    fromExpr = "[12 / [([1 / 4]) ^ 2] - 9] = [8 * [1 / 4] / [1 / 4] - 3] - [2 / [1 / 4] + 3]"
                    toExpr = "-[192 / 143] = -[192 / 143]"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }

                step {
                    fromExpr = "-[192 / 143] = -[192 / 143]"
                    toExpr = "Identity[-[192 / 143] = -[192 / 143]]"
                    explanation {
                        key = EquationsExplanation.ExtractTruthFromTrueEquality
                    }
                }
            }

            task {
                taskId = "#4"
                startExpr = "SetSolution[x: {[1 / 4]}]"
                explanation {
                    key = EquationsExplanation.SomeSolutionsDoNotSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test solve equation with rational expression on both the sides`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"

        check {
            fromExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
            toExpr = "SetSolution[x : {-4, [1 / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            // not necessary to test domain
            task { }

            task {
                taskId = "#2"
                startExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                    toExpr = "SetSolution[x: {-4, [1 / 2]}]"
                    explanation {
                        key = EquationsExplanation.SolveRationalEquation
                    }

                    step {
                        fromExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                        toExpr = "[x + 2 / x - 3] * <.(x - 3) (3 x - 2).> = [x / 3 x - 2] * <.(x - 3) (3 x - 2).>"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesByDenominator
                        }
                    }

                    step {
                        fromExpr = "[x + 2 / x - 3] * (x - 3) (3 x - 2) = [x / 3 x - 2] * (x - 3) (3 x - 2)"
                        toExpr = "(x + 2) (3 x - 2) = x (x - 3)"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "(x + 2) (3 x - 2) = x (x - 3)"
                        toExpr = "(x + 2) (3 x - 2) - x (x - 3) = 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                        }
                    }

                    step {
                        fromExpr = "(x + 2) (3 x - 2) - x (x - 3) = 0"
                        toExpr = "(2 x - 1) (x + 4) = 0"
                        explanation {
                            key = FactorExplanation.FactorPolynomial
                        }
                    }

                    step {
                        fromExpr = "(2 x - 1) (x + 4) = 0"
                        toExpr = "2 x - 1 = 0 OR x + 4 = 0"
                        explanation {
                            key = EquationsExplanation.SeparateFactoredEquation
                        }
                    }

                    step {
                        fromExpr = "2 x - 1 = 0 OR x + 4 = 0"
                        toExpr = "SetSolution[x: {-4, [1 / 2]}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUnion
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "SetSolution[x : {-4, [1 / 2]}]"
                explanation {
                    key = EquationsExplanation.AllSolutionsSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test solve equation with rational expression on both the sides US method`() = testMethod {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
        context = Context(curriculum = Curriculum.US, solutionVariables = listOf("x"))

        check {
            fromExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
            toExpr = "SetSolution[x : {-4, [1 / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            task {
                taskId = "#1"
                startExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                // same as the equation before
            }

            task {
                taskId = "#2"
                startExpr = "[-4 + 2 / -4 - 3] = [-4 / 3 * (-4) - 2]"
                explanation {
                    key = EquationsExplanation.CheckIfSolutionSatisfiesConstraint
                }

                step {
                    fromExpr = "[-4 + 2 / -4 - 3] = [-4 / 3 * (-4) - 2]"
                    toExpr = "[2 / 7] = [2 / 7]"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }

                step {
                    fromExpr = "[2 / 7] = [2 / 7]"
                    toExpr = "Identity[[2 / 7] = [2 / 7]]"
                    explanation {
                        key = EquationsExplanation.ExtractTruthFromTrueEquality
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "[[1 / 2] + 2 / [1 / 2] - 3] = [[1 / 2] / 3 * [1 / 2] - 2]"
                explanation {
                    key = EquationsExplanation.CheckIfSolutionSatisfiesConstraint
                }

                step {
                    fromExpr = "[[1 / 2] + 2 / [1 / 2] - 3] = [[1 / 2] / 3 * [1 / 2] - 2]"
                    toExpr = "-1 = -1"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }

                step {
                    fromExpr = "-1 = -1"
                    toExpr = "Identity[-1 = -1]"
                    explanation {
                        key = EquationsExplanation.ExtractTruthFromTrueEquality
                    }
                }
            }

            task {
                taskId = "#4"
                startExpr = "SetSolution[x : {-4, [1 / 2]}]"
                explanation {
                    key = EquationsExplanation.AllSolutionsSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test cancel numerator and denominators to reduce to quadratic equation`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[[x ^ 2] + 5 x + 6 / x + 3] = [x + 2 / [x ^ 2] - 3 x - 10]"

        check {
            fromExpr = "[[x ^ 2] + 5 x + 6 / x + 3] = [x + 2 / [x ^ 2] - 3 x - 10]"
            toExpr = "SetSolution[x : {[3 - sqrt[53] / 2], [3 + sqrt[53] / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            task {
                taskId = "#1"
                startExpr = "[[x ^ 2] + 5 x + 6 / x + 3] = [x + 2 / [x ^ 2] - 3 x - 10]"
                explanation {
                    key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                }

                step {
                    fromExpr = "[[x ^ 2] + 5 x + 6 / x + 3] = [x + 2 / [x ^ 2] - 3 x - 10]"
                    toExpr = "SetSolution[x : /reals/ \\ {-3, -2, 5}]"
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "[[x ^ 2] + 5 x + 6 / x + 3] = [x + 2 / [x ^ 2] - 3 x - 10]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "[[x ^ 2] + 5 x + 6 / x + 3] = [x + 2 / [x ^ 2] - 3 x - 10]"
                    toExpr = "SetSolution[x: {[3 - sqrt[53] / 2], [3 + sqrt[53] / 2]}]"
                    explanation {
                        key = EquationsExplanation.SolveRationalEquation
                    }

                    step {
                        fromExpr = "[[x ^ 2] + 5 x + 6 / x + 3] = [x + 2 / [x ^ 2] - 3 x - 10]"
                        toExpr = "x + 2 = [1 / x - 5]"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "x + 2 = [1 / x - 5]"
                        toExpr = "x * (x - 5) + 2 * (x - 5) = [1 / x - 5] * (x - 5)"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesByDenominator
                        }
                    }

                    step {
                        fromExpr = "x * (x - 5) + 2 * (x - 5) = [1 / x - 5] * (x - 5)"
                        toExpr = "x (x - 5) + 2 (x - 5) = 1"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "x (x - 5) + 2 (x - 5) = 1"
                        toExpr = "x (x - 5) + 2 x - 10 = 1"
                        explanation {
                            key = PolynomialsExplanation.ExpandSingleBracketWithIntegerCoefficient
                        }
                    }

                    step {
                        fromExpr = "x (x - 5) + 2 x - 10 = 1"
                        toExpr = "[x ^ 2] - 3 x - 10 = 1"
                        explanation {
                            key = PolynomialsExplanation.ExpandPolynomialExpression
                        }
                    }

                    step {
                        fromExpr = "[x ^ 2] - 3 x - 10 = 1"
                        toExpr = "[x ^ 2] - 3 x - 11 = 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                        }
                    }

                    step {
                        fromExpr = "[x ^ 2] - 3 x - 11 = 0"
                        toExpr = "x = [-(-3) +/- sqrt[[(-3) ^ 2] - 4 * 1 * (-11)] / 2 * 1]"
                        explanation {
                            key = EquationsExplanation.ApplyQuadraticFormula
                        }
                    }

                    step {
                        fromExpr = "x = [-(-3) +/- sqrt[[(-3) ^ 2] - 4 * 1 * (-11)] / 2 * 1]"
                        toExpr = "x = [3 +/- sqrt[53] / 2]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }

                    step {
                        fromExpr = "x = [3 +/- sqrt[53] / 2]"
                        toExpr = "SetSolution[x: {[3 - sqrt[53] / 2], [3 + sqrt[53] / 2]}]"
                        explanation {
                            key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "SetSolution[x : {[3 - sqrt[53] / 2], [3 + sqrt[53] / 2]}]"
                explanation {
                    key = EquationsExplanation.AllSolutionsSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test lcd polynomial and number`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[12 x / x - 9] + [1 / 3] = 8"

        check {
            fromExpr = "[12 x / x - 9] + [1 / 3] = 8"
            toExpr = "SetSolution[x : {-[207 / 13]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            // domain computation task, not necessary to test
            task { }

            task {
                taskId = "#2"
                startExpr = "[12 x / x - 9] + [1 / 3] = 8"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "[12 x / x - 9] + [1 / 3] = 8"
                    toExpr = "SetSolution[x: {-[207 / 13]}]"
                    explanation {
                        key = EquationsExplanation.SolveRationalEquation
                    }

                    step {
                        fromExpr = "[12 x / x - 9] + [1 / 3] = 8"
                        toExpr = "[12 x / x - 9] * <.3 (x - 9).> + [1 / 3] * <.3 (x - 9).> = 8 * <.3 (x - 9).>"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesByDenominator
                        }
                    }

                    step {
                        fromExpr = "[12 x / x - 9] * 3 (x - 9) + [1 / 3] * 3 (x - 9) = 8 * 3 (x - 9)"
                        toExpr = "37 x - 9 = 24 (x - 9)"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "37 x - 9 = 24 (x - 9)"
                        toExpr = "37 x - 9 = 24 x - 216"
                        explanation {
                            key = ExpandExplanation.ExpandSingleBracketAndSimplify
                        }
                    }

                    step {
                        fromExpr = "37 x - 9 = 24 x - 216"
                        toExpr = "13 x - 9 = -216"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveVariablesToTheLeftAndSimplify
                        }
                    }

                    step {
                        fromExpr = "13 x - 9 = -216"
                        toExpr = "13 x = -207"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
                        }
                    }

                    step {
                        fromExpr = "13 x = -207"
                        toExpr = "x = -[207 / 13]"
                        explanation {
                            key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                        }
                    }

                    step {
                        fromExpr = "x = -[207 / 13]"
                        toExpr = "SetSolution[x: {-[207 / 13]}]"
                        explanation {
                            key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "SetSolution[x : {-[207 / 13]}]"
                explanation {
                    key = EquationsExplanation.AllSolutionsSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test same denominator in rational equation`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[12x / [x^2] - 9] - [1/[x^2] - 9] = 8"

        check {
            fromExpr = "[12 x / [x ^ 2] - 9] - [1 / [x ^ 2] - 9] = 8"
            toExpr = "SetSolution[x : {[12 - 4 sqrt[151] / 16], [12 + 4 sqrt[151] / 16]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            // no need to test computation of domain
            task { }

            task {
                taskId = "#2"
                startExpr = "[12 x / [x ^ 2] - 9] - [1 / [x ^ 2] - 9] = 8"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "[12 x / [x ^ 2] - 9] - [1 / [x ^ 2] - 9] = 8"
                    toExpr = "SetSolution[x: {[12 - 4 sqrt[151] / 16], [12 + 4 sqrt[151] / 16]}]"
                    explanation {
                        key = EquationsExplanation.SolveRationalEquation
                    }

                    step {
                        fromExpr = "[12 x / [x ^ 2] - 9] - [1 / [x ^ 2] - 9] = 8"
                        toExpr = "[12 x / [x ^ 2] - 9] * ([x ^ 2] - 9) - [1 / [x ^ 2] - 9] * ([x ^ 2] - 9) = 8 * ([x ^ 2] - 9)"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesByDenominator
                        }
                    }

                    step {
                        fromExpr = "[12 x / [x ^ 2] - 9] * ([x ^ 2] - 9) - [1 / [x ^ 2] - 9] * ([x ^ 2] - 9) = 8 * ([x ^ 2] - 9)"
                        toExpr = "12 x - 1 = 8 ([x ^ 2] - 9)"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "12 x - 1 = 8 ([x ^ 2] - 9)"
                        toExpr = "12 x - 1 = 8 [x ^ 2] - 72"
                        explanation {
                            key = ExpandExplanation.ExpandSingleBracketAndSimplify
                        }
                    }

                    step {
                        fromExpr = "12 x - 1 = 8 [x ^ 2] - 72"
                        toExpr = "12 x + 71 - 8 [x ^ 2] = 0"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                        }
                    }

                    step {
                        fromExpr = "12 x + 71 - 8 [x ^ 2] = 0"
                        toExpr = "-8 [x ^ 2] + 12 x + 71 = 0"
                        explanation {
                            key = PolynomialsExplanation.NormalizePolynomial
                        }
                    }

                    step {
                        fromExpr = "-8 [x ^ 2] + 12 x + 71 = 0"
                        toExpr = "8 [x ^ 2] - 12 x - 71 = 0"
                        explanation {
                            key = EquationsExplanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient
                        }
                    }

                    step {
                        fromExpr = "8 [x ^ 2] - 12 x - 71 = 0"
                        toExpr = "x = [-(-12) +/- sqrt[[(-12) ^ 2] - 4 * 8 * (-71)] / 2 * 8]"
                        explanation {
                            key = EquationsExplanation.ApplyQuadraticFormula
                        }
                    }

                    step {
                        fromExpr = "x = [-(-12) +/- sqrt[[(-12) ^ 2] - 4 * 8 * (-71)] / 2 * 8]"
                        toExpr = "x = [12 +/- 4 sqrt[151] / 16]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }

                    step {
                        fromExpr = "x = [12 +/- 4 sqrt[151] / 16]"
                        toExpr = "SetSolution[x: {[12 - 4 sqrt[151] / 16], [12 + 4 sqrt[151] / 16]}]"
                        explanation {
                            key = EquationsExplanation.ExtractSolutionFromEquationInPlusMinusForm
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "SetSolution[x : {[12 - 4 sqrt[151] / 16], [12 + 4 sqrt[151] / 16]}]"
                explanation {
                    key = EquationsExplanation.AllSolutionsSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test one of the term is a squareRoot`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[12 / [x^2] - 9] = sqrt[2] - [2 /x + 3]"

        check {
            fromExpr = "[12 / [x ^ 2] - 9] = sqrt[2] - [2 / x + 3]"
            // on one of the good days, we will be able to simplify the below
            // result to "3 + 2sqrt[2]"
            toExpr = "SetSolution[x : {[2 sqrt[2] + 2 sqrt[38 + 12 sqrt[2]] / 4]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            // no need to test the domain computation task
            task { }

            task {
                taskId = "#2"
                startExpr = "[12 / [x ^ 2] - 9] = sqrt[2] - [2 / x + 3]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "[12 / [x ^ 2] - 9] = sqrt[2] - [2 / x + 3]"
                    toExpr = "SetSolution[x: {[2 sqrt[2] - 2 sqrt[38 + 12 sqrt[2]] / 4], [2 sqrt[2] + 2 sqrt[38 + 12 sqrt[2]] / 4]}]"
                    explanation {
                        key = EquationsExplanation.SolveRationalEquation
                    }

                    step {
                        fromExpr = "[12 / [x ^ 2] - 9] = sqrt[2] - [2 / x + 3]"
                        toExpr = "[12 / (x - 3) (x + 3)] = sqrt[2] - [2 / x + 3]"
                        explanation {
                            key = RationalExpressionsExplanation.FactorDenominatorOfFraction
                        }
                    }

                    step {
                        fromExpr = "[12 / (x - 3) (x + 3)] = sqrt[2] - [2 / x + 3]"
                        toExpr = "[12 / (x - 3) (x + 3)] * <. (x - 3) (x + 3) .> = sqrt[2] * <. (x - 3) (x + 3) .> - [2 / x + 3] * <. (x - 3) (x + 3) .>"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesByDenominator
                        }
                    }

                    step {
                        fromExpr =
                            "[12 / (x - 3) (x + 3)] * (x - 3) (x + 3) = sqrt[2] * (x - 3) (x + 3) - [2 / x + 3] * (x - 3) (x + 3)"
                        toExpr = "12 = sqrt[2] (x - 3) (x + 3) - 2 (x - 3)"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    repeat(9) {
                        // This solution has way too many steps!!
                        step { }
                    }

                    step {
                        fromExpr = "x = [2 sqrt[2] + (-2 sqrt[19 + 6 sqrt[2]]) sqrt[2] / 4] OR x = [2 sqrt[2] + (2 sqrt[19 + 6 sqrt[2]]) sqrt[2] / 4]"
                        toExpr = "SetSolution[x: {[2 sqrt[2] - 2 sqrt[38 + 12 sqrt[2]] / 4], [2 sqrt[2] + 2 sqrt[38 + 12 sqrt[2]] / 4]}]"
                        explanation {
                            key = EquationsExplanation.SolveEquationUnion
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "SetSolution[x : {[2 sqrt[2] + 2 sqrt[38 + 12 sqrt[2]] / 4]}]"
                explanation {
                    key = EquationsExplanation.SomeSolutionsDoNotSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test simplify power of lhs of rational equation`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[([x / x + 1]) ^ 2] = 1"

        check {
            fromExpr = "[([x / x + 1]) ^ 2] = 1"
            toExpr = "SetSolution[x : {-[1 / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            // domain computation task, not necessary to test it
            task { }

            task {
                taskId = "#2"
                startExpr = "[([x / x + 1]) ^ 2] = 1"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "[([x / x + 1]) ^ 2] = 1"
                    toExpr = "SetSolution[x: {-[1 / 2]}]"
                    explanation {
                        key = EquationsExplanation.SolveRationalEquation
                    }

                    step {
                        fromExpr = "[([x / x + 1]) ^ 2] = 1"
                        toExpr = "[[x ^ 2] / [(x + 1) ^ 2]] = 1"
                        explanation {
                            key = RationalExpressionsExplanation.SimplifyPowerOfRationalExpression
                        }
                    }

                    step {
                        fromExpr = "[[x ^ 2] / [(x + 1) ^ 2]] = 1"
                        toExpr = "[[x ^ 2] / [(x + 1) ^ 2]] * [(x + 1) ^ 2] = 1 * [(x + 1) ^ 2]"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesByDenominator
                        }
                    }

                    step {
                        fromExpr = "[[x ^ 2] / [(x + 1) ^ 2]] * [(x + 1) ^ 2] = 1 * [(x + 1) ^ 2]"
                        toExpr = "[x ^ 2] = [(x + 1) ^ 2]"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "[x ^ 2] = [(x + 1) ^ 2]"
                        toExpr = "[x ^ 2] = [x ^ 2] + 2 x + 1"
                        explanation {
                            key = ExpandExplanation.ExpandBinomialSquaredAndSimplify
                        }
                    }

                    step {
                        fromExpr = "[x ^ 2] = [x ^ 2] + 2 x + 1"
                        toExpr = "0 = 2 x + 1"
                        explanation {
                            key = methods.solvable.EquationsExplanation.CancelCommonTermsOnBothSides
                        }
                    }

                    step {
                        fromExpr = "0 = 2 x + 1"
                        toExpr = "-1 = 2 x"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveConstantsToTheLeftAndSimplify
                        }
                    }

                    step {
                        fromExpr = "-1 = 2 x"
                        toExpr = "2 x = -1"
                        explanation {
                            key = methods.solvable.EquationsExplanation.FlipEquation
                        }
                    }

                    step {
                        fromExpr = "2 x = -1"
                        toExpr = "x = -[1 / 2]"
                        explanation {
                            key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                        }
                    }

                    step {
                        fromExpr = "x = -[1 / 2]"
                        toExpr = "SetSolution[x: {-[1 / 2]}]"
                        explanation {
                            key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "SetSolution[x : {-[1 / 2]}]"
                explanation {
                    key = EquationsExplanation.AllSolutionsSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test solve p(x) over r(x) = q(x) over r(x)`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[2 x / x] = [4 / x]"

        check {
            fromExpr = "[2 x / x] = [4 / x]"
            toExpr = "SetSolution[x : {2}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            // not necessary to test the domain computation task
            task { }

            task {
                taskId = "#2"
                startExpr = "[2 x / x] = [4 / x]"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "[2 x / x] = [4 / x]"
                    toExpr = "SetSolution[x: {2}]"
                    explanation {
                        key = EquationsExplanation.SolveRationalEquation
                    }

                    step {
                        fromExpr = "[2 x / x] = [4 / x]"
                        toExpr = "[2 x / x] * x = [4 / x] * x"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesByDenominator
                        }
                    }

                    step {
                        fromExpr = "[2 x / x] * x = [4 / x] * x"
                        toExpr = "2 x = 4"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "2 x = 4"
                        toExpr = "x = 2"
                        explanation {
                            key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
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

            task {
                taskId = "#3"
                startExpr = "SetSolution[x : {2}]"
                explanation {
                    key = EquationsExplanation.AllSolutionsSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test rational equation reducible to polynomial by expanding`() = testMethodInX {
        method = EquationsPlans.SolveEquation
        inputExpr = "([1 / x] + 2) x = [2 / x] x"

        check {
            fromExpr = "([1 / x] + 2) x = [2 / x] x"
            toExpr = "SetSolution[x: {[1 / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquation
            }

            // not necessary to test the domain computation task
            task { }

            task {
                taskId = "#2"
                startExpr = "([1 / x] + 2) x = [2 / x] x"
                explanation {
                    key = EquationsExplanation.SolveEquation
                }

                step {
                    fromExpr = "([1 / x] + 2) x = [2 / x] x"
                    toExpr = "SetSolution[x: {[1 / 2]}]"
                    explanation {
                        key = EquationsExplanation.SolveRationalEquation
                    }

                    step {
                        fromExpr = "([1 / x] + 2) x = [2 / x] x"
                        toExpr = "x ([1 / x] + 2) = 2"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }
                    }

                    step {
                        fromExpr = "x ([1 / x] + 2) = 2"
                        toExpr = "1 + 2 x = 2"
                        explanation {
                            key = PolynomialsExplanation.ExpandPolynomialExpression
                        }
                    }

                    step {
                        fromExpr = "1 + 2 x = 2"
                        toExpr = "2 x = 1"
                        explanation {
                            key = methods.solvable.EquationsExplanation.MoveConstantsToTheRightAndSimplify
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

            task {
                taskId = "#3"
                startExpr = "SetSolution[x: {[1 / 2]}]"
                explanation {
                    key = EquationsExplanation.AllSolutionsSatisfyConstraint
                }
            }
        }
    }
}
