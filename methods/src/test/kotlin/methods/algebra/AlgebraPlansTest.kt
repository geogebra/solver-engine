package methods.algebra

import engine.methods.testMethod
import engine.methods.testMethodInX
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.inequations.InequationsExplanation
import methods.rationalexpressions.RationalExpressionsExplanation
import org.junit.jupiter.api.Test

class AlgebraPlansTest {

    @Test
    fun `test dividing rational expressions`() = testMethod {
        method = AlgebraPlans.SimplifyAlgebraicExpression
        inputExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] : [x + 1 / [x ^ 2] - x + 1]"

        check {
            fromExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] : [x + 1 / [x ^ 2] - x + 1]"
            toExpr = "x + 1"
            explanation {
                key = AlgebraExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] : [x + 1 / [x ^ 2] - x + 1]"
                toExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] * [[x ^ 2] - x + 1 / x + 1]"
                explanation {
                    key = FractionArithmeticExplanation.RewriteDivisionAsMultiplicationByReciprocal
                }
            }

            step {
                fromExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] * [[x ^ 2] - x + 1 / x + 1]"
                toExpr = "[[(x + 1) ^ 2] / [x ^ 2] - x + 1] * [[x ^ 2] - x + 1 / x + 1]"
                explanation {
                    key = RationalExpressionsExplanation.SimplifyRationalExpression
                }
            }

            step {
                fromExpr = "[[(x + 1) ^ 2] / [x ^ 2] - x + 1] * [[x ^ 2] - x + 1 / x + 1]"
                toExpr = "x + 1"
                explanation {
                    key = RationalExpressionsExplanation.MultiplyRationalExpressions
                }
            }
        }
    }

    @Test
    fun `test simplifying before multiplying of rational expressions`() = testMethod {
        method = AlgebraPlans.SimplifyAlgebraicExpression
        inputExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x] * [[x ^ 3] / [x ^ 2] - 4]"

        check {
            fromExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x] * [[x ^ 3] / [x ^ 2] - 4]"
            toExpr = "[[x ^ 2] / 4 (x - 2)]"
            explanation {
                key = AlgebraExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x] * [[x ^ 3] / [x ^ 2] - 4]"
                toExpr = "[x + 2 / 4 x] * [[x ^ 3] / [x ^ 2] - 4]"
                explanation {
                    key = RationalExpressionsExplanation.SimplifyRationalExpression
                }
            }

            step {
                fromExpr = "[x + 2 / 4 x] * [[x ^ 3] / [x ^ 2] - 4]"
                toExpr = "[[x ^ 2] / 4 (x - 2)]"
                explanation {
                    key = RationalExpressionsExplanation.MultiplyRationalExpressions
                }
            }
        }
    }

    @Test
    fun `test computing the domain of an expression with a rational subexpression`() = testMethodInX {
        method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
        inputExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x]"

        check {
            fromExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x]"
            toExpr = "SetSolution[x : /reals/ \\ {-3, 0}]"
            explanation {
                key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
            }

            task {
                taskId = "#1"
                startExpr = "4 [x ^ 2] + 12 x != 0"
                explanation {
                    key = AlgebraExplanation.DenominatorMustNotBeZero
                }

                step {
                    fromExpr = "4 [x ^ 2] + 12 x != 0"
                    toExpr = "SetSolution[x : /reals/ \\ {-3, 0}]"
                    explanation {
                        key = InequationsExplanation.SolveInequationInOneVariable
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "SetSolution[x : /reals/ \\ {-3, 0}]"
                explanation {
                    key = AlgebraExplanation.CollectDomainRestrictions
                }
            }
        }
    }

    @Test
    fun `test computing the domain of an expression with a non-constant divisor`() = testMethodInX {
        method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
        inputExpr = "[x ^ 2] : ([x ^ 2] - x + 1)"

        check {
            fromExpr = "[x ^ 2] : ([x ^ 2] - x + 1)"
            toExpr = "SetSolution[x : /reals/]"
            explanation {
                key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
            }

            task {
                taskId = "#1"
                startExpr = "[x ^ 2] - x + 1 != 0"
                explanation {
                    key = AlgebraExplanation.DivisorMustNotBeZero
                }

                step {
                    fromExpr = "[x ^ 2] - x + 1 != 0"
                    toExpr = "Identity[x : x = [1 +/- sqrt[-3] / 2]]"
                    explanation {
                        key = InequationsExplanation.SolveInequationInOneVariable
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "SetSolution[x : /reals/]"
                explanation {
                    key = AlgebraExplanation.CollectDomainRestrictions
                }
            }
        }
    }
}
