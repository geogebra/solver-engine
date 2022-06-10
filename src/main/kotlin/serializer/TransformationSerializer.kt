package serializer

import com.fasterxml.jackson.jr.ob.JSON
import com.fasterxml.jackson.jr.ob.JSONComposer
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer
import com.fasterxml.jackson.jr.ob.comp.ComposerBase
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer
import engine.expressions.PathMapping
import engine.expressions.RootPath
import engine.steps.Transformation
import engine.steps.metadata.Metadata

object TransformationSerializer {

    fun toPrettyJsonString(transformation: Transformation): String {
        return toJsonString(JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT), transformation)
    }

    fun toJsonString(transformation: Transformation): String {
        return toJsonString(JSON.std, transformation)
    }

    private fun toJsonString(json: JSON, transformation: Transformation): String {
        return serialize(json.composeString(), transformation).finish()
    }

    fun <T> serialize(composer: JSONComposer<T>, transformation: Transformation): JSONComposer<T> {
        val jsonBuilder = composer.startObject()
        buildJson(jsonBuilder, transformation)
        return jsonBuilder.end()
    }

    private fun <T : ComposerBase> buildJson(composer: ObjectComposer<T>, transformation: Transformation) {
        composer.put("path", transformation.fromExpr.path.toString())
            .put("fromExpr", transformation.fromExpr.expr.toString())
            .put("toExpr", transformation.toExpr.expr.toString())

        val pathMappingComposer = composer.startArrayField("pathMappings")
        for (mapping in transformation.toExpr.mappings.pathMappings(RootPath)) {
            addPathMapping(pathMappingComposer, mapping)
        }
        pathMappingComposer.end()

        if (transformation.explanation != null) {
            val explanationComposer = composer.startObjectField("explanation")
            addMetadata(explanationComposer, transformation.explanation)
            explanationComposer.end()
        }

        if (transformation.skills.isNotEmpty()) {
            val skillsComposer = composer.startArrayField("skills")
            for (skill in transformation.skills) {
                val skillComposer = skillsComposer.startObject()
                addMetadata(skillComposer, skill)
                skillComposer.end()
            }
            skillsComposer.end()
        }

        if (transformation.steps != null) {
            val stepsComposer = composer.startArrayField("steps")
            for (step in transformation.steps) {
                val stepComposer = stepsComposer.startObject()
                buildJson(stepComposer, step)
                stepComposer.end()
            }
            stepsComposer.end()
        }
    }

    private fun <T : ComposerBase> addPathMapping(composer: ArrayComposer<T>, pathMapping: PathMapping) {
        val pathMappingObject = composer.startObject()
        pathMappingObject.put("type", pathMapping.type.toString())

        val fromPaths = pathMappingObject.startArrayField("fromPaths")
        for (path in pathMapping.fromPaths) {
            fromPaths.add(path.toString())
        }
        fromPaths.end()

        val toPaths = pathMappingObject.startArrayField("toPaths")
        for (path in pathMapping.toPaths) {
            toPaths.add(path.toString())
        }
        toPaths.end()

        pathMappingObject.end()
    }

    private fun <T : ComposerBase> addMetadata(composer: ObjectComposer<T>, metadata: Metadata) {
        composer.put("key", metadata.key.toString())
        val paramsComposer = composer.startArrayField("params")
        for (param in metadata.mappedParams) {
            val paramComposer = paramsComposer.startObject()
            paramComposer.put("expression", param.expr.toString())
            val paths = paramComposer.startArrayField("pathMappings")
            for (pathMapping in param.mappings.pathMappings(RootPath)) {
                addPathMapping(paths, pathMapping)
            }
            paths.end()
            paramComposer.end()
        }
        paramsComposer.end()
    }
}
