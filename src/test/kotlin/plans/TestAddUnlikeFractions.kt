package plans

import org.junit.jupiter.api.Test
import steps.metadata.Explanation

class TestAddUnlikeFractions {

    @Test
    fun simpleTest() = testPlan {
        plan = addUnlikeFractions
        inputExpr = "[1/5] + [2/5]"

        check {
            toExpr = "[3/5]"
//
//            explanation {
//                key = "add fractions"
//                param("[1/5]")
//                param("[3/5]")
//            }

            step {
                toExpr = "[1 + 2/5]"

                explanation = Explanation.AddLikeFractions
            }

            step {
                fromExpr = "[1 + 2/5]"
                toExpr = "[3/5]"

                step {
                    step {
                        fromExpr = "1 + 2"
                        toExpr = "3"
                    }
                }
            }
        }
    }
}