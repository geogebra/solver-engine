package engine.methods

import engine.context.Context
import engine.context.emptyContext
import engine.expressions.Root
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertNull

fun testRule(inputExpr: String, rule: Method, outputExpr: String?, context: Context = emptyContext) {
    val expression = parseExpression(inputExpr)
    val step = rule.tryExecute(context, expression.withOrigin(Root()))
    if (outputExpr == null) {
        assertNull(step)
    } else {
        assertEquals(parseExpression(outputExpr), step?.toExpr?.removeBrackets())
    }
}

fun testRuleInX(inputExpr: String, rule: Method, outputExpr: String?) =
    testRule(inputExpr, rule, outputExpr, Context(solutionVariable = "x"))
