package plans

import context.Context
import context.emptyContext
import expressions.*
import parser.parseExpression
import steps.Transformation
import steps.metadata.Metadata
import steps.metadata.MetadataKey
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

open class PathMappingsCheck {
    var pathMappings: MutableList<PathMapping>? = null

    private fun addPathMapping(type: PathMappingType, init: PathMappingPathsBuilder.() -> Unit) {
        if (pathMappings == null) {
            pathMappings = mutableListOf()
        }

        val builder = PathMappingPathsBuilder(type)
        builder.init()
        pathMappings?.add(builder.getPathMapping())
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

    fun checkPathMappings(mappings: PathMappingTree?, rootPath: Path) {
        if (pathMappings != null) {
            assertNotNull(mappings)
            assertContentEquals(
                pathMappings,
                mappings.pathMappings(RootPath).map { it.relativeTo(rootPath) }.toList()
            )
        }
    }
}

class MappedExpressionCheck : PathMappingsCheck() {
    lateinit var expr: String

    fun checkMappedExpression(mappedExpression: MappedExpression, rootPath: Path) {
        assertEquals(parseExpression(expr), mappedExpression.expr)
        checkPathMappings(mappedExpression.mappings, rootPath)
    }
}

class MetadataCheck {
    lateinit var key: MetadataKey
    var params: MutableList<MappedExpressionCheck>? = null

    fun param(init: MappedExpressionCheck.() -> Unit) {
        val param = MappedExpressionCheck()
        param.init()
        if (params == null) {
            params = mutableListOf(param)
        } else {
            params!!.add(param)
        }
    }

    fun checkMetadata(metadata: Metadata, rootPath: Path) {
        assertEquals(key, metadata.key)
        if (params != null) {
            assertEquals(params!!.size, metadata.mappedParams.size)
            for ((checker, mappedExpr) in params!!.zip(metadata.mappedParams)) {
                checker.checkMappedExpression(mappedExpr, rootPath)
            }
        }
    }

    fun checkMetadataMappedExpression(mappedExpression: MappedExpression?, rootPath: Path) {
        assertNotNull(mappedExpression)
        val operator = mappedExpression.expr.operator as MetadataOperator
        val paramExprs = mappedExpression.expr.operands
        val paramMappings = mappedExpression.mappings.childList(paramExprs.size)
        val metadata = Metadata(
            operator.key,
            paramExprs.zip(paramMappings).map { (expr, mappings) -> MappedExpression(expr, mappings) })
        checkMetadata(metadata, rootPath)

    }
}

class TransformationCheck : PathMappingsCheck() {
    var fromExpr: String? = null
    var toExpr: String? = null
    var steps: MutableList<TransformationCheck>? = null
    private var explanationCheck: MetadataCheck? = null
    private var skillChecks = mutableMapOf<Operator, MetadataCheck>()

    var nullTransformation = false

    fun noTransformation() {
        nullTransformation = true
    }

    fun step(init: TransformationCheck.() -> Unit) {
        val stepCheck = TransformationCheck()
        stepCheck.init()
        if (steps == null) {
            steps = mutableListOf(stepCheck)
        } else {
            steps!!.add(stepCheck)
        }
    }

    fun explanation(init: MetadataCheck.() -> Unit) {
        explanationCheck = MetadataCheck()
        explanationCheck!!.init()
    }

    fun skill(init: MetadataCheck.() -> Unit) {
        val skillCheck = MetadataCheck()
        skillCheck.init()
        skillChecks[MetadataOperator(skillCheck.key)] = skillCheck
    }

    fun checkTransformation(trans: Transformation?) {
        if (nullTransformation) {
            assertNull(trans)
            return
        }
        assertNotNull(trans)
        if (fromExpr != null) {
            assertEquals(parseExpression(fromExpr!!), trans.fromExpr.expr)
        }
        if (toExpr != null) {
            assertEquals(parseExpression(toExpr!!), trans.toExpr.expr)
        }
        if (steps != null) {
            val transSteps = trans.steps
            assertNotNull(transSteps)
            assertEquals(steps!!.size, transSteps.size)
            for ((s, t) in steps!!.zip(transSteps)) {
                s.checkTransformation(t)
            }
        }
        checkPathMappings(trans.toExpr.mappings, trans.fromExpr.path)
        if (explanationCheck != null) {
            explanationCheck!!.checkMetadataMappedExpression(trans.explanation, trans.fromExpr.path)
        }
        if (skillChecks.isNotEmpty()) {
            assertEquals(skillChecks.size, trans.skills.size)
            for (skill in trans.skills) {
                val skillCheck = skillChecks[skill.expr.operator]
                assertNotNull(skillCheck)
                skillCheck.checkMetadataMappedExpression(skill, trans.fromExpr.path)
            }
        }
    }
}

fun makeCheck(init: TransformationCheck.() -> Unit): TransformationCheck {
    val check = TransformationCheck()
    check.init()
    return check
}

fun testPlan(init: PlanTestCase.() -> Unit) {
    val testCase = PlanTestCase()
    testCase.init()
    testCase.assert()
}

class PlanTestCase {
    lateinit var plan: Plan
    var context: Context = emptyContext
    lateinit var inputExpr: String
    lateinit var checkTrans: TransformationCheck

    fun check(init: TransformationCheck.() -> Unit) {
        checkTrans = makeCheck(init)
    }

    fun assert() {
        val expr = parseExpression(inputExpr)
        val trans = plan.tryExecute(context, Subexpression(RootPath, expr))
        checkTrans.checkTransformation(trans)
        trans?.prettyPrint()
    }
}