package engine.expressions

import engine.operators.NameOperator
import engine.operators.VariableListOperator
import engine.operators.VariableOperator
import engine.sign.Sign

class Variable(
    val variableName: String,
    meta: NodeMeta = BasicMeta(),
) : Expression(VariableOperator(variableName), listOf(), meta) {
    override fun signOf() = Sign.UNKNOWN

    override fun fillJson2(s: MutableMap<String, Any>) {
        s["type"] = "Variable"
        s["value"] = variableName
    }
}

class VariableList(
    variables: List<Variable>,
    meta: NodeMeta = BasicMeta(),
) : Expression(VariableListOperator, variables, meta) {
    val variableExpressions get() = children.map { it as Variable }
}

class Name(
    val text: String,
    meta: NodeMeta = BasicMeta(),
) : Expression(NameOperator(text), listOf(), meta) {
    override fun fillJson2(s: MutableMap<String, Any>) {
        s["type"] = "Name"
        s["value"] = text
    }
}
