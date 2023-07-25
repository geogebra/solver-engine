package methods.equations

import engine.methods.testMethodInX
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class UndefinedInEquationTest {
    @Test
    fun `test RHS is undefined`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "x = [1 / 2 - 2] + sqrt[3]"

        check {
            fromExpr = "x = [1 / 2 - 2] + sqrt[3]"
            toExpr = "Contradiction[x : x = /undefined/]"
            explanation {
                key = EquationsExplanation.SolveEquationWithUndefinedInEquation
            }
            step {
                fromExpr = "x = [1 / 2 - 2] + sqrt[3]"
                toExpr = "x = /undefined/"
                explanation {
                    key = EquationsExplanation.SimplifyLhsAndRhsSeparately
                }

                task {
                    taskId = "#1"
                    startExpr = "[1 / 2 - 2] + sqrt[3]"
                    explanation {
                        key = EquationsExplanation.SimplifyRhs
                    }

                    step {
                        fromExpr = "[1 / 2 - 2] + sqrt[3]"
                        toExpr = "[1 / 0] + sqrt[3]"
                        explanation {
                            key = GeneralExplanation.CancelAdditiveInverseElements
                        }
                    }

                    step {
                        fromExpr = "[1 / 0] + sqrt[3]"
                        toExpr = "/undefined/"
                        explanation {
                            key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "x = /undefined/"
                    explanation {
                        key = EquationsExplanation.SubstitutingSimplifiedLhsAndRhsIntoOriginalEquation
                    }
                }
            }
            step {
                fromExpr = "x = /undefined/"
                toExpr = "Contradiction[x : x = /undefined/]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromUndefinedInEquation
                }
            }
        }
    }

    @Test
    fun `test both the sides are undefined`() = testMethodInX {
        method = EquationsPlans.SolveEquationInOneVariable
        inputExpr = "[x - 1 / sqrt[3] - sqrt[3]] = [1 / 2 - 2] + sqrt[3]"

        check {
            fromExpr = "[x - 1 / sqrt[3] - sqrt[3]] = [1 / 2 - 2] + sqrt[3]"
            toExpr = "Contradiction[x : /undefined/ = /undefined/]"
            explanation {
                key = EquationsExplanation.SolveEquationWithUndefinedInEquation
            }
            step {
                fromExpr = "[x - 1 / sqrt[3] - sqrt[3]] = [1 / 2 - 2] + sqrt[3]"
                toExpr = "/undefined/ = /undefined/"
                explanation {
                    key = EquationsExplanation.SimplifyLhsAndRhsSeparately
                }

                task {
                    taskId = "#1"
                    startExpr = "[x - 1 / sqrt[3] - sqrt[3]]"
                    explanation {
                        key = EquationsExplanation.SimplifyLhs
                    }

                    step {
                        fromExpr = "[x - 1 / sqrt[3] - sqrt[3]]"
                        toExpr = "[x - 1 / 0]"
                        explanation {
                            key = GeneralExplanation.CancelAdditiveInverseElements
                        }
                    }

                    step {
                        fromExpr = "[x - 1 / 0]"
                        toExpr = "/undefined/"
                        explanation {
                            key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "[1 / 2 - 2] + sqrt[3]"
                    explanation {
                        key = EquationsExplanation.SimplifyRhs
                    }

                    step {
                        fromExpr = "[1 / 2 - 2] + sqrt[3]"
                        toExpr = "[1 / 0] + sqrt[3]"
                        explanation {
                            key = GeneralExplanation.CancelAdditiveInverseElements
                        }
                    }

                    step {
                        fromExpr = "[1 / 0] + sqrt[3]"
                        toExpr = "/undefined/"
                        explanation {
                            key = GeneralExplanation.SimplifyZeroDenominatorFractionToUndefined
                        }
                    }
                }

                task {
                    taskId = "#3"
                    startExpr = "/undefined/ = /undefined/"
                    explanation {
                        key = EquationsExplanation.SubstitutingSimplifiedLhsAndRhsIntoOriginalEquation
                    }
                }
            }
            step {
                fromExpr = "/undefined/ = /undefined/"
                toExpr = "Contradiction[x : /undefined/ = /undefined/]"
                explanation {
                    key = EquationsExplanation.ExtractSolutionFromUndefinedInEquation
                }
            }
        }
    }
}
