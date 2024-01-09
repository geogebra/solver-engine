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
        if (solution != null && method is RunnerMethod && testHasGMActionTag) {
            gmActionTestData.add(
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
        writeGmActionTestData()
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

    private val gmActionTestDataFilePath = "build/test-results/gmActionTests.json"
}

@Serializable
private data class TestData(
    val testClassName: String,
    val testName: String,
    val context: engine.serialization.Context,
    val transformation: engine.serialization.Transformation,
)
