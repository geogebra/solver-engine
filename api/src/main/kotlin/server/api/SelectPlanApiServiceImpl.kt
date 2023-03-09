package server.api

import methods.methodRegistry
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.springframework.stereotype.Service
import parser.parseExpression
import server.models.PlanSelection
import server.models.PlanSelectionMetadata
import server.models.SolveRequest

@Service
class SelectPlanApiServiceImpl : SelectPlansApiService {
    override fun selectPlans(solveRequest: SolveRequest): List<PlanSelection> {
        val expr = try {
            parseExpression(solveRequest.input)
        } catch (e: ParseCancellationException) {
            throw InvalidExpressionException(solveRequest.input, e)
        }
        val modeller = TransformationModeller(solveRequest.format)
        val context = getContext(solveRequest.context, expr.variables.firstOrNull())

        val selectedPlans = methodRegistry.selectSuccessfulPlansMethodIdAndTransformation(expr, context)
            .map { (methodId, transformation) ->
                PlanSelection(
                    modeller.modelTransformation(transformation),
                    PlanSelectionMetadata(methodId.toString()),
                )
            }

        return selectedPlans
    }
}
