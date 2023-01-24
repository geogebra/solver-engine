package methods.equations

import engine.context.Context
import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class SolveFactorisedQuadraticPlanTest {

    @Test
    fun `solve experimental task-based plan`() = testMethod {
        context = Context(solutionVariable = "x")
        method = EquationsPlans.SolveFactorisedQuadraticEquation
        inputExpr = "(3 x - 1) (x + 4) = 0"

        check {
            fromExpr = "(3 x - 1) (x + 4) = 0"
            toExpr = "Solution[x, {[1 / 3], -4}]"
            explanation {
                key = ExperimentalExplanation.SolveFactorisedQuadratic
            }

            task {
                taskId = "#1"
                explanation {
                    key = ExperimentalExplanation.SolveFactorOfQuadratic
                }

                step {
                    fromExpr = "3 x - 1 = 0"
                    toExpr = "Solution[x, {[1 / 3]}]"
                    explanation {
                        key = EquationsExplanation.SolveLinearEquation
                    }

                    step {
                        fromExpr = "3 x - 1 = 0"
                        toExpr = "3 x = 1"
                        explanation {
                            key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
                        }

                        step {
                            fromExpr = "3 x - 1 = 0"
                            toExpr = "3 x - 1 + 1 = 0 + 1"
                            explanation {
                                key = methods.solvable.EquationsExplanation.MoveConstantsToTheRight
                            }
                        }

                        step {
                            fromExpr = "3 x - 1 + 1 = 0 + 1"
                            toExpr = "3 x = 1"
                            explanation {
                                key = PolynomialsExplanation.SimplifyAlgebraicExpression
                            }

                            step {
                                fromExpr = "3 x - 1 + 1 = 0 + 1"
                                toExpr = "3 x = 0 + 1"
                                explanation {
                                    key = GeneralExplanation.CancelAdditiveInverseElements
                                }
                            }

                            step {
                                fromExpr = "3 x = 0 + 1"
                                toExpr = "3 x = 1"
                                explanation {
                                    key = GeneralExplanation.EliminateZeroInSum
                                }
                            }
                        }
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

                            step {
                                fromExpr = "[3 x / 3] = [1 / 3]"
                                toExpr = "1 x = [1 / 3]"
                                explanation {
                                    key = PolynomialsExplanation.NormalizeMonomialAndSimplify
                                }

                                step {
                                    fromExpr = "[3 x / 3]"
                                    toExpr = "[3 / 3] x"
                                    explanation {
                                        key = PolynomialsExplanation.NormalizeMonomial
                                    }
                                }

                                step {
                                    fromExpr = "[3 / 3] x"
                                    toExpr = "1 x"
                                    explanation {
                                        key = FractionArithmeticExplanation.SimplifyFraction
                                    }

                                    step {
                                        fromExpr = "[3 / 3]"
                                        toExpr = "1"
                                        explanation {
                                            key = GeneralExplanation.SimplifyUnitFractionToOne
                                        }
                                    }
                                }
                            }

                            step {
                                fromExpr = "1 x = [1 / 3]"
                                toExpr = "x = [1 / 3]"
                                explanation {
                                    key = PolynomialsExplanation.NormalizeMonomialAndSimplify
                                }

                                step {
                                    fromExpr = "1 x"
                                    toExpr = "x"
                                    explanation {
                                        key = PolynomialsExplanation.NormalizeMonomial
                                    }
                                }
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

            task {
                taskId = "#2"
                explanation {
                    key = ExperimentalExplanation.SolveFactorOfQuadratic
                }

                step {
                    fromExpr = "x + 4 = 0"
                    toExpr = "Solution[x, {-4}]"
                    explanation {
                        key = EquationsExplanation.SolveLinearEquation
                    }

                    step {
                        fromExpr = "x + 4 = 0"
                        toExpr = "x = -4"
                        explanation {
                            key = EquationsExplanation.MoveConstantsToTheRightAndSimplify
                        }

                        step {
                            fromExpr = "x + 4 = 0"
                            toExpr = "x + 4 - 4 = 0 - 4"
                            explanation {
                                key = methods.solvable.EquationsExplanation.MoveConstantsToTheRight
                            }
                        }

                        step {
                            fromExpr = "x + 4 - 4 = 0 - 4"
                            toExpr = "x = -4"
                            explanation {
                                key = PolynomialsExplanation.SimplifyAlgebraicExpression
                            }

                            step {
                                fromExpr = "x + 4 - 4 = 0 - 4"
                                toExpr = "x = 0 - 4"
                                explanation {
                                    key = GeneralExplanation.CancelAdditiveInverseElements
                                }
                            }

                            step {
                                fromExpr = "x = 0 - 4"
                                toExpr = "x = -4"
                                explanation {
                                    key = GeneralExplanation.EliminateZeroInSum
                                }
                            }
                        }
                    }

                    step {
                        fromExpr = "x = -4"
                        toExpr = "Solution[x, {-4}]"
                        explanation {
                            key = EquationsExplanation.ExtractSolutionFromEquationInSolvedForm
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                explanation {
                    key = ExperimentalExplanation.CollectSolutions
                }
            }
        }
    }
}
