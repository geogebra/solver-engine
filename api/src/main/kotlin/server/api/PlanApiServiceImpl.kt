package server.api

import engine.context.Context
import engine.context.emptyContext
import engine.expressions.Subexpression
import methods.methodRegistry
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.springframework.stereotype.Service
import parser.parseExpression
import server.models.ApplyPlanRequest
import server.models.Transformation

@Service
class PlanApiServiceImpl : PlansApiService {
    override fun applyPlan(planId: String, applyPlanRequest: ApplyPlanRequest): Transformation {
        val plan =
            methodRegistry.getMethodByName(planId) ?: throw NotFoundException("plan not found")
        val expr = try {
            parseExpression(applyPlanRequest.input)
        } catch (e: ParseCancellationException) {
            throw InvalidExpressionException(applyPlanRequest.input, e)
        }
        val context = getContext(applyPlanRequest.context)
        val trans = plan.tryExecute(context, Subexpression(expr))
            ?: throw PlanNotApplicableException(planId)
        val modeller = TransformationModeller(format = applyPlanRequest.format)
        return modeller.modelTransformation(trans)
    }

    override fun getPlan(planId: String): Any {
        TODO()
    }

    override fun listPlans(): List<String> {
        return methodRegistry.getPublicEntries().map { it.methodId.key }.toList()
    }
}

fun getContext(apiCtx: server.models.Context?) = apiCtx?.let {
    Context(
        curriculum = apiCtx.curriculum,
        precision = apiCtx.precision?.toInt()
    )
} ?: emptyContext
