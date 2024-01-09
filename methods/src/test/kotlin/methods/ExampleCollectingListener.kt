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

private fun extractExplanationsKeys(trans: Transformation): Sequence<MetadataKey> =
    sequence {
        val explanation = trans.explanation
        if (explanation != null) {
            yield(explanation.key)
        }
        for (step in trans.steps ?: emptyList()) {
            yieldAll(extractExplanationsKeys(step))
        }
    }
