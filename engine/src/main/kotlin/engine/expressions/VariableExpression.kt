package engine.expressions

import engine.operators.VariableListOperator
import engine.operators.VariableOperator

class Variable(
    val variableName: String,
    meta: NodeMeta = BasicMeta(),
) : Expression(VariableOperator(variableName), listOf(), meta)

class VariableList(
    variables: List<Variable>,
    meta: NodeMeta = BasicMeta(),
) : Expression(VariableListOperator, variables, meta) {
    val variableExpressions get() = children.map { it as Variable }
}
