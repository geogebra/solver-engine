package steps

import expressions.Expression

data class Step(
    val fromExpr: Expression,
    val toExpr: Expression,
//    val transformation: Transformation,
    val pathMappings: List<PathMapping>,
//    val skills: List<Skill>

) {

    fun prettyPrint() {
        println("fromExpr: $fromExpr")
        println("toExpr: $toExpr")
        println("pathMappings: [${pathMappings.joinToString("") { "\n\t${it.type} ${it.fromPath} => ${it.toPath}"}}\n]")
    }
}
