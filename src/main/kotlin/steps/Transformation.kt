package steps

import expressionmakers.MappedExpression
import expressions.RootPath
import expressions.Subexpression

data class Transformation(
    val fromExpr: Subexpression,
    val toExpr: MappedExpression,
    val steps: List<Transformation>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList(),
) {
    fun prettyPrint(prefix: String = "") {
        println("$prefix{")
        println("$prefix  fromExpr: \"$fromExpr\",")
        println("$prefix  toExpr: \"${toExpr.expr}\",")
        if (explanation != null) {
            println("$prefix  explanation: \"$explanation\"")
        }
        println("$prefix  pathMappings: [")
        for (mapping in toExpr.mappings.pathMappings(RootPath)) {
            println("$prefix    ${mapping.type}  ${mapping.fromPaths} -> ${mapping.toPath}")
        }
        println("$prefix ]")
        if (steps != null) {
            println("$prefix  substeps: [")
            for (step in steps) {
                step.prettyPrint("$prefix  ")
            }
            println("$prefix  ]")
        }
        println("$prefix}")
    }
}
