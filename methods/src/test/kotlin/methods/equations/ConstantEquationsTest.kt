package methods.equations

import engine.methods.testMethod
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class ConstantEquationsTest {

    @Test
    fun `test simple constant equation resulting in falsehood`() = testMethod {
        method = EquationsPlans.SolveConstantEquation
        inputExpr = "abs[1 + sqrt[2]] = 0"

        check {
            fromExpr = "abs[1 + sqrt[2]] = 0"
            toExpr = "Contradiction[1 + sqrt[2] = 0]"
            explanation {
                key = EquationsExplanation.SolveConstantEquation
            }

            step {
                fromExpr = "abs[1 + sqrt[2]] = 0"
                toExpr = "1 + sqrt[2] = 0"
                explanation {
                    key = GeneralExplanation.EvaluateAbsoluteValue
                }
            }

            step {
                fromExpr = "1 + sqrt[2] = 0"
                toExpr = "Contradiction[1 + sqrt[2] = 0]"
                explanation {
                    key = EquationsExplanation.ExtractFalsehoodFromFalseEquality
                }
            }
        }
    }

    @Test
    fun `test simple constant equation resulting in truth`() = testMethod {
        method = EquationsPlans.SolveConstantEquation
        inputExpr = "1 + sqrt[2] = 1 + sqrt[2]"

        check {
            fromExpr = "1 + sqrt[2] = 1 + sqrt[2]"
            toExpr = "Identity[1 + sqrt[2] = 1 + sqrt[2]]"
            explanation {
                key = EquationsExplanation.ExtractTruthFromTrueEquality
            }
        }
    }

    @Test
    fun `test constant equation requiring simplification and resulting in falsehood`() = testMethod {
        method = EquationsPlans.SolveConstantEquation
        inputExpr = "5 + [(1 + sqrt[2]) ^ 2] = [(2 + sqrt[2]) ^ 2] + 1"

        check {
            fromExpr = "5 + [(1 + sqrt[2]) ^ 2] = [(2 + sqrt[2]) ^ 2] + 1"
            toExpr = "Contradiction[1 - 2 sqrt[2] = 0]"
            explanation {
                key = EquationsExplanation.SolveConstantEquation
            }

            step {
                fromExpr = "5 + [(1 + sqrt[2]) ^ 2] = [(2 + sqrt[2]) ^ 2] + 1"
                toExpr = "8 + 2 sqrt[2] = 7 + 4 sqrt[2]"
                explanation {
                    key = EquationsExplanation.SimplifyEquation
                }
            }

            step {
                fromExpr = "8 + 2 sqrt[2] = 7 + 4 sqrt[2]"
                toExpr = "1 - 2 sqrt[2] = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.MoveEverythingToTheLeftAndSimplify
                }
            }

            step {
                fromExpr = "1 - 2 sqrt[2] = 0"
                toExpr = "Contradiction[1 - 2 sqrt[2] = 0]"
                explanation {
                    key = EquationsExplanation.ExtractFalsehoodFromFalseEquality
                }
            }
        }
    }

    @Test
    fun `test constant equation requiring simplification resulting in truth`() = testMethod {
        method = EquationsPlans.SolveConstantEquation
        inputExpr = "[(2 + sqrt[2]) ^ 2] + 1 - 3 = 4 (1 + sqrt[2])"

        check {
            fromExpr = "[(2 + sqrt[2]) ^ 2] + 1 - 3 = 4 (1 + sqrt[2])"
            toExpr = "Identity[4 + 4 sqrt[2] = 4 + 4 sqrt[2]]"
            explanation {
                key = EquationsExplanation.SolveConstantEquation
            }

            step {
                fromExpr = "[(2 + sqrt[2]) ^ 2] + 1 - 3 = 4 (1 + sqrt[2])"
                toExpr = "4 + 4 sqrt[2] = 4 + 4 sqrt[2]"
                explanation {
                    key = EquationsExplanation.SimplifyEquation
                }
            }

            step {
                fromExpr = "4 + 4 sqrt[2] = 4 + 4 sqrt[2]"
                toExpr = "Identity[4 + 4 sqrt[2] = 4 + 4 sqrt[2]]"
                explanation {
                    key = EquationsExplanation.ExtractTruthFromTrueEquality
                }
            }
        }
    }
}
