package plans

import context.Context
import context.emptyContext
import expressions.*
import parser.parseExpression
import steps.Transformation
import steps.metadata.Explanation
import steps.metadata.Metadata
import steps.metadata.MetadataKey
import steps.metadata.PlanExplanation
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun parsePath(s: String): Path {
    val pieces = s.split('/')
    if (pieces[0] != ".") throw IllegalArgumentException("$s is not a valid path")

    var path: Path = RootPath
    for (piece in pieces.drop(1)) {
        path = path.child(piece.toInt())
    }

    return path
}

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
}

class TransformationCheck : PathMappingsCheck() {
    var fromExpr: String? = null
    var toExpr: String? = null
    var steps: MutableList<TransformationCheck>? = null
    private var explanationCheck: MetadataCheck? = null

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

    fun checkTransformation(trans: Transformation?) {
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
            val operator = trans.explanation!!.expr.operator as MetadataOperator
            val paramExprs = trans.explanation!!.expr.operands
            val paramMappings = trans.explanation!!.mappings.childList(paramExprs.size)
            val metadata = Metadata(
                operator.key,
                paramExprs.zip(paramMappings).map { (expr, mappings) -> MappedExpression(expr, mappings) })
            explanationCheck!!.checkMetadata(metadata, trans.fromExpr.path)
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

class TestSimplifyIntegerExpression {

    @Test
    fun simpleTest() = testPlan {
        plan = simplifyArithmeticExpression
        inputExpr = "1 + 2 + 3"

        check {
            toExpr = "6"
            step {
                step {
                    step {
                        fromExpr = "1 + 2 + 3"
                        toExpr = "3 + 3"

                        combine {
                            fromPaths("./0", "./1")
                            toPaths("./0")
                        }

                        move {
                            fromPaths("./2")
                            toPaths("./1")
                        }
                    }
                    step { toExpr = "6" }
                }
            }
        }
    }

    @Test
    fun testAddMultiplyDivide() = testPlan {
        plan = simplifyArithmeticExpression
        inputExpr = "3*4*5:6 + 6 + 7"

        check {
            toExpr = "23"

            step {
                toExpr = "10 + 6 + 7"

                step {
                    fromExpr = "3*4*5:6"
                    toExpr = "10"

                    step { toExpr = "12*5:6" }
                    step { toExpr = "60:6" }
                    step { toExpr = "10" }
                }
            }

            step {
                toExpr = "23"

                step {
                    toExpr = "23"

                    step { toExpr = "16 + 7" }
                    step { toExpr = "23" }
                }
            }
        }
    }

    @Test
    fun testBracketsAndNegativeMultiply() = testPlan {
        plan = simplifyArithmeticExpression
        inputExpr = "34 + 60 + 6 - (4 + 10 - 3 * 5 * (-2))"


        check {
            toExpr = "56"

            explanation {
                key = PlanExplanation.SimplifyArithmeticExpression
            }

            step {
                fromExpr = "34 + 60 + 6 - (4 + 10 - 3 * 5 * (-2))"
                toExpr = "34 + 60 + 6 - (4 + 10 - (-30))"

                step {
                    fromExpr = "3 * 5 * (-2)"
                    toExpr = "(-30)"

                    explanation {
                        key = PlanExplanation.SimplifyIntegerProduct
                    }

                    step {
                        fromExpr = "3 * 5 * (-2)"
                        toExpr = "15 * (-2)"

                        combine {
                            fromPaths("./0", "./1")
                            toPaths("./0")
                        }

                        move {
                            fromPaths("./2")
                            toPaths("./1")
                        }
                    }
                    step {
                        fromExpr = "15 * (-2)"
                        toExpr = "(-30)"
                    }
                }
            }

            step {
                fromExpr = "34 + 60 + 6 - (4 + 10 - (-30))"
                toExpr = "34 + 60 + 6 - (4 + 10 + 30)"

                step {
                    fromExpr = "-(-30)"
                    toExpr = "30"

                    explanation {
                        key = Explanation.SimplifyDoubleMinus
                    }
                }
            }

            step {
                fromExpr = "34 + 60 + 6 - (4 + 10 + 30)"
                toExpr = "34 + 60 + 6 - (44)"

                step {
                    fromExpr = "4 + 10 + 30"
                    toExpr = "44"

                    explanation {
                        key = PlanExplanation.SimplifyIntegerSum
                    }
                }
            }

            step {
                step {
                    fromExpr = "(44)"
                    toExpr = "44"

                    explanation {
                        key = Explanation.RemoveBracketUnsignedInteger
                    }
                }
            }

            step {

                step {
                    fromExpr = "34 + 60 + 6 - 44"
                    toExpr = "56"

                    explanation {
                        key = PlanExplanation.SimplifyIntegerSum
                    }
                }
            }
        }
    }

    @Test
    fun testPowerAndBrackets() = testPlan {
        plan = simplifyArithmeticExpression
        inputExpr = "[(1 + 1) ^ [2 ^ 3]]"

        check {
            toExpr = "256"

            step {
                fromExpr = "[(1 + 1) ^ [2 ^ 3]]"
                toExpr = "[(2) ^ [2 ^ 3]]"

                step {
                    fromExpr = "1 + 1"
                    toExpr = "2"
                }
            }

            step {
                fromExpr = "[(2) ^ [2 ^ 3]]"
                toExpr = "[2 ^ [2 ^ 3]]"

                step {
                    fromExpr = "(2)"
                    toExpr = "2"
                }
            }

            step {
                fromExpr = "[2 ^ [2 ^ 3]]"
                toExpr = "[2 ^ 8]"

                step {
                    fromExpr = "[2 ^ 3]"
                    toExpr = "8"
                }
            }

            step {
                fromExpr = "[2 ^ 8]"
                toExpr = "256"

                step {
                    fromExpr = "[2 ^ 8]"
                    toExpr = "256"
                }
            }
        }
    }
}
