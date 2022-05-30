package plans

import org.junit.jupiter.api.Test

class TestAddUnlikeFractions {

    @Test
    fun simpleTest() = testPlan {
        plan = addUnlikeFractions
        inputExpr = "[1/5] + [2/5]"

        check {
            toExpr = "[3/5]"

            explanation = "add unlike fractions"

            step {
                toExpr = "[1 + 2/5]"

                explanation = "add like fractions"
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