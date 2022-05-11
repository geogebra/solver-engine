package steps

import expressions.Expression
import expressions.Path
import expressions.Subexpression

data class Transformation(
    val path: Path,
    val fromExpr: Expression,
    val toExpr: Expression,
    val pathMappings: List<PathMapping>,
    val steps: List<Transformation>? = null,
) {
    fun prettyPrint(prefix: String = "") {
        println("$prefix- path: $path")
        println("$prefix  fromExpr: $fromExpr")
        println("$prefix  toExpr: $toExpr")
        println("$prefix  pathMappings: [${
            pathMappings.joinToString("") { "\n$prefix    ${it.type} ${it.fromPath} => ${it.toPath}" }
        }\n$prefix  ]")
        if (steps != null) {
            println("$prefix  substeps:")
            for (step in steps) {
                step.prettyPrint("$prefix  ")
            }
        }
    }

    val fromSubexpr: Subexpression get() = Subexpression(path, fromExpr)
    val toSubexpr: Subexpression get() = Subexpression(path, toExpr)
}

interface Step {
    fun fromExpr(): Expression
    fun toExpr(): Expression
    fun pathMappings(): List<PathMapping>
}

data class TransformationStep(val expr: Expression, val transformation: Transformation) : Step {
    override fun fromExpr() = expr
    override fun toExpr() = expr.substitute(Subexpression(transformation.path, transformation.toExpr))
    override fun pathMappings() = transformation.pathMappings
}

data class GroupedStep(val innerSteps: List<Step>) : Step {
    override fun fromExpr() = innerSteps.first().fromExpr()
    override fun toExpr() = innerSteps.last().toExpr()

    override fun pathMappings(): List<PathMapping> {
        val pathMappings = mutableListOf<PathMapping>()

        for (innerStep in innerSteps) {
            for (innerPathMapping in innerStep.pathMappings()) {

            }
        }

        return pathMappings
    }
}