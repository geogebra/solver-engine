package server.api

import engine.expressions.Root
import engine.methods.MethodId
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

        val successfulPlansIds = mutableSetOf<MethodId>()

        for (entryData in methodRegistry.publicEntries) {
            if (methodRegistry.getMoreSpecificMethods(entryData.methodId).any { it in successfulPlansIds }) {
                successfulPlansIds.add(entryData.methodId)
                context.log(Level.FINE, "Skipping plan ID: ${entryData.methodId}")
                continue
            }
            val transformation = try {
                entryData.implementation.tryExecute(context, expr.withOrigin(Root()))
            } catch (e: Exception) {
                context.log(Level.FINE, "Exception caught: ${e.stackTraceToString()}")
                null
            }
            if (transformation != null) {
                context.log(Level.FINE, "Success for plan ID: ${entryData.methodId}")
                successfulPlansIds.add(entryData.methodId)

                selections.add(
                    PlanSelection(
                        modeller.modelTransformation(transformation),
                        PlanSelectionMetadata(entryData.methodId.toString())
                    )
                )
            } else {
                context.log(Level.FINE, "Failure for plan ID: ${entryData.methodId}")
            }
        }
        return selections
    }
}
