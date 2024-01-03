package engine.graphing

import engine.expressions.Comparison
import engine.expressions.Expression
import engine.expressions.Variable

/**
 * Specifies the name for the horizontal and vertical axis variables in a Cartesian 2D graph.
 */
data class AxisVariables(
    val horizontal: String,
    val vertical: String,
)

/**
 * Decides what are suitable axes for the given [exprs].  Will return null if it can't decide or it's not appropriate
 * (e.g. the expressions are constant, or there are more than 2 variables).œ
 */
fun selectAxisVariables(allVars: Set<String>, solutionVars: List<String>, exprs: List<Expression>): AxisVariables? {
    return when {
        allVars.isEmpty() -> null
        allVars.size == 1 -> {
            val theVar = allVars.first()
            if (theVar == "y") {
                AxisVariables("x", "y")
            } else {
                AxisVariables(theVar, "y")
            }
        }
        allVars.size == 2 -> {
            // We have two variables in total.
            when {
                allVars == setOf("x", "y") -> {
                    // 1. Special case - if the variables are x and y then always have x as the horizontal axis.
                    AxisVariables("x", "y")
                }
                solutionVars.size == 1 -> {
                    // 2. If there is one solution variable, it will be the dependent variable (vertical axis).
                    val vertVar = solutionVars.first()
                    val horizVar = allVars.first { it != vertVar }
                    AxisVariables(horizVar, vertVar)
                }
                else -> {
                    // 3. If there is one variable which is the subject of the equation, we use it for the dependent
                    // variable (vertical axis).
                    val subjectVars = getSubjectVars(exprs)
                    if (subjectVars.size == 1) {
                        val vertVar = subjectVars.first()
                        val horizVar = allVars.first { it != vertVar }
                        AxisVariables(horizVar, vertVar)
                    } else {
                        val varsInOrder = allVars.sorted()
                        AxisVariables(varsInOrder[0], varsInOrder[1])
                    }
                }
            }
        }
        else -> null
    }
}

private fun getSubjectVars(exprs: List<Expression>): Set<String> {
    var subjectVars = mutableSetOf<String>()
    for (expr in exprs) {
        if (expr is Comparison) {
            if (expr.lhs is Variable) {
                subjectVars.addAll(expr.lhs.variables)
            }
            if (expr.rhs is Variable) {
                subjectVars.addAll(expr.rhs.variables)
            }
        }
    }
    return subjectVars
}
