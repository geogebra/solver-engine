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
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList(),
) {
    fun prettyPrint(prefix: String = "") {
        println("$prefix- path: $path")
        println("$prefix  fromExpr: $fromExpr")
        println("$prefix  toExpr: $toExpr")
        if (explanation != null) {
            println("$prefix  explanation: $explanation")
        }
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
