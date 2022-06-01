package plans

import org.junit.jupiter.api.Test
import steps.metadata.Explanation
import steps.metadata.PlanExplanation
import steps.metadata.Skill

class TestAddUnlikeFractions {

    @Test
    fun simpleTest() = testPlan {
        plan = addUnlikeFractions
        inputExpr = "[1/5] + [2/5]"

        check {
            toExpr = "[3/5]"

            explanation {
                key = PlanExplanation.AddFractions

                param {
                    expr = "[1/5]"
                }
                param {
                    expr = "[2/5]"
                }
            }

            skill {
                key = Skill.AddFractions

                param {
                    expr = "[1/5]"
                }
                param {
                    expr = "[2/5]"
                }
            }

            step {
                toExpr = "[1 + 2/5]"

                explanation {
                    key = Explanation.AddLikeFractions
                }
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