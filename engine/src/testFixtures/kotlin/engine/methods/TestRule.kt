/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package engine.methods

import engine.context.Context
import engine.context.Setting
import engine.context.SettingValue
import engine.context.emptyContextWithLabels
import engine.expressions.Expression
import engine.expressions.RootOrigin
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
    context: Context = emptyContextWithLabels(),
    settings: Map<Setting, SettingValue> = mapOf(),
    testWithoutBrackets: Boolean = true,
) {
    val expression = parseExpression(inputExpr)
    return testRule(expression, rule, outputExpr, gmAction, context, settings, testWithoutBrackets)
}

fun testRule(
    inputExpr: Expression,
    rule: Method,
    outputExpr: String?,
    gmAction: SerializedGmAction? = null,
    context: Context = emptyContextWithLabels(),
    settings: Map<Setting, SettingValue> = mapOf(),
    testWithoutBrackets: Boolean = true,
) {
    val step = rule.tryExecute(context.addSettings(settings), inputExpr.withOrigin(RootOrigin()))
    if (outputExpr == null) {
        assertNull(step)
    } else {
        if (testWithoutBrackets) {
            assertEquals(parseExpression(outputExpr), step?.toExpr?.removeBrackets())
        } else {
            assertEquals(parseExpression(outputExpr), step?.toExpr)
        }
    }
    if (gmAction != null) {
        assertEquals(gmAction.type, step?.gmAction?.type?.name)
        assertEquals(gmAction.expressionPaths, step?.gmAction?.expressionsAsPathStrings())
        assertEquals(gmAction.dragToExpressionPath, step?.gmAction?.dragToExpressionAsPathString())
        assertEquals(gmAction.dragToPosition, step?.gmAction?.dragTo?.position?.name)
        assertEquals(gmAction.formulaId, step?.gmAction?.formulaId)
    } else {
        // Assert that the gmAction will probably not cause problems when being
        // serialized, like in PLUT-567
        step?.gmAction?.expressionsAsPathStrings()
        step?.gmAction?.dragToExpressionAsPathString()
    }
}

fun testRuleInX(inputExpr: String, rule: Method, outputExpr: String?) =
    testRule(inputExpr, rule, outputExpr, null, Context(solutionVariables = listOf("x")))

fun testRuleInXY(inputExpr: String, rule: Method, outputExpr: String?) =
    testRule(inputExpr, rule, outputExpr, null, Context(solutionVariables = listOf("x", "y")))
