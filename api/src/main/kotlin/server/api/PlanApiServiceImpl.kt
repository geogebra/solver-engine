package server.api

import engine.expressions.RootOrigin
import methods.methodRegistry
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.springframework.stereotype.Service
import parser.parseExpression
import server.models.SolveRequest
import server.models.Transformation
import java.util.UUID
import java.util.logging.Level
import kotlin.time.measureTimedValue

@Service
class PlanApiServiceImpl : PlansApiService {

    override fun applyPlan(planId: String, solveRequest: SolveRequest): Transformation {
        val plan =
            methodRegistry.getMethodByName(planId) ?: throw NotFoundException("plan not found")
        val expr = try {
            parseExpression(solveRequest.input)
        } catch (e: ParseCancellationException) {
            throw InvalidExpressionException(solveRequest.input, e)
        }
        val context = getContext(solveRequest.context, expr.variables, logger)
        try {
            ThreadContext.put("traceId", UUID.randomUUID().toString())
            context.log(Level.INFO) {
                mapOf(
                    "type" to "requestData",
                    "requestType" to "applyPlan",
                    "requestParams" to mapOf(
                        "input" to solveRequest.input,
                        "planId" to planId,
                    ),
                )
            }
            val (trans, duration) = measureTimedValue {
                plan.tryExecute(context, expr.withOrigin(RootOrigin()))
            }
            context.log(Level.INFO) {
                mapOf(
                    "type" to "resultData",
                    "methodId" to planId,
                    "result" to (trans?.toExpr.toString()),
                )
            }
            context.log(Level.INFO) {
                mapOf(
                    "type" to "timing",
                    "ms" to duration.inWholeMilliseconds,
                )
            }
            if (trans != null) {
                val modeller = TransformationModeller(format = solveRequest.format)
                return modeller.modelTransformation(trans)
            } else {
                throw PlanNotApplicableException(planId)
            }
        } finally {
            ThreadContext.clearMap()
        }
    }

    override fun getPlan(planId: String): Any {
        TODO()
    }

    override fun listPlans(): List<String> {
        return methodRegistry.listedEntries.map { it.methodId.toString() }.toList()
    }

    companion object {
        private val logger = LogManager.getLogger("engine")
    }
}
