package methods.constantexpressions

import engine.methods.testMethod
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class NormalizationTest {
    @Test
    fun testSimpleNormalization() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(+1 + (3))"

            check {
                step {
                    toExpr = "1 + 3"
                    explanation {
                        key = GeneralExplanation.NormalizeExpression
                    }

                    step { toExpr = "+1 + (3)" }
                    step { toExpr = "+1 + 3" }
                    step { toExpr = "1 + 3" }
                }

                step { toExpr = "4" }
            }
        }

    @Test
    fun testNoNormalizationIfNotNeeded() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "0 * (1)"

            check {
                fromExpr = "0 * (1)"
                toExpr = "0"

                explanation {
                    key = GeneralExplanation.EvaluateProductContainingZero
                }
            }
        }

    @Test
    fun `test remove brackets and simplify a (bc) (de)`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"

            check {
                fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                toExpr = "12 sqrt[2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                    toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                    explanation {
                        key = GeneralExplanation.RemoveAllBracketProductInProduct
                    }

                    step {
                        fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                        toExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                        explanation {
                            key = GeneralExplanation.RemoveBracketProductInProduct
                        }
                    }

                    step {
                        fromExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                        toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                        explanation {
                            key = GeneralExplanation.RemoveBracketProductInProduct
                        }
                    }
                }

                step { }
            }
        }

    @Test
    fun `test remove brackets and simplify (a) (bc) (de)`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(sqrt[2]) (2 sqrt[2]) (3 sqrt[2])"

            check {
                fromExpr = "(sqrt[2]) (2 sqrt[2]) (3 sqrt[2])"
                toExpr = "12 sqrt[2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "(sqrt[2]) (2 sqrt[2]) (3 sqrt[2])"
                    toExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                    explanation {
                        key = GeneralExplanation.RemoveRedundantBracket
                    }
                }

                step {
                    fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                    toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                    explanation {
                        key = GeneralExplanation.RemoveAllBracketProductInProduct
                    }

                    step {
                        fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                        toExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                        explanation {
                            key = GeneralExplanation.RemoveBracketProductInProduct
                        }
                    }

                    step {
                        fromExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                        toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                        explanation {
                            key = GeneralExplanation.RemoveBracketProductInProduct
                        }
                    }
                }

                step { }
            }
        }
}
