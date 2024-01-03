package methods.constantexpressions

import engine.methods.testMethod
import methods.expand.ExpandExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class ExpandConstantExpressionsTest {
    @Test
    fun `test expanding single bracket with positive factor on the right`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(1 + sqrt[2]) * 2"

            check {
                fromExpr = "(1 + sqrt[2]) * 2"
                toExpr = "2 + 2 sqrt[2]"
                explanation {
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }

                step {
                    fromExpr = "(1 + sqrt[2]) * 2"
                    toExpr = "1 * 2 + sqrt[2] * 2"
                    explanation {
                        key = ExpandExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "1 * 2 + sqrt[2] * 2"
                    toExpr = "2 + 2 sqrt[2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }
            }
        }

    @Test
    fun `test expanding single bracket with negative factor on the right`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(1 + sqrt[2]) * (-2)"

            check {
                fromExpr = "(1 + sqrt[2]) * (-2)"
                toExpr = "-2 - 2 sqrt[2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "(1 + sqrt[2]) * (-2)"
                    toExpr = "(-2) (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "(-2) (1 + sqrt[2])"
                    toExpr = "-2 (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                    }
                }

                step {
                    fromExpr = "-2 (1 + sqrt[2])"
                    toExpr = "-2 - 2 sqrt[2]"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "-2 (1 + sqrt[2])"
                        toExpr = "-2 * 1 - 2 * sqrt[2]"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                            param { expr = "-2" }
                        }
                    }

                    step {
                        fromExpr = "-2 * 1 - 2 * sqrt[2]"
                        toExpr = "-2 - 2 sqrt[2]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }
                }
            }
        }

    @Test
    fun `test expanding single bracket in negated product`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(1 + sqrt[2]) * (-2)"

            check {
                fromExpr = "(1 + sqrt[2]) * (-2)"
                toExpr = "-2 - 2 sqrt[2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "(1 + sqrt[2]) * (-2)"
                    toExpr = "(-2) (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "(-2) (1 + sqrt[2])"
                    toExpr = "-2 (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                    }
                }

                step {
                    fromExpr = "-2 (1 + sqrt[2])"
                    toExpr = "-2 - 2 sqrt[2]"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "-2 (1 + sqrt[2])"
                        toExpr = "-2 * 1 - 2 * sqrt[2]"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "-2 * 1 - 2 * sqrt[2]"
                        toExpr = "-2 - 2 sqrt[2]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }
                }
            }
        }

    @Test
    fun `test expanding single bracket with factors on both sides`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "2 (1 + sqrt[2]) sqrt[2]"

            check {
                fromExpr = "2 (1 + sqrt[2]) sqrt[2]"
                toExpr = "2 sqrt[2] + 4"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "2 (1 + sqrt[2]) sqrt[2]"
                    toExpr = "2 sqrt[2] (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "2 sqrt[2] (1 + sqrt[2])"
                    toExpr = "2 sqrt[2] + 4"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "2 sqrt[2] (1 + sqrt[2])"
                        toExpr = "<.2 sqrt[2].> * 1 + <.2 sqrt[2].> * sqrt[2]"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "2 sqrt[2] * 1 + 2 sqrt[2] * sqrt[2]"
                        toExpr = "2 sqrt[2] + 4"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }
                }
            }
        }
}
