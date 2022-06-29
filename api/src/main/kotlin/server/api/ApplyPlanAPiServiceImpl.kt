package server.api

import engine.context.emptyContext
import engine.expressions.RootPath
import engine.expressions.Subexpression
import engine.plans.PlanRegistry
import methods.plans.registerPlans
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import parser.parseExpression
import server.models.ApplyPlanRequest
import server.models.MappedExpression
import server.models.Metadata
import server.models.PathMapping
import server.models.Transformation

@Service
class PlanApiServiceImpl : PlansApiService {
    override fun applyPlan(planId: String, applyPlanRequest: ApplyPlanRequest): Transformation {
        val plan = PlanRegistry.getPlan(planId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found")
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
        return PlanRegistry.allPlans().map { it.planId.toString() }.toList()
    }

    companion object {
        init {
            registerPlans()
        }
    }
}

data class TransformationModeller(val format: ApplyPlanRequest.Format) {

    fun modelTransformation(trans: engine.steps.Transformation): Transformation {
        return Transformation(
            path = trans.fromExpr.path.toString(),
            fromExpr = modelExpression(trans.fromExpr.expr),
            toExpr = modelExpression(trans.toExpr.expr),
            pathMappings = modelPathMappings(trans.toExpr.mappings.pathMappings(RootPath)),
            explanation = trans.explanation?.let { modelMetadata(it) },
            skills = trans.skills.map { modelMetadata(it) },
            steps = trans.steps?.let { step -> step.map { modelTransformation(it) } }
        )
    }

    private fun modelPathMappings(mappings: Sequence<engine.expressions.PathMapping>): List<PathMapping> {
        return mappings.map { modelPathMapping(it) }.toList()
    }

    private fun modelPathMapping(mapping: engine.expressions.PathMapping): PathMapping {
        return PathMapping(
            type = mapping.type.toString(),
            fromPaths = mapping.fromPaths.map { it.toString() },
            toPaths = mapping.toPaths.map { it.toString() }
        )
    }

    private fun modelMetadata(metadata: engine.steps.metadata.Metadata): Metadata {
        return Metadata(
            key = metadata.key.toString(),
            params = metadata.mappedParams.map {
                MappedExpression(
                    expression = modelExpression(it.expr),
                    pathMappings = modelPathMappings(it.mappings.pathMappings(RootPath))
                )
            }
        )
    }

    private fun modelExpression(expr: engine.expressions.Expression): String {
        return when (format) {
            ApplyPlanRequest.Format.Latex -> expr.toLatexString()
            ApplyPlanRequest.Format.Solver -> expr.toString()
        }
    }
}
