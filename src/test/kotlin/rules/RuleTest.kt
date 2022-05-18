package rules

import context.emptyContext
import expressions.RootPath
import expressions.Subexpression
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertNull

data class RuleTestCase(val inputExpr: String, val rule: Rule, val outputExpr: String?) {
    fun assert() {
        val expression = parseExpression(inputExpr)
        val step = rule.tryExecute(emptyContext, Subexpression(RootPath, expression))
        if (outputExpr == null) {
            assertNull(step)
        } else {
            assertEquals(parseExpression(outputExpr), step?.toExpr, inputExpr)
            step?.prettyPrint()
        }
    }
}

abstract class RuleTest {

    @ParameterizedTest
    @MethodSource("testCaseProvider")
    fun testRule(testCase: RuleTestCase) {
        testCase.assert()
    }
}