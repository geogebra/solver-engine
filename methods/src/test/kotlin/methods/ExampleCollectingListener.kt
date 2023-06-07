package methods

import engine.methods.AssertionCollector
import engine.methods.Format
import engine.methods.Method
import engine.methods.MethodId
import engine.methods.MethodTestCase
import engine.methods.RunnerMethod
import engine.methods.SolutionProcessor
import engine.methods.encodeTransformationToString
import engine.methods.getMethodId
import engine.steps.Transformation
import engine.steps.metadata.MetadataKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.File
import java.util.Date

/**
 * This allows collecting data from the tests that run.  Note that if the tests do not run because they are cached
 * this will not run either!
 */
class ExampleCollectingListener : TestExecutionListener, SolutionProcessor {
    private var testName = ""
    private var testClassName = ""

    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        MethodTestCase.addSolutionProcessor(this)
        File("../solver-poker/test-results-src/tests-running.json").writeText("true")
    }

    override fun executionStarted(testIdentifier: TestIdentifier?) {
        val testClass = testIdentifier?.source?.orElse(null)
        if (testClass is MethodSource) {
            testClassName = testClass.className
            testName = testIdentifier.displayName
        } else {
            testClassName = ""
            testName = ""
        }
    }

    override fun executionFinished(testIdentifier: TestIdentifier?, testExecutionResult: TestExecutionResult?) {
        if (testExecutionResult?.status == TestExecutionResult.Status.FAILED) {
            val test = testReports.last()
            val throwable = testExecutionResult.throwable.get()
            test.failureMessage = """$throwable
${throwable.stackTrace.joinToString("\n") { "    at $it" }
                .substringBefore("\n    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)")}
${throwable.cause ?: ""}""".trimEnd()
        }
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
        if (examplesFilePath != null) {
            val writer = File(examplesFilePath).writer()
            with(writer) {
                appendLine("Translation Key, Plan Id, Input")
                for (example in examples.sortedBy { it.key }) {
                    appendLine("${example.key}, ${example.method}, \"${example.input}\"")
                }
            }
            writer.close()
        }
        writeReport()
        File("../solver-poker/test-results-src/tests-running.json").writeText("false")
    }

    private fun writeReport() {
        File("../solver-poker/test-results-src/test-results.ts").writeText(
            """export const dateGenerated = "${Date()}";
export const testResults = [
${testReports.sortedBy { !it.passed }.joinToString(",\n", transform = ::generateReport)}
];
""",
        )
    }

    override fun processSolution(input: String, method: Method, solution: Transformation?) {
        if (examplesFilePath == null) return
        if (solution != null && method is RunnerMethod) {
            val category = method::class.simpleName?.removeSuffix("Plans")
            val keys = extractExplanationsKeys(solution).toSet()
            if (category != null && keys.isNotEmpty()) {
                val methodId = MethodId(category, method.name)
                if (methodRegistry.methodIsPublic(methodId)) {
                    examples.addAll(keys.map { Example(input, method.toString(), KeyNameRegistry.getKeyName(it)) })
                }
            }
        }
    }

    override fun registerTest(test: MethodTestCase) {
        // Uncomment line below to have report page update as tests run. Will noticeably slow down execution of
        // the test suite in exchange for having the first test results sooner.
        // writeReport()
        test.testName = testName
        test.testClassName = testClassName
        testReports.add(test)
    }

    private val testReports = mutableListOf<MethodTestCase>()
    private val examples = mutableListOf<Example>()
    private val examplesFilePath by lazy { System.getenv("SOLVER_EXAMPLES_FILE_PATH") }
}

fun generateReport(test: MethodTestCase): String {
    val inputExpr = test.inputExpr
    val failureMessage = test.failureMessage
    val transformation = test.transformation

    // We need to get rid of the root-level assertion collector, the root-level
    // AssertionCollector is not actually a step in the transformation, it is just there
    // to collect information outside the `check(callback)` lambda function in the test.
    var newCollector: AssertionCollector? = test.assertionCollector.steps!![0].copy()
    if (newCollector!!.fromExpr == null) newCollector.fromExpr = test.assertionCollector.fromExpr
    // This will make it easier to compare the assertion tree to the transformation,
    // because if `noSteps` is true then the transformation should be null.
    if (newCollector.noSteps == true) newCollector = null

    val transformationString = encodeTransformationToString(transformation, Format.Solver)
    return """    {
      "testClassName": ${stringToJson(test.testClassName)},
      "testName": ${stringToJson(test.testName)},
      "inputExpression": ${stringToJson(inputExpr.toString())},
      "methodId": ${stringToJson(getMethodId(test.method))},
      "assertionTree": ${stringToJson(newCollector?.toJsonString() ?: "null")},
      "transformation": ${stringToJson(transformationString)},
      "transformationJsonMath": ${stringToJson(encodeTransformationToString(transformation, Format.Json))},
      "passed": ${test.passed},
      "failureMessage": ${if (failureMessage === null) null else stringToJson(failureMessage)}
    }"""
}

private fun stringToJson(str: String) = Json.encodeToJsonElement(str).jsonPrimitive.toString()

private data class Example(val input: String, val method: String, val key: String)

private fun extractExplanationsKeys(trans: Transformation): Sequence<MetadataKey> = sequence {
    val explanation = trans.explanation
    if (explanation != null) {
        yield(explanation.key)
    }
    for (step in trans.steps ?: emptyList()) {
        yieldAll(extractExplanationsKeys(step))
    }
}
