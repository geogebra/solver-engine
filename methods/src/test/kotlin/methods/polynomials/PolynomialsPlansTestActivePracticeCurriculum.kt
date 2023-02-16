package methods.polynomials

import engine.context.Context
import engine.context.Curriculum
import engine.methods.testMethod
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerrationalexponents.IntegerRationalExponentsExplanation
import methods.integerroots.IntegerRootsExplanation
import org.junit.jupiter.api.Test

class PolynomialsPlansTestActivePracticeCurriculum {

    @Test
    fun testCollectSimpleLikeTerms() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        context = Context(curriculum = Curriculum.GM)
        inputExpr = "2 x + [x ^ 2] + 3 x - 5 [x ^ 2]"

        check {
            fromExpr = "2 x + [x ^ 2] + 3 x - 5 [x ^ 2]"
            toExpr = "5 x - 4 [x ^ 2]"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "2 x + [x ^ 2] + 3 x - 5 [x ^ 2]"
                toExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.CombineTwoSimpleLikeTerms
                }
            }

            step {
                fromExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                toExpr = "5 x - 4 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.CombineTwoSimpleLikeTerms
                }
            }
        }
    }

    @Test
    fun testMultiplyMonomials() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        context = Context(curriculum = Curriculum.GM)
        inputExpr = "3[x^2]*4[x^3]x + 5*7[x^6]"

        check {
            fromExpr = "3 [x ^ 2] * 4 [x ^ 3] x + 5 * 7 [x ^ 6]"
            toExpr = "47 [x ^ 6]"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "3 [x ^ 2] * 4 [x ^ 3] x + 5 * 7 [x ^ 6]"
                toExpr = "3 [x ^ 5] * 4 x + 5 * 7 [x ^ 6]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }

                step {
                    fromExpr = "3 [x ^ 2] * 4 [x ^ 3] x"
                    toExpr = "3 [x ^ 2 + 3] * 4 x"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "3 [x ^ 2 + 3] * 4 x"
                    toExpr = "3 [x ^ 5] * 4 x"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }
            }

            step {
                fromExpr = "3 [x ^ 5] * 4 x + 5 * 7 [x ^ 6]"
                toExpr = "3 [x ^ 6] * 4 + 5 * 7 [x ^ 6]"
                explanation {
                    key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                }

                step {
                    fromExpr = "3 [x ^ 5] * 4 x"
                    toExpr = "3 [x ^ 5 + 1] * 4"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "3 [x ^ 5 + 1] * 4"
                    toExpr = "3 [x ^ 6] * 4"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                    }
                }
            }

            step {
                fromExpr = "3 [x ^ 6] * 4 + 5 * 7 [x ^ 6]"
                toExpr = "12 [x ^ 6] + 35 [x ^ 6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }

                step {
                    fromExpr = "3 [x ^ 6] * 4 + 5 * 7 [x ^ 6]"
                    toExpr = "12 [x ^ 6] + 5 * 7 [x ^ 6]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                    }

                    step {
                        fromExpr = "3 [x ^ 6] * 4"
                        toExpr = "12 [x ^ 6]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }

                step {
                    fromExpr = "12 [x ^ 6] + 5 * 7 [x ^ 6]"
                    toExpr = "12 [x ^ 6] + 35 [x ^ 6]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                    }

                    step {
                        fromExpr = "5 * 7 [x ^ 6]"
                        toExpr = "35 [x ^ 6]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }
            }

            step {
                fromExpr = "12 [x ^ 6] + 35 [x ^ 6]"
                toExpr = "47 [x ^ 6]"
                explanation {
                    key = PolynomialsExplanation.CombineTwoSimpleLikeTerms
                }
            }
        }
    }

    @Test
    fun testMonomialWithZeroCoefficientEliminated() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        context = Context(curriculum = Curriculum.GM)
        inputExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"

        check {
            fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
            toExpr = "7 a"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                toExpr = "5 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                explanation {
                    key = PolynomialsExplanation.CombineTwoSimpleLikeTerms
                }
            }

            step {
                fromExpr = "5 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                toExpr = "-3 a + 10 a"
                explanation {
                    key = GeneralExplanation.CancelAdditiveInverseElements
                }
            }

            step {
                fromExpr = "-3 a + 10 a"
                toExpr = "7 a"
                explanation {
                    key = PolynomialsExplanation.CombineTwoSimpleLikeTerms
                }
            }
        }
    }
}
