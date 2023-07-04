package engine.methods

import engine.context.Context
import engine.context.emptyContext
import engine.expressions.Expression
import engine.expressions.Path
import engine.expressions.PathMapping
import engine.expressions.PathMappingType
import engine.expressions.PathScope
import engine.expressions.Root
import engine.expressions.RootPath
import engine.expressions.parsePathAndScope
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import engine.steps.metadata.MetadataKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.opentest4j.AssertionFailedError
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DslMarker
annotation class TestCaseBuilderMarker

data class PathMappingPathsBuilder(val type: PathMappingType) {
    private var _fromPaths: List<Pair<Path, PathScope>> = emptyList()
    private var _toPaths: List<Pair<Path, PathScope>> = emptyList()

    fun fromPaths(vararg pathStrings: String) {
        _fromPaths = pathStrings.map { parsePathAndScope(it) }
    }

    fun toPaths(vararg pathStrings: String) {
        _toPaths = pathStrings.map { parsePathAndScope(it) }
    }

    fun getPathMapping(): PathMapping {
        return PathMapping(_fromPaths, type, _toPaths)
    }
}

@TestCaseBuilderMarker
open class PathMappingsCheck(mappings: List<PathMapping>, private val rootPath: Path) {
    private val pathMappings = mappings.map { it.relativeTo(rootPath) }.toList()

    private var checkedMappings: Int? = null

    private fun addPathMapping(type: PathMappingType, init: PathMappingPathsBuilder.() -> Unit) {
        val builder = PathMappingPathsBuilder(type)
        builder.init()
        addPathMapping(builder.getPathMapping())
    }

    private fun addPathMapping(pathMapping: PathMapping) {
        assert(pathMappings.contains(pathMapping)) { "No such path mapping found: $pathMapping" }
        checkedMappings = (checkedMappings ?: 0) + 1
    }

    fun cancel(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Cancel, init)
    }

    fun introduce(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Introduce, init)
    }

    fun introduce(toPath: String) {
        addPathMapping(PathMapping(emptyList(), PathMappingType.Introduce, listOf(parsePathAndScope(toPath))))
    }

    fun combine(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Combine, init)
    }

    fun shift(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Shift, init)
    }

    fun shift(fromPath: String, toPath: String) {
        addPathMapping(PathMapping(listOf(parsePathAndScope(fromPath)), PathMappingType.Shift, listOf(parsePathAndScope((toPath)))))
    }

    fun move(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Move, init)
    }

    fun move(fromPath: String, toPath: String) {
        addPathMapping(PathMapping(listOf(parsePathAndScope(fromPath)), PathMappingType.Move, listOf(parsePathAndScope((toPath)))))
    }

    fun keep(vararg paths: String) {
        for (path in paths) {
            shift(path, path)
        }
    }

    fun factor(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Factor, init)
    }

    fun distribute(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Distribute, init)
    }

    fun transform(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Transform, init)
    }

    fun transform(fromPath: String, toPath: String) {
        addPathMapping(PathMapping(listOf(parsePathAndScope(fromPath)), PathMappingType.Transform, listOf(parsePathAndScope((toPath)))))
    }

    fun cancel(vararg paths: String) {
        addPathMapping(PathMapping(paths.toList().map { parsePathAndScope(it) }, PathMappingType.Cancel, emptyList()))
    }

    fun cancel(paths: Map<String, List<PathScope>>) {
        addPathMapping(
            PathMapping(
                paths.toList().map { parsePathAndScope(it.first) },
                PathMappingType.Cancel,
                emptyList(),
            ),
        )
    }

    open fun finish() {
        if (checkedMappings != null) {
            assertEquals(pathMappings.count(), checkedMappings, "Some path mappings have not been checked")
        }
    }
}

class MappedExpressionCheck(
    private val mappedExpression: Expression,
    rootPath: Path,
    private val test: MethodTestCase,
) : PathMappingsCheck(mappedExpression.mergedPathMappings(), rootPath) {

    var expr: String?
        get() = null
        set(value) {
            if (!test.logMode) assertEquals(parseExpression(value!!), mappedExpression)
        }
}

@TestCaseBuilderMarker
class MetadataCheck(
    private val rootPath: Path?,
    private val test: MethodTestCase,
    /** Returns the actual (as opposed to expected) metadata object, for that given key.
     * Also, this should run assertions that the key is right, if not in `logMode`. */
    private val keyChecker: (MetadataKey) -> Metadata?,
) {
    private var checkedParamCount = 0
    private var params: List<Expression>? = null

    var key: MetadataKey? = null
        set(value) {
            field = value!!
            params = keyChecker(value)?.mappedParams
        }

    fun param(assert: MappedExpressionCheck.() -> Unit) {
        assertNotNull(params, "Please specify the MetadataKey before the parameters")

        if (!test.logMode) {
            val param = MappedExpressionCheck(params!![checkedParamCount], rootPath!!, test)
            param.assert()
            param.finish()
            checkedParamCount++
        }
    }

    fun finish() {
        if (!test.logMode && checkedParamCount != 0) {
            assertEquals(params!!.size, checkedParamCount, "Some parameters have not been checked")
        }
    }
}

@TestCaseBuilderMarker
class TaskCheck(
    private val task: Task?,
    private val assertionCollector: AssertionCollector,
    private val test: MethodTestCase,
) :
    PathMappingsCheck(task?.startExpr?.mergedPathMappings() ?: emptyList(), task?.rootPath ?: RootPath()) {
    var startExpr: String?
        get() = null
        set(value) {
            assertionCollector.fromExpr = value
            if (!test.logMode) {
                assertNotNull(task)
                assertEquals((parseExpression(value!!)), task.startExpr)
            }
        }

    var taskId: String?
        get() = null
        set(value) {
            assertionCollector.toExpr = value
            if (!test.logMode) {
                assertNotNull(task)
                assertEquals(value, task.taskId)
            }
        }

    private var checkedSteps: Int? = null

    fun explanation(init: MetadataCheck.() -> Unit) {
        assertNull(
            assertionCollector.explanation,
            "Don't assert that there are two different explanations. You have already asserted that there was an explanation with key ${assertionCollector.explanation?.key}",
        )
        val explanationCheck = MetadataCheck(task?.rootPath, test) {
            if (test.logMode) {
                assertionCollector.explanation = AssertionCollector(key = it.keyName)
            } else {
                assertNotNull(task!!.explanation, "Explanation is empty")
                assertEquals(
                    it.keyName,
                    task.explanation!!.key.keyName,
                    "Explanation key does not match",
                )
            }
            task?.explanation
        }

        explanationCheck.init()
        explanationCheck.finish()
    }

    private fun getCurrentStep(): Transformation? {
        val currentStep = checkedSteps ?: 0
        checkedSteps = currentStep + 1

        if (!test.logMode) {
            assertNotNull(task)
            assertNotNull(task.steps, "The tasks has no steps, even though some were specified")
            assert(task.steps.size > currentStep) {
                "$checkedSteps steps were specified, but the task only has ${task.steps.size}"
            }
        }

        return task?.steps?.getOrNull(currentStep)
    }

    fun step(assert: TransformationCheck.() -> Unit) {
        val currentStep = getCurrentStep()

        if (currentStep?.tags?.contains(Transformation.Tag.InvisibleChange) == true) {
            // if this is an invisible step, check the next one instead
            step(assert)
        } else {
            checkTransformation(currentStep, assertionCollector, test, assert)
        }
    }

    fun noStep() {
        assertionCollector.noSteps = true
        assertNotNull(task)
        assert(task.steps.isEmpty()) { "The tasks has ${task.steps.size} steps but should have none" }
    }

    override fun finish() {
        super.finish()
        if (checkedSteps != null) {
            val transSteps = task!!.steps
            assertNotNull(transSteps) // should fail already in `step` and never here
            assertEquals(checkedSteps, transSteps.size, "Some steps have not been checked")
        }
    }
}

@TestCaseBuilderMarker
class TransformationCheck(
    private val trans: Transformation?,
    private val assertionCollector: AssertionCollector,
    private val test: MethodTestCase,
) :
    PathMappingsCheck(trans?.toExpr?.mergedPathMappings() ?: emptyList(), trans?.fromExpr?.origin?.path ?: RootPath()) {
    var fromExpr: String?
        get() = null
        set(value) {
            assertionCollector.fromExpr = value
            if (!test.logMode) {
                assertNotNull(trans)
                assertEquals(parseExpression(value!!), trans.fromExpr)
            }
        }

    var toExpr: String?
        get() = null
        set(value) {
            assertionCollector.toExpr = value
            if (!test.logMode) {
                assertNotNull(trans)
                assertEquals(parseExpression(value!!), trans.toExpr.removeBrackets())
            }
        }

    private var checkedSkills: Int? = null
    private var checkedSteps: Int? = null
    private var checkedTasks: Int? = null

    fun noTransformation() {
        assertionCollector.noSteps = true
        assertNull(trans, "Transformation should not have been executed")
    }

    fun explanation(init: MetadataCheck.() -> Unit) {
        assertNull(
            assertionCollector.explanation,
            "Don't assert that there are two different explanations. You have already asserted that there was an explanation with key ${assertionCollector.explanation?.key}",
        )
        val explanationCheck = MetadataCheck(trans?.fromExpr?.origin?.path, test) {
            val mainExplanation = trans?.explanation
            if (test.logMode) {
                assertionCollector.explanation = AssertionCollector(key = it.keyName)
            } else {
                assertNotNull(mainExplanation, "Explanation is empty")
                assertEquals(
                    it.keyName,
                    mainExplanation.key.keyName,
                    "Explanation key does not match",
                )
            }
            mainExplanation
        }

        explanationCheck.init()
        explanationCheck.finish()
    }

    fun skill(init: MetadataCheck.() -> Unit) {
        assertNotNull(trans)

        val skillCheck = MetadataCheck(trans.fromExpr.origin.path!!, test) {
            val skill = trans.skills?.find { s -> s.key == it }
            if (!test.logMode) assertNotNull(skill, "No skill with given key found")
            skill
        }
        skillCheck.init()
        skillCheck.finish()
        checkedSkills = (checkedSkills ?: 0) + 1
    }

    fun getCurrentStep(): Transformation? {
        val currentStep = checkedSteps ?: 0
        checkedSteps = currentStep + 1

        val mainSteps = trans?.steps
        if (!test.logMode) {
            assertNotNull(trans)
            assertNotNull(mainSteps, "The transformation has no steps, even though some were specified")
            assert(mainSteps.size > currentStep) {
                "$checkedSteps steps were specified, but the transformation only has ${trans.steps!!.size}"
            }
        }

        return mainSteps?.getOrNull(currentStep)
    }

    fun invisibleStep(assert: TransformationCheck.() -> Unit) {
        val currentStep = getCurrentStep()

        if (!test.logMode) {
            assert(currentStep!!.tags?.contains(Transformation.Tag.InvisibleChange) == true)
        }
        checkTransformation(currentStep, assertionCollector, test, assert)
    }

    fun step(assert: TransformationCheck.() -> Unit) {
        val currentStep = getCurrentStep()

        if (currentStep?.tags?.contains(Transformation.Tag.InvisibleChange) == true) {
            // if this is an invisible step, check the next one instead
            step(assert)
        } else {
            checkTransformation(currentStep, assertionCollector, test, assert)
        }
    }

    fun task(assert: TaskCheck.() -> Unit) {
        assertNotNull(trans)

        val currentTask = checkedTasks ?: 0
        checkedTasks = currentTask + 1

        assertNotNull(trans.tasks, "The transformation has no tasks, even though some were specified")
        assert(trans.tasks!!.size > currentTask) {
            "$checkedTasks tasks were specified, but the transformation only has ${trans.tasks!!.size}"
        }

        val newChild = assertionCollector.newTask()
        newChild.isTask = true
        val check = TaskCheck(trans.tasks!![currentTask], newChild, test)
        check.assert()
        check.finish()
    }

    override fun finish() {
        super.finish()
        if (!test.logMode) {
            if (checkedSkills != null) {
                assertEquals(checkedSkills, trans!!.skills?.size, "Some skills have not been checked")
            }
            if (checkedSteps != null) {
                val transSteps = trans?.steps
                assertNotNull(transSteps) // should fail already in `step` and never here
                assertEquals(checkedSteps, transSteps.size, "Some steps have not been checked")
            }
            if (checkedTasks != null) {
                val transTasks = trans!!.tasks
                assertNotNull(transTasks)
                assertEquals(checkedTasks, transTasks.size, "Some tasks have not been checked")
            }
            // Assert that the gmAction will probably not cause problems when being
            // serialized, like in PLUT-567
            trans?.gmAction?.expressionsAsPathStrings()
            trans?.gmAction?.dragToExpressionAsPathString()
        }
    }
}

@TestCaseBuilderMarker
class MethodTestCase {
    var testName: String = "unknown"
    var testClassName: String = "unknown"
    var context: Context = emptyContext
    lateinit var method: Method
    var inputExpr: String? = null
        set(value) {
            field = value
            assertionCollector.fromExpr = value
        }
    var transformation: Transformation? = null
    var passed = false
    var failureMessage: String? = null
    internal var logMode = false
    internal var firstCheckTransformation = true
    internal val throughSteps = mutableSetOf<Transformation>()
    var assertionCollector: AssertionCollector = AssertionCollector(noSteps = false)
        internal set
    init {
        for (processor in solutionProcessors) {
            processor.registerTest(this)
        }
    }

    fun check(assert: TransformationCheck.() -> Unit) {
        if (!logMode) {
            val expr = parseExpression(inputExpr!!)
            this.transformation = method.tryExecute(context, expr.withOrigin(Root()))

            for (processor in solutionProcessors) {
                processor.processSolution(inputExpr!!, method, transformation)
            }
        }
        checkTransformation(transformation, assertionCollector, this, assert)
    }

    companion object {
        private val solutionProcessors = mutableListOf<SolutionProcessor>()

        fun addSolutionProcessor(processor: SolutionProcessor) {
            solutionProcessors.add(processor)
        }
    }

    internal fun finish() {
        if (!logMode) passed = true
    }
}

fun getMethodId(method: Method): String {
    val category = method::class.simpleName!!.removeSuffix("Plans")
    return if (method is RunnerMethod) {
        MethodId(category, method.name).toString()
    } else {
        "<an anonymous plan>"
    }
}

interface SolutionProcessor {
    fun processSolution(input: String, method: Method, solution: Transformation?)
    fun registerTest(test: MethodTestCase)
}

private fun Transformation.isThroughStep() =
    steps?.let { it.size == 1 && it[0].fromExpr == fromExpr } ?: false

private fun checkTransformation(
    trans: Transformation?,
    assertionCollector: AssertionCollector,
    test: MethodTestCase,
    assert: TransformationCheck.() -> Unit,
): TransformationCheck {
    var transVar = trans
    val newAssertionCollector = if (test.firstCheckTransformation) {
        test.firstCheckTransformation = false
        assertionCollector
    } else {
        assertionCollector.newStep()
    }
    if (test.logMode) {
        if (test.throughSteps.contains(transVar)) {
            newAssertionCollector.wasIgnoredDuringTestAsAThroughStep = true
            transVar = transVar?.steps?.get(0)
        }
    } else if (transVar != null && transVar.isThroughStep()) {
        try {
            val transCheckOfChild = checkTransformation(transVar.steps?.get(0), newAssertionCollector, test, assert)
            test.throughSteps.add(transVar)
            return transCheckOfChild
        } catch (_: AssertionFailedError) {
            // do nothing
        }
    }

    val check = TransformationCheck(transVar, newAssertionCollector, test)
    check.assert()
    check.finish()
    return check
}

@Serializable
data class AssertionCollector(
    var explanation: AssertionCollector? = null,
    var fromExpr: String? = null,
    var steps: MutableList<AssertionCollector>? = null,
    var tasks: MutableList<AssertionCollector>? = null,
    var toExpr: String? = null,
    var key: String? = null,
    var noSteps: Boolean? = null,
    var wasIgnoredDuringTestAsAThroughStep: Boolean? = null,
    var isTask: Boolean? = null,
) {
    fun newStep(): AssertionCollector {
        if (steps == null) steps = mutableListOf()
        val child = AssertionCollector()
        steps!!.add(child)
        return child
    }

    fun newTask(): AssertionCollector {
        if (tasks == null) tasks = mutableListOf()
        val child = AssertionCollector()
        tasks!!.add(child)
        return child
    }

    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}

fun testMethod(init: MethodTestCase.() -> Unit) {
    val testCase = MethodTestCase()
    try {
        testCase.init()
        testCase.finish()
    } finally {
        testCase.logMode = true
        // Throw away the old assertionCollector. It was just a dummy. This will be the real one.
        testCase.assertionCollector = AssertionCollector()
        testCase.init()
        testCase.finish()
    }
}

/**
 * Test a method with a context set up with solutionVariable equal to "x"
 */
fun testMethodInX(init: MethodTestCase.() -> Unit) {
    val testCase = MethodTestCase()
    try {
        testCase.context = Context(solutionVariables = listOf("x"))
        testCase.init()
        testCase.finish()
    } finally {
        testCase.logMode = true
        // Throw away the old assertionCollector. It was just a dummy. This will be the real one.
        testCase.assertionCollector = AssertionCollector()
        testCase.init()
        testCase.finish()
    }
}
