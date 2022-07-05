package server.api

import engine.context.emptyContext
import engine.expressions.RootPath
import engine.expressions.Subexpression
import methods.plans.planRegistry
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import parser.parseExpression
import server.models.ApplyPlanRequest
import server.models.Transformation

@Service
class PlanApiServiceImpl : PlansApiService {
    override fun applyPlan(planId: String, applyPlanRequest: ApplyPlanRequest): Transformation {
        val plan = planRegistry.getPlan(planId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found")
        val expr = try {
            parseExpression(applyPlanRequest.input)
        } catch (e: ParseCancellationException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid expression", e)
        }
        val trans = plan.tryExecute(emptyContext, Subexpression(RootPath, expr))
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan cannot be applied to expression")
        val modeller = TransformationModeller(format = applyPlanRequest.format)
        return modeller.modelTransformation(trans)
    }

    override fun getPlan(planId: String): Any {
        TODO()
    }

    override fun listPlans(): List<String> {
        return planRegistry.allPlans().map { it.planId.toString() }.toList()
    }
}
