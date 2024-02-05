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

package server.api

import engine.expressions.Expression
import engine.expressions.RootOrigin
import engine.graphing.bestWindowForExprs
import engine.graphing.extractGraphableExpressions
import engine.graphing.selectAxisVariables
import methods.equations.EquationsPlans
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import parser.parseExpression
import server.models.Cartesian2DSystem
import server.models.GraphAxis
import server.models.GraphRequest
import server.models.GraphResponse
import server.models.GraphResponseObjectsInner
import java.math.BigDecimal

@Service
class GraphApiServiceImpl : GraphApiService {
    override fun createGraph(graphRequest: GraphRequest): GraphResponse {
        val expr = try {
            parseExpression(graphRequest.input)
        } catch (e: ParseCancellationException) {
            throw InvalidExpressionException(graphRequest.input, e)
        }
        val context = getContext(graphRequest.context, expr.variables, logger)

        val (extractedExprs, intersections) = extractGraphableExpressions(expr)
            ?: throw ExpressionNotGraphableException(graphRequest.input)

        val axisVariables = selectAxisVariables(expr.variables, context.solutionVariables, extractedExprs)
            ?: throw ExpressionNotGraphableException(graphRequest.input)

        // Try to find a suitable window.  For now, go through each expression and if it is a function, try to adjust
        // its window
        fun solveForVariable(expr: Expression, variable: String) =
            EquationsPlans.SolveEquation.tryExecute(
                context.copy(solutionVariables = listOf(variable)),
                expr.withOrigin(RootOrigin()),
            )?.toExpr

        val window = bestWindowForExprs(extractedExprs, axisVariables, ::solveForVariable)
            .bestSquareFit()
            .withPadding(paddingFactor = 0.05)

        val exprObjects = extractedExprs.mapIndexed { i, expr ->
            GraphResponseObjectsInner(
                type = GraphResponseObjectsInner.Type.Curve2D,
                label = "C_${i + 1}",
                expression = graphRequest.format.modelExpression(expr),
            )
        }

        val intersectionObjects = intersections.map { intersection ->
            GraphResponseObjectsInner(
                type = GraphResponseObjectsInner.Type.Intersection,
                objectLabels = intersection.objectIndexes.map { i -> "C_${i + 1}" },
                projectOntoHorizontalAxis = intersection.projectOntoHorizontalAxis,
                projectOntoVerticalAxis = intersection.projectOntoVerticalAxis,
            )
        }

        return GraphResponse(
            coordinateSystem = Cartesian2DSystem(
                type = Cartesian2DSystem.Type.Cartesian2D,
                horizontalAxis = GraphAxis(
                    variable = axisVariables.horizontal,
                    label = axisVariables.horizontal,
                    minValue = BigDecimal(window.x0),
                    maxValue = BigDecimal(window.x1),
                ),
                verticalAxis = GraphAxis(
                    variable = axisVariables.vertical,
                    label = axisVariables.vertical,
                    minValue = BigDecimal(window.y0),
                    maxValue = BigDecimal(window.y1),
                ),
            ),
            objects = exprObjects + intersectionObjects,
        )
    }

    companion object {
        val logger = LogManager.getLogger("graph")
    }
}
