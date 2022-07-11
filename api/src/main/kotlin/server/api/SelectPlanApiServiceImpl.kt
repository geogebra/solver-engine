package server.api

import engine.context.emptyContext
import engine.expressions.RootPath
import engine.expressions.Subexpression
import engine.methods.PlanId
import methods.methodRegistry
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import parser.parseExpression
import server.models.ApplyPlanRequest
import server.models.PlanSelection
import server.models.PlanSelectionMetadata

@Service
class SelectPlanApiServiceImpl : SelectPlansApiService {
    override fun selectPlans(applyPlanRequest: ApplyPlanRequest): List<PlanSelection> {
        val expr = try {
            parseExpression(applyPlanRequest.input)
        } catch (e: ParseCancellationException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid expression", e)
        }
        val modeller = TransformationModeller(applyPlanRequest.format)
        val selections = mutableListOf<PlanSelection>()
        for ((methodData, plan) in methodRegistry.getPublicEntries()) {
            val transformation = plan?.tryExecute(emptyContext, Subexpression(RootPath, expr))
            if (transformation != null) {
                selections.add(
                    PlanSelection(
                        modeller.modelTransformation(transformation),
                        PlanSelectionMetadata(methodData.methodId.key)
                    )
                )
            }
        }
        return selections
    }

    companion object {
        val planIds = listOf(
            PlanId.SimplifyArithmeticExpression,
            PlanId.CombineFractionsInExpression,
            PlanId.AddMixedNumbers
        )
    }
}
