package methods

import engine.context.Context
import engine.methods.Method
import engine.methods.MethodId
import engine.methods.MethodTestCase
import engine.methods.RunnerMethod
import engine.methods.SolutionProcessor
import engine.steps.Transformation
import engine.steps.metadata.MetadataKey
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan
import java.io.File

/**
 * This allows collecting data from the tests that run.  Note that if the tests do not run because they are cached
 * this will not run either!
 */
class ExampleCollectingListener : TestExecutionListener, SolutionProcessor {

    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        if (examplesFilePath != null) {
            MethodTestCase.addSolutionProcessor(this)
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
    }

    override fun processSolution(context: Context, input: String, method: Method, solution: Transformation?) {
        if (solution != null && method is RunnerMethod) {
            val category = method::class.simpleName?.removeSuffix("Plans")
            val keys = extractExplanationsKeys(solution).toSet()
            if (category != null && keys.isNotEmpty()) {
                val methodId = MethodId(category, method.name)
                if (!methodRegistry.methodIsNotListed(methodId)) {
                    examples.addAll(keys.map { Example(input, method.toString(), KeyNameRegistry.getKeyName(it)) })
                }
            }
        }
    }

    private val examples = mutableListOf<Example>()
    private val examplesFilePath by lazy { System.getenv("SOLVER_EXAMPLES_FILE_PATH") }
}

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
