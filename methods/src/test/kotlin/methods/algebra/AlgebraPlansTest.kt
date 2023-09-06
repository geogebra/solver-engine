package methods.algebra

import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
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
                toExpr = "[[(x + 1) ^ 2] / [x ^ 2] - x + 1] : [x + 1 / [x ^ 2] - x + 1]"
                explanation {
                    key = RationalExpressionsExplanation.SimplifyRationalExpression
                }
            }

            step {
                fromExpr = "[[(x + 1) ^ 2] / [x ^ 2] - x + 1] : [x + 1 / [x ^ 2] - x + 1]"
                toExpr = "[[(x + 1) ^ 2] / [x ^ 2] - x + 1] * [[x ^ 2] - x + 1 / x + 1]"
                explanation {
                    key = FractionArithmeticExplanation.RewriteDivisionAsMultiplicationByReciprocal
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
    fun `test computing the domain of an expression with a rational subexpression`() = testMethod {
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
                    key = AlgebraExplanation.ExpressionMustNotBeZero
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
    fun `test simplifying algebraic expression containing additions and divisions`() = testMethod {
        method = AlgebraPlans.SimplifyAlgebraicExpression
        inputExpr = "([4 / x] - [7 / x - 3]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"

        check {
            fromExpr = "([4 / x] - [7 / x - 3]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
            toExpr = "-[9 / [(x - 3) ^ 2]]"
            explanation {
                key = AlgebraExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "([4 / x] - [7 / x - 3]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "[-3 x - 12 / x (x - 3)] : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = RationalExpressionsExplanation.AddRationalExpressions
                }
            }

            step {
                fromExpr = "[-3 x - 12 / x (x - 3)] : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "[-(3 x + 12) / x (x - 3)] : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = GeneralExplanation.FactorMinusFromSum
                }
            }

            step {
                fromExpr = "[-(3 x + 12) / x (x - 3)] : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "(-[3 x + 12 / x (x - 3)]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyNegativeInNumerator
                }
            }

            step {
                fromExpr = "(-[3 x + 12 / x (x - 3)]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "(-[3 x + 12 / x (x - 3)]) : ([x + 1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = RationalExpressionsExplanation.AddTermAndRationalExpression
                }
            }

            step {
                fromExpr = "(-[3 x + 12 / x (x - 3)]) : ([x + 1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "(-[3 x + 12 / x (x - 3)]) : [[x ^ 2] + x - 12 / [x ^ 2]] + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = RationalExpressionsExplanation.AddRationalExpressions
                }
            }

            step {
                fromExpr = "(-[3 x + 12 / x (x - 3)]) : [[x ^ 2] + x - 12 / [x ^ 2]] + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "(-[3 x + 12 / x (x - 3)]) * [[x ^ 2] / [x ^ 2] + x - 12] + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = FractionArithmeticExplanation.RewriteDivisionsAsFractionInExpression
                }
            }

            step {
                fromExpr = "(-[3 x + 12 / x (x - 3)]) * [[x ^ 2] / [x ^ 2] + x - 12] + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "-[3 x + 12 / x (x - 3)] * [[x ^ 2] / [x ^ 2] + x - 12] + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = GeneralExplanation.NormalizeNegativeSignsInProduct
                }
            }

            step {
                fromExpr = "-[3 x + 12 / x (x - 3)] * [[x ^ 2] / [x ^ 2] + x - 12] + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "-[3 x / [(x - 3) ^ 2]] + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = RationalExpressionsExplanation.MultiplyRationalExpressions
                }
            }

            step {
                fromExpr = "-[3 x / [(x - 3) ^ 2]] + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "[-3 [x ^ 2] + 9 x - 27 / x * [(x - 3) ^ 2]] + [3 / x]"
                explanation {
                    key = RationalExpressionsExplanation.AddRationalExpressions
                }
            }

            step {
                fromExpr = "[-3 [x ^ 2] + 9 x - 27 / x * [(x - 3) ^ 2]] + [3 / x]"
                toExpr = "-[9 / [(x - 3) ^ 2]]"
                explanation {
                    key = RationalExpressionsExplanation.AddRationalExpressions
                }
            }
        }
    }

    @Test
    fun `test computing the domain of an expression with a non-constant divisor`() = testMethod {
        method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
        inputExpr = "[x ^ 2] : ([x ^ 2] - x + 1)"

        check {
            fromExpr = "[x ^ 2] : ([x ^ 2] - x + 1)"
            toExpr = "Identity[x : x = [1 +/- sqrt[-3] / 2]]"
            explanation {
                key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
            }

            task {
                taskId = "#1"
                startExpr = "[x ^ 2] - x + 1 != 0"
                explanation {
                    key = AlgebraExplanation.ExpressionMustNotBeZero
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
                startExpr = "Identity[x : x = [1 +/- sqrt[-3] / 2]]"
                explanation {
                    key = AlgebraExplanation.ExpressionIsDefinedEverywhere
                }
            }
        }
    }

    @Test
    fun `test computing the domain of a multivariate expression`() = testMethod {
        method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
        inputExpr = "a b c : c f + c : (2 f + 1) + f : (3 f + 1)"

        check {
            fromExpr = "a b c : c f + c : (2 f + 1) + f : (3 f + 1)"
            toExpr = "c f != 0 AND SetSolution[f : /reals/ \\ {-[1 / 2], -[1 / 3]}]"
            explanation {
                key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
            }

            task {
                taskId = "#1"
                startExpr = "c f != 0"
                explanation {
                    key = AlgebraExplanation.ExpressionMustNotBeZero
                }
            }

            task {
                taskId = "#2"
                startExpr = "2 f + 1 != 0"
                explanation {
                    key = AlgebraExplanation.ExpressionMustNotBeZero
                }

                step {
                    fromExpr = "2 f + 1 != 0"
                    toExpr = "SetSolution[f : /reals/ \\ {-[1 / 2]}]"
                    explanation {
                        key = InequationsExplanation.SolveInequationInOneVariable
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "3 f + 1 != 0"
                explanation {
                    key = AlgebraExplanation.ExpressionMustNotBeZero
                }

                step {
                    fromExpr = "3 f + 1 != 0"
                    toExpr = "SetSolution[f : /reals/ \\ {-[1 / 3]}]"
                    explanation {
                        key = InequationsExplanation.SolveInequationInOneVariable
                    }
                }
            }

            task {
                taskId = "#4"
                startExpr = "c f != 0 AND SetSolution[f : /reals/ \\ {-[1 / 2], -[1 / 3]}]"
                explanation {
                    key = AlgebraExplanation.CollectDomainRestrictions
                }
            }
        }
    }

    @Test
    fun `test computing the domain for equal denominators is done in the same task`() = testMethod {
        method = AlgebraPlans.ComputeDomainOfAlgebraicExpression
        inputExpr = "[6 / x + 1] + [6 x / x + 1] + [3 / 2 x + 3]"

        check {
            fromExpr = "[6 / x + 1] + [6 x / x + 1] + [3 / 2 x + 3]"
            toExpr = "SetSolution[x: /reals/ \\ {-[3 / 2], -1}]"
            explanation {
                key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
            }

            task {
                taskId = "#1"
                startExpr = "x + 1 != 0"
                explanation {
                    key = AlgebraExplanation.ExpressionMustNotBeZeroPlural
                    param { expr = "x + 1" }
                    param { expr = "[6 / x + 1], [6 x / x + 1]" }
                }

                step {
                    fromExpr = "x + 1 != 0"
                    toExpr = "SetSolution[x: /reals/ \\ {-1}]"
                    explanation {
                        key = InequationsExplanation.SolveInequationInOneVariable
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "2 x + 3 != 0"
                explanation {
                    key = AlgebraExplanation.ExpressionMustNotBeZero
                }

                step {
                    fromExpr = "2 x + 3 != 0"
                    toExpr = "SetSolution[x: /reals/ \\ {-[3 / 2]}]"
                    explanation {
                        key = InequationsExplanation.SolveInequationInOneVariable
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "SetSolution[x: /reals/ \\ {-[3 / 2], -1}]"
                explanation {
                    key = AlgebraExplanation.CollectDomainRestrictions
                }
            }
        }
    }

    @Test
    fun `test simplifying an algebraic expression after computing its domain`() = testMethod {
        method = AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression
        inputExpr = "[x / x + 1] + [x / x + 2]"

        check {
            fromExpr = "[x / x + 1] + [x / x + 2]"
            toExpr = "[2 [x ^ 2] + 3 x / (x + 1) (x + 2)] GIVEN SetSolution[x : /reals/ \\ {-2, -1}]"
            explanation {
                key = AlgebraExplanation.ComputeDomainAndSimplifyAlgebraicExpression
            }

            task {
                taskId = "#1"
                startExpr = "[x / x + 1] + [x / x + 2]"
                explanation {
                    key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                }

                step {
                    fromExpr = "[x / x + 1] + [x / x + 2]"
                    toExpr = "SetSolution[x : /reals/ \\ {-2, -1}]"
                    explanation {
                        key = AlgebraExplanation.ComputeDomainOfAlgebraicExpression
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "[x / x + 1] + [x / x + 2]"
                explanation {
                    key = AlgebraExplanation.SimplifyAlgebraicExpression
                }

                step {
                    fromExpr = "[x / x + 1] + [x / x + 2]"
                    toExpr = "[2 [x ^ 2] + 3 x / (x + 1) (x + 2)]"
                    explanation {
                        key = RationalExpressionsExplanation.AddRationalExpressions
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "[2 [x ^ 2] + 3 x / (x + 1) (x + 2)] GIVEN SetSolution[x : /reals/ \\ {-2, -1}]"
                explanation {
                    key = AlgebraExplanation.CombineSimplifiedExpressionWithConstraint
                }
            }
        }
    }
}
