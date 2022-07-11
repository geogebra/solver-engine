package methods.plans

import engine.context.Context
import engine.context.emptyContext
import engine.expressions.MappedExpression
import engine.expressions.Path
import engine.expressions.PathMapping
import engine.expressions.PathMappingTree
import engine.expressions.PathMappingType
import engine.expressions.RootPath
import engine.expressions.Subexpression
import engine.expressions.parsePath
import engine.methods.Plan
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
    private var _fromPaths: List<Path> = emptyList()
    private var _toPaths: List<Path> = emptyList()

    fun fromPaths(vararg pathStrings: String) {
        _fromPaths = pathStrings.map { parsePath(it) }
    }

    fun toPaths(vararg pathStrings: String) {
        _toPaths = pathStrings.map { parsePath(it) }
    }

    fun getPathMapping(): PathMapping {
        return PathMapping(_fromPaths, type, _toPaths)
    }
}

@TestCaseBuilderMarker
open class PathMappingsCheck(mappings: PathMappingTree?, private val rootPath: Path) {
    private val pathMappings: Sequence<PathMapping> =
        mappings?.pathMappings(RootPath)?.map { it.relativeTo(rootPath) } ?: emptySequence()

    private var checkedMappings: Int? = null

    private fun addPathMapping(type: PathMappingType, init: PathMappingPathsBuilder.() -> Unit) {
        val builder = PathMappingPathsBuilder(type)
        builder.init()
        assert(pathMappings.contains(builder.getPathMapping())) { "No such path mapping found" }
        checkedMappings = (checkedMappings ?: 0) + 1
    }

    fun cancel(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Cancel, init)
    }

    fun introduce(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Introduce, init)
    }

    fun combine(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Combine, init)
    }

    fun move(init: PathMappingPathsBuilder.() -> Unit) {
        addPathMapping(PathMappingType.Move, init)
    }

    open fun finalize() {
        if (checkedMappings != null) {
            assertEquals(pathMappings.count(), checkedMappings, "Some path mappings have not been checked")
        }
    }
}

class MappedExpressionCheck(private val mappedExpression: MappedExpression, rootPath: Path) :
    PathMappingsCheck(mappedExpression.mappings, rootPath) {

    var expr: String?
        get() = null
        set(value) {
            assertEquals(parseExpression(value!!), mappedExpression.expr)
        }
}

@TestCaseBuilderMarker
class MetadataCheck(private val rootPath: Path, private val keyChecker: (MetadataKey) -> Metadata) {
    private var checkedParams: Int? = null
    private var params: List<MappedExpression>? = null

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

class TransformationCheck(private val trans: Transformation?) :
    PathMappingsCheck(trans?.toExpr?.mappings, trans?.fromExpr?.path ?: RootPath) {
    var fromExpr: String?
        get() = null
        set(value) {
            assertNotNull(trans)
            assertEquals(parseExpression(value!!), trans.fromExpr.expr)
        }

    var toExpr: String?
        get() = null
        set(value) {
            assertNotNull(trans)
            assertEquals(parseExpression(value!!), trans.toExpr.expr)
        }

    private var checkedSkills: Int? = null
    private var checkedSteps: Int? = null

    fun noTransformation() {
        assertNull(trans, "Transformation should not have been executed")
    }

    fun explanation(init: MetadataCheck.() -> Unit) {
        assertNotNull(trans)
        val explanationCheck = MetadataCheck(trans.fromExpr.path) {
            assertNotNull(trans.explanation, "Explanation is empty")
            assertEquals(
                it,
                trans.explanation!!.key,
                "Explanation key does not match"
            )
            trans.explanation!!
        }

        explanationCheck.init()
        explanationCheck.finalize()
    }

    fun skill(init: MetadataCheck.() -> Unit) {
        assertNotNull(trans)

        val skillCheck = MetadataCheck(trans.fromExpr.path) {
            val skill = trans.skills.find { s -> s.key == it }
            assertNotNull(skill, "No skill with given key found")
            skill
        }
        skillCheck.init()
        skillCheck.finalize()
        checkedSkills = (checkedSkills ?: 0) + 1
    }

    fun step(assert: TransformationCheck.() -> Unit) {
        assertNotNull(trans)

        val currentStep = checkedSteps ?: 0
        checkedSteps = currentStep + 1

        assertNotNull(trans.steps, "The transformation has no steps, even though some were specified")
        assert(trans.steps!!.size > currentStep) {
            "$checkedSteps steps were specified, but the transformation only has ${trans.steps!!.size}"
        }

        val stepCheck = TransformationCheck(trans.steps!![currentStep])
        stepCheck.assert()
        stepCheck.finalize()
    }

    override fun finalize() {
        super.finalize()
        if (checkedSkills != null) {
            assertEquals(checkedSkills, trans!!.skills.size, "Some skills have not been checked")
        }
        if (checkedSteps != null) {
            val transSteps = trans!!.steps
            assertNotNull(transSteps) // should fail already in `step` and never here
            assertEquals(checkedSteps, transSteps.size, "Some steps have not been checked")
        }
    }
}

@TestCaseBuilderMarker
class PlanTestCase {
    var context: Context = emptyContext
    lateinit var plan: Plan
    lateinit var inputExpr: String

    fun check(assert: TransformationCheck.() -> Unit) {
        val expr = parseExpression(inputExpr)
        val trans = plan.tryExecute(context, Subexpression(RootPath, expr))

        val check = TransformationCheck(trans)
        check.assert()
        check.finalize()
    }
}

fun testPlan(init: PlanTestCase.() -> Unit) {
    val testCase = PlanTestCase()
    testCase.init()
}
