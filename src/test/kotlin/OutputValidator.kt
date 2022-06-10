import engine.steps.Transformation
import net.pwall.json.schema.JSONSchema
import serializer.TransformationSerializer
import kotlin.test.fail

object OutputValidator {
    private val schema = JSONSchema.parseFile("src/main/openapi/transformation-schema.yaml")

    fun validateAgainstSchema(transformation: Transformation) {
        val jsonOutput = TransformationSerializer.toPrettyJsonString(transformation)

        val output = schema.validateBasic(jsonOutput)
        if (output.errors != null) {
            output.errors?.forEach {
                println("${it.error} - ${it.instanceLocation}")
            }
            fail("JSON schema validation failed")
        }

        println(jsonOutput)
    }
}