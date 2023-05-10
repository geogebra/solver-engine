package engine.expressions

import engine.operators.SetOperators
import engine.operators.SolutionOperator

interface Solution {
    val solutionVariables: VariableList
    val solutionVariable get() = solutionVariables.variableExpressions.single().variableName
}

class Identity(
    solutionVariables: VariableList,
    identityExpression: Expression,
    meta: NodeMeta = BasicMeta(),
) : Solution, Expression(SolutionOperator.Identity, listOf(solutionVariables, identityExpression), meta) {
    override val solutionVariables = firstChild as VariableList
    val identityExpression = secondChild
}

class Contradiction(
    solutionVariables: VariableList,
    contradictionExpression: Expression,
    meta: NodeMeta = BasicMeta(),
) : Solution, Expression(SolutionOperator.Contradiction, listOf(solutionVariables, contradictionExpression), meta) {
    override val solutionVariables = firstChild as VariableList
    val contradictionExpression = secondChild
}

class ImplicitSolution(
    solutionVariables: VariableList,
    implicitRelation: Expression,
    meta: NodeMeta = BasicMeta(),
) : Solution, Expression(SolutionOperator.ImplicitSolution, listOf(solutionVariables, implicitRelation), meta) {
    override val solutionVariables = firstChild as VariableList
    val implicitRelation = secondChild
}

class SetSolution(
    solutionVariables: VariableList,
    solutionSet: Expression,
    meta: NodeMeta = BasicMeta(),
) : Solution, Expression(SolutionOperator.SetSolution, listOf(solutionVariables, solutionSet), meta) {
    override val solutionVariables = firstChild as VariableList

    val solutionSet = secondChild
    val solution get() = secondChild.children.single()

    /**
     * Turn a unique solution into an equation x = k
     */
    fun asEquation(): Expression? {
        val variable = when (firstChild.childCount) {
            1 -> firstChild.firstChild
            else -> return null
        }

        return when (secondChild.operator) {
            SetOperators.FiniteSet -> {
                if (secondChild.childCount == 1) equationOf(variable, secondChild.firstChild) else null
            }
            else -> null
        }
    }
}
