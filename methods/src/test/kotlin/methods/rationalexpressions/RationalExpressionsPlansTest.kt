package methods.rationalexpressions

import engine.methods.testMethod
import methods.expand.ExpandExplanation
import methods.factor.FactorExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class RationalExpressionsPlansTest {
    @Test
    fun `test simplification of rational expression`() = testMethod {
        method = RationalExpressionsPlans.SimplifyRationalExpression
        inputExpr = "[[x ^ 2] + 4 x - 45 / [x ^ 2] + x - 30]"

        check {
            fromExpr = "[[x ^ 2] + 4 x - 45 / [x ^ 2] + x - 30]"
            toExpr = "[x + 9 / x + 6]"
            explanation {
                key = RationalExpressionsExplanation.SimplifyRationalExpression
            }

            step {
                fromExpr = "[[x ^ 2] + 4 x - 45 / [x ^ 2] + x - 30]"
                toExpr = "[(x - 5) (x + 9) / [x ^ 2] + x - 30]"
                explanation {
                    key = FactorExplanation.FactorTrinomialByGuessing
                }
            }

            step {
                fromExpr = "[(x - 5) (x + 9) / [x ^ 2] + x - 30]"
                toExpr = "[(x - 5) (x + 9) / (x - 5) (x + 6)]"
                explanation {
                    key = FactorExplanation.FactorTrinomialByGuessing
                }
            }

            step {
                fromExpr = "[(x - 5) (x + 9) / (x - 5) (x + 6)]"
                toExpr = "[x + 9 / x + 6]"
                explanation {
                    key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                }
            }
        }
    }

    @Test
    fun `test addition of like rational expressions`() = testMethod {
        method = RationalExpressionsPlans.AddLikeRationalExpressions
        inputExpr = "[[x ^ 2] / x + 1] + [x / x + 1]"

        check {
            fromExpr = "[[x ^ 2] / x + 1] + [x / x + 1]"
            toExpr = "x"
            explanation {
                key = RationalExpressionsExplanation.AddLikeRationalExpressions
            }

            step {
                fromExpr = "[[x ^ 2] / x + 1] + [x / x + 1]"
                toExpr = "[[x ^ 2] + x / x + 1]"
                explanation {
                    key = FractionArithmeticExplanation.AddLikeFractions
                }
            }

            step {
                fromExpr = "[[x ^ 2] + x / x + 1]"
                toExpr = "[x (x + 1) / x + 1]"
                explanation {
                    key = FactorExplanation.FactorGreatestCommonFactor
                }
            }

            step {
                fromExpr = "[x (x + 1) / x + 1]"
                toExpr = "x"
                explanation {
                    key = GeneralExplanation.CancelDenominator
                }
            }
        }
    }

    @Test
    fun `test addition of non-fractional term and rational expression`() = testMethod {
        method = RationalExpressionsPlans.AddTermAndRationalExpression
        inputExpr = "2 x + [1 - x / 1 + x]"

        check {
            fromExpr = "2 x + [1 - x / 1 + x]"
            toExpr = "[2 [x ^ 2] + x + 1 / 1 + x]"
            explanation {
                key = RationalExpressionsExplanation.AddTermAndRationalExpression
            }

            step {
                fromExpr = "2 x + [1 - x / 1 + x]"
                toExpr = "[2 x (1 + x) / 1 + x] + [1 - x / 1 + x]"
                explanation {
                    key = FractionArithmeticExplanation.BringToCommonDenominatorWithNonFractionalTerm
                }
            }

            step {
                fromExpr = "[2 x (1 + x) / 1 + x] + [1 - x / 1 + x]"
                toExpr = "[2 x (1 + x) + 1 - x / 1 + x]"
                explanation {
                    key = FractionArithmeticExplanation.AddLikeFractions
                }
            }

            step {
                fromExpr = "[2 x (1 + x) + 1 - x / 1 + x]"
                toExpr = "[2 [x ^ 2] + x + 1 / 1 + x]"
                explanation {
                    key = PolynomialsExplanation.ExpandPolynomialExpression
                }
            }
        }
    }

    @Test
    fun `test addition of term and fraction which is not a rational expression fails`() = testMethod {
        method = RationalExpressionsPlans.AddTermAndRationalExpression
        inputExpr = "2 x + [y / 2]"

        check {
            noTransformation()
        }
    }

    @Test
    fun `test addition of unlike rational expressions without factorization`() = testMethod {
        method = RationalExpressionsPlans.AddRationalExpressions
        inputExpr = "[[x ^ 2] + 1 / x + 1] + [x - 1 / x + 2]"

        check {
            fromExpr = "[[x ^ 2] + 1 / x + 1] + [x - 1 / x + 2]"
            toExpr = "[[x ^ 3] + 3 [x ^ 2] + x + 1 / (x + 1) (x + 2)]"
            explanation {
                key = RationalExpressionsExplanation.AddRationalExpressions
            }

            task {
                taskId = "#1"
                startExpr = "(x + 1) (x + 2)"
                explanation {
                    key = RationalExpressionsExplanation.ComputeLeastCommonDenominatorOfFractions
                }
            }

            task {
                taskId = "#2"
                startExpr = "[([x ^ 2] + 1) * (x + 2) / (x + 1) * (x + 2)]"
                explanation {
                    key = RationalExpressionsExplanation.BringFractionToLeastCommonDenominator
                }

                step {
                    fromExpr = "[([x ^ 2] + 1) * (x + 2) / (x + 1) * (x + 2)]"
                    toExpr = "[[x ^ 3] + 2 [x ^ 2] + x + 2 / (x + 1) * (x + 2)]"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }

                    step { /* remove product sign */ }

                    step {
                        fromExpr = "([x ^ 2] + 1) (x + 2)"
                        toExpr = "[x ^ 3] + 2 [x ^ 2] + x + 2"
                        explanation {
                            key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
                        }
                    }
                }

                step {
                    fromExpr = "[[x ^ 3] + 2 [x ^ 2] + x + 2 / (x + 1) * (x + 2)]"
                    toExpr = "[[x ^ 3] + 2 [x ^ 2] + x + 2 / (x + 1) (x + 2)]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
                    }

                    step {
                        fromExpr = "(x + 1) * (x + 2)"
                        toExpr = "(x + 1) (x + 2)"
                        explanation {
                            key = GeneralExplanation.NormalizeProducts
                        }
                    }
                }
            }

            task {
                taskId = "#3"
                startExpr = "[(x - 1) * (x + 1) / (x + 2) * (x + 1)]"
                explanation {
                    key = RationalExpressionsExplanation.BringFractionToLeastCommonDenominator
                }

                step {
                    fromExpr = "[(x - 1) * (x + 1) / (x + 2) * (x + 1)]"
                    toExpr = "[[x ^ 2] - 1 / (x + 2) * (x + 1)]"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }

                step {
                    fromExpr = "[[x ^ 2] - 1 / (x + 2) * (x + 1)]"
                    toExpr = "[[x ^ 2] - 1 / (x + 2) (x + 1)]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
                    }
                }
            }

            task {
                taskId = "#4"
                startExpr = "[[x ^ 3] + 2 [x ^ 2] + x + 2 + [x ^ 2] - 1 / (x + 1) (x + 2)]"
                explanation {
                    key = RationalExpressionsExplanation.AddLikeRationalExpressions
                }

                step {
                    fromExpr = "[[x ^ 3] + 2 [x ^ 2] + x + 2 + [x ^ 2] - 1 / (x + 1) (x + 2)]"
                    toExpr = "[[x ^ 3] + 3 [x ^ 2] + x + 1 / (x + 1) (x + 2)]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
                    }
                }
            }
        }
    }

    @Test
    fun `test addition of rational expressions with one term needing factorization`() = testMethod {
        method = RationalExpressionsPlans.AddRationalExpressions
        inputExpr = "[[x ^ 2] + 1 / [x ^ 2] + 3 x + 2] + [x - 1 / x + 2]"

        check {
            fromExpr = "[[x ^ 2] + 1 / [x ^ 2] + 3 x + 2] + [x - 1 / x + 2]"
            toExpr = "[2 [x ^ 2] / (x + 1) (x + 2)]"
            explanation {
                key = RationalExpressionsExplanation.AddRationalExpressions
            }

            task {
                taskId = "#1"
                startExpr = "[[x ^ 2] + 1 / [x ^ 2] + 3 x + 2]"
                explanation {
                    key = RationalExpressionsExplanation.FactorDenominatorOfFraction
                }

                step {
                    fromExpr = "[[x ^ 2] + 1 / [x ^ 2] + 3 x + 2]"
                    toExpr = "[[x ^ 2] + 1 / (x + 1) (x + 2)]"
                    explanation {
                        key = FactorExplanation.FactorPolynomial
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "(x + 1) (x + 2)"
                explanation {
                    key = RationalExpressionsExplanation.ComputeLeastCommonDenominatorOfFractions
                }
            }

            task {
                taskId = "#3"
                startExpr = "[(x - 1) * (x + 1) / (x + 2) * (x + 1)]"
                explanation {
                    key = RationalExpressionsExplanation.BringFractionToLeastCommonDenominator
                }

                step {
                    fromExpr = "[(x - 1) * (x + 1) / (x + 2) * (x + 1)]"
                    toExpr = "[[x ^ 2] - 1 / (x + 2) * (x + 1)]"
                    explanation {
                        key = PolynomialsExplanation.ExpandPolynomialExpression
                    }
                }

                step {
                    fromExpr = "[[x ^ 2] - 1 / (x + 2) * (x + 1)]"
                    toExpr = "[[x ^ 2] - 1 / (x + 2) (x + 1)]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
                    }
                }
            }

            task {
                taskId = "#4"
                startExpr = "[[x ^ 2] + 1 + [x ^ 2] - 1 / (x + 1) (x + 2)]"
                explanation {
                    key = RationalExpressionsExplanation.AddLikeRationalExpressions
                }

                step {
                    fromExpr = "[[x ^ 2] + 1 + [x ^ 2] - 1 / (x + 1) (x + 2)]"
                    toExpr = "[2 [x ^ 2] / (x + 1) (x + 2)]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
                    }
                }
            }
        }
    }

    @Test
    fun `test power of rational expressions`() = testMethod {
        method = RationalExpressionsPlans.SimplifyPowerOfRationalExpression
        inputExpr = "[([2 x / x + 1]) ^ -2]"

        check {
            fromExpr = "[([2 x / x + 1]) ^ -2]"
            toExpr = "[[(x + 1) ^ 2] / 4 [x ^ 2]]"
            explanation {
                key = RationalExpressionsExplanation.SimplifyPowerOfRationalExpression
            }

            step {
                fromExpr = "[([2 x / x + 1]) ^ -2]"
                toExpr = "[([x + 1 / 2 x]) ^ 2]"
                explanation {
                    key = GeneralExplanation.FlipFractionUnderNegativePower
                }
            }

            step {
                fromExpr = "[([x + 1 / 2 x]) ^ 2]"
                toExpr = "[[(x + 1) ^ 2] / [(2 x) ^ 2]]"
                explanation {
                    key = FractionArithmeticExplanation.DistributeFractionPositivePower
                }
            }

            step {
                fromExpr = "[[(x + 1) ^ 2] / [(2 x) ^ 2]]"
                toExpr = "[[(x + 1) ^ 2] / 4 [x ^ 2]]"
                explanation {
                    key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
                }
            }
        }
    }

    @Test
    fun `test division of polynomial by monomial`() = testMethod {
        method = RationalExpressionsPlans.SimplifyDivisionOfPolynomial
        inputExpr = "(5 [x ^ 7] + 12 [x ^ 3] [y ^ 7] - [10 / 7] [x ^ 5] y) : (-[1 / 5] [x ^ 3])"

        check {
            fromExpr = "(5 [x ^ 7] + 12 [x ^ 3] [y ^ 7] - [10 / 7] [x ^ 5] y) : (-[1 / 5] [x ^ 3])"
            toExpr = "-25 [x ^ 4] - 60 [y ^ 7] + [50 / 7] [x ^ 2] y"
            explanation {
                key = RationalExpressionsExplanation.SimplifyDivisionOfPolynomial
            }

            step {
                fromExpr = "(5 [x ^ 7] + 12 [x ^ 3] [y ^ 7] - [10 / 7] [x ^ 5] y) : (-[1 / 5] [x ^ 3])"
                toExpr = "5 [x ^ 7] : (-[1 / 5] [x ^ 3]) + 12 [x ^ 3] [y ^ 7] : (-[1 / 5] [x ^ 3]) " +
                    "+ (-[10 / 7] [x ^ 5] y) : (-[1 / 5] [x ^ 3])"
                explanation {
                    key = RationalExpressionsExplanation.DistributeDivisionOverSum
                }
            }

            step {
                fromExpr = "5 [x ^ 7] : (-[1 / 5] [x ^ 3]) + 12 [x ^ 3] [y ^ 7] : (-[1 / 5] [x ^ 3]) " +
                    "+ (-[10 / 7] [x ^ 5] y) : (-[1 / 5] [x ^ 3])"
                toExpr = "-25 [x ^ 4] + 12 [x ^ 3] [y ^ 7] : (-[1 / 5] [x ^ 3]) " +
                    "+ (-[10 / 7] [x ^ 5] y) : (-[1 / 5] [x ^ 3])"
                explanation {
                    key = RationalExpressionsExplanation.SimplifyRationalExpression
                }
            }

            step {
                fromExpr = "-25 [x ^ 4] + 12 [x ^ 3] [y ^ 7] : (-[1 / 5] [x ^ 3]) " +
                    "+ (-[10 / 7] [x ^ 5] y) : (-[1 / 5] [x ^ 3])"
                toExpr = "-25 [x ^ 4] - 60 [y ^ 7] + (-[10 / 7] [x ^ 5] y) : (-[1 / 5] [x ^ 3])"
                explanation {
                    key = RationalExpressionsExplanation.SimplifyRationalExpression
                }
            }

            step {
                fromExpr = "-25 [x ^ 4] - 60 [y ^ 7] + (-[10 / 7] [x ^ 5] y) : (-[1 / 5] [x ^ 3])"
                toExpr = "-25 [x ^ 4] - 60 [y ^ 7] + [50 / 7] [x ^ 2] y"
                explanation {
                    key = RationalExpressionsExplanation.SimplifyRationalExpression
                }
            }
        }
    }
}
