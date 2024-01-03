package server.api

import methods.methodRegistry
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.springframework.stereotype.Service
import parser.parseExpression
import server.models.PlanSelection
import server.models.PlanSelectionMetadata
import server.models.SolveRequest
import java.util.UUID
import java.util.logging.Level
import kotlin.time.measureTimedValue

@Service
class SelectPlanApiServiceImpl : SelectPlansApiService {
    override fun selectPlans(solveRequest: SolveRequest): List<PlanSelection> {
        val expr = try {
            parseExpression(solveRequest.input)
        } catch (e: ParseCancellationException) {
            throw InvalidExpressionException(solveRequest.input, e)
        }
        val modeller = TransformationModeller(solveRequest.format)
        val context = getContext(solveRequest.context, expr.variables, logger)

        try {
            ThreadContext.put("traceId", UUID.randomUUID().toString())
            context.log(Level.INFO) {
                mapOf(
                    "type" to "requestData",
                    "requestType" to "selectPlans",
                    "requestParams" to mapOf("input" to solveRequest.input),
                )
            }
            val (selectedPlans, duration) = measureTimedValue {
                methodRegistry.selectSuccessfulPlansMethodIdAndTransformation(expr, context)
            }
            for ((methodId, transformation) in selectedPlans) {
                context.log(Level.INFO) {
                    mapOf(
                        "type" to "resultData",
                        "methodId" to methodId.toString(),
                        "result" to transformation.toExpr.toString(),
                    )
                }
            }
            context.log(Level.INFO) {
                mapOf(
                    "type" to "timing",
                    "ms" to duration.inWholeMilliseconds,
                )
            }
            return selectedPlans.map { (methodId, transformation) ->
                PlanSelection(
                    modeller.modelTransformation(transformation),
                    PlanSelectionMetadata(methodId.toString()),
                )
            }
        } finally {
            ThreadContext.clearMap()
        }
    }

    companion object {
        private val logger = LogManager.getLogger("engine")
    }
}
