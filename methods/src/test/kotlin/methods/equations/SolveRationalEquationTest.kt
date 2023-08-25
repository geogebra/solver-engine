
import engine.context.Context
import engine.context.Curriculum
import engine.methods.testMethod
import engine.methods.testMethodInX
import methods.algebra.AlgebraExplanation
import methods.equations.EquationsExplanation
import methods.equations.EquationsPlans
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.rationalexpressions.RationalExpressionsExplanation
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength", "LargeClass")
class SolveRationalEquationTest {
    @Test
    fun `test rational equation with one valid solution`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[12 / [x^2] - 9] = [8x / x - 3] - [2 /x + 3]"

        check {
            fromExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
            toExpr = "SetSolution[x : {[1 / 4]}]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            // not necessary to test domain
            task { }

            task {
                taskId = "#2"
                startExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
                explanation {
                    key = EquationsExplanation.SimplifyToPolynomialEquation
                }

                step {
                    fromExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
                    toExpr = "12 = 8 x (x + 3) - 2 (x - 3)"
                    explanation {
                        key = EquationsExplanation.SimplifyToPolynomialEquation
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
                        explanation {
                            key = EquationsExplanation.FactorDenominatorOfFraction
                        }

                        step {
                            fromExpr = "[12 / [x ^ 2] - 9] = [8 x / x - 3] - [2 / x + 3]"
                            toExpr = "[12 / (x - 3) (x + 3)] = [8 x / x - 3] - [2 / x + 3]"
                            explanation {
                                key = RationalExpressionsExplanation.FactorDenominatorOfFraction
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "(x - 3) (x + 3)"
                        explanation {
                            key = EquationsExplanation.ComputeLeastCommonDenominatorOfFractions
                        }
                    }

                    task {
                        taskId = "#3"
                        startExpr = "[12 / (x - 3) (x + 3)] = [8 x / x - 3] - [2 / x + 3]"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                        }

                        step {
                            fromExpr = "[12 / (x - 3) (x + 3)] = [8 x / x - 3] - [2 / x + 3]"
                            toExpr = "12 = 8 x (x + 3) - 2 (x - 3)"
                            explanation {
                                key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                            }

                            task {
                                taskId = "#1"
                                startExpr = "[12 / (x - 3) (x + 3)] (x - 3) (x + 3)"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "[12 / (x - 3) (x + 3)] (x - 3) (x + 3)"
                                    toExpr = "12"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "[12 / (x - 3) (x + 3)] (x - 3) (x + 3)"
                                        toExpr = "[12 (x - 3) (x + 3) / (x - 3) (x + 3)]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "[12 (x - 3) (x + 3) / (x - 3) (x + 3)]"
                                        toExpr = "12"
                                        explanation {
                                            key = FractionArithmeticExplanation.SimplifyFraction
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#2"
                                startExpr = "[8 x / x - 3] (x - 3) (x + 3)"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "[8 x / x - 3] (x - 3) (x + 3)"
                                    toExpr = "8 x (x + 3)"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "[8 x / x - 3] (x - 3) (x + 3)"
                                        toExpr = "[8 x (x - 3) (x + 3) / x - 3]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "[8 x (x - 3) (x + 3) / x - 3]"
                                        toExpr = "8 x (x + 3)"
                                        explanation {
                                            key = GeneralExplanation.CancelDenominator
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#3"
                                startExpr = "(-[2 / x + 3]) (x - 3) (x + 3)"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "(-[2 / x + 3]) (x - 3) (x + 3)"
                                    toExpr = "-2 (x - 3)"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "(-[2 / x + 3]) (x - 3) (x + 3)"
                                        toExpr = "-[2 (x - 3) (x + 3) / x + 3]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "-[2 (x - 3) (x + 3) / x + 3]"
                                        toExpr = "-2 (x - 3)"
                                        explanation {
                                            key = FractionArithmeticExplanation.SimplifyFraction
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#4"
                                startExpr = "12 = 8 x (x + 3) - 2 (x - 3)"
                                explanation {
                                    key = EquationsExplanation.SimplifiedPolynomialEquation
                                }
                            }
                        }
                    }
                }
            }

            // not necessary to test solving an equation in one variable
            task { }

            task {
                taskId = "#4"
                startExpr = "SetSolution[x : {[1 / 4]}]"
                explanation {
                    key = EquationsExplanation.SomeSolutionsDoNotSatisfyConstraint
                }
            }
        }
    }

    // the below test fails, because `solveConstantEquationSteps` fails for verifying
    // when whether `x = -3` is a valid solution or not (as it results in "Undefined")
    // PLUT-652 might be needed here?
    @Test
    fun `test rational equation with one valid solution by US method`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[12 / [x^2] - 9] = [8x / x - 3] - [2 /x + 3]"
        context = Context(curriculum = Curriculum.US)
    }

    @Test
    fun `test solve equation with rational expression on both the sides`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[x + 2 / x - 3] = [x / 3x - 2]"

        check {
            fromExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
            toExpr = "SetSolution[x : {-4, [1 / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            // not necessary to test domain
            task { }

            task {
                taskId = "#2"
                startExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                explanation {
                    key = EquationsExplanation.SimplifyToPolynomialEquation
                }

                step {
                    fromExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                    toExpr = "(x + 2) (3 x - 2) = x (x - 3)"
                    explanation {
                        key = EquationsExplanation.SimplifyToPolynomialEquation
                    }

                    task {
                        taskId = "#1"
                        startExpr = "(x - 3) (3 x - 2)"
                        explanation {
                            key = EquationsExplanation.ComputeLeastCommonDenominatorOfFractions
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                        }

                        step {
                            fromExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                            toExpr = "(x + 2) (3 x - 2) = x (x - 3)"
                            explanation {
                                key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                            }

                            task {
                                taskId = "#1"
                                startExpr = "[x + 2 / x - 3] (x - 3) (3 x - 2)"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "[x + 2 / x - 3] (x - 3) (3 x - 2)"
                                    toExpr = "(x + 2) (3 x - 2)"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "[x + 2 / x - 3] (x - 3) (3 x - 2)"
                                        toExpr = "[(x + 2) (x - 3) (3 x - 2) / x - 3]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "[(x + 2) (x - 3) (3 x - 2) / x - 3]"
                                        toExpr = "(x + 2) (3 x - 2)"
                                        explanation {
                                            key = GeneralExplanation.CancelDenominator
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#2"
                                startExpr = "[x / 3 x - 2] (x - 3) (3 x - 2)"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "[x / 3 x - 2] (x - 3) (3 x - 2)"
                                    toExpr = "x (x - 3)"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "[x / 3 x - 2] (x - 3) (3 x - 2)"
                                        toExpr = "[x (x - 3) (3 x - 2) / 3 x - 2]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "[x (x - 3) (3 x - 2) / 3 x - 2]"
                                        toExpr = "x (x - 3)"
                                        explanation {
                                            key = GeneralExplanation.CancelDenominator
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#3"
                                startExpr = "(x + 2) (3 x - 2) = x (x - 3)"
                                explanation {
                                    key = EquationsExplanation.SimplifiedPolynomialEquation
                                }
                            }
                        }
                    }
                }
            }

            // not necessary to test solving an equation in one variable
            task { }

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
    fun `test solve equation with rational expression on both the sides US method`() = testMethod {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[x + 2 / x - 3] = [x / 3x - 2]"
        context = Context(curriculum = Curriculum.US, solutionVariables = listOf("x"))

        check {
            fromExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
            toExpr = "SetSolution[x : {-4, [1 / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            task {
                taskId = "#1"
                startExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                explanation {
                    key = EquationsExplanation.SimplifyToPolynomialEquation
                }

                step {
                    fromExpr = "[x + 2 / x - 3] = [x / 3 x - 2]"
                    toExpr = "(x + 2) (3 x - 2) = x (x - 3)"
                    explanation {
                        key = EquationsExplanation.SimplifyToPolynomialEquation
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "(x + 2) (3 x - 2) = x (x - 3)"
                explanation {
                    key = EquationsExplanation.SolveEquationInOneVariable
                }

                step {
                    fromExpr = "(x + 2) (3 x - 2) = x (x - 3)"
                    toExpr = "SetSolution[x : {-4, [1 / 2]}]"
                    explanation {
                        key = EquationsExplanation.SolveEquationByFactoring
                    }
                }
            }

            task {
                taskId = "#3"
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
                taskId = "#4"
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
                taskId = "#5"
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
        inputExpr = "[[x^2] + 5x + 6 / x + 3] = [x + 2 / [x^2] - 3x - 10]"

        check {
            fromExpr = "[[x ^ 2] + 5 x + 6 / x + 3] = [x + 2 / [x ^ 2] - 3 x - 10]"
            toExpr = "SetSolution[x : {[3 - sqrt[53] / 2], [3 + sqrt[53] / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
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
                    key = EquationsExplanation.SimplifyToPolynomialEquation
                }

                step {
                    fromExpr = "[[x ^ 2] + 5 x + 6 / x + 3] = [x + 2 / [x ^ 2] - 3 x - 10]"
                    toExpr = "x (x - 5) + 2 (x - 5) = 1"
                    explanation {
                        key = EquationsExplanation.SimplifyToPolynomialEquation
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "x (x - 5) + 2 (x - 5) = 1"
                explanation {
                    key = EquationsExplanation.SolveEquationInOneVariable
                }

                step {
                    fromExpr = "x (x - 5) + 2 (x - 5) = 1"
                    toExpr = "SetSolution[x : {[3 - sqrt[53] / 2], [3 + sqrt[53] / 2]}]"
                    explanation {
                        key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                    }
                }
            }

            task {
                taskId = "#4"
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
        inputExpr = "[12x / x - 9] + [1/3] = 8"

        check {
            fromExpr = "[12 x / x - 9] + [1 / 3] = 8"
            toExpr = "SetSolution[x : {-[207 / 13]}]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            // domain computation task, not necessary to test
            task { }

            task {
                taskId = "#2"
                startExpr = "[12 x / x - 9] + [1 / 3] = 8"
                explanation {
                    key = EquationsExplanation.SimplifyToPolynomialEquation
                }

                step {
                    fromExpr = "[12 x / x - 9] + [1 / 3] = 8"
                    toExpr = "36 x + x - 9 = 24 (x - 9)"
                    explanation {
                        key = EquationsExplanation.SimplifyToPolynomialEquation
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "36 x + x - 9 = 24 (x - 9)"
                explanation {
                    key = EquationsExplanation.SolveEquationInOneVariable
                }

                step {
                    fromExpr = "36 x + x - 9 = 24 (x - 9)"
                    toExpr = "SetSolution[x : {-[207 / 13]}]"
                    explanation {
                        key = EquationsExplanation.SolveLinearEquation
                    }
                }
            }

            task {
                taskId = "#4"
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
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            // no need to test computation of domain
            task { }

            task {
                taskId = "#2"
                startExpr = "[12 x / [x ^ 2] - 9] - [1 / [x ^ 2] - 9] = 8"
                explanation {
                    key = EquationsExplanation.SimplifyToPolynomialEquation
                }

                step {
                    fromExpr = "[12 x / [x ^ 2] - 9] - [1 / [x ^ 2] - 9] = 8"
                    toExpr = "12 x - 1 = 8 ([x ^ 2] - 9)"
                    explanation {
                        key = EquationsExplanation.SimplifyToPolynomialEquation
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[12 x / [x ^ 2] - 9] - [1 / [x ^ 2] - 9] = 8"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                        }

                        step {
                            fromExpr = "[12 x / [x ^ 2] - 9] - [1 / [x ^ 2] - 9] = 8"
                            toExpr = "12 x - 1 = 8 ([x ^ 2] - 9)"
                            explanation {
                                key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                            }

                            task {
                                taskId = "#1"
                                startExpr = "[12 x / [x ^ 2] - 9] ([x ^ 2] - 9)"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "[12 x / [x ^ 2] - 9] ([x ^ 2] - 9)"
                                    toExpr = "12 x"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "[12 x / [x ^ 2] - 9] ([x ^ 2] - 9)"
                                        toExpr = "[12 x ([x ^ 2] - 9) / [x ^ 2] - 9]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "[12 x ([x ^ 2] - 9) / [x ^ 2] - 9]"
                                        toExpr = "12 x"
                                        explanation {
                                            key = GeneralExplanation.CancelDenominator
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#2"
                                startExpr = "(-[1 / [x ^ 2] - 9]) ([x ^ 2] - 9)"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "(-[1 / [x ^ 2] - 9]) ([x ^ 2] - 9)"
                                    toExpr = "-1"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "(-[1 / [x ^ 2] - 9]) ([x ^ 2] - 9)"
                                        toExpr = "-[1 ([x ^ 2] - 9) / [x ^ 2] - 9]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "-[1 ([x ^ 2] - 9) / [x ^ 2] - 9]"
                                        toExpr = "-1"
                                        explanation {
                                            key = FractionArithmeticExplanation.SimplifyFraction
                                        }
                                        step {
                                            fromExpr = "[1 ([x ^ 2] - 9) / [x ^ 2] - 9]"
                                            toExpr = "1"
                                            explanation {
                                                key = GeneralExplanation.CancelDenominator
                                            }
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#3"
                                startExpr = "12 x - 1 = 8 ([x ^ 2] - 9)"
                                explanation {
                                    key = EquationsExplanation.SimplifiedPolynomialEquation
                                }
                            }
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "12 x - 1 = 8 ([x ^ 2] - 9)"
                explanation {
                    key = EquationsExplanation.SolveEquationInOneVariable
                }

                step {
                    fromExpr = "12 x - 1 = 8 ([x ^ 2] - 9)"
                    toExpr = "SetSolution[x : {[12 - 4 sqrt[151] / 16], [12 + 4 sqrt[151] / 16]}]"
                    explanation {
                        key = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula
                    }
                }
            }

            task {
                taskId = "#4"
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
            // result to "3 + sqrt[2]"
            toExpr = "SetSolution[x : {[2 sqrt[2] + sqrt[152 + 48 sqrt[2]] / 4]}]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            // no need to test the domain computation task
            task { }

            task {
                taskId = "#2"
                startExpr = "[12 / [x ^ 2] - 9] = sqrt[2] - [2 / x + 3]"
                explanation {
                    key = EquationsExplanation.SimplifyToPolynomialEquation
                }

                step {
                    fromExpr = "[12 / [x ^ 2] - 9] = sqrt[2] - [2 / x + 3]"
                    toExpr = "12 = sqrt[2] (x - 3) (x + 3) - 2 (x - 3)"
                    explanation {
                        key = EquationsExplanation.SimplifyToPolynomialEquation
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[12 / [x ^ 2] - 9] = sqrt[2] - [2 / x + 3]"
                        explanation {
                            key = EquationsExplanation.FactorDenominatorOfFraction
                        }

                        step {
                            fromExpr = "[12 / [x ^ 2] - 9] = sqrt[2] - [2 / x + 3]"
                            toExpr = "[12 / (x - 3) (x + 3)] = sqrt[2] - [2 / x + 3]"
                            explanation {
                                key = RationalExpressionsExplanation.FactorDenominatorOfFraction
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "(x - 3) (x + 3)"
                        explanation {
                            key = EquationsExplanation.ComputeLeastCommonDenominatorOfFractions
                        }
                    }

                    task {
                        taskId = "#3"
                        startExpr = "[12 / (x - 3) (x + 3)] = sqrt[2] - [2 / x + 3]"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                        }

                        step {
                            fromExpr = "[12 / (x - 3) (x + 3)] = sqrt[2] - [2 / x + 3]"
                            toExpr = "12 = sqrt[2] (x - 3) (x + 3) - 2 (x - 3)"
                            explanation {
                                key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                            }

                            task {
                                taskId = "#1"
                                startExpr = "[12 / (x - 3) (x + 3)] (x - 3) (x + 3)"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "[12 / (x - 3) (x + 3)] (x - 3) (x + 3)"
                                    toExpr = "12"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "[12 / (x - 3) (x + 3)] (x - 3) (x + 3)"
                                        toExpr = "[12 (x - 3) (x + 3) / (x - 3) (x + 3)]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "[12 (x - 3) (x + 3) / (x - 3) (x + 3)]"
                                        toExpr = "12"
                                        explanation {
                                            key = FractionArithmeticExplanation.SimplifyFraction
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#2"
                                startExpr = "(-[2 / x + 3]) (x - 3) (x + 3)"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "(-[2 / x + 3]) (x - 3) (x + 3)"
                                    toExpr = "-2 (x - 3)"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "(-[2 / x + 3]) (x - 3) (x + 3)"
                                        toExpr = "-[2 (x - 3) (x + 3) / x + 3]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "-[2 (x - 3) (x + 3) / x + 3]"
                                        toExpr = "-2 (x - 3)"
                                        explanation {
                                            key = FractionArithmeticExplanation.SimplifyFraction
                                        }
                                        step {
                                            fromExpr = "[2 (x - 3) (x + 3) / x + 3]"
                                            toExpr = "2 (x - 3)"
                                            explanation {
                                                key = GeneralExplanation.CancelDenominator
                                            }
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#3"
                                startExpr = "12 = sqrt[2] (x - 3) (x + 3) - 2 (x - 3)"
                                explanation {
                                    key = EquationsExplanation.SimplifiedPolynomialEquation
                                }
                            }
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "12 = sqrt[2] (x - 3) (x + 3) - 2 (x - 3)"
                explanation {
                    key = EquationsExplanation.SolveEquationInOneVariable
                }
            }

            task {
                taskId = "#4"
                startExpr = "SetSolution[x : {[2 sqrt[2] + sqrt[152 + 48 sqrt[2]] / 4]}]"
                explanation {
                    key = EquationsExplanation.SomeSolutionsDoNotSatisfyConstraint
                }
            }
        }
    }

    @Test
    fun `test simplify power of lhs of rational equation`() = testMethodInX {
        method = EquationsPlans.SolveRationalEquation
        inputExpr = "[([x / x + 1]) ^ 2 ] = 1"

        check {
            fromExpr = "[([x / x + 1]) ^ 2] = 1"
            toExpr = "SetSolution[x : {-[1 / 2]}]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            // domain computation task, not necessary to test it
            task { }

            task {
                taskId = "#2"
                startExpr = "[([x / x + 1]) ^ 2] = 1"
                explanation {
                    key = EquationsExplanation.SimplifyToPolynomialEquation
                }

                step {
                    fromExpr = "[([x / x + 1]) ^ 2] = 1"
                    toExpr = "[x ^ 2] = [(x + 1) ^ 2]"
                    explanation {
                        key = EquationsExplanation.SimplifyToPolynomialEquation
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[([x / x + 1]) ^ 2] = 1"
                        explanation {
                            key = EquationsExplanation.SimplifyEquation
                        }

                        step {
                            fromExpr = "[([x / x + 1]) ^ 2] = 1"
                            toExpr = "[[x ^ 2] / [(x + 1) ^ 2]] = 1"
                            explanation {
                                key = RationalExpressionsExplanation.SimplifyPowerOfRationalExpression
                            }
                            step {
                                fromExpr = "[([x / x + 1]) ^ 2]"
                                toExpr = "[[x ^ 2] / [(x + 1) ^ 2]]"
                                explanation {
                                    key = FractionArithmeticExplanation.DistributeFractionPositivePower
                                }
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[[x ^ 2] / [(x + 1) ^ 2]] = 1"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                        }

                        step {
                            fromExpr = "[[x ^ 2] / [(x + 1) ^ 2]] = 1"
                            toExpr = "[x ^ 2] = [(x + 1) ^ 2]"
                            explanation {
                                key = EquationsExplanation.MultiplyBothSidesAndSimplifyEquation
                            }

                            task {
                                taskId = "#1"
                                startExpr = "[[x ^ 2] / [(x + 1) ^ 2]] * [(x + 1) ^ 2]"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "[[x ^ 2] / [(x + 1) ^ 2]] * [(x + 1) ^ 2]"
                                    toExpr = "[x ^ 2]"
                                    explanation {
                                        key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                                    }
                                    step {
                                        fromExpr = "[[x ^ 2] / [(x + 1) ^ 2]] * [(x + 1) ^ 2]"
                                        toExpr = "[[x ^ 2] * [(x + 1) ^ 2] / [(x + 1) ^ 2]]"
                                        explanation {
                                            key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                        }
                                    }
                                    step {
                                        fromExpr = "[[x ^ 2] * [(x + 1) ^ 2] / [(x + 1) ^ 2]]"
                                        toExpr = "[x ^ 2]"
                                        explanation {
                                            key = GeneralExplanation.CancelDenominator
                                        }
                                    }
                                }
                            }

                            task {
                                taskId = "#2"
                                startExpr = "1 * [(x + 1) ^ 2]"
                                explanation {
                                    key = EquationsExplanation.SimplifyRationalExpression
                                }

                                step {
                                    fromExpr = "1 * [(x + 1) ^ 2]"
                                    toExpr = "[(x + 1) ^ 2]"
                                    explanation {
                                        key = GeneralExplanation.RemoveUnitaryCoefficient
                                    }
                                }
                            }

                            task {
                                taskId = "#3"
                                startExpr = "[x ^ 2] = [(x + 1) ^ 2]"
                                explanation {
                                    key = EquationsExplanation.SimplifiedPolynomialEquation
                                }
                            }
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "[x ^ 2] = [(x + 1) ^ 2]"
                explanation {
                    key = EquationsExplanation.SolveEquationInOneVariable
                }
            }

            task {
                taskId = "#4"
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
        inputExpr = "[2x / x] = [4 / x]"

        check {
            fromExpr = "[2 x / x] = [4 / x]"
            toExpr = "SetSolution[x : {2}]"
            explanation {
                key = EquationsExplanation.SolveEquationInOneVariable
            }

            // not necessary to test the domain computation task
            task { }

            task {
                taskId = "#2"
                startExpr = "[2 x / x] = [4 / x]"
                explanation {
                    key = EquationsExplanation.SimplifyToPolynomialEquation
                }

                step {
                    fromExpr = "[2 x / x] = [4 / x]"
                    toExpr = "2 x = 4"
                    explanation {
                        key = EquationsExplanation.SimplifyToPolynomialEquation
                    }

                    task {
                        taskId = "#1"
                        startExpr = "[2 x / x] = [4 / x]"
                        explanation {
                            key = EquationsExplanation.MultiplyBothSidesByDenominator
                        }

                        step {
                            fromExpr = "[2 x / x] = [4 / x]"
                            toExpr = "[2 x / x] x = [4 / x] x"
                            explanation {
                                key = EquationsExplanation.MultiplyBothSidesByDenominator
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[2 x / x] x = [4 / x] x"
                        explanation {
                            key = EquationsExplanation.SimplifyRationalExpression
                        }

                        step {
                            fromExpr = "[2 x / x] x = [4 / x] x"
                            toExpr = "2 x = [4 / x] x"
                            explanation {
                                key = FractionArithmeticExplanation.SimplifyFraction
                            }
                            step {
                                fromExpr = "[2 x / x]"
                                toExpr = "2"
                                explanation {
                                    key = GeneralExplanation.CancelDenominator
                                }
                            }
                        }

                        step {
                            fromExpr = "2 x = [4 / x] x"
                            toExpr = "2 x = 4"
                            explanation {
                                key = EquationsExplanation.MultiplyAndSimplifyRationalFractions
                            }
                            step {
                                fromExpr = "[4 / x] x"
                                toExpr = "[4 x / x]"
                                explanation {
                                    key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                                }
                            }
                            step {
                                fromExpr = "[4 x / x]"
                                toExpr = "4"
                                explanation {
                                    key = GeneralExplanation.CancelDenominator
                                }
                            }
                        }
                    }
                }
            }

            // not necessary to test solution of equation here
            task { }

            task {
                taskId = "#4"
                startExpr = "SetSolution[x : {2}]"
                explanation {
                    key = EquationsExplanation.AllSolutionsSatisfyConstraint
                }
            }
        }
    }
}
