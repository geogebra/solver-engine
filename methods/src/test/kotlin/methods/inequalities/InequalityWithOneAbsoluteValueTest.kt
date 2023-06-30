package methods.inequalities

import engine.methods.testMethodInX
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class InequalityWithOneAbsoluteValueTest {
    @Test
    fun `test abs(ax + b) GreaterThan positiveConstant`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "abs[3x - 1] > 2"

        check {
            fromExpr = "abs[3 x - 1] > 2"
            toExpr = "SetSolution[x : SetUnion[( -/infinity/, -[1 / 3] ), ( 1, /infinity/ )]]"
            explanation {
                key = InequalitiesExplanation.SolveInequalityWithVariablesInOneAbsoluteValue
            }
            step {
                fromExpr = "abs[3 x - 1] > 2"
                toExpr = "3 x - 1 < -2 OR 3 x - 1 > 2"
                explanation {
                    key = InequalitiesExplanation.SeparateModulusGreaterThanPositiveConstant
                }
            }
            step {
                fromExpr = "3 x - 1 < -2 OR 3 x - 1 > 2 "
                toExpr = "SetSolution[x : SetUnion[( -/infinity/, -[1 / 3] ), ( 1, /infinity/ )]]"
                explanation {
                    key = InequalitiesExplanation.SolveInequalityUnion
                }

                task {
                    taskId = "#1"
                    startExpr = "3 x - 1 < -2"
                    explanation {
                        key = InequalitiesExplanation.SolveInequalityInInequalityUnion
                    }

                    step {
                        fromExpr = "3 x - 1 < -2"
                        toExpr = "SetSolution[x : ( -/infinity/, -[1 / 3] )]"
                        explanation {
                            key = InequalitiesExplanation.SolveLinearInequality
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "3 x - 1 > 2"
                    explanation {
                        key = InequalitiesExplanation.SolveInequalityInInequalityUnion
                    }

                    step {
                        fromExpr = "3 x - 1 > 2"
                        toExpr = "SetSolution[x : ( 1, /infinity/ )]"
                        explanation {
                            key = InequalitiesExplanation.SolveLinearInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : SetUnion[( -/infinity/, -[1 / 3] ), ( 1, /infinity/ )]]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test abs(ax + b) GreaterThanEqualTo positiveConstant`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "abs[-3x + 1] >= 2"

        check {
            fromExpr = "abs[-3 x + 1] >= 2"
            toExpr = "SetSolution[x : SetUnion[( -/infinity/, -[1 / 3] ], [ 1, /infinity/ )]]"
            explanation {
                key = InequalitiesExplanation.SolveInequalityWithVariablesInOneAbsoluteValue
            }
            step {
                fromExpr = "abs[-3 x + 1] >= 2"
                toExpr = "-3 x + 1 >= 2 OR -3 x + 1 <= -2"
                explanation {
                    key = InequalitiesExplanation.SeparateModulusGreaterThanEqualToPositiveConstant
                }
            }
            step {
                fromExpr = "-3 x + 1 >= 2 OR -3 x + 1 <= -2"
                toExpr = "SetSolution[x : SetUnion[( -/infinity/, -[1 / 3] ], [ 1, /infinity/ )]]"
                explanation {
                    key = InequalitiesExplanation.SolveInequalityUnion
                }

                task {
                    taskId = "#1"
                    startExpr = "-3 x + 1 >= 2"
                    explanation {
                        key = InequalitiesExplanation.SolveInequalityInInequalityUnion
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "-3 x + 1 <= -2"
                    explanation {
                        key = InequalitiesExplanation.SolveInequalityInInequalityUnion
                    }

                    step {
                        fromExpr = "-3 x + 1 <= -2"
                        toExpr = "SetSolution[x : [ 1, /infinity/ )]"
                        explanation {
                            key = InequalitiesExplanation.SolveLinearInequality
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : SetUnion[( -/infinity/, -[1 / 3] ], [ 1, /infinity/ )]]"
                    explanation {
                        key = InequalitiesExplanation.CollectUnionSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test abs(ax + b) GreaterThan 0`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "abs[3x - 1] > 0"

        check {
            fromExpr = "abs[3 x - 1] > 0"
            toExpr = "SetSolution[x : SetUnion[( -/infinity/, [1 / 3] ), ( [1 / 3], /infinity/ )]]"
            explanation {
                key = InequalitiesExplanation.SolveInequalityWithVariablesInOneAbsoluteValue
            }
            step {
                fromExpr = "abs[3 x - 1] > 0"
                toExpr = "3 x - 1 != 0"
                explanation {
                    key = InequalitiesExplanation.ConvertModulusGreaterThanZero
                }
            }
            step {
                fromExpr = "3 x - 1 != 0"
                toExpr = "SetSolution[x : SetUnion[( -/infinity/, [1 / 3] ), ( [1 / 3], /infinity/ )]]"
                explanation {
                    key = InequalitiesExplanation.SolveLinearInequality
                }
                step {
                    fromExpr = "3 x - 1 != 0"
                    toExpr = "3 x != 1"
                    step {
                        fromExpr = "3 x - 1 != 0"
                        toExpr = "3 x - 1 + 1 != 0 + 1"
                    }
                    step {
                        fromExpr = "3 x - 1 + 1 != 0 + 1"
                        toExpr = "3 x != 1"
                        explanation {
                            key = InequalitiesExplanation.SimplifyInequality
                        }
                        step {
                            fromExpr = "3 x - 1 + 1 != 0 + 1"
                            toExpr = "3 x != 0 + 1"
                            explanation {
                                key = GeneralExplanation.CancelAdditiveInverseElements
                            }
                        }
                        step {
                            fromExpr = "3 x != 0 + 1"
                            toExpr = "3 x != 1"
                            explanation {
                                key = GeneralExplanation.EliminateZeroInSum
                            }
                        }
                    }
                }
                step {
                    fromExpr = "3 x != 1"
                    toExpr = "x != [1 / 3]"
                    explanation {
                        key = InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                    }
                    step {
                        fromExpr = "3 x != 1"
                        toExpr = "[3 x / 3] != [1 / 3]"
                        explanation {
                            key = InequalitiesExplanation.DivideByCoefficientOfVariable
                        }
                    }
                    step {
                        fromExpr = "[3 x / 3] != [1 / 3]"
                        toExpr = "x != [1 / 3]"
                        explanation {
                            key = PolynomialsExplanation.SimplifyMonomial
                        }
                        step {
                            fromExpr = "[3 x / 3]"
                            toExpr = "x"
                            explanation {
                                key = GeneralExplanation.CancelDenominator
                            }
                        }
                    }
                }
                step {
                    fromExpr = "x != [1 / 3]"
                    toExpr = "SetSolution[x : SetUnion[( -/infinity/, [1 / 3] ), ( [1 / 3], /infinity/ )]]"
                    explanation {
                        key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                    }
                }
            }
        }
    }

    @Test
    fun `test abs(ax + b) GreaterThanEqualTo 0`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "abs[3x - 1] >= 0"

        check {
            fromExpr = "abs[3 x - 1] >= 0"
            toExpr = "Identity[x : abs[3 x - 1] >= 0]"
            explanation {
                key = InequalitiesExplanation.ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant
            }
        }
    }

    @Test
    fun `test normalize to abs(ax + b) GreaterThan negativeConstant`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "3 + abs[x + 5] > 2 - 4"

        check {
            fromExpr = "3 + abs[x + 5] > 2 - 4"
            toExpr = "Identity[x : abs[x + 5] > -5]"
            explanation {
                key = InequalitiesExplanation.SolveInequalityWithVariablesInOneAbsoluteValue
            }
            step {
                fromExpr = "3 + abs[x + 5] > 2 - 4"
                toExpr = "3 + abs[x + 5] > -2"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
                step {
                    fromExpr = "2 - 4"
                    toExpr = "-2"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                    }
                }
            }
            step {
                fromExpr = "3 + abs[x + 5] > -2"
                toExpr = "abs[x + 5] > -5"
                explanation {
                    key = InequalitiesExplanation.IsolateAbsoluteValue
                }
                step {
                    fromExpr = "3 + abs[x + 5] > -2"
                    toExpr = "3 + abs[x + 5] - 3 > -2 - 3"
                }
                step {
                    fromExpr = "3 + abs[x + 5] - 3 > -2 - 3"
                    toExpr = "abs[x + 5] > -5"
                    explanation {
                        key = InequalitiesExplanation.SimplifyInequality
                    }
                    step {
                        fromExpr = "3 + abs[x + 5] - 3 > -2 - 3"
                        toExpr = "abs[x + 5] > -2 - 3"
                        explanation {
                            key = GeneralExplanation.CancelAdditiveInverseElements
                        }
                    }
                    step {
                        fromExpr = "abs[x + 5] > -2 - 3"
                        toExpr = "abs[x + 5] > -5"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                        }
                        step {
                            fromExpr = "-2 - 3"
                            toExpr = "-5"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                            }
                        }
                    }
                }
            }
            step {
                fromExpr = "abs[x + 5] > -5"
                toExpr = "Identity[x : abs[x + 5] > -5]"
            }
        }
    }

    @Test
    fun `test normalize inequality on rhs to right`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "2 - 4 > 3 + abs[x + 5]"

        check {
            fromExpr = "2 - 4 > 3 + abs[x + 5]"
            toExpr = "Contradiction[x : abs[x + 5] < -5]"
            explanation {
                key = InequalitiesExplanation.SolveInequalityWithVariablesInOneAbsoluteValue
            }
            step {
                fromExpr = "2 - 4 > 3 + abs[x + 5]"
                toExpr = "-2 > 3 + abs[x + 5]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
                step {
                    fromExpr = "2 - 4"
                    toExpr = "-2"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                    }
                }
            }
            step {
                fromExpr = "-2 > 3 + abs[x + 5]"
                toExpr = "-5 > abs[x + 5]"
                explanation {
                    key = InequalitiesExplanation.IsolateAbsoluteValue
                }
            }
            step {
                fromExpr = "-5 > abs[x + 5]"
                toExpr = "abs[x + 5] < -5"
                explanation {
                    key = InequalitiesExplanation.FlipInequality
                }
            }
            step {
                fromExpr = "abs[x + 5] < -5"
                toExpr = "Contradiction[x : abs[x + 5] < -5]"
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromModulusLessThanNonPositiveConstant
                }
            }
        }
    }

    @Test
    fun `test abs(ax + b) LessThan positiveConstant`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "abs[3x - 1] < 2"

        check {
            fromExpr = "abs[3 x - 1] < 2"
            toExpr = "SetSolution[x : ( -[1 / 3], 1 )]"
            explanation {
                key = InequalitiesExplanation.SolveInequalityWithVariablesInOneAbsoluteValue
            }
            step {
                fromExpr = "abs[3 x - 1] < 2"
                toExpr = "-2 < 3 x - 1 < 2"
                explanation {
                    key = InequalitiesExplanation.ConvertModulusLessThanPositiveConstant
                }
            }
            step {
                fromExpr = "-2 < 3 x - 1 < 2"
                toExpr = "SetSolution[x : ( -[1 / 3], 1 )]"
                explanation {
                    key = InequalitiesExplanation.SolveDoubleInequality
                }

                task {
                    taskId = "#1"
                    startExpr = "-2 < 3 x - 1"
                    explanation {
                        key = InequalitiesExplanation.SolveLeftInequalityInDoubleInequality
                    }

                    step {
                        fromExpr = "-2 < 3 x - 1"
                        toExpr = "SetSolution[x : ( -[1 / 3], /infinity/ )]"
                        explanation {
                            key = InequalitiesExplanation.SolveLinearInequality
                        }
                        step {
                            fromExpr = "-2 < 3 x - 1"
                            toExpr = "-1 < 3 x"
                        }
                        step {
                            fromExpr = "-1 < 3 x"
                            toExpr = "3 x > -1"
                            explanation {
                                key = InequalitiesExplanation.FlipInequality
                            }
                        }
                        step {
                            fromExpr = "3 x > -1"
                            toExpr = "x > -[1 / 3]"
                            explanation {
                                key = InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                            }
                        }
                        step {
                            fromExpr = "x > -[1 / 3]"
                            toExpr = "SetSolution[x : ( -[1 / 3], /infinity/ )]"
                            explanation {
                                key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                            }
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "3 x - 1 < 2"
                    explanation {
                        key = InequalitiesExplanation.SolveRightInequalityInDoubleInequality
                    }

                    step {
                        fromExpr = "3 x - 1 < 2"
                        toExpr = "SetSolution[x : ( -/infinity/, 1 )]"
                        explanation {
                            key = InequalitiesExplanation.SolveLinearInequality
                        }
                        step {
                            fromExpr = "3 x - 1 < 2"
                            toExpr = "3 x < 3"
                        }
                        step {
                            fromExpr = "3 x < 3"
                            toExpr = "x < 1"
                            explanation {
                                key = InequalitiesExplanation.DivideByCoefficientOfVariableAndSimplify
                            }
                        }
                        step {
                            fromExpr = "x < 1"
                            toExpr = "SetSolution[x : ( -/infinity/, 1 )]"
                            explanation {
                                key = InequalitiesExplanation.ExtractSolutionFromInequalityInSolvedForm
                            }
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "SetSolution[x : ( -[1 / 3], 1 )]"
                    explanation {
                        key = InequalitiesExplanation.CollectIntersectionSolutions
                    }
                }
            }
        }
    }

    @Test
    fun `test abs(ax + b) LessThan negativeConstant`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "abs[3x - 1] < -5"

        check {
            fromExpr = "abs[3 x - 1] < -5"
            toExpr = "Contradiction[x : abs[3 x - 1] < -5]"
            explanation {
                key = InequalitiesExplanation.ExtractSolutionFromModulusLessThanNonPositiveConstant
            }
        }
    }

    @Test
    fun `test flip and invert absolute inequality with unit coefficient`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "-1 < -abs[x + 1]"

        check {
            fromExpr = "-1 < -abs[x + 1]"
            toExpr = "SetSolution[x : ( -2, 0 )]"
            explanation {
                key = InequalitiesExplanation.SolveInequalityWithVariablesInOneAbsoluteValue
            }
            step {
                fromExpr = "-1 < -abs[x + 1]"
                toExpr = "-abs[x + 1] > -1"
                explanation {
                    key = InequalitiesExplanation.FlipInequality
                }
            }
            step {
                fromExpr = "-abs[x + 1] > -1"
                toExpr = "abs[x + 1] < 1"
                explanation {
                    key = InequalitiesExplanation.NegateBothSidesAndFlipTheSign
                }
            }
            step {
                fromExpr = "abs[x + 1] < 1"
                toExpr = "-1 < x + 1 < 1"
                explanation {
                    key = InequalitiesExplanation.ConvertModulusLessThanPositiveConstant
                }
            }
            step {
                fromExpr = "-1 < x + 1 < 1"
                toExpr = "SetSolution[x : ( -2, 0 )]"
                explanation {
                    key = InequalitiesExplanation.SolveDoubleInequality
                }
            }
        }
    }

    @Test
    fun `test flip and invert absolute inequality with non unit coefficient`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "-3 < -2 abs[x + 1]"

        check {
            fromExpr = "-3 < -2 * abs[x + 1]"
            toExpr = "SetSolution[x : ( -[5 / 2], [1 / 2] )]"
            explanation {
                key = InequalitiesExplanation.SolveInequalityWithVariablesInOneAbsoluteValue
            }
            step {
                fromExpr = "-3 < -2 * abs[x + 1]"
                toExpr = "-2 * abs[x + 1] > -3"
                explanation {
                    key = InequalitiesExplanation.FlipInequality
                }
            }
            step {
                fromExpr = "-2 * abs[x + 1] > -3"
                toExpr = "2 * abs[x + 1] < 3"
                explanation {
                    key = InequalitiesExplanation.NegateBothSidesAndFlipTheSign
                }
            }
            step {
                fromExpr = "2 * abs[x + 1] < 3"
                toExpr = "-3 < 2 (x + 1) < 3"
                explanation {
                    key = InequalitiesExplanation.ConvertModulusLessThanPositiveConstant
                }
            }
            step {
                fromExpr = "-3 < 2 (x + 1) < 3"
                toExpr = "SetSolution[x : ( -[5 / 2], [1 / 2] )]"
                explanation {
                    key = InequalitiesExplanation.SolveDoubleInequality
                }
            }
        }
    }

    @Test
    fun `test flip and invert absolute value inequality`() = testMethodInX {
        method = InequalitiesPlans.SolveInequalityWithVariablesInOneAbsoluteValue
        inputExpr = "-3 > -2*abs[x + 1]"

        check {
            fromExpr = "-3 > -2 * abs[x + 1]"
            toExpr = "SetSolution[x : SetUnion[( -/infinity/, -[5 / 2] ), ( [1 / 2], /infinity/ )]]"
            explanation {
                key = InequalitiesExplanation.SolveInequalityWithVariablesInOneAbsoluteValue
            }
            step {
                fromExpr = "-3 > -2 * abs[x + 1]"
                toExpr = "-2 * abs[x + 1] < -3"
                explanation {
                    key = InequalitiesExplanation.FlipInequality
                }
            }
            step {
                fromExpr = "-2 * abs[x + 1] < -3"
                toExpr = "2 * abs[x + 1] > 3"
                explanation {
                    key = InequalitiesExplanation.NegateBothSidesAndFlipTheSign
                }
            }
            step {
                fromExpr = "2 * abs[x + 1] > 3"
                toExpr = "2 (x + 1) < -3 OR 2 (x + 1) > 3 "
                explanation {
                    key = InequalitiesExplanation.SeparateModulusGreaterThanPositiveConstant
                }
            }
            step {
                fromExpr = "2 (x + 1) < -3 OR 2 (x + 1) > 3 "
                toExpr = "SetSolution[x : SetUnion[( -/infinity/, -[5 / 2] ), ( [1 / 2], /infinity/ )]]"
                explanation {
                    key = InequalitiesExplanation.SolveInequalityUnion
                }
            }
        }
    }
}