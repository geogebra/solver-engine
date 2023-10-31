package methods.simplify

import engine.methods.SolverEngineExplanation
import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.rationalexpressions.RationalExpressionsExplanation
import org.junit.jupiter.api.Test

class AlgebraicSimplificationsTest {

    @Test
    fun `test product of fractions and non-fractional terms`() = testMethod {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "[x + 1 / x + 2] (x + 3) (x + 4) * [x + 2 / x + 3]"

        check {
            fromExpr = "[x + 1 / x + 2] (x + 3) (x + 4) * [x + 2 / x + 3]"
            toExpr = "(x + 1) (x + 4)"
            explanation {
                key = SimplifyExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "[x + 1 / x + 2] (x + 3) (x + 4) * [x + 2 / x + 3]"
                toExpr = "[x + 1 / x + 3] (x + 3) (x + 4)"
                explanation {
                    key = RationalExpressionsExplanation.MultiplyRationalExpressions
                }

                step {
                    fromExpr = "[x + 1 / x + 2] (x + 3) (x + 4) * [x + 2 / x + 3]"
                    toExpr = "<.[x + 1 / x + 2] * [x + 2 / x + 3].> (x + 3) (x + 4)"
                    explanation {
                        key = SolverEngineExplanation.RearrangeProduct
                    }
                }

                step {
                    fromExpr = "<.[x + 1 / x + 2] * [x + 2 / x + 3].> (x + 3) (x + 4)"
                    toExpr = "[(x + 1) (x + 2) / (x + 2) (x + 3)] (x + 3) (x + 4)"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyFractions
                    }
                }

                step {
                    fromExpr = "[(x + 1) (x + 2) / (x + 2) (x + 3)] (x + 3) (x + 4)"
                    toExpr = "[x + 1 / x + 3] (x + 3) (x + 4)"
                    explanation {
                        key = FractionArithmeticExplanation.SimplifyFraction
                    }
                }
            }

            step {
                fromExpr = "[x + 1 / x + 3] (x + 3) (x + 4)"
                toExpr = "(x + 1) (x + 4)"
                explanation {
                    key = RationalExpressionsExplanation.MultiplyRationalExpressions
                }

                step {
                    fromExpr = "[x + 1 / x + 3] (x + 3) (x + 4)"
                    toExpr = "[(x + 1) (x + 3) (x + 4) / x + 3]"
                    explanation {
                        key = FractionArithmeticExplanation.TurnProductOfFractionAndNonFractionFactorIntoFraction
                    }
                }

                step {
                    fromExpr = "[(x + 1) (x + 3) (x + 4) / x + 3]"
                    toExpr = "(x + 1) (x + 4)"
                    explanation {
                        key = GeneralExplanation.CancelDenominator
                    }
                }
            }
        }
    }

    @Test
    fun `test dividing rational expressions`() = testMethod {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] : [x + 1 / [x ^ 2] - x + 1]"

        check {
            fromExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] : [x + 1 / [x ^ 2] - x + 1]"
            toExpr = "x + 1"
            explanation {
                key = SimplifyExplanation.SimplifyAlgebraicExpression
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
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x] * [[x ^ 3] / [x ^ 2] - 4]"

        check {
            fromExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x] * [[x ^ 3] / [x ^ 2] - 4]"
            toExpr = "[[x ^ 2] / 4 (x - 2)]"
            explanation {
                key = SimplifyExplanation.SimplifyAlgebraicExpression
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
    fun `test simplifying algebraic expression containing additions and divisions`() = testMethod {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "([4 / x] - [7 / x - 3]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"

        check {
            fromExpr = "([4 / x] - [7 / x - 3]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
            toExpr = "-[9 / [(x - 3) ^ 2]]"
            explanation {
                key = SimplifyExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "([4 / x] - [7 / x - 3]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "(-[3 x + 12 / x (x - 3)]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = SimplifyExplanation.SimplifyExpressionInBrackets
                }
            }

            step {
                fromExpr = "(-[3 x + 12 / x (x - 3)]) : (1 + [1 / x] - [12 / [x ^ 2]]) + [9 / [x ^ 2] - 3 x] + [3 / x]"
                toExpr = "(-[3 x + 12 / x (x - 3)]) : [[x ^ 2] + x - 12 / [x ^ 2]] + [9 / [x ^ 2] - 3 x] + [3 / x]"
                explanation {
                    key = SimplifyExplanation.SimplifyExpressionInBrackets
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
}
