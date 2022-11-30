package engine.methods

import engine.context.emptyContext
import engine.expressions.Root
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertNull

fun testRule(inputExpr: String, rule: Method, outputExpr: String?) {
    val expression = parseExpression(inputExpr)
    val step = rule.tryExecute(emptyContext, expression.withOrigin(Root()))
    if (outputExpr == null) {
        assertNull(step)
    } else {
        assertEquals(parseExpression(outputExpr), step?.toExpr?.removeBrackets())
    }
}
