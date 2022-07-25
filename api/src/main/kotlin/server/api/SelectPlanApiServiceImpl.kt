package server.api

import engine.context.Context
import engine.expressions.RootPath
import engine.expressions.Subexpression
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
        val context = Context(curriculum = applyPlanRequest.curriculum)

        for (entryData in methodRegistry.getPublicEntries()) {
            val transformation = entryData.implementation.tryExecute(context, Subexpression(expr, null, RootPath))
            if (transformation != null) {
                selections.add(
                    PlanSelection(
                        modeller.modelTransformation(transformation),
                        PlanSelectionMetadata(entryData.methodId.key)
                    )
                )
            }
        }
        return selections
    }
}
