package methods.expand

import engine.context.BooleanSetting
import engine.context.Setting
import engine.methods.testMethod
import engine.methods.testRule
import methods.expand.ExpandRules.ApplyFoilMethod
import methods.expand.ExpandRules.DistributeMultiplicationOverSum
import methods.expand.ExpandRules.DistributeNegativeOverBracket
import methods.expand.ExpandRules.ExpandBinomialCubedUsingIdentity
import methods.expand.ExpandRules.ExpandBinomialSquaredUsingIdentity
import methods.expand.ExpandRules.ExpandDoubleBrackets
import methods.expand.ExpandRules.ExpandProductOfSumAndDifference
import methods.expand.ExpandRules.ExpandTrinomialSquaredUsingIdentity
import org.junit.jupiter.api.Test

class ExpandRulesTest {

    @Test
    fun testExpandBinomialSquaredUsingIdentity() {
        testRule(
            "[(a + b) ^ 2]",
            ExpandBinomialSquaredUsingIdentity,
            "[a ^ 2] + 2 * a * b + [b ^ 2]",
        )
        testRule(
            "[(sqrt[2] + 1) ^ 2]",
            ExpandBinomialSquaredUsingIdentity,
            "[(sqrt[2]) ^ 2] + 2 * sqrt[2] * 1 + [1 ^ 2]",
        )
        testRule(
            "[(x - y) ^ 2]",
            ExpandBinomialSquaredUsingIdentity,
            "[x ^ 2] + 2 * x * (-y) + [(-y) ^ 2]",
        )
        testRule(
            "[(2x - 3)^2]",
            ExpandBinomialSquaredUsingIdentity,
            "[(2 x) ^ 2] + 2 * <. 2 x .> * (-3) + [(-3) ^ 2]",
        )
    }

    @Test
    fun testExpandBinomialCubedUsingIdentity() {
        testRule(
            "[(a + b) ^ 3]",
            ExpandBinomialCubedUsingIdentity,
            "[a^3] + 3 * [a^2] * b + 3 * a * [b^2] + [b^3]",
        )
        testRule(
            "[(a - b)^3]",
            ExpandBinomialCubedUsingIdentity,
            "[a^3] + 3 * [a^2] * (-b) + 3 * a * [(-b)^2] + [(-b)^3]",
        )
        testRule(
            "[(2x - 4) ^ 3]",
            ExpandBinomialCubedUsingIdentity,
            "[(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-4) + 3 * <. 2 x .> * [(-4) ^ 2] + [(-4) ^ 3]",
        )
        testRule(
            "[(-1 + 2x) ^ 3]",
            ExpandBinomialCubedUsingIdentity,
            "[(-1) ^ 3] + 3 * [(-1) ^ 2] * <. 2 x .> + 3 * (-1) * [(2 x) ^ 2] + [(2 x) ^ 3]",
        )
    }

    @Test
    fun testIdentityProductOfSumAndDifference() {
        testRule(
            "(1 + sqrt[2]) (1 - sqrt[2])",
            ExpandProductOfSumAndDifference,
            "[1^2] - [(sqrt[2]) ^ 2]",
        )
        testRule(
            "(1 - sqrt[2]) (1 + sqrt[2])",
            ExpandProductOfSumAndDifference,
            "[1^2] - [(sqrt[2]) ^ 2]",
        )
        testRule(
            "(sqrt[2] - 1) (1 + sqrt[2])",
            ExpandProductOfSumAndDifference,
            "[(sqrt[2]) ^ 2] - [1^2]",
        )
        testRule(
            "(-1 + sqrt[2]) (sqrt[2] + 1)",
            ExpandProductOfSumAndDifference,
            "[(sqrt[2]) ^ 2] - [1^2]",
        )
        testRule(
            "(1 + sqrt[2])*(1 - sqrt[2])",
            ExpandProductOfSumAndDifference,
            "[1 ^ 2] - [(sqrt[2]) ^ 2]",
        )
        testRule(
            "(2x - 3) (2x + 3)",
            ExpandProductOfSumAndDifference,
            "[(2x)^2] - [3^2]",
        )
    }

    @Test
    fun testExpandProductOfSingleTermAndSum() {
        testMethod {
            method = DistributeMultiplicationOverSum
            inputExpr = "sqrt[2] * (3 + sqrt[4])"
            check {
                toExpr = "sqrt[2] * 3 + sqrt[2] * sqrt[4]"

                distribute {
                    fromPaths("./0")
                    toPaths("./0/0", "./1/0")
                }
                move("./1/0", "./0/1")
                move("./1/1", "./1/1")
            }
        }
        testRule(
            "sqrt[2] * (3 - sqrt[4])",
            DistributeMultiplicationOverSum,
            "sqrt[2] * 3 + sqrt[2] * (-sqrt[4])",
        )
        testRule(
            "(3 - sqrt[4]) * sqrt[2]",
            DistributeMultiplicationOverSum,
            "3 * sqrt[2] + (-sqrt[4]) * sqrt[2]",
        )
        testRule(
            "sqrt[2] * (3 + sqrt[4] + sqrt[5])",
            DistributeMultiplicationOverSum,
            "sqrt[2] * 3 + sqrt[2] * sqrt[4] + sqrt[2] * sqrt[5]",
        )
        testRule(
            "-2 * (3 + sqrt[5])",
            DistributeMultiplicationOverSum,
            "-2 * 3 - 2 * sqrt[5]",
        )
        testRule(
            "-sqrt[2] * (3 + sqrt[5])",
            DistributeMultiplicationOverSum,
            "-sqrt[2] * 3 - sqrt[2] * sqrt[5]",
        )
        testRule(
            "-2 * (-3 - sqrt[5])",
            DistributeMultiplicationOverSum,
            "-2 * (-3) - 2 * (-sqrt[5])",
        )
        testRule(
            "2 (4x - 3)",
            DistributeMultiplicationOverSum,
            "2 * <. 4x .> + 2 * (-3)",
        )
        testRule(
            "2*sqrt[2]*(1 + sqrt[3])",
            DistributeMultiplicationOverSum,
            "<. 2 sqrt[2] .> * 1 + <. 2 sqrt[2] .> * sqrt[3]",
        )
        testRule(
            "3 sqrt[2] * [x^2] * (2x - 7)",
            DistributeMultiplicationOverSum,
            "<. 3 sqrt[2] * [x ^ 2] .> * <. 2 x .> + <. 3 sqrt[2] * [x ^ 2] .> * (-7)",
        )
        testRule(
            "2*(1 + sqrt[3])*sqrt[2]",
            DistributeMultiplicationOverSum,
            null,
        )
        testRule(
            "x*(1 + sqrt[3])",
            DistributeMultiplicationOverSum,
            null,
        )
    }

    @Test
    fun `test ExpandProductOfSingleTermAndSum with CopySumSignsWhenDistributing`() {
        val settings = mapOf(Setting.CopySumSignsWhenDistributing to BooleanSetting.True)
        testRule(
            "5(x - 2)",
            DistributeMultiplicationOverSum,
            "5*x - 5*2",
            settings = settings,
        )
        testRule(
            "5(-2x + 2)",
            DistributeMultiplicationOverSum,
            "-5*<. 2x .> + 5*2",
            settings = settings,
        )
        testRule(
            "(-5)(-2x - 2 + (-4))",
            DistributeMultiplicationOverSum,
            "-(-5)*<. 2x .> - (-5)*2 + (-5)*(-4)",
            settings = settings,
        )
        testRule(
            "-5(3x - 2)",
            DistributeMultiplicationOverSum,
            "(-5)*<. 3x .> - (-5)*2",
            settings = settings,
        )
    }

    @Test
    fun testApplyFoilMethod() {
        testRule(
            "(1 + sqrt[2]) * (1 + sqrt[2])",
            ApplyFoilMethod,
            "1*1 + 1*sqrt[2] + sqrt[2]*1 + sqrt[2]*sqrt[2]",
        )
        testRule(
            "(4x - 3) * (4x - 3)",
            ApplyFoilMethod,
            "<. 4 x .> * <. 4 x .> + <. 4 x .> * (-3) + (-3) * <. 4 x .> + (-3) * (-3)",
        )
        testRule(
            "(4x - 5[x^3]) * (2[x^2] - 3x)",
            ApplyFoilMethod,
            "<. 4 x .> * <. 2 [x ^ 2] .> + <. 4 x .> * (-3 x) " +
                "+ (-5 [x ^ 3]) * <. 2 [x ^ 2] .> + (-5 [x ^ 3]) * (-3 x)",
        )
        testRule(
            "(2x - 3) * (3x + 3)",
            ApplyFoilMethod,
            "<. 2 x .> * <. 3 x .> + <. 2 x .> * 3 + (-3) * <. 3 x .> + (-3) * 3",
        )
        testRule(
            "(x + [x^2]) (5x + [x^2])",
            ApplyFoilMethod,
            "x * <. 5 x .> + x * [x ^ 2] + [x ^ 2] * <. 5 x .> + [x ^ 2] * [x ^ 2]",
        )
        testRule(
            "(2x - 3) (2x - 3)",
            ApplyFoilMethod,
            "<. 2 x .> * <. 2 x .> + <. 2 x .> * (-3) + (-3) * <. 2 x .> + (-3) * (-3)",
        )
        testMethod {
            method = ApplyFoilMethod
            inputExpr = "(x + 2)(x + 3)"

            check {
                toExpr = "x*x + x*3 + 2*x + 2*3"

                distribute {
                    fromPaths("./0/0")
                    toPaths("./0/0", "./1/0")
                }
                distribute {
                    fromPaths("./0/1")
                    toPaths("./2/0", "./3/0")
                }
                distribute {
                    fromPaths("./1/0")
                    toPaths("./0/1", "./2/1")
                }
                distribute {
                    fromPaths("./1/1")
                    toPaths("./1/1", "./3/1")
                }
            }
        }
    }

    @Test
    fun testExpandDoubleBrackets() {
        testMethod {
            method = ExpandDoubleBrackets
            inputExpr = "([x^2] + 5x - 2) * (3x - 5)"

            check {
                toExpr = "[x ^ 2] * <. 3 x .> + [x ^ 2] * (-5) + <. 5 x .> * <. 3 x .> " +
                    "+ <. 5 x .> * (-5) + (-2) * <. 3 x .> + (-2) * (-5)"

                distribute {
                    fromPaths("./0/0")
                    toPaths("./0/0", "./1/0")
                }
                distribute {
                    fromPaths("./0/1")
                    toPaths("./2/0", "./3/0")
                }
                distribute {
                    fromPaths("./0/2")
                    toPaths("./4/0", "./5/0")
                }
                distribute {
                    fromPaths("./1/0")
                    toPaths("./0/1", "./2/1", "./4/1")
                }
                distribute {
                    fromPaths("./1/1")
                    toPaths("./1/1", "./3/1", "./5/1")
                }
            }
        }
    }

    @Test
    fun testExpandTrinomialSquaredUsingIdentity() {
        testRule(
            "[(1 + sqrt[2] + sqrt[3])^2]",
            ExpandTrinomialSquaredUsingIdentity,
            "[1 ^ 2] + [(sqrt[2]) ^ 2] + [(sqrt[3]) ^ 2] + " +
                "2 * 1 * sqrt[2] + 2 * sqrt[2] * sqrt[3] + 2 * sqrt[3] * 1",
        )
        testRule(
            "[(1 - sqrt[2] - sqrt[3])^2]",
            ExpandTrinomialSquaredUsingIdentity,
            "[1 ^ 2] + [(-sqrt[2]) ^ 2] + [(-sqrt[3]) ^ 2] + " +
                "2 * 1 * (-sqrt[2]) + 2 * (-sqrt[2]) * (-sqrt[3]) + 2 * (-sqrt[3]) * 1",
        )
        testRule(
            "[(1 - x - y)^2]",
            ExpandTrinomialSquaredUsingIdentity,
            "[1 ^ 2] + [(-x) ^ 2] + [(-y) ^ 2] + 2 * 1 * (-x) + 2 * (-x) * (-y) + 2 * (-y) * 1",
        )
    }

    @Test
    fun testDistributeNegativeOverBracket() {
        testRule(
            "-(sqrt[2]+7)",
            DistributeNegativeOverBracket,
            "-sqrt[2] - 7",
        )
        testRule(
            "5 - (sqrt[2] + 7)",
            DistributeNegativeOverBracket,
            "5 - sqrt[2] - 7",
        )
        testRule(
            "5 - (sqrt[2] - 7)",
            DistributeNegativeOverBracket,
            "5 - sqrt[2] + 7",
        )
        testRule(
            "5 - (-sqrt[2] + 7)",
            DistributeNegativeOverBracket,
            "5 + sqrt[2] - 7",
        )
        testRule(
            "5 - (-sqrt[2] - 7)",
            DistributeNegativeOverBracket,
            "5 + sqrt[2] + 7",
        )
        testRule(
            "-(-a - 2)",
            DistributeNegativeOverBracket,
            "a + 2",
        )
        testRule(
            "sqrt[2] - (-a + 2)",
            DistributeNegativeOverBracket,
            "sqrt[2] + a - 2",
        )
        testRule(
            "sqrt[2] - (a - 2)",
            DistributeNegativeOverBracket,
            "sqrt[2] - a + 2",
        )
        testRule(
            "sqrt[2] - (-5a - 7)",
            DistributeNegativeOverBracket,
            "sqrt[2] + 5a + 7",
        )
    }
}
