package methods.constantexpressions

import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class CollectingLikeTermsTest {
    @Test
    fun testCollectLikeRootsAndSimplify() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "2 - 3 sqrt[3] + root[3, 3] + [2 sqrt[3] / 3] + 2 sqrt[3]"

            check {
                fromExpr = "2 - 3 sqrt[3] + root[3, 3] + [2 sqrt[3] / 3] + 2 sqrt[3]"
                toExpr = "2 - [sqrt[3] / 3] + root[3, 3]"
                explanation {
                    key = CollectingExplanation.CollectLikeRootsAndSimplify
                }

                step {
                    fromExpr = "2 - 3 sqrt[3] + root[3, 3] + [2 sqrt[3] / 3] + 2 sqrt[3]"
                    toExpr = "2 + (-3 + [2 / 3] + 2) sqrt[3] + root[3, 3]"
                    explanation {
                        key = CollectingExplanation.CollectLikeRoots
                    }
                }

                step {
                    fromExpr = "2 + (-3 + [2 / 3] + 2) sqrt[3] + root[3, 3]"
                    toExpr = "2 - [sqrt[3] / 3] + root[3, 3]"
                    explanation {
                        key = CollectingExplanation.SimplifyCoefficient
                    }

                    step {
                        fromExpr = "(-3 + [2 / 3] + 2) sqrt[3]"
                        toExpr = "(-1 + [2 / 3]) sqrt[3]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                        }
                    }

                    step {
                        fromExpr = "(-1 + [2 / 3]) sqrt[3]"
                        toExpr = "(-[1 / 3]) sqrt[3]"
                        explanation {
                            key = FractionArithmeticExplanation.AddIntegerAndFraction
                        }
                    }

                    step {
                        fromExpr = "(-[1 / 3]) sqrt[3]"
                        toExpr = "-[1 / 3] sqrt[3]"
                        explanation {
                            key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                        }
                    }

                    step {
                        fromExpr = "-[1 / 3] sqrt[3]"
                        toExpr = "-[sqrt[3] / 3]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                        }
                    }
                }
            }
        }
}
