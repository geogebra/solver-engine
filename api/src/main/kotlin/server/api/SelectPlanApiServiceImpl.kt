package server.api

import engine.expressions.Subexpression
import methods.methodRegistry
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.springframework.stereotype.Service
import parser.parseExpression
import server.models.ApplyPlanRequest
import server.models.PlanSelection
import server.models.PlanSelectionMetadata
import java.util.logging.Level

@Suppress("TooGenericExceptionCaught", "SwallowedException")
@Service
class SelectPlanApiServiceImpl : SelectPlansApiService {
    override fun selectPlans(applyPlanRequest: ApplyPlanRequest): List<PlanSelection> {
        val expr = try {
            parseExpression(applyPlanRequest.input)
        } catch (e: ParseCancellationException) {
            throw InvalidExpressionException(applyPlanRequest.input, e)
        }
        val modeller = TransformationModeller(applyPlanRequest.format)
        val selections = mutableListOf<PlanSelection>()
        val context = getContext(applyPlanRequest.context)

        for (entryData in methodRegistry.getPublicEntries()) {
            val transformation = try {
                entryData.implementation.tryExecute(context, Subexpression(expr))
            } catch (e: Exception) {
                context.log(Level.SEVERE, "Exception caught: ${e.stackTraceToString()}")
                null
            }
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
