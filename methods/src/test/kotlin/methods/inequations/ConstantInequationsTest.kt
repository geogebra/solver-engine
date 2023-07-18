package methods.inequations

import engine.methods.testMethod
import methods.expand.ExpandExplanation
import org.junit.jupiter.api.Test

class ConstantInequationsTest {

    @Test
    fun `test trivial false inequation`() = testMethod {
        method = InequationsPlans.SolveConstantInequation
        inputExpr = "sqrt[2] != sqrt[2]"
        check {
            fromExpr = "sqrt[2] != sqrt[2]"
            toExpr = "Contradiction[sqrt[2] != sqrt[2]]"
            explanation {
                key = InequationsExplanation.ExtractFalsehoodFromFalseInequation
            }
        }
    }

    @Test
    fun `test inequation with incommensurable values`() = testMethod {
        method = InequationsPlans.SolveConstantInequation
        inputExpr = "1 + sqrt[3] != 3 - sqrt[3]"
        check {
            fromExpr = "1 + sqrt[3] != 3 - sqrt[3]"
            toExpr = "Identity[-2 + 2 sqrt[3] != 0]"
            explanation {
                key = InequationsExplanation.SolveConstantInequation
            }
            step {
                fromExpr = "1 + sqrt[3] != 3 - sqrt[3]"
                toExpr = "-2 + 2 sqrt[3] != 0"
                explanation {
                    key = methods.solvable.InequalitiesExplanation.MoveEverythingToTheLeftAndSimplify
                }
            }
            step {
                fromExpr = "-2 + 2 sqrt[3] != 0"
                toExpr = "Identity[-2 + 2 sqrt[3] != 0]"
                explanation {
                    key = InequationsExplanation.ExtractTruthFromTrueInequation
                }
            }
        }
    }

    @Test
    fun `test false inequation with simplification`() = testMethod {
        method = InequationsPlans.SolveConstantInequation
        inputExpr = "[(1 + sqrt[2]) ^ 2] != 3 + 2 sqrt[2]"
        check {
            fromExpr = "[(1 + sqrt[2]) ^ 2] != 3 + 2 sqrt[2]"
            toExpr = "Contradiction[3 + 2 sqrt[2] != 3 + 2 sqrt[2]]"
            explanation {
                key = InequationsExplanation.SolveConstantInequation
            }
            step {
                fromExpr = "[(1 + sqrt[2]) ^ 2] != 3 + 2 sqrt[2]"
                toExpr = "3 + 2 sqrt[2] != 3 + 2 sqrt[2]"
                explanation {
                    key = ExpandExplanation.ExpandBinomialSquaredAndSimplify
                }
            }
            step {
                fromExpr = "3 + 2 sqrt[2] != 3 + 2 sqrt[2]"
                toExpr = "Contradiction[3 + 2 sqrt[2] != 3 + 2 sqrt[2]]"
                explanation {
                    key = InequationsExplanation.ExtractFalsehoodFromFalseInequation
                }
            }
        }
    }
}
