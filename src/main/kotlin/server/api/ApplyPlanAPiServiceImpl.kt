package server.api

import engine.context.emptyContext
import engine.expressions.RootPath
import engine.expressions.Subexpression
import methods.plans.simplifyArithmeticExpression
import org.springframework.stereotype.Service
import parser.parseExpression
import server.models.ApplyPlanPostRequest
import server.models.MappedExpression
import server.models.Metadata
import server.models.PathMapping
import server.models.Transformation

@Service
class ApplyPlanApiServiceImpl : ApplyPlanApiService {

    override fun applyPlanPost(applyPlanPostRequest: ApplyPlanPostRequest): Transformation {
        val expr = parseExpression(applyPlanPostRequest.input)
        val trans = simplifyArithmeticExpression.tryExecute(emptyContext, Subexpression(RootPath, expr))
        return TransformationModeller.modelTransformation(trans!!)
    }
}

object TransformationModeller {

    fun modelTransformation(trans: engine.steps.Transformation): Transformation {
        return Transformation(
            path = trans.fromExpr.path.toString(),
            fromExpr = trans.fromExpr.expr.toString(),
            toExpr = trans.toExpr.expr.toString(),
            pathMappings = modelPathMappings(trans.toExpr.mappings.pathMappings(RootPath)),
            explanation = trans.explanation?.let { modelMetadata(it) },
            skills = trans.skills.map { modelMetadata(it) },
            steps = trans.steps?.let { it.map { modelTransformation(it) } }
        )
    }

    fun modelPathMappings(mappings: Sequence<engine.expressions.PathMapping>): List<PathMapping> {
        return mappings.map { modelPathMapping(it) }.toList()
    }

    fun modelPathMapping(mapping: engine.expressions.PathMapping): PathMapping {
        return PathMapping(
            type = mapping.type.toString(),
            fromPaths = mapping.fromPaths.map { it.toString() },
            toPaths = mapping.toPaths.map { it.toString() }
        )
    }

    fun modelMetadata(metadata: engine.steps.metadata.Metadata): Metadata {
        return server.models.Metadata(
            key = metadata.key.toString(),
            params = metadata.mappedParams.map {
                MappedExpression(
                    expression = it.expr.toString(),
                    pathMappings = modelPathMappings(it.mappings.pathMappings(RootPath))
                )
            }
        )
    }
}
