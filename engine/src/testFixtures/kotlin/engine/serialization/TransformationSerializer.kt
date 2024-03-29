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

package engine.serialization

import engine.expressions.Expression
import engine.expressions.RootPath
import engine.methods.GMTOEXPR_KEY
import engine.steps.metadata.MetadataKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class Context(
    val settings: Map<String, String>,
    val precision: Int,
    val solutionVariables: List<String>,
    val preferredStrategies: Map<String, String>,
) {
    companion object {
        fun fromContext(context: engine.context.Context) =
            Context(
                settings = context.settings.entries.associate { it.key.name to it.value.name },
                precision = context.effectivePrecision,
                solutionVariables = context.solutionVariables,
                preferredStrategies = context.preferredStrategies.entries.associate {
                    it.key.simpleName.toString() to it.value.name
                },
            )
    }
}

@Serializable
enum class Format(val value: String) {
    Latex("latex"),
    Solver("solver"),
    Json("json"),
}

@Serializable
data class GmAction(
    val type: String,
    val expressions: List<String>? = null,
    val dragTo: GmActionDragTo? = null,
    val formulaId: String? = null,
)

@Serializable
data class GmActionDragTo(
    val expression: String? = null,
    val position: String? = null,
)

@Serializable
data class MappedExpression(
    @Serializable(with = ExpressionSerializer::class)
    val expression: Any,
    val pathMappings: List<PathMapping>,
)

@Serializable
data class Metadata(
    val key: String,
    val params: List<MappedExpression>? = null,
)

@Serializable
data class PathMapping(
    val type: String,
    val fromPaths: List<String>,
    val toPaths: List<String>,
)

@Serializable
data class Task(
    val taskId: String,
    @Serializable(with = ExpressionSerializer::class)
    val startExpr: Any,
    val pathMappings: List<PathMapping>,
    val explanation: Metadata? = null,
    val steps: List<Transformation>? = null,
    val dependsOn: List<String>? = null,
)

@Serializable
data class Transformation(
    val path: String,
    @Serializable(with = ExpressionSerializer::class)
    val fromExpr: Any,
    @Serializable(with = ExpressionSerializer::class)
    val toExpr: Any,
    @Serializable(with = ExpressionSerializer::class)
    val gmToExpr: Any? = null,
    val pathMappings: List<PathMapping>,
    val type: String? = null,
    val tags: List<String>? = null,
    val explanation: Metadata? = null,
    val gmAction: GmAction? = null,
    val skills: List<Metadata>? = null,
    val steps: List<Transformation>? = null,
    val tasks: List<Task>? = null,
) {
    companion object {
        fun fromTransformation(transformation: engine.steps.Transformation, format: Format = Format.Json) =
            TransformationModeller(format).modelTransformation(transformation)
    }
}

class KeyNameRegistry {
    companion object {
        fun getKeyName(key: MetadataKey): String {
            return key.keyName
        }
    }
}

private class ExpressionSerializer : kotlinx.serialization.KSerializer<Any> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Expression", PrimitiveKind.STRING)

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: Any) {
        val jsonElement = when (value) {
            is String -> Json.encodeToJsonElement(value)
            is Boolean -> Json.encodeToJsonElement(value)
            is List<*> -> Json.encodeToJsonElement(ListSerializer(this), value as List<Any>)
            is Map<*, *> -> Json.encodeToJsonElement(
                MapSerializer(String.serializer(), this),
                value as Map<String, Any>,
            )
            else -> throw Exception("Unexpected type: ${value::class}")
        }
        (encoder as JsonEncoder).encodeJsonElement(jsonElement)
    }

    override fun deserialize(decoder: Decoder): Any {
        throw UnsupportedOperationException()
    }
}

private class TransformationModeller(val format: Format) {
    // ***********************************************************************************************
// The rest of this file is a copy of the TransformationModeller class from the engine
// module, minus the top half of the file.
// ***********************************************************************************************

    fun modelTransformation(trans: engine.steps.Transformation): Transformation {
        return Transformation(
            type = trans.type.toString(),
            tags = trans.tags?.map { it.toString() },
            path = trans.fromExpr.path.toString(),
            fromExpr = modelExpression(trans.fromExpr),
            toExpr = modelExpression(trans.toExpr.removeBrackets()),
            gmToExpr = trans.getExtra<Expression>(GMTOEXPR_KEY)?.let { modelExpression(it) },
            pathMappings = modelPathMappings(trans.toExpr.mergedPathMappings(trans.fromExpr.path!!)),
            explanation = trans.explanation?.let { modelMetadata(it) },
            skills = trans.skills?.map { modelMetadata(it) },
            gmAction = trans.gmAction?.let { modelGmAction(it) },
            steps = trans.steps?.let { steps -> steps.map { modelTransformation(it) } },
            tasks = trans.tasks?.let { tasks -> tasks.map { modelTask(it) } },
        )
    }

    private fun modelTask(task: engine.steps.Task): Task {
        return Task(
            taskId = task.taskId,
            startExpr = modelExpression(task.startExpr),
            pathMappings = modelPathMappings(task.startExpr.mergedPathMappings(RootPath(task.taskId))),
            explanation = task.explanation?.let { modelMetadata(it) },
            steps = if (task.steps.isEmpty()) null else task.steps.map { modelTransformation(it) },
            dependsOn = task.dependsOn.ifEmpty { null },
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
            params = metadata.mappedParams.map {
                MappedExpression(
                    expression = modelExpression(it),
                    pathMappings = modelPathMappings(it.mergedPathMappings(RootPath())),
                )
            },
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

    private fun modelExpression(expr: Expression): Any {
        return when (format) {
            Format.Latex -> expr.toLatexString()
            Format.Solver -> expr.toString()
            Format.Json -> expr.toJson()
        }
    }
}
