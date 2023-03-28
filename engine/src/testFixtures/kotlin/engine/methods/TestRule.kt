package engine.methods

import engine.context.Context
import engine.context.emptyContext
import engine.expressions.Root
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertNull

data class SerializedGmAction(
    val type: String,
    /** Expression paths with path modifier appended. */
    val expressionPaths: List<String> = listOf(),
    /** Expression path with the dragTo path modifier appended. */
    val dragToExpressionPath: String? = null,
    val dragToPosition: String? = if (dragToExpressionPath == null) null else "Onto",
    val formulaId: String? = null,
) {
    constructor(
        type: String,
        expressionPath: String,
        dragToExpressionPath: String? = null,
        dragToPosition: String? = if (dragToExpressionPath == null) null else "Onto",
        formulaId: String? = null,
    ) : this(type, listOf(expressionPath), dragToExpressionPath, dragToPosition, formulaId)
}

fun testRule(
    inputExpr: String,
    rule: Method,
    outputExpr: String?,
    gmAction: SerializedGmAction? = null,
    context: Context = emptyContext,
) {
    val expression = parseExpression(inputExpr)
    val step = rule.tryExecute(context, expression.withOrigin(Root()))
    if (outputExpr == null) {
        assertNull(step)
    } else {
        assertEquals(parseExpression(outputExpr), step?.toExpr?.removeBrackets())
    }
    if (gmAction != null) {
        assertEquals(gmAction.type, step?.gmAction?.type?.name)
        assertEquals(gmAction.expressionPaths, step?.gmAction?.expressionsAsPathStrings())
        assertEquals(gmAction.dragToExpressionPath, step?.gmAction?.dragToExpressionAsPathString())
        assertEquals(gmAction.dragToPosition, step?.gmAction?.dragTo?.position?.name)
        assertEquals(gmAction.formulaId, step?.gmAction?.formulaId)
    }
}

fun testRuleInX(inputExpr: String, rule: Method, outputExpr: String?) =
    testRule(inputExpr, rule, outputExpr, null, Context(solutionVariables = listOf("x")))
