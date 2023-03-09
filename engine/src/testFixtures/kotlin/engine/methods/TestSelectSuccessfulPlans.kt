package engine.methods

import engine.context.Context
import engine.context.emptyContext
import parser.parseExpression
import kotlin.test.assertEquals

fun testSelectSuccessfulPlans(
    inputExpr: String,
    expectedMethodSelections: Set<Method>,
    methodRegistry: MethodRegistry,
    context: Context = emptyContext,
) {
    val expr = parseExpression(inputExpr)
    val actualSelections = methodRegistry.selectSuccessfulPlansMethodIdAndTransformation(expr, context)
    val actualMethodSelections = actualSelections.map {
        methodRegistry.getMethodByName(it.first.toString())
    }.toSet()
    assertEquals(expectedMethodSelections, actualMethodSelections)
}
