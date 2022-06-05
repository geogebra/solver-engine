package plans

import org.junit.jupiter.api.Test
import steps.metadata.Explanation
import steps.metadata.PlanExplanation
import steps.metadata.Skill

class TestAddUnlikeFractions {

    @Test
    fun addLikeFractionsTest() = testPlan {
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
                    fromExpr = "1 + 2"
                    toExpr = "3"
                }
            }
        }
    }

    @Test
    fun addUnlikeFractionsTest() = testPlan {
        plan = addUnlikeFractions
        inputExpr = "[1/3] + [2/5]"

        check {
            toExpr = "[11 / 15]"

            step {
                toExpr = "[1 * 5 / 3 * 5] + [2 * 3 / 5 * 3]"
            }

            step {
                toExpr = "[5 / 15] + [6 / 15]"
            }

            step {
                toExpr = "[5 + 6 / 15]"
            }

            step {
                toExpr = "[11 / 15]"
            }
        }
    }
}