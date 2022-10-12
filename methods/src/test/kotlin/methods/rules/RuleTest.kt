package methods.rules

import engine.context.emptyContext
import engine.expressions.Subexpression
import engine.methods.Method
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertNull

fun testRule(inputExpr: String, rule: Method, outputExpr: String?) {
    val expression = parseExpression(inputExpr)
    val step = rule.tryExecute(emptyContext, Subexpression(expression))
    if (outputExpr == null) {
        assertNull(step)
    } else {
        assertEquals(parseExpression(outputExpr), step?.toExpr?.expr)
    }
}
