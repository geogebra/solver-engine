package methods

import engine.context.Context
import engine.methods.Method
import engine.methods.MethodTestCase
import engine.methods.RunnerMethod
import engine.methods.SolutionProcessor
import engine.steps.Transformation
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.platform.engine.TestTag
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.File
import java.util.Date

class TestResultsListener : TestExecutionListener, SolutionProcessor {

    companion object {
        val gmActionTestTag = TestTag.create("GmAction")
    }

    var testClassName = ""
    var testName = ""
    var testHasGMActionTag = false

    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        MethodTestCase.addSolutionProcessor(this)
    }

    override fun executionStarted(testIdentifier: TestIdentifier?) {
        val testClass = testIdentifier?.source?.orElse(null)
        if (testClass is MethodSource) {
            testClassName = testClass.className
            testName = testIdentifier.displayName
            testHasGMActionTag = testIdentifier.tags.contains(gmActionTestTag)
        } else {
            testClassName = ""
            testName = ""
            testHasGMActionTag = false
        }
    }

    override fun processSolution(context: Context, input: String, method: Method, solution: Transformation?) {
        if (solution != null && method is RunnerMethod) {
            val testDataList = if (testHasGMActionTag) gmActionTestData else testData
            testDataList.add(
                TestData(
                    testClassName = testClassName,
                    testName = testName,
                    context = engine.serialization.Context.fromContext(context),
                    transformation = engine.serialization.Transformation.fromTransformation(solution),
                ),
            )
        }
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
        // @erik eventually we do not write those tests anymore once coverage is good enough for the new ones
        writeTestData()

        writeGmActionTestData()
    }

    private fun writeTestData() {
        val writer = File(testDataFilePath).writer()
        with(writer) {
            appendLine("export const dateGenerated = \"${Date()}\";")
            appendLine("export const testResults = " + Json.encodeToString(testData))
        }
        writer.close()
    }

    private fun writeGmActionTestData() {
        val writer = File(gmActionTestDataFilePath).writer()
        with(writer) {
            // Write one test per line to make it a little bit easier to read.
            appendLine("[")
            for ((i, item) in gmActionTestData.withIndex()) {
                appendLine((if (i == 0) " " else ",") + Json.encodeToString(item))
            }
            appendLine("]")
        }
        writer.close()
    }

    private val gmActionTestData = mutableListOf<TestData>()
    private val testData = mutableListOf<TestData>()

    private val gmActionTestDataFilePath = "build/test-results/gmActionTests.json"
    private val testDataFilePath = "../solver-poker/test-results-src/test-results.ts"
}

@Serializable
private data class TestData(
    val testClassName: String,
    val testName: String,
    val context: engine.serialization.Context,
    val transformation: engine.serialization.Transformation,
)
