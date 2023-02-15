package methods.inequalities

import engine.methods.testRuleInX
import methods.inequalities.InequalitiesRules.MultiplyByInverseCoefficientOfVariable
import org.junit.jupiter.api.Test

class InequalitiesRulesTest {

    @Test
    fun testMultiplyByInverseCoefficientOfVariable() {
        testRuleInX(
            "3x > 1",
            MultiplyByInverseCoefficientOfVariable,
            null,
        )
        testRuleInX(
            "[x / 5] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "[x / 5] * 5 > 1 * 5",
        )
        testRuleInX(
            "[3x / 2] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "[3x / 2] * [2 / 3] > 1 * [2 / 3]",
        )
        testRuleInX(
            "-[x / 5] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[x / 5]) * (-5) < 1 * (-5)",
        )
        testRuleInX(
            "-[3x / 2] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[3x / 2]) (-[2 / 3]) < 1 (-[2 / 3])",
        )
    }
}
