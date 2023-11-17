package methods.simplify

import engine.methods.SolverEngineExplanation
import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerroots.IntegerRootsExplanation
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

    @Test
    fun `test simplify algebraic expression with common factor`() = testMethod {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "[x - 1 / (x + 1) [(1 - x)^3]]"

        check {
            fromExpr = "[x - 1 / (x + 1) [(1 - x) ^ 3]]"
            toExpr = "-[1 / (x + 1) [(x - 1) ^ 2]]"
            explanation {
                key = RationalExpressionsExplanation.SimplifyRationalExpression
            }

            step {
                fromExpr = "[x - 1 / (x + 1) [(1 - x) ^ 3]]"
                toExpr = "[1 / -(x + 1) [(x - 1) ^ 2]]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }

                step {
                    fromExpr = "[x - 1 / (x + 1) [(1 - x) ^ 3]]"
                    toExpr = "[x - 1 / (x + 1) [(-(-1 + x)) ^ 3]]"
                    explanation {
                        key = GeneralExplanation.FactorMinusFromSum
                    }
                }

                step {
                    fromExpr = "[x - 1 / (x + 1) [(-(-1 + x)) ^ 3]]"
                    toExpr = "[x - 1 / (x + 1) (-[(-1 + x) ^ 3])]"
                    explanation {
                        key = GeneralExplanation.SimplifyOddPowerOfNegative
                    }
                }

                step {
                    fromExpr = "[x - 1 / (x + 1) (-[(-1 + x) ^ 3])]"
                    toExpr = "[x - 1 / -(x + 1) [(-1 + x) ^ 3]]"
                    explanation {
                        key = GeneralExplanation.NormalizeNegativeSignsInProduct
                    }

                    step {
                        fromExpr = "(x + 1) (-[(-1 + x) ^ 3])"
                        toExpr = "-(x + 1) [(-1 + x) ^ 3]"
                        explanation {
                            key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                        }
                    }
                }

                step {
                    fromExpr = "[x - 1 / -(x + 1) [(-1 + x) ^ 3]]"
                    toExpr = "[x - 1 / -(x + 1) [(x - 1) ^ 3]]"
                    explanation {
                        key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                    }
                }

                step {
                    fromExpr = "[x - 1 / -(x + 1) [(x - 1) ^ 3]]"
                    toExpr = "[1 / -(x + 1) [(x - 1) ^ 2]]"
                    explanation {
                        key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                    }
                }
            }

            step {
                fromExpr = "[1 / -(x + 1) [(x - 1) ^ 2]]"
                toExpr = "-[1 / (x + 1) [(x - 1) ^ 2]]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyNegativeInDenominator
                }
            }
        }
    }

    @Test
    fun `test cancel two multiple after extracting -ve`() = testMethod {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "[(x - 1)[(x - 2)^2] / [(2 - x)^3](1 - x)]"

        check {
            fromExpr = "[(x - 1) [(x - 2) ^ 2] / [(2 - x) ^ 3] (1 - x)]"
            toExpr = "[1 / x - 2]"
            explanation {
                key = FractionArithmeticExplanation.SimplifyFraction
            }

            step {
                fromExpr = "[(x - 1) [(x - 2) ^ 2] / [(2 - x) ^ 3] (1 - x)]"
                toExpr = "[(x - 1) [(x - 2) ^ 2] / [(-(-2 + x)) ^ 3] (1 - x)]"
                explanation {
                    key = GeneralExplanation.FactorMinusFromSum
                }
            }

            step {
                fromExpr = "[(x - 1) [(x - 2) ^ 2] / [(-(-2 + x)) ^ 3] (1 - x)]"
                toExpr = "[(x - 1) [(x - 2) ^ 2] / (-[(-2 + x) ^ 3]) (1 - x)]"
                explanation {
                    key = GeneralExplanation.SimplifyOddPowerOfNegative
                }
            }

            step {
                fromExpr = "[(x - 1) [(x - 2) ^ 2] / (-[(-2 + x) ^ 3]) (1 - x)]"
                toExpr = "[(x - 1) [(x - 2) ^ 2] / (-[(-2 + x) ^ 3]) (-(-1 + x))]"
                explanation {
                    key = GeneralExplanation.FactorMinusFromSum
                }
            }

            step {
                fromExpr = "[(x - 1) [(x - 2) ^ 2] / (-[(-2 + x) ^ 3]) (-(-1 + x))]"
                toExpr = "[(x - 1) [(x - 2) ^ 2] / [(-2 + x) ^ 3] (-1 + x)]"
                explanation {
                    key = GeneralExplanation.NormalizeNegativeSignsInProduct
                }
            }

            step {
                fromExpr = "[(x - 1) [(x - 2) ^ 2] / [(-2 + x) ^ 3] (-1 + x)]"
                toExpr = "[(x - 1) [(x - 2) ^ 2] / [(-2 + x) ^ 3] (x - 1)]"
                explanation {
                    key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                }
            }

            step {
                fromExpr = "[(x - 1) [(x - 2) ^ 2] / [(-2 + x) ^ 3] (x - 1)]"
                toExpr = "[[(x - 2) ^ 2] / [(-2 + x) ^ 3]]"
                explanation {
                    key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                }
            }

            step {
                fromExpr = "[[(x - 2) ^ 2] / [(-2 + x) ^ 3]]"
                toExpr = "[[(x - 2) ^ 2] / [(x - 2) ^ 3]]"
                explanation {
                    key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                }
            }

            step {
                fromExpr = "[[(x - 2) ^ 2] / [(x - 2) ^ 3]]"
                toExpr = "[1 / x - 2]"
                explanation {
                    key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                }
            }
        }
    }

    @Test
    fun `test cancellation of one factor even after possible minus extraction`() = testMethod {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "[(-x - 1)[(x - 2)^2] / [(2 - x)^3](x - 1)]"

        check {
            fromExpr = "[(-x - 1) [(x - 2) ^ 2] / [(2 - x) ^ 3] (x - 1)]"
            toExpr = "[x + 1 / (x - 2) (x - 1)]"
            explanation {
                key = RationalExpressionsExplanation.SimplifyRationalExpression
            }

            step {
                fromExpr = "[(-x - 1) [(x - 2) ^ 2] / [(2 - x) ^ 3] (x - 1)]"
                toExpr = "[-x - 1 / -(x - 2) (x - 1)]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }

                step {
                    fromExpr = "[(-x - 1) [(x - 2) ^ 2] / [(2 - x) ^ 3] (x - 1)]"
                    toExpr = "[(-x - 1) [(x - 2) ^ 2] / [(-(-2 + x)) ^ 3] (x - 1)]"
                    explanation {
                        key = GeneralExplanation.FactorMinusFromSum
                    }
                }

                step {
                    fromExpr = "[(-x - 1) [(x - 2) ^ 2] / [(-(-2 + x)) ^ 3] (x - 1)]"
                    toExpr = "[(-x - 1) [(x - 2) ^ 2] / (-[(-2 + x) ^ 3]) (x - 1)]"
                    explanation {
                        key = GeneralExplanation.SimplifyOddPowerOfNegative
                    }
                }

                step {
                    fromExpr = "[(-x - 1) [(x - 2) ^ 2] / (-[(-2 + x) ^ 3]) (x - 1)]"
                    toExpr = "[(-x - 1) [(x - 2) ^ 2] / -[(-2 + x) ^ 3] (x - 1)]"
                    explanation {
                        key = GeneralExplanation.NormalizeNegativeSignsInProduct
                    }

                    step {
                        fromExpr = "(-[(-2 + x) ^ 3]) (x - 1)"
                        toExpr = "-[(-2 + x) ^ 3] (x - 1)"
                        explanation {
                            key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                        }
                    }
                }

                step {
                    fromExpr = "[(-x - 1) [(x - 2) ^ 2] / -[(-2 + x) ^ 3] (x - 1)]"
                    toExpr = "[(-x - 1) [(x - 2) ^ 2] / -[(x - 2) ^ 3] (x - 1)]"
                    explanation {
                        key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                    }
                }

                step {
                    fromExpr = "[(-x - 1) [(x - 2) ^ 2] / -[(x - 2) ^ 3] (x - 1)]"
                    toExpr = "[-x - 1 / -(x - 2) (x - 1)]"
                    explanation {
                        key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                    }
                }
            }

            step {
                fromExpr = "[-x - 1 / -(x - 2) (x - 1)]"
                toExpr = "[-(x + 1) / -(x - 2) (x - 1)]"
                explanation {
                    key = GeneralExplanation.FactorMinusFromSumWithAllNegativeTerms
                }
            }

            step {
                fromExpr = "[-(x + 1) / -(x - 2) (x - 1)]"
                toExpr = "[x + 1 / (x - 2) (x - 1)]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyNegativeInNumeratorAndDenominator
                }
            }
        }
    }
}

class AlgebraicExpressionContainingAbsoluteValue {
    @Test
    fun `test simplify even power of absoluteValue`() = testMethod {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "[abs[y]^2]"

        check {
            fromExpr = "[abs[y] ^ 2]"
            toExpr = "[y ^ 2]"
            explanation {
                key = GeneralExplanation.SimplifyEvenPowerOfAbsoluteValue
            }
        }
    }

    @Test
    fun `test simplify cancellable root of power to even power`() = testMethod {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "root[[y^4], 2]"

        check {
            fromExpr = "root[[y ^ 4], 2]"
            toExpr = "[y ^ 2]"
            explanation {
                key = SimplifyExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "root[[y ^ 4], 2]"
                toExpr = "[abs[y] ^ 2]"
                explanation {
                    key = IntegerRootsExplanation.RewriteAndCancelPowerUnderRoot
                }

                step {
                    fromExpr = "root[[y ^ 4], 2]"
                    toExpr = "root[[abs[y] ^ 4], 2]"
                    explanation {
                        key = GeneralExplanation.RewriteEvenPowerOfBaseAsEvenPowerOfAbsoluteValueOfBase
                    }
                }

                step {
                    fromExpr = "root[[abs[y] ^ 4], 2]"
                    toExpr = "sqrt[[abs[y] ^ 2 * 2]]"
                    explanation {
                        key = GeneralExplanation.RewritePowerUnderRoot
                    }
                }

                step {
                    fromExpr = "sqrt[[abs[y] ^ 2 * 2]]"
                    toExpr = "[abs[y] ^ 2]"
                    explanation {
                        key = GeneralExplanation.CancelRootIndexAndExponent
                    }
                }
            }

            step {
                fromExpr = "[abs[y] ^ 2]"
                toExpr = "[y ^ 2]"
                explanation {
                    key = GeneralExplanation.SimplifyEvenPowerOfAbsoluteValue
                }
            }
        }
    }

    @Test
    fun `test simplify cancellable root of power to power of absolute value`() = testMethod {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "root[[(y + 1)^12], 4]"

        check {
            fromExpr = "root[[(y + 1) ^ 12], 4]"
            toExpr = "[abs[y + 1] ^ 3]"
            explanation {
                key = IntegerRootsExplanation.RewriteAndCancelPowerUnderRoot
            }

            step {
                fromExpr = "root[[(y + 1) ^ 12], 4]"
                toExpr = "root[[abs[y + 1] ^ 12], 4]"
                explanation {
                    key = GeneralExplanation.RewriteEvenPowerOfBaseAsEvenPowerOfAbsoluteValueOfBase
                }
            }

            step {
                fromExpr = "root[[abs[y + 1] ^ 12], 4]"
                toExpr = "root[[abs[y + 1] ^ 3 * 4], 4]"
                explanation {
                    key = GeneralExplanation.RewritePowerUnderRoot
                }
            }

            step {
                fromExpr = "root[[abs[y + 1] ^ 3 * 4], 4]"
                toExpr = "[abs[y + 1] ^ 3]"
                explanation {
                    key = GeneralExplanation.CancelRootIndexAndExponent
                }
            }
        }
    }
}
