package methods

import engine.methods.Format
import engine.methods.Method
import engine.methods.MethodTestCase
import engine.methods.RunnerMethod
import engine.methods.SolutionProcessor
import engine.methods.encodeTransformationToString
import engine.steps.Transformation
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.File
import java.util.Date

class TestResultsListener : TestExecutionListener, SolutionProcessor {

    var testClassName = ""
    var testName = ""

    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        MethodTestCase.addSolutionProcessor(this)
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

    override fun processSolution(input: String, method: Method, solution: Transformation?) {
        if (solution != null && method is RunnerMethod) {
            testData.add(TestData(testClassName, testName, encodeTransformationToString(solution, Format.Json)))
        }
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
        val writer = File(testDataFilePath).writer()
        with(writer) {
            appendLine("export const dateGenerated = \"${Date()}\";")
            appendLine("export const testResults = " + Json.encodeToString(testData))
        }
        writer.close()
    }

    private val testData = mutableListOf<TestData>()
    private val testDataFilePath = "../solver-poker/test-results-src/test-results.ts"
}

@Serializable
private data class TestData(val testClassName: String, val testName: String, val transformation: String)
