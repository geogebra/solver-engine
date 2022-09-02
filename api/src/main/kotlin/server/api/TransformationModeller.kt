package server.api

import engine.expressions.RootPath
import server.models.ApplyPlanRequest
import server.models.MappedExpression
import server.models.Metadata
import server.models.PathMapping
import server.models.Transformation

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
            key = metadata.key.keyName,
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
