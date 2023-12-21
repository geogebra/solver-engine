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
import server.models.ExpressionGraphObject
import server.models.GraphAxis
import server.models.GraphRequest
import server.models.GraphResponse
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

        val exprs = extractGraphableExpressions(expr) ?: throw ExpressionNotGraphableException(graphRequest.input)

        val axisVariables = selectAxisVariables(expr.variables, context.solutionVariables, exprs)
            ?: throw ExpressionNotGraphableException(graphRequest.input)

        // Try to find a suitable window.  For now, go through each expression and if it is a function, try to adjust
        // its window
        fun solveForVariable(expr: Expression, variable: String) = EquationsPlans.SolveEquation.tryExecute(
            context.copy(solutionVariables = listOf(variable)),
            expr.withOrigin(RootOrigin()),
        )?.toExpr

        val window = bestWindowForExprs(exprs, axisVariables, ::solveForVariable)
            .bestSquareFit()
            .withPadding(paddingFactor = 0.05)

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
            objects = exprs.mapIndexed { i, expr ->
                ExpressionGraphObject(
                    type = ExpressionGraphObject.Type.Curve2D,
                    label = "C_${i + 1}",
                    expression = graphRequest.format.modelExpression(expr),
                )
            },
        )
    }

    companion object {
        val logger = LogManager.getLogger("graph")
    }
}
