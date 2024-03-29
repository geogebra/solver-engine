/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package server.api

import engine.expressions.RootPath
import engine.steps.metadata.metadata
import methods.KeyNameRegistry
import server.models.Alternative
import server.models.Format
import server.models.GmAction
import server.models.GmActionDragTo
import server.models.MappedExpression
import server.models.Metadata
import server.models.PathMapping
import server.models.Task
import server.models.Transformation

class TransformationModeller(val format: Format) {
    fun modelTransformation(trans: engine.steps.Transformation): Transformation {
        return Transformation(
            type = trans.type.toString(),
            tags = trans.tags?.map { it.toString() },
            path = trans.fromExpr.path.toString(),
            fromExpr = format.modelExpression(trans.fromExpr),
            toExpr = format.modelExpression(trans.toExpr),
            pathMappings = modelPathMappings(trans.toExpr.mergedPathMappings(trans.fromExpr.path!!)),
            explanation = trans.explanation?.let { modelMetadata(it) },
            formula = trans.formula?.let { modelMappedExpression(it) },
            skills = trans.skills?.map { modelMetadata(it) },
            gmAction = trans.gmAction?.let { modelGmAction(it) },
            steps = trans.steps?.let { steps -> steps.map { modelTransformation(it) } },
            tasks = trans.tasks?.let { tasks -> tasks.map { modelTask(it) } },
            alternatives = trans.alternatives?.let { alt -> alt.map { modelAlternative(it) } },
        )
    }

    private fun modelTask(task: engine.steps.Task): Task {
        return Task(
            taskId = task.taskId,
            startExpr = format.modelExpression(task.startExpr),
            pathMappings = modelPathMappings(task.startExpr.mergedPathMappings(RootPath(task.taskId))),
            explanation = task.explanation?.let { modelMetadata(it) },
            steps = if (task.steps.isEmpty()) null else task.steps.map { modelTransformation(it) },
            dependsOn = task.dependsOn.ifEmpty { null },
        )
    }

    private fun modelAlternative(alternative: engine.steps.Alternative): Alternative {
        return Alternative(
            strategy = alternative.strategy.name,
            explanation = modelMetadata(metadata(alternative.strategy.explanation)),
            steps = alternative.steps.map { modelTransformation(it) },
        )
    }

    private fun modelPathMappings(mappings: List<engine.expressions.PathMapping>): List<PathMapping> {
        return mappings.map { modelPathMapping(it) }
    }

    private fun modelPathMapping(mapping: engine.expressions.PathMapping): PathMapping {
        return PathMapping(
            type = mapping.type.toString(),
            fromPaths = mapping.fromPaths.map {
                if (it.second.toString().isNotEmpty()) {
                    "${it.first}:${it.second}"
                } else {
                    it.first.toString()
                }
            },
            toPaths = mapping.toPaths.map {
                if (it.second.toString().isNotEmpty()) {
                    "${it.first}:${it.second}"
                } else {
                    it.first.toString()
                }
            },
        )
    }

    private fun modelMetadata(metadata: engine.steps.metadata.Metadata): Metadata {
        return Metadata(
            key = KeyNameRegistry.getKeyName(metadata.key),
            params = metadata.mappedParams.map { modelMappedExpression(it) },
        )
    }

    private fun modelGmAction(gmAction: engine.steps.metadata.GmAction): GmAction {
        return GmAction(
            type = gmAction.type.name,
            expressions = gmAction.expressionsAsPathStrings(),
            dragTo = if (gmAction.dragTo == null) {
                null
            } else {
                GmActionDragTo(gmAction.dragToExpressionAsPathString(), gmAction.dragTo?.position?.name)
            },
            formulaId = gmAction.formulaId,
        )
    }

    private fun modelMappedExpression(expr: engine.expressions.Expression): MappedExpression {
        return MappedExpression(
            expression = format.modelExpression(expr),
            pathMappings = modelPathMappings(expr.mergedPathMappings(RootPath())),
        )
    }
}

internal fun Format.modelExpression(expr: engine.expressions.Expression): Any {
    return when (this) {
        Format.Latex -> expr.toLatexString()
        Format.Solver -> expr.toString()
        Format.Json2 -> expr.toJson()
    }
}
