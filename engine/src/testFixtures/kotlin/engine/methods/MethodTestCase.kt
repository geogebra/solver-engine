package engine.methods

import engine.context.Context
import engine.context.emptyContext
import engine.expressions.Expression
import engine.expressions.Path
import engine.expressions.PathMapping
import engine.expressions.PathMappingType
import engine.expressions.PathScope
import engine.expressions.RootOrigin
import engine.expressions.RootPath
import engine.expressions.parsePathAndScope
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import engine.steps.metadata.MetadataKey
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

    open fun finalize() {
        if (checkedMappings != null) {
            assertEquals(pathMappings.count(), checkedMappings, "Some path mappings have not been checked")
        }
    }
}

class MappedExpressionCheck(private val mappedExpression: Expression, rootPath: Path) :
    PathMappingsCheck(mappedExpression.mergedPathMappings(), rootPath) {

    var expr: String?
        get() = null
        set(value) {
            assertEquals(parseExpression(value!!), mappedExpression)
        }
}

@TestCaseBuilderMarker
class MetadataCheck(private val rootPath: Path, private val keyChecker: (MetadataKey) -> Metadata) {
    private var checkedParams: Int? = null
    private var params: List<Expression>? = null

    var key: MetadataKey?
        get() = null
        set(value) {
            params = keyChecker(value!!).mappedParams
        }

    fun param(assert: MappedExpressionCheck.() -> Unit) {
        assertNotNull(params, "Please specify the MetadataKey before the parameters")

        val param = MappedExpressionCheck(params!![checkedParams ?: 0], rootPath)
        param.assert()
        param.finalize()
        checkedParams = (checkedParams ?: 0) + 1
    }

    fun finalize() {
        if (checkedParams != null) {
            assertNotNull(params) // should fail already in `param` and never here
            assertEquals(params!!.size, checkedParams, "Some parameters have not been checked")
        }
    }
}

@TestCaseBuilderMarker
class TaskCheck(private val task: Task?) :
    PathMappingsCheck(
        task?.startExpr?.mergedPathMappings() ?: emptyList(),
        task?.rootPath ?: RootPath(),
    ) {
    var startExpr: String?
        get() = null
        set(value) {
            assertNotNull(task)
            assertEquals((parseExpression(value!!)), task.startExpr)
        }

    var taskId: String?
        get() = null
        set(value) {
            assertNotNull(task)
            assertEquals(value, task.taskId)
        }

    private var checkedSteps: Int? = null

    fun explanation(init: MetadataCheck.() -> Unit) {
        assertNotNull(task)
        val explanationCheck = MetadataCheck(task.rootPath) {
            assertNotNull(task.explanation, "Explanation is empty")
            assertEquals(
                it,
                task.explanation!!.key,
                "Explanation key does not match",
            )
            task.explanation!!
        }

        explanationCheck.init()
        explanationCheck.finalize()
    }

    private fun getCurrentStep(): Transformation {
        assertNotNull(task)

        val currentStep = checkedSteps ?: 0
        checkedSteps = currentStep + 1

        assertNotNull(task.steps, "The tasks has no steps, even though some were specified")
        assert(task.steps.size > currentStep) {
            "$checkedSteps steps were specified, but the task only has ${task.steps.size}"
        }

        return task.steps[currentStep]
    }

    fun step(assert: TransformationCheck.() -> Unit) {
        val currentStep = getCurrentStep()

        if (currentStep.tags?.contains(Transformation.Tag.InvisibleChange) == true) {
            // if this is an invisible step, check the next one instead
            step(assert)
        } else {
            checkTransformation(currentStep, assert)
        }
    }

    fun noStep() {
        assertNotNull(task)
        assert(task.steps.isEmpty()) { "The tasks has ${task.steps.size} steps but should have none" }
    }

    override fun finalize() {
        super.finalize()
        if (checkedSteps != null) {
            val transSteps = task!!.steps
            assertNotNull(transSteps) // should fail already in `step` and never here
            assertEquals(checkedSteps, transSteps.size, "Some steps have not been checked")
        }
    }
}

@TestCaseBuilderMarker
class TransformationCheck(private val trans: Transformation?) :
    PathMappingsCheck(
        trans?.toExpr?.mergedPathMappings() ?: emptyList(),
        trans?.fromExpr?.path ?: RootPath(),
    ) {
    var fromExpr: String?
        get() = null
        set(value) {
            assertNotNull(trans)
            assertEquals(parseExpression(value!!), trans.fromExpr)
        }

    var toExpr: String?
        get() = null
        set(value) {
            assertNotNull(trans)
            assertEquals(parseExpression(value!!), trans.toExpr.removeBrackets())
        }

    private var checkedSkills: Int? = null
    private var checkedSteps: Int? = null
    private var checkedTasks: Int? = null

    fun noTransformation() {
        assertNull(trans, "Transformation should not have been executed")
    }

    fun explanation(init: MetadataCheck.() -> Unit) {
        assertNotNull(trans)
        val explanationCheck = MetadataCheck(trans.fromExpr.path!!) {
            assertNotNull(trans.explanation, "Explanation is empty")
            assertEquals(
                it,
                trans.explanation!!.key,
                "Explanation key does not match",
            )
            trans.explanation!!
        }

        explanationCheck.init()
        explanationCheck.finalize()
    }

    fun skill(init: MetadataCheck.() -> Unit) {
        assertNotNull(trans)

        val skillCheck = MetadataCheck(trans.fromExpr.path!!) {
            val skill = trans.skills?.find { s -> s.key == it }
            assertNotNull(skill, "No skill with given key found")
            skill
        }
        skillCheck.init()
        skillCheck.finalize()
        checkedSkills = (checkedSkills ?: 0) + 1
    }

    fun getCurrentStep(): Transformation {
        assertNotNull(trans)

        val currentStep = checkedSteps ?: 0
        checkedSteps = currentStep + 1

        assertNotNull(trans.steps, "The transformation has no steps, even though some were specified")
        assert(trans.steps!!.size > currentStep) {
            "$checkedSteps steps were specified, but the transformation only has ${trans.steps!!.size}"
        }

        return trans.steps!![currentStep]
    }

    fun invisibleStep(assert: TransformationCheck.() -> Unit) {
        val currentStep = getCurrentStep()

        assert(currentStep.tags?.contains(Transformation.Tag.InvisibleChange) == true)
        checkTransformation(currentStep, assert)
    }

    fun step(assert: TransformationCheck.() -> Unit) {
        val currentStep = getCurrentStep()

        if (currentStep.tags?.contains(Transformation.Tag.InvisibleChange) == true) {
            // if this is an invisible step, check the next one instead
            step(assert)
        } else {
            checkTransformation(currentStep, assert)
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

        val check = TaskCheck(trans.tasks!![currentTask])
        check.assert()
        check.finalize()
    }

    override fun finalize() {
        super.finalize()
        if (checkedSkills != null) {
            assertEquals(checkedSkills, trans!!.skills?.size, "Some skills have not been checked")
        }
        if (checkedSteps != null) {
            val transSteps = trans!!.steps
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

@TestCaseBuilderMarker
class MethodTestCase {
    var context: Context = emptyContext
    lateinit var method: Method
    lateinit var inputExpr: String

    fun check(assert: TransformationCheck.() -> Unit) {
        val expr = parseExpression(inputExpr)
        val trans = method.tryExecute(context, expr.withOrigin(RootOrigin()))

        for (processor in solutionProcessors) {
            processor.processSolution(inputExpr, method, trans)
        }

        checkTransformation(trans, assert)
    }

    companion object {
        private val solutionProcessors = mutableListOf<SolutionProcessor>()

        fun addSolutionProcessor(processor: SolutionProcessor) {
            solutionProcessors.add(processor)
        }
    }
}

fun interface SolutionProcessor {
    fun processSolution(input: String, method: Method, solution: Transformation?)
}

private fun Transformation.isThroughStep() =
    steps?.let { it.size == 1 && it[0].fromExpr == fromExpr } ?: false

private fun checkTransformation(trans: Transformation?, assert: TransformationCheck.() -> Unit) {
    if (trans != null && trans.isThroughStep()) {
        checkTransformation(trans.steps?.get(0), assert)
        return
    }

    val check = TransformationCheck(trans)
    check.assert()
    check.finalize()
}

fun testMethod(init: MethodTestCase.() -> Unit) {
    val testCase = MethodTestCase()
    testCase.init()
}

/**
 * Test a method with a context set up with solutionVariable equal to "x"
 */
fun testMethodInX(init: MethodTestCase.() -> Unit) {
    val testCase = MethodTestCase()
    testCase.context = Context(solutionVariables = listOf("x"))
    testCase.init()
}
